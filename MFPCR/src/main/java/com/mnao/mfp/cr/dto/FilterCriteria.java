package com.mnao.mfp.cr.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.mnao.mfp.common.util.AppConstants;
import com.mnao.mfp.cr.util.LocationEnum;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDate;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class FilterCriteria {

    private String rgnCd;

    private String zoneCd;

    private String districtCd;

    private String dlrCd;

    private List<String> issuesFilter;

    @JsonFormat(pattern = AppConstants.LOCALDATE_FORMAT)
    private LocalDate startDate;

    @JsonFormat(pattern = AppConstants.LOCALDATE_FORMAT)
    private LocalDate endDate;

	public LocationEnum forLocation() {
		if (notIsNullOrEmpty(this.rgnCd) && isNullOrEmpty(zoneCd) && isNullOrEmpty(districtCd) && isNullOrEmpty(dlrCd)) {
			return LocationEnum.REGION;
		} else if (notIsNullOrEmpty(this.rgnCd) && notIsNullOrEmpty(zoneCd) && isNullOrEmpty(districtCd) && isNullOrEmpty(dlrCd)) {
			return LocationEnum.ZONE;
		} else if (notIsNullOrEmpty(this.rgnCd) && notIsNullOrEmpty(zoneCd) && notIsNullOrEmpty(districtCd) && isNullOrEmpty(dlrCd)) {
			return LocationEnum.DISTRICT;
		} else if (notIsNullOrEmpty(this.rgnCd) && notIsNullOrEmpty(zoneCd) && notIsNullOrEmpty(districtCd) && notIsNullOrEmpty(dlrCd)) {
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
