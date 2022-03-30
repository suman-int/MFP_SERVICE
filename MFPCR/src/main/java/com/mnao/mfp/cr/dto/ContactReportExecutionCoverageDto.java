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
	private String type;
	private String author;
	private String coverage;
	private Integer reportCount;
	private String dealerCode;
	private List<ContactReportInfo> reports;
	private String dealerName;	
}
