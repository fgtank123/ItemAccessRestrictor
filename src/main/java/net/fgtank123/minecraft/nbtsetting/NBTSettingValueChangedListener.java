package net.fgtank123.minecraft.nbtsetting;

public interface NBTSettingValueChangedListener<V> {
    void onValueChanged(NBTSettingDefinition<V> definition, V newValue, V oldValue);
}
