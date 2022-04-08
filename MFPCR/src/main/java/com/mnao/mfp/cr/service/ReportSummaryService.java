package com.mnao.mfp.cr.service;

import com.mnao.mfp.common.datafilters.FilterCriteria;
import com.mnao.mfp.user.dao.MFPUser;

import java.util.List;
import java.util.Map;

public interface ReportSummaryService {
    List<Map<String, String>> getSummaryByLocation(FilterCriteria filterCriteria, MFPUser mfpUser);

    List<Map<String, String>> getSummaryOfMonthByLocation(FilterCriteria filterCriteria, MFPUser mfpUser);
}
