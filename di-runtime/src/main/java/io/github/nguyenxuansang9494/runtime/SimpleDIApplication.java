package io.github.nguyenxuansang9494.runtime;

import io.github.nguyenxuansang9494.annotations.ComponentScan;
import io.github.nguyenxuansang9494.runtime.context.DIContext;
import io.github.nguyenxuansang9494.runtime.context.DIContextHelper;
import io.github.nguyenxuansang9494.runtime.context.SimpleDIContext;
import io.github.nguyenxuansang9494.runtime.exception.UnsupportedClassException;

public final class SimpleDIApplication {
    private SimpleDIApplication() {
        super();
    }

    private static String[] args;

    public static void run(Class<?> clazz, String[] args) {
        try {
            clazz.getDeclaredMethod("main", String[].class);
            SimpleDIApplication.args = args;
            setUpApplication(clazz);
        } catch (NoSuchMethodException | SecurityException e) {
            throw new UnsupportedClassException(e);
        }
    }

    public static String[] getArgs() {
        return args;
    }

    private static void setUpApplication(Class<?> clazz) {
        DIContext context = SimpleDIContext.getContext();
        DIContextHelper contextHelper = DIContextHelper.getInstance();
        ComponentScan componentScan = clazz.getDeclaredAnnotation(ComponentScan.class);
        String[] scannedComponent = new String[] { clazz.getPackage().getName() };
        if (componentScan!=null) {
            scannedComponent = componentScan.packages();
        }
        contextHelper.setUpContext(clazz, scannedComponent);
        context.executeRunners();
    }
}
