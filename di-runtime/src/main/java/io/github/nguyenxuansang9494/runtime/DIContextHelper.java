package io.github.nguyenxuansang9494.runtime;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import io.github.nguyenxuansang9494.annotations.Component;
import io.github.nguyenxuansang9494.annotations.Inject;
import io.github.nguyenxuansang9494.runtime.exception.ClassNotFoundRuntimeException;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class DIContextHelper {
    private final ClassProcessor classProcessor;
    private final ClassPathProcessor classPathProcessor;
    private final DIContext context;
    private final Map<Class<?>, InstanceProvider> instanceProviderMap;

    public void add(Class<?> clazz, Method instantiateMethod, Object configObject) {
        instanceProviderMap.put(clazz, new InstanceProvider(context, this, 0, clazz, new Field[]{}, new Method[]{}, instantiateMethod, configObject));
    }

    public InstanceProvider get(Class<?> clazz) {
        return instanceProviderMap.get(clazz);
    }

    public void add(Class<?> clazz) {
        List<Field> injectAnnotatedFields = classProcessor.findInheritedAnnotatedFields(clazz, Inject.class);
        List<Method> componentAnnotatedDeclaredMethods = classProcessor.findDeclaredAnnotatedMethods(clazz, Component.class);
        int priorityLevel = calculatePriorityLevel(injectAnnotatedFields, Inject.class);
        InstanceProvider instanceProvider = new InstanceProvider(context, this, priorityLevel, clazz, injectAnnotatedFields.toArray(new Field[]{}),
                componentAnnotatedDeclaredMethods.toArray(new Method[]{}), null, null);
        instanceProviderMap.put(instanceProvider.getClazz(), instanceProvider);
    }

    private void registerComponents() {
        List<InstanceProvider> instanceProviders = instanceProviderMap.values().stream()
                .sorted(Comparator.comparingInt(InstanceProvider::getPriorityLevel))
                .collect(Collectors.toList());
        for (InstanceProvider instanceProvider : instanceProviders) {
            registerComponent(instanceProvider.getClazz());
        }
    }

    public Object registerComponent(Class<?> clazz) {
        InstanceProvider instanceProvider = instanceProviderMap.get(clazz);
        if (instanceProvider == null) {
            throw new ClassNotFoundRuntimeException("DIContextHelper.registerComponent - missing InstanceProvider");
        }
        Object object = instanceProvider.instantiate();
        context.registerComponent(clazz, object);
        return object;
    }

    public void setUpContext(Class<?> mainClass) {
        Class<?>[] classes = classPathProcessor.scanAllClasses(mainClass);
        for (Class<?> clazz : classes) {
            if (clazz.getDeclaredAnnotation(Component.class) != null) {
                this.add(clazz);
            }
        }
        registerComponents();
    }

    private int calculatePriorityLevel(List<Field> annotatedFields, Class<? extends Annotation> annotation) {
        int count = annotatedFields.size();
        for (Field field : annotatedFields) {
            InstanceProvider instanceProvider = instanceProviderMap.get(field.getType());
            if (instanceProvider != null) {
                count += instanceProvider.getPriorityLevel();
                continue;
            }
            List<Field> depAnnotatedField = classProcessor.findInheritedAnnotatedFields(field.getType(), annotation);
            count+= calculatePriorityLevel(depAnnotatedField, annotation);
        }
        return count;
    }
}
