package io.github.nguyenxuansang9494.runtime;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import io.github.nguyenxuansang9494.annotations.Component;
import io.github.nguyenxuansang9494.annotations.Inject;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class DIContextHelper {
    private final ClassProcessor classProcessor;
    private final ClassPathProcessor classPathProcessor;
    private final DIContext context;
    private final Map<Class<?>, InstanceProvider> instanceProviderMap;

    public void add(Class<?> clazz, Method instantiateMethod, Object configObject) {
        instanceProviderMap.put(clazz, new InstanceProvider(context, this, clazz, new Field[]{}, new Method[]{}, instantiateMethod, configObject));
    }
    public void add(Class<?> clazz) {
        Field[] injectAnnotatedFields = classProcessor.findAnnotatedFields(clazz, Inject.class);
        Method[] componentAnnotatedDeclaredMethods = classProcessor.findDeclaredAnnotatedMethods(clazz, Component.class);
        InstanceProvider instanceBuilder = new InstanceProvider(context, this, clazz, injectAnnotatedFields,
                componentAnnotatedDeclaredMethods, null, null);
        instanceProviderMap.put(instanceBuilder.getClass(), instanceBuilder);
    }

    private void registerComponents() {
        List<InstanceProvider> instanceBuilders = instanceProviderMap.values().stream()
                .sorted((e1, e2) -> e1.getInjectAnnotatedField().length - e2.getInjectAnnotatedField().length)
                .collect(Collectors.toList());
        for (InstanceProvider instanceBuilder : instanceBuilders) {
            context.registerComponent(instanceBuilder.getClass(), instanceBuilders);
        }
    }

    public Object registerComponent(Class<?> clazz) {
        Object object = instanceProviderMap.get(clazz).instantiate();
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
}
