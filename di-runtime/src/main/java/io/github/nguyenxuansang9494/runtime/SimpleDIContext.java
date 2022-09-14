package io.github.nguyenxuansang9494.runtime;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public final class SimpleDIContext implements DIContext {
    Map<Class<?>, List<Object>> pool;
    private static final DIContext CONTEXT = new SimpleDIContext();
    private SimpleDIContext() {
        super();
        this.pool = new HashMap<>();
    }

    public static DIContext getContext() {
        return CONTEXT;
    }

    @Override
    public List<Object> getChildrenClassComponent(Class<?> clazz) {
        return pool.entrySet().stream()
                .filter(e -> clazz.isAssignableFrom(e.getKey()))
                .map(Entry::getValue)
                .reduce((a,b) -> {
                    a.addAll(b);
                    return a;
                })
                .orElse(Collections.emptyList());

    }

    @Override
    public List<Object> getComponents(Class<?> clazz) {
        return pool.getOrDefault(clazz, Collections.emptyList());
    }

    @Override
    public void registerComponent(Class<?> clazz, Object component) {
        List<Object> components = pool.getOrDefault(clazz, new LinkedList<>());
        components.add(component);
        pool.put(clazz, components);
    }

    @Override
    public <T> T getComponent(Class<T> clazz) {
        return clazz.cast(getComponents(clazz).get(0));
    }
}
