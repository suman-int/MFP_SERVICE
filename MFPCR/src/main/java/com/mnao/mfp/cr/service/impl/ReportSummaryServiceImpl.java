package com.mnao.mfp.cr.service.impl;

import static com.mnao.mfp.common.util.AppConstants.MONTHS_LIST;
import static com.mnao.mfp.common.util.Utils.isNotNullOrEmpty;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import com.mnao.mfp.common.datafilters.FilterCriteria;
import com.mnao.mfp.common.util.AppConstants;
import com.mnao.mfp.common.util.IsActiveEnum;
import com.mnao.mfp.common.util.NullCheck;
import com.mnao.mfp.cr.entity.ContactReportInfo;
import com.mnao.mfp.cr.entity.Dealers;
import com.mnao.mfp.cr.repository.ContactInfoRepository;
import com.mnao.mfp.cr.service.ReportSummaryService;
import com.mnao.mfp.cr.util.ContactReportEnum;
import com.mnao.mfp.cr.util.DataOperationFilter;
import com.mnao.mfp.cr.util.LocationEnum;
import com.mnao.mfp.user.dao.MFPUser;

@Service
public class ReportSummaryServiceImpl implements ReportSummaryService {
	private Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private ContactInfoRepository contactInfoRepository;

	@Autowired
	private DataOperationFilter dataOperationFilter;

	@Override
	public List<Map<String, String>> getSummaryByLocation(FilterCriteria filter, MFPUser mfpUser) {
		List<Map<String, String>> finalListData = new ArrayList<>();
		List<ContactReportInfo> contactReports = contactInfoRepository
				.findByCurrentIssuesNotNullAndIsActiveAndContactDtBetween(IsActiveEnum.YES.getValue(),
						filter.getStartDate(), filter.getEndDate());
		long[] totals = new long[2];
		if (!CollectionUtils.isEmpty(filter.getIssuesFilter())) {
			contactReports = dataOperationFilter.filterContactReportsByIssues(filter, contactReports);
		}
		if (new NullCheck<>(filter).with(FilterCriteria::getStartDate).isNotNullOrEmpty()
				&& (new NullCheck<>(filter).with(FilterCriteria::getEndDate).isNotNullOrEmpty())) {
			contactReports = dataOperationFilter.filterContactReportsByDateRange(filter, contactReports);
		}
		Map<String, Map<String, List<ContactReportInfo>>> reports;
		reports = dataOperationFilter.filterContactReportsByLocationAndGroupingByDealer(filter, contactReports,
				mfpUser);

		// MW -> Other => [], Isssue => []
		reports.forEach((dealerData, dealerSpecificMap) -> {
			HashMap<String, List<ContactReportInfo>> finalData = new HashMap<>();
			HashMap<String, String> responseData = new HashMap<>();
			Set<String> distinctIssues = new HashSet<>();
			dealerSpecificMap.forEach(
					(issues, crList) -> Arrays.asList(issues.split("\\|")).forEach(data -> distinctIssues.add(data)));
			distinctIssues.forEach(issue -> {
				List<ContactReportInfo> crInfoList = new ArrayList<>();
				dealerSpecificMap.forEach((issues, crList) -> {
					if (Arrays.asList(issues.split("\\|")).contains(issue)) {
						crInfoList.addAll(crList);
					}
				});
				logger.info("{} => {}", issue, crInfoList.size());
				finalData.put(issue, crInfoList);
			});
			logger.info("FInal Data => {}", finalData.size());
			finalData.forEach((distinctIssue, crList) -> {
				logger.info("FInal Data Loop => {} => {}", distinctIssue, crList.size());
				if (filter.getIssuesFilter().contains(distinctIssue)) {
					long submittedCount = crList.stream()
							.filter(report -> report.getContactStatus() == ContactReportEnum.SUBMITTED.getStatusCode())
							.count();
					long discussionReqCount = crList
							.stream().filter(report -> report
									.getContactStatus() == ContactReportEnum.DISCUSSION_REQUESTED.getStatusCode())
							.count();
					long pendingReviewCount = discussionReqCount + submittedCount;
					long reviewedCount = crList.stream()
							.filter(report -> report.getContactStatus() == ContactReportEnum.REVIEWED.getStatusCode())
							.count();
					if (pendingReviewCount > 0 || reviewedCount > 0) {
						totals[0] += pendingReviewCount;
						totals[1] += reviewedCount;
						responseData.put(distinctIssue,
								String.format(AppConstants.DOUBLE_INT_PERCENT, pendingReviewCount, reviewedCount));
					}

				}
			});
			if (!CollectionUtils.isEmpty(responseData)) {
				responseData.put(getStringByType(filter.forLocation().name()), dealerData);
				responseData.put("TOTAL", String.format(AppConstants.DOUBLE_INT_PERCENT, totals[0], totals[1]));
				totals[0] = 0;
				totals[1] = 0;
				finalListData.add(responseData);
			}

		});

		return finalListData;
	}

	@Override
	public List<Map<String, String>> getSummaryOfMonthByLocation(FilterCriteria filter, MFPUser mfpUser) {
		List<Map<String, String>> finalListData = new ArrayList<>();
		List<ContactReportInfo> contactReports = contactInfoRepository
				.findByCurrentIssuesNotNullAndContactDtNotNullAndIsActiveAndContactDtBetween(
						IsActiveEnum.YES.getValue(), filter.getStartDate(), filter.getEndDate());
		contactReports = contactReports.stream()
				.filter(cr -> cr.getContactStatus() != ContactReportEnum.DRAFT.getStatusCode())
				.collect(Collectors.toList());
		long[] totals = new long[2];
		if (!CollectionUtils.isEmpty(filter.getIssuesFilter())) {
			contactReports = dataOperationFilter.filterContactReportsByIssues(filter, contactReports);
		}
		if (isNotNullOrEmpty(filter.getStartDate()) && isNotNullOrEmpty(filter.getEndDate())) {
			contactReports = dataOperationFilter.filterContactReportsByDateRange(filter, contactReports);
		}
		contactReports.forEach(value -> logger.info("{} > {}", value.getContactReportId(), value.getContactDt()));
		Map<String, Map<Object, List<ContactReportInfo>>> reports;
		if (filter.forLocation() == LocationEnum.DISTRICT) {
			reports = dataOperationFilter.filterContactReportByDistrict(contactReports, filter, mfpUser)
					.collect(Collectors.groupingBy(group -> {
						Dealers dealer = group.getDealers();
						return String.format(AppConstants.DOUBLE_STRING_FORMAT, dealer.getDlrCd().trim(),
								dealer.getDbaNm());
					}, Collectors.groupingBy(gr -> gr.getContactDt().format(DateTimeFormatter.ofPattern("MMM")))));
		} else if (filter.forLocation() == LocationEnum.ZONE) {
			reports = dataOperationFilter.filterContactReportByZone(contactReports, filter, mfpUser)
					.collect(Collectors.groupingBy(group -> group.getDealers().getDistrictCd(),
							Collectors.groupingBy(gr -> gr.getContactDt().format(DateTimeFormatter.ofPattern("MMM")))));
		} else if (filter.forLocation() == LocationEnum.DEALER) {
			reports = contactReports.stream().collect(Collectors.groupingBy(group -> group.getDealers().getDlrCd(),
					Collectors.groupingBy(gr -> gr.getContactDt().format(DateTimeFormatter.ofPattern("MMM")))));
		} else if (filter.forLocation() == LocationEnum.REGION) {
			reports = dataOperationFilter.filterContactReportByRegion(contactReports, filter.getRgnCd(), mfpUser)
					.collect(Collectors.groupingBy(group -> group.getDealers().getZoneCd(),
							Collectors.groupingBy(gr -> gr.getContactDt().format(DateTimeFormatter.ofPattern("MMM")))));
		} else {
			reports = dataOperationFilter.getFilteredRegionByUser(mfpUser, contactReports).stream()
					.collect(Collectors.groupingBy(group -> group.getDealers().getRgnCd(),
							Collectors.groupingBy(gr -> gr.getContactDt().format(DateTimeFormatter.ofPattern("MMM")))));
		}

		reports.forEach((key1, value1) -> {
			HashMap<String, List<ContactReportInfo>> finalData = new HashMap<>();
			HashMap<String, String> responseData = new HashMap<>();
			value1.forEach((key, value) -> finalData.put((String) key, value));
			finalData.forEach((key, value) -> {
				long submittedCount = value.stream()
						.filter(report -> report.getContactStatus() == ContactReportEnum.SUBMITTED.getStatusCode())
						.count();
				long discussionReqCount = value.stream().filter(
						report -> report.getContactStatus() == ContactReportEnum.DISCUSSION_REQUESTED.getStatusCode())
						.count();
				long pendingReqvieCount = discussionReqCount + submittedCount;
				long reviewedCount = value.stream()
						.filter(report -> report.getContactStatus() == ContactReportEnum.REVIEWED.getStatusCode())
						.count();
				responseData.put(key,
						String.format(AppConstants.DOUBLE_INT_PERCENT, pendingReqvieCount, reviewedCount));
				totals[0] += pendingReqvieCount;
				totals[1] += reviewedCount;
			});
			if (!CollectionUtils.isEmpty(responseData)) {
				MONTHS_LIST.forEach(value -> {
					if (!responseData.containsKey(value)) {
						responseData.put(value, "0/0");
					}
				});
				responseData.put(getStringByType(filter.forLocation().name()), key1);
				responseData.put("TOTAL", String.format(AppConstants.DOUBLE_INT_PERCENT, totals[0], totals[1]));
				totals[0] = 0;
				totals[1] = 0;
				finalListData.add(responseData);
			}
		});

		return finalListData;
	}

	private String getStringByType(String type) {
		if (type.equalsIgnoreCase(LocationEnum.DISTRICT.name())) {
			return LocationEnum.DEALER.name();
		} else if (type.equalsIgnoreCase(LocationEnum.DEALER.name())) {
			return LocationEnum.DEALER.name();
		} else if (type.equalsIgnoreCase(LocationEnum.ZONE.name())) {
			return LocationEnum.DISTRICT.name();
		} else if (type.equalsIgnoreCase(LocationEnum.REGION.name())) {
			return LocationEnum.ZONE.name();
		}
		return LocationEnum.REGION.name();
	}

}
