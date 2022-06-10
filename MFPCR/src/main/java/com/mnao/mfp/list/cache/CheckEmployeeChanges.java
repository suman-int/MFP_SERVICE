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

import com.mnao.mfp.list.dao.ListPersonnel;

@Service
public class CheckEmployeeChanges {
	//
	private static final Logger log = LoggerFactory.getLogger(CheckEmployeeChanges.class);
	//
	@Value("${emp.sync.changes.thread.pool.size}")
	private int threadPoolSize;
	private ExecutorService checkEmpChangesExecutor;
	//
	@Autowired AllEmployeesCache allEmployeesCache;
	//
	public class CheckEmpDomainChanges implements Runnable {
		private List<ListPersonnel> checkEmps;
		//
		public CheckEmpDomainChanges(List<ListPersonnel> checkEmps) {
			super();
			this.checkEmps = checkEmps;
		}
		
		@Override
		public void run() {
			Instant start = Instant.now();
			allEmployeesCache.checkDomaniChanged(checkEmps);
			Instant end = Instant.now();
			Duration timeElapsed = Duration.between(start, end);
			log.info("Time taken to check Domain Change of " + checkEmps.size() + " Employees : "+ timeElapsed.toMillis() +" milliseconds.");
	}
	}
	//
	@PostConstruct
	private void initialize() throws Exception {
		if (checkEmpChangesExecutor == null) {
			this.checkEmpChangesExecutor = Executors.newFixedThreadPool(threadPoolSize);
		}
	}

	@PreDestroy
	private void cleanUp() throws Exception {
		if (checkEmpChangesExecutor != null) {
			checkEmpChangesExecutor.shutdown();
			try {
				if (!checkEmpChangesExecutor.awaitTermination(800, TimeUnit.MILLISECONDS)) {
					checkEmpChangesExecutor.shutdownNow();
				}
			} catch (InterruptedException e) {
				checkEmpChangesExecutor.shutdownNow();
			}
		}
	}
	//
	public void checkEmpChanges(List<ListPersonnel> checkEmps) {
		checkEmpChangesExecutor.execute(new CheckEmpDomainChanges(checkEmps));
	}
}
