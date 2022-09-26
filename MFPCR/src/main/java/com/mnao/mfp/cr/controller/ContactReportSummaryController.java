package com.mnao.mfp.cr.controller;

import com.mnao.mfp.common.datafilters.FilterCriteria;
import com.mnao.mfp.common.dto.CommonResponse;
import com.mnao.mfp.common.service.AbstractService;
import com.mnao.mfp.cr.dto.SummaryByContactStatusDto;
import com.mnao.mfp.cr.dto.SummaryByDealerListDto;
import com.mnao.mfp.cr.entity.ContactReportDiscussion;
import com.mnao.mfp.cr.model.ContactReportResponse;
import com.mnao.mfp.cr.model.DealersByIssue;
import com.mnao.mfp.cr.service.ContactReportService;
import com.mnao.mfp.cr.service.ContactReportSummaryService;
import com.mnao.mfp.cr.service.GenericResponseWrapper;
import com.mnao.mfp.cr.util.FilterCriteriaBuilder;
import com.mnao.mfp.cr.util.IssueType;
import com.mnao.mfp.user.dao.MFPUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping(value = "/report-summary")
public class ContactReportSummaryController {

    @Autowired
    private ContactReportService contactReportService;

    @Autowired
    private ContactReportSummaryService contactReportSummaryService;

    @Autowired
    private IssueType issueType;

    @GetMapping(value = "/dealer-issue")
    public List<DealersByIssue> dealerIssue() {
        return contactReportService.getAllDealersByIssue();
    }

    @GetMapping(value = "/by-issue/{type}/{value}/{category}")
    @Deprecated()
    public ContactReportResponse summaryByIssue(@PathVariable("type") String type,
                                                @PathVariable("value") String value,
                                                @PathVariable("category") String category,
                                                @SessionAttribute(name = "mfpUser") MFPUser mfpUser) {
        return GenericResponseWrapper.contactReportResponseFunction
                .apply(contactReportSummaryService
                        .getSummaryByLocation(type, value, category, mfpUser, (contactReportInfoList, issue, status, predicate) ->
                                contactReportInfoList.stream()
                                        .filter(contactReportInfo -> predicate.test(contactReportInfo, status))
                                        .filter(contactReportInfo -> {
                                            Optional<ContactReportDiscussion> optionalContactReportDiscussion = contactReportInfo.getDiscussions().stream()
                                                    .filter(contactReportDiscussion -> contactReportDiscussion
                                                            .getDiscussion().equals(issue)).findAny();
                                            return optionalContactReportDiscussion.isPresent();
                                        })
                                        .count()

                        ), null);
    }


    @GetMapping(value = "/by-month/{type}/{value}")
    public ContactReportResponse summaryByMonth(@PathVariable("type") String type,
                                                @PathVariable("value") String value
    ) {
        return GenericResponseWrapper.contactReportResponseFunction
                .apply(contactReportSummaryService.getSummaryByMonth(type, value, (contactReportInfoList, i) ->
                        contactReportInfoList.stream()
                                .filter(c -> Objects.nonNull(c.getContactDt()))
                                .filter(d -> d.getContactDt().getMonth().name().equals(i))
                                .collect(Collectors.toList())), null);
    }


    @GetMapping(value = "/summary-current-status/{issueType}")
    public ContactReportResponse summaryByCurrentStatus(@PathVariable("issueType") String issType) {
        List<Map<String, Object>> rList = new ArrayList<>();
        if (!issType.equalsIgnoreCase("all")) {
            rList = contactReportSummaryService.summaryByCurrentStatus(issType);
        } else {
            for (String iss : this.issueType.getIssuesByCategory().keySet()) {
                rList.addAll(contactReportSummaryService.summaryByCurrentStatus(iss));
            }
        }
        return GenericResponseWrapper.contactReportResponseFunction.apply(rList, null);
    }

    @GetMapping(value = "/summary-current-status-dealership-list/{issue}")
    public ContactReportResponse summaryByCurrentStatusDealershipList(@PathVariable("issue") String issue) {
        issue = issue.replaceAll("~", "/");
        return GenericResponseWrapper.contactReportResponseFunction
                .apply(contactReportSummaryService.summaryByCurrentStatusDealershipList(issue), null);
    }


    @GetMapping(value = "/report-execution-coverage/{date}")
    public ContactReportResponse reportExecutionBycoverage(@PathVariable("date") String date) {
        return GenericResponseWrapper.contactReportResponseFunction
                .apply(contactReportSummaryService.reportExecutionCoverageByReportTime(date), null);
    }

    @GetMapping(value = "/report-execution-exception/{date}")
    public ContactReportResponse reportExecutionByException(@PathVariable("date") String date) {
        return GenericResponseWrapper.contactReportResponseFunction
                .apply(contactReportSummaryService.reportExecutionByException(date), null);
    }

    @GetMapping(value = "/v2/summary-current-status")
    public CommonResponse<List<SummaryByContactStatusDto>> summaryByCurrentStatusUsingIssues(
            @RequestParam(required = false) String regionId,
            @RequestParam(required = false) String zoneId,
            @RequestParam(required = false) String districtId,
            @RequestParam(required = false) String dealerId,
            @SessionAttribute(name = "mfpUser") MFPUser mfpUser
    ) {
        try {
            FilterCriteria filterCriteria = FilterCriteriaBuilder.buildFilterByLocationAndIssueAndTiming(regionId, zoneId, districtId, dealerId, null, null, null);
            List<SummaryByContactStatusDto> response = contactReportSummaryService.filterSummaryByCurrentStatusUsingIssues(filterCriteria, mfpUser);
            return AbstractService.httpPostSuccess(response, "Success");
        } catch (Exception e) {
            return AbstractService.httpPostError(e);
        }
    }

    @GetMapping(value = "/v2/summary-current-status/dealership-list/{issue}")
    public CommonResponse<List<SummaryByDealerListDto>> summaryByCurrentStatusOfDealershipList(
            @PathVariable("issue") String issue,
            @RequestParam(required = false) String regionId,
            @RequestParam(required = false) String zoneId,
            @RequestParam(required = false) String districtId,
            @RequestParam(required = false) String dealerId,
            @SessionAttribute(name = "mfpUser") MFPUser mfpUser) {

        try {
            issue = issue.replace("~", "/");
            FilterCriteria filterCriteria = FilterCriteriaBuilder.buildFilterByLocationAndIssueAndTiming(regionId, zoneId, districtId, dealerId, issue, null, null);
            List<SummaryByDealerListDto> response = contactReportSummaryService.summaryByCurrentStatusOfDealershipList(filterCriteria, mfpUser);
            return AbstractService.httpPostSuccess(response, "Success");

        } catch (Exception e) {
            return AbstractService.httpPostError(e);
        }
    }


}
