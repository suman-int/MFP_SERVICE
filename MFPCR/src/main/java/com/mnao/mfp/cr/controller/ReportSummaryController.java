package com.mnao.mfp.cr.controller;

import com.mnao.mfp.common.dao.MetricData;
import com.mnao.mfp.common.datafilters.FilterCriteria;
import com.mnao.mfp.common.dto.CommonResponse;
import com.mnao.mfp.common.service.AbstractService;
import com.mnao.mfp.cr.service.ReportSummaryService;
import com.mnao.mfp.cr.util.FilterCriteriaBuilder;
import com.mnao.mfp.user.dao.MFPUser;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping(value = "/report-summary")
public class ReportSummaryController {
	//
	private static final Logger log = LoggerFactory.getLogger(ReportSummaryController.class);
	//

    @Autowired
    private ReportSummaryService summaryService;

    @GetMapping(value = "/v2/by-issue/all")
    public CommonResponse<List<Map<String, String>>> summaryByIssueForRegion(
            @RequestParam(required = false) String issues,
            @RequestParam(required = false) String startOf,
            @RequestParam(required = false) String endOf,
            @SessionAttribute(name = "mfpUser") MFPUser mfpUser
    ) {
        try {
            FilterCriteria filterCriteria = FilterCriteriaBuilder.buildFilterByIssueAndTiming(issues, startOf, endOf);
            List<Map<String, String>> response = summaryService.getSummaryByLocation(filterCriteria, mfpUser);
            return AbstractService.httpPostSuccess(response, "Success");
        } catch (Exception e) {
            return AbstractService.httpPostError(e);
        }
    }

    @GetMapping(value = "/v2/by-issue/region")
    public CommonResponse<List<Map<String, String>>> summaryByIssueForRegion(
            @RequestParam() String regionId,
            @RequestParam(required = false) String issues,
            @RequestParam(required = false) String startOf,
            @RequestParam(required = false) String endOf,
            @SessionAttribute(name = "mfpUser") MFPUser mfpUser
    ) {
        try {
            FilterCriteria filterCriteria = FilterCriteriaBuilder.buildFilterByLocationAndIssueAndTiming(regionId, null, null, null, issues, startOf, endOf);
            List<Map<String, String>> response = summaryService.getSummaryByLocation(filterCriteria, mfpUser);
            return AbstractService.httpPostSuccess(response, "Success");
        } catch (Exception e) {
            return AbstractService.httpPostError(e);
        }
    }

    @GetMapping(value = "/v2/by-issue/region/zone")
    public CommonResponse<List<Map<String, String>>> summaryByIssueForZone(
            @RequestParam() String regionId,
            @RequestParam() String zoneId,
            @RequestParam(required = false) String issues,
            @RequestParam(required = false) String startOf,
            @RequestParam(required = false) String endOf,
            @SessionAttribute(name = "mfpUser") MFPUser mfpUser
    ) {
        try {
            FilterCriteria filterCriteria = FilterCriteriaBuilder.buildFilterByLocationAndIssueAndTiming(regionId, zoneId, null, null, issues, startOf, endOf);
            List<Map<String, String>> response = summaryService.getSummaryByLocation(filterCriteria, mfpUser);
            return AbstractService.httpPostSuccess(response, "Success");
        } catch (Exception e) {
            return AbstractService.httpPostError(e);
        }
    }

    @GetMapping(value = "/v2/by-issue/region/zone/district")
    public CommonResponse<List<Map<String, String>>> summaryByIssueForDistrict(
            @RequestParam() String regionId,
            @RequestParam() String zoneId,
            @RequestParam() String districtId,
            @RequestParam(required = false) String issues,
            @RequestParam(required = false) String startOf,
            @RequestParam(required = false) String endOf,
            @SessionAttribute(name = "mfpUser") MFPUser mfpUser
    ) {
        try {
            FilterCriteria filterCriteria = FilterCriteriaBuilder.buildFilterByLocationAndIssueAndTiming(regionId, zoneId, districtId, null, issues, startOf, endOf);
            List<Map<String, String>> response = summaryService.getSummaryByLocation(filterCriteria, mfpUser);
            return AbstractService.httpPostSuccess(response, "Success");
        } catch (Exception e) {
            return AbstractService.httpPostError(e);
        }
    }

    @GetMapping(value = "/v2/by-month/all")
    public CommonResponse<List<Map<String, String>>> summaryByMonthForAll(
            @RequestParam(required = false) String issues,
            @RequestParam(required = false) String startOf,
            @RequestParam(required = false) String endOf,
            @SessionAttribute(name = "mfpUser") MFPUser mfpUser
    ) {
        try {
            FilterCriteria filterCriteria = FilterCriteriaBuilder.buildFilterByIssueAndTiming(issues, startOf, endOf);
            List<Map<String, String>> response = summaryService.getSummaryOfMonthByLocation(filterCriteria, mfpUser);
            return AbstractService.httpPostSuccess(response, "Success");
        } catch (Exception e) {
            log.error("", e);
            return AbstractService.httpPostError(e);
        }
    }

    @GetMapping(value = "/v2/by-month/region")
    public CommonResponse<List<Map<String, String>>> summaryByMonthForRegion(
            @RequestParam() String regionId,
            @RequestParam(required = false) String issues,
            @RequestParam(required = false) String startOf,
            @RequestParam(required = false) String endOf,
            @SessionAttribute(name = "mfpUser") MFPUser mfpUser
    ) {
        try {
            FilterCriteria filterCriteria = FilterCriteriaBuilder.buildFilterByLocationAndIssueAndTiming(regionId, null, null, null, issues, startOf, endOf);
            List<Map<String, String>> response = summaryService.getSummaryOfMonthByLocation(filterCriteria, mfpUser);
            return AbstractService.httpPostSuccess(response, "Success");
        } catch (Exception e) {
            return AbstractService.httpPostError(e);
        }
    }

    @GetMapping(value = "/v2/by-month/region/zone")
    public CommonResponse<List<Map<String, String>>> summaryByMonthForZone(
            @RequestParam() String regionId,
            @RequestParam() String zoneId,
            @RequestParam(required = false) String issues,
            @RequestParam(required = false) String startOf,
            @RequestParam(required = false) String endOf,
            @SessionAttribute(name = "mfpUser") MFPUser mfpUser
    ) {
        try {
            FilterCriteria filterCriteria = FilterCriteriaBuilder.buildFilterByLocationAndIssueAndTiming(regionId, zoneId, null, null, issues, startOf, endOf);
            List<Map<String, String>> response = summaryService.getSummaryOfMonthByLocation(filterCriteria, mfpUser);
            return AbstractService.httpPostSuccess(response, "Success");
        } catch (Exception e) {
            return AbstractService.httpPostError(e);
        }
    }

    @GetMapping(value = "/v2/by-month/region/zone/district")
    public CommonResponse<List<Map<String, String>>> summaryByMonthForDistrict(
            @RequestParam() String regionId,
            @RequestParam() String zoneId,
            @RequestParam() String districtId,
            @RequestParam(required = false) String issues,
            @RequestParam(required = false) String startOf,
            @RequestParam(required = false) String endOf,
            @SessionAttribute(name = "mfpUser") MFPUser mfpUser
    ) {
        try {
            FilterCriteria filterCriteria = FilterCriteriaBuilder.buildFilterByLocationAndIssueAndTiming(regionId, zoneId, districtId, null, issues, startOf, endOf);
            List<Map<String, String>> response = summaryService.getSummaryOfMonthByLocation(filterCriteria, mfpUser);
            return AbstractService.httpPostSuccess(response, "Success");
        } catch (Exception e) {
            return AbstractService.httpPostError(e);
        }
    }

}
