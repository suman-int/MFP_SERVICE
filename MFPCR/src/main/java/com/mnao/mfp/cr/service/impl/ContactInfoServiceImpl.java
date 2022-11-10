package com.mnao.mfp.cr.service.impl;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import com.mnao.mfp.common.datafilters.FilterCriteria;
import com.mnao.mfp.common.dto.CommonResponse;
import com.mnao.mfp.common.service.AbstractService;
import com.mnao.mfp.cr.dto.RegionZoneReviewer;
import com.mnao.mfp.cr.dto.ReportByDealerShipResponse;
import com.mnao.mfp.cr.dto.ReportByDealershipDto;
import com.mnao.mfp.cr.repository.ContactInfoRepository;
import com.mnao.mfp.cr.service.ContactInfoService;
import com.mnao.mfp.cr.util.ContactReportEnum;
import com.mnao.mfp.cr.util.DataOperationFilter;
import com.mnao.mfp.list.service.ListEmployeeDataService;
import com.mnao.mfp.user.dao.MFPUser;

@Service
public class ContactInfoServiceImpl implements ContactInfoService {
	//
	private static final Logger log = LoggerFactory.getLogger(ContactReportServiceImpl.class);
	//

	@Autowired
	ContactInfoRepository contactInfoRepository;

	@Autowired
	private DataOperationFilter dataOperationFilter;

	@Autowired
	private ListEmployeeDataService employeeDataService;

	@Override
	public CommonResponse<ReportByDealerShipResponse> byDealership(FilterCriteria filterCriteria) {
		try {
			ReportByDealerShipResponse byDealerShipResponse = new ReportByDealerShipResponse();
			List<ReportByDealershipDto> data = contactInfoRepository
					.findCurrentIssuesByDlrCd(filterCriteria.getDlrCd());
			if (!filterCriteria.getIssuesFilter().isEmpty()) {
				data = data.stream()
						.filter(value -> filterCriteria.getIssuesFilter().stream()
								.anyMatch(filterQuery -> value.getCurrentIssues().contains(filterQuery)))
						.collect(Collectors.toList());
			}

			Map<Integer, List<ReportByDealershipDto>> groupByStatus = data.stream()
					.collect(Collectors.groupingBy(ReportByDealershipDto::getContactStatus));
			byDealerShipResponse
					.setDraft(groupByStatus.getOrDefault(ContactReportEnum.DRAFT.getStatusCode(), new ArrayList<>()));
			byDealerShipResponse.setDiscussionRequested(groupByStatus
					.getOrDefault(ContactReportEnum.DISCUSSION_REQUESTED.getStatusCode(), new ArrayList<>()));
			byDealerShipResponse.setReviewed(
					groupByStatus.getOrDefault(ContactReportEnum.REVIEWED.getStatusCode(), new ArrayList<>()));
			byDealerShipResponse.setSubmitted(
					groupByStatus.getOrDefault(ContactReportEnum.SUBMITTED.getStatusCode(), new ArrayList<>()));
			return AbstractService.httpPostSuccess(byDealerShipResponse, "Success");
		} catch (Exception exp) {
			return AbstractService.httpPostError(exp);
		}
	}

	@Override
	public ReportByDealerShipResponse byDealershipByIssues(MFPUser mfpUser, FilterCriteria filterCriteria) {
		ReportByDealerShipResponse byDealerShipResponse = new ReportByDealerShipResponse();
		Instant start = Instant.now();
		Map<String, RegionZoneReviewer> rzReviewer = employeeDataService.loadAllReviewer(mfpUser);
		Instant end = Instant.now();
		Duration timeElapsed = Duration.between(start, end);
		log.info("Reviewers loaded in " + timeElapsed.toMillis() + " ms.");
		List<ReportByDealershipDto> data = contactInfoRepository.findCurrentIssuesByDlrCd(filterCriteria.getDlrCd());
		if (!CollectionUtils.isEmpty(filterCriteria.getIssuesFilter())) {
			data = data.stream().filter(dealer -> Objects.nonNull(dealer.getCurrentIssues()))
					.filter(value -> filterCriteria.getIssuesFilter().stream()
							.anyMatch(filterQuery -> value.getCurrentIssues().contains(filterQuery)))
					.collect(Collectors.toList());
		}
		data.forEach(reportInfo -> validateAndForceDraft(reportInfo, mfpUser, rzReviewer));
		Map<Integer, List<ReportByDealershipDto>> groupByStatus = data.stream()
				.collect(Collectors.groupingBy(ReportByDealershipDto::getContactStatus));
		byDealerShipResponse
				.setDraft(groupByStatus.getOrDefault(ContactReportEnum.DRAFT.getStatusCode(), new ArrayList<>()));
		byDealerShipResponse.setDiscussionRequested(
				groupByStatus.getOrDefault(ContactReportEnum.DISCUSSION_REQUESTED.getStatusCode(), new ArrayList<>()));
		byDealerShipResponse
				.setReviewed(groupByStatus.getOrDefault(ContactReportEnum.REVIEWED.getStatusCode(), new ArrayList<>()));
		byDealerShipResponse.setSubmitted(
				groupByStatus.getOrDefault(ContactReportEnum.SUBMITTED.getStatusCode(), new ArrayList<>()));
		return byDealerShipResponse;
	}

	private boolean validateAndForceDraft(ReportByDealershipDto reportInfo, MFPUser mfpUser,
			Map<String, RegionZoneReviewer> rzReviewer) {
		boolean matched = true;
		matched = employeeDataService.validateReviewer(mfpUser, rzReviewer, reportInfo.getContactReportId(),
				reportInfo.getContactStatus(), reportInfo.getContactAuthor(), reportInfo.getContactReviewer(),
				reportInfo.getDlrCd(), reportInfo.getRgnCd(), reportInfo.getZoneCd());
		if (!matched) {
			reportInfo.setForcedDraft(true);
			reportInfo.setContactStatus(ContactReportEnum.DRAFT.getStatusCode());
			reportInfo.setContactReviewer(null);
		}
		return reportInfo.isForcedDraft();
	}

}
