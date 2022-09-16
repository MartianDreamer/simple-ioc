package io.github.nguyenxuansang9494.runtime;

import io.github.nguyenxuansang9494.runtime.processor.ClassPathProcessor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ClassPathProcessorTest {
    @Test
    void testScanAllClasses() {
        ClassPathProcessor classPathProcessor = ClassPathProcessor.getInstance();
        Assertions.assertEquals(classPathProcessor.scanAllClasses(this.getClass()).get(0), this.getClass());
    }
}
