package org.brudergrimm.jmonad.future;

import org.brudergrimm.jmonad.option.Option;
import org.brudergrimm.jmonad.option.Some;
import org.brudergrimm.jmonad.tried.Failure;
import org.brudergrimm.jmonad.tried.Success;
import org.brudergrimm.jmonad.tried.Try;

import java.time.Duration;
import java.util.NoSuchElementException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class DefaultPromise<T> extends Future<T> {
    final Try<CompletableFuture<T>> task;

    DefaultPromise(Supplier<T> callable) {
        this.task = Try.apply(() -> CompletableFuture.supplyAsync(callable));
    }

    DefaultPromise(CompletableFuture<T> javaFuture) {
        this.task = Try.apply(() -> javaFuture);
    }

    @Override public CompletableFuture toJavaFuture() {
        return this.task
                .toEither() // the combination of toEither with fold is basically 'transform'
                .fold(
                        (CompletableFuture::completedFuture), // alreadyFailed
                        (notYetFailed -> notYetFailed)
                );
    }

    @Override public <R> Future<R> map(Function<T, R> f) {
        return this.task.fold(
                Failed::apply,
                future -> fromJavaFuture(future.thenApplyAsync(f))
        );
    }

    @Override public <R> Future<R> flatMap(Function<T, Future<R>> f) {
        return this.task.fold(
                Failed::apply,
                future -> fromJavaFuture(future.thenComposeAsync(f.andThen(Future::toJavaFuture)))
        );
    }

    @Override public Option<Try<T>> value() {
        return this.task.fold(
                failure -> Some.apply(Failure.apply(failure)),
                future -> Option
                        .apply(future.getNow(null))
                        .map(Success::apply)
        );
    }

    @Override public Try<T> await(Duration atMost) {
        return this.task.flatMap(future ->
                Try.applyThrowing(() -> future.get(atMost.toMillis(), TimeUnit.MILLISECONDS))
        );
    }

    @Override public Future<T> onSuccess(Consumer<T> t) {
        this.task.map(future -> future.thenAcceptAsync(t));
        return this;
    }

    @Override public Future<T> onFailure(Consumer<Throwable> t) {
        this.task.map(future -> future.handle((i, throwable) -> {
            t.accept(throwable);
            return i;
        }));
        return this;
    }

    @Override public boolean isCompleted() {
        return task.map(CompletableFuture::isDone).getOrElse(true);
    }

    @Override public Future<T> filter(Predicate<T> predicate) {
        return this.map( r -> {
            if (predicate.test(r)) {
                return r;
            } else throw new NoSuchElementException("Predicate didn't match value");
        });
    }

    @Override public Future<Throwable> failed() {
        return this.task.fold(
                        Failed::apply,
                        notYetFailed -> Failed.apply(new Throwable("Future hasn't failed"))
                );
    }
}
