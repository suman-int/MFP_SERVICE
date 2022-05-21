package com.mnao.mfp.sync;

import java.time.LocalDateTime;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.support.CronExpression;
import org.springframework.stereotype.Component;

@Component
public class CRSyncScheduler {
	//
	private static final Logger log = LoggerFactory.getLogger(CRSyncScheduler.class);

	@Value("${edw.sync.schedule.cron}")
	private String CronSetting;

	@PostConstruct
	public void init() {
		CronExpression cronTrigger = CronExpression.parse(CronSetting);
		if (cronTrigger != null) {
			LocalDateTime next = cronTrigger.next(LocalDateTime.now());
			log.debug("Next Sync Execution Time: " + next);
		}
	}

	@Scheduled(cron = "${edw.sync.schedule.cron}")
	public void ExecEDWSync() {
		CRSyncFromEDW crSync = new CRSyncFromEDW();
		crSync.StartEDWSync();
		// Just to log when the next sync would be run.
		init();
	}

}
