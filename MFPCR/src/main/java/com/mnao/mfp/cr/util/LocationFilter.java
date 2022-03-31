package com.mnao.mfp.cr.util;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Builder
public class LocationFilter {
	private String rgnCd;
	private String zoneCd;
	private String districtCd;
	private String dlrCd;

	public LocationEnum forLocation() {
		if (isNotNullOrEmpty(this.rgnCd) && isNullOrEmpty(zoneCd) && isNullOrEmpty(districtCd) && isNullOrEmpty(dlrCd)) {
			return LocationEnum.REGION;
		} else if (isNotNullOrEmpty(this.rgnCd) && isNotNullOrEmpty(zoneCd) && isNullOrEmpty(districtCd) && isNullOrEmpty(dlrCd)) {
			return LocationEnum.ZONE;
		} else if (isNotNullOrEmpty(this.rgnCd) && isNotNullOrEmpty(zoneCd) && isNotNullOrEmpty(districtCd) && isNullOrEmpty(dlrCd)) {
			return LocationEnum.DISTRICT;
		} else if (isNotNullOrEmpty(this.rgnCd) && isNotNullOrEmpty(zoneCd) && isNotNullOrEmpty(districtCd) && isNotNullOrEmpty(dlrCd)) {
			return LocationEnum.DEALER;
		}
		return LocationEnum.ALL;
	}

	private boolean isNotNullOrEmpty(String value) {
		return (value != null && value.trim().length() > 0);
	}
	private boolean isNullOrEmpty(String value) {
		return !isNotNullOrEmpty(value);
	}
}
