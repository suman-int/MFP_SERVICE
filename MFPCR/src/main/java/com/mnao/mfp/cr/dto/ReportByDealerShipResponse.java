package com.mnao.mfp.cr.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
public class ReportByDealerShipResponse {
	private  List<ReportByDealershipDto> draft;
	private  List<ReportByDealershipDto> discussionRequested;
	private  List<ReportByDealershipDto> submitted;
	private  List<ReportByDealershipDto> reviewed;
}
