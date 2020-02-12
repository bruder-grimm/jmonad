package org.brudergrimm.jmonad.future;

import org.brudergrimm.jmonad.option.Option;
import org.brudergrimm.jmonad.tried.Try;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.stream.Collectors;

public class SequencePromise<T> extends Future<List<T>> {
    private final CompletableFuture<Void> indicator;
    private final List<Future<T>> futures;

    SequencePromise(CompletableFuture<Void> indicator, List<Future<T>> futures) {
        this.indicator = indicator;
        this.futures = futures;
    }

    @Override public CompletableFuture<List<T>> toJavaFuture() {
        return null;
    }

    private List<T> results() {
        return this.futures
                .parallelStream()
                .map(Future::value)
                .filter(Option::isDefined)
                .map(Option::get)
                .filter(Try::isSuccess)
                .map(Try::get)
                .collect(Collectors.toList());
    }

    @Override public <R> Future<R> map(Function<List<T>, R> f) {
        return fromJavaFuture(
                indicator.thenApplyAsync(i -> f.apply(this.results()))
        );
    }

    @Override public <R> Future<R> flatMap(Function<List<T>, Future<R>> f) {
        return fromJavaFuture(
                indicator.thenComposeAsync(i -> {
                            Function<List<T>, CompletableFuture<R>> fprime = f.andThen(Future::toJavaFuture);
                            return fprime.apply(this.results());
                        }
                )
        );
    }

    @Override public Option<Try<List<T>>> value() {
        return null;
    }

    @Override public boolean isCompleted() {
        return indicator.isDone();
    }

    @Override public Future<Throwable> failed() {
        return null;
    }
}
