package com.mnao.mfp.common.datafilters;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.mnao.mfp.common.util.AppConstants;
import com.mnao.mfp.cr.util.LocationEnum;
import lombok.*;

import java.time.LocalDate;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
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

    public boolean isNotNullOrEmpty(String value) {
        return (value != null && value.trim().length() > 0);
    }

    public boolean isNullOrEmpty(String value) {
        return !isNotNullOrEmpty(value);
    }

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

    public boolean isNotNullOrEmpty(LocalDate value) {
        return (value != null);
    }
}
