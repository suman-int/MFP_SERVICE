package com.mnao.mfp.cr.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class ReportByStatusDto {
	
	private String dlrCd;
	private long contactReportId;
	private int contactStatus;
	private String currentIssues;
	
	
}
