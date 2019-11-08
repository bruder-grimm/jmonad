package org.brudergrimm.jmonad.either;

import java.util.NoSuchElementException;
import java.util.function.Function;

final public class LeftProjection<L, R> {
    private Either<L, R> either;
    LeftProjection(Either<L, R> either) {
        this.either = either;
    }
    public L get() {
        if (this.either instanceof Left) {
            return ((Left<L, R>) this.either).get();
        }
        throw new NoSuchElementException("LeftProjection of Right");
    }
    public <T> Either<T, R> map(Function<L, T> fn) {
        if (this.either instanceof Left) {
            return Left.apply(fn.apply(((Left<L, R>) this.either).get()));
        }
        return (Either<T, R>) this.either;
    }
}
