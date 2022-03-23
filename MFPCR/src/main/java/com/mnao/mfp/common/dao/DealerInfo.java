package com.mnao.mfp.common.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.mnao.mfp.cr.entity.ContactReportInfo;

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

}
