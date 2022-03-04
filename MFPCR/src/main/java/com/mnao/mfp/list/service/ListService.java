package com.mnao.mfp.list.service;

import java.sql.Connection;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mnao.mfp.common.dao.DealerFilter;
import com.mnao.mfp.common.dao.MetricData;
import com.mnao.mfp.common.service.DBDataService;
import com.mnao.mfp.common.util.MFPDatabase;
import com.mnao.mfp.common.util.MFPDatabase.DB;
import com.mnao.mfp.common.util.Utils;

public class ListService<T extends MetricData> extends DBDataService<T> {
	private static final Logger log = LoggerFactory.getLogger(ListService.class);

	public List<T> getListData(String sqlFile, Class<T> tclass, DealerFilter df, String... prms)
			throws ParseException, InstantiationException, IllegalAccessException {
		List<T> retRows = new ArrayList<T>();
		String sqlText = Utils.readTextFromFile(sqlFile);
		String sql = injectWhere(sqlText, df);
		MFPDatabase db = new MFPDatabase();
		try (Connection conn = db.getConnection(DB.cr)) {
			sql = Utils.replaceSchemaName(conn, sql);
			retRows = super.getResultList(conn, sql, tclass, prms);
		} catch (SQLException e) {
			log.error("Error connecting do DB: ", e);
		}
		return retRows;
	}

}
