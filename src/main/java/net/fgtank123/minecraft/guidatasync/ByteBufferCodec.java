package net.fgtank123.minecraft.guidatasync;

import net.minecraft.network.RegistryFriendlyByteBuf;

import javax.annotation.Nullable;

@SuppressWarnings("ClassCanBeRecord")
public class ByteBufferCodec<V> implements ByteBufferWriterWithLatestSynchronizedValue<V>, ByteBufferReader<V> {
    private final ByteBufferWriterWithLatestSynchronizedValue<V> writer;
    private final ByteBufferReader<V> reader;

    public ByteBufferCodec(ByteBufferWriterWithLatestSynchronizedValue<V> writer, ByteBufferReader<V> reader) {
        this.writer = writer;
        this.reader = reader;
    }

    public ByteBufferCodec(ByteBufferWriter<V> writer, ByteBufferReader<V> reader) {
        this(
            (byteBuf, value, latestSynchronizedValue) -> writer.writeValue(byteBuf, value),
            reader
        );
    }


    @Override
    public void writeValue(RegistryFriendlyByteBuf byteBuf, V value, @Nullable V latestSynchronizedValue) {
        writer.writeValue(byteBuf, value, latestSynchronizedValue);
    }

    @Override
    public V readValue(RegistryFriendlyByteBuf byteBuf) {
        return reader.readValue(byteBuf);
    }

}
