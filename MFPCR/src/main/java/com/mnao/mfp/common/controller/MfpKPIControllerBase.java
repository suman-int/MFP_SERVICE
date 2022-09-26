package com.mnao.mfp.common.controller;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.mnao.mfp.common.dao.DealerFilter;
import com.mnao.mfp.common.dao.DealerInfo;
import com.mnao.mfp.common.db.KPIQuerySpecs;
import com.mnao.mfp.common.service.AppPropertiesService;
import com.mnao.mfp.common.util.AppConstants;
import com.mnao.mfp.common.util.DateUtils;
import com.mnao.mfp.common.util.Utils;
import com.mnao.mfp.list.service.ListService;
import com.mnao.mfp.user.dao.MFPUser;
@Component
public abstract class MfpKPIControllerBase {
	//
	private static final Logger log = LoggerFactory.getLogger(MfpKPIControllerBase.class);
	//
	// Region, Zone, District, Dealer
	//
	// Market
	//
	//
	private static List<KPIQuerySpecs> kpiQuerySpecs = null;
	private static Map<String, KPIQuerySpecs> kpiMetricQueries = new HashMap<String, KPIQuerySpecs>();
	//private String sqlRootFolder = Utils.getAppProperty("location.sqlfiles");
	protected String kpiQueryFolder = "";
	//
	protected DateUtils dateUtils = new DateUtils();
	//
	@Autowired
	private AppPropertiesService propService;
	//
	private KPIQuerySpecs getKPIQuerySpecs(String kpiMetric) {
		if (kpiQuerySpecs == null) {
			loadKPIQueryConfig();
		}
		KPIQuerySpecs ks = kpiMetricQueries.get(kpiMetric);
		return ks;
	}

	private String getKpiQueryFolder() {
		if (kpiQueryFolder == null || kpiQueryFolder.trim().length() == 0)
			loadKPIQueryConfig();
		return kpiQueryFolder;
	}

	//
	protected String getKPIQueryFilePath(String fileName) {
		return getKpiQueryFolder() + "/" + fileName;
	}

	//
	protected String[] getKPIQueryMetricFile(String kpiMetric) {
		String[] rv = null;
		KPIQuerySpecs ks = getKPIQuerySpecs(kpiMetric);
		if (ks != null) {
			String sqls = ks.getSqlQueryFile();
			if (sqls != null) {
				rv = sqls.split("[,]");
				for (int i = 0; i < rv.length; i++)
					rv[i] = getKPIQueryFilePath(rv[i]);
			}
		}
		return rv;
	}

	protected String getBaseTableName(String kpiMetric) {
		String rv = null;
		KPIQuerySpecs ks = getKPIQuerySpecs(kpiMetric);
		if (ks != null) {
			rv = ks.getBaseTable();
		}
		return rv;
	}

	private void loadKPIQueryConfig() {
		String sqlRootFolder = Utils.getAppProperty(AppConstants.LOCATION_SQLFILES);
		if (!sqlRootFolder.endsWith("/"))
			sqlRootFolder = sqlRootFolder + "/";
		kpiQueryFolder = sqlRootFolder + AppConstants.KPI_QUERY_SCRIPTS_FOLDER;
		String fPath = kpiQueryFolder + "/" + AppConstants.KPI_QUERY_CONFIG;
		String jsonConfig = Utils.readTextFromFile(fPath);
		JsonMapper mapper = new JsonMapper();
		mapper.setSerializationInclusion(Include.NON_NULL);
		try {
			kpiQuerySpecs = mapper.readValue(jsonConfig,
					new TypeReference<ArrayList<KPIQuerySpecs>>() {
					});
			for (KPIQuerySpecs ks : kpiQuerySpecs) {
				kpiMetricQueries.put(ks.getKpiMetric(), ks);
			}
		} catch (JsonProcessingException e) {
			log.error("ERROR Loading KPIQueryConfig from " + fPath, e);
		}
	}

	protected String getJson(Object obj) {
		String json = "";
		JsonMapper mapper = new JsonMapper();
		mapper.enable(SerializationFeature.INDENT_OUTPUT);
		mapper.setSerializationInclusion(Include.NON_NULL);
		try {
			json = mapper.writeValueAsString(obj);
		} catch (JsonProcessingException e) {
			log.error("ERROR Serializing object to JSON ", e);
		}
		return json;
	}

	protected Pageable getPagingObject(int pgNo, int pgSize, String orderBy, String orderDirection) {
		Pageable paging = null;
		if (orderBy.isEmpty())
			paging = PageRequest.of(pgNo, pgSize);
		else
			paging = PageRequest.of(pgNo, pgSize,
					Sort.by(Direction.fromString(orderDirection.toUpperCase()), orderBy.toUpperCase()));
		return paging;
	}

	protected DealerInfo getDealerInfo(MFPUser mfpUser, String dlrCd) {
		DealerInfo dInfo = null;
		String sqlName = getKPIQueryFilePath(AppConstants.SQL_LIST_DEALERS);
		ListService<DealerInfo> service = new ListService<DealerInfo>();
		List<DealerInfo> retRows = null;
		DealerFilter df = new DealerFilter(mfpUser, dlrCd, null, null, null, null);
		try {
			retRows = service.getListData(sqlName, DealerInfo.class, df);
		} catch (InstantiationException | IllegalAccessException | ParseException e) {
			log.error("ERROR retrieving list of Dealers:", e);
		}
		if ((retRows != null) && (retRows.size() > 0)) {
			dInfo = retRows.get(0);
		}
		return dInfo;
	}


}
