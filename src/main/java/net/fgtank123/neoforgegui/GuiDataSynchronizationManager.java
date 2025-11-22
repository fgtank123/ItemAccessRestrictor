package net.fgtank123.neoforgegui;

import com.mojang.logging.LogUtils;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import org.slf4j.Logger;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.IntStream;

/**
 * 通过{@link  GuiDataSynchronizationPacketPayload#register}方法注册网络包类型后，
 * 才可使用{@link GuiDataSynchronizationManager}
 */
public class GuiDataSynchronizationManager {
    protected static final WeakHashMap<AbstractContainerMenu, GuiDataSynchronizationManager> registeredManagers = new WeakHashMap<>();
    private final int hostId;
    private final Player player;
    @SuppressWarnings("rawtypes")
    private final List<RegisteredValueRef> registeredValueRefs = new ArrayList<>();
    private final List<Object> latestSynchronizedValues = new ArrayList<>();
    private static final Logger LOGGER = LogUtils.getLogger();

    public GuiDataSynchronizationManager(AbstractContainerMenu menu, Player player) {
        if (registeredManagers.containsKey(menu)) {
            throw new IllegalArgumentException("This manager has already been registered to the menu.");
        }
        this.hostId = menu.containerId;
        this.player = player;
        registeredManagers.put(menu, this);
    }

    private static class RegisteredValueRef<V> extends ValueRef<V> {
        private final ByteBufferWriterWithLatestSynchronizedValue<V> writer;
        private final ByteBufferReader<V> reader;
        private final BiPredicate<V, V> equalsChecker;

        public RegisteredValueRef(
            Supplier<V> getter,
            Consumer<V> setter,
            ByteBufferWriterWithLatestSynchronizedValue<V> writer,
            ByteBufferReader<V> reader,
            BiPredicate<V, V> equalsChecker
        ) {
            super(getter, setter);
            this.writer = writer;
            this.reader = reader;
            this.equalsChecker = equalsChecker;
        }

        public void writeToByteBuffer(RegistryFriendlyByteBuf byteBuf, V value, @Nullable V latestSynchronizedValue) {
            writer.writeValue(byteBuf, value, latestSynchronizedValue);
        }

        public V readFromByteBuffer(RegistryFriendlyByteBuf byteBuf) {
            return reader.readValue(byteBuf);
        }

        @SuppressWarnings("BooleanMethodIsAlwaysInverted")
        public boolean equals(V v1, V v2) {
            return equalsChecker.test(v1, v2);
        }
    }

    private boolean isServerSide() {
        return player instanceof ServerPlayer;
    }

    private static final Map<Class<?>, ByteBufferCodec<?>> basicClassCodecMap = new HashMap<>();

    static {
        ByteBufferCodec<Integer> integerCodec = new ByteBufferCodec<>(
            FriendlyByteBuf::writeInt,
            FriendlyByteBuf::readInt
        );
        basicClassCodecMap.put(int.class, integerCodec);
        basicClassCodecMap.put(Integer.class, integerCodec);

        ByteBufferCodec<Long> longCodec = new ByteBufferCodec<>(
            FriendlyByteBuf::writeLong,
            FriendlyByteBuf::readLong
        );
        basicClassCodecMap.put(long.class, longCodec);
        basicClassCodecMap.put(Long.class, longCodec);

        ByteBufferCodec<Short> shortCodec = new ByteBufferCodec<>(
            (RegistryFriendlyByteBuf byteBuf, Short value) -> byteBuf.writeShort(value),
            FriendlyByteBuf::readShort
        );
        basicClassCodecMap.put(short.class, shortCodec);
        basicClassCodecMap.put(Short.class, shortCodec);

        ByteBufferCodec<Byte> byteCodec = new ByteBufferCodec<>(
            FriendlyByteBuf::writeByte,
            FriendlyByteBuf::readByte
        );
        basicClassCodecMap.put(byte.class, byteCodec);
        basicClassCodecMap.put(Byte.class, byteCodec);

        ByteBufferCodec<Boolean> booleanCodec = new ByteBufferCodec<>(
            FriendlyByteBuf::writeBoolean,
            FriendlyByteBuf::readBoolean
        );
        basicClassCodecMap.put(boolean.class, booleanCodec);
        basicClassCodecMap.put(Boolean.class, booleanCodec);

        ByteBufferCodec<Float> floatCodec = new ByteBufferCodec<>(
            FriendlyByteBuf::writeFloat,
            FriendlyByteBuf::readFloat
        );
        basicClassCodecMap.put(float.class, floatCodec);
        basicClassCodecMap.put(Float.class, floatCodec);

        ByteBufferCodec<Double> doubleCodec = new ByteBufferCodec<>(
            FriendlyByteBuf::writeDouble,
            FriendlyByteBuf::readDouble
        );
        basicClassCodecMap.put(double.class, doubleCodec);
        basicClassCodecMap.put(Double.class, doubleCodec);

        basicClassCodecMap.put(String.class, new ByteBufferCodec<>(
            (byteBuf, string) -> byteBuf.writeUtf(string),
            FriendlyByteBuf::readUtf
        ));

        basicClassCodecMap.put(int[].class, new ByteBufferCodec<>(
            FriendlyByteBuf::writeVarIntArray,
            FriendlyByteBuf::readVarIntArray
        ));

        basicClassCodecMap.put(long[].class, new ByteBufferCodec<>(
            FriendlyByteBuf::writeLongArray,
            FriendlyByteBuf::readLongArray
        ));

        basicClassCodecMap.put(byte[].class, new ByteBufferCodec<>(
            (RegistryFriendlyByteBuf byteBuf, byte[] array) -> byteBuf.writeByteArray(array),
            (RegistryFriendlyByteBuf byteBuf) -> byteBuf.readByteArray()
        ));

        basicClassCodecMap.put(boolean[].class, new ByteBufferCodec<>(
            (RegistryFriendlyByteBuf byteBuf, boolean[] value) -> {
                BitSet bitSet = new BitSet(value.length);
                for (int i = 0; i < value.length; i++) {
                    if (value[i]) {
                        bitSet.set(i);
                    }
                }
                byteBuf.writeVarInt(value.length);
                byteBuf.writeBitSet(bitSet);
            },
            (RegistryFriendlyByteBuf byteBuf) -> {
                int length = byteBuf.readVarInt();
                BitSet bitSet = byteBuf.readBitSet();
                boolean[] value = new boolean[length];
                for (int i = 0; i < length; i++) {
                    value[i] = bitSet.get(i);
                }
                return value;
            }
        ));

        basicClassCodecMap.put(ResourceLocation.class, new ByteBufferCodec<>(
            (RegistryFriendlyByteBuf byteBuf, ResourceLocation value) -> {
                if (value == null) {
                    byteBuf.writeBoolean(false);
                } else {
                    byteBuf.writeBoolean(true);
                    byteBuf.writeResourceLocation(value);
                }
            },
            byteBuf -> {
                if (byteBuf.readBoolean()) {
                    return byteBuf.readResourceLocation();
                } else {
                    return null;
                }
            }
        ));

    }

    @SuppressWarnings("unchecked")
    private <V> ByteBufferCodec<V> resolve(Class<V> valueClass) {
        if (basicClassCodecMap.containsKey(valueClass)) {
            return (ByteBufferCodec<V>) basicClassCodecMap.get(valueClass);
        } else if (valueClass.isEnum()) {
            Enum<?>[] enumConstants = valueClass.asSubclass(Enum.class).getEnumConstants();
            return (ByteBufferCodec<V>) new ByteBufferCodec<>(
                (RegistryFriendlyByteBuf byteBuf, Enum<?> value) -> {
                    int ordinal;
                    if (value == null) {
                        ordinal = -1;
                    } else {
                        ordinal = value.ordinal();
                    }
                    byteBuf.writeVarInt(ordinal);
                },
                byteBuf -> {
                    int ordinal = byteBuf.readVarInt();
                    if (ordinal == -1) {
                        return null;
                    } else {
                        return enumConstants[ordinal];
                    }
                }
            );
        } else if (valueClass.isAssignableFrom(Component.class)) {
            return (ByteBufferCodec<V>) new ByteBufferCodec<>(
                (RegistryFriendlyByteBuf byteBuf, Component value) -> {
                    if (value == null) {
                        byteBuf.writeBoolean(false);
                    } else {
                        byteBuf.writeBoolean(true);
                        ComponentSerialization.TRUSTED_STREAM_CODEC.encode(byteBuf, value);
                    }
                },
                byteBuf -> {
                    if (byteBuf.readBoolean()) {
                        return ComponentSerialization.TRUSTED_STREAM_CODEC.decode(byteBuf);
                    } else {
                        return null;
                    }
                }
            );
        } else {
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    public <V> ValueRef<V> register(Supplier<V> getter, Consumer<V> setter) {
        V defaultValue = getter.get();
        if (defaultValue == null) {
            throw new IllegalArgumentException("default value can not be null");
        }
        Class<?> valueClass = defaultValue.getClass();
        return register(getter, setter, (Class<V>) valueClass);
    }

    public <V> ValueRef<V> register(Supplier<V> getter, Consumer<V> setter, Class<V> valueClass) {
        ByteBufferCodec<V> resolved = resolve(valueClass);
        if (resolved == null) {
            throw new IllegalArgumentException("value class [" + valueClass.getSimpleName() + "] need custom ByteBufferWriter and ByteBufferReader");
        }
        return register(getter, setter, resolved, resolved, Objects::deepEquals);
    }

    public <V> ValueRef<V> register(
        Supplier<V> getter,
        Consumer<V> setter,
        ByteBufferWriterWithLatestSynchronizedValue<V> writer,
        ByteBufferReader<V> reader,
        BiPredicate<V, V> equalsChecker
    ) {
        registeredValueRefs.add(new RegisteredValueRef<>(getter, setter, writer, reader, equalsChecker));
        int index = registeredValueRefs.size() - 1;
        latestSynchronizedValues.add(null);
        if (isServerSide()) {
            return new ValueRef<>(
                getter,
                setter
            );
        } else {
            return new ValueRef<>(
                getter,
                v -> {
                    // 向服务端发送数据
                    if (player instanceof LocalPlayer localPlayer) {
                        localPlayer.connection.send(new GuiDataSynchronizationPacketPayload(
                            hostId,
                            byteBuf -> {
                                byteBuf.writeVarInt(index);
                                writer.writeValue(byteBuf, v, getter.get());
                                terminate(byteBuf);
                            },
                            registryAccess()
                        ));
                    }
                }
            );
        }
    }

    public <V> ValueRef<List<V>> registerList(
        Supplier<List<V>> getter,
        Consumer<List<V>> setter,
        ByteBufferWriter<V> elementWriter,
        ByteBufferReader<V> elementReader,
        BiPredicate<V, V> elementEqualsChecker
    ) {
        int fullUpdateFlag = 1;
        int incrementalUpdateFlag = 2;
        int endFlag = -1;
        return register(
            getter,
            setter,
            (byteBuf, values, latestSynchronizedValue) -> {
                if (latestSynchronizedValue == null || values.size() != latestSynchronizedValue.size()) {
                    byteBuf.writeVarInt(fullUpdateFlag);
                    for (int i = 0; i < values.size(); i++) {
                        byteBuf.writeVarInt(i);
                        V value = values.get(i);
                        elementWriter.writeValue(byteBuf, value);
                    }
                    byteBuf.writeVarInt(endFlag);
                } else {
                    byteBuf.writeVarInt(incrementalUpdateFlag);
                    for (int i = 0; i < values.size(); i++) {
                        V value = values.get(i);
                        if (!elementEqualsChecker.test(value, latestSynchronizedValue.get(i))) {
                            byteBuf.writeVarInt(i);
                            elementWriter.writeValue(byteBuf, value);
                        }
                    }
                    byteBuf.writeVarInt(endFlag);
                }
            },
            byteBuf -> {
                int flag = byteBuf.readVarInt();
                List<V> values;
                if (flag == fullUpdateFlag) {
                    values = new ArrayList<>();
                } else {
                    List<V> getValues = getter.get();
                    values = getValues == null ? new ArrayList<>() : new ArrayList<>(getValues);
                }
                int index = byteBuf.readVarInt();
                while (index != endFlag) {
                    V value = elementReader.readValue(byteBuf);
                    setWithExpansion(values, index, value);
                    index = byteBuf.readVarInt();
                }
                return values;
            },
            (values1, values2) -> {
                if ((values1 == null) != (values2 == null)) {
                    return false;
                }
                if (values1 == null) {
                    return true;
                }
                if (values1.size() != values2.size()) {
                    return false;
                }
                for (int i = 0; i < values1.size(); i++) {
                    if (!elementEqualsChecker.test(values1.get(i), values2.get(i))) {
                        return false;
                    }
                }
                return true;
            }
        );
    }

    private static <E> void setWithExpansion(List<E> list, int index, E element) {
        if (index >= list.size()) {
            // 扩充列表
            for (int i = list.size(); i <= index; i++) {
                list.add(null);
            }
        }
        list.set(index, element);
    }

    private RegistryAccess registryAccess() {
        //noinspection resource
        return player.level().registryAccess();
    }

    private void terminate(RegistryFriendlyByteBuf byteBuf) {
        byteBuf.writeVarInt(-1);
    }

    private boolean notTerminated(int index) {
        return index != -1;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public void broadcastChanges() {
        if (isServerSide()) {
            ServerPlayer serverPlayer = (ServerPlayer) player;
            if (IntStream.range(0, registeredValueRefs.size()).anyMatch(index -> {
                RegisteredValueRef registeredValueRef = registeredValueRefs.get(index);
                Object latestSynchronizedValue = latestSynchronizedValues.get(index);
                Object value = registeredValueRef.get();
                boolean asd = !registeredValueRef.equals(value, latestSynchronizedValue);
                return asd;
            })) {
                serverPlayer.connection.send(new GuiDataSynchronizationPacketPayload(
                    hostId,
                    byteBuf -> {
                        for (int index = 0; index < registeredValueRefs.size(); index++) {
                            RegisteredValueRef registeredValueRef = registeredValueRefs.get(index);
                            Object latestSynchronizedValue = latestSynchronizedValues.get(index);
                            Object value = registeredValueRef.get();
                            if (!registeredValueRef.equals(value, latestSynchronizedValue)) {
                                byteBuf.writeVarInt(index);
                                registeredValueRef.writeToByteBuffer(byteBuf, value, latestSynchronizedValue);
                                latestSynchronizedValues.set(index, value);
                            }
                        }
                        terminate(byteBuf);
                    },
                    registryAccess()
                ));
            }
        }
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private void doReceiveSynchronizationData(RegistryFriendlyByteBuf byteBuf) {
        for (int index = byteBuf.readVarInt(); notTerminated(index) && registeredValueRefs.size() > index; index = byteBuf.readVarInt()) {
            RegisteredValueRef registeredValueRef = registeredValueRefs.get(index);
            Object value = registeredValueRef.readFromByteBuffer(byteBuf);
            registeredValueRef.set(value);
        }
    }

    void receiveServerSynchronizationData(RegistryFriendlyByteBuf byteBuf) {
        try {
            doReceiveSynchronizationData(byteBuf);
        } catch (Exception e) {
            LOGGER.warn("receive server synchronization data failed", e);
        }
    }

    void receiveClientSynchronizationData(RegistryFriendlyByteBuf byteBuf) {
        try {
            doReceiveSynchronizationData(byteBuf);
            broadcastChanges();
        } catch (Exception e) {
            LOGGER.warn("receive client synchronization data failed", e);
        }
    }

}
