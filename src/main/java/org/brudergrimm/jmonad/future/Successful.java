package org.brudergrimm.jmonad.future;

import org.brudergrimm.jmonad.option.Option;
import org.brudergrimm.jmonad.option.Some;
import org.brudergrimm.jmonad.tried.Failure;
import org.brudergrimm.jmonad.tried.Success;
import org.brudergrimm.jmonad.tried.Try;

import java.time.Duration;
import java.util.NoSuchElementException;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

public class Successful<T> extends Future<T> {
    private final T result;

    Successful(T result) {
        this.result = result;
    }

    public static <T> Future<T> apply(T result) {
        return new Successful<>(result);
    }

    @Override public CompletableFuture<T> toJavaFuture() {
        return CompletableFuture.completedFuture(this.result);
    }

    @Override public <R> Future<R> map(Function<T, R> f) {
        return new DefaultPromise<>(() -> f.apply(result));
    }

    @Override public <R> Future<R> flatMap(Function<T, Future<R>> f) {
        return f.apply(result);
    }

    @Override public Option<Try<T>> value() {
        return Some.apply(Success.apply(result));
    }

    @Override public Try<T> await(Duration atMost) {
        return Success.apply(this.result);
    }

    @Override public Future<T> onSuccess(Consumer<T> t) {
        t.accept(this.result);
        return this;
    }

    @Override public Future<T> onFailure(Consumer<Throwable> t) {
        return this;
    }

    @Override public boolean isCompleted() {
        return true;
    }

    @Override public Future<T> filter(Predicate<T> predicate) {
            if (predicate.test(this.result)) {
                return this;
            } else return Failed.apply(new NoSuchElementException("Predicate didn't match value"));
    }

    @Override public Future<Throwable> failed() {
        return Successful.apply(new Throwable("Future didn't fail"));
    }
}
