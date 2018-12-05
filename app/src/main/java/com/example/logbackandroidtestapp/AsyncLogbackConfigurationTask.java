package com.example.logbackandroidtestapp;

import android.app.Activity;
import android.os.AsyncTask;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.lang.ref.WeakReference;
import java.nio.charset.Charset;

import ch.qos.logback.classic.AsyncAppender;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.rolling.RollingFileAppender;
import ch.qos.logback.core.rolling.SizeAndTimeBasedFNATP;
import ch.qos.logback.core.rolling.TimeBasedRollingPolicy;
import ch.qos.logback.core.status.OnConsoleStatusListener;
import ch.qos.logback.core.util.FileSize;
import ch.qos.logback.core.util.StatusListenerConfigHelper;

class AsyncLogbackConfigurationTask extends AsyncTask<String, Integer, String> {
    private final WeakReference<Activity> context;

    AsyncLogbackConfigurationTask(Activity context) {
        this.context = new WeakReference<>(context);
    }

    @Override
    protected String doInBackground(String[] paths) {
        Activity ctx = context.get();
        if (ctx == null
          || ctx.isFinishing()
          /* || ctx.isDestroyed() */ /* APK 17 */) {
            // context is no longer valid, don't do anything!
            return null;
        }
        configureLogback(ctx.getFilesDir().getAbsolutePath() + paths[0]);
        return null;
    }

    private void configureLogback(String logDirectory) {
        final long fileSizeKB = 5 * 1024;
        final String logFileName = "mylog";
        final int historyLength = 7;
        final Level level = Level.DEBUG;

        LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
        loggerContext.stop();

        StatusListenerConfigHelper.addOnConsoleListenerInstance(loggerContext, new OnConsoleStatusListener());

        RollingFileAppender<ILoggingEvent> rollingFileAppender = new RollingFileAppender<>();
        rollingFileAppender.setContext(loggerContext);
        rollingFileAppender.setName("FILE");
        rollingFileAppender.setAppend(true);
        rollingFileAppender.setFile(logDirectory + File.separator + logFileName + ".log");

        SizeAndTimeBasedFNATP<ILoggingEvent> fileNamingPolicy = new SizeAndTimeBasedFNATP<>();
        fileNamingPolicy.setContext(loggerContext);
        fileNamingPolicy.setMaxFileSize(new FileSize(fileSizeKB));

        TimeBasedRollingPolicy<ILoggingEvent> rollingPolicy = new TimeBasedRollingPolicy<>();
        rollingPolicy.setContext(loggerContext);
        rollingPolicy.setFileNamePattern(logDirectory + File.separator + logFileName + ".%d{yyyy-MM-dd_HHmmss}.%i.log");
        rollingPolicy.setMaxHistory(historyLength);
        rollingPolicy.setTimeBasedFileNamingAndTriggeringPolicy(fileNamingPolicy);
        rollingPolicy.setParent(rollingFileAppender);  // parent and context required!
        rollingPolicy.setCleanHistoryOnStart(true);
        rollingPolicy.start();

        PatternLayoutEncoder encoder = new PatternLayoutEncoder();
        encoder.setContext(loggerContext);
        encoder.setCharset(Charset.forName("UTF-8"));
        encoder.setPattern("%date %level [%thread] %msg%n");
        encoder.start();

        rollingFileAppender.setRollingPolicy(rollingPolicy);
        rollingFileAppender.setEncoder(encoder);
        rollingFileAppender.start();

        AsyncAppender asyncAppender = new AsyncAppender();
        asyncAppender.setName("ASYNC");
        asyncAppender.setContext(loggerContext);
        asyncAppender.addAppender(rollingFileAppender);
        asyncAppender.start();

        // add the newly created appenders to the root logger;
        // qualify Logger to disambiguate from org.slf4j.Logger
        ch.qos.logback.classic.Logger root = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
        root.setLevel(level);
        root.addAppender(asyncAppender);
    }
}
