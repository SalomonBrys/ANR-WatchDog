ANR-WatchDog
============

A simple watchdog that detects Android ANR (Application Not Responding) error and throws a meaningful exception


Why it exists
-------------

There is currently no way for an android application to catch and report ANR errors.
If your application is not in the play store (either because you are still developing it or because you are distributing it differently),
the only way to investigate an ANR is to pull the file /data/anr/traces.txt and try to navigate your way in this enormous file.

There is an [issue entry](https://code.google.com/p/android/issues/detail?id=35380) in the android bug tracker describing this lack, feel free to star it ;)


What it does
------------

It sets up a watchdog that will detect when the UI thread stops responding. When it does, it raises an exception with the UI thread stack trace.


Can it work with crash reporters like [ACRA](https://github.com/ACRA/acra) ?
----------------------------------------------------------------------------

Yes ! I'm glad you ask : That's the reason why it was developped in the first place !
As this throws an exception, a crash handler can intercept it and handle it the way it needs.


How it works
------------

The watchdog is a simple thread that :  

1.  Schedule small code to be run on the UI thread as soon as possible.
2.  Wait for 5 seconds. (5 seconds is the default, but it can be configured).
3.  See if the code has run : if it has, go back to 1
4.  If the code has not run, it means that the UI thread has been blocked for at least 5 seconds, raises an exception with the UI thread stack trace


How to use it
-------------

1.  [Download the jar](https://github.com/SalomonBrys/ANR-WatchDog/blob/master/ANRWatchDog.jar?raw=true)

2.  Put *ANRWatchDog.jar* in the libs/ directory of your project

3.  In your application class, add an ANRWatchDog field:

        public ANRWatchDog watchDog = new ANRWatchDog();

    However, if you want to debug your app, then you probably want to use something like this:

        if (BuildConfig.DEBUG == false) {
            ANRWatchDog watchDog = new ANRWatchDog();
            watchDog.start();
        }

    Note that you can configure the watchdog interval (5000 miliseconds by default).
    For example, if you want to have a 10 seconds interval:

        public ANRWatchDog watchDog = new ANRWatchDog(10000);

4.  In your application class, in *onCreate*, add:

		watchDog.start();

5.  ***You're done***


Advanced use
------------

*  ANRWatchDog is a thread, so you can interrupt it at any time.

*  If you are programming with Android's multi process capability (like starting an activity in a new thread), remember that you will need an ANRWatchDog thread per process

