package io.github.nguyenxuansang9494.runtime;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import io.github.nguyenxuansang9494.annotations.Component;
import io.github.nguyenxuansang9494.annotations.ComponentScope;
import io.github.nguyenxuansang9494.annotations.Inject;
import io.github.nguyenxuansang9494.runtime.exception.FailedToInjectDependencyException;
import io.github.nguyenxuansang9494.runtime.exception.UnsupportedClassException;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class InstanceProvider {
    private static final Logger LOGGER = LogManager.getLogger(InstanceProvider.class);
    private final DIContext context;
    private final DIContextHelper contextHelper;
    private final Class<?> clazz;
    private final Field[] injectAnnotatedField;
    private final Method[] componentAnnotatedMethod;
    private final Method instantiateMethod;
    private final Object configObject;

    public Object instantiate() {
        try {
            if (instantiateMethod != null && configObject != null) {
                return instantiateMethod.invoke(configObject);
            }
            Constructor<?> constructor = clazz.getConstructor();
            Object instance = constructor.newInstance();
            for (Field field : injectAnnotatedField) {
                setField(instance, field);
            }
            for (Method method : componentAnnotatedMethod) {
                Component component = method.getAnnotation(Component.class);
                if (ComponentScope.PROTOTYPE.equals(component.scope())) {
                    continue;
                }
                context.registerComponent(method.getReturnType(), method.invoke(instance));
            }
            return instance;
        } catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException
                | IllegalArgumentException | InvocationTargetException e) {
            LOGGER.error("InstanceBuilder.instantiate - {}", e.getMessage());
            throw new UnsupportedClassException(e);
        }
    }

    private void setField(Object instance, Field field) throws IllegalArgumentException, IllegalAccessException {
        field.setAccessible(true);
        Inject inject = field.getAnnotation(Inject.class);
        Class<?> qualifiedClass = inject.qualified();
        Class<?> fieldClass = field.getType();
        Component component = fieldClass.getAnnotation(Component.class);
        if (ComponentScope.PROTOTYPE.equals(component.scope())) {
            field.set(instance, contextHelper.registerComponent(fieldClass));
            return;
        }
        if (qualifiedClass != null) {
            if (!fieldClass.isAssignableFrom(qualifiedClass)) {
                throw new UnsupportedClassException("InstanceBuilder.setField - invalid qualified class.");
            }
            List<Object> objects = context.getComponents(fieldClass);
            if (objects.size() != 1) {
                throw new FailedToInjectDependencyException(
                        "InstanceBuilder.setField - more or less than one valid bean");
            }
            field.set(instance, objects.get(0));
        }
        List<Object> objects = context.getChildrenClassComponent(fieldClass);
        if (objects.size() != 1) {
            throw new FailedToInjectDependencyException("InstanceBuilder.setField - more or less than one valid bean");
        }
        field.set(instance, objects.get(0));
    }
}
