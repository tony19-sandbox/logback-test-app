package com.example;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import android.content.Context;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.android.LogcatAppender;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.FileAppender;

/**
 * Integration test for logback-android running on a real device/emulator.
 *
 * <p>The library is configured programmatically (the same way the app's
 * {@link com.example.logbackandroidtestapp.AsyncLogbackConfigurationTask} does
 * it) rather than via {@code assets/logback.xml}. Auto-configuration relies on
 * {@code ClassLoader.getResource("assets/logback.xml")}, which is not reliably
 * exposed for the app-under-test inside the instrumentation process, so we wire
 * the appenders up directly. This still exercises logback-android end-to-end:
 * real {@link FileAppender} file I/O and the Android {@link LogcatAppender} on a
 * real device.
 */
@RunWith(AndroidJUnit4.class)
public class LogbackIntegrationTest {

    private static final String FILE_APPENDER = "FILE";
    private static final String LOGCAT_APPENDER = "logcat";

    private File logFile;

    @Before
    public void configureLogback() {
        Context ctx = ApplicationProvider.getApplicationContext();
        logFile = new File(ctx.getFilesDir(), "integration-logs/test.log");
        // Start each test from a clean file so assertions can't see stale logs.
        //noinspection ResultOfMethodCallIgnored
        logFile.delete();

        LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();
        lc.reset();

        PatternLayoutEncoder fileEncoder = new PatternLayoutEncoder();
        fileEncoder.setContext(lc);
        fileEncoder.setPattern("%-5level %logger{0} - %msg%n");
        fileEncoder.start();

        FileAppender<ILoggingEvent> fileAppender = new FileAppender<>();
        fileAppender.setContext(lc);
        fileAppender.setName(FILE_APPENDER);
        fileAppender.setFile(logFile.getAbsolutePath());
        fileAppender.setAppend(false);
        fileAppender.setEncoder(fileEncoder);
        fileAppender.start();

        PatternLayoutEncoder logcatEncoder = new PatternLayoutEncoder();
        logcatEncoder.setContext(lc);
        logcatEncoder.setPattern("%msg");
        logcatEncoder.start();

        LogcatAppender logcatAppender = new LogcatAppender();
        logcatAppender.setContext(lc);
        logcatAppender.setName(LOGCAT_APPENDER);
        logcatAppender.setEncoder(logcatEncoder);
        logcatAppender.start();

        ch.qos.logback.classic.Logger root =
                lc.getLogger(Logger.ROOT_LOGGER_NAME);
        root.setLevel(Level.DEBUG);
        root.addAppender(fileAppender);
        root.addAppender(logcatAppender);
    }

    /** The programmatically configured appenders should be attached and started. */
    @Test
    public void appendersAreConfiguredAndStarted() {
        LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();
        ch.qos.logback.classic.Logger root =
                lc.getLogger(Logger.ROOT_LOGGER_NAME);

        assertNotNull("file appender should be attached",
                root.getAppender(FILE_APPENDER));
        assertNotNull("logcat appender should be attached",
                root.getAppender(LOGCAT_APPENDER));
        assertTrue("file appender should be started",
                root.getAppender(FILE_APPENDER).isStarted());
        assertTrue("logcat appender should be started",
                root.getAppender(LOGCAT_APPENDER).isStarted());
    }

    /** A DEBUG message routed through SLF4J should land in the on-device log file. */
    @Test
    public void debugMessageIsWrittenToFile() throws Exception {
        String unique = "logback-integration-" + System.nanoTime();

        Logger log = LoggerFactory.getLogger(LogbackIntegrationTest.class);
        log.debug(unique);

        // The FileAppender uses immediateFlush (the default), so the event is on
        // disk once log.debug() returns.
        assertTrue("expected log file to exist: " + logFile, logFile.exists());

        String contents = readFile(logFile);
        assertTrue("expected " + logFile + " to contain \"" + unique + "\", was:\n"
                + contents, contents.contains(unique));
    }

    private static String readFile(File file) throws Exception {
        try (InputStream in = new FileInputStream(file)) {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            byte[] buf = new byte[4096];
            int n;
            while ((n = in.read(buf)) != -1) {
                out.write(buf, 0, n);
            }
            return new String(out.toByteArray(), StandardCharsets.UTF_8);
        }
    }
}
