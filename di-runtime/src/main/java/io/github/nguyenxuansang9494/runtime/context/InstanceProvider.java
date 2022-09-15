package io.github.nguyenxuansang9494.runtime.context;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

import io.github.nguyenxuansang9494.annotations.Runner;
import io.github.nguyenxuansang9494.runtime.processor.ClassProcessor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import io.github.nguyenxuansang9494.annotations.Component;
import io.github.nguyenxuansang9494.annotations.ComponentScope;
import io.github.nguyenxuansang9494.annotations.Inject;
import io.github.nguyenxuansang9494.runtime.exception.FailedToRegisterDependencyException;
import io.github.nguyenxuansang9494.runtime.exception.UnsupportedClassException;
import lombok.Getter;

@Getter
public class InstanceProvider {
    private static final Logger LOGGER = LogManager.getLogger(InstanceProvider.class);
    private final DIContext context = SimpleDIContext.getContext();
    private final DIContextHelper contextHelper = DIContextHelper.getInstance();
    private final ClassProcessor classProcessor = ClassProcessor.getInstance();
    private final Class<?> clazz;
    private final Field[] injectAnnotatedField;
    private final Method[] componentAnnotatedMethod;
    private final Method[] runnerAnnotatedMethod;
    private final Method instantiateMethod;
    private final Object configObject;

    public InstanceProvider(Class<?> clazz) {
        this.clazz = clazz;
        this.injectAnnotatedField = classProcessor.findInheritedAnnotatedFields(clazz, Inject.class).toArray(new Field[]{});
        this.componentAnnotatedMethod = classProcessor.findDeclaredAnnotatedMethods(clazz, Component.class).toArray(new Method[]{});
        this.runnerAnnotatedMethod = classProcessor.findDeclaredAnnotatedMethods(clazz, Runner.class).toArray(new Method[]{});
        this.instantiateMethod = null;
        this.configObject = null;
    }

    public InstanceProvider(Object configObject, Method instantiateMethod) {
        this.clazz = instantiateMethod.getReturnType();
        this.configObject = configObject;
        this.instantiateMethod = instantiateMethod;
        this.injectAnnotatedField = new Field[]{};
        this.componentAnnotatedMethod = new Method[]{};
        this.runnerAnnotatedMethod = new Method[]{};
    }

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
                if (ComponentScope.SINGLETON.equals(component.scope())) {
                    context.registerComponent(method.getReturnType(), method.invoke(instance));
                } else {
                    contextHelper.addPrototypeInstanceProvider(instance, method);
                }
            }
            for (Method method : runnerAnnotatedMethod) {
                contextHelper.registerExecutor(method, instance);
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
        Class<?> injectQualifiedClass = field.getDeclaredAnnotation(Inject.class).qualified();
        List<Class<?>> childClasses = contextHelper.getChildClasses(field.getType());
        if (injectQualifiedClass != Object.class) {
            if (!field.getType().isAssignableFrom(injectQualifiedClass)) {
                throw new FailedToRegisterDependencyException("InstanceProvider.setField - invalid qualified type.");
            }
            setField(instance, field, injectQualifiedClass);
            return;
        }
        if (childClasses.size() != 1) {
            throw new FailedToRegisterDependencyException("InstanceProvider.setField - can not decide valid component");
        }
        injectQualifiedClass = childClasses.get(0);
        setField(instance, field, injectQualifiedClass);

    }

    private void setField(Object instance, Field field, Class<?> injectQualifiedClass) throws IllegalAccessException {
        Component component;
        ComponentScope scope;
        component = injectQualifiedClass.getDeclaredAnnotation(Component.class);
        if (component != null) {
            scope = component.scope();
        } else {
            scope = contextHelper.getPrototypeInstanceProvider(injectQualifiedClass) == null ? ComponentScope.SINGLETON : ComponentScope.PROTOTYPE;
        }
        if (ComponentScope.SINGLETON.equals(scope)) {
            setFieldSingleton(instance, field, injectQualifiedClass);
        } else {
            setFieldPrototype(instance, field, injectQualifiedClass);
        }
    }

    private void setFieldPrototype(Object instance, Field field, Class<?> clazz) throws IllegalAccessException {
        field.setAccessible(true);
        Object registeredComponent = contextHelper.registerComponent(clazz);
        while (registeredComponent == null) {
            registeredComponent = contextHelper.registerComponent(clazz);
        }
        field.set(instance, registeredComponent);
    }

    private void setFieldSingleton(Object instance, Field field, Class<?> clazz) throws IllegalAccessException {
        field.setAccessible(true);
        List<Object> objects = context.getComponents(clazz);
        if (objects.size() > 1) {
            throw new FailedToRegisterDependencyException("InstanceBuilder.setField - more or less than one valid bean");
        } else if (objects.isEmpty()) {
            field.set(instance, contextHelper.registerComponent(clazz));
        } else {
            field.set(instance, objects.get(0));
        }
    }
}
