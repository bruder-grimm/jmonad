package org.brudergrimm.jmonad.future;

import org.brudergrimm.jmonad.option.Option;
import org.brudergrimm.jmonad.tried.Try;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.function.Supplier;

import static java.util.function.Function.identity;

public abstract class Future<T> {
    /** Apply a new future that will eventually finish computation of the supplier or fail
     *  @param t the supplier
     *  @param <T> type of the future
     *  @return a future holding your eventual value */
    public static <T> Future<T> apply(Supplier<T> t) {
        return new DefaultPromise<>(t);
    }

    /** @return Turns this into a completeable future */
    public abstract CompletableFuture<T> toJavaFuture();

    /** Constructs a future from a completeable future
     *  @param javaFuture the future you want to box
     *  @param <T> return type
     *  @return a future of type t */
    public static <T> Future<T> fromJavaFuture(CompletableFuture<T> javaFuture) {
        return new DefaultPromise<>(javaFuture);
    }

    /** Apply a function to the eventual value of this future
     *  @param f the function
     *  @param <R> the functions return type
     *  @return Future with eventually applied function */
    public abstract <R> Future<R> map(Function<T, R> f);

    /** Same as map but the applied funtion returns a future itself
     *  keeps the root
     *  @param f the function
     *  @param <R> type of the eventual value in the result of the function
     *  @return a future of the eventual value of function f */
    public abstract <R> Future<R> flatMap(Function<T, Future<R>> f);

    /** Get the value - if computation was finished or not
     *  we will have some way to await in the future
     *  get it?
     *  @return Some or None if the future was completed, Failure or Success of the value */
    public abstract Option<Try<T>> value();

    public abstract boolean isCompleted();

    /** Turns a Future[Future[T]] into a Future[T]
     *  @param <Evidence> type of the inner future
     *  @return the inner future */
    public <Evidence> Future<Evidence> flatten() {
        Future<Future<Evidence>> toFlatten;
        try {
            toFlatten = (Future<Future<Evidence>>) this;
        } catch (ClassCastException e) {
            return (Future<Evidence>) this;
        }
        return fromJavaFuture(
                toFlatten.map(Future::toJavaFuture)
                        .toJavaFuture()
                        .thenCompose(identity())
        );
    }

    /** @return Either the acutaly completionexception or an error if the future didn't fail */
    public abstract Future<Throwable> failed();

    /** Turns a List of homogenous Futures into a homogenous Future List
     *  @param futures the list of futures
     *  @param <T> type of whatever is in the list
     *  @return the eventual list */
    public static <T> Future<List<T>> sequence(List<Future<T>> futures) {
        CompletableFuture<Void> indicator = CompletableFuture.allOf(
                futures.parallelStream()
                        .map(Future::toJavaFuture)
                        .toArray(CompletableFuture[]::new)
        );

        return new SequencePromise<>(indicator, futures);
    }
}
