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

public class MMAListService<T extends MetricData> extends DBDataService<T> {
	private static final Logger log = LoggerFactory.getLogger(ListService.class);

	public List<T> getListData(String sqlFile, Class<T> tclass, DealerFilter df, String... prms)
			throws ParseException, InstantiationException, IllegalAccessException {
		List<T> retRows = new ArrayList<T>();
		String sqlText = Utils.readTextFromFile(sqlFile);
		String sql = injectWhere(sqlText, df);
		MFPDatabase db = new MFPDatabase(DB.mma);
		try (Connection conn = db.getConnection()) {
			sql = Utils.replaceSchemaName(conn, sql);
			retRows = super.getResultList(conn, sql, tclass, prms);
		} catch (SQLException e) {
			log.error("Error connecting do DB: ", e);
		}
		return retRows;
	}

	public List<T> getEmpDataAllEmployees(String sqlFile, Class<T> tclass, String idColumn, List<String> idCdList) {
		List<T> retRows = new ArrayList<T>();
		String sqlText = Utils.readTextFromFile(sqlFile);
		String sql = buildInClause(sqlText, idColumn, idCdList);
		MFPDatabase db = new MFPDatabase(DB.mma);
		try (Connection conn = db.getConnection()) {
			sql = Utils.replaceSchemaName(conn, sql);
			retRows = super.getResultList(conn, sql, tclass, idCdList.toArray(new String[0]));
		} catch (SQLException e) {
			log.error("Error connecting do DB: ", e);
		}
		return retRows;
	}

	private String buildInClause(String sqlText, String idCol, List<String> idCdList) {
		StringBuilder sb = new StringBuilder(sqlText.trim());
		if( idCdList != null && idCdList.size() > 0 ) {
			sb.append(" WHERE " + idCol + " IN (");
			for( int i = 0 ; i < idCdList.size(); i++ ) {
				if( i > 0 ) {
					sb.append(", ");
				}
				sb.append("?");
			}
			sb.append(")");
		}
		return sb.toString();
	}

}
