package com.mnao.mfp.common.dao;

import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mnao.mfp.MFPContactReportsApplication;

public abstract class MetricData {
	//
	private static final Logger log = LoggerFactory.getLogger(MetricData.class);
	//
	public abstract void setResultSetRow(ResultSet rs) throws SQLException;

	protected void setFieldValue(String fldName, ResultSet rs, int colIdx) throws SQLException {
		String val = rs.getString(colIdx);
		if (val == null)
			val = "";
		try {
			val = val.trim();
			Field fld = this.getClass().getDeclaredField(fldName);
			fld.setAccessible(true);
			fld.set(this, val);
		} catch (NoSuchFieldException e) {
			log.error("", e);
		} catch (SecurityException e) {
			log.error("", e);
		} catch (IllegalArgumentException e) {
			log.error("", e);
		} catch (IllegalAccessException e) {
			log.error("", e);
		}
	}
}