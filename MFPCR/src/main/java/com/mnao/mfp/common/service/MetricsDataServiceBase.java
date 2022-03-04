package com.mnao.mfp.common.service;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Order;

import com.mnao.mfp.common.dao.DealerFilter;
import com.mnao.mfp.common.dao.MetricData;
import com.mnao.mfp.common.util.DateUtils;
import com.mnao.mfp.common.util.MFPDatabase;
import com.mnao.mfp.common.util.MFPDatabase.DB;
import com.mnao.mfp.common.util.Utils;

public abstract class MetricsDataServiceBase<T extends MetricData> extends DBDataService<T>{
	private static final Logger log = LoggerFactory.getLogger(MetricsDataServiceBase.class);
	private DateUtils dateUtils = new DateUtils();
	protected String today = dateUtils.today();

	//
	public abstract List<T> getMetricsDataByDate(Pageable page, String sqlFile, Class<T> tClass, DealerFilter df,
			String... prms) throws ParseException, InstantiationException, IllegalAccessException;

	public abstract List<T> getMetricsDataByDealer(Pageable page, String sqlFile, Class<T> tClass, DealerFilter df,
			String... prms) throws ParseException, InstantiationException, IllegalAccessException;

	public abstract int getMetricsTotalRowsByDealer(String sqlFile, DealerFilter df, String... prms)
			throws ParseException;


	public Date getLastEDWUpdateDate(String baseTable) {
		Date lastUpdt = null;
		if (baseTable != null && baseTable.trim().length() > 0) {
			String sql = "SELECT LAST_UPDATE_DATE FROM S$SCHEMA$LAST_EDW_UPDATE_DATES WHERE TABLE_NAME = '" + baseTable
					+ "'";
			MFPDatabase db = new MFPDatabase();
			try (ResultSet rs = db.executeQueryCRS(db.getConnection(DB.local), sql)) {
				if (rs.next()) {
					lastUpdt = rs.getDate(1);
				}
			} catch (SQLException e) {
				log.error("ERROR executing query: " + sql, e);
			}
		}
		return lastUpdt;
	}

	public String AddPagingAndSorting(Pageable page) {

		return AddSorting(page) + AddPaging(page);
	}

	public String AddPaging(Pageable page) {
		return " LIMIT " + page.getPageSize() + " OFFSET " + page.getOffset();
	}

	public String AddSorting(Pageable page) {
		Order order = !page.getSort().isEmpty() ? page.getSort().toList().get(0) : Order.by("ID");
		String orderBy = order.getProperty().toUpperCase();
		String dir = "";
		if (orderBy.indexOf(" ASC") < 0 && orderBy.indexOf(" DESC") < 0) {
			dir = order.getDirection().name();
		}
		return " ORDER BY " + orderBy + " " + dir;
	}

	public int getTotalRecords(Connection conn, String sqlFile, DealerFilter df, String... prms) {
		int rows = 0;
		String sqlText = Utils.readTextFromFile(sqlFile);
		String sql = injectWhere(sqlText, df);
		sql = Utils.replaceSchemaName(conn, sql);
		String cntRowsSql = "SELECT COUNT(*) FROM (" + sql + ")";
		try (PreparedStatement ps = conn.prepareStatement(cntRowsSql)) {
			for (int i = 0; i < prms.length; i++) {
				ps.setString(i + 1, prms[i]);
			}
			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next()) {
					rows = rs.getInt(1);
				}
			}
		} catch (SQLException e) {
			log.error("ERROR executing SQL : " + sql, e);
		}

		return rows;
	}

}
