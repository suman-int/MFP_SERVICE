package com.mnao.mfp.sync;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class CRSyncScheduler {
	// run during the first second every minute every hour every day of month
	// //every month every day of week
	@Scheduled(cron = "${edw.sync.schedule.cron}")
	public void ExecEDWSync() {
		CRSyncFromEDW crSync = new CRSyncFromEDW();
		crSync.StartEDWSync();
	}

}
