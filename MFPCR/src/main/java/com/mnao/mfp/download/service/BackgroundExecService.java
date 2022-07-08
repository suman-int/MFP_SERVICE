package com.mnao.mfp.download.service;

import java.util.Timer;
import java.util.TimerTask;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.lowagie.text.DocumentException;
import com.mnao.mfp.common.datafilters.FilterCriteria;
import com.mnao.mfp.user.dao.MFPUser;

@Service
@Transactional
public class BackgroundExecService {

	private static final Logger log = LoggerFactory.getLogger(BackgroundExecService.class);
	
	private static final long TIMEOUT_MS = 60 * 60 * 1000 ;

	public enum ExportType {
		UNKOWN, PDF, EXCEL
	};

	@Autowired
	ContactReportPDFService contactReportPDFService;

	class RunGenerateBulkExport implements Runnable {
		FilterCriteria filter;
		MFPUser mfpUser;
		ExportType exportType;

		public RunGenerateBulkExport(FilterCriteria filter, MFPUser mfpUser, ExportType exportType) {
			super();
			this.filter = filter;
			this.mfpUser = mfpUser;
			this.exportType = exportType;
		}

		@Override
		public void run() {
			if (this.exportType == ExportType.PDF) {
				try {
					contactReportPDFService.emailBulkPdfByFilterCriteria(filter, mfpUser);
				} catch (DocumentException e) {
					log.error("ERROR executing Bulk PDF in the Background", e);
				}
			} else {
				contactReportPDFService.emailBulkExcelReportByFilterCriteria(filter, mfpUser);
			}
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
	
	public void startBackgroundExport(FilterCriteria filter, MFPUser mfpUser, ExportType exportType) {
			Thread thread = new Thread(new RunGenerateBulkExport(filter, mfpUser, exportType));
			thread.start();

			Timer timer = new Timer();
			TimeOutTask timeOutTask = new TimeOutTask(thread, timer);
			timer.schedule(timeOutTask, TIMEOUT_MS);
	}


}
