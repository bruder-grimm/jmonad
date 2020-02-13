package org.brudergrimm.jmonad.future;

import org.brudergrimm.jmonad.option.Option;
import org.brudergrimm.jmonad.option.Some;
import org.brudergrimm.jmonad.tried.Failure;
import org.brudergrimm.jmonad.tried.Success;
import org.brudergrimm.jmonad.tried.Try;

import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
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

    @Override public boolean isCompleted() {
        // if the task has failed it is inherently done
        return task.map(CompletableFuture::isDone).getOrElse(true);
    }

    @Override public Future<Throwable> failed() {
        return this.task.fold(
                        Failed::apply,
                        notYetFailed -> Failed.apply(new Throwable("Future hasn't failed"))
                );
    }
}
