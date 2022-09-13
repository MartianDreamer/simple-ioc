package io.github.nguyenxuansang9494.runtime;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class InstanceBuilder {
    private final DIContext context;
    private final Class<?> clazz;
    private final Field[] injectAnnotatedField;
    private final Method[] componentAnnotatedMethod;

    public DIContext getContext() {
        return context;
    }

    public Class<?> getClazz() {
        return clazz;
    }

    public Field[] getInjectAnnotatedField() {
        return injectAnnotatedField;
    }

    public Method[] getComponentAnnotatedMethod() {
        return componentAnnotatedMethod;
    }

    public InstanceBuilder(DIContext context, Class<?> clazz, Field[] injectAnnotatedField, Method[] componentAnnotatedMethod) {
        this.context = context;
        this.clazz = clazz;
        this.injectAnnotatedField = injectAnnotatedField;
        this.componentAnnotatedMethod = componentAnnotatedMethod;
    }

    public Object buildInstance() {
        return null;
    }
}
