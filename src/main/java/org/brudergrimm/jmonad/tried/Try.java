package org.brudergrimm.jmonad.tried;

import org.brudergrimm.jmonad.either.Either;
import org.brudergrimm.jmonad.option.Option;
import org.brudergrimm.jmonad.tried.function.*;

import java.io.Serializable;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

import static org.brudergrimm.jmonad.tried.Deescelator.asDeescelatedSupplier;
import static org.brudergrimm.jmonad.tried.Deescelator.asDeescelatedFunction;

/** Since it may not have become clear this boxes an operation that might fail
 *  and forces the callee to deal with the failure, may it have occured. The
 *  Idea is that you can map a function to the value of the monad and only before
 *  having to unbox check if something went wrong, deescalating errors substantially
 *
 *  @param <T> the boxed type the operation given to apply will return */
abstract public class Try<T> implements Serializable {
    /** Constructs a try from your supplier that has a throws in it's signature
     *  @param r the supplier that might fail with an exception
     *  @param <T> the return type of your supplier
     *  @return A try that might either be an exception or the supplied value  */
    public static <T> Try<T> applyThrowing(ThrowingSupplier<T> r) {
        return Try.apply(asDeescelatedSupplier(r));
    }

    /** Constructs a try from a supplier
     *  @param f the supplier that might fail with an exception
     *  @param <T> the return type of your supplier
     *  @return A try that might either be an exception or the supplied value */
    public static <T> Try<T> apply(Supplier<T> f) {
        try {
            return Success.apply(f.get());
        }
        catch (VirtualMachineError | ThreadDeath | LinkageError fatal) { throw fatal; }
        catch (Exception nonFatal) { return Failure.apply(nonFatal); }
    }

    static <U, T> Try<T> applyThrowing(U v1, ThrowingFunction<U, T> fn) {
        return Try.apply(v1, asDeescelatedFunction(fn)); }
    static <U, T> Try<T> apply(U v1, Function<U, T> fn) {
        return Try.apply(() -> fn.apply(v1));
    }

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

    /** @return practically invertes the result to Success(failure) or Failure(didn't fail) */
    public abstract Try<Throwable> failed();

    /** maps a function to the state value
     *  @param fn the function to apply to the boxed value, must return R
     *  @param <R> return type
     *  @return the mapped try */
    public abstract <R> Try<R> map(Function<T, R> fn);

    /** This will take a function that returns another try and return just an try instead of an try[try]
     *  @param fn the function to apply to the boxed value, must return Try of R
     *  @param <R> return type
     *  @return the mapped try */
    public abstract <R> Try<R> flatMap(Function<T, Try<R>> fn);

    /** Converts this to a Failure if the predicate is not satisfied.
     * @param condition predicate
     * @return the try */
    public abstract Try<T> filter(Predicate<T> condition);

    /** If this was a failure, return other instead
     *  @param other the fallback
     *  @return state or other */
    public abstract T getOrElse(T other);

    /** Applies the given function if this is a Failure, otherwise returns this if this is a Success.
     *  This is supposed to be like map for the exception, but there is no lower type bounds in java
     *  like <U super T> that would enable us to have this return a Try<U>
     *  @param fn the fallback-callback
     *  @return this or the result of the callback */
    public abstract Try<T> recover(Function<Throwable, T> fn);

    /** Applies the given function if this is a Failure, otherwise returns this if this is a Success.
     *  This is like flatMap for the exception.
     *
     *  @param fn the fallback-callback
     *  @return this or the result of the callback */
    public abstract Try<T> recoverWith(Function<Throwable, Try<T>> fn);

    /** Applies fa if this was a failure, applies fb if this was a success
     *  @param fa the function to apply if this is a failure
     *  @param fb the function to apply if this is a success
     *  @param <U> return type
     *  @return result of either fa or db */
    public abstract <U> U fold(Function<Throwable, U> fa, Function<T, U> fb);

    /** I have not found out how to supply evidence to this so use with caution
     * @return the inner try */
    public <U> Try<U> flatten() {
        if (this.isSuccess() && this.get() instanceof Try) {
            @SuppressWarnings("unchecked") Try<U> flattened = (Try<U>) this.get();
            return flattened;
        } else {
            @SuppressWarnings("unchecked") Try<U> identity = (Try<U>) this;
            return identity;
        }
    }

    /** @return an Option of this, empty if failure */
    public abstract Option<T> toOption();

    /** @return either Left of the exception or right of the value */
    public abstract Either<Throwable, T> toEither();

    @Override public abstract String toString();
}
