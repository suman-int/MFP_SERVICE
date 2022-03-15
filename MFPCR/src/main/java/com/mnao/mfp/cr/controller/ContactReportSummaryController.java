package com.mnao.mfp.cr.controller;

import com.mnao.mfp.cr.Service.ContactReportServiceImpl;
import com.mnao.mfp.cr.Service.ContactReportSummaryService;
import com.mnao.mfp.cr.Service.GenericResponseWrapper;
import com.mnao.mfp.cr.entity.ContactReportDiscussion;
import com.mnao.mfp.cr.model.ContactReportResponse;
import com.mnao.mfp.cr.model.DealersByIssue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping(value = "report-summary")
public class ContactReportSummaryController {

    @Autowired
    private ContactReportServiceImpl contactReportService;

    @Autowired
    private ContactReportSummaryService contactReportSummaryService;

    @GetMapping(value = "dealer-issue")
    public List<DealersByIssue> dealerIssue() {
        return contactReportService.getAllDealersByIssue();
    }

    @GetMapping(value = "by-issue/{type}/{value}/{category}")
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

    @GetMapping(value = "by-month/{type}/{value}")
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
}
