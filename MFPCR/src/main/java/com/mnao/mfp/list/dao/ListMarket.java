package com.mnao.mfp.list.dao;

import java.sql.ResultSet;
import java.sql.SQLException;

import com.mnao.mfp.common.dao.MetricData;

public class ListMarket extends MetricData {
	/*-
	 * ==================================
	 * Query: 
	 *	SELECT DISTINCT d.MDA_CD, d.MDA_NM 
	 *	FROM MFP.DEALERS d 
	 *	WHERE d.MDA_CD IS NOT NULL
	 *
	 * ====================================
	*/
	//
	// MDA_CD VARCHAR
	private String mdaCd;
	//
	// MDA_NM VARCHAR
	private String mdaNm;

	@Override
	public void setResultSetRow(ResultSet rs) throws SQLException {
		super.setFieldValue("mdaCd", rs, 1);
		super.setFieldValue("mdaNm", rs, 2);
	}

	public String getMdaCd() {
		return mdaCd;
	}

	public void setMdaCd(String mdaCd) {
		this.mdaCd = mdaCd;
	}

	public String getMdaNm() {
		return mdaNm;
	}

	public void setMdaNm(String mdaNm) {
		this.mdaNm = mdaNm;
	}

	@Override
	public String toString() {
		return "ListMarkets [mdaCd=" + mdaCd + ", mdaNm=" + mdaNm + "]";
	}

}
