package io.github.nguyenxuansang9494.runtime;

import io.github.nguyenxuansang9494.runtime.exception.UnsupportedClass;

public final class SimpleDIApplication {
    private SimpleDIApplication() {
        super();
    }

    public static void run(Class<?> clazz, String[] args) {
        try {
            clazz.getDeclaredMethod("main", String[].class);
        } catch (NoSuchMethodException | SecurityException e) {
            throw new UnsupportedClass(e);
        }
    }
}
