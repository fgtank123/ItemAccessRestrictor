package net.fgtank123.nbtsetting;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings({"rawtypes", "unchecked"})
public class NBTSettingsManager {
    public NBTSettingsManager() {
    }

    private final List<NBTSetting> nbtSettings = new ArrayList<>();

    public <V> NBTSetting<V> register(NBTSettingDefinition<V> nbtSettingDefinition) {
        NBTSetting<V> nbtSetting = nbtSettingDefinition.newNBTSetting();
        nbtSettings.add(nbtSetting);
        return nbtSetting;
    }

    public void loadFromNBT(@Nonnull CompoundTag tag) {
        for (NBTSetting nbtSetting : nbtSettings) {
            NBTSettingDefinition definition = nbtSetting.getDefinition();
            Tag settingValueTag = tag.get(definition.getName());
            if (settingValueTag != null) {
                Object settingValue = definition.fromTag(settingValueTag);
                definition.validateValue(settingValue);
                nbtSetting.initializeValue(settingValue);
            }
        }
    }

    public void saveToNBT(@Nonnull CompoundTag tag) {
        for (NBTSetting nbtSetting : nbtSettings) {
            NBTSettingDefinition definition = nbtSetting.getDefinition();
            Object settingValue = nbtSetting.getValue();
            definition.validateValue(settingValue);
            Tag settingValueTag = definition.toTag(settingValue);
            tag.put(definition.getName(), settingValueTag);
        }
    }

    @SuppressWarnings("unused")
    public List<NBTSetting> getNbtSettings() {
        return nbtSettings;
    }

    public void onValueChanged(NBTSettingValueChangedListener listener) {
        for (NBTSetting nbtSetting : nbtSettings) {
            nbtSetting.addListener(listener);
        }
    }
}
