package net.fgtank123.nbtsetting;

import net.minecraft.nbt.Tag;

import javax.annotation.Nonnull;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;

public class NBTSettingDefinition<V> {
    private final String name;
    private final V defaultValue;
    private final Function<V, Tag> toTag;
    private final Function<Tag, V> fromTag;
    private final Predicate<V> valueValidator;

    NBTSettingDefinition(
        @Nonnull String name,
        @Nonnull V defaultValue,
        @Nonnull Function<V, Tag> toTag,
        @Nonnull Function<Tag, V> fromTag,
        @Nonnull Predicate<V> valueValidator
    ) {
        this.name = name;
        this.defaultValue = defaultValue;
        this.toTag = toTag;
        this.fromTag = fromTag;
        this.valueValidator = valueValidator;
    }

    public String getName() {
        return name;
    }

    public V getDefaultValue() {
        return defaultValue;
    }

    public Tag toTag(V value) {
        return toTag.apply(value);
    }

    public V fromTag(Tag tag) {
        return fromTag.apply(tag);
    }

    public void validateValue(V value) {
        if (value == null || !valueValidator.test(value)) {
            throw new IllegalArgumentException("Received invalid value '" + value + "' for NBT setting '" + name + "'");
        }
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + ":" + name;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof NBTSettingDefinition<?> that)) return false;
        return Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }


    public NBTSetting<V> newNBTSetting() {
        return new NBTSetting<>(this);
    }
}
