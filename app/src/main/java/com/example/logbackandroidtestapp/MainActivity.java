package com.example.logbackandroidtestapp;

import android.os.Bundle;
import android.os.StrictMode;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import ch.qos.logback.classic.LoggerContext;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        enableStrictMode();

        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        configureLogback();
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Writing logs", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                writeLogs();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // We're using AsyncAppender, so we should stop the logger context
        // to make sure the appender can finish before the app shutsdown.
        LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
        loggerContext.stop();
    }

    private void enableStrictMode() {
        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
                .detectAll()
                .penaltyLog()
                .build());
        StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
                .detectAll()
                .penaltyLog()
                .build());
    }

    private void writeLogs() {
        logHello();
        logMarker();
    }

    private String stringRepeat(int n, String s) {
        return String.format("%0" + n + "d", 0).replace("0",s);
    }

    private void logHello() {
        Logger log = LoggerFactory.getLogger(MainActivity.class);
        log.info("hello world!");
        for (int i = 0; i < 10; i++) {
            log.debug("i={} {}", i, stringRepeat(5*1024, "*"));
            System.out.println("sleeping for 1s");
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void logMarker() {
        Marker notifyAdmin = MarkerFactory.getMarker("NOTIFY_ADMIN");
        Logger log = LoggerFactory.getLogger(MainActivity.class);
        log.error(notifyAdmin,
                "This is a serious an error requiring the admin's attention",
                new Exception("Just testing"));
    }

    private void configureLogback() {
        final String logDirectory = "/logs";
        new AsyncLogbackConfigurationTask(this).execute(logDirectory);
    }
}
