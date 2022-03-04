package com.mnao.mfp.list.dao;

import java.sql.ResultSet;
import java.sql.SQLException;

import com.mnao.mfp.common.dao.MetricData;

public class ListDistrict extends MetricData {
	/*-
	 * ==================================
	 * Query: 
	 *	SELECT DISTINCT DISTRICT_CD 
	 *	FROM MFP.DEALERS d 
	 *	WHERE d.RGN_CD = ? 
	 *	AND   d.ZONE_CD = ? 
	 *
	 * ====================================
	*/
	//
	//DISTRICT_CD	VARCHAR
	private String districtCd ;

	public String getDistrictCd() {
		return districtCd;
	}

	public void setDistrictCd(String districtCd) {
		this.districtCd = districtCd;
	}

	@Override
	public String toString() {
		return "ListDistrict [districtCd=" + districtCd + "]";
	}

	@Override
	public void setResultSetRow(ResultSet rs) throws SQLException {
		this.districtCd = rs.getString(1);
	}

}
