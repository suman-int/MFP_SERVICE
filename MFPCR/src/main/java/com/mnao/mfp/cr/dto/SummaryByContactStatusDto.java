package com.mnao.mfp.cr.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SummaryByContactStatusDto {

	private String issue;
	private long draftCount;
	private long pendingReviewCount;
	private long reviewCount;
	private long total;
}
