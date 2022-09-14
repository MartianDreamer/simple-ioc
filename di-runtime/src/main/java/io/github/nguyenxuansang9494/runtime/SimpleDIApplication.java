package io.github.nguyenxuansang9494.runtime;

import io.github.nguyenxuansang9494.runtime.exception.UnsupportedClassException;

import java.util.HashMap;
import java.util.Map;

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
        ClassPathProcessor classPathProcessor = new ClassPathProcessor();
        ClassProcessor classProcessor = new ClassProcessor();
        Map<Class<?>, InstanceProvider> instanceProviderMap = new HashMap<>();
        DIContextHelper contextHelper = new DIContextHelper(classProcessor, classPathProcessor, context, instanceProviderMap);
        contextHelper.setUpContext(clazz);
    }
}
