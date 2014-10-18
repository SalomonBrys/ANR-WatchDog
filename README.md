ANR-WatchDog
============

A simple watchdog that detects Android ANRs (Application Not Responding).


Why it exists
-------------

There is currently no way for an android application to catch and report ANR errors.
If your application is not in the play store (either because you are still developing it or because you are distributing it differently), the only way to investigate an ANR is to pull the file /data/anr/traces.txt.
Additionally, we found that using the Play Store was not as effective as being able to choose our own bug tracking service.

There is an [issue entry](https://code.google.com/p/android/issues/detail?id=35380) in the android bug tracker describing this lack, feel free to star it ;)


What it does
------------

It sets up a watchdog timer that will detect when the UI thread stops responding. When it does, it raises an error with the main thread's stack trace.


Can it work with crash reporters like [ACRA](https://github.com/ACRA/acra) ?
----------------------------------------------------------------------------

Yes! I'm glad you asked: That's the reason why it was developed in the first place!
As this throws an error, a crash handler can intercept it and handle it the way it needs.


Wait! What if I don't want it to crash?
---------------------------------------

Great question! Neither did we! See *advanced use* for how to enable a callback instead.

How it works
------------

The watchdog is a simple thread does the following in a loop:

1.  Schedules a runnable to be run on the UI thread as soon as possible.
2.  Wait for 5 seconds. (5 seconds is the default, but it can be configured).
3.  See if the runnable has been run. If it has, go back to 1.
4.  If the runnable has not been run, it means that the UI thread has been blocked for at least 5 seconds, raises an error with the UI thread stack trace


How to use with Gradle / Android Studio
---------------------------------------

1.  In the `app/build.gradle` file, add

        compile 'com.github.anrwatchdog:anrwatchdog:1.0'

2.  In your application class, in `onCreate`, add:

```java
    if (!BuildConfig.DEBUG) {
        new ANRWatchDog().start();
    }
```

 Note that this will not enable the watchdog in debug mode, because the watchdog will prevent the debugger
 from hanging execution at breakpoints or exceptions (it will detect the debugging pause as an ANR).


How to use with Eclipse
-----------------------

1.  [Download the jar](https://github.com/SalomonBrys/ANR-WatchDog/raw/master/target/anrwatchdog-1.0.jar)

2.  Put *AnrWatchDog.jar* in the `libs/` directory of your project


Advanced use
------------

*  ANRWatchDog is a thread, so you can interrupt it at any time.

*  If you are programming with Android's multi process capability (like starting an activity in a new thread), remember that you will need an ANRWatchDog thread per process.

* If you would prefer not to crash the application in the case that an ANR is detected, you can enable a callback instead:

```java
    if (BuildConfig.DEBUG == false) {
        new ANRWatchDog().setListener(new ANRWatchDog.ANRListener() {
            @Override
            public void onAppNotResponding(ANRError error) {
                // Do something with the error. Here, we log it to HockeyApp:
                ExceptionHandler.saveException(error, new CrashManager());
            }
        }).start();
    }
```

* To set a different timeout (5000 millis is the default):

```java
    if (BuildConfig.DEBUG == false) {
        new ANRWatchDog(10000 /*timeout*/).start();
    }
```

Integration with Proguard (optional)
------------------------------------

Suppose you use Proguard to obfuscate your code. When you receive an ANR report, the stack trace will say something like this:

* `com.a.a.a: Application Not Responding`

You may find this harder to read. So you may want to add a Proguard exception for ANRWatchDog, by adding this to your `proguard-project.txt`:

        -keep class com.github.anrwatchdog.** {*;}

So now the stack trace will say this:
* `com.github.anrwatchdog.ANRError: Application Not Responding`
