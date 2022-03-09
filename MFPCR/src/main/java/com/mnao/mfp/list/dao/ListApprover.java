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

	private String prsnIdCd, statusCd, jobCd, prsnTypeCd, firstNm, midlNm, lastNm;

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

	@Override
	public String toString() {
		return "ListApprover [prsnIdCd=" + prsnIdCd + ", statusCd=" + statusCd + ", jobCd=" + jobCd + ", prsnTypeCd="
				+ prsnTypeCd + ", firstNm=" + firstNm + ", midlNm=" + midlNm + ", lastNm=" + lastNm + "]";
	}

	@Override
	public void setResultSetRow(ResultSet rs) throws SQLException {
		prsnIdCd = rs.getString(1);
		statusCd = rs.getString(2);
		jobCd = rs.getString(3);
		prsnTypeCd = rs.getString(4);
		firstNm = rs.getString(5);
		midlNm = rs.getString(6);
		lastNm = rs.getString(7);
	}

}
