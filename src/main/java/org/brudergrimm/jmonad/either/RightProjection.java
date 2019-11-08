package org.brudergrimm.jmonad.either;

import java.util.NoSuchElementException;
import java.util.function.Function;

final public class RightProjection<L, R> {
    private Either<L, R> either;
    RightProjection(Either<L, R> either) { this.either = either; }

    public R get() {
        if (this.either instanceof Right) {
            return ((Right<L, R>) this.either).get();
        }
        throw new NoSuchElementException("RightProjection of Right");
    }
    public <T> Either<L, T> map(Function<R, T> fn) {
        if (this.either instanceof Right) {
            return Right.apply(fn.apply(((Right<L, R>) this.either).get()));
        }
        return (Either<L, T>) this.either;
    }
}
