package com.mnao.mfp.sync;

import java.util.Timer;
import java.util.TimerTask;

import com.mnao.mfp.common.util.AppConstants;
import com.mnao.mfp.common.util.Utils;

public class CRSyncFromEDW {

	class RunSyncTask implements Runnable {
		@Override
		public void run() {
			SyncDLR syncEDW = new SyncDLR();
			syncEDW.startSync();
		}
	}

	class TimeOutTask extends TimerTask {
		private Thread thread;
		private Timer timer;

		public TimeOutTask(Thread thread, Timer timer) {
			this.thread = thread;
			this.timer = timer;
		}

		@Override
		public void run() {
			if (thread != null && thread.isAlive()) {
				thread.interrupt();
			}
			timer.cancel();
		}
	}

	public void StartEDWSync() {
		long timeoutms = Integer.parseInt(Utils.getAppProperty(AppConstants.DLR_SYNC_TIMEOUT, "0")) * 1000;
		if (timeoutms > 0) {
			Thread thread = new Thread(new RunSyncTask());
			thread.start();

			Timer timer = new Timer();
			TimeOutTask timeOutTask = new TimeOutTask(thread, timer);
			timer.schedule(timeOutTask, timeoutms);
		}
	}

//	public static void main(String[] args) {
//		CRSyncFromEDW crSync = new CRSyncFromEDW();
//		crSync.StartEDWSync();
//	}

}
