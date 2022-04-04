package com.mnao.mfp.cr.service;

import com.mnao.mfp.common.datafilters.FilterCriteria;

import java.util.List;
import java.util.Map;

public interface ReportSummaryService {
    List<Map<String, String>> getSummaryByLocation(FilterCriteria filterCriteria);

    List<Map<String, String>> getSummaryOfMonthByLocation(FilterCriteria filterCriteria);
}
