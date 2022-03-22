package com.mnao.mfp.list.dao;

import java.sql.ResultSet;
import java.sql.SQLException;

import com.mnao.mfp.common.dao.MetricData;

public class ListApprover extends MetricData {
	/*
	 * SELECT A.PRSN_ID_CD, A.STATUS_CD, B.JOB_CD, A.PRSN_TYPE_CD, A.FRST_NM,
	 * A.MIDL_NM, A.LAST_NM, A.STR1_AD, A.STR2_AD, A.STR3_AD, A.CITY_AD, A.ST_CD,
	 * A.ZIP_CD, A.CNTRY_CD, A.PHNAC_NO, A.PHNEXC_NO, A.PHONE_NO, A.EXTNSN_NO,
	 * A.HIRE_CD, A.HIRE_DT, A.TRMNTN_DT, A.MINORITY_CD, A.SEX_CD, A.LOCTN_CD,
	 * A.PRSN_CMNT_TX, A.JOB_START_DT, A.LAST_UPDT_TM, A.LAST_USERID_CD,
	 * A.PRSN_SLTN_NM, A.HOME_PHONE_NO, A.EMPLOYEE_ID, A.PRSN_ID_CNTRY_CD,
	 * A.HRIS_JOB_TITLE_TX, A.MNAO_EMPLOYEE_ID, A.VENDOR_ID FROM mmas.btc03020 A,
	 * mmas.btc03050 B where a.PRSN_ID_CD = b.PRSN_ID_CD and b.JOB_CD IN ('MZ11',
	 * 'MB11', 'MC11', 'MD11', 'ME11', 'MO11', 'MP11')
	 * 
	 */

	private String prsnIdCd, statusCd, prsnTypeCd, firstNm, midlNm, lastNm;
	private String jobCd, loctnCd, userId, emailAddr, rgnCd, zoneCd, districtCd, jobTitleFx;

	public String getPrsnIdCd() {
		return prsnIdCd;
	}

	public void setPrsnIdCd(String prsnIdCd) {
		this.prsnIdCd = prsnIdCd;
	}

	public String getStatusCd() {
		return statusCd;
	}

	public void setStatusCd(String statusCd) {
		this.statusCd = statusCd;
	}

	public String getJobCd() {
		return jobCd;
	}

	public void setJobCd(String jobCd) {
		this.jobCd = jobCd;
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

	public String getLoctnCd() {
		return loctnCd;
	}

	public void setLoctnCd(String loctnCd) {
		this.loctnCd = loctnCd;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getEmailAddr() {
		return emailAddr;
	}

	public void setEmailAddr(String emailAddr) {
		this.emailAddr = emailAddr;
	}

	public String getRgnCd() {
		return rgnCd;
	}

	public void setRgnCd(String rgnCd) {
		this.rgnCd = rgnCd;
	}

	public String getZoneCd() {
		return zoneCd;
	}

	public void setZoneCd(String zoneCd) {
		this.zoneCd = zoneCd;
	}

	public String getDistrictCd() {
		return districtCd;
	}

	public void setDistrictCd(String districtCd) {
		this.districtCd = districtCd;
	}

	public String getJobTitleFx() {
		return jobTitleFx;
	}

	public void setJobTitleFx(String jobTitleFx) {
		this.jobTitleFx = jobTitleFx;
	}

	@Override
	public String toString() {
		return "ListApprover [prsnIdCd=" + prsnIdCd + ", statusCd=" + statusCd + ", prsnTypeCd=" + prsnTypeCd
				+ ", firstNm=" + firstNm + ", midlNm=" + midlNm + ", lastNm=" + lastNm + ", jobCd=" + jobCd
				+ ", loctnCd=" + loctnCd + ", userId=" + userId + ", emailAddr=" + emailAddr + ", rgnCd=" + rgnCd
				+ ", zoneCd=" + zoneCd + ", districtCd=" + districtCd + ", jobTitleFx=" + jobTitleFx + "]";
	}

	@Override
	public void setResultSetRow(ResultSet rs) throws SQLException {
		prsnIdCd = rs.getString(1);
		statusCd = rs.getString(2);
		prsnTypeCd = rs.getString(3);
		firstNm = rs.getString(4);
		midlNm = rs.getString(5);
		lastNm = rs.getString(6);
		jobCd = rs.getString(7);
		loctnCd = rs.getString(8);
		userId = rs.getString(9);
		emailAddr = rs.getString(10);
		rgnCd = rs.getString(11);
		zoneCd = rs.getString(12);
		districtCd = rs.getString(13);
		jobTitleFx = rs.getString(14);
	}

}
