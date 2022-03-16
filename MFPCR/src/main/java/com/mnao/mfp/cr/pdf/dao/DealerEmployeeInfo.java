package com.mnao.mfp.cr.pdf.dao;

import java.sql.ResultSet;
import java.sql.SQLException;

import com.mnao.mfp.common.dao.MetricData;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class DealerEmployeeInfo extends MetricData {
	/*
	 * SELECT PRSN_ID_CD, STATUS_CD, PRSN_TYPE_CD, FRST_NM, MIDL_NM, LAST_NM,
	 * STR1_AD, STR2_AD, STR3_AD, CITY_AD, ST_CD, ZIP_CD, CNTRY_CD, HIRE_CD,
	 * HIRE_DT, TRMNTN_DT, LOCTN_CD, JOB_START_DT, JOB_CD, JOB_TITLE_TX,
	 * JOB_TYPE_CD, W_UPDT_DT FROM PUBLIC.MFP.EMPLOYEES;
	 */
	private String prsnIdCd, statusCd, prsnTypeCd, firstNm, midlNm, lastNm, str1Ad, str2Ad, str3Ad, cityAd, stCd, zipCd,
			cntryCd, hireCd, hireDt, trmntnDt, loctnCd, jobStartDt, jobCd, jobTitleTx, jobTypeCd;

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
		cntryCd = rs.getString(13);
		hireCd = rs.getString(14);
		hireDt = rs.getString(15);
		trmntnDt = rs.getString(16);
		loctnCd = rs.getString(18);
		jobStartDt = rs.getString(19);
		jobCd = rs.getString(20);
		jobTitleTx = rs.getString(21);
		jobTypeCd = rs.getString(22);
	}

}
