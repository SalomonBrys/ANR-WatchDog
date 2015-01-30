package com.github.anrtestapp;

import android.app.Application;

import com.github.anrwatchdog.ANRWatchDog;

public class ANRWatchdogTestApplication extends Application {

    ANRWatchDog anrWatchDog = new ANRWatchDog(2000);

    @Override
    public void onCreate() {
        super.onCreate();

        anrWatchDog.start();
    }
}
