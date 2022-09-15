package io.github.nguyenxuansang9494.runtime;

import io.github.nguyenxuansang9494.runtime.context.DIContext;
import io.github.nguyenxuansang9494.runtime.context.DIContextHelper;
import io.github.nguyenxuansang9494.runtime.context.SimpleDIContext;
import io.github.nguyenxuansang9494.runtime.exception.UnsupportedClassException;

public final class SimpleDIApplication {
    private SimpleDIApplication() {
        super();
    }

    public static void run(Class<?> clazz) {
        try {
            clazz.getDeclaredMethod("main", String[].class);
            setUpApplication(clazz);
        } catch (NoSuchMethodException | SecurityException e) {
            throw new UnsupportedClassException(e);
        }
    }

    private static void setUpApplication(Class<?> clazz) {
        DIContext context = SimpleDIContext.getContext();
        DIContextHelper contextHelper = DIContextHelper.getInstance();
        contextHelper.setUpContext(clazz);
        context.executeRunners();
    }
}
