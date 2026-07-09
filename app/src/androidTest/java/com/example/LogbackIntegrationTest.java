package com.example;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import android.content.Context;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import ch.qos.logback.classic.LoggerContext;

/**
 * Integration test for logback-android running on a real device/emulator.
 *
 * <p>These tests exercise the library end-to-end: they rely on the app's
 * {@code assets/logback.xml} being auto-loaded by logback-android on the first
 * {@link LoggerFactory} call, then verify that (a) the configured appenders are
 * present and (b) log messages actually reach the on-device file appenders.
 */
@RunWith(AndroidJUnit4.class)
public class LogbackIntegrationTest {

    /**
     * The library loaded the app's assets/logback.xml, so the root logger
     * should carry the "logcat" and file appenders declared there.
     */
    @Test
    public void appConfigurationIsLoaded() {
        // Touch a logger to trigger lazy auto-configuration from assets/logback.xml.
        LoggerFactory.getLogger(LogbackIntegrationTest.class).trace("init");

        LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();
        ch.qos.logback.classic.Logger root =
                lc.getLogger(Logger.ROOT_LOGGER_NAME);

        assertNotNull("logcat appender should be configured",
                root.getAppender("logcat"));
        assertNotNull("DebugLog file appender should be configured",
                root.getAppender("DebugLog"));
        assertNotNull("WarnLog file appender should be configured",
                root.getAppender("WarnLog"));
    }

    /**
     * A DEBUG message should be written to the file appender's log file on disk.
     */
    @Test
    public void debugMessageIsWrittenToFile() throws Exception {
        Context ctx = ApplicationProvider.getApplicationContext();
        assertNotNull(ctx);

        String unique = "logback-integration-" + System.nanoTime();

        Logger log = LoggerFactory.getLogger(LogbackIntegrationTest.class);
        log.debug(unique);

        // Stop the context to flush and close the file appenders before reading.
        LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();
        lc.stop();

        File debugLog = new File(resolveLogDir(lc), "debug.log");
        assertTrue("expected log file to exist: " + debugLog, debugLog.exists());

        String contents = readFile(debugLog);
        assertTrue("expected log file to contain the message \"" + unique + "\"",
                contents.contains(unique));
    }

    /**
     * Resolves the log directory the same way {@code assets/logback.xml} does:
     * {@code ${EXT_DIR:-${DATA_DIR}}/logs}. Both properties are populated by
     * logback-android's {@code AndroidContextUtil}.
     */
    private static File resolveLogDir(LoggerContext lc) {
        String extDir = lc.getProperty("EXT_DIR");
        String dataDir = lc.getProperty("DATA_DIR");
        String base = (extDir != null && !extDir.isEmpty()) ? extDir : dataDir;
        assertNotNull("logback-android should define DATA_DIR/EXT_DIR", base);
        return new File(base, "logs");
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
