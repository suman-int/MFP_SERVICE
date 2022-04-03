package com.mnao.mfp.cr.service;

import com.mnao.mfp.common.datafilters.FilterCriteria;
import com.mnao.mfp.common.util.AppConstants;
import com.mnao.mfp.cr.dto.ContactReportExecutionCoverageAuthorDto;
import com.mnao.mfp.cr.dto.ContactReportExecutionCoverageDto;
import com.mnao.mfp.cr.dto.SummaryByContactStatusDto;
import com.mnao.mfp.cr.dto.SummaryByDealerListDto;
import com.mnao.mfp.cr.entity.ContactReportDiscussion;
import com.mnao.mfp.cr.entity.ContactReportInfo;
import com.mnao.mfp.cr.entity.Dealers;
import com.mnao.mfp.cr.repository.ContactInfoRepository;
import com.mnao.mfp.cr.util.ContactReportEnum;
import com.mnao.mfp.cr.util.IssueType;
import com.mnao.mfp.cr.util.LocationEnum;
import com.mnao.mfp.cr.util.TriFunction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class ContactReportSummaryService {

	@Autowired
	private ContactInfoRepository contactInfoRepository;

	@Autowired
	private IssueType issueType;

	@Autowired
	private DealerService dealerService;

	private final List<String> months = Arrays.asList("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep",
			"Oct", "Nov", "Dec");

	Function<List<Dealers>, List<String>> extractDealerCodes = dealers -> dealers.stream().map(Dealers::getDlrCd)
			.collect(Collectors.toList());
	Function<String, List<Dealers>> filterByRegion = i -> dealerService.findAll().stream()
			.filter(dealers -> dealers.getRgnCd().equals(i)).collect(Collectors.toList());
	Function<String, List<Dealers>> filterByDistrict = i -> dealerService.findAll().stream()
			.filter(dealers -> dealers.getDistrictCd().equals(i)).collect(Collectors.toList());
	Function<String, List<Dealers>> filterByZone = i -> dealerService.findAll().stream()
			.filter(dealers -> dealers.getDistrictCd().equals(i)).collect(Collectors.toList());
	Function<String, List<Dealers>> filterByDealer = i -> dealerService.findAll().stream()
			.filter(dealers -> dealers.getDlrCd().equals(i)).collect(Collectors.toList());

	private Map<String, String> calcMetrics(List<ContactReportInfo> contactReportInfos, List<String> issues,
			String type, String value,
			TriFunction<List<ContactReportInfo>, String, Integer, BiPredicate<ContactReportInfo, Integer>> filtered) {
		Map<String, String> map = new HashMap<>();
		map.put(type, value);
		issues.forEach(i -> {
			BiPredicate<ContactReportInfo, Integer> reviewPredicate = (contactReportInfo,
					statusCode) -> contactReportInfo.getContactStatus() == statusCode;
			BiPredicate<ContactReportInfo, Integer> exceptReviewPredicate = (contactReportInfo,
					statusCode) -> contactReportInfo.getContactStatus() != statusCode;
			long reviewedCount = filtered.discussionCount(contactReportInfos, i,
					ContactReportEnum.REVIEWED.getStatusCode(), reviewPredicate);
			long total = filtered.discussionCount(contactReportInfos, i, ContactReportEnum.SUBMITTED.getStatusCode(),
					exceptReviewPredicate);

			map.put(i, String.format("%d/%d", reviewedCount, total));
		});
		return map;
	}

	private Map<String, String> calcMetrics(List<ContactReportInfo> contactReportInfos, List<String> issues,
			String type, String value, BiFunction<List<ContactReportInfo>, String, List<ContactReportInfo>> filtered) {
		Map<String, String> map = new HashMap<>();
		map.put(type, value);
		issues.forEach(i -> {
			List<ContactReportInfo> list = filtered.apply(contactReportInfos, i);
			long reviewed = list.stream()
					.filter(f -> f.getContactStatus() == ContactReportEnum.REVIEWED.getStatusCode()).count();

			map.put(i, String.format("%d/%d", reviewed, list.size()));
		});
		return map;
	}

	public List<Map<String, String>> getSummaryByMonth(String type, String value,
			BiFunction<List<ContactReportInfo>, String, List<ContactReportInfo>> filteredByMonth) {
		List<Dealers> dealers = dealerService.findAll();
		List<ContactReportInfo> contactReportInfos;
		List<Map<String, String>> summaryList;
		if (type.equalsIgnoreCase(LocationEnum.DISTRICT.name())) {
			dealers = filterByDistrict.apply(value);
			Map<String, List<Dealers>> dealersByDealer = dealers.stream()
					.collect(Collectors.groupingBy(Dealers::getDlrCd));
			Set<String> dealerSet = dealersByDealer.keySet();
			summaryList = dealerSet.stream().map(r -> {
				List<ContactReportInfo> contactReportInfoList = contactInfoRepository.findByDlrCdInAndContactStatusNot(
						extractDealerCodes.apply(dealersByDealer.get(r)), ContactReportEnum.DRAFT.getStatusCode());
				return calcMetrics(contactReportInfoList, months, LocationEnum.DEALER.getLocationText(), r,
						filteredByMonth);
			}).collect(Collectors.toList());
		} else if (type.equalsIgnoreCase(LocationEnum.ZONE.name())) {
			dealers = filterByZone.apply(value);
			Map<String, List<Dealers>> dealersByDistrict = dealers.stream()
					.collect(Collectors.groupingBy(Dealers::getDistrictCd));
			Set<String> districts = dealersByDistrict.keySet();
			summaryList = districts.stream().map(r -> {
				List<ContactReportInfo> contactReportInfoList = contactInfoRepository.findByDlrCdInAndContactStatusNot(
						extractDealerCodes.apply(dealersByDistrict.get(r)), ContactReportEnum.DRAFT.getStatusCode());
				return calcMetrics(contactReportInfoList, months, LocationEnum.DISTRICT.getLocationText(), r,
						filteredByMonth);
			}).collect(Collectors.toList());
		} else if (type.equalsIgnoreCase(LocationEnum.REGION.name())) {
			dealers = filterByRegion.apply(value);
			Map<String, List<Dealers>> dealersByZone = dealers.stream()
					.collect(Collectors.groupingBy(Dealers::getZoneCd));
			Set<String> zones = dealersByZone.keySet();
			summaryList = zones.stream().map(r -> {
				List<ContactReportInfo> contactReportInfoList = contactInfoRepository.findByDlrCdInAndContactStatusNot(
						extractDealerCodes.apply(dealersByZone.get(r)), ContactReportEnum.DRAFT.getStatusCode());
				return calcMetrics(contactReportInfoList, months, LocationEnum.ZONE.getLocationText(), r,
						filteredByMonth);
			}).collect(Collectors.toList());
		} else if (type.equalsIgnoreCase(LocationEnum.DEALER.name())) {
			dealers = filterByDealer.apply(value);
			Map<String, List<Dealers>> dealersByRegion = dealers.stream()
					.collect(Collectors.groupingBy(Dealers::getRgnCd));
			Set<String> regions = dealersByRegion.keySet();
			summaryList = regions.stream().map(r -> {
				List<ContactReportInfo> contactReportInfoList = contactInfoRepository.findByDlrCdInAndContactStatusNot(
						extractDealerCodes.apply(dealersByRegion.get(r)), ContactReportEnum.DRAFT.getStatusCode());
				return calcMetrics(contactReportInfoList, months, LocationEnum.REGION.getLocationText(), r,
						filteredByMonth);
			}).collect(Collectors.toList());
		} else {
			Map<String, List<Dealers>> dealersByRegion = dealerService.findAll().stream()
					.collect(Collectors.groupingBy(Dealers::getRgnCd));
			Set<String> regions = dealersByRegion.keySet();
			summaryList = regions.stream().map(r -> {
				List<ContactReportInfo> contactReportInfoList = contactInfoRepository.findByDlrCdInAndContactStatusNot(
						extractDealerCodes.apply(dealersByRegion.get(r)), ContactReportEnum.DRAFT.getStatusCode());
				return calcMetrics(contactReportInfoList, months, LocationEnum.REGION.getLocationText(), r,
						filteredByMonth);
			}).collect(Collectors.toList());
		}
		return summaryList;
	}

	public List<Map<String, String>> getSummaryByLocation(String type, String value, String category,
			TriFunction<List<ContactReportInfo>, String, Integer, BiPredicate<ContactReportInfo, Integer>> issueCount) {
		List<Map<String, String>> finalListData = new ArrayList<>();
		List<ContactReportInfo> contactReports = contactInfoRepository.findByCurrentIssuesNotNull();
		Map<String, Map<String, List<ContactReportInfo>>> reports;
		if (type.equalsIgnoreCase(LocationEnum.DISTRICT.name())) {
			reports = contactReports.stream().collect(Collectors.groupingBy(group -> group.getDealers().getDlrCd(),
					Collectors.groupingBy(ContactReportInfo::getCurrentIssues)));
		} else if (type.equalsIgnoreCase(LocationEnum.ZONE.name())) {
			reports = contactReports.stream().collect(Collectors.groupingBy(group -> group.getDealers().getDistrictCd(),
					Collectors.groupingBy(ContactReportInfo::getCurrentIssues)));
		} else if (type.equalsIgnoreCase(LocationEnum.DEALER.name())) {
			reports = contactReports.stream().collect(Collectors.groupingBy(group -> group.getDealers().getDlrCd(),
					Collectors.groupingBy(ContactReportInfo::getCurrentIssues)));
		} else if (type.equalsIgnoreCase(LocationEnum.REGION.name())) {
			reports = filterContactReportByRegion(contactReports, value)
					.collect(Collectors.groupingBy(group -> group.getDealers().getZoneCd(),
							Collectors.groupingBy(ContactReportInfo::getCurrentIssues)));
		} else {
			reports = contactReports.stream().collect(Collectors.groupingBy(group -> group.getDealers().getRgnCd(),
					Collectors.groupingBy(ContactReportInfo::getCurrentIssues)));
		}

		reports.forEach((key, value1) -> {
			HashMap<String, List<ContactReportInfo>> finalData = new HashMap<>();
			HashMap<String, String> responseData = new HashMap<>();
			value1.forEach((key1, value2) -> Arrays.asList(key1.split("\\|")).forEach(issue -> {
				finalData.put(issue, value2);
			}));
			finalData.forEach((key1, value2) -> {
				Long submittedCount = value2.stream()
						.filter(report -> report.getContactStatus() == ContactReportEnum.SUBMITTED.getStatusCode())
						.count();
				Long reviewedCount = value2.stream()
						.filter(report -> report.getContactStatus() == ContactReportEnum.REVIEWED.getStatusCode())
						.count();
				responseData.put(key1, String.format("%d/%d", submittedCount, reviewedCount));
			});
			responseData.put(getStringByType(type), key);
			finalListData.add(responseData);
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

	public List<Map<String, Object>> summaryByCurrentStatus(String category) {
		List<String> issueTypes = issueType.getIssuesByCategory().get(category.toLowerCase());
		List<ContactReportInfo> contactReportInfos = contactInfoRepository.findAll();
		return issueTypes.stream().map(issueType -> {
			Map<String, Object> summaryMap = new HashMap<>();
			Map<String, Long> stCntMap = new HashMap<>();
			List<ContactReportInfo> contactReportInfoList = contactReportInfos.stream().filter(contactReportInfo -> {
				Optional<ContactReportDiscussion> optionalContactReportDiscussion = contactReportInfo.getDiscussions()
						.stream().filter(contactReportDiscussion -> contactReportDiscussion.getTopic() != null)
						.filter(contactReportDiscussion -> contactReportDiscussion.getTopic().equals(issueType))
						.findAny();
				return optionalContactReportDiscussion.isPresent();
			}).collect(Collectors.toList());
			//
			contactReportInfoList.forEach((crList) -> {
				ContactReportEnum stat = null;
				if (crList.getContactStatus() == ContactReportEnum.DRAFT.getStatusCode()) {
					stat = ContactReportEnum.DRAFT;
				} else if (crList.getContactStatus() == ContactReportEnum.DISCUSSION_REQUESTED.getStatusCode()) {
					stat = ContactReportEnum.DISCUSSION_REQUESTED;
				} else if (crList.getContactStatus() == ContactReportEnum.SUBMITTED.getStatusCode()) {
					stat = ContactReportEnum.SUBMITTED;
				} else if (crList.getContactStatus() == ContactReportEnum.REVIEWED.getStatusCode()) {
					stat = ContactReportEnum.REVIEWED;
				} 
				if (stat != null) {
					long lv = stCntMap.getOrDefault(stat.getDisplayText(), 0L);
					lv++;
					stCntMap.put(stat.getDisplayText(), lv);
				}
			});
			/*
			 * draft: DRAFT, pendingReview: DISCUSSION_REQUESTED + SUBMITTED + REVIEWED
			 * completed: COMPLETED
			 */
			summaryMap.put("draft", stCntMap.getOrDefault(ContactReportEnum.DRAFT.getDisplayText(), 0L));
			summaryMap.put("completed",stCntMap.getOrDefault(ContactReportEnum.REVIEWED.getDisplayText(), 0L));
			summaryMap.put("pendingReview",
					stCntMap.getOrDefault(ContactReportEnum.DISCUSSION_REQUESTED.getDisplayText(), 0L)
							+ stCntMap.getOrDefault(ContactReportEnum.SUBMITTED.getDisplayText(), 0L));
			summaryMap.put("total",
					stCntMap.getOrDefault(ContactReportEnum.DISCUSSION_REQUESTED.getDisplayText(), 0L)
							+ stCntMap.getOrDefault(ContactReportEnum.SUBMITTED.getDisplayText(), 0L)
							+ stCntMap.getOrDefault(ContactReportEnum.REVIEWED.getDisplayText(), 0L)
//							+ stCntMap.getOrDefault(ContactReportEnum.COMPLETED.getDisplayText(), 0L)
							+ stCntMap.getOrDefault(ContactReportEnum.DRAFT.getDisplayText(), 0L));
			summaryMap.put("issue", issueType);
			return summaryMap;
		}).collect(Collectors.toList());

	}

	//
	// BACKUP of summaryByCurrentStatus
	//
	public List<Map<String, Object>> summaryByCurrentStatusBAK(String category) {
		List<String> issueTypes = issueType.getIssuesByCategory().get(category.toLowerCase());
		List<ContactReportInfo> contactReportInfos = contactInfoRepository.findAll();
		return issueTypes.stream().map(issueType -> {
			Map<String, Object> summaryMap = new HashMap<>();
			List<ContactReportInfo> contactReportInfoList = contactReportInfos.stream().filter(contactReportInfo -> {
				Optional<ContactReportDiscussion> optionalContactReportDiscussion = contactReportInfo.getDiscussions()
						.stream().filter(contactReportDiscussion ->
//                            contactReportDiscussion.getDiscussion().equals(issueType)
				contactReportDiscussion.getTopic().equals(issueType)).findAny();
				return optionalContactReportDiscussion.isPresent();
			}).collect(Collectors.toList());
			long requiredReportCompletion = contactReportInfoList.stream().filter(contactReportInfo -> contactReportInfo
					.getContactStatus() == ContactReportEnum.REVIEWED.getStatusCode()).count();
			long notStarted = contactReportInfoList.stream().filter(contactReportInfo -> contactReportInfo
					.getContactStatus() == ContactReportEnum.REVIEWED.getStatusCode()).count();
			long drafts = contactReportInfoList.stream().filter(contactReportInfo -> contactReportInfo
					.getContactStatus() == ContactReportEnum.DRAFT.getStatusCode()).count();
			long pendingReview = contactReportInfoList.stream().filter(contactReportInfo -> contactReportInfo
					.getContactStatus() != ContactReportEnum.REVIEWED.getStatusCode()).count();
			summaryMap.put("requiredReportCompletion", requiredReportCompletion);
			summaryMap.put("notStarted", notStarted);
			summaryMap.put("drafts", drafts);
			summaryMap.put("pendingReview", pendingReview);
			summaryMap.put("issue", issueType);
			return summaryMap;
		}).collect(Collectors.toList());

	}

	public List<SummaryByDealerListDto> summaryByCurrentStatusDealershipList(String issue) {
		List<ContactReportInfo> contactReportInfoList = contactInfoRepository.findByCurrentIssuesContaining(issue);
		List<Dealers> dealerList = contactReportInfoList.stream().filter(cr -> Objects.nonNull(cr.getDealers())).map(ContactReportInfo::getDealers)
				.collect(Collectors.toList());

		return dealerList.stream()
				.map(dlr -> SummaryByDealerListDto.builder().cityName(dlr.getCityNm()).dealerCode(dlr.getDlrCd())
						.dealerName(dlr.getDbaNm()).issue(issue).zipCode(dlr.getZipCd()).stateName(dlr.getStCd())
						.build())
				.collect(Collectors.toList());

	}

	public List<ContactReportExecutionCoverageDto> reportExecutionByCoverage(String date) {
		LocalDate startDate;
		try {
			startDate = LocalDate.parse(date).withDayOfMonth(1);
		} catch (Exception e) {
			throw new IllegalArgumentException("The date format should be " + AppConstants.LOCALDATE_FORMAT);
		}
		LocalDate endDate = startDate.withDayOfMonth(startDate.lengthOfMonth());

		List<ContactReportInfo> contactReportInfos = contactInfoRepository.findByContactDtBetween(startDate, endDate);
		Map<String, Long> reportCount = contactReportInfos.stream().map(ContactReportInfo::getDlrCd)
				.collect(Collectors.groupingBy(e -> e, Collectors.counting()));

		Set<Dealers> dealers = contactReportInfos.stream().map(ContactReportInfo::getDealers)
				.collect(Collectors.toSet());
		Map<String, List<ContactReportInfo>> authorContactReports = contactReportInfos.stream()
				.collect(Collectors.groupingBy(ContactReportInfo::getContactAuthor));
		return dealers.stream()
				.map(dealer -> ContactReportExecutionCoverageDto.builder().dealerName(dealer.getDbaNm().trim())
						.dealerCode(dealer.getDlrCd().trim()).type(dealer.getCRI().get(0).getContactType())
						.author(dealer.getCRI().get(0).getContactAuthor())
						.coverage(getCoverage(dealer.getCRI().get(0).getContactType()))
						.reportCount(reportCount.get(dealer.getDlrCd()))
						.authorDtos(dealer.getCRI().stream().map(contactReportInfo -> {
							List<ContactReportInfo> lists = authorContactReports
									.get(contactReportInfo.getContactAuthor());
							Function<String, Boolean> isExist = issueTopic -> lists.stream().anyMatch(
									l -> l.getDiscussions().stream().anyMatch(x -> x.getTopic().equals(issueTopic)));
							return ContactReportExecutionCoverageAuthorDto.builder()
									.author(contactReportInfo.getContactAuthor())
									.isDealerDefeciencyIdentified(isExist.apply("Dealer Dev Deficiencies Identifed"))
									.isServiceRetentionFysl(isExist.apply("Service Retention/FYSL"))
									.reportsCreatedByAuthor(lists.size()).build();
						}

						).collect(Collectors.toList())).build()).collect(Collectors.toList());
	}

	public List<ContactReportExecutionCoverageDto> reportExecutionBycoverageBAK(String date) {
		LocalDate startDate;
		try {
			startDate = LocalDate.parse(date).withDayOfMonth(1);
		} catch (Exception e) {
			throw new IllegalArgumentException("The date format should be " + AppConstants.LOCALDATE_FORMAT);
		}
		LocalDate endDate = startDate.withDayOfMonth(startDate.lengthOfMonth());

		List<ContactReportInfo> contactReportInfos = contactInfoRepository.findByContactDtBetween(startDate, endDate);

		Map<String, Long> reportCount = contactReportInfos.stream().map(ContactReportInfo::getDlrCd)
				.collect(Collectors.groupingBy(e -> e, Collectors.counting()));

		Set<Dealers> dealers = contactReportInfos.stream().map(ContactReportInfo::getDealers)
				.collect(Collectors.toSet());
		Map<String, List<ContactReportInfo>> authorContactReports = contactReportInfos.stream()
				.collect(Collectors.groupingBy(ContactReportInfo::getContactAuthor));
		return dealers.stream()
				.map(dealer -> ContactReportExecutionCoverageDto.builder().dealerName(dealer.getDbaNm().trim())
						.dealerCode(dealer.getDlrCd().trim()).type(dealer.getCRI().get(0).getContactType())
						.author(dealer.getCRI().get(0).getContactAuthor())
						.coverage(getCoverage(dealer.getCRI().get(0).getContactType()))
						.reportCount(reportCount.get(dealer.getDlrCd()))
						.authorDtos(dealer.getCRI().stream().map(contactReportInfo -> {
							List<ContactReportInfo> lists = authorContactReports
									.get(contactReportInfo.getContactAuthor());
							Function<String, Boolean> isExist = issueTopic -> lists.stream().anyMatch(
									l -> l.getDiscussions().stream().anyMatch(x -> x.getTopic().equals(issueTopic)));
							return ContactReportExecutionCoverageAuthorDto.builder()
									.author(contactReportInfo.getContactAuthor())
									.isDealerDefeciencyIdentified(isExist.apply("Dealer Dev Deficiencies Identifed"))
									.isServiceRetentionFysl(isExist.apply("Service Retention/FYSL"))
									.reportsCreatedByAuthor(lists.size()).build();
						}

						).collect(Collectors.toList())).build()).collect(Collectors.toList());

	}

	private String getCoverage(String contactType) {
		if (contactType.equalsIgnoreCase("Sales | Service") || contactType.equalsIgnoreCase("Sales | Service | Other"))
			return "100";
		else
			return "50";
	}

	public List<Map<String, List<Object>>> reportExecutionByException(String date) {
		LocalDate startDate;
		try {
			startDate = LocalDate.parse(date).withDayOfMonth(1);
		} catch (Exception e) {
			throw new IllegalArgumentException("The date format should be " + AppConstants.LOCALDATE_FORMAT);
		}
		LocalDate endDate = startDate.withDayOfMonth(startDate.lengthOfMonth());

		List<ContactReportInfo> contactReportInfos = contactInfoRepository.findByContactDtBetween(startDate, endDate);

		Map<String, List<Object>> SummaryMap = new HashMap<>();
		return contactReportInfos.stream().map(cr -> {
			if (SummaryMap.containsKey(cr.getDlrCd())) {
				List<Object> obj = SummaryMap.get(cr.getDlrCd());
				List<Object> ob1 = new ArrayList<>();
				ob1.add(cr.getDlrCd());
				ob1.add(cr.getContactReportId());
				ob1.add(cr.getContactAuthor());
				obj.add(ob1);
				SummaryMap.put(cr.getDlrCd(), obj);
			} else {
				List<Object> ob1 = new ArrayList<>();
				ob1.add(cr.getDlrCd());
				ob1.add(cr.getContactReportId());
				ob1.add(cr.getContactAuthor());
				SummaryMap.put(cr.getDlrCd(), ob1);
			}
			return SummaryMap;
		}).collect(Collectors.toList());
	}

	public List<Map<String, String>> getSummaryByLocation(FilterCriteria filter, String category) {
		List<Map<String, String>> finalListData = new ArrayList<>();
		List<ContactReportInfo> contactReports = contactInfoRepository.findByCurrentIssuesNotNull();
		if (!CollectionUtils.isEmpty(filter.getIssuesFilter())) {
			contactReports = filterContactReportsByIssues(filter, contactReports);
		}
		if (filter.isNotNullOrEmpty(filter.getStartDate()) && filter.isNotNullOrEmpty(filter.getEndDate())) {
			contactReports = filterContactReportsByDateRange(filter, contactReports);
		}
		Map<String, Map<String, List<ContactReportInfo>>> reports;
		reports = filterContactReportsByLocation(filter, contactReports);

		reports.forEach((key, value) -> {
			HashMap<String, List<ContactReportInfo>> finalData = new HashMap<>();
			HashMap<String, String> responseData = new HashMap<>();
			value.forEach((key1, value1) -> Arrays.asList(key1.split("\\|")).forEach(issue -> {
				if (finalData.containsKey(issue)) {
					List<ContactReportInfo> existingData = finalData.get(issue);
					existingData.addAll(value1);
					finalData.put(issue, existingData);
				} else {
					finalData.put(issue, value1);
				}
			}));
			finalData.forEach((key1, value1) -> {
				long submittedCount = value1.stream()
						.filter(report -> report.getContactStatus() == ContactReportEnum.SUBMITTED.getStatusCode())
						.count();
				long reviewedCount = value1.stream()
						.filter(report -> report.getContactStatus() == ContactReportEnum.REVIEWED.getStatusCode())
						.count();
				if (submittedCount > 0 || reviewedCount > 0) {
					responseData.put(key1, String.format("%d/%d", submittedCount, reviewedCount));
				}
			});
			if (!CollectionUtils.isEmpty(responseData)) {
				responseData.put(getStringByType(filter.forLocation().name()), key);
				finalListData.add(responseData);
			}

		});

		return finalListData;
	}

	private Map<String, Map<String, List<ContactReportInfo>>> filterContactReportsByLocation(FilterCriteria filter,
			List<ContactReportInfo> contactReports) {
		Map<String, Map<String, List<ContactReportInfo>>> reports;
		if (filter.forLocation() == LocationEnum.DISTRICT) {
			reports = filterContactReportByDistrict(contactReports, filter)
					.collect(Collectors.groupingBy(group -> group.getDealers().getDlrCd(),
							Collectors.groupingBy(ContactReportInfo::getCurrentIssues)));
		} else if (filter.forLocation() == LocationEnum.ZONE) {
			reports = filterContactReportByZone(contactReports, filter)
					.collect(Collectors.groupingBy(group -> group.getDealers().getDistrictCd(),
							Collectors.groupingBy(ContactReportInfo::getCurrentIssues)));
		} else if (filter.forLocation() == LocationEnum.DEALER) {
			reports = contactReports.stream().collect(Collectors.groupingBy(group -> group.getDealers().getDlrCd(),
					Collectors.groupingBy(ContactReportInfo::getCurrentIssues)));
		} else if (filter.forLocation() == LocationEnum.REGION) {
			reports = filterContactReportByRegion(contactReports, filter.getRgnCd())
					.collect(Collectors.groupingBy(group -> group.getDealers().getZoneCd(),
							Collectors.groupingBy(ContactReportInfo::getCurrentIssues)));
		} else {
			reports = contactReports.stream().collect(Collectors.groupingBy(group -> group.getDealers().getRgnCd(),
					Collectors.groupingBy(ContactReportInfo::getCurrentIssues)));
		}
		return reports;
	}

	private Stream<ContactReportInfo> filterContactReportByRegion(List<ContactReportInfo> contactReports,
			String filter) {
		return contactReports.stream().filter(cr -> filter.equalsIgnoreCase(cr.getDealers().getRgnCd()));
	}

	private Stream<ContactReportInfo> filterContactReportByZone(List<ContactReportInfo> contactReports,
			FilterCriteria filter) {
		return filterContactReportByRegion(contactReports, filter.getRgnCd())
				.filter(cr -> filter.getZoneCd().equalsIgnoreCase(cr.getDealers().getZoneCd()));
	}

	private Stream<ContactReportInfo> filterContactReportByDistrict(List<ContactReportInfo> contactReports,
			FilterCriteria filter) {
		return filterContactReportByZone(contactReports, filter)
				.filter(cr -> filter.getDistrictCd().equalsIgnoreCase(cr.getDealers().getDistrictCd()));
	}

	private List<ContactReportInfo> filterContactReportsByDateRange(FilterCriteria filter,
			List<ContactReportInfo> contactReports) {
		return contactReports.stream().filter(cr -> cr.getContactDt().isAfter(filter.getStartDate())
				&& cr.getContactDt().isBefore(filter.getEndDate())).collect(Collectors.toList());
	}

	public List<ContactReportInfo> filterContactReportsByIssues(FilterCriteria filter,
																List<ContactReportInfo> contactReports) {
		return contactReports.stream().filter(
				cr -> filter.getIssuesFilter().stream().anyMatch(value -> cr.getCurrentIssues().contains(value)))
				.collect(Collectors.toList());
	}

	public List<Map<String, String>> getSummaryOfMonthByLocation(FilterCriteria filter, String category) {
		List<Map<String, String>> finalListData = new ArrayList<>();
		List<ContactReportInfo> contactReports = contactInfoRepository.findByCurrentIssuesNotNullAndContactDtNotNull();
		if (!CollectionUtils.isEmpty(filter.getIssuesFilter())) {
			contactReports = filterContactReportsByIssues(filter, contactReports);
		}
		if (filter.isNotNullOrEmpty(filter.getStartDate()) && filter.isNotNullOrEmpty(filter.getEndDate())) {
			contactReports = filterContactReportsByDateRange(filter, contactReports);
		}
		contactReports.forEach(value -> System.out.println(value.getContactReportId() + ">" + value.getContactDt()));
		Map<String, Map<Object, List<ContactReportInfo>>> reports;
		if (filter.forLocation() == LocationEnum.DISTRICT) {
			reports = filterContactReportByDistrict(contactReports, filter)
					.collect(Collectors.groupingBy(group -> group.getDealers().getDlrCd(),
							Collectors.groupingBy(gr -> formatDate(gr.getContactDt()))));
		} else if (filter.forLocation() == LocationEnum.ZONE) {
			reports = filterContactReportByZone(contactReports, filter)
					.collect(Collectors.groupingBy(group -> group.getDealers().getDistrictCd(),
							Collectors.groupingBy(gr -> formatDate(gr.getContactDt()))));
		} else if (filter.forLocation() == LocationEnum.DEALER) {
			reports = contactReports.stream().collect(Collectors.groupingBy(group -> group.getDealers().getDlrCd(),
					Collectors.groupingBy(gr -> formatDate(gr.getContactDt()))));
		} else if (filter.forLocation() == LocationEnum.REGION) {
			reports = filterContactReportByRegion(contactReports, filter.getRgnCd())
					.collect(Collectors.groupingBy(group -> group.getDealers().getZoneCd(),
							Collectors.groupingBy(gr -> formatDate(gr.getContactDt()))));
		} else {
			reports = contactReports.stream().collect(Collectors.groupingBy(group -> group.getDealers().getRgnCd(),
					Collectors.groupingBy(gr -> gr.getContactDt().format(DateTimeFormatter.ofPattern("MMM")))));
		}

		reports.forEach((key1, value1) -> {
			HashMap<String, List<ContactReportInfo>> finalData = new HashMap<>();
			HashMap<String, String> responseData = new HashMap<>();
			value1.forEach((key, value) -> finalData.put((String) key, value));
			finalData.entrySet().stream().forEach(newEntry -> {
				Long submittedCount = newEntry.getValue().stream()
						.filter(report -> report.getContactStatus() == ContactReportEnum.SUBMITTED.getStatusCode())
						.count();
				Long reviewedCount = newEntry.getValue().stream()
						.filter(report -> report.getContactStatus() == ContactReportEnum.REVIEWED.getStatusCode())
						.count();
				responseData.put(newEntry.getKey(), String.format("%d/%d", submittedCount, reviewedCount));
			});
			months.forEach(value -> {
				if (!responseData.containsKey(value)) {
					responseData.put(value, "0/0");
				}
			});
			responseData.put(getStringByType(filter.forLocation().name()), key1);
			finalListData.add(responseData);
		});

		return finalListData;
	}

	private String formatDate(LocalDate contactDt) {
		return DateTimeFormatter.ofPattern("MMM").format(contactDt);
	}

	public List<SummaryByContactStatusDto> filterSummaryByCurrentStatusUsingIssues(FilterCriteria filter) {
		List<SummaryByContactStatusDto> finalListData = new ArrayList<>();
		List<ContactReportInfo> contactReports = contactInfoRepository.findByCurrentIssuesNotNull();
		if (!CollectionUtils.isEmpty(filter.getIssuesFilter())) {
			contactReports = filterContactReportsByIssues(filter, contactReports);
		}
		Map<String, List<ContactReportInfo>> reports;
		Map<String, List<ContactReportInfo>> finalData = new HashMap<>();
		reports = contactReports.stream().collect(Collectors.groupingBy(ContactReportInfo::getCurrentIssues));

		reports.forEach((key, value) -> {
			Arrays.asList(key.split("\\|")).forEach(issue -> {
				if (finalData.containsKey(issue)) {
					List<ContactReportInfo> existingData = finalData.get(issue);
					existingData.addAll(value);
					finalData.put(issue, existingData);
				} else {
					finalData.put(issue, value);
				}
			});
		});

		finalData.forEach((key, value) -> {
			if (filter.getIssuesFilter().contains(key)) {
				long submittedCount = value.stream()
						.filter(report -> report.getContactStatus() == ContactReportEnum.SUBMITTED.getStatusCode()).count();
				long reviewedCount = value.stream()
						.filter(report -> report.getContactStatus() == ContactReportEnum.REVIEWED.getStatusCode()).count();
				long draftCount = value.stream()
						.filter(report -> report.getContactStatus() == ContactReportEnum.DRAFT.getStatusCode()).count();
				long discussionReqCount = value.stream()
						.filter(report -> report.getContactStatus() == ContactReportEnum.DISCUSSION_REQUESTED.getStatusCode()).count();
				finalListData.add(SummaryByContactStatusDto.builder()
						.issue(key)
						.draftCount(draftCount)
						.pendingReviewCount(submittedCount + discussionReqCount)
						.reviewCount(reviewedCount)
						.total(draftCount + submittedCount + discussionReqCount + reviewedCount)
						.build());
			}
		});

		return finalListData;
	}
	
	
	public List<SummaryByDealerListDto> summaryByCurrentStatusOfDealershipList(String issue) {
		List<ContactReportInfo> contactReportInfoList = contactInfoRepository.findByCurrentIssuesContaining(issue);
		List<Dealers> dealerList = contactReportInfoList.stream().map(ContactReportInfo::getDealers)
				.collect(Collectors.toList());

		return dealerList.stream()
				.map(dlr -> SummaryByDealerListDto.builder().cityName(dlr.getCityNm()).dealerCode(dlr.getDlrCd())
						.dealerName(dlr.getDbaNm()).issue(issue).zipCode(dlr.getZipCd()).stateName(dlr.getStCd())
						.build())
				.collect(Collectors.toList());

	}
}
