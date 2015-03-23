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

It sets up a watchdog timer that will detect when the UI thread stops responding. When it does, it raises an error with all threads stack stack traces (main first).


Can it work with crash reporters?
---------------------------------

Yes! I'm glad you asked: That's the reason why it was developed in the first place!  
As this throws an error, a crash handler can intercept it and handle it the way it needs.

Known working crash reporters include:

 * [ACRA](https://github.com/ACRA/acra)
 * [Crashlytics](https://get.fabric.io/crashlytics)
 * [HokeyApp](http://hockeyapp.net/)

And there is no reason why it should not work with *[insert your favourite crash reporting system here]*.


How it works
------------

The watchdog is a simple thread does the following in a loop:

1.  Schedules a runnable to be run on the UI thread as soon as possible.
2.  Wait for 5 seconds. (5 seconds is the default, but it can be configured).
3.  See if the runnable has been run. If it has, go back to 1.
4.  If the runnable has not been run, it means that the UI thread has been blocked for at least 5 seconds, raises an error with the all threads stack traces


How to use with Gradle / Android Studio
---------------------------------------

1.  In the `app/build.gradle` file, add

		compile 'com.github.anrwatchdog:anrwatchdog:1.1.+'

2.  In your application class, in `onCreate`, add:

	```java
	if (!BuildConfig.DEBUG) {
		new ANRWatchDog().start();
	}
	```

	Note that this will not enable the watchdog in debug mode, because the watchdog will prevent the debugger from hanging execution at breakpoints or exceptions (it will detect the debugging pause as an ANR).


How to use with Eclipse
-----------------------

1.  [Download the latest jar](https://search.maven.org/remote_content?g=com.github.anrwatchdog&a=anrwatchdog&v=LATEST)

2.  Put the jar in the `libs/` directory of your project


Reading the ANRError exception report
-------------------------------------

The `ANRError` exception report is a bit particular, it has the stack traces of all the threads running in your application. So, in the report, each `caused by` section is not the cause of the precedent exception, but the stack trace of a different thread.

Here is a dead lock example:

	FATAL EXCEPTION: |ANR-WatchDog|
	    Process: anrwatchdog.github.com.testapp, PID: 26737
	    com.github.anrwatchdog.ANRError: Application Not Responding
	    Caused by: com.github.anrwatchdog.ANRError$_$_Thread: main
	        at testapp.MainActivity$1.run(MainActivity.java:46)
	        at android.os.Handler.handleCallback(Handler.java:739)
	        at android.os.Handler.dispatchMessage(Handler.java:95)
	        at android.os.Looper.loop(Looper.java:135)
	        at android.app.ActivityThread.main(ActivityThread.java:5221)
	    Caused by: com.github.anrwatchdog.ANRError$_$_Thread: APP: Locker
	        at java.lang.Thread.sleep(Native Method)
	        at java.lang.Thread.sleep(Thread.java:1031)
	        at java.lang.Thread.sleep(Thread.java:985)
	        at testapp.MainActivity.SleepAMinute(MainActivity.java:18)
	        at testapp.MainActivity.access$100(MainActivity.java:12)
	        at testapp.MainActivity$LockerThread.run(MainActivity.java:36)

From this report, we can see that the stack traces of two threads. The first (the "main" thread) is stuck at `MainActivity.java:46` while the second thread (named "App: Locker") is locked in a Sleep at `MainActivity.java:18`.  
From there, if we looked at those two lines, we would surely understand the cause of the dead lock!

Note that some crash reporting library (such as Crashlytics) report all thread stack traces at the time of an uncaught exception. In that case, having all threads in the same exception can be cumbersome. In such cases, simply use `setReportMainThreadOnly()`.


Advanced use
------------


#### ANRWatchdog work

*  ANRWatchDog is a thread, so you can interrupt it at any time.

* To set a different timeout (5000 millis is the default):

	```java
	if (BuildConfig.DEBUG == false) {
		new ANRWatchDog(10000 /*timeout*/).start();
	}
	```

*  If you are programming with Android's multi process capability (like starting an activity in a new process), remember that you will need an ANRWatchDog thread per process.


#### On ANR callback

* If you would prefer not to crash the application in the case that an ANR is detected, you can enable a callback instead:

	```java
	new ANRWatchDog().setANRListener(new ANRWatchDog.ANRListener() {
		@Override
		public void onAppNotResponding(ANRError error) {
			// Handle the error. For example, log it to HockeyApp:
			ExceptionHandler.saveException(error, new CrashManager());
		}
	}).start();
	```


#### Filtering reports

* If you would like to have only your own threads to be reported in the ANRError, and not all threads (including system threads such as the `FinalizerDaemon` thread), you can set a prefix: only the threads whose name starts with this prefix will be reported.

	```java
	new ANRWatchDog().setReportThreadNamePrefix("APP:").start();
	```

	Then, when you start a thread, don't forget to set its name to something that starts with this prefix (if you want it to be reported):

	```java
	public class MyAmazingThread extends Thread {
		@Override
		public void run() {
			setName("APP: Amazing!");
			/* ... do amazing things ... */
		}
	}
	```

* If you want to have only the main thread stack trace and not all the other threads (like in version 1.0), you can:

	```java
	new ANRWatchDog().setReportMainThreadOnly().start();
	```
