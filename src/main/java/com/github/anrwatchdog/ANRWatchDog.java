package com.github.anrwatchdog;

/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2013 Salomon BRYS
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
 * the Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
 * FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
 * IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

/**
 * A watchdog timer thread that detects when the UI thread has frozen.
 */
public class ANRWatchDog extends Thread {

    public interface ANRListener {
        public void onAppNotResponding(ANRError error);
    }

    public interface InterruptionListener {
        public void onInterrupted(InterruptedException exception);
    }

    private static final int DEFAULT_ANR_TIMEOUT = 5000;

    private static final ANRListener DEFAULT_ANR_LISTENER = new ANRListener() {
        @Override public void onAppNotResponding(ANRError error) {
            throw error;
        }
    };

    private static final InterruptionListener DEFAULT_INTERRUPTION_LISTENER = new InterruptionListener() {
        @Override public void onInterrupted(InterruptedException exception) {
            Log.d("ANRWatchdog", "Interrupted: " + exception.getMessage());
        }
    };

    private ANRListener mANRListener = DEFAULT_ANR_LISTENER;
    private InterruptionListener mInterruptionListener = DEFAULT_INTERRUPTION_LISTENER;

    private final Handler mHandler = new Handler(Looper.getMainLooper());
    private final int mTimeoutInterval;

    private volatile int mTick = 0;

    private final Runnable mTicker = new Runnable() {
        @Override public void run() {
            mTick = (mTick + 1) % 10;
        }
    };

    /**
     * Constructs a watchdog that checks the ui thread every {@value #DEFAULT_ANR_TIMEOUT} milliseconds
     */
    public ANRWatchDog() {
        this(DEFAULT_ANR_TIMEOUT);
    }

    /**
     * Constructs a watchdog that checks the ui thread every given interval
     *
     * @param timeoutInterval The interval, in milliseconds, between to checks of the UI thread.
     *                        It is therefore the maximum time the UI may freeze before being reported as ANR.
     */
    public ANRWatchDog(int timeoutInterval) {
        super();
        this.mTimeoutInterval = timeoutInterval;
    }

    /**
     * Sets an interface for when an ANR is detected.
     * If not set, the default behavior is to throw an error and crash the application.
     *
     * @param listener The new listener or null
     * @return itself for chaining.
     */
    public ANRWatchDog setANRListener(ANRListener listener) {
        if (listener == null) {
            mANRListener = DEFAULT_ANR_LISTENER;
        }
        else {
            mANRListener = listener;
        }
        return this;
    }

    /**
     * Sets an interface for when the watchdog thread is interrupted.
     * If not set, the default behavior is to just log the interruption message.
     *
     * @param listener The new listener or null
     * @return itself for chaining.
     */
    public ANRWatchDog setInterruptionListener(InterruptionListener listener) {
        if (listener == null) {
            mInterruptionListener = DEFAULT_INTERRUPTION_LISTENER;
        }
        else {
            mInterruptionListener = listener;
        }
        return this;
    }

    @Override
    public void run() {
        setName("AnrWatchDog");

        int lastTick;
        while (!isInterrupted()) {
            lastTick = mTick;
            mHandler.post(mTicker);
            try {
                Thread.sleep(mTimeoutInterval);
            }
            catch (InterruptedException e) {
                mInterruptionListener.onInterrupted(e);
                return ;
            }

            // If the main thread has not handled mTicker, it is blocked. ANR.
            if (mTick == lastTick) {
                ANRError error = new ANRError();
                mANRListener.onAppNotResponding(error);
                return ;
            }
        }
    }

}