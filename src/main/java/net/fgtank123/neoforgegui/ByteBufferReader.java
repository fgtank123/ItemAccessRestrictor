package net.fgtank123.neoforgegui;

import net.minecraft.network.RegistryFriendlyByteBuf;

@FunctionalInterface
public interface ByteBufferReader<V> {
    V readValue(RegistryFriendlyByteBuf byteBuf);
}
