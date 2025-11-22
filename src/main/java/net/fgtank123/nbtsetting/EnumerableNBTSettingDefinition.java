package net.fgtank123.nbtsetting;

import net.minecraft.nbt.Tag;

import javax.annotation.Nonnull;
import java.util.Set;
import java.util.function.Function;

public class EnumerableNBTSettingDefinition<V> extends NBTSettingDefinition<V> {
    private final Set<V> values;

    EnumerableNBTSettingDefinition(@Nonnull String name, @Nonnull V defaultValue, @Nonnull Function<V, Tag> toTag, @Nonnull Function<Tag, V> fromTag, @Nonnull Set<V> values) {
        super(name, defaultValue, toTag, fromTag, values::contains);
        this.values = values;
    }

    public Set<V> getValues() {
        return values;
    }
}
