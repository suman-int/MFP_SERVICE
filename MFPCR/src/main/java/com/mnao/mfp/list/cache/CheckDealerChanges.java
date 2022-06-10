package com.mnao.mfp.list.cache;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.mnao.mfp.common.dao.DealerInfo;

@Service
public class CheckDealerChanges {
	//
	private static final Logger log = LoggerFactory.getLogger(CheckEmployeeChanges.class);
	//
	@Value("${dlr.sync.changes.thread.pool.size}")
	private int threadPoolSize;
	private ExecutorService checkDlrChangesExecutor;
	//
	@Autowired
	AllDealersCache allDealersCache;
	//
	public class CheckDlrDomainChanges implements Runnable {
		private List<DealerInfo> checkDealers;

		//
		public CheckDlrDomainChanges(List<DealerInfo> checkDealers) {
			super();
			this.checkDealers = checkDealers;
		}

		//
		@Override
		public void run() {
			Instant start = Instant.now();
			allDealersCache.checkDealerChanges(checkDealers);
			Instant end = Instant.now();
			Duration timeElapsed = Duration.between(start, end);
			log.info("Time taken to check for Domain Change of " + checkDealers.size() + " Dealers : "+ timeElapsed.toMillis() +" milliseconds.");
		}
	}
	//
	@PostConstruct
	private void initialize() throws Exception {
		if (checkDlrChangesExecutor == null) {
			this.checkDlrChangesExecutor = Executors.newFixedThreadPool(threadPoolSize);
		}
	}
	//
	@PreDestroy
	private void cleanUp() throws Exception {
		if (checkDlrChangesExecutor != null) {
			checkDlrChangesExecutor.shutdown();
			try {
				if (!checkDlrChangesExecutor.awaitTermination(800, TimeUnit.MILLISECONDS)) {
					checkDlrChangesExecutor.shutdownNow();
				}
			} catch (InterruptedException e) {
				checkDlrChangesExecutor.shutdownNow();
			}
		}
	}
	//
	public void checkDealerChanges(List<DealerInfo> dealers) {
		checkDlrChangesExecutor.execute(new CheckDlrDomainChanges(dealers));
	}
}
