package com.mnao.mfp.list.dao;

import java.sql.ResultSet;
import java.sql.SQLException;

import com.mnao.mfp.common.dao.MetricData;

public class ListEmployee extends MetricData {
	/*
	 * SELECT PRSN_ID_CD, STATUS_CD, PRSN_TYPE_CD, FRST_NM, MIDL_NM, LAST_NM,
	 * STR1_AD, STR2_AD, STR3_AD, CITY_AD, ST_CD, ZIP_CD, CNTRY_CD, HIRE_CD,
	 * HIRE_DT, TRMNTN_DT, LOCTN_CD, JOB_START_DT, JOB_CD, JOB_TITLE_TX,
	 * JOB_TYPE_CD, W_UPDT_DT FROM PUBLIC.MFP.EMPLOYEES;
	 */
	private String prsnIdCd, statusCd, prsnTypeCd, firstNm, midlNm, lastNm, str1Ad, str2Ad, str3Ad, cityAd, stCd, zipCd;

	public String getPrsnIdCd() {
		return prsnIdCd;
	}

	public void setPrsnIdCd(String prnnIdCd) {
		this.prsnIdCd = prnnIdCd;
	}

	public String getStatusCd() {
		return statusCd;
	}

	public void setStatusCd(String statusCd) {
		this.statusCd = statusCd;
	}

	public String getPrsnTypeCd() {
		return prsnTypeCd;
	}

	public void setPrsnTypeCd(String prsnTypeCd) {
		this.prsnTypeCd = prsnTypeCd;
	}

	public String getFirstNm() {
		return firstNm;
	}

	public void setFirstNm(String firstNm) {
		this.firstNm = firstNm;
	}

	public String getMidlNm() {
		return midlNm;
	}

	public void setMidlNm(String midlNm) {
		this.midlNm = midlNm;
	}

	public String getLastNm() {
		return lastNm;
	}

	public void setLastNm(String lastNm) {
		this.lastNm = lastNm;
	}

	public String getStr1Ad() {
		return str1Ad;
	}

	public void setStr1Ad(String str1Ad) {
		this.str1Ad = str1Ad;
	}

	public String getStr2Ad() {
		return str2Ad;
	}

	public void setStr2Ad(String str2Ad) {
		this.str2Ad = str2Ad;
	}

	public String getStr3Ad() {
		return str3Ad;
	}

	public void setStr3Ad(String str3Ad) {
		this.str3Ad = str3Ad;
	}

	public String getCityAd() {
		return cityAd;
	}

	public void setCityAd(String cityAd) {
		this.cityAd = cityAd;
	}

	public String getStCd() {
		return stCd;
	}

	public void setStCd(String stCd) {
		this.stCd = stCd;
	}

	public String getZipCd() {
		return zipCd;
	}

	public void setZipCd(String zipCd) {
		this.zipCd = zipCd;
	}

	@Override
	public String toString() {
		return "ListEmployee [prnnIdCd=" + prsnIdCd + ", statusCd=" + statusCd + ", prsnTypeCd=" + prsnTypeCd
				+ ", firstNm=" + firstNm + ", midlNm=" + midlNm + ", lastNm=" + lastNm + ", str1Ad=" + str1Ad
				+ ", str2Ad=" + str2Ad + ", str3Ad=" + str3Ad + ", cityAd=" + cityAd + ", stCd=" + stCd + ", zipCd="
				+ zipCd + "]";
	}

	@Override
	public void setResultSetRow(ResultSet rs) throws SQLException {
		prsnIdCd = rs.getString(1);
		statusCd = rs.getString(2);
		prsnTypeCd = rs.getString(3);
		firstNm = rs.getString(4);
		midlNm = rs.getString(5);
		lastNm = rs.getString(6);
		str1Ad = rs.getString(7);
		str2Ad = rs.getString(8);
		str3Ad = rs.getString(9);
		cityAd = rs.getString(10);
		stCd = rs.getString(11);
		zipCd = rs.getString(12);

	}

}
