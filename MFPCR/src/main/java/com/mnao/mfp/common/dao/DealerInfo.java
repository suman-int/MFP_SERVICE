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
		dlrCd = rs.getString(1);
		dbaNm = rs.getString(2);
		statusCd = rs.getString(3);
		statusDt = rs.getString(4);
		rgnCd = rs.getString(5);
		cityNm = rs.getString(6);
		zip1Cd = rs.getString(7);
		cntyCd = rs.getString(8);
		stCd = rs.getString(9);
		mdaCd = rs.getString(10);
		soaNm = rs.getString(11);
		apptDt = rs.getString(12);
		termDt = rs.getString(13);
		prevDLrCd = rs.getString(14);
		nxtDlrCd = rs.getString(15);
		timeZoneCd = rs.getString(16);
		soaCd = rs.getString(17);
		usedCarFl = rs.getString(18);
		cntryCd = rs.getString(19);
		zip2Cd = rs.getString(20);
		dlrInactvDt = rs.getString(21);
		svcOnlyFl = rs.getString(22);
		svcOnlyDt = rs.getString(23);
		mdaNm = rs.getString(24);
		lat = rs.getString(25);
		lon = rs.getString(26);
		zoneCd = rs.getString(27);
		districtCd = rs.getString(28);
		dealerStrtEnd = rs.getString(29);
		dealerEffEnd = rs.getString(20);
		facilityType = rs.getString(31);
		showroomType = rs.getString(32);
		rgnNm = rs.getString(33);
		dealershipFlag = rs.getString(34);
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
