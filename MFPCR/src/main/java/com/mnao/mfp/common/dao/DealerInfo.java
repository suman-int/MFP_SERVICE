package com.mnao.mfp.common.dao;

import java.sql.ResultSet;
import java.sql.SQLException;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class DealerInfo extends MetricData {
	String dlrCd, dbaNm, statusCd, statusDt, rgnCd, cityNm, zip1Cd, cntyCd, stCd, mdaCd, soaNm, apptDt, termDt,
			prevDLrCd, nxtDlrCd, timeZoneCd, soaCd, usedCarFl, cntryCd, zip2Cd, dlrInactvDt, svcOnlyFl, svcOnlyDt,
			mdaNm, lat, lon, zoneCd, districtCd, dealerStrtEnd, dealerEffEnd, facilityType, showroomType, rgnNm,
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
		this.dbaNm = (this.dbaNm != null) ? this.dbaNm.trim() : "";
		this.statusCd = (this.statusCd != null) ? this.statusCd.trim() : "";
		this.statusDt = (this.statusDt != null) ? this.statusDt.trim() : "";
		this.rgnCd = (this.rgnCd != null) ? this.rgnCd.trim() : "";
		this.cityNm = (this.cityNm != null) ? this.cityNm.trim() : "";
		this.zip1Cd = (this.zip1Cd != null) ? this.zip1Cd.trim() : "";
		this.cntyCd = (this.cntyCd != null) ? this.cntyCd.trim() : "";
		this.stCd = (this.stCd != null) ? this.stCd.trim() : "";
		this.mdaCd = (this.mdaCd != null) ? this.mdaCd.trim() : "";
		this.soaNm = (this.soaNm != null) ? this.soaNm.trim() : "";
		this.apptDt = (this.apptDt != null) ? this.apptDt.trim() : "";
		this.termDt = (this.termDt != null) ? this.termDt.trim() : "";
		this.prevDLrCd = (this.prevDLrCd != null) ? this.prevDLrCd.trim() : "";
		this.nxtDlrCd = (this.nxtDlrCd != null) ? this.nxtDlrCd.trim() : "";
		this.timeZoneCd = (this.timeZoneCd != null) ? this.timeZoneCd.trim() : "";
		this.soaCd = (this.soaCd != null) ? this.soaCd.trim() : "";
		this.usedCarFl = (this.usedCarFl != null) ? this.usedCarFl.trim() : "";
		this.cntryCd = (this.cntryCd != null) ? this.cntryCd.trim() : "";
		this.zip2Cd = (this.zip2Cd != null) ? this.zip2Cd.trim() : "";
		this.dlrInactvDt = (this.dlrInactvDt != null) ? this.dlrInactvDt.trim() : "";
		this.svcOnlyFl = (this.svcOnlyFl != null) ? this.svcOnlyFl.trim() : "";
		this.svcOnlyDt = (this.svcOnlyDt != null) ? this.svcOnlyDt.trim() : "";
		this.zoneCd = (this.zoneCd != null) ? this.zoneCd.trim() : "";
		this.districtCd = (this.districtCd != null) ? this.districtCd.trim() : "";
		this.facilityType = (this.facilityType != null) ? this.facilityType.trim() : "";
		this.dlrCd = (this.dlrCd != null) ? this.dlrCd.trim() : "";
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
