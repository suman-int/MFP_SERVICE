package com.mnao.mfp.cr.dto;

import java.util.List;

import com.mnao.mfp.list.dao.ListPersonnel;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class RegionZoneReviewer {
	private String region;
	private String zone;
	private List<ListPersonnel> reviewers;
	//
	public String getRegionZone() {
		String retStr = (region.trim() + "  ").substring(0,2);
		retStr = retStr + (zone.trim() + "00").substring(0,2);
		return retStr;
	}
}
