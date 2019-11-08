package org.brudergrimm.jmonad.function;

public interface ThrowingFunction<T, R> {
    R apply(T t) throws Throwable;
}
