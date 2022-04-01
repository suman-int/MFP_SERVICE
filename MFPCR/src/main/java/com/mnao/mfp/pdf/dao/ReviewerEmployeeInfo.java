package com.mnao.mfp.pdf.dao;

import java.sql.ResultSet;
import java.sql.SQLException;

import com.mnao.mfp.common.dao.MetricData;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ReviewerEmployeeInfo extends MetricData {

	private String prsnIdCd, statusCd, prsnTypeCd, firstNm, midlNm, lastNm, jobCd, loctnCd, userId, emailAddr, rgnCd,
			zoneCd, districtCd, jobTitleTx;

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
		jobTitleTx = rs.getString(14);

	}

}
