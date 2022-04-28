package ru.shelest.utils;

import java.util.function.Supplier;

public final class ConditionChecker {

    private ConditionChecker() {}

    public static void require(boolean condition, Supplier<RuntimeException> exceptionSupplier) {
        if (!condition) {
            throw exceptionSupplier.get();
        }
    }
}
