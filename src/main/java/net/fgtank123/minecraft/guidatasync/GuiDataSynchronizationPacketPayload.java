package net.fgtank123.minecraft.guidatasync;

import io.netty.buffer.Unpooled;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.neoforged.neoforge.network.connection.ConnectionType;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

import javax.annotation.Nonnull;
import java.util.function.Consumer;

public record GuiDataSynchronizationPacketPayload(int containerId, byte[] data) implements CustomPacketPayload {
    private static CustomPacketPayload.Type<GuiDataSynchronizationPacketPayload> TYPE;

    public static void register(PayloadRegistrar registrar, String modId) {
        TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(modId, "gui_data_synchronization_packet_payload_type"));
        registrar.playBidirectional(
            TYPE,
            StreamCodec.ofMember(
                GuiDataSynchronizationPacketPayload::write,
                GuiDataSynchronizationPacketPayload::decode
            ),
            (payload, context) -> {
                if (context.flow().isClientbound()) {
                    context.enqueueWork(() -> payload.handleOnClient(context.player()));
                } else if (context.flow().isServerbound()) {
                    context.enqueueWork(() -> {
                        if (context.player() instanceof ServerPlayer serverPlayer) {
                            payload.handleOnServer(serverPlayer);
                        }
                    });
                }
            }
        );
    }

    @Nonnull
    @Override
    public CustomPacketPayload.Type<GuiDataSynchronizationPacketPayload> type() {
        return TYPE;
    }

    public GuiDataSynchronizationPacketPayload(int containerId, Consumer<RegistryFriendlyByteBuf> writer, RegistryAccess registryAccess) {
        this(containerId, createSynchronizationData(writer, registryAccess));
    }

    private static byte[] createSynchronizationData(Consumer<RegistryFriendlyByteBuf> writer, RegistryAccess registryAccess) {
        var buffer = new RegistryFriendlyByteBuf(Unpooled.buffer(), registryAccess, ConnectionType.OTHER);
        writer.accept(buffer);
        var result = new byte[buffer.readableBytes()];
        buffer.readBytes(result);
        return result;
    }

    public static GuiDataSynchronizationPacketPayload decode(RegistryFriendlyByteBuf data) {
        var containerId = data.readVarInt();
        var syncData = data.readByteArray();
        return new GuiDataSynchronizationPacketPayload(containerId, syncData);
    }

    public void write(RegistryFriendlyByteBuf byteBuf) {
        byteBuf.writeVarInt(this.containerId);
        byteBuf.writeByteArray(this.data);
    }

    public void handleOnClient(Player player) {
        AbstractContainerMenu c = player.containerMenu;
        if (c.containerId == this.containerId) {
            GuiDataSynchronizationManager guiDataSynchronizationManager = GuiDataSynchronizationManager.registeredManagers.get(c);
            if (guiDataSynchronizationManager != null) {
                guiDataSynchronizationManager.receiveServerSynchronizationData(new RegistryFriendlyByteBuf(
                    Unpooled.wrappedBuffer(this.data),
                    player.registryAccess(),
                    ConnectionType.OTHER
                ));
            }
        }
    }

    public void handleOnServer(ServerPlayer player) {
        AbstractContainerMenu c = player.containerMenu;
        if (c.containerId == this.containerId) {
            GuiDataSynchronizationManager guiDataSynchronizationManager = GuiDataSynchronizationManager.registeredManagers.get(c);
            if (guiDataSynchronizationManager != null) {
                guiDataSynchronizationManager.receiveClientSynchronizationData(new RegistryFriendlyByteBuf(
                    Unpooled.wrappedBuffer(this.data),
                    player.registryAccess(),
                    ConnectionType.OTHER
                ));
            }
        }
    }
}

