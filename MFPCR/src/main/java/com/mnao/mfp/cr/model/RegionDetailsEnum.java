package com.mnao.mfp.cr.model;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;

@Getter
public enum RegionDetailsEnum {
	GU(),
	MW(),
	NE(),
	PA();

	public static List<String> namevalues() {
		List<String> allRegionName = new ArrayList<>();
		for (RegionDetailsEnum regionEnum : values()) {
			allRegionName.add(regionEnum.name());
		}
		return allRegionName;
	}
}
