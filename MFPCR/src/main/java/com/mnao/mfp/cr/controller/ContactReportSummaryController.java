package com.mnao.mfp.cr.controller;

import com.mnao.mfp.common.dto.CommonResponse;
import com.mnao.mfp.common.service.AbstractService;
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
@RequestMapping(value = "/report-summary")
public class ContactReportSummaryController {

    @Autowired
    private ContactReportServiceImpl contactReportService;

    @Autowired
    private ContactReportSummaryService contactReportSummaryService;

    @GetMapping(value = "/dealer-issue")
    public CommonResponse<List<DealersByIssue>> dealerIssue(){
        return AbstractService.httpPostSuccess(contactReportService.getAllDealersByIssue(), "Success");
    }

    @GetMapping(value = "/by-issue/{type}/{category}")
    public CommonResponse<List<AbstractMap.SimpleEntry<String, String>>> summaryByIssue(@PathVariable("type") String type,
                                                                    @PathVariable("category") String category){
        return AbstractService.httpPostSuccess(contactReportSummaryService.getSummaryByLocation(type, category), "Success");
    }
}
