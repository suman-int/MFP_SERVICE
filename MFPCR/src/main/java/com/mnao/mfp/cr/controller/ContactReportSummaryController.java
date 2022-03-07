package com.mnao.mfp.cr.controller;

import com.mnao.mfp.cr.Service.ContactReportServiceImpl;
import com.mnao.mfp.cr.Service.ContactReportSummaryService;
import com.mnao.mfp.cr.model.DealersByIssue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.AbstractMap;
import java.util.List;

@RestController
@RequestMapping(value = "report-summary")
public class ContactReportSummaryController {

    @Autowired
    private ContactReportServiceImpl contactReportService;

    @Autowired
    private ContactReportSummaryService contactReportSummaryService;

    @GetMapping(value = "dealer-issue")
    public List<DealersByIssue> dealerIssue(){
        return contactReportService.getAllDealersByIssue();
    }

    @GetMapping(value = "by-issue/{type}/{category}")
    public List<AbstractMap.SimpleEntry<String, String>> summaryByIssue(@PathVariable("type") String type,
                                                                        @PathVariable("category") String category){
        return contactReportSummaryService.getSummaryByLocation(type, category);
    }
}
