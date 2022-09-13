package io.github.nguyenxuansang9494.runtime;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import io.github.nguyenxuansang9494.annotations.Component;
import io.github.nguyenxuansang9494.annotations.Inject;

public class DIContextHelper {
    private final ClassProcessor classHelper;
    private final DIContext context;
    private final Map<Integer, List<InstanceBuilder>> instanceBuilderMap;

    public DIContextHelper(ClassProcessor classHelper, DIContext context, Map<Integer, List<InstanceBuilder>> instanceBuilderMap) {
        this.classHelper = classHelper;
        this.context = context;
        this.instanceBuilderMap = instanceBuilderMap;
    }

    public void  add(Class<?> clazz) {
        Field[] injectAnnotatedFields = classHelper.findAnnotatedFields(clazz, Inject.class);
        Method[] componentAnnotatedDeclaredMethods = classHelper.findDeclaredAnnotatedMethods(clazz, Component.class);
        InstanceBuilder instanceBuilder = new InstanceBuilder(context, clazz, injectAnnotatedFields, componentAnnotatedDeclaredMethods);
        List<InstanceBuilder> instanceBuilderList = instanceBuilderMap.getOrDefault(injectAnnotatedFields.length, new LinkedList<>());
        instanceBuilderList.add(instanceBuilder);
        instanceBuilderMap.putIfAbsent(injectAnnotatedFields.length, instanceBuilderList);
    }

    public void setupContext() {
        List<Entry<Integer, List<InstanceBuilder>>> entries = instanceBuilderMap.entrySet().stream().sorted(Comparator.comparingInt(Entry::getKey)).collect(Collectors.toList());
        for (Entry<Integer, List<InstanceBuilder>> entry : entries) {
            for (InstanceBuilder builder : entry.getValue()) {
                context.registerComponent(builder.getClazz(), builder.buildInstance());
            }
        }
    }

}
