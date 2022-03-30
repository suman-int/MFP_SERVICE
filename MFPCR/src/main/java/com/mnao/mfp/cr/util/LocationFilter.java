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
		if (this.regionCd != null && (zoneCd == null && districtCd == null && dealerCd == null)) {
			return LocationEnum.REGION;
		} else if (this.regionCd != null && zoneCd != null && (districtCd == null && dealerCd == null)) {
			return LocationEnum.ZONE;
		} else if (this.regionCd != null && zoneCd != null && districtCd != null && (dealerCd == null)) {
			return LocationEnum.DISTRICT;
		} else if (this.regionCd != null && zoneCd != null && districtCd != null && dealerCd != null) {
			return LocationEnum.DEALER;
		}
		return LocationEnum.ALL;
	}
}
