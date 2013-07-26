package com.github.anrwatchdog;

import android.os.Looper;
import android.util.Log;

class ANRError extends Error {
	private static final long serialVersionUID = 1L;
	public ANRError() {
		super("Application Not Responding");
	}
	@Override
	public Throwable fillInStackTrace() {
		
		Log.e("ANRError", "Filling stack trace");

		setStackTrace(Looper.getMainLooper().getThread().getStackTrace());
		
		return this;
	}
}