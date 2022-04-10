package com.mnao.mfp.cr.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
public class ContactReportTopicDto {
	private String groupName;
	private List<String> topics;

}
