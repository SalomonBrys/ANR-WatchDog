package com.github.anrwatchdog;

import android.os.Looper;

/**
 * Error thrown by {@link com.github.anrwatchdog.ANRWatchDog} when an ANR is detected.
 * Contains the stack trace of the frozen UI thread.
 */
public class ANRError extends Error {
    private static final long serialVersionUID = 1L;
    public ANRError() {
        super("Application Not Responding");
    }
    @Override
    public Throwable fillInStackTrace() {
        setStackTrace(Looper.getMainLooper().getThread().getStackTrace());
        return this;
    }
}