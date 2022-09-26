package com.mnao.mfp.cr.service;

import com.mnao.mfp.common.datafilters.FilterCriteria;
import com.mnao.mfp.cr.dto.ContactReportExecutionCoverageDto;
import com.mnao.mfp.user.dao.MFPUser;

import java.util.List;

public interface ContactReportExecutionService {

    List<ContactReportExecutionCoverageDto> reportExecutionCoverageByReportTime(FilterCriteria filterCriteria, MFPUser mfpUser);
}
