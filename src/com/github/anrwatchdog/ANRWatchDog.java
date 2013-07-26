package com.github.anrwatchdog;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

public class ANRWatchDog extends Thread {

	private ANRWDSSeter seter = new ANRWDSSeter();
	
	private Handler handler = new Handler(Looper.getMainLooper());
	
	private int tick = 0;
	
	private final int interval;
	
	public ANRWatchDog(int interval) {
		super();
		this.interval = interval;
	}
	
	public ANRWatchDog() {
		this(5000);
	}

	private class ANRWDSSeter implements Runnable {
		@Override
		public void run() {
			tick = (tick + 1) % 10;
//			Log.i("ANRWatchDog", "Setting tick: " + (tick));
		}
	}
	
	@Override
	public void run() {
		try {
			int lastTick = 0;
			for (;;) {
				lastTick = tick;
				handler.post(seter);
				Thread.sleep(interval);
				
				if (tick == lastTick) {
					Log.e("ANRWatchDog", "ANR DETECTED");
					throw new ANRError();
				}
			}
		}
		catch (InterruptedException e) {
			Log.i("ANRWatchDog", "Interrupted");
		}
	}
	
}
