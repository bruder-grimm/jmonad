package org.brudergrimm.jmonad.tried;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class TryTest {
    private TestClass testClass;

    @BeforeEach void construct() {
        testClass = new TestClass();
    }

    @Test void testConstructionThrowing() {
        Try<Integer> tried = Try.applyThrowing(() -> testClass.throwing());
        assertFalse(tried.isSuccess());
    }

    @Test void testConstructionUndeclared() {
        Try<Integer> tried = Try.apply(() -> testClass.undeclaredThrowing());
        assertFalse(tried.isSuccess());
    }

    @Test void testConstructionRuntime() {
        Try<Integer> tried = Try.apply(() -> testClass.runtimeException());
        assertFalse(tried.isSuccess());
    }

    @Test void testConstruction() {
        Try<Integer> tried = Try.apply(() -> testClass.returns());
        assertTrue(tried.isSuccess());
    }

    @Test void testMap() {
        Try<Integer> number = Try.apply(() -> 5);
        Try<Integer> doubled = number.map(i -> i * 2);
        Try<Integer> failed = number.map(i -> {
            throw new RuntimeException();
        });

        assertFalse(failed.isSuccess());
        assertEquals(10, doubled.get());
    }

    @Test void testFlatMap() {
        Try<Integer> number = Try.apply(() -> 5);
        Try<Integer> doubled = number.flatMap(i -> Try.apply(() -> i * 2));
        Try<Integer> failed = number.flatMap(i -> Try.apply(() -> {
            throw new RuntimeException();
        }));

        assertFalse(failed.isSuccess());
        assertEquals(10, doubled.get());
    }

    @Test void testRecover() {
        Try<Integer> failed = Try.applyThrowing(() -> testClass.throwing());
        Try<Integer> recovered = failed.recover(exception -> {
            String message = exception.getMessage();
            return 10;
        });

        assertEquals(10, recovered.get());
    }

    @Test void testRecoverWith() {
        Try<Integer> failed = Try.applyThrowing(() -> testClass.throwing());
        Try<Integer> recovered = failed.recoverWith(exception -> Try.apply(() -> 10));

        assertEquals(10, recovered.get());
    }

    @Test void testFilter() {
        Try<Integer> number = Try.apply(() -> 5);
        Try<Integer> filteredNo = number.filter(a -> a % 2 == 0);
        Try<Integer> filteredYes = number.filter(a -> a == 5);

        assertFalse(filteredNo.isSuccess());
        assertTrue(filteredYes.isSuccess());
    }

    @Test void testGetOrElse() {
        Try<String> nil = Try.apply(() -> {
            throw new RuntimeException("a");
        });
        String orElse = nil.getOrElse("a");

        assertEquals(orElse, "a");
    }


    static class TestClass {
        int throwing() throws IOException {
            throw new IOException("Expected");
        }
        int undeclaredThrowing() {
            throw new ArithmeticException("Unexpected");
        }
        int runtimeException() {
            throw new RuntimeException(new ArithmeticException("RuntimeError"));
        }
        int returns() {
            return 5;
        }
    }
}
