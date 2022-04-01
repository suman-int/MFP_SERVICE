package com.mnao.mfp.cr.service;

import com.mnao.mfp.common.datafilters.FilterCriteria;
import com.mnao.mfp.common.dto.CommonResponse;
import com.mnao.mfp.cr.dto.ReportByDealerShipResponse;

public interface ContactInfoService {

    CommonResponse<ReportByDealerShipResponse> byDealership(FilterCriteria filterCriteria);
}
