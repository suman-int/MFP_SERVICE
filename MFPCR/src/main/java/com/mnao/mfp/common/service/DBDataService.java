package com.mnao.mfp.common.service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mnao.mfp.common.dao.DealerFilter;
import com.mnao.mfp.common.dao.MetricData;
import com.mnao.mfp.common.util.Utils;

public class DBDataService<T extends MetricData> {
	private static final Logger log = LoggerFactory.getLogger(DBDataService.class);

	//
	//
	protected List<T> getResultList(Connection conn, String sql, Class<T> tClass, String... prms) {
		List<T> retRows = new ArrayList<T>();
		sql = Utils.replaceSchemaName(conn, sql);
		try (PreparedStatement ps = conn.prepareStatement(sql)) {
			int numPrms = ps.getParameterMetaData().getParameterCount();
			for (int i = 0; i < prms.length && i < numPrms; i++) {
				ps.setString(i + 1, prms[i]);
			}
			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					T rcmbd = tClass.newInstance();
					rcmbd.setResultSetRow(rs);
					retRows.add(rcmbd);
				}
			} catch (InstantiationException | IllegalAccessException e) {
				log.error("ERROR Retrieving data from SQL : " + sql, e);
			}
		} catch (SQLException e) {
			log.error("ERROR executing SQL : " + sql, e);
		}
		return retRows;
	}

	protected String injectWhere(String sql, DealerFilter df) {
		String sqlText = sql;
		int st = sqlText.indexOf('{');
		int en = sqlText.indexOf('}');
		String ret = "";
		if (st > 0 && en > 0) {
			while (st > 0 && en > 0) {
				String firstPart = sqlText.substring(0, st);
				String midPart = sqlText.substring(st + 1, en);
				String lastPart = sqlText.substring(en + 1);
				String[] wrds = midPart.split("\\s+");
				ret = firstPart;
				String ta = wrds[1].split("[=:]")[1];
				String wCond = df.getWhereCondition(ta);
				if (wCond.trim().length() > 0) {
					ret = ret + " " + wrds[0] + " " + wCond;
				}
				st = lastPart.indexOf('{');
				en = lastPart.indexOf('}');
				if (st > 0 && en > 0) {
					ret = ret + " " + lastPart.substring(0, st);
					sqlText = lastPart.substring(st + 1);
				} else {
					ret = ret + " " + lastPart;
				}
			}
		} else {
			ret = sql;
		}
		return ret;
	}

}
