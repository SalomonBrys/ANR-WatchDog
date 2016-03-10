package com.github.anrtestapp;

import android.app.Application;
import android.util.Log;

import com.github.anrwatchdog.ANRError;
import com.github.anrwatchdog.ANRWatchDog;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

public class ANRWatchdogTestApplication extends Application {

    ANRWatchDog anrWatchDog = new ANRWatchDog(2000);

    @Override
    public void onCreate() {
        super.onCreate();

        anrWatchDog.setANRListener(new ANRWatchDog.ANRListener() {
            @Override
            public void onAppNotResponding(ANRError error) {
                Log.e("ANR-Watchdog", "Detected Application Not Responding!");

                // Some tools like ACRA are serializing the exception, so we must make sure the exception serializes correctly
                try {
                    new ObjectOutputStream(new ByteArrayOutputStream()).writeObject(error);
                }
                catch (IOException ex) {
                    throw new RuntimeException(ex);
                }

                Log.i("ANR-Watchdog", "Error was successfully serialized");

                throw error;
            }
        });

        anrWatchDog.start();
    }
}
