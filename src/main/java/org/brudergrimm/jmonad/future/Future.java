package org.brudergrimm.jmonad.future;

import org.brudergrimm.jmonad.option.Option;
import org.brudergrimm.jmonad.tried.Try;

import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

public abstract class Future<T> {
    public abstract CompletableFuture<T> toJavaFuture();

    public static <T> Future<T> fromJavaFuture(CompletableFuture<T> javaFuture) {
        return new DefaultPromise<T>(javaFuture);
    }

    public abstract <R> Future<R> map(Function<T, R> f);

    public abstract  <R> Future<R> flatMap(Function<T, Future<R>> f);

    /** Block and get the value, we will have some way to await in the future
     *  get it?
     *  @return Some or None if the future was completed, Failure or Success of the value */
    public abstract Option<Try<T>> value();

    public abstract boolean isCompleted();

    public abstract Future<Throwable> failed();

    public static <T> Future<Collection<T>> sequence(Collection<Future<T>> futures) {
        ConcurrentHashMap<Integer, Boolean> completed = new ConcurrentHashMap<>(futures.size());

        Future<Collection<T>> seq = new DefaultPromise<Collection<T>>(() -> {
            futures
                    .parallelStream()
                    .map(future -> future
                            .toJavaFuture()
                            .thenRun()
                    )
        });
    }
}
