package org.brudergrimm.jmonad.either;

import org.brudergrimm.jmonad.option.None;
import org.brudergrimm.jmonad.option.Option;
import org.brudergrimm.jmonad.option.Some;

import java.util.function.Function;

/** Java has no data type to represent a disjunct set. This is why I created Either
 *  You may either create a Left or Right that may hold it's corresponding type (L and R respectively).
 *  A callee may now expect an Either of <\L, R>, which he has to account for.
 *  @param <L> left of the disjunct value
 *  @param <R> right of the disjunct value */
public abstract class Either<L, R> {
    public abstract boolean isLeft();
    public abstract boolean isRight();

    public LeftProjection<L, R> left() {
        return new LeftProjection<>(this);
    }

    public RightProjection<L, R> right() {
        return new RightProjection<>(this);
    }

    public <T> T fold(Function<L, T> functionL, Function<R, T> functionR) {
        if (this instanceof Left) {
            return functionL.apply(((Left<L, R>) this).get());
        }
        return functionR.apply(((Right<L, R>) this).get());
    }

    public Option<R> toOption() {
        if (this instanceof Right) {
            return Some.apply(((Right<L, R>) this).get());
        }
        return None.apply();
    }
}

