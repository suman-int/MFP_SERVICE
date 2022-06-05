package com.mnao.mfp.list.cache;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.mnao.mfp.common.controller.MfpKPIControllerBase;
import com.mnao.mfp.common.dao.DealerFilter;
import com.mnao.mfp.common.dao.DealerInfo;
import com.mnao.mfp.common.util.AppConstants;
import com.mnao.mfp.list.service.MMAListService;

@Service
public class AllDealersCache extends MfpKPIControllerBase {
	//
	private static final Logger log = LoggerFactory.getLogger(AllDealersCache.class);
	//
	private static final List<DealerInfo> allDealersList = new ArrayList<>();
	private static final Map<String, DealerInfo> allDealersByDlrCd = new HashMap<>();
	//
	public synchronized Map<String, DealerInfo> getAllDealers() {
		if (allDealersList.size() == 0 ) {
			loadAllDealers();
		}
		return allDealersByDlrCd;
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
				allDealersList.add(di);
				allDealersByDlrCd.put(di.getDlrCd(), di);
			}
		}
		log.debug("" + allDealersList.size() + " Dealers loaded to cache.");
	}
}