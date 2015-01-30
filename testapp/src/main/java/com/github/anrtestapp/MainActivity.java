package com.github.anrtestapp;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;

import com.github.anrwatchdog.ANRWatchDog;


public class MainActivity extends Activity {

    private final Object _mutex = new Object();

    private static void SleepAMinute() {
        try {
            Thread.sleep(60 * 1000);
        }
        catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public class LockerThread extends Thread {

        public LockerThread() {
            setName("APP: Locker");
        }

        @Override
        public void run() {
            synchronized (_mutex) {
                //noinspection InfiniteLoopStatement
                while (true)
                    SleepAMinute();
            }
        }
    }

    private void _deadLock() {
        new LockerThread().start();

        new Handler().postDelayed(new Runnable() {
            @Override public void run() {
                synchronized (_mutex) {
                    Log.e("ANR-Failed", "There should be a dead lock before this message");
                }
            }
        }, 1000);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final ANRWatchDog anrWatchDog = ((ANRWatchdogTestApplication) getApplication()).anrWatchDog;


        findViewById(R.id.simpleAll).setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                SleepAMinute();
            }
        });

        findViewById(R.id.simpleMain).setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                anrWatchDog.setReportMainThreadOnly();
                SleepAMinute();
            }
        });

        findViewById(R.id.deadlock).setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                _deadLock();
            }
        });

        findViewById(R.id.filtered).setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                anrWatchDog.setReportThreadNamePrefix("APP:");
                _deadLock();
            }
        });

    }
}
