package io.github.nguyenxuansang9494.runtime.processor;

import io.github.nguyenxuansang9494.annotations.PostConstruct;
import io.github.nguyenxuansang9494.runtime.context.MethodExecutor;
import io.github.nguyenxuansang9494.runtime.exception.FailedToRegisterDependencyException;

import java.lang.reflect.Method;
import java.util.List;

public class PostConstructProcessor {

    private PostConstructProcessor() {
        super();
    }

    public static void invoke(Object object) {
        final MethodExecutor methodExecutor;
        ClassProcessor classProcessor = ClassProcessor.getInstance();
        List<Method> postConstructMethod = classProcessor.findDeclaredAnnotatedMethods(object.getClass(), PostConstruct.class);
        if (postConstructMethod.size() > 1) {
            throw new FailedToRegisterDependencyException("PostConstructProcessor.PostConstructProcessor - invalid number of PostConstructor method");
        } else if (postConstructMethod.isEmpty()) {
            methodExecutor = null;
        } else {
            methodExecutor = new MethodExecutor(postConstructMethod.get(0), object);
        }
        if (methodExecutor != null)
            methodExecutor.execute();
    }
}
