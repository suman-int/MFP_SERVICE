package com.mnao.mfp.cr.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiPredicate;
import java.util.function.Predicate;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import com.mnao.mfp.cr.entity.ContactReportInfo;
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

	private BiPredicate<ContactReportInfo, FilterCriteria> regionFilter = (c, f) ->{
		if(Objects.nonNull(f.getRgnCd())){
			return c.getDealers().getRgnCd().equals(f.getRgnCd());
		}
		return true;
	};
	private BiPredicate<ContactReportInfo, FilterCriteria> zoneFilter = (c, f) ->{
		if(Objects.nonNull(f.getZoneCd())){
			return c.getDealers().getZoneCd().equals(f.getZoneCd());
		}
		return true;
	};
	private BiPredicate<ContactReportInfo, FilterCriteria> districtFilter = (c, f) ->{
		if(Objects.nonNull(f.getDistrictCd())){
			return c.getDealers().getDistrictCd().equals(f.getDistrictCd());
		}
		return true;
	};
	private BiPredicate<ContactReportInfo, FilterCriteria> dealerFilter = (c, f) ->{
		if(Objects.nonNull(f.getDlrCd())){
			return c.getDealers().getDlrCd().equals(f.getDlrCd());
		}
		return true;
	};


	public List<ContactReportInfo> filterContactReportsBasedOnFilter(FilterCriteria filterCriteria){
		List<ContactReportInfo> contactReportInfos = contactInfoRepository.findAll();
		List<ContactReportInfo> regionContactReportInfos = contactReportInfos.stream()
				.filter(c-> regionFilter.test(c, filterCriteria)).collect(Collectors.toList());
		List<ContactReportInfo> zoneContactReportInfos = regionContactReportInfos.stream()
				.filter(c-> zoneFilter.test(c, filterCriteria)).collect(Collectors.toList());
		List<ContactReportInfo> districtContactReportInfos = zoneContactReportInfos.stream()
				.filter(c-> districtFilter.test(c, filterCriteria)).collect(Collectors.toList());
		List<ContactReportInfo> dealerContactReportInfos = districtContactReportInfos.stream()
				.filter(c-> dealerFilter.test(c, filterCriteria)).collect(Collectors.toList());
		return dealerContactReportInfos;
	}

	@Override
	public CommonResponse<ReportByDealerShipResponse> byDealership(FilterCriteria filterCriteria) {
		try {
			ReportByDealerShipResponse byDealerShipResponse = new ReportByDealerShipResponse();
			List<ReportByDealershipDto> data = contactInfoRepository.findCurrentIssuesByDlrCd(filterCriteria.getDlrCd());
			if (!filterCriteria.getIssuesFilter().isEmpty()) {
				data = data.stream().filter(value -> {
					return filterCriteria.getIssuesFilter().stream().anyMatch(filterQuery -> value.getCurrentIssues().contains(filterQuery));
				}).collect(Collectors.toList());
			} 
			
			Map<Integer, List<ReportByDealershipDto>> groupByStatus = data.stream()
					.collect(Collectors.groupingBy(dealer -> dealer.getContactStatus()));
			byDealerShipResponse.setDraft(groupByStatus.getOrDefault(ContactReportEnum.DRAFT.getStatusCode(),
					new ArrayList<ReportByDealershipDto>()));
			byDealerShipResponse.setDiscussionRequested(groupByStatus.getOrDefault(
					ContactReportEnum.DISCUSSION_REQUESTED.getStatusCode(), new ArrayList<ReportByDealershipDto>()));
			byDealerShipResponse.setReviewed(groupByStatus.getOrDefault(ContactReportEnum.REVIEWED.getStatusCode(),
					new ArrayList<ReportByDealershipDto>()));
			byDealerShipResponse.setSubmitted(groupByStatus.getOrDefault(ContactReportEnum.SUBMITTED.getStatusCode(),
					new ArrayList<ReportByDealershipDto>()));
			return AbstractService.httpPostSuccess(byDealerShipResponse, "Success");
		} catch (Exception exp) {
			return AbstractService.httpPostError(exp);	
		}
	}

}
