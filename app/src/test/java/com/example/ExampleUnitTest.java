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
    public void addition_isCorrect() throws Exception {
        assertEquals(4, 2 + 2);
        Logger log = LoggerFactory.getLogger(ExampleUnitTest.class);
        log.info("hello world");
    }
}