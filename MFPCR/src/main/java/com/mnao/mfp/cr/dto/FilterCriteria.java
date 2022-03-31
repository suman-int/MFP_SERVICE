package com.mnao.mfp.cr.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.mnao.mfp.common.util.AppConstants;
import com.mnao.mfp.cr.util.LocationEnum;

import com.mnao.mfp.cr.util.LocationFilter;
import lombok.*;

import java.time.LocalDate;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class FilterCriteria extends LocationFilter {

    private List<String> issuesFilter;

    @JsonFormat(pattern = AppConstants.LOCALDATE_FORMAT)
    private LocalDate startDate;

    @JsonFormat(pattern = AppConstants.LOCALDATE_FORMAT)
    private LocalDate endDate;

}
