package com.mnao.mfp.list.dao;

import java.sql.ResultSet;
import java.sql.SQLException;

import com.mnao.mfp.common.dao.MetricData;

public class ListRegion extends MetricData {

	/*-
	 * ==================================
	 * Query: 
	 *	SELECT DISTINCT d.RGN_CD, d.RGN_NM
	 *	FROM MFP.DEALERS d 
	 *
	 * ====================================
	*/
	//
	// RGN_CD VARCHAR
	private String rgnCd;
	//
	// RGN_NM VARCHAR
	private String rgnNm;

	@Override
	public String toString() {
		return "ListRegions [rgnCd=" + rgnCd + ", rgnNm=" + rgnNm + "]";
	}

	public String getRgnCd() {
		return rgnCd;
	}

	public void setRgnCd(String rgnCd) {
		this.rgnCd = rgnCd;
	}

	public String getRgnNm() {
		return rgnNm;
	}

	public void setRgnNm(String rgnNm) {
		this.rgnNm = rgnNm;
	}

	@Override
	public void setResultSetRow(ResultSet rs) throws SQLException {
		super.setFieldValue("rgnCd", rs, 1);
		super.setFieldValue("rgnNm", rs, 2);
	}

}
