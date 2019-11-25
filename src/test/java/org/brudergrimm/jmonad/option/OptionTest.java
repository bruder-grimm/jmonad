package org.brudergrimm.jmonad.option;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.function.Predicate;

import static org.junit.jupiter.api.Assertions.*;

class OptionTest {
    private TestClass testClass;

    @BeforeEach void construct() {
        testClass = new TestClass();
    }

    @Test void testConstruct() {
        Option<String> nil = Option.apply(testClass.returnsNull());
        Option<String> empty = Option.apply(testClass.returnsEmptyString());
        Option<String> string = Option.apply(testClass.returnsString());

        assertFalse(nil.isDefined());
        assertTrue(empty.isDefined());
        assertTrue(string.isDefined());
    }

    @Test void testMap() {
        Option<String> string = Option.apply(testClass.returnsString());
        Option<String> upper = string.map(String::toUpperCase);
        Option<String> nulled = string.map(s -> null);

        assertTrue(nulled.isEmpty());
        assertEquals("HELLO", upper.get());
    }

    @Test void testFlatMap() {
        Option<String> string = Option.apply(testClass.returnsString());
        Option<String> upper = string.flatMap(testClass::lowered);
        Option<String> nulled = string.flatMap(testClass::nulled);

        assertTrue(nulled.isEmpty());
        assertEquals("HELLO", upper.get());
    }

    @Test void testFilter() {
        Option<String> string = Option.apply(testClass.returnsString());
        Option<String> empty = Option.apply(testClass.returnsEmptyString());

        Predicate<String> stringPredicate = s -> s.length() > 0;

        Option<String> filteredNonEmpty = string.filter(stringPredicate);
        Option<String> filteredEmpty = empty.filter(stringPredicate);

        assertTrue(filteredNonEmpty.isDefined());
        assertFalse(filteredEmpty.isDefined());
    }

    @Test void testFilter2() {
        Option<Integer> integer = Some.apply(2);
        Option<Integer> filtered = integer.filter(content -> content.equals(2));

        assertTrue(filtered.isDefined());
        assertEquals(2, filtered.get());
    }

    @Test void testGetOrElse() {
        Option<String> nil = Option.apply(testClass.returnsNull());
        String notNull1 = nil.getOrElse("a");
        String notNull2 = nil.orElseGet(() -> "a");
        String isNull = nil.orNull();

        assertEquals(notNull1, notNull2);
        assertNull(isNull);
    }

    static class TestClass {
        String returnsNull() {
            return null;
        }
        String returnsEmptyString() {
            return "";
        }
        String returnsString() {
            return "Hello";
        }
        Option<String> lowered(String string) {
            return Option.apply(string.toUpperCase());
        }
        Option<String> nulled(String string) {
            return Option.apply(null);
        }
    }
}
