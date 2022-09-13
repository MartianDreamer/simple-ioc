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
    private final ClassProcessor classHelper;
    private final DIContext context;
    private final Map<Class<?>, InstanceProvider> instanceProviderMap;

    public void add(Class<?> clazz) {
        Field[] injectAnnotatedFields = classHelper.findAnnotatedFields(clazz, Inject.class);
        Method[] componentAnnotatedDeclaredMethods = classHelper.findDeclaredAnnotatedMethods(clazz, Component.class);
        InstanceProvider instanceBuilder = new InstanceProvider(context, this, clazz, injectAnnotatedFields,
                componentAnnotatedDeclaredMethods, null, null);
        instanceProviderMap.putIfAbsent(instanceBuilder.getClass(), instanceBuilder);
    }

    public void setupContext() {
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
}
