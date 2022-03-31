package com.mnao.mfp.cr.util;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class LocationFilter {
	private String regionCd;
	private String zoneCd;
	private String districtCd;
	private String dealerCd;

	public LocationEnum forLocation() {
		if (notIsNullOrEmpty(this.regionCd) && isNullOrEmpty(zoneCd) && isNullOrEmpty(districtCd) && isNullOrEmpty(dealerCd)) {
			return LocationEnum.REGION;
		} else if (notIsNullOrEmpty(this.regionCd) && notIsNullOrEmpty(zoneCd) && isNullOrEmpty(districtCd) && isNullOrEmpty(dealerCd)) {
			return LocationEnum.ZONE;
		} else if (notIsNullOrEmpty(this.regionCd) && notIsNullOrEmpty(zoneCd) && notIsNullOrEmpty(districtCd) && isNullOrEmpty(dealerCd)) {
			return LocationEnum.DISTRICT;
		} else if (notIsNullOrEmpty(this.regionCd) && notIsNullOrEmpty(zoneCd) && notIsNullOrEmpty(districtCd) && notIsNullOrEmpty(dealerCd)) {
			return LocationEnum.DEALER;
		}
		return LocationEnum.ALL;
	}

	private boolean notIsNullOrEmpty(String value) {
		return (value != null && value.trim().length() > 0);
	}
	private boolean isNullOrEmpty(String value) {
		return !notIsNullOrEmpty(value);
	}
}
