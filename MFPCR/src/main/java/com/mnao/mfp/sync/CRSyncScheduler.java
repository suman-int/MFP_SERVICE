package com.mnao.mfp.sync;

import java.time.LocalDateTime;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.support.CronExpression;
import org.springframework.stereotype.Component;

@Component
public class CRSyncScheduler {
	//
	private static final Logger log = LoggerFactory.getLogger(CRSyncScheduler.class);
	//
	//
	@Value("${dlr.sync.schedule.cron}")
	private String CronSetting;
	@Value("${dlr.sync.on.startup}")
	private boolean syncOnStartup;
	//
	@Autowired
	private CRSyncDealers crSync;

	//
	@PostConstruct
	public void init() {
		showNextSyncTime();
		// For TEST only
		if (syncOnStartup)
			execDLRSync();
	}

	private void showNextSyncTime() {
		CronExpression cronTrigger = CronExpression.parse(CronSetting);
		if (cronTrigger != null) {
			LocalDateTime next = cronTrigger.next(LocalDateTime.now());
			log.debug("Next Sync Execution Time: " + next);
		}
	}

	@Scheduled(cron = "${dlr.sync.schedule.cron}")
	public void execDLRSync() {
		crSync.startDealersSync();
		// Just to log when the next sync would be run.
		showNextSyncTime();
	}

}
