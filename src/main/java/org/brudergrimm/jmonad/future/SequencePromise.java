package org.brudergrimm.jmonad.future;

import org.brudergrimm.jmonad.option.Option;
import org.brudergrimm.jmonad.option.Some;
import org.brudergrimm.jmonad.tried.Failure;
import org.brudergrimm.jmonad.tried.Success;
import org.brudergrimm.jmonad.tried.Try;

import java.time.Duration;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class SequencePromise<T> extends Future<List<T>> {
    private final Try<CompletableFuture<Void>> indicator;
    private final List<Future<T>> futures;

    SequencePromise(CompletableFuture<Void> indicator, List<Future<T>> futures) {
        this.indicator = Try.apply(() -> indicator); // might throw completionException
        this.futures = futures; // wont do that
    }

    @Override public CompletableFuture<List<T>> toJavaFuture() {
        return CompletableFuture.supplyAsync(() -> indicator
                .map(future -> {
                    future.join(); // await the indicator in another thread
                    return futures.stream() // then collect all of our futures
                            .map(v -> v.value().get().get())
                            .collect(Collectors.toList());
                }).getOrElse(Collections.emptyList())
        );
    }

    private List<T> results() {
        /* we can safely get here because - via the definition of CompletableFuture.allOf - all of our futures are
        a) completed
        b) not completed exceptionally
        Users have to deal with nulls that futures might return themselves, might be a problem in the future */
        return this.futures
                .parallelStream()
                .map(future -> future.value().get().get())
                .collect(Collectors.toList());
    }

    @Override public <R> Future<R> map(Function<List<T>, R> f) {
        return indicator.fold(
                Failed::apply,
                success -> fromJavaFuture(
                        success.thenApplyAsync(i -> f.apply(this.results()))
                )
        );
    }

    @Override public <R> Future<R> flatMap(Function<List<T>, Future<R>> f) {
        return indicator.fold(
                Failed::apply,
                success -> fromJavaFuture(
                        success.thenComposeAsync(i ->
                                f.andThen(Future::toJavaFuture).apply(this.results())
                        )
                )
        );
    }

    @Override public Option<Try<List<T>>> value() {
        return this.indicator.fold(
                failure -> Some.apply(Failure.apply(failure)),
                success -> Option
                        .apply(this.results())
                        .map(Success::apply)
        );
    }

    @Override public Try<List<T>> await(Duration atMost) {
        return this.indicator
                .flatMap(f ->
                        Try.applyThrowing(() -> f.get(atMost.toMillis(), TimeUnit.MILLISECONDS))
                )
                .map(i -> results());
    }

    @Override public Future<List<T>> onSuccess(Consumer<List<T>> t) {
        this.indicator.map(future ->
                future.thenRunAsync(() -> t.accept(this.results()))
        );
        return this;
    }

    @Override public Future<List<T>> onFailure(Consumer<Throwable> t) {
        this.indicator.map(future ->
                future.whenCompleteAsync((i, throwable) -> {
                    if (throwable != null) t.accept(throwable);
                })
        );
        return this;
    }

    @Override public boolean isCompleted() {
        return indicator
                .map(CompletableFuture::isDone)
                .getOrElse(true);
    }

    @Override public Future<List<T>> filter(Predicate<List<T>> predicate) {
        return this.map( r -> {
            if (predicate.test(r)) {
                return r;
            } else throw new NoSuchElementException("Predicate didn't match value");
        });
    }

    @Override public Future<Throwable> failed() {
        return this.indicator.fold(
                Successful::apply,
                i -> Failed.apply(new Throwable("Future didn't fail"))
        );
    }
}
