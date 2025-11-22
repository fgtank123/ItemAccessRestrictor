package net.fgtank123.nbtsetting;

import org.apache.commons.lang3.ArrayUtils;

public final class NBTSetting<V> {
    private final NBTSettingDefinition<V> definition;
    private V value;
    private NBTSettingValueChangedListener<V>[] listeners;

    NBTSetting(NBTSettingDefinition<V> definition) {
        this.definition = definition;
        this.value = definition.getDefaultValue();
    }

    public NBTSettingDefinition<V> getDefinition() {
        return definition;
    }

    public V getValue() {
        return value;
    }

    public void setValue(V value) {
        this.getDefinition().validateValue(value);
        var oldValue = this.value;
        this.value = value;
        if (listeners != null) {
            for (NBTSettingValueChangedListener<V> listener : listeners) {
                listener.onValueChanged(definition, value, oldValue);
            }
        }
    }

    public void initializeValue(V value) {
        this.value = value;
    }

    public void addListener(NBTSettingValueChangedListener<V> listener) {
        if (listeners == null) {
            // noinspection unchecked
            listeners = new NBTSettingValueChangedListener[0];
        }
        listeners = ArrayUtils.add(listeners, listener);
    }

    @SuppressWarnings("unused")
    public boolean removeListener(NBTSettingValueChangedListener<V> listener) {
        if (listeners == null) {
            return false;
        }
        int index = ArrayUtils.indexOf(listeners, listener);
        if (index == -1) {
            return false;
        }
        listeners = ArrayUtils.remove(listeners, index);
        return true;
    }
}