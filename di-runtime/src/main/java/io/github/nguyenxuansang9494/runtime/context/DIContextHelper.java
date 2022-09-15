package io.github.nguyenxuansang9494.runtime.context;

import io.github.nguyenxuansang9494.annotations.Component;
import io.github.nguyenxuansang9494.annotations.ComponentScope;
import io.github.nguyenxuansang9494.annotations.Configuration;
import io.github.nguyenxuansang9494.runtime.exception.UnsupportedClassException;
import io.github.nguyenxuansang9494.runtime.processor.ClassPathProcessor;
import io.github.nguyenxuansang9494.runtime.processor.ClassProcessor;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class DIContextHelper {
    private final ClassProcessor classProcessor = ClassProcessor.getInstance();
    private final ClassPathProcessor classPathProcessor = ClassPathProcessor.getInstance();
    private final DIContext context = SimpleDIContext.getContext();
    private final Map<Class<?>, InstanceProvider> prototypeInstanceProviderMap = new HashMap<>();
    private Set<Class<?>> registeredClasses;
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
        return registeredClasses.stream().filter(clazz::isAssignableFrom).collect(Collectors.toList());
    }

    public Object registerComponent(Class<?> clazz) {
        Component component = clazz.getDeclaredAnnotation(Component.class);
        Configuration configuration = clazz.getDeclaredAnnotation(Configuration.class);
        if (!registeredClasses.contains(clazz)) {
            throw new UnsupportedClassException("DIContextHelper.registerComponent - no bean is declared.");
        }
        if (configuration != null || (prototypeInstanceProviderMap.get(clazz) == null && ComponentScope.SINGLETON.equals(component.scope()))) {
            Object provideInstance = context.getComponent(clazz);
            if (provideInstance != null) {
                return provideInstance;
            }
            provideInstance = new InstanceProvider(clazz).instantiate();
            context.registerComponent(clazz, provideInstance);
            return provideInstance;
        }
        if (prototypeInstanceProviderMap.get(clazz) != null) {
            Object providedInstance = prototypeInstanceProviderMap.get(clazz).instantiate();
            context.registerComponent(clazz, providedInstance);
            return providedInstance;
        }
        prototypeInstanceProviderMap.put(clazz, new InstanceProvider(clazz));
        return null;
    }


    public void setUpContext(Class<?> mainClass) {
        List<Class<?>> componentClasses = Stream.of(classPathProcessor.scanAllClasses(mainClass)).filter(c -> c.getDeclaredAnnotation(Component.class) != null || c.getDeclaredAnnotation(Configuration.class) != null).collect(Collectors.toList());
        this.registeredClasses = new HashSet<>(componentClasses);
        for (Class<?> clazz : componentClasses) {
            if (clazz.getDeclaredAnnotation(Configuration.class) != null) {
                List<Method> componentMethods = classProcessor.findDeclaredAnnotatedMethods(clazz, Component.class);
                registeredClasses.addAll(componentMethods.stream().map(Method::getReturnType).collect(Collectors.toList()));
                registerComponent(clazz);
            }
        }
        for (Class<?> clazz : componentClasses) {
            if (clazz.getDeclaredAnnotation(Component.class) != null) {
                registerComponent(clazz);
            }
        }
    }


    public void registerExecutor(Method method, Object object) {
        context.registerExecutor(new MethodExecutor(method, object));
    }
}
