package io.github.nguyenxuansang9494.runtime;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ClassPathProcessorTest {
    @Test
    void testScanAllClasses() {
        ClassPathProcessor classPathProcessor = new ClassPathProcessor();
        Assertions.assertEquals(classPathProcessor.scanAllClasses(this.getClass())[0], this.getClass());
    }
}
