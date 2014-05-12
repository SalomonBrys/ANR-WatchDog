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

/**
 * A watchdog timer thread that detects when the UI thread has frozen.
 *
 * Adapted from https://github.com/SalomonBrys/ANR-WatchDog.
 */
public class ANRWatchDog extends Thread {

    public interface ANRListener {
        public void onAppNotResponding(ANRError error);
    }

    private static final int DEFAULT_ANR_TIMEOUT = 5000;

    private ANRListener mListener;

    private final Handler mHandler = new Handler(Looper.getMainLooper());
    private final int mTimeoutInterval;

    private int mTick = 0;

    private final Runnable mTicker = new Runnable() {
        @Override
        public void run() {
            mTick = (mTick + 1) % 10;
        }
    };

    @SuppressWarnings("unused")
    public ANRWatchDog() {
        this(DEFAULT_ANR_TIMEOUT);
    }

    @SuppressWarnings("unused")
    public ANRWatchDog(int timeoutInterval) {
        super();
        this.mTimeoutInterval = timeoutInterval;
    }

    /**
     * Sets an interface for when an ANR is detected.
     * If not set, the default behavior is to throw an error and crash the application.
     * @return itself for chaining.
     */
    public ANRWatchDog setListener(ANRListener listener) {
        mListener = listener;
        return this;
    }

    @SuppressWarnings("InfiniteLoopStatement")
    @Override
    public void run() {
        setName("AnrWatchDog Thread");

        int lastTick;
        while (true) {
            lastTick = mTick;
            mHandler.post(mTicker);
            try {
                Thread.sleep(mTimeoutInterval);
            }  catch (InterruptedException e) {
                e.printStackTrace();
                continue;
            }

            // If the main thread has not handled mTicker, it is blocked. ANR.
            if (mTick == lastTick) {
                ANRError error = new ANRError();
                if (mListener == null) {
                    throw error;
                } else {
                    mListener.onAppNotResponding(error);
                    return; // continuing will result in duplicate reports.
                }
            }
        }
    }

}