package io.github.nguyenxuansang9494.runtime;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

public class SimpleDIContext implements DIContext {
    Map<Class<?>, List<Object>> pool;
    private static final DIContext CONTEXT = new SimpleDIContext();

    public static DIContext getContext() {
        return CONTEXT;
    }

    public List<Entry<Class<?>, List<Object>>> getChildrenClassComponent(Class<?> clazz) {
        return pool.entrySet().stream().filter(e -> clazz.isAssignableFrom(e.getKey())).collect(Collectors.toList());
    }

    public List<Object> getComponents(Class<?> clazz) {
        return pool.getOrDefault(clazz, Collections.emptyList());
    }

    public void registerComponent(Class<?> clazz, Object component) {
        List<Object> components = pool.getOrDefault(clazz, new LinkedList<>());
        components.add(component);
        pool.putIfAbsent(clazz, components);
    }
}
