package com.mnao.mfp.cr.dto;

import java.util.List;

import com.mnao.mfp.cr.entity.ContactReportInfo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class ContactReportExecutionCoverageDto {
	//private String type;

	//private String coverage;
	private Long reportCount;
	private String dealerCode;
	private boolean isDealerDefeciencyIdentified;
	private boolean isServiceRetentionFysl;
	private List<ContactReportExecutionCoverageAuthorDto> authorDtos;
	private String dealerName;	
}
