package io.github.nguyenxuansang9494.runtime.context;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public final class SimpleDIContext implements DIContext {
    private final Map<Class<?>, List<Object>> componentPool;
    private final List<MethodExecutor> executors;
    private static final DIContext CONTEXT = new SimpleDIContext();
    private SimpleDIContext() {
        super();
        this.executors = new LinkedList<>();
        this.componentPool = new HashMap<>();
    }

    public static DIContext getContext() {
        return CONTEXT;
    }

    @Override
    public List<Object> getChildrenClassComponent(Class<?> clazz) {
        return componentPool.entrySet().stream()
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
        return componentPool.getOrDefault(clazz, Collections.emptyList());
    }

    @Override
    public void registerComponent(Class<?> clazz, Object component) {
        List<Object> components = componentPool.getOrDefault(clazz, new LinkedList<>());
        components.add(component);
        componentPool.put(clazz, components);
    }

    @Override
    public void registerExecutor(MethodExecutor methodExecutor) {
        executors.add(methodExecutor);
    }

    @Override
    public void executeRunners() {
        executors.sort(Comparator.comparingInt(MethodExecutor::getPriorityLevel));
        executors.forEach(MethodExecutor::execute);
    }

    @Override
    public <T> T getComponent(Class<T> clazz) {
        List<Object> components = getComponents(clazz);
        return components.isEmpty() ? clazz.cast(getComponents(clazz).get(0)) : null;
    }
}
