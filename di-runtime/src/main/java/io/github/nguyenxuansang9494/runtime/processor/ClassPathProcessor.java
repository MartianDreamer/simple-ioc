package io.github.nguyenxuansang9494.runtime.processor;

import io.github.nguyenxuansang9494.runtime.exception.InvalidClassPathException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;


public class ClassPathProcessor {

    private static final Logger LOGGER = LogManager.getLogger(ClassPathProcessor.class);
    
    private List<File> findAllFiles(File classPath) {
        List<File> result = new LinkedList<>();
        File[] files = classPath.listFiles();
        if (files == null) {
            throw new InvalidClassPathException("ClassPathProcessor.findAllFiles - classpath is not a directory");
        }
        for (File e : files) {
            if (e.isFile() && e.getName().endsWith(".class"))
                result.add(e);
            else if (e.isDirectory())
                result.addAll(findAllFiles(e));
        }
        return result;
    }

    private String getClassName(String classPath, String filePath) {
        String className = filePath.substring(classPath.length()+1, filePath.indexOf(".class"));
        className = className.replace(File.separatorChar, '.');
        return className;
    }

    public Class<?>[] scanAllClasses(Class<?> clazz) {
        final String WHITESPACE = "%20";
        String classPathStr = clazz.getProtectionDomain().getCodeSource().getLocation().getPath();
        classPathStr = classPathStr.replace(WHITESPACE, " ");
        File classPath = new File(classPathStr);
        List<File> files = findAllFiles(classPath);
        List<Class<?>> classes = new ArrayList<>();
        for (File file : files) {
            String className = getClassName(classPath.getPath(), file.getPath());
            LOGGER.info("File path: {}", file.getName());
            try {
                classes.add(Class.forName(className));
            } catch (ClassNotFoundException e) {
                LOGGER.error("ClassPathProcessor.scanAllClasses - {}", e.getMessage());
            }
        }
        return classes.toArray(new Class[]{});
    }
}
