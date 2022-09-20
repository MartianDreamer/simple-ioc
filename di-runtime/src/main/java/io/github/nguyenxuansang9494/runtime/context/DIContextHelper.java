package io.github.nguyenxuansang9494.runtime.context;

import io.github.nguyenxuansang9494.annotations.Component;
import io.github.nguyenxuansang9494.annotations.ComponentScope;
import io.github.nguyenxuansang9494.annotations.Configuration;
import io.github.nguyenxuansang9494.runtime.context.model.OptionalObject;
import io.github.nguyenxuansang9494.runtime.exception.UnsupportedClassException;
import io.github.nguyenxuansang9494.runtime.processor.ClassPathProcessor;
import io.github.nguyenxuansang9494.runtime.processor.ClassProcessor;
import io.github.nguyenxuansang9494.runtime.processor.PostConstructProcessor;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class DIContextHelper {
    private final ClassProcessor classProcessor = ClassProcessor.getInstance();
    private final ClassPathProcessor classPathProcessor = ClassPathProcessor.getInstance();
    private final DIContext context = SimpleDIContext.getContext();
    private final Map<Class<?>, InstanceProvider> prototypeInstanceProviderMap = new HashMap<>();
    private final Map<Class<?>, Annotation> registeredClasses = new HashMap<>();
    private final Map<Class<?>, Object> partlyInstantiatingObjects = new HashMap<>();
    private static final DIContextHelper instance = new DIContextHelper();

    public static DIContextHelper getInstance() {
        return instance;
    }

    public void addPrototypeInstanceProvider(Object object, Method method) {
        InstanceProvider instanceProvider = new InstanceProvider(object, method);
        prototypeInstanceProviderMap.put(method.getReturnType(), instanceProvider);
    }

    public InstanceProvider getPrototypeInstanceProvider(Class<?> clazz) {
        return prototypeInstanceProviderMap.get(clazz);
    }

    public List<Class<?>> getChildClasses(Class<?> clazz) {
        return registeredClasses.keySet().stream().filter(clazz::isAssignableFrom).collect(Collectors.toList());
    }

    public OptionalObject registerComponent(Class<?> clazz, Set<Class<?>> dependantClasses) {
        if (!registeredClasses.containsKey(clazz)) {
            throw new UnsupportedClassException("DIContextHelper.registerComponent - no bean is declared.");
        }
        Annotation annotation = registeredClasses.get(clazz);
        OptionalObject optionalObject;
        Object providedInstance;
        if (annotation instanceof Configuration || ((Component) annotation).scope().equals(ComponentScope.SINGLETON)) {
            providedInstance = context.getComponent(clazz);
            if (providedInstance != null) {
                return new OptionalObject(providedInstance, true);
            }
            providedInstance = partlyInstantiatingObjects.get(clazz);
            if (providedInstance != null) {
                return new OptionalObject(providedInstance, true);
            }
            optionalObject = new InstanceProvider(clazz).provide(dependantClasses);
        } else {
            if (!prototypeInstanceProviderMap.containsKey(clazz)) {
                prototypeInstanceProviderMap.put(clazz, new InstanceProvider(clazz));
                return null;
            }
            optionalObject = prototypeInstanceProviderMap.get(clazz).provide(dependantClasses);
        }
        providedInstance = optionalObject.getObject();
        if (optionalObject.isComplete()) {
            context.registerComponent(clazz, optionalObject.getObject());
            PostConstructProcessor.invoke(optionalObject.getObject());
            dependantClasses.remove(clazz);
        } else if (providedInstance != null) {
            partlyInstantiatingObjects.put(clazz, providedInstance);
            dependantClasses.remove(clazz);
        }
        if (dependantClasses.isEmpty()) {
            for (Map.Entry<Class<?>, Object> entry : partlyInstantiatingObjects.entrySet()) {
                context.registerComponent(entry.getKey(), classProcessor.wiringUnwiredFields(entry.getValue(), partlyInstantiatingObjects));
            }
            partlyInstantiatingObjects.clear();
        }
        return optionalObject;
    }

    public void registerComponent(Class<?> clazz) {
        registerComponent(clazz, new HashSet<>());
    }


    public void setUpContext(Class<?> mainClass, String[] packages) {
        List<Class<?>> allClasses = classPathProcessor.scanAllClasses(mainClass, packages);
        for (Class<?> clazz : allClasses) {
            Component component = clazz.getDeclaredAnnotation(Component.class);
            Configuration configuration = clazz.getDeclaredAnnotation(Configuration.class);
            if (component != null) {
                registeredClasses.put(clazz, component);
            } else if (configuration != null) {
                registeredClasses.put(clazz, configuration);
            }
        }
        final List<Method> componentMethodSet = new LinkedList<>();
        for (Class<?> clazz : registeredClasses.keySet()) {
            if (clazz.getDeclaredAnnotation(Configuration.class) != null) {
                List<Method> componentMethods = classProcessor.findDeclaredAnnotatedMethods(clazz, Component.class);
                componentMethodSet.addAll(componentMethods);
                registerComponent(clazz);
            }
        }
        for (Method method : componentMethodSet) {
            registeredClasses.put(method.getReturnType(), method.getDeclaredAnnotation(Component.class));
        }
        for (Class<?> clazz : allClasses) {
            if (clazz.getDeclaredAnnotation(Component.class) != null) {
                registerComponent(clazz);
            }
        }
    }


    public void registerExecutor(Method method, Object object) {
        context.registerExecutor(new MethodExecutor(method, object));
    }
}
