package net.fgtank123.neoforgegui;

import net.minecraft.network.RegistryFriendlyByteBuf;

@FunctionalInterface
public interface ByteBufferWriter<V> {
    void writeValue(RegistryFriendlyByteBuf byteBuf, V value);
}
