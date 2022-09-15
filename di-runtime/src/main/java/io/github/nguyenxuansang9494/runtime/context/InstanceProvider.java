package io.github.nguyenxuansang9494.runtime.context;

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
    private int priorityLevel;
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
                Component component = method.getDeclaredAnnotation(Component.class);
                contextHelper.add(method.getReturnType(), method, instance);
                if (ComponentScope.SINGLETON.equals(component.scope())) {
                    context.registerComponent(method.getReturnType(), method.invoke(instance));
                }
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
        Class<?> fieldClass = field.getType();
        Component component = fieldClass.getDeclaredAnnotation(Component.class);
        ComponentScope scope;
        if (component == null) {
            InstanceProvider instanceProvider = contextHelper.getInstanceProvider(fieldClass);
            if (instanceProvider == null) {
                throw new UnsupportedClassException("InstanceProvider.setField - no bean found");
            }
            scope = instanceProvider.getInstantiateMethod().getDeclaredAnnotation(Component.class).scope();
        } else {
            scope = component.scope();
        }
        if (ComponentScope.PROTOTYPE.equals(scope)) {
            setFieldPrototype(instance, field);
            return;
        }
        setFieldSingleton(instance, field);
    }

    private void setFieldPrototype(Object instance, Field field) throws IllegalAccessException {
        field.setAccessible(true);
        Inject inject = field.getDeclaredAnnotation(Inject.class);
        Class<?> qualifiedClass = inject.qualified();
        Class<?> fieldClass = field.getType();
        if (qualifiedClass != Object.class) {
            if (!fieldClass.isAssignableFrom(qualifiedClass)) {
                throw new UnsupportedClassException("InstanceBuilder.setField - invalid qualified class.");
            }
            field.set(instance, contextHelper.registerComponent(qualifiedClass));
            return;
        }
        List<Class<?>> childClasses = contextHelper.getChildClasses(fieldClass);
        if (childClasses.size() != 1) {
            throw new FailedToInjectDependencyException("InstanceBuilder.setPrototypeField - more or less than one valid bean");
        }
        field.set(instance, contextHelper.registerComponent(childClasses.get(0)));
    }

    private void setFieldSingleton(Object instance, Field field) throws IllegalAccessException {
        field.setAccessible(true);
        Inject inject = field.getDeclaredAnnotation(Inject.class);
        Class<?> qualifiedClass = inject.qualified();
        Class<?> fieldClass = field.getType();
        if (qualifiedClass != Object.class) {
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
