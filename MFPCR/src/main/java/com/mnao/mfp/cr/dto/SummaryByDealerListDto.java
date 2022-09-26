package com.mnao.mfp.cr.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
public class SummaryByDealerListDto {
	private String dealerCode;
	private String dealerName;
	private String stateName;
	private String cityName;
	private Integer zipCode;
	private String issue;

}
