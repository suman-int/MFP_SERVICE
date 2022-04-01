package com.mnao.mfp.cr.controller;

import com.mnao.mfp.common.datafilters.FilterCriteria;
import com.mnao.mfp.common.dto.CommonResponse;
import com.mnao.mfp.cr.dto.ReportByDealerShipResponse;
import com.mnao.mfp.cr.dto.ReportByIssuesDto;
import com.mnao.mfp.cr.dto.ReportByMonthDto;
import com.mnao.mfp.cr.service.ContactInfoService;
import com.mnao.mfp.cr.service.ReportByIssuesService;
import com.mnao.mfp.cr.service.ReportByMonthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;


@RestController
@RequestMapping(value = "ContactReportInfo")
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
