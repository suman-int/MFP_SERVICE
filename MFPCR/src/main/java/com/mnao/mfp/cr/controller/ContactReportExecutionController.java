package com.mnao.mfp.cr.controller;

import com.mnao.mfp.common.datafilters.FilterCriteria;
import com.mnao.mfp.common.dto.CommonResponse;
import com.mnao.mfp.common.service.AbstractService;
import com.mnao.mfp.cr.dto.ContactReportExecutionCoverageDto;
import com.mnao.mfp.cr.service.ContactReportExecutionService;
import com.mnao.mfp.cr.util.FilterCriteriaBuilder;
import com.mnao.mfp.user.dao.MFPUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(value = "/report-summary")
public class ContactReportExecutionController {

    @Autowired
    private ContactReportExecutionService contactReportExecutionService;

    @GetMapping(value = "/report-execution/coverage")
    public CommonResponse<List<ContactReportExecutionCoverageDto>> reportExecutionByCoverage(
            @RequestParam(required = false) String regionId,
            @RequestParam(required = false) String zoneId,
            @RequestParam(required = false) String districtId,
            @RequestParam(required = false) String dealerId,
            @RequestParam() String startOf,
            @RequestParam(required = false) String endOf,
            @SessionAttribute(name = "mfpUser") MFPUser mfpUser
    ) {
        try {
            FilterCriteria filterCriteria = FilterCriteriaBuilder.buildFilterByLocationAndIssueAndTiming(regionId, zoneId, districtId, dealerId, null, startOf, endOf);
            List<ContactReportExecutionCoverageDto> response = contactReportExecutionService.reportExecutionCoverageByReportTime(filterCriteria);
            return AbstractService.httpPostSuccess(response, "Success");
        } catch (Exception e) {
            return AbstractService.httpPostError(e);
        }
    }

    @GetMapping(value = "/report-execution/exception")
    public CommonResponse<List<ContactReportExecutionCoverageDto>> reportExecutionByException(
            @RequestParam(required = false) String regionId,
            @RequestParam(required = false) String zoneId,
            @RequestParam(required = false) String districtId,
            @RequestParam(required = false) String dealerId,
            @RequestParam() String startOf,
            @RequestParam(required = false) String endOf,
            @SessionAttribute(name = "mfpUser") MFPUser mfpUser
    ) {
        try {
            FilterCriteria filterCriteria = FilterCriteriaBuilder.buildFilterByLocationAndIssueAndTiming(regionId, zoneId, districtId, dealerId, null, startOf, endOf);
            List<ContactReportExecutionCoverageDto> response = contactReportExecutionService.reportExecutionCoverageByReportTime(filterCriteria);
            return AbstractService.httpPostSuccess(response, "Success");
        } catch (Exception e) {
            return AbstractService.httpPostError(e);
        }
    }
}
