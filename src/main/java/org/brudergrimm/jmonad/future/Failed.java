package org.brudergrimm.jmonad.future;

import org.brudergrimm.jmonad.option.Option;
import org.brudergrimm.jmonad.option.Some;
import org.brudergrimm.jmonad.tried.Failure;
import org.brudergrimm.jmonad.tried.Try;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

public class Failed<T> extends Future<T> {
    private final Throwable exception;

    private Failed(Throwable exception) {
        this.exception = exception;
    }

    public static <T> Future<T> apply(Throwable throwable) {
        return new Failed<>(throwable);
    }

    @Override public CompletableFuture toJavaFuture() {
        return CompletableFuture.completedFuture(this.exception);
    }

    @Override public <R> Future<R> map(Function<T, R> f) {
        @SuppressWarnings("unchecked")
        Future<R> failed = (Future<R>) this;
        return failed;
    }

    @Override public <R> Future<R> flatMap(Function<T, Future<R>> f) {
        @SuppressWarnings("unchecked")
        Future<R> failed = (Future<R>) this;
        return failed;
    }

    @Override public Option<Try<T>> value() {
        return Some.apply(Failure.apply(this.exception));
    }

    @Override public Try<T> await(Duration atMost) {
        return Failure.apply(this.exception);
    }

    @Override public Future<T> onSuccess(Consumer<T> t) {
        return this;
    }

    @Override public Future<T> onFailure(Consumer<Throwable> t) {
        t.accept(this.exception);
        return this;
    }

    @Override public boolean isCompleted() {
        return true;
    }

    @Override public Future<T> filter(Predicate<T> predicate) {
        return this;
    }

    @Override public Future<Throwable> failed() {
        @SuppressWarnings("unchecked")
        Future<Throwable> failed = (Future<Throwable>) this;
        return failed;
    }
}
