package org.brudergrimm.jmonad.option;

import java.util.NoSuchElementException;
import java.util.function.Consumer;

public class None extends Option<Void> {
    private static None instance = null;
    private None() { }

    public static Option apply() {
        if (instance == null) { instance = new None(); }
        return instance;
    }

    @Override public boolean isEmpty() { return true; }
    @Override public Void get() { throw new NoSuchElementException("None.get"); }

    @Override public Option<Void> ifSome(Consumer<Void> consumer) { return this; }
    @Override public Option<Void> ifNone(Runnable runnable) {
        runnable.run();
        return this;
    }

    @Override public String toString() { return "None"; }
}
