package com.github.anrwatchdog;

import android.os.Looper;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

/**
 * Error thrown by {@link com.github.anrwatchdog.ANRWatchDog} when an ANR is detected.
 * Contains the stack trace of the frozen UI thread.
 * <p>
 * It is important to notice that, in an ANRError, all the "Caused by" are not really the cause
 * of the exception. Each "Caused by" is the stack trace of a running thread. Note that the main
 * thread always comes first.
 */
@SuppressWarnings({"Convert2Diamond", "UnusedDeclaration"})
public class ANRError extends Error {

    private static class $ {
        private final String _name;
        private final StackTraceElement[] _stackTrace;

        private class _Thread extends Throwable {
            private _Thread(_Thread other) {
                super(_name, other);
            }

            @Override
            public Throwable fillInStackTrace() {
                setStackTrace(_stackTrace);
                return this;
            }
        }

        private $(String name, StackTraceElement[] stackTrace) {
            _name = name;
            _stackTrace = stackTrace;
        }
    }

    private static final long serialVersionUID = 1L;

    private final Map<Thread, StackTraceElement[]> _stackTraces;

    private ANRError($._Thread st, Map<Thread, StackTraceElement[]> stackTraces) {
        super("Application Not Responding", st);
        _stackTraces = stackTraces;
    }

    /**
     * @return all the reported threads and stack traces.
     */
    public Map<Thread, StackTraceElement[]> getStackTraces() {
        return _stackTraces;
    }

    @Override
    public Throwable fillInStackTrace() {
        setStackTrace(new StackTraceElement[] {});
        return this;
    }

    static ANRError New(String prefix, boolean logThreadsWithoutStackTrace) {
        final Thread mainThread = Looper.getMainLooper().getThread();

        final Map<Thread, StackTraceElement[]> stackTraces = new TreeMap<Thread, StackTraceElement[]>(new Comparator<Thread>() {
            @Override public int compare(Thread lhs, Thread rhs) {
                if (lhs == rhs)
                    return 0;
                if (lhs == mainThread)
                    return 1;
                if (rhs == mainThread)
                    return -1;
                return rhs.getName().compareTo(lhs.getName());
            }
        });

        for (Map.Entry<Thread, StackTraceElement[]> entry : Thread.getAllStackTraces().entrySet())
            if (
                    entry.getKey() == mainThread
                ||  (
                        entry.getKey().getName().startsWith(prefix)
                    &&  (
                            logThreadsWithoutStackTrace
                        ||
                            entry.getValue().length > 0
                        )
                    )
                )
                stackTraces.put(entry.getKey(), entry.getValue());

        $._Thread tst = null;
        for (Map.Entry<Thread, StackTraceElement[]> entry : stackTraces.entrySet())
            tst = new $(entry.getKey().getName(), entry.getValue()).new _Thread(tst);

        return new ANRError(tst, stackTraces);
    }

    static ANRError NewMainOnly() {
        final Thread mainThread = Looper.getMainLooper().getThread();
        final StackTraceElement[] mainStackTrace = mainThread.getStackTrace();

        final HashMap<Thread, StackTraceElement[]> stackTraces = new HashMap<Thread, StackTraceElement[]>(1);
        stackTraces.put(mainThread, mainStackTrace);

        return new ANRError(new $(mainThread.getName(), mainStackTrace).new _Thread(null), stackTraces);
    }
}