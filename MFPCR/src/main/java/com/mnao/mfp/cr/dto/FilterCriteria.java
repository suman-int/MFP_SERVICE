package com.mnao.mfp.cr.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.mnao.mfp.common.util.AppConstants;

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
}
