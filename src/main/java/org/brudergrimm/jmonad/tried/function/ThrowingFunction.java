package org.brudergrimm.jmonad.tried.function;

public interface ThrowingFunction<T, R> {
    R apply(T t) throws Throwable;
}
