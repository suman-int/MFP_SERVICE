package com.mnao.mfp.common.dao;

import java.sql.ResultSet;
import java.sql.SQLException;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class DealerInfo extends MetricData {
	String dlrCd,
	dbaNm,
	statusCd,
	statusDt,
	rgnCd,
	cityNm,
	zip1Cd,
	cntyCd,
	stCd,
	mdaCd,
	soaNm,
	apptDt,
	termDt,
	prevDLrCd,
	nxtDlrCd,
	timeZoneCd,
	soaCd,
	usedCarFl,
	cntryCd,
	zip2Cd,
	dlrInactvDt,
	svcOnlyFl,
	svcOnlyDt,
	mdaNm,
	lat,
	lon,
	zoneCd,
	districtCd,
	dealerStrtEnd,
	dealerEffEnd,
	facilityType,
	showroomType,
	rgnNm,
	dealershipFlag;

	@Override
	public void setResultSetRow(ResultSet rs) throws SQLException {
		this.dbaNm = rs.getString(1);
		this.statusCd = rs.getString(2);
		this.statusDt = rs.getString(3);
		this.rgnCd = rs.getString(4);
		this.cityNm = rs.getString(5);
		this.zip1Cd = rs.getString(6);
		this.cntyCd = rs.getString(7);
		this.stCd = rs.getString(8);
		this.mdaCd = rs.getString(9);
		this.soaNm = rs.getString(10);
		this.apptDt = rs.getString(11);
		this.termDt = rs.getString(12);
		this.prevDLrCd = rs.getString(13);
		this.nxtDlrCd = rs.getString(14);
		this.timeZoneCd = rs.getString(15);
		this.soaCd = rs.getString(16);
		this.usedCarFl = rs.getString(17);
		this.cntryCd = rs.getString(18);
		this.zip2Cd = rs.getString(19);
		this.dlrInactvDt = rs.getString(20);
		this.svcOnlyFl = rs.getString(21);
		this.svcOnlyDt = rs.getString(22);
		this.zoneCd = rs.getString(23);
		this.districtCd = rs.getString(24);
		this.facilityType = rs.getString(25);
		this.dlrCd = rs.getString(26);
	}

	public void copy(DealerInfo src) {
		this.dlrCd = src.dlrCd;
		this.dbaNm = src.dbaNm;
		this.statusCd = src.statusCd;
		this.statusDt = src.statusDt;
		this.rgnCd = src.rgnCd;
		this.cityNm = src.cityNm;
		this.zip1Cd = src.zip1Cd;
		this.cntyCd = src.cntyCd;
		this.stCd = src.stCd;
		this.mdaCd = src.mdaCd;
		this.soaNm = src.soaNm;
		this.apptDt = src.apptDt;
		this.termDt = src.termDt;
		this.prevDLrCd = src.prevDLrCd;
		this.nxtDlrCd = src.nxtDlrCd;
		this.timeZoneCd = src.timeZoneCd;
		this.soaCd = src.soaCd;
		this.usedCarFl = src.usedCarFl;
		this.cntryCd = src.cntryCd;
		this.zip2Cd = src.zip2Cd;
		this.dlrInactvDt = src.dlrInactvDt;
		this.svcOnlyFl = src.svcOnlyFl;
		this.svcOnlyDt = src.svcOnlyDt;
		this.mdaNm = src.mdaNm;
		this.lat = src.lat;
		this.lon = src.lon;
		this.zoneCd = src.zoneCd;
		this.districtCd = src.districtCd;
		this.dealerStrtEnd = src.dealerStrtEnd;
		this.dealerEffEnd = src.dealerEffEnd;
		this.facilityType = src.facilityType;
		this.showroomType = src.showroomType;
		this.rgnNm = src.rgnNm;
		this.dealershipFlag = src.dealershipFlag;
	}

}
