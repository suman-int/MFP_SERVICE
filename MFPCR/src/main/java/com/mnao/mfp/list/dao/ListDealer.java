package com.mnao.mfp.list.dao;

import java.sql.ResultSet;
import java.sql.SQLException;

import com.mnao.mfp.common.dao.MetricData;

public class ListDealer extends MetricData {
	/*-
	 * ==================================
	 * Query: 
	 *	SELECT DISTINCT d.DLR_CD , d.DBA_NM 
	 *	FROM MFP.DEALERS d 
	 *	WHERE d.RGN_CD = ? 
	 *	AND   d.ZONE_CD = ?
	 *	AND   d.DISTRICT_CD = ? 
	 *
	 * ====================================
	*/
	//
	//DLR_CD	VARCHAR
	private String dlrCd ;
	//
	//DBA_NM	VARCHAR
	private String dbaNm ;
	//
	//CITY_NM	VARCHAR
	private String cityNm ;
	//
	//
	//ZIP1_CD	VARCHAR
	private String zip1Cd ;
	//
	//
	//ST_CD	VARCHAR
	private String stCd ;
	//

	public String getDlrCd() {
		return dlrCd;
	}

	public void setDlrCd(String dlrCd) {
		this.dlrCd = dlrCd;
	}

	public String getDbaNm() {
		return dbaNm;
	}

	public void setDbaNm(String dbaNm) {
		this.dbaNm = dbaNm;
	}

	public String getCityNm() {
		return cityNm;
	}

	public void setCityNm(String cityNm) {
		this.cityNm = cityNm;
	}

	public String getZip1Cd() {
		return zip1Cd;
	}

	public void setZip1Cd(String zip1Cd) {
		this.zip1Cd = zip1Cd;
	}

	public String getStCd() {
		return stCd;
	}

	public void setStCd(String stCd) {
		this.stCd = stCd;
	}

	@Override
	public String toString() {
		return "ListDealer [dlrCd=" + dlrCd + ", dbaNm=" + dbaNm + ", cityNm=" + cityNm + ", zip1Cd=" + zip1Cd
				+ ", stCd=" + stCd + "]";
	}

	@Override
	public void setResultSetRow(ResultSet rs) throws SQLException {
		super.setFieldValue("dlrCd", rs, 1);
		super.setFieldValue("dbaNm", rs, 2);
		super.setFieldValue("cityNm", rs, 6);
		super.setFieldValue("stCd", rs, 9);
		super.setFieldValue("zip1Cd", rs, 7);
	}

}
