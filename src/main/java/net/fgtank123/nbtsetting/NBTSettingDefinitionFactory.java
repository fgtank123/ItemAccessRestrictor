package net.fgtank123.nbtsetting;

import com.google.common.collect.ImmutableSet;
import net.minecraft.nbt.ByteTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;

import javax.annotation.Nonnull;
import java.util.function.Function;
import java.util.function.Predicate;

public final class NBTSettingDefinitionFactory {
    @SafeVarargs
    public static <V extends Enum<V>> EnumerableNBTSettingDefinition<V> createDefinedEnumerable(@Nonnull String name, @Nonnull Class<V> enumClass, V... possibleSettingValues) {
        V defaultValue = possibleSettingValues.length == 0 ? enumClass.getEnumConstants()[0] : possibleSettingValues[0];
        return new EnumerableNBTSettingDefinition<>(
            name,
            defaultValue,
            v -> StringTag.valueOf(v.name()),
            tag -> {
                String stringValue = tag.getAsString();
                for (V possibleSettingValue : possibleSettingValues) {
                    if (possibleSettingValue.name().equals(stringValue)) {
                        return possibleSettingValue;
                    }
                }
                return defaultValue;
            },
            possibleSettingValues.length == 0 ? ImmutableSet.copyOf(enumClass.getEnumConstants()) : ImmutableSet.copyOf(possibleSettingValues)
        );
    }

    @SafeVarargs
    public static <V> EnumerableNBTSettingDefinition<V> createEnumerable(@Nonnull String name, @Nonnull Function<V, Tag> toTag, @Nonnull Function<Tag, V> fromTag, V... possibleSettingValues) {
        if (possibleSettingValues.length == 0) {
            throw new IllegalArgumentException("Possible setting values could not be empty.");
        }
        V defaultValue = possibleSettingValues[0];
        return new EnumerableNBTSettingDefinition<>(
            name,
            defaultValue,
            toTag,
            fromTag,
            ImmutableSet.copyOf(possibleSettingValues)
        );
    }

    public static EnumerableNBTSettingDefinition<Integer> createIntegers(@Nonnull String name, Integer... possibleSettingValues) {
        return createEnumerable(
            name,
            IntTag::valueOf,
            tag -> tag instanceof IntTag ? ((IntTag) tag).getAsInt() : Integer.parseInt(tag.getAsString()),
            possibleSettingValues
        );
    }

    public static EnumerableNBTSettingDefinition<Boolean> createBooleans(@Nonnull String name, boolean defaultSettingValue) {
        return new EnumerableNBTSettingDefinition<>(
            name,
            defaultSettingValue,
            ByteTag::valueOf,
            tag -> tag.equals(ByteTag.ONE),
            defaultSettingValue ? ImmutableSet.of(true, false) : ImmutableSet.of(false, true)
        );
    }

    public static <V> NBTSettingDefinition<V> create(
        @Nonnull String name,
        @Nonnull V defaultValue,
        @Nonnull Function<V, Tag> toTag,
        @Nonnull Function<Tag, V> fromTag,
        @Nonnull Predicate<V> valueValidator
    ) {
        return new NBTSettingDefinition<>(
            name,
            defaultValue,
            toTag,
            fromTag,
            valueValidator
        );
    }

    public static <V> NBTSettingDefinition<V> createNonValidate(
        @Nonnull String name,
        @Nonnull V defaultValue,
        @Nonnull Function<V, Tag> toTag,
        @Nonnull Function<Tag, V> fromTag
    ) {
        return create(
            name,
            defaultValue,
            toTag,
            fromTag,
            v -> true
        );
    }
}
