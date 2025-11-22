package net.fgtank123.minecraft.itemaccessrestrictor.definitions;

import net.fgtank123.minecraft.nbtsetting.NBTSettingDefinitionEnumerable;
import net.fgtank123.minecraft.nbtsetting.NBTSettingDefinition;
import net.fgtank123.minecraft.nbtsetting.NBTSettingDefinitionFactory;
import net.minecraft.nbt.ByteArrayTag;
import org.apache.commons.lang3.ArrayUtils;

import java.util.stream.IntStream;

public class ModNBTSettingDefinitions {

    public static final NBTSettingDefinitionEnumerable<Boolean> BLOCKING_INPUT_IF_NOT_EMPTY = NBTSettingDefinitionFactory.createBooleans(
        "blocking_input_if_not_empty",
        true
    );

    public static final NBTSettingDefinitionEnumerable<Boolean> BLOCKING_INPUT_IF_RECEIVING_REDSTONE_SIGNAL = NBTSettingDefinitionFactory.createBooleans(
        "blocking_input_if_receiving_redstone_signal",
        true
    );

    public static final NBTSettingDefinitionEnumerable<Integer> INPUT_STACKING_LIMIT = NBTSettingDefinitionFactory.createIntegers(
        "input_stacking_limit",
        ArrayUtils.addFirst(
            IntStream.rangeClosed(1, 64).boxed().toArray(Integer[]::new),
            -1
        )
    );

    public static final NBTSettingDefinitionEnumerable<ComparatorOutputMode> COMPARATOR_OUTPUT_MODE = NBTSettingDefinitionFactory.createDefinedEnumerable(
        "comparator_output_mode",
        ComparatorOutputMode.class
    );

    public static final NBTSettingDefinitionEnumerable<Integer> QUANTITY_OF_RETAINED_ITEMS = NBTSettingDefinitionFactory.createIntegers(
        "quantity_of_retained_items",
        ArrayUtils.addFirst(
            IntStream.rangeClosed(1, 64).boxed().toArray(Integer[]::new),
            0
        )
    );

    public static final NBTSettingDefinition<boolean[]> SLOT_DISABLES = NBTSettingDefinitionFactory.createNonValidate(
        "slot_disables",
        new boolean[0],
        booleans -> {
            byte[] bytes = new byte[booleans.length];
            for (int i = 0; i < booleans.length; i++) {
                bytes[i] = booleans[i] ? (byte) 1 : (byte) 0;
            }
            return new ByteArrayTag(bytes);
        },
        tag -> {
            if (tag instanceof ByteArrayTag byteArrayTag) {
                byte[] byteArray = byteArrayTag.getAsByteArray();
                boolean[] booleans = new boolean[byteArray.length];
                for (int i = 0; i < byteArray.length; i++) {
                    booleans[i] = byteArray[i] == (byte) 1;
                }
                return booleans;
            } else {
                return new boolean[0];
            }
        }
    );

}
