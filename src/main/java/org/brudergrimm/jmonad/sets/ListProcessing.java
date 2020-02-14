package org.brudergrimm.jmonad.sets;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ListProcessing {
    public static <T> List<T> union(List<T> a, List<T> b) {
        return Stream.concat(a.stream(), b.stream()).collect(Collectors.toList());
    }

    public static <T> List<T> intersection(List<T> a, List<T> b) {
        return a.stream()
                .filter(b::contains)
                .collect(Collectors.toList());
    }

    public static <T> List<T> difference(List<T> a, List<T> without) {
        return a.stream()
                .filter(val -> !without.contains(val))
                .collect(Collectors.toList());
    }

    public static <T> List<T> symmetricDifference(List<T> a, List<T> b) {
        return difference(union(a, b), intersection(a, b));
    }
}
