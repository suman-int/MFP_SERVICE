package com.mnao.mfp.cr.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mnao.mfp.common.dto.CommonResponse;
import com.mnao.mfp.common.service.AbstractService;
import com.mnao.mfp.cr.dto.FilterCriteria;
import com.mnao.mfp.cr.dto.ReportByDealerShipResponse;
import com.mnao.mfp.cr.dto.ReportByDealershipDto;
import com.mnao.mfp.cr.repository.ContactInfoRepository;
import com.mnao.mfp.cr.util.ContactReportEnum;

@Service
public class ContactInfoServiceImpl implements ContactInfoService {
	
	@Autowired
    ContactInfoRepository contactInfoRepository;

	@Override
	public CommonResponse<ReportByDealerShipResponse> byDealership(FilterCriteria filterCriteria) {
		try {
			ReportByDealerShipResponse byDealerShipResponse = new ReportByDealerShipResponse();
			List<ReportByDealershipDto> data = contactInfoRepository.findCurrentIssuesByDlrCd(filterCriteria.getDlrCd());
			if (!filterCriteria.getIssuesFilter().isEmpty()) {
				data = data.stream().filter(value -> filterCriteria.getIssuesFilter().stream().anyMatch(filterQuery -> value.getCurrentIssues().contains(filterQuery))).collect(Collectors.toList());
			} 
			
			Map<Integer, List<ReportByDealershipDto>> groupByStatus = data.stream()
					.collect(Collectors.groupingBy(ReportByDealershipDto::getContactStatus));
			byDealerShipResponse.setDraft(groupByStatus.getOrDefault(ContactReportEnum.DRAFT.getStatusCode(),
					new ArrayList<>()));
			byDealerShipResponse.setDiscussionRequested(groupByStatus.getOrDefault(
					ContactReportEnum.DISCUSSION_REQUESTED.getStatusCode(), new ArrayList<ReportByDealershipDto>()));
			byDealerShipResponse.setReviewed(groupByStatus.getOrDefault(ContactReportEnum.REVIEWED.getStatusCode(),
					new ArrayList<>()));
			byDealerShipResponse.setSubmitted(groupByStatus.getOrDefault(ContactReportEnum.SUBMITTED.getStatusCode(),
					new ArrayList<>()));
			return AbstractService.httpPostSuccess(byDealerShipResponse, "Success");
		} catch (Exception exp) {
			return AbstractService.httpPostError(exp);	
		}
	}

}
