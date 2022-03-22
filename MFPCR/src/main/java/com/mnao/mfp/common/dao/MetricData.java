package com.mnao.mfp.common.dao;

import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.sql.SQLException;

public abstract  class MetricData  {
	public abstract void setResultSetRow(ResultSet rs) throws SQLException ;
	protected void setFieldValue(String fldName, ResultSet rs, int  colIdx ) throws SQLException {
		String val = rs.getString(colIdx);
		if( val != null && val.trim().length() > 0 ) {
			try {
				val = val.trim();
				Field fld = this.getClass().getDeclaredField(fldName);
				fld.setAccessible(true);
				fld.set(this, val);
			} catch (NoSuchFieldException e) {
				e.printStackTrace();
			} catch (SecurityException e) {
				e.printStackTrace();
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}
		}
	}
}