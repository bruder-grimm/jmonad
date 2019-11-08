package org.brudergrimm.jmonad.function;

@FunctionalInterface
public interface ThrowingSupplier<T> {
    T get() throws Throwable;
}
