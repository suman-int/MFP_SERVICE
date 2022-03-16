package com.mnao.mfp.cr.controller;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.mnao.mfp.common.dto.CommonResponse;
import com.mnao.mfp.common.service.AbstractService;
import com.mnao.mfp.cr.Service.ReportByIssuesService;
import com.mnao.mfp.cr.Service.ReportByMonthService;
import com.mnao.mfp.cr.dto.*;
import com.mnao.mfp.cr.repository.ContactInfoRepository;
import com.mnao.mfp.cr.util.ContactReportEnum;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@RestController
@RequestMapping(path="/ContactReportInfo")
public class ContactInfoController {

    @Autowired
    ContactInfoRepository contactInfoRepository;

    @Autowired
    ReportByIssuesService reportByIssuesService;

    @Autowired
    ReportByMonthService reportByMonthService;

    @PostMapping(value = "/byIssues")
    public List<ReportByIssuesDto> byIssues(@RequestBody FilterCriteria filterCriteria) {
        return reportByIssuesService.findReportByIssues(filterCriteria);
    }
   
    @PostMapping(value = "/byDealership")
    public CommonResponse<ReportByDealerShipResponse> byDealership(@RequestBody FilterCriteria filterCriteria) {
    	 ReportByDealerShipResponse byDealerShipResponse = new ReportByDealerShipResponse();
         List<ReportByDealershipDto> data = contactInfoRepository.findByDlrCd(filterCriteria.getDlrCd(), filterCriteria.getIssuesFilter());
         Map<Integer, List<ReportByDealershipDto>> groupByStatus = data.stream().collect(Collectors.groupingBy(dealer -> dealer.getContactStatus()));
         byDealerShipResponse.setDraft(groupByStatus.getOrDefault(ContactReportEnum.DRAFT.getStatusCode(), new ArrayList<ReportByDealershipDto>()));
         byDealerShipResponse.setDiscussionRequested(groupByStatus.getOrDefault(ContactReportEnum.DISCUSSION_REQUESTED.getStatusCode(), new ArrayList<ReportByDealershipDto>()));
         byDealerShipResponse.setReviewed(groupByStatus.getOrDefault(ContactReportEnum.REVIEWED.getStatusCode(), new ArrayList<ReportByDealershipDto>()));
         byDealerShipResponse.setSubmitted(groupByStatus.getOrDefault(ContactReportEnum.SUBMITTED.getStatusCode(), new ArrayList<ReportByDealershipDto>()));
         return AbstractService.httpPostSuccess(byDealerShipResponse, "Success");
    }

    @PostMapping(value = "/byMonth")
    public List<ReportByMonthDto> byMonth(@RequestBody FilterCriteria filterCriteria) {
        return reportByMonthService.findReportByMonth(filterCriteria);
    }



}
