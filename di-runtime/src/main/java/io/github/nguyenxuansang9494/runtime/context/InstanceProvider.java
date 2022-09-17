package io.github.nguyenxuansang9494.runtime.context;

import io.github.nguyenxuansang9494.annotations.Component;
import io.github.nguyenxuansang9494.annotations.ComponentScope;
import io.github.nguyenxuansang9494.annotations.Inject;
import io.github.nguyenxuansang9494.annotations.Runner;
import io.github.nguyenxuansang9494.runtime.context.model.OptionalObject;
import io.github.nguyenxuansang9494.runtime.exception.FailedToRegisterDependencyException;
import io.github.nguyenxuansang9494.runtime.exception.UnsupportedClassException;
import io.github.nguyenxuansang9494.runtime.processor.ClassProcessor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Set;

public class InstanceProvider {
    private static final Logger LOGGER = LogManager.getLogger(InstanceProvider.class);
    private final DIContext context = SimpleDIContext.getContext();
    private final DIContextHelper contextHelper = DIContextHelper.getInstance();
    private final Class<?> clazz;
    private final Field[] injectAnnotatedField;
    private final Method[] componentAnnotatedMethod;
    private final Method[] runnerAnnotatedMethod;
    private final Method instantiateMethod;
    private final Object configObject;

    public Class<?> getClazz() {
        return clazz;
    }

    public Field[] getInjectAnnotatedField() {
        return injectAnnotatedField;
    }

    public Method[] getComponentAnnotatedMethod() {
        return componentAnnotatedMethod;
    }

    public Method[] getRunnerAnnotatedMethod() {
        return runnerAnnotatedMethod;
    }

    public Method getInstantiateMethod() {
        return instantiateMethod;
    }

    public Object getConfigObject() {
        return configObject;
    }

    public InstanceProvider(Class<?> clazz) {
        this.clazz = clazz;
        ClassProcessor classProcessor = ClassProcessor.getInstance();
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

    public OptionalObject provide(Set<Class<?>> dependantClasses) {
        if (dependantClasses.contains(this.clazz)) {
            return new OptionalObject(null, false);
        }
        dependantClasses.add(this.clazz);
        try {
            boolean isComplete = true;
            if (instantiateMethod != null && configObject != null) {
                return new OptionalObject(instantiateMethod.invoke(configObject), true);
            }
            Constructor<?> constructor = clazz.getConstructor();
            Object instance = constructor.newInstance();
            for (Field field : injectAnnotatedField) {
                isComplete &= setField(instance, field, dependantClasses);
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
            return new OptionalObject(instance, isComplete);
        } catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException
                 | IllegalArgumentException | InvocationTargetException e) {
            LOGGER.error("InstanceBuilder.instantiate - {}", e.getMessage());
            throw new UnsupportedClassException(e);
        }
    }

    private boolean setField(Object instance, Field field, Set<Class<?>> dependantClasses) throws IllegalArgumentException, IllegalAccessException {
        field.setAccessible(true);
        Class<?> injectQualifiedClass = field.getDeclaredAnnotation(Inject.class).qualified();
        List<Class<?>> childClasses = contextHelper.getChildClasses(field.getType());
        if (injectQualifiedClass != Object.class) {
            if (!field.getType().isAssignableFrom(injectQualifiedClass)) {
                throw new FailedToRegisterDependencyException("InstanceProvider.setField - invalid qualified type.");
            }
            return setField(instance, field, injectQualifiedClass, dependantClasses);
        }
        if (childClasses.size() != 1) {
            throw new FailedToRegisterDependencyException("InstanceProvider.setField - can not decide valid component");
        }
        injectQualifiedClass = childClasses.get(0);
        return setField(instance, field, injectQualifiedClass, dependantClasses);
    }

    private boolean setField(Object instance, Field field, Class<?> injectQualifiedClass, Set<Class<?>> dependantClasses) throws IllegalAccessException {
        Component component;
        ComponentScope scope;
        component = injectQualifiedClass.getDeclaredAnnotation(Component.class);
        if (component != null) {
            scope = component.scope();
        } else {
            scope = contextHelper.getPrototypeInstanceProvider(injectQualifiedClass) == null ? ComponentScope.SINGLETON : ComponentScope.PROTOTYPE;
        }
        if (ComponentScope.SINGLETON.equals(scope)) {
            return setFieldSingleton(instance, field, injectQualifiedClass, dependantClasses);
        } else {
            return setFieldPrototype(instance, field, injectQualifiedClass, dependantClasses);
        }
    }

    private boolean setFieldPrototype(Object instance, Field field, Class<?> clazz, Set<Class<?>> dependentClasses) throws IllegalAccessException {
        field.setAccessible(true);
        OptionalObject optionalObject = contextHelper.registerComponent(clazz, dependentClasses);
        while (optionalObject == null) {
            optionalObject = contextHelper.registerComponent(clazz, dependentClasses);
        }
        if (optionalObject.isComplete()) {
            field.set(instance, optionalObject.getObject());
        }
        return optionalObject.isComplete();
    }

    private boolean setFieldSingleton(Object instance, Field field, Class<?> clazz, Set<Class<?>> dependentClasses) throws IllegalAccessException {
        field.setAccessible(true);
        List<Object> objects = context.getComponents(clazz);
        if (objects.size() > 1) {
            throw new FailedToRegisterDependencyException("InstanceBuilder.setField - more or less than one valid bean");
        } else if (objects.isEmpty()) {
            OptionalObject optionalObject = contextHelper.registerComponent(clazz, dependentClasses);
            if (optionalObject.isComplete()){
                field.set(instance, optionalObject.getObject());
            }
            return optionalObject.isComplete();
        } else {
            field.set(instance, objects.get(0));
            return true;
        }
    }
}
