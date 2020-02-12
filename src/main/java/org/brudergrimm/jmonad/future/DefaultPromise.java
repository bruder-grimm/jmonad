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
        if (this.task.isSuccess()) {
            return fromJavaFuture(this.task.get().thenApplyAsync(f));
        }
        return Failed.apply(this.task.failed().get());
    }

    @Override public <R> Future<R> flatMap(Function<T, Future<R>> f) {
        if (this.task.isSuccess()) {
            return fromJavaFuture(task.get().thenComposeAsync(f.andThen(Future::toJavaFuture)));
        }
        return Failed.apply(this.task.failed().get());
    }

    @Override public Option<Try<T>> value() {
        if (this.task.isSuccess()) {
            return Option.apply(this.task.get().getNow(null))
                    .map(Success::apply);
        }
        // we need to rebox here because types
        return Some.apply(Failure.apply(this.task.failed().get()));
    }

    @Override public boolean isCompleted() {
        // if the task has failed it is inherently done
        return task.map(CompletableFuture::isDone).getOrElse(true);
    }

    @Override public Future<Throwable> failed() {
        return this.task
                .toEither()
                .fold(
                        (Failed::apply), // alreadyFailed
                        (notYetFailed -> Failed.apply(new Throwable("Future hasn't failed")))
                );
    }
}
