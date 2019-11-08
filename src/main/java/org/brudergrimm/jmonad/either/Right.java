package org.brudergrimm.jmonad.either;

final public class Right<L, R> extends Either<L, R> {
    private final R value;
    private Right(R value) {
        this.value = value;
    }
    public static <L, R> Right<L, R> apply(R value) { return new Right<>(value); }

    public R get() { return this.value; }

    @Override public boolean isLeft() { return false; }
    @Override public boolean isRight() { return true; }
}
