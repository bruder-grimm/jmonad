package org.brudergrimm.jmonad.tried;

import org.brudergrimm.jmonad.function.ThrowingFunction;
import org.brudergrimm.jmonad.option.Option;
import org.brudergrimm.jmonad.either.Either;
import org.brudergrimm.jmonad.either.Left;
import org.brudergrimm.jmonad.either.Right;
import org.brudergrimm.jmonad.function.ThrowingSupplier;

import java.io.Serializable;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Since it may not have become clear this boxes an operation that might fail
 * and forces the callee to deal with the failure, may it have occured. The
 * Idea is that you can map a function to the value of the monad and only before
 * having to unbox check if something went wrong, deescalating errors substantially
 *
 *  @param <T> the boxed type the operation given to apply will return */
abstract public class Try<T> implements Serializable {

    public static <T> Try<T> applyThrowing(ThrowingSupplier<T> r) {
        Supplier<T> f = () -> { try { return r.get(); } catch (Throwable e) { throw new RuntimeException(e); } };
        return Try.apply(f);
    }

    public static <T> Try<T> apply(Supplier<T> r) {
        Supplier<T> f = () -> { try { return r.get(); } catch (Throwable e) { throw new RuntimeException(e); } };
        try {
            return Success.apply(f.get());
        }
        catch (VirtualMachineError | ThreadDeath | LinkageError fatal) { throw fatal; }
        catch (Exception nonFatal) { return Failure.apply(nonFatal); }
    }

    static <U, T> Try<T> applyThrowing(U v1, ThrowingFunction<U, T> fn) {
        return Try.applyThrowing(() -> fn.apply(v1)); }

    static <U, T> Try<T> apply(U v1, Function<U, T> fn) {
        return Try.apply(() -> fn.apply(v1)); }

    public abstract boolean getException();
    public abstract boolean isSuccess();

    public static <T> Try<T> failed(Throwable e) { return Failure.apply(e); }
    public static <T> Try<T> successful(T t) { return Success.apply(t); }

    /** register a consumer that will be called with T on Success
     *  @param consumer that takes T
     *  @return this try for chaining */
    public abstract Try<T> onSuccess(Consumer<T> consumer);

    /** register a consumer that will be called with the Exception on Failure
     *  @param exceptionConsumer that takes T
     *  @return this try for chaining */
    public abstract Try<T> onFailure(Consumer<Throwable> exceptionConsumer);

    /** fail early, do not use this, always check if "isSuccess()" before
     *  @return getBy the underling value */
    public abstract T get();

    /** maps a function to the state value
     *  @param fn the function to apply to the boxed value, must return R
     *  @param <R> return type
     *  @return the mapped try */
    public abstract <R> Try<R> map(Function<T, R> fn);
    /** maps a function to the state value, but the function has a throws in it's signature
     *  @param fn the function to apply to the boxed value, must return R
     *  @param <R> return type
     *  @return the mapped try */
    public abstract <R> Try<R> map(ThrowingFunction<T, R> fn);

    /** This will take a function that returns another try and return just an try instead of an try[try]
     *  @param fn the function to apply to the boxed value, must return Try of R
     *  @param <R> return type
     *  @return the mapped try */
    public abstract <R> Try<R> flatMap(Function<T, Try<R>> fn);
    /** Same as flatmap, but the function has a throws in it's signature
     *  This will take a function that returns another try and return just an try instead of an try[try]
     *  @param fn the function to apply to the boxed value, must return Try of R
     *  @param <R> return type
     *  @return the mapped try */
    public abstract <R> Try<R> flatMap(ThrowingFunction<T, Try<R>> fn);

    /** If this was a failure, return other instead
     *  @param other the fallback
     *  @return state or other */
    public abstract T getOrElse(T other);

    /** If this was a failure, call this supplier instead
     *  @param fn the fallback-callback
     *  @return this or the result of the callback */
    public abstract Try<T> orElseTry(Supplier<T> fn);

    /** @return an Option of this, empty if failure */
    public abstract Option<T> toOption();

    /** @return either Left of the exception or right of the value */
    public abstract Either<Throwable, T> toEither();

    @Override public abstract String toString();
}
