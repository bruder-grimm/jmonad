package org.brudergrimm.jmonad.tried;

import org.brudergrimm.jmonad.either.Either;
import org.brudergrimm.jmonad.either.Right;
import org.brudergrimm.jmonad.function.ThrowingFunction;
import org.brudergrimm.jmonad.option.Option;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class Success<T> extends Try<T> {
    private final T value;

    private Success(T value) {
        this.value = value;
    }

    public static <T> Success<T> apply(T value) {
        return new Success<>(value);
    }

    @Override public boolean getException() { return false; }
    @Override public boolean isSuccess() { return true; }

    @Override public T get() { return this.value; }

    @Override public Try<T> onSuccess(Consumer<T> consumer) {
        consumer.accept(this.get());
        return this;
    }

    @Override public Try<T> onFailure(Consumer<Throwable> exceptionConsumer) { return this; }

    @Override public <R> Try<R> map(Function<T, R> fn) {
        return Try.apply(this.get(), fn);
    }

    @Override
    public <R> Try<R> map(ThrowingFunction<T, R> fn) {
        Function<T, R> f = (r) -> {
            try { return fn.apply(r); } catch (Throwable e) { throw new RuntimeException(e); }
        };

        return map(f);
    }

    @Override
    public <R> Try<R> flatMap(Function<T, Try<R>> fn) {
        try {
            return fn.apply(this.get());
        }
        catch (VirtualMachineError | ThreadDeath | LinkageError fatal) { throw fatal; }
        catch (Exception nonFatal) { return new Failure<>(nonFatal); }
    }

    @Override public <R> Try<R> flatMap(ThrowingFunction<T, Try<R>> fn) {
        Function<T, Try<R>> f = (r) -> {
            try { return fn.apply(r); } catch (Throwable e) { throw new RuntimeException(e); }
        };

        return flatMap(f);
    }

    @Override public T getOrElse(T other) { return this.get(); }
    @Override public Try<T> orElseTry(Supplier<T> fn) { return this; }

    @Override public Option<T> toOption() { return Option.apply(this.get()); }
    @Override public Either<Throwable, T> toEither() { return Right.apply(this.get()); }

    @Override public String toString() { return String.format("Success(%s)", this.get()); }
}
