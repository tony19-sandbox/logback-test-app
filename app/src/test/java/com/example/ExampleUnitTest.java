package com.example;

import org.junit.Test;

import static org.junit.Assert.*;

import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.robolectric.RobolectricTestRunner;

/**
 * To work on unit tests, switch the Test Artifact in the Build Variants view.
 */
@Config(manifest = Config.NONE)
@RunWith(RobolectricTestRunner.class)
public class ExampleUnitTest {
    @Test
    public void log() throws Exception {
        assertEquals(4, 2 + 2);

        Logger log = LoggerFactory.getLogger(ExampleUnitTest.class);
        log.info("info message");
        log.info("info message", new RuntimeException("dummy error"));
        log.debug("debug message");
        log.debug("debug message", new RuntimeException("dummy error"));
        log.trace("trace message");
        log.trace("trace message", new RuntimeException("dummy error"));
        log.warn("warn message");
        log.warn("warn message", new RuntimeException("dummy error"));
        log.error("error message");
        log.error("error message", new RuntimeException("dummy error"));

//        LoggerContext loggerContext = (LoggerContext)LoggerFactory.getILoggerFactory();
//        boolean hasError = loggerContext.getStatusManager().getCopyOfStatusList().stream().anyMatch(s -> s instanceof ErrorStatus);
//        assertFalse("no errors occurred in config", hasError);
    }
}