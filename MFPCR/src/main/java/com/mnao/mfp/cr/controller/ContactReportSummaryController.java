package com.mnao.mfp.cr.controller;

import com.mnao.mfp.cr.Service.ContactReportService;
import com.mnao.mfp.cr.Service.ContactReportServiceImpl;
import com.mnao.mfp.cr.Service.ContactReportSummaryService;
import com.mnao.mfp.cr.Service.GenericResponseWrapper;
import com.mnao.mfp.cr.entity.ContactReportDiscussion;
import com.mnao.mfp.cr.entity.ContactReportInfo;
import com.mnao.mfp.cr.entity.Dealers;
import com.mnao.mfp.cr.model.ContactReportResponse;
import com.mnao.mfp.cr.model.DealersByIssue;
import com.mnao.mfp.cr.util.IssueType;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
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

    @GetMapping(value = "/by-month/{type}/{value}")
    public ContactReportResponse summaryByMonth(@PathVariable("type") String type,
                                                @PathVariable("value") String value
                                                ){
        return GenericResponseWrapper.contactReportResponseFunction
                .apply(contactReportSummaryService.getSummaryByMonth(type,value,(contactReportInfoList, i) ->
                        contactReportInfoList.stream()
                                .filter(c -> Objects.nonNull(c.getContactDt()))
                                .filter(d -> d.getContactDt().getMonth().name().equals(i))
                                .collect(Collectors.toList())), null);
    }

    @GetMapping(value = "/summary-current-status/{issueType}")
    public ContactReportResponse summaryByCurrentStatus(@PathVariable("issueType") String issType){
		List<Map<String, Object>> rList = new ArrayList<>();
		if (! issType.equalsIgnoreCase("all")) {
			rList = contactReportSummaryService.summaryByCurrentStatus(issType);
		} else {
			for(String iss: this.issueType.getIssuesByCategory().keySet()) {
				rList.addAll(contactReportSummaryService.summaryByCurrentStatus(iss));
			}
		}
		return GenericResponseWrapper.contactReportResponseFunction.apply(rList, null);
    }

    @GetMapping(value = "/summary-current-status-dealership-list/{issue}")
    public ContactReportResponse summaryByCurrentStatusDealershipList(@PathVariable("issue") String issue){
    	issue = issue.replaceAll("~", "/");
        return GenericResponseWrapper.contactReportResponseFunction
                .apply(contactReportSummaryService.summaryByCurrentStatusDealershipList(issue), null);
    }


    @GetMapping(value = "/report-execution-coverage/{date}")
    public ContactReportResponse reportExecutionBycoverage(@PathVariable("date") String date){
        return GenericResponseWrapper.contactReportResponseFunction
                .apply(contactReportSummaryService.reportExecutionBycoverage(date), null);
    }

    @GetMapping(value = "/report-execution-exception/{date}")
    public ContactReportResponse reportExecutionByException(@PathVariable("date") String date){
        return GenericResponseWrapper.contactReportResponseFunction
                .apply(contactReportSummaryService.reportExecutionByException(date), null);
    }



}
