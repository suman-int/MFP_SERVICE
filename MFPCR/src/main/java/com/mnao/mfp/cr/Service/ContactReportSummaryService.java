package com.mnao.mfp.cr.Service;

import com.mnao.mfp.common.dao.DealerFilter;
import com.mnao.mfp.common.util.AppConstants;
import com.mnao.mfp.cr.dto.ContactReportExecutionCoverageAuthorDto;
import com.mnao.mfp.cr.dto.ContactReportExecutionCoverageDto;
import com.mnao.mfp.cr.dto.SummaryByDealerListDto;
import com.mnao.mfp.cr.entity.ContactReportDiscussion;
import com.mnao.mfp.cr.entity.ContactReportInfo;
import com.mnao.mfp.cr.entity.Dealers;
import com.mnao.mfp.cr.pdf.dao.DealerAndContactLocationInfo;
import com.mnao.mfp.cr.repository.ContactInfoRepository;
import com.mnao.mfp.cr.util.ContactReportEnum;
import com.mnao.mfp.cr.util.IssueType;
import com.mnao.mfp.cr.util.LocationEnum;
import com.mnao.mfp.cr.util.LocationFilter;
import com.mnao.mfp.cr.util.TriFunction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class ContactReportSummaryService {

	@Autowired
	private ContactInfoRepository contactInfoRepository;

	@Autowired
	private IssueType issueType;

	@Autowired
	private DealerService dealerService;

	private List<String> months = Arrays.asList("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct",
			"Nov", "Dec");

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
		issues.stream().forEach(i -> {
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
		issues.stream().forEach(i -> {
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
			reports = contactReports.stream().collect(Collectors.groupingBy(group -> group.getDealers().getDlrCd(), Collectors.groupingBy(gr -> gr.getCurrentIssues())));
		} else if (type.equalsIgnoreCase(LocationEnum.ZONE.name())) {
			reports = contactReports.stream().collect(Collectors.groupingBy(group -> group.getDealers().getDistrictCd(), Collectors.groupingBy(gr -> gr.getCurrentIssues())));
		} else if (type.equalsIgnoreCase(LocationEnum.DEALER.name())) {
			reports = contactReports.stream().collect(Collectors.groupingBy(group -> group.getDealers().getDlrCd(), Collectors.groupingBy(gr -> gr.getCurrentIssues())));
		} else if (type.equalsIgnoreCase(LocationEnum.REGION.name())) {
			reports = contactReports.stream().filter(cr -> value.equalsIgnoreCase(cr.getDealers().getRgnCd())).collect(Collectors.groupingBy(group -> group.getDealers().getZoneCd(), Collectors.groupingBy(gr -> gr.getCurrentIssues())));
		} else {
			reports = contactReports.stream().collect(Collectors.groupingBy(group -> group.getDealers().getRgnCd(), Collectors.groupingBy(gr -> gr.getCurrentIssues())));
		}
		
		reports.entrySet().forEach(entry -> {
			HashMap<String, List<ContactReportInfo>> finalData = new HashMap<>();
			HashMap<String, String> responseData = new HashMap<>();
			entry.getValue().entrySet().forEach(childEtry -> {
				Arrays.asList(childEtry.getKey().split("\\|")).forEach(issue -> {
					finalData.put(issue, childEtry.getValue());
				});
			});
			finalData.entrySet().stream().forEach(newEntry -> {
				Long submittedCount = newEntry.getValue().stream().filter(report -> report.getContactStatus() == ContactReportEnum.SUBMITTED.getStatusCode()).count();
				Long reviewedCount = newEntry.getValue().stream().filter(report -> report.getContactStatus() == ContactReportEnum.REVIEWED.getStatusCode()).count();
				responseData.put(newEntry.getKey(), String.format("%d/%d", submittedCount, reviewedCount));
			});
			responseData.put(getStringByType(type), entry.getKey());
			finalListData.add(responseData);
		});
		
		
//		List<String> issueTypes = issueType.getIssuesByCategory().get(category.toLowerCase());
//		List<Dealers> dealers;
//		List<ContactReportInfo> contactReportInfos;	
//		List<Map<String, String>> summaryList;
//		if (type.equalsIgnoreCase(LocationEnum.DISTRICT.name())) {
//			dealers = filterByDistrict.apply(value);
//			Map<String, List<Dealers>> dealersByDealer = dealers.stream()
//					.collect(Collectors.groupingBy(Dealers::getDlrCd));
//			Set<String> dealerSet = dealersByDealer.keySet();
//			summaryList = dealerSet.stream().map(r -> {
//				List<ContactReportInfo> contactReportInfoList = contactInfoRepository.findByDlrCdInAndContactStatusNot(
//						extractDealerCodes.apply(dealersByDealer.get(r)), ContactReportEnum.DRAFT.getStatusCode());
//
//				return calcMetrics(contactReportInfoList, issueTypes, LocationEnum.DEALER.getLocationText(), r,
//						issueCount);
//
//			}).collect(Collectors.toList());
//		} else if (type.equalsIgnoreCase(LocationEnum.ZONE.name())) {
//			dealers = filterByZone.apply(value);
//			Map<String, List<Dealers>> dealersByDistrict = dealers.stream()
//					.collect(Collectors.groupingBy(Dealers::getDistrictCd));
//			Set<String> districts = dealersByDistrict.keySet();
//			summaryList = districts.stream().map(r -> {
//				List<ContactReportInfo> contactReportInfoList = contactInfoRepository.findByDlrCdInAndContactStatusNot(
//						extractDealerCodes.apply(dealersByDistrict.get(r)), ContactReportEnum.DRAFT.getStatusCode());
//				return calcMetrics(contactReportInfoList, issueTypes, LocationEnum.DISTRICT.getLocationText(), r,
//						issueCount);
//			}).collect(Collectors.toList());
//		} else if (type.equalsIgnoreCase(LocationEnum.REGION.name())) {
//			dealers = filterByRegion.apply(value);
//			Map<String, List<Dealers>> dealersByZone = dealers.stream()
//					.collect(Collectors.groupingBy(Dealers::getZoneCd));
//			Set<String> zones = dealersByZone.keySet();
//			summaryList = zones.stream().map(r -> {
//				List<ContactReportInfo> contactReportInfoList = contactInfoRepository.findByDlrCdInAndContactStatusNot(
//						extractDealerCodes.apply(dealersByZone.get(r)), ContactReportEnum.DRAFT.getStatusCode());
//				return calcMetrics(contactReportInfoList, issueTypes, LocationEnum.ZONE.getLocationText(), r,
//						issueCount);
//			}).collect(Collectors.toList());
//		} else if (type.equalsIgnoreCase(LocationEnum.DEALER.name())) {
//			dealers = filterByDealer.apply(value);
//			Map<String, List<Dealers>> dealersByRegion = dealers.stream()
//					.collect(Collectors.groupingBy(Dealers::getRgnCd));
//			Set<String> regions = dealersByRegion.keySet();
//			summaryList = regions.stream().map(r -> {
//				List<ContactReportInfo> contactReportInfoList = contactInfoRepository.findByDlrCdInAndContactStatusNot(
//						extractDealerCodes.apply(dealersByRegion.get(r)), ContactReportEnum.DRAFT.getStatusCode());
//				return calcMetrics(contactReportInfoList, issueTypes, LocationEnum.REGION.getLocationText(), r,
//						issueCount);
//			}).collect(Collectors.toList());
//		} else {
//			Map<String, List<Dealers>> dealersByRegion = dealerService.findAll().stream()
//					.collect(Collectors.groupingBy(Dealers::getRgnCd));
//			Set<String> regions = dealersByRegion.keySet();
//			summaryList = regions.stream().map(r -> {
//				List<ContactReportInfo> contactReportInfoList = contactInfoRepository.findByDlrCdInAndContactStatusNot(
//						extractDealerCodes.apply(dealersByRegion.get(r)), ContactReportEnum.DRAFT.getStatusCode());
//				return calcMetrics(contactReportInfoList, issueTypes, LocationEnum.REGION.getLocationText(), r,
//						issueCount);
//			}).collect(Collectors.toList());
//		}
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
			contactReportInfoList.stream().forEach((crList) -> {
				ContactReportEnum stat = null;
				if (crList.getContactStatus() == ContactReportEnum.DRAFT.getStatusCode()) {
					stat = ContactReportEnum.DRAFT;
				} else if (crList.getContactStatus() == ContactReportEnum.DISCUSSION_REQUESTED.getStatusCode()) {
					stat = ContactReportEnum.DISCUSSION_REQUESTED;
				} else if (crList.getContactStatus() == ContactReportEnum.SUBMITTED.getStatusCode()) {
					stat = ContactReportEnum.SUBMITTED;
				} else if (crList.getContactStatus() == ContactReportEnum.REVIEWED.getStatusCode()) {
					stat = ContactReportEnum.REVIEWED;
				} else if (crList.getContactStatus() == ContactReportEnum.COMPLETED.getStatusCode()) {
					stat = ContactReportEnum.COMPLETED;
				}
				if (stat != null) {
					long lv = stCntMap.getOrDefault(stat.getDisplayText(), 0l);
					lv++;
					stCntMap.put(stat.getDisplayText(), lv);
				}
			});
			/*
			 * draft: DRAFT, pendingReview: DISCUSSION_REQUESTED + SUBMITTED + REVIEWED
			 * completed: COMPLETED
			 */
			summaryMap.put("draft", stCntMap.getOrDefault(ContactReportEnum.DRAFT.getDisplayText(), 0l));
			summaryMap.put("completed", stCntMap.getOrDefault(ContactReportEnum.COMPLETED.getDisplayText(), 0l));
			summaryMap.put("pendingReview",
					stCntMap.getOrDefault(ContactReportEnum.DISCUSSION_REQUESTED.getDisplayText(), 0l)
							+ stCntMap.getOrDefault(ContactReportEnum.SUBMITTED.getDisplayText(), 0l)
							+ stCntMap.getOrDefault(ContactReportEnum.REVIEWED.getDisplayText(), 0l));
			summaryMap.put("total",
					stCntMap.getOrDefault(ContactReportEnum.DISCUSSION_REQUESTED.getDisplayText(), 0l)
							+ stCntMap.getOrDefault(ContactReportEnum.SUBMITTED.getDisplayText(), 0l)
							+ stCntMap.getOrDefault(ContactReportEnum.REVIEWED.getDisplayText(), 0l)
							+ stCntMap.getOrDefault(ContactReportEnum.COMPLETED.getDisplayText(), 0l)
							+ stCntMap.getOrDefault(ContactReportEnum.DRAFT.getDisplayText(), 0l));
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
		List<Dealers> dealerList = contactReportInfoList.stream().map(data -> data.getDealers())
				.collect(Collectors.toList());

		List<SummaryByDealerListDto> listData = dealerList.stream().map(dlr -> {
			return SummaryByDealerListDto.builder().cityName(dlr.getCityNm()).dealerCode(dlr.getDlrCd())
					.dealerName(dlr.getDbaNm()).issue(issue).zipCode(dlr.getZipCd()).stateName(dlr.getStCd()).build();
		}).collect(Collectors.toList());
		return listData;

	}

	public List<ContactReportExecutionCoverageDto> reportExecutionBycoverage(String date) {
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
		return dealers.stream().map(dealer ->
			 ContactReportExecutionCoverageDto.builder()
					.dealerName(dealer.getDbaNm().trim())
					.dealerCode(dealer.getDlrCd().trim())
					 .type(dealer.getCRI().get(0).getContactType())
					 .author(dealer.getCRI().get(0).getContactAuthor())
					 .coverage(getCoverage(dealer.getCRI().get(0).getContactType()))
					.reportCount(reportCount.get(dealer.getDlrCd()))
					.authorDtos(dealer.getCRI().stream().map(contactReportInfo ->{
						List<ContactReportInfo> lists = authorContactReports.get(contactReportInfo.getContactAuthor());
						Function<String, Boolean> isExist = issueTopic -> lists.stream().filter(l-> l.getDiscussions().stream().filter(x->x.getTopic().equals(issueTopic)).findAny().isPresent()).findAny().isPresent();
						return ContactReportExecutionCoverageAuthorDto.builder()
								.author(contactReportInfo.getContactAuthor())
								.isDealerDefeciencyIdentified(isExist.apply("Dealer Dev Deficiencies Identifed"))
								.isServiceRetentionFysl(isExist.apply("Service Retention/FYSL"))
								.reportsCreatedByAuthor(lists.size())
								.build();
							}

					).collect(Collectors.toList()))
					.build()
		).collect(Collectors.toList());

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

		Map<String, String> dlrInfo = dealerService.findAll().stream()
				.collect(Collectors.toMap(Dealers::getDlrCd, Dealers::getDbaNm));

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
	
	public List<Map<String, String>> getSummaryByLocation(LocationFilter filter, String category) {
		List<Map<String, String>> finalListData = new ArrayList<>();
		List<ContactReportInfo> contactReports = contactInfoRepository.findByCurrentIssuesNotNull();
		Map<String, Map<String, List<ContactReportInfo>>> reports;
		if (filter.forLocation() == LocationEnum.DISTRICT) {
			reports = contactReports.stream()
					.filter(cr -> filter.getRegionCd().equalsIgnoreCase(cr.getDealers().getRgnCd()))
					.filter(cr -> filter.getZoneCd().equalsIgnoreCase(cr.getDealers().getZoneCd()))
					.filter(cr -> filter.getDistrictCd().equalsIgnoreCase(cr.getDealers().getDistrictCd()))
					.collect(Collectors.groupingBy(group -> group.getDealers().getDlrCd(), Collectors.groupingBy(gr -> gr.getCurrentIssues())));
		} else if (filter.forLocation() == LocationEnum.ZONE) {
			reports = contactReports.stream()
					.filter(cr -> filter.getRegionCd().equalsIgnoreCase(cr.getDealers().getRgnCd()))
					.filter(cr -> filter.getZoneCd().equalsIgnoreCase(cr.getDealers().getZoneCd()))
					.collect(Collectors.groupingBy(group -> group.getDealers().getDistrictCd(), Collectors.groupingBy(gr -> gr.getCurrentIssues())));
		} else if (filter.forLocation() == LocationEnum.DEALER) {
			reports = contactReports.stream().collect(Collectors.groupingBy(group -> group.getDealers().getDlrCd(), Collectors.groupingBy(gr -> gr.getCurrentIssues())));
		} else if (filter.forLocation() == LocationEnum.REGION) {
			reports = contactReports.stream().filter(cr -> filter.getRegionCd().equalsIgnoreCase(cr.getDealers().getRgnCd())).collect(Collectors.groupingBy(group -> group.getDealers().getZoneCd(), Collectors.groupingBy(gr -> gr.getCurrentIssues())));
		} else {
			reports = contactReports.stream().collect(Collectors.groupingBy(group -> group.getDealers().getRgnCd(), Collectors.groupingBy(gr -> gr.getCurrentIssues())));
		}
		
		reports.entrySet().forEach(entry -> {
			HashMap<String, List<ContactReportInfo>> finalData = new HashMap<>();
			HashMap<String, String> responseData = new HashMap<>();
			entry.getValue().entrySet().forEach(childEtry -> {
				Arrays.asList(childEtry.getKey().split("\\|")).forEach(issue -> {
					if (finalData.containsKey(issue)) {
						List<ContactReportInfo> existingData = finalData.get(issue);
						existingData.addAll(childEtry.getValue());
						finalData.put(issue, existingData);
					} else {
						finalData.put(issue, childEtry.getValue());
					}
				});
			});
			finalData.entrySet().stream().forEach(newEntry -> {
				Long submittedCount = newEntry.getValue().stream().filter(report -> report.getContactStatus() == ContactReportEnum.SUBMITTED.getStatusCode()).count();
				Long reviewedCount = newEntry.getValue().stream().filter(report -> report.getContactStatus() == ContactReportEnum.REVIEWED.getStatusCode()).count();
				if (submittedCount > 0 || reviewedCount > 0) {
					responseData.put(newEntry.getKey(), String.format("%d/%d", submittedCount, reviewedCount));
				}
			});
			responseData.put(getStringByType(filter.forLocation().name()), entry.getKey());
			finalListData.add(responseData);
		});
		
		return finalListData;
	}
	
	public List<Map<String, String>> getSummaryOfMonthByLocation(LocationFilter filter, String category) {
		List<Map<String, String>> finalListData = new ArrayList<>();
		List<ContactReportInfo> contactReports = contactInfoRepository.findByCurrentIssuesNotNullAndContactDtNotNull();
		contactReports.forEach(value -> System.out.println(value.getContactReportId() + ">" + value.getContactDt()));
		Map<String, Map<Object, List<ContactReportInfo>>> reports;
		if (filter.forLocation() == LocationEnum.DISTRICT) {
			reports = contactReports.stream()
					.filter(cr -> filter.getRegionCd().equalsIgnoreCase(cr.getDealers().getRgnCd()))
					.filter(cr -> filter.getZoneCd().equalsIgnoreCase(cr.getDealers().getZoneCd()))
					.filter(cr -> filter.getDistrictCd().equalsIgnoreCase(cr.getDealers().getDistrictCd()))
					.collect(Collectors.groupingBy(group -> group.getDealers().getDlrCd(), Collectors.groupingBy(gr -> formatDate(gr.getContactDt()))));
		} else if (filter.forLocation() == LocationEnum.ZONE) {
			reports = contactReports.stream()
					.filter(cr -> filter.getRegionCd().equalsIgnoreCase(cr.getDealers().getRgnCd()))
					.filter(cr -> filter.getZoneCd().equalsIgnoreCase(cr.getDealers().getZoneCd()))
					.collect(Collectors.groupingBy(group -> group.getDealers().getDistrictCd(), Collectors.groupingBy(gr -> formatDate(gr.getContactDt()))));
		} else if (filter.forLocation() == LocationEnum.DEALER) {
			reports = contactReports.stream().collect(Collectors.groupingBy(group -> group.getDealers().getDlrCd(), Collectors.groupingBy(gr -> formatDate(gr.getContactDt()))));
		} else if (filter.forLocation() == LocationEnum.REGION) {
			reports = contactReports.stream()
					.filter(cr -> filter.getRegionCd().equalsIgnoreCase(cr.getDealers().getRgnCd()))
					.collect(Collectors.groupingBy(group -> group.getDealers().getZoneCd(), Collectors.groupingBy(gr -> formatDate(gr.getContactDt()))));
		} else {
			reports = contactReports.stream().collect(Collectors.groupingBy(group -> group.getDealers().getRgnCd(), Collectors.groupingBy(gr -> formatDate(gr.getContactDt()))));
		}
		
		reports.entrySet().forEach(entry -> {
			HashMap<String, List<ContactReportInfo>> finalData = new HashMap<>();
			HashMap<String, String> responseData = new HashMap<>();
			entry.getValue().entrySet().forEach(childEtry -> {
				months.forEach(issue -> {
					if (finalData.containsKey(issue)) {
						List<ContactReportInfo> existingData = finalData.get(issue);
						existingData.addAll(childEtry.getValue());
						finalData.put(issue, existingData);
					} else if (issue.equalsIgnoreCase((String) childEtry.getKey())){
						finalData.put(issue, childEtry.getValue());
					} else {
						finalData.put(issue, new ArrayList<>());
					}
				});
			});
			finalData.entrySet().stream().forEach(newEntry -> {
				Long submittedCount = newEntry.getValue().stream().filter(report -> report.getContactStatus() == ContactReportEnum.SUBMITTED.getStatusCode()).count();
				Long reviewedCount = newEntry.getValue().stream().filter(report -> report.getContactStatus() == ContactReportEnum.REVIEWED.getStatusCode()).count();
				responseData.put(newEntry.getKey(), String.format("%d/%d", submittedCount, reviewedCount));
			});
			responseData.put(getStringByType(filter.forLocation().name()), entry.getKey());
			finalListData.add(responseData);
		});
		
		return finalListData;
	}

	private String formatDate(LocalDate contactDt) {
		return DateTimeFormatter.ofPattern("MMM").format(contactDt);
	}
}
