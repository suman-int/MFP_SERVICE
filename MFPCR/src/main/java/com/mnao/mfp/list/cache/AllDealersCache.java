package com.mnao.mfp.list.cache;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.mnao.mfp.common.controller.MfpKPIControllerBase;
import com.mnao.mfp.common.dao.DealerFilter;
import com.mnao.mfp.common.dao.DealerInfo;
import com.mnao.mfp.common.util.AppConstants;
import com.mnao.mfp.list.service.MMAListService;
import com.mnao.mfp.sync.RealTimeSyncDlr;

@Service
public class AllDealersCache extends MfpKPIControllerBase {
	//
	private enum DealerInfoChange {
		SAME,
		MODIFIED,
		NEW
	}
	//
	private static final Logger log = LoggerFactory.getLogger(AllDealersCache.class);
	//
	private static final List<DealerInfo> allDealersList = new ArrayList<>();
	private static final Map<String, DealerInfo> allDealersByDlrCd = new HashMap<>();
	//
	private ExecutorService dealerRTSync;
	//
	@PostConstruct
	private void initialize() throws Exception {
		if (dealerRTSync == null) {
			this.dealerRTSync = Executors.newSingleThreadExecutor();
		}
	}
	//
	@PreDestroy
	private void cleanUp() throws Exception {
		if (dealerRTSync != null) {
			dealerRTSync.shutdown();
			try {
				if (!dealerRTSync.awaitTermination(800, TimeUnit.MILLISECONDS)) {
					dealerRTSync.shutdownNow();
				}
			} catch (InterruptedException e) {
				dealerRTSync.shutdownNow();
			}
		}
	}
	//
	//
	public synchronized Map<String, DealerInfo> getAllDealers() {
		if (allDealersList.size() == 0 ) {
			loadAllDealers();
		}
		return allDealersByDlrCd;
	}
	//
	public DealerInfo getDealerInfo(String dlrCd) {
		DealerInfo dInfo = getAllDealers().get(dlrCd);
		return dInfo;
	}
	//
	public boolean checkDealerChanges(List<DealerInfo> dInfos) {
		boolean rv = false;
		List<DealerInfo> newDealers = new ArrayList<>();
		List<DealerInfo> modifiedDealers = new ArrayList<>();
		dInfos.forEach(d -> {
			DealerInfoChange dc = isDomainChanged(d);
			if( dc == DealerInfoChange.MODIFIED )
				modifiedDealers.add(d);
			else if(dc == DealerInfoChange.NEW)
				newDealers.add(d);
		});
		if( newDealers.size() > 0 || modifiedDealers.size() > 0 ) {
			rv = true;
			RealTimeSyncDlr rtSync = new RealTimeSyncDlr(newDealers, modifiedDealers);
			dealerRTSync.execute(rtSync);
		}
		return rv;
	}
	//
	private DealerInfoChange isDomainChanged(DealerInfo dInfo) {
		DealerInfoChange rv = DealerInfoChange.SAME;
		DealerInfo cacheDInfo = allDealersByDlrCd.get(dInfo.getDlrCd());
		if( cacheDInfo == null ) {
			rv = DealerInfoChange.NEW;
			addDealerInfo(dInfo);
		} else {
			if(dInfo.getRgnCd().equals(cacheDInfo.getRgnCd()) &&
					dInfo.getZoneCd().equals(cacheDInfo.getZoneCd()) &&
					dInfo.getDistrictCd().equals(cacheDInfo.getDistrictCd())) {
				rv = DealerInfoChange.SAME;
			} else {
				rv = DealerInfoChange.MODIFIED;
				updateDealerInfo(cacheDInfo, dInfo);
			}
		}
		return rv;
	}
	//
	private void loadAllDealers() {
		String sqlName = getKPIQueryFilePath(AppConstants.SQL_LIST_ALL_DEALERS_UDB);
		MMAListService<DealerInfo> service = new MMAListService<DealerInfo>();
		List<DealerInfo> retRows = null;
		DealerFilter df = new DealerFilter();
		allDealersList.clear();
		allDealersByDlrCd.clear();
		try {
			retRows = service.getListData(sqlName, DealerInfo.class, df);
		} catch (InstantiationException | IllegalAccessException | ParseException e) {
			log.error("ERROR retrieving list of Dealers:", e);
		}
		if (retRows != null) {
			for (DealerInfo di : retRows) {
				addDealerInfo(di);
			}
		}
		log.debug("" + allDealersList.size() + " Dealers loaded to cache.");
	}
	//
	private void addDealerInfo(DealerInfo di) {
		allDealersList.add(di);
		allDealersByDlrCd.put(di.getDlrCd(), di);
	}
	//
	private void updateDealerInfo(DealerInfo cacheDInfo, DealerInfo modDInfo) {
		cacheDInfo.copy(modDInfo);
	}
}