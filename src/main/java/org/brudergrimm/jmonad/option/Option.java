package org.brudergrimm.jmonad.option;

import java.io.Serializable;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * I didn't like the way javas optional handled, so I created my own
 *
 * In the future, I will create Some and None, just like in Scala, so you can
 * match for Some(value) and have a comparable None with a serial.
 *
 * @param <T> the boxed type */
public abstract class Option<T> implements Serializable {
    private static Option none = None.apply();

    public static <T> Option<T> apply(T value) {
        return value == null ? none : Some.apply(value);
    }

    public abstract boolean isEmpty();
    public boolean isDefined() { return !isEmpty(); }

    public abstract T get();

    public abstract Option<T> ifSome(Consumer<T> consumer);
    public abstract Option<T> ifNone(Runnable runnable);

    /** maps a function to the state value
     *  @param f the function to apply to the state value
     *  @param <R> output type of function
     *  @return Option of the result of f() */
    public <R> Option<R> map(Function<T, R> f) {
        return isEmpty() ? none : Option.apply(f.apply(this.get()));
    }

    /** This will take a function that returns another option and return just an option instead of an option[option]
     *  @param f the function to apply to the state value
     *  @param <R> Boxed output type of function
     *  @return Option of the result of f() */
    public <R> Option<R> flatMap(Function<T, Option<R>> f) {
        return isEmpty() ? none : f.apply(this.get()); // see @map
    }

    /** @return I need not mention to use this with care */
    public T orNull() { return getOrElse(null); }
    
    /** If the state value is null, this will be returned instead
     *  @param other the fallback
     *  @return state or other */
    public T getOrElse(T other) {
        return isEmpty() ? other : this.get();
    }

    /** If the state value is null, this supplier will be called instead
     *  @param other the fallback-callback
     *  @return state or the result of the callback */
    public T orElseGet(Supplier<T> other) {
        return isEmpty() ? other.get() : this.get();
    }

    public Option<T> filter(Predicate<T> condition) {
        return (!isEmpty() && condition.test(this.get())) ? this : none;
    }

    /* Bridges the gap between implementations with Javas Optional */
    public Optional<T> toOptional() { return Optional.ofNullable(this.get()); }
    public static <T> Option<T> fromOptional(Optional<T> t) {
        return t.map(Some::apply).orElseGet(None::apply);
    }
}
