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
import com.mnao.mfp.cr.util.IssueType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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
                                                @PathVariable("category") String category) {
        return GenericResponseWrapper.contactReportResponseFunction
                .apply(contactReportSummaryService
                        .getSummaryByLocation(type, value, category, (contactReportInfoList, issue, status, predicate) ->
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

    @GetMapping(value = "/v2/by-issue/all/{category}")
    public CommonResponse<List<Map<String, String>>> summaryByIssueForRegion(@PathVariable("category") String category, @RequestParam(required = false) String issues) {
        try {
        	List<String> issuesList = new ArrayList<>();
        	if (issues != null) {
        		issuesList = Arrays.asList(issues.split(","));
        	}
            List<Map<String, String>> response = contactReportSummaryService.getSummaryByLocation(FilterCriteria.builder().issuesFilter(issuesList).build(), category);
            return AbstractService.httpPostSuccess(response, "Success");
        } catch (Exception e) {
            return AbstractService.httpPostError(e);
        }
    }

    @GetMapping(value = "/v2/by-issue/region/{value}/{category}")
    public CommonResponse<List<Map<String, String>>> summaryByIssueForRegion(@PathVariable("value") String value,
                                                                             @PathVariable("category") String category,
                                                                             @RequestParam(required = false) String issues) {
        try {
        	List<String> issuesList = new ArrayList<>();
        	if (issues != null) {
        		issuesList = Arrays.asList(issues.split(","));
        	}
            List<Map<String, String>> response = contactReportSummaryService.getSummaryByLocation(FilterCriteria.builder()
            		.rgnCd(value).issuesFilter(issuesList).build(), category);
            return AbstractService.httpPostSuccess(response, "Success");
        } catch (Exception e) {
            return AbstractService.httpPostError(e);
        }
    }

    @GetMapping(value = "/v2/by-issue/region/{value}/zone/{zoneValue}/{category}")
    public CommonResponse<List<Map<String, String>>> summaryByIssueForZone(
            @PathVariable("value") String value,
            @PathVariable("zoneValue") String zoneValue,
            @PathVariable("category") String category,
            @RequestParam(required = false) String issues
     ) {
        try {
        	List<String> issuesList = new ArrayList<>();
        	if (issues != null) {
        		issuesList = Arrays.asList(issues.split(","));
        	}
            List<Map<String, String>> response = contactReportSummaryService.getSummaryByLocation(FilterCriteria.builder()
                    .rgnCd(value).zoneCd(zoneValue).issuesFilter(issuesList).build(), category);
            return AbstractService.httpPostSuccess(response, "Success");
        } catch (Exception e) {
            return AbstractService.httpPostError(e);
        }
    }

    @GetMapping(value = "/v2/by-issue/region/{value}/zone/{zoneValue}/district/{districtId}/{category}")
    public CommonResponse<List<Map<String, String>>> summaryByIssueForDistrict(
            @PathVariable("value") String value,
            @PathVariable("zoneValue") String zoneValue,
            @PathVariable("districtId") String districtId,
            @PathVariable("category") String category,
            @RequestParam(required = false) String issues
    ) {
        try {
        	List<String> issuesList = new ArrayList<>();
        	if (issues != null) {
        		issuesList = Arrays.asList(issues.split(","));
        	}
            List<Map<String, String>> response = contactReportSummaryService.getSummaryByLocation(FilterCriteria.builder()
                    .rgnCd(value).zoneCd(zoneValue).districtCd(districtId).issuesFilter(issuesList)
                    .build(), category);
            return AbstractService.httpPostSuccess(response, "Success");
        } catch (Exception e) {
            return AbstractService.httpPostError(e);
        }
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

    @GetMapping(value = "/v2/by-month/all")
    public CommonResponse<List<Map<String, String>>> summaryByMonthForAll(
    		@RequestParam(required = false) String issues
    ) {
        try {
        	List<String> issuesList = new ArrayList<>();
        	if (issues != null) {
        		issuesList = Arrays.asList(issues.split(","));
        	}
            List<Map<String, String>> response = contactReportSummaryService.getSummaryOfMonthByLocation(FilterCriteria.builder().issuesFilter(issuesList).build(), null);
            return AbstractService.httpPostSuccess(response, "Success");
        } catch (Exception e) {
            e.printStackTrace();
            return AbstractService.httpPostError(e);
        }
    }

    @GetMapping(value = "/v2/by-month/region/{value}")
    public CommonResponse<List<Map<String, String>>> summaryByMonthForRegion(
    		@PathVariable("value") String value,
    		@RequestParam(required = false) String issues
    ) {
        try {
        	List<String> issuesList = new ArrayList<>();
        	if (issues != null) {
        		issuesList = Arrays.asList(issues.split(","));
        	}
            List<Map<String, String>> response = contactReportSummaryService.getSummaryOfMonthByLocation(
            		FilterCriteria.builder().rgnCd(value).issuesFilter(issuesList)
            		.build(),
            		null);
            return AbstractService.httpPostSuccess(response, "Success");
        } catch (Exception e) {
            return AbstractService.httpPostError(e);
        }
    }

    @GetMapping(value = "/v2/by-month/region/{value}/zone/{zoneValue}")
    public CommonResponse<List<Map<String, String>>> summaryByMonthForZone(
            @PathVariable("value") String value,
            @PathVariable("zoneValue") String zoneValue,
            @RequestParam(required = false) String issues
    ) {
        try {
        	List<String> issuesList = new ArrayList<>();
        	if (issues != null) {
        		issuesList = Arrays.asList(issues.split(","));
        	}
            List<Map<String, String>> response = contactReportSummaryService.getSummaryOfMonthByLocation(FilterCriteria.builder()
                    .rgnCd(value).zoneCd(zoneValue).issuesFilter(issuesList).build(), null);
            return AbstractService.httpPostSuccess(response, "Success");
        } catch (Exception e) {
            return AbstractService.httpPostError(e);
        }
    }

    @GetMapping(value = "/v2/by-month/region/{value}/zone/{zoneValue}/district/{districtId}")
    public CommonResponse<List<Map<String, String>>> summaryByMonthForDistrict(
            @PathVariable("value") String value,
            @PathVariable("zoneValue") String zoneValue,
            @PathVariable("districtId") String districtId,
            @RequestParam(required = false) String issues
    ) {
        try {
        	List<String> issuesList = new ArrayList<>();
        	if (issues != null) {
        		issuesList = Arrays.asList(issues.split(","));
        	}
            List<Map<String, String>> response = contactReportSummaryService.getSummaryOfMonthByLocation(FilterCriteria.builder()
                    .rgnCd(value).zoneCd(zoneValue).districtCd(districtId).issuesFilter(issuesList)
                    .build(), null);
            return AbstractService.httpPostSuccess(response, "Success");
        } catch (Exception e) {
            return AbstractService.httpPostError(e);
        }
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
    public CommonResponse<List<SummaryByContactStatusDto>> summaryByCurrentStatusUsingIssues( @RequestParam(required = false) String issues) {
    	 try {
         	List<String> issuesList = new ArrayList<>();
         	if (issues != null) {
         		issuesList = Arrays.asList(issues.split(","));
         	}
         	List<SummaryByContactStatusDto> response = contactReportSummaryService.filterSummaryByCurrentStatusUsingIssues(FilterCriteria.builder().issuesFilter(issuesList).build());
             return AbstractService.httpPostSuccess(response, "Success");
         } catch (Exception e) {
             return AbstractService.httpPostError(e);
         }
    }

    @GetMapping(value = "/v2/summary-current-status/dealership-list/{issue}")
    public CommonResponse<List<SummaryByDealerListDto>> summaryByCurrentStatusOfDealershipList(@PathVariable("issue") String issue) {
        issue = issue.replaceAll("~", "/");
        try {
        	List<SummaryByDealerListDto> response = contactReportSummaryService.summaryByCurrentStatusOfDealershipList(issue);
        	 return AbstractService.httpPostSuccess(response, "Success");
        	
        } catch (Exception e) {
            return AbstractService.httpPostError(e);
        }
    }


}
