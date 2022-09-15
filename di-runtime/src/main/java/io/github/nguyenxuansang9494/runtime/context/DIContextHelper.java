package io.github.nguyenxuansang9494.runtime.context;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import io.github.nguyenxuansang9494.annotations.Component;
import io.github.nguyenxuansang9494.annotations.ComponentScope;
import io.github.nguyenxuansang9494.annotations.Inject;
import io.github.nguyenxuansang9494.annotations.Runner;
import io.github.nguyenxuansang9494.runtime.exception.ClassNotFoundRuntimeException;
import io.github.nguyenxuansang9494.runtime.processor.ClassPathProcessor;
import io.github.nguyenxuansang9494.runtime.processor.ClassProcessor;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class DIContextHelper {
    private final ClassProcessor classProcessor;
    private final ClassPathProcessor classPathProcessor;
    private final DIContext context;
    private final Map<Class<?>, InstanceProvider> instanceProviderMap;

    public void addInstanceProvider(Class<?> clazz, Method instantiateMethod, Object configObject) {
        instanceProviderMap.put(clazz, new InstanceProvider(context, this, 0, clazz, new Field[]{}, new Method[]{}, new Method[]{}, instantiateMethod,configObject));
    }

    public InstanceProvider getInstanceProvider(Class<?> clazz) {
        return instanceProviderMap.get(clazz);
    }
    public List<Class<?>> getChildClasses(Class<?> clazz) {
        return instanceProviderMap.keySet().stream().filter(clazz::isAssignableFrom).collect(Collectors.toList());
    }

    public void addInstanceProvider(Class<?> clazz) {
        List<Field> injectAnnotatedFields = classProcessor.findInheritedAnnotatedFields(clazz, Inject.class);
        List<Method> runnerAnnotatedMethods = classProcessor.findDeclaredAnnotatedMethods(clazz, Runner.class);
        List<Method> componentAnnotatedMethods = classProcessor.findDeclaredAnnotatedMethods(clazz, Component.class);
        int priorityLevel = calculatePriorityLevel(injectAnnotatedFields, Inject.class);
        InstanceProvider instanceProvider = new InstanceProvider(context, this, priorityLevel, clazz, injectAnnotatedFields.toArray(new Field[]{}),
                componentAnnotatedMethods.toArray(new Method[]{}), runnerAnnotatedMethods.toArray(new Method[]{}), null, null);
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
        Component component = clazz.getDeclaredAnnotation(Component.class);
        if (instanceProvider == null) {
            throw new ClassNotFoundRuntimeException("DIContextHelper.registerComponent - missing InstanceProvider");
        }
        if (ComponentScope.PROTOTYPE.equals(component.scope())) {
            return null;
        }
        Object object = instanceProvider.instantiate();
        context.registerComponent(clazz, object);
        return object;
    }

    public void setUpContext(Class<?> mainClass) {
        Class<?>[] classes = classPathProcessor.scanAllClasses(mainClass);
        for (Class<?> clazz : classes) {
            if (clazz.getDeclaredAnnotation(Component.class) != null) {
                this.addInstanceProvider(clazz);
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

    public void registerExecutor(Method method, Object object) {
        context.registerExecutor(new MethodExecutor(method, object));
    }
}
