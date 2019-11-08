package org.brudergrimm.jmonad.option;

import java.util.function.Consumer;

public class Some<T> extends Option<T> {
    private final T value;
    private Some(T value) { this.value = value; }

    public static <T> Option<T> apply(T value) {
        return new Some<>(value);
    }

    @Override public boolean isEmpty() { return false; }
    @Override public T get() { return value; }

    @Override public Option<T> ifSome(Consumer<T> consumer) {
        consumer.accept(this.get());
        return this;
    }
    @Override public Option<T> ifNone(Runnable runnable) { return this; }

    @Override public String toString() {
        return String.format(this.get() instanceof Double ? "Some(%.2f)" : "Some(%s)", this.get());
    }
}
