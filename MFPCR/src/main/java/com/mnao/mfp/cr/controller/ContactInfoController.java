package com.mnao.mfp.cr.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.mnao.mfp.common.dto.CommonResponse;

import com.mnao.mfp.cr.service.ContactInfoService;
import com.mnao.mfp.cr.service.ReportByIssuesService;
import com.mnao.mfp.cr.service.ReportByMonthService;
import com.mnao.mfp.cr.dto.*;

import java.util.List;


@RestController
@RequestMapping(value="ContactReportInfo")
public class ContactInfoController {


    @Autowired
    ReportByIssuesService reportByIssuesService;

    @Autowired
    ReportByMonthService reportByMonthService;
    
    @Autowired
    private ContactInfoService contactInfoService;

    @PostMapping(value = "byIssues")
    public List<ReportByIssuesDto> byIssues(@RequestBody FilterCriteria filterCriteria) {
        return reportByIssuesService.findReportByIssues(filterCriteria);
    }
   
    @PostMapping(value = "byDealership")
    public CommonResponse<ReportByDealerShipResponse> byDealership(@RequestBody FilterCriteria filterCriteria) {
    	return contactInfoService.byDealership(filterCriteria);
    }

    @PostMapping(value = "byMonth")
    public List<ReportByMonthDto> byMonth(@RequestBody FilterCriteria filterCriteria) {
        return reportByMonthService.findReportByMonth(filterCriteria);
    }
}
