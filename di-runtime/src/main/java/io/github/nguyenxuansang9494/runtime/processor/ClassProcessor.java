package io.github.nguyenxuansang9494.runtime.processor;

import io.github.nguyenxuansang9494.annotations.Inject;
import io.github.nguyenxuansang9494.runtime.context.DIContextHelper;
import io.github.nguyenxuansang9494.runtime.exception.FailedToRegisterDependencyException;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;


public class ClassProcessor {
    private static final ClassProcessor instance = new ClassProcessor();

    private ClassProcessor() {
        super();
    }

    public static ClassProcessor getInstance() {
        return instance;
    }

    private List<Field> findDeclaredAnnotatedFields(Class<?> clazz, Class<? extends Annotation> annotation) {
        return Stream.of(clazz.getDeclaredFields()).filter(f -> f.getAnnotation(annotation) != null).collect(Collectors.toList());
    }

    public List<Field> findInheritedAnnotatedFields(Class<?> clazz, Class<? extends Annotation> annotation) {
        Class<?> superClass = clazz.getSuperclass();
        List<Field> annotatedFields = findDeclaredAnnotatedFields(clazz, annotation);
        while (superClass != null && superClass != Object.class) {
            annotatedFields.addAll(findDeclaredAnnotatedFields(superClass, annotation));
            superClass = superClass.getSuperclass();
        }
        return annotatedFields;
    }

    public List<Method> findDeclaredAnnotatedMethods(Class<?> clazz, Class<? extends Annotation> annotation) {
        return Stream.of(clazz.getDeclaredMethods()).filter(m -> m.getDeclaredAnnotation(annotation) != null).collect(Collectors.toList());
    }

    public Object wiringUnwiredFields(Object object, Map<Class<?>, Object> wiredFieldObjects) {
        try {
            List<Field> fields = findInheritedAnnotatedFields(object.getClass(), Inject.class);
            for (Field field : fields) {
                field.setAccessible(true);
                if (field.get(object) == null) {
                    Inject inject = field.getDeclaredAnnotation(Inject.class);
                    Class<?> qualifiedClass = inject.qualified();
                    Object wiredFieldObject;
                    if (qualifiedClass != Object.class) {
                        wiredFieldObject = wiredFieldObjects.get(qualifiedClass);
                    } else {
                        List<Class<?>> qualifiedClasses = DIContextHelper.getInstance().getChildClasses(field.getType());
                        if (qualifiedClasses.size() != 1) {
                            throw new FailedToRegisterDependencyException("ClassProcessor.wiringUnwiredFields - failed to find valid component");
                        }
                        wiredFieldObject = wiredFieldObjects.get(qualifiedClasses.get(0));
                    }
                    if (wiredFieldObject == null) {
                        throw new FailedToRegisterDependencyException("ClassProcessor.wiringUnwiredFields - component not found.");
                    }
                    field.set(object, wiredFieldObject);
                }
            }
            return object;
        } catch (IllegalAccessException e) {
            throw new FailedToRegisterDependencyException(e);
        }
    }
}
