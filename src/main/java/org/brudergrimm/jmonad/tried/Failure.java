package org.brudergrimm.jmonad.tried;

import org.brudergrimm.jmonad.either.Either;
import org.brudergrimm.jmonad.either.Left;
import org.brudergrimm.jmonad.function.ThrowingFunction;
import org.brudergrimm.jmonad.option.None;
import org.brudergrimm.jmonad.option.Option;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class Failure<T> extends Try<T> {
    private final Throwable exception;

    Failure(Throwable exception) {
        this.exception = exception;
    }

    public static <T> Try<T> apply(Throwable failure) {
        return new Failure<>(failure);
    }

    @Override public boolean getException() { return true; }
    @Override public boolean isSuccess() { return false; }

    @Override public Try<T> onSuccess(Consumer<T> consumer) { return this; }
    @Override public Try<T> onFailure(Consumer<Throwable> exceptionConsumer) {
        exceptionConsumer.accept(exception);
        return this;
    }

    @Override public T get() { throw new RuntimeException(exception); }

    @Override public <R> Try<R> map(Function<T, R> fn) {
        @SuppressWarnings("unchecked")
        Try<R> res = (Try<R>) this;
        return res;
    }
    @Override public <R> Try<R> map(ThrowingFunction<T, R> fn) {
        @SuppressWarnings("unchecked")
        Try<R> res = (Try<R>) this;
        return res;
    }
    @Override public <R> Try<R> flatMap(Function<T, Try<R>> fn) {
        @SuppressWarnings("unchecked")
        Try<R> res = (Try<R>) this;
        return res;
    }
    @Override public <R> Try<R> flatMap(ThrowingFunction<T, Try<R>> fn) {
        @SuppressWarnings("unchecked")
        Try<R> res = (Try<R>) this;
        return res;
    }

    @Override public T getOrElse(T other) { return other; }
    @Override public Try<T> orElseTry(Supplier<T> fn) { return Try.apply(fn); }

    @Override public Option<T> toOption() { return None.apply(); }
    @Override public Either<Throwable, T> toEither() { return Left.apply(exception); }

    @Override public String toString() { return String.format("Failure(%s)", exception.getMessage()); }
}
