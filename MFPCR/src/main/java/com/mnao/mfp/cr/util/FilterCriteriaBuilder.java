package com.mnao.mfp.cr.util;

import com.mnao.mfp.common.datafilters.FilterCriteria;
import com.mnao.mfp.common.util.AppConstants;
import org.springframework.util.Assert;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.mnao.mfp.common.util.Utils.isNotNullOrEmpty;

public class FilterCriteriaBuilder {

    private void FilterCriteriaBuilder() {

    }

    public static FilterCriteria buildFilterByIssueAndTiming(String issues, String startOf, String endOf) {
        List<String> issuesList = new ArrayList<>();
        if (isNotNullOrEmpty(issues)) {
            issuesList = Arrays.asList(issues.split(","));
        }
        FilterCriteria filterCriteria = FilterCriteria.builder().issuesFilter(issuesList).build();
        if (isNotNullOrEmpty(startOf)) {
            filterCriteria.setStartDate(LocalDate.parse(startOf, DateTimeFormatter.ofPattern(AppConstants.LOCALDATE_FORMAT)));
        }
        if (isNotNullOrEmpty(endOf)) {
            Assert.notNull(startOf, "startOf Required");
            filterCriteria.setEndDate(LocalDate.parse(endOf, DateTimeFormatter.ofPattern(AppConstants.LOCALDATE_FORMAT)));
        } else {
            filterCriteria.setEndDate(filterCriteria.getStartDate());
        }
        return filterCriteria;
    }

    public static FilterCriteria buildFilterByLocationAndIssueAndTiming(String regionId, String zoneId, String districtId, String dealerId, String issues, String startOf, String endOf) {
        FilterCriteria filterCriteria = buildFilterByIssueAndTiming(issues, startOf, endOf);
        if (isNotNullOrEmpty(regionId)) {
            filterCriteria.setRgnCd(regionId);
        }
        if (isNotNullOrEmpty(zoneId)) {
            filterCriteria.setZoneCd(zoneId);
        }
        if (isNotNullOrEmpty(districtId)) {
            filterCriteria.setDistrictCd(districtId);
        }
        if (isNotNullOrEmpty(dealerId)) {
            filterCriteria.setDlrCd(dealerId);
        }
        return filterCriteria;
    }
}
