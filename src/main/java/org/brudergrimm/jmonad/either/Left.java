package org.brudergrimm.jmonad.either;

final public class Left<L, R> extends Either<L, R> {
    private final L value;
    private Left(L value) {
        this.value = value;
    }
    public static <L, R> Left<L, R> apply(L left) { return new Left<>(left); }

    public L get() { return this.value; }

    @Override public boolean isLeft() { return true; }
    @Override public boolean isRight() { return false; }
}
