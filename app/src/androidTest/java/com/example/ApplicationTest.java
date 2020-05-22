package com.example;

import android.content.Context;
import android.os.Build;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;

@RunWith(AndroidJUnit4.class)
public class ApplicationTest {
    @Test
    public void checkSdkVersion() {
        assertEquals(23, Build.VERSION.SDK_INT);
    }

    @Test
    public void useAppContext() throws Exception {
        // Context of the app under test.
        Context appContext = InstrumentationRegistry.getTargetContext();

        assertEquals("tony19.github.com.logbackexample", appContext.getPackageName());
    }
}