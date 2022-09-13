package io.github.nguyenxuansang9494.runtime;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

import io.github.nguyenxuansang9494.runtime.exception.InvalidClassPathException;

public class ClassPathProcessor {
    public List<File> findAllFiles(File classPath) {
        List<File> result = new LinkedList<>();
        if (!classPath.isDirectory())
            throw new InvalidClassPathException("classpath is not a directory");
        File[] files = classPath.listFiles();
        for (File e : files) {
            if (e.isFile() && e.getName().endsWith(".class"))
                result.add(e);
            else if (e.isDirectory())
                result.addAll(findAllFiles(e));
        }
        return result;
    }

    
}
