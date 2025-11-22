package net.fgtank123.minecraft.guidatasync;

import net.minecraft.network.RegistryFriendlyByteBuf;

@FunctionalInterface
public interface ByteBufferReader<V> {
    V readValue(RegistryFriendlyByteBuf byteBuf);
}
