package com.mnao.mfp.cr.dto;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.mnao.mfp.common.util.AppConstants;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class ReportByDealershipDto {

	private String rgnCd;

	private String zoneCd;

	private String districtCd;

	private String dlrCd;

	private String dbaNm;

	private long contactReportId;
	
	@JsonFormat(pattern = AppConstants.LOCALDATE_FORMAT)
	private LocalDate contactDt;
	
	private String contactAuthor;
	
	private int contactStatus;
	
	private String currentIssues;

}
