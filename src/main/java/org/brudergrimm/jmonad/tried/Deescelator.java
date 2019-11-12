package org.brudergrimm.jmonad.tried;

import org.brudergrimm.jmonad.tried.function.*;
import java.util.function.Function;
import java.util.function.Supplier;

public class Deescelator {
    static <T> Supplier<T> asDeescelatedSupplier(ThrowingSupplier<T> throwingSupplier) {
        return () -> {
            try {
                return throwingSupplier.get();
            }
            catch (VirtualMachineError | ThreadDeath | LinkageError fatal) { throw fatal; }
            catch (Throwable e) { throw new RuntimeException(e); } };
    }

    static <T, R> Function<T, R> asDeescelatedFunction(ThrowingFunction<T, R> throwingFunction) {
        return (r) -> {
            try {
                return throwingFunction.apply(r);
            }
            catch (VirtualMachineError | ThreadDeath | LinkageError fatal) { throw fatal; }
            catch (Throwable e) { throw new RuntimeException(e); }
        };
    }
}
