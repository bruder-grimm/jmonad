package org.brudergrimm.jmonad.either;

import org.junit.jupiter.api.Test;

import java.util.NoSuchElementException;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;

class EitherTest {
    @Test void testConstruct() {
        Either<Integer, String> either1 = Right.apply("String");
        Either<Integer, String> either2 = Left.apply(5);

        assertTrue(either1.isRight());
        assertTrue(either2.isLeft());
        assertFalse(either1.isLeft());
        assertFalse(either2.isRight());
    }

    @Test void testProjections() {
        Either<Integer, String> left = Left.apply(5);
        Either<Integer, String> right = Right.apply("String");

        Either<Integer, String> doubledLeft = left.left().map(a -> a * 2);
        Either<Integer, String> unchangedLeft = left.right().map(String::toLowerCase);

        Either<Integer, String> upperRight = right.right().map(String::toUpperCase);
        Either<Integer, String> unchangedRight = right.left().map(a -> a * 3);

        assertEquals(10, doubledLeft.left().get());
        assertThrows(NoSuchElementException.class, () -> unchangedLeft.right().get());

        assertEquals("STRING", upperRight.right().get());
        assertThrows(NoSuchElementException.class, () -> unchangedRight.left().get());
    }

    @Test void fold() {
        Either<Integer, String> right = Right.apply("5");
        Either<Integer, String> left = Left.apply(5);

        Function<String, Double> convertsRight = Double::valueOf;
        Function<Integer, Double> doublesLeft = i -> Double.parseDouble(i.toString() + "d") * 2;

        Double fromRight = right.fold(doublesLeft, convertsRight);
        Double fromLeft = left.fold(doublesLeft, convertsRight);

        assertEquals( 5, fromRight);
        assertEquals(10, fromLeft);
    }
}
