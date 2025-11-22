package net.fgtank123.neoforgegui;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class ValueRef<V> {
    private final Supplier<V> getter;
    private final Consumer<V> setter;

    public ValueRef(Supplier<V> getter, Consumer<V> setter) {
        this.getter = getter;
        this.setter = setter;
    }

    public V get() {
        return getter.get();
    }

    public void set(V v) {
        setter.accept(v);
    }
}
