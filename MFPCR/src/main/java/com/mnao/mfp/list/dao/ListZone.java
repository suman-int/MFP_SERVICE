package com.mnao.mfp.list.dao;

import java.sql.ResultSet;
import java.sql.SQLException;

import com.mnao.mfp.common.dao.MetricData;

public class ListZone extends MetricData {
	/*-
	 * ==================================
	 * Query: 
	 *	SELECT DISTINCT d.ZONE_CD
	 *	FROM MFP.DEALERS d 
	 *	WHERE d.RGN_CD = ?
	 *
	 * ====================================
	*/
	//
	// ZONE_CD VARCHAR
	private String zoneCd;

	public String getZoneCd() {
		return zoneCd;
	}

	public void setZoneCd(String zoneCd) {
		this.zoneCd = zoneCd;
	}


	@Override
	public void setResultSetRow(ResultSet rs) throws SQLException {
		super.setFieldValue("zoneCd", rs, 1);
	}

	@Override
	public String toString() {
		return "ListZone [zoneCd=" + zoneCd + "]";
	}

}
