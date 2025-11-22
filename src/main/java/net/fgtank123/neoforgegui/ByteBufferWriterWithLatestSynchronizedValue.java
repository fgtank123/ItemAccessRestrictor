package net.fgtank123.neoforgegui;

import net.minecraft.network.RegistryFriendlyByteBuf;

import javax.annotation.Nullable;

@FunctionalInterface
public interface ByteBufferWriterWithLatestSynchronizedValue<V> {
    void writeValue(RegistryFriendlyByteBuf byteBuf, V value, @Nullable V latestSynchronizedValue);
}
