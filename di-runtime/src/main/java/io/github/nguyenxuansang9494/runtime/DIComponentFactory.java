package io.github.nguyenxuansang9494.runtime;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import io.github.nguyenxuansang9494.annotations.Inject;

public class DIComponentFactory {
    private final Map<Integer, List<Class<?>>> classMap;

    public DIComponentFactory() {
        this.classMap = new HashMap<>();
    }

    public void add(Class<?> clazz) {
        long injectCount = 0;
        Class<?> superClass = clazz.getSuperclass();
        Field[] fields;
        while (superClass != null && superClass != Object.class) {
            fields = superClass.getDeclaredFields();
            injectCount += Stream.of(fields).filter(f -> f.getAnnotation(Inject.class) != null).count();
        }
        fields = clazz.getDeclaredFields();
        injectCount += Stream.of(fields).filter(f -> f.getAnnotation(Inject.class) != null).count();
        List<Class<?>> classList = classMap.getOrDefault((int)injectCount, new LinkedList<>());
        classList.add(clazz);
        classMap.putIfAbsent((int) injectCount, classList);
    }

    public void createComponents() {
        List<Entry<Integer,List<Class<?>>>> listClasses = classMap.entrySet().stream().sorted((e1, e2) -> e1.getKey() - e2.getKey()).collect(Collectors.toList());
    }
}
