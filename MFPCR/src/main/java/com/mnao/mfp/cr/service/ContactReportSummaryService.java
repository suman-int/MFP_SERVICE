package com.mnao.mfp.cr.service;

import static com.mnao.mfp.common.util.AppConstants.MONTHS_LIST;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mnao.mfp.common.datafilters.FilterCriteria;
import com.mnao.mfp.common.util.AppConstants;
import com.mnao.mfp.common.util.IsActiveEnum;
import com.mnao.mfp.cr.dto.ContactReportExecutionCoverageAuthorDto;
import com.mnao.mfp.cr.dto.ContactReportExecutionCoverageDto;
import com.mnao.mfp.cr.dto.SummaryByContactStatusDto;
import com.mnao.mfp.cr.dto.SummaryByDealerListDto;
import com.mnao.mfp.cr.entity.ContactReportDiscussion;
import com.mnao.mfp.cr.entity.ContactReportInfo;
import com.mnao.mfp.cr.entity.Dealers;
import com.mnao.mfp.cr.repository.ContactInfoRepository;
import com.mnao.mfp.cr.util.ContactReportEnum;
import com.mnao.mfp.cr.util.DataOperationFilter;
import com.mnao.mfp.cr.util.IssueType;
import com.mnao.mfp.cr.util.LocationEnum;
import com.mnao.mfp.cr.util.TriFunction;
import com.mnao.mfp.user.dao.MFPUser;

@Service
@Transactional(readOnly = true)
public class ContactReportSummaryService {

    @Autowired
    private ContactInfoRepository contactInfoRepository;

    @Autowired
    private IssueType issueType;

    @Autowired
    private DealerService dealerService;

    @Autowired
    private DataOperationFilter dataOperationFilter;

    Function<List<Dealers>, List<String>> extractDealerCodes = dealers -> dealers.stream().map(Dealers::getDlrCd).collect(Collectors.toList());
    Function<String, List<Dealers>> filterByRegion = i -> dealerService.findAll().stream().filter(dealers -> dealers.getRgnCd().equals(i)).collect(Collectors.toList());
    Function<String, List<Dealers>> filterByDistrict = i -> dealerService.findAll().stream().filter(dealers -> dealers.getDistrictCd().equals(i)).collect(Collectors.toList());
    Function<String, List<Dealers>> filterByZone = i -> dealerService.findAll().stream().filter(dealers -> dealers.getDistrictCd().equals(i)).collect(Collectors.toList());
    Function<String, List<Dealers>> filterByDealer = i -> dealerService.findAll().stream().filter(dealers -> dealers.getDlrCd().equals(i)).collect(Collectors.toList());

    private Map<String, String> calcMetrics(List<ContactReportInfo> contactReportInfos, List<String> issues, String type, String value, TriFunction<List<ContactReportInfo>, String, Integer, BiPredicate<ContactReportInfo, Integer>> filtered) {
        Map<String, String> map = new HashMap<>();
        map.put(type, value);
        issues.forEach(issue -> {
            BiPredicate<ContactReportInfo, Integer> reviewPredicate = (contactReportInfo, statusCode) -> contactReportInfo.getContactStatus() == statusCode;
            BiPredicate<ContactReportInfo, Integer> exceptReviewPredicate = (contactReportInfo, statusCode) -> contactReportInfo.getContactStatus() != statusCode;
            long reviewedCount = filtered.discussionCount(contactReportInfos, issue, ContactReportEnum.REVIEWED.getStatusCode(), reviewPredicate);
            long total = filtered.discussionCount(contactReportInfos, issue, ContactReportEnum.SUBMITTED.getStatusCode(), exceptReviewPredicate);

            map.put(issue, String.format(AppConstants.DOUBLE_INT_PERCENT, reviewedCount, total));
        });
        return map;
    }

    private Map<String, String> calcMetrics(List<ContactReportInfo> contactReportInfos, List<String> issues, String type, String value, BiFunction<List<ContactReportInfo>, String, List<ContactReportInfo>> filtered) {
        Map<String, String> map = new HashMap<>();
        map.put(type, value);
        issues.forEach(issue -> {
            List<ContactReportInfo> list = filtered.apply(contactReportInfos, issue);
            long reviewed = list.stream().filter(f -> f.getContactStatus() == ContactReportEnum.REVIEWED.getStatusCode()).count();

            map.put(issue, String.format(AppConstants.DOUBLE_INT_PERCENT, reviewed, list.size()));
        });
        return map;
    }

    public List<Map<String, String>> getSummaryByMonth(String type, String value, BiFunction<List<ContactReportInfo>, String, List<ContactReportInfo>> filteredByMonth) {
        List<Dealers> dealers;
        List<Map<String, String>> summaryList;
        if (type.equalsIgnoreCase(LocationEnum.DISTRICT.name())) {
            dealers = filterByDistrict.apply(value);
            Map<String, List<Dealers>> dealersByDealer = dealers.stream().collect(Collectors.groupingBy(Dealers::getDlrCd));
            Set<String> dealerSet = dealersByDealer.keySet();
            summaryList = dealerSet.stream().map(r -> {
                List<ContactReportInfo> contactReportInfoList = contactInfoRepository.findByDlrCdInAndContactStatusNotAndIsActiveAndContactDtBetween(extractDealerCodes.apply(dealersByDealer.get(r)), ContactReportEnum.DRAFT.getStatusCode(), IsActiveEnum.YES.getValue(), AppConstants.MIN_DB_DATE, AppConstants.MAX_DB_DATE);
                return calcMetrics(contactReportInfoList, MONTHS_LIST, LocationEnum.DEALER.getLocationText(), r, filteredByMonth);
            }).collect(Collectors.toList());
        } else if (type.equalsIgnoreCase(LocationEnum.ZONE.name())) {
            dealers = filterByZone.apply(value);
            Map<String, List<Dealers>> dealersByDistrict = dealers.stream().collect(Collectors.groupingBy(Dealers::getDistrictCd));
            Set<String> districts = dealersByDistrict.keySet();
            summaryList = districts.stream().map(r -> {
                List<ContactReportInfo> contactReportInfoList = contactInfoRepository.findByDlrCdInAndContactStatusNotAndIsActiveAndContactDtBetween(extractDealerCodes.apply(dealersByDistrict.get(r)), ContactReportEnum.DRAFT.getStatusCode(), IsActiveEnum.YES.getValue(), AppConstants.MIN_DB_DATE, AppConstants.MAX_DB_DATE);
                return calcMetrics(contactReportInfoList, MONTHS_LIST, LocationEnum.DISTRICT.getLocationText(), r, filteredByMonth);
            }).collect(Collectors.toList());
        } else if (type.equalsIgnoreCase(LocationEnum.REGION.name())) {
            dealers = filterByRegion.apply(value);
            Map<String, List<Dealers>> dealersByZone = dealers.stream().collect(Collectors.groupingBy(Dealers::getZoneCd));
            Set<String> zones = dealersByZone.keySet();
            summaryList = zones.stream().map(r -> {
                List<ContactReportInfo> contactReportInfoList = contactInfoRepository.findByDlrCdInAndContactStatusNotAndIsActiveAndContactDtBetween(extractDealerCodes.apply(dealersByZone.get(r)), ContactReportEnum.DRAFT.getStatusCode(), IsActiveEnum.YES.getValue(), AppConstants.MIN_DB_DATE, AppConstants.MAX_DB_DATE);
                return calcMetrics(contactReportInfoList, MONTHS_LIST, LocationEnum.ZONE.getLocationText(), r, filteredByMonth);
            }).collect(Collectors.toList());
        } else if (type.equalsIgnoreCase(LocationEnum.DEALER.name())) {
            dealers = filterByDealer.apply(value);
            Map<String, List<Dealers>> dealersByRegion = dealers.stream().collect(Collectors.groupingBy(Dealers::getRgnCd));
            Set<String> regions = dealersByRegion.keySet();
            summaryList = regions.stream().map(r -> {
                List<ContactReportInfo> contactReportInfoList = contactInfoRepository.findByDlrCdInAndContactStatusNotAndIsActiveAndContactDtBetween(extractDealerCodes.apply(dealersByRegion.get(r)), ContactReportEnum.DRAFT.getStatusCode(), IsActiveEnum.YES.getValue(), AppConstants.MIN_DB_DATE, AppConstants.MAX_DB_DATE);
                return calcMetrics(contactReportInfoList, MONTHS_LIST, LocationEnum.REGION.getLocationText(), r, filteredByMonth);
            }).collect(Collectors.toList());
        } else {
            Map<String, List<Dealers>> dealersByRegion = dealerService.findAll().stream().collect(Collectors.groupingBy(Dealers::getRgnCd));
            Set<String> regions = dealersByRegion.keySet();
            summaryList = regions.stream().map(r -> {
                List<ContactReportInfo> contactReportInfoList = contactInfoRepository.findByDlrCdInAndContactStatusNotAndIsActiveAndContactDtBetween(extractDealerCodes.apply(dealersByRegion.get(r)), ContactReportEnum.DRAFT.getStatusCode(), IsActiveEnum.YES.getValue(), AppConstants.MIN_DB_DATE, AppConstants.MAX_DB_DATE);
                return calcMetrics(contactReportInfoList, MONTHS_LIST, LocationEnum.REGION.getLocationText(), r, filteredByMonth);
            }).collect(Collectors.toList());
        }
        return summaryList;
    }

    public List<Map<String, String>> getSummaryByLocation(String type, String value, String category, MFPUser mfpUser, TriFunction<List<ContactReportInfo>, String, Integer, BiPredicate<ContactReportInfo, Integer>> issueCount) {
        List<Map<String, String>> finalListData = new ArrayList<>();
        List<ContactReportInfo> contactReports = contactInfoRepository.findByCurrentIssuesNotNullAndIsActiveAndContactDtBetween(IsActiveEnum.YES.getValue(), AppConstants.MIN_DB_DATE, AppConstants.MAX_DB_DATE);
        Map<String, Map<String, List<ContactReportInfo>>> reports;
        if (type.equalsIgnoreCase(LocationEnum.DISTRICT.name())) {
            reports = contactReports.stream().collect(Collectors.groupingBy(group -> group.getDealers().getDlrCd(), Collectors.groupingBy(ContactReportInfo::getCurrentIssues)));
        } else if (type.equalsIgnoreCase(LocationEnum.ZONE.name())) {
            reports = contactReports.stream().collect(Collectors.groupingBy(group -> group.getDealers().getDistrictCd(), Collectors.groupingBy(ContactReportInfo::getCurrentIssues)));
        } else if (type.equalsIgnoreCase(LocationEnum.DEALER.name())) {
            reports = contactReports.stream().collect(Collectors.groupingBy(group -> group.getDealers().getDlrCd(), Collectors.groupingBy(ContactReportInfo::getCurrentIssues)));
        } else if (type.equalsIgnoreCase(LocationEnum.REGION.name())) {
            reports = dataOperationFilter.filterContactReportByRegion(contactReports, value, mfpUser).collect(Collectors.groupingBy(group -> group.getDealers().getZoneCd(), Collectors.groupingBy(ContactReportInfo::getCurrentIssues)));
        } else {
            reports = contactReports.stream().collect(Collectors.groupingBy(group -> group.getDealers().getRgnCd(), Collectors.groupingBy(ContactReportInfo::getCurrentIssues)));
        }

        reports.forEach((key, value1) -> {
            HashMap<String, List<ContactReportInfo>> finalData = new HashMap<>();
            HashMap<String, String> responseData = new HashMap<>();
            value1.forEach((key1, value2) -> Arrays.asList(key1.split("\\|")).forEach(issue -> finalData.put(issue, value2)));
            finalData.forEach((key1, value2) -> {
                Long submittedCount = value2.stream().filter(report -> report.getContactStatus() == ContactReportEnum.SUBMITTED.getStatusCode()).count();
                Long reviewedCount = value2.stream().filter(report -> report.getContactStatus() == ContactReportEnum.REVIEWED.getStatusCode()).count();
                responseData.put(key1, String.format(AppConstants.DOUBLE_INT_PERCENT, submittedCount, reviewedCount));
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
        return issueTypes.stream().map(type -> {
            Map<String, Object> summaryMap = new HashMap<>();
            Map<String, Long> stCntMap = new HashMap<>();
            List<ContactReportInfo> contactReportInfoList = contactReportInfos.stream().filter(contactReportInfo -> {
                Optional<ContactReportDiscussion> optionalContactReportDiscussion = contactReportInfo.getDiscussions().stream().filter(contactReportDiscussion -> contactReportDiscussion.getTopic() != null).filter(contactReportDiscussion -> contactReportDiscussion.getTopic().equals(type)).findAny();
                return optionalContactReportDiscussion.isPresent();
            }).collect(Collectors.toList());
            contactReportInfoList.forEach(crList -> {
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
            summaryMap.put("completed", stCntMap.getOrDefault(ContactReportEnum.REVIEWED.getDisplayText(), 0L));
            summaryMap.put("pendingReview", stCntMap.getOrDefault(ContactReportEnum.DISCUSSION_REQUESTED.getDisplayText(), 0L) + stCntMap.getOrDefault(ContactReportEnum.SUBMITTED.getDisplayText(), 0L));
            summaryMap.put("total", stCntMap.getOrDefault(ContactReportEnum.DISCUSSION_REQUESTED.getDisplayText(), 0L) + stCntMap.getOrDefault(ContactReportEnum.SUBMITTED.getDisplayText(), 0L) + stCntMap.getOrDefault(ContactReportEnum.REVIEWED.getDisplayText(), 0L)
//							+ stCntMap.getOrDefault(ContactReportEnum.COMPLETED.getDisplayText(), 0L)
                    + stCntMap.getOrDefault(ContactReportEnum.DRAFT.getDisplayText(), 0L));
            summaryMap.put("issue", type);
            return summaryMap;
        }).collect(Collectors.toList());

    }

    //
    // BACKUP of summaryByCurrentStatus
    //
    public List<Map<String, Object>> summaryByCurrentStatusBAK(String category) {
        List<String> issueTypes = issueType.getIssuesByCategory().get(category.toLowerCase());
        List<ContactReportInfo> contactReportInfos = contactInfoRepository.findAll();
        return issueTypes.stream().map(type -> {
            Map<String, Object> summaryMap = new HashMap<>();
            List<ContactReportInfo> contactReportInfoList = contactReportInfos.stream().filter(contactReportInfo -> {
                Optional<ContactReportDiscussion> optionalContactReportDiscussion = contactReportInfo.getDiscussions().stream().filter(contactReportDiscussion ->
                        contactReportDiscussion.getTopic().equals(type)).findAny();
                return optionalContactReportDiscussion.isPresent();
            }).collect(Collectors.toList());
            long requiredReportCompletion = contactReportInfoList.stream().filter(contactReportInfo -> contactReportInfo.getContactStatus() == ContactReportEnum.REVIEWED.getStatusCode()).count();
            long notStarted = contactReportInfoList.stream().filter(contactReportInfo -> contactReportInfo.getContactStatus() == ContactReportEnum.REVIEWED.getStatusCode()).count();
            long drafts = contactReportInfoList.stream().filter(contactReportInfo -> contactReportInfo.getContactStatus() == ContactReportEnum.DRAFT.getStatusCode()).count();
            long pendingReview = contactReportInfoList.stream().filter(contactReportInfo -> contactReportInfo.getContactStatus() != ContactReportEnum.REVIEWED.getStatusCode()).count();
            summaryMap.put("requiredReportCompletion", requiredReportCompletion);
            summaryMap.put("notStarted", notStarted);
            summaryMap.put("drafts", drafts);
            summaryMap.put("pendingReview", pendingReview);
            summaryMap.put("issue", issueType);
            return summaryMap;
        }).collect(Collectors.toList());

    }

    public List<SummaryByDealerListDto> summaryByCurrentStatusDealershipList(String issue) {
        List<ContactReportInfo> contactReportInfoList = contactInfoRepository.findByCurrentIssuesContainingAndIsActiveAndContactDtBetween(issue, IsActiveEnum.YES.getValue(), AppConstants.MIN_DB_DATE, AppConstants.MAX_DB_DATE);
        List<Dealers> dealerList = contactReportInfoList.stream().map(ContactReportInfo::getDealers).filter(Objects::nonNull).collect(Collectors.toList());

        return dealerList.stream().map(dlr -> SummaryByDealerListDto.builder().cityName(dlr.getCityNm()).dealerCode(dlr.getDlrCd()).dealerName(dlr.getDbaNm()).issue(issue).zipCode(dlr.getZipCd()).stateName(dlr.getStCd()).build()).collect(Collectors.toList());

    }

    public List<ContactReportExecutionCoverageDto> reportExecutionBycoverageBAK(String date) {
        LocalDate startDate;
        try {
            startDate = LocalDate.parse(date).withDayOfMonth(1);
        } catch (Exception e) {
            throw new IllegalArgumentException("The date format should be " + AppConstants.LOCALDATE_FORMAT);
        }
        LocalDate endDate = startDate.withDayOfMonth(startDate.lengthOfMonth());

        List<ContactReportInfo> contactReportInfos = contactInfoRepository.findByContactDtBetweenAndIsActive(startDate, endDate, IsActiveEnum.YES.getValue());

        Map<String, Long> reportCount = contactReportInfos.stream().map(ContactReportInfo::getDlrCd).collect(Collectors.groupingBy(e -> e, Collectors.counting()));

        Set<Dealers> dealers = contactReportInfos.stream().map(ContactReportInfo::getDealers).collect(Collectors.toSet());
        Map<String, List<ContactReportInfo>> authorContactReports = contactReportInfos.stream().collect(Collectors.groupingBy(ContactReportInfo::getContactAuthor));
        return dealers.stream().map(dealer -> ContactReportExecutionCoverageDto.builder().dealerName(dealer.getDbaNm().trim()).dealerCode(dealer.getDlrCd().trim()).type(dealer.getCRI().get(0).getContactType()).author(dealer.getCRI().get(0).getContactAuthor()).coverage(getCoverage(dealer.getCRI().get(0).getContactType())).reportCount(reportCount.get(dealer.getDlrCd())).authorDtos(dealer.getCRI().stream().map(contactReportInfo -> {
                    List<ContactReportInfo> lists = authorContactReports.get(contactReportInfo.getContactAuthor());
                    Function<String, Boolean> isExist = issueTopic -> lists.stream().anyMatch(l -> l.getDiscussions().stream().anyMatch(x -> x.getTopic().equals(issueTopic)));
                    return ContactReportExecutionCoverageAuthorDto.builder().author(contactReportInfo.getContactAuthor()).isDealerDefeciencyIdentified(isExist.apply("Dealer Dev Deficiencies Identifed")).isServiceRetentionFysl(isExist.apply("Service Retention/FYSL")).reportsCreatedByAuthor(lists.size()).build();
                }

        ).collect(Collectors.toList())).build()).collect(Collectors.toList());

    }

    private String getCoverage(String contactType) {
        if (contactType.equalsIgnoreCase("Sales | Service") || contactType.equalsIgnoreCase("Sales | Service | Other"))
            return "100";
        else return "50";
    }

    public List<Map<String, List<Object>>> reportExecutionByException(String date) {
        LocalDate startDate;
        try {
            startDate = LocalDate.parse(date).withDayOfMonth(1);
        } catch (Exception e) {
            throw new IllegalArgumentException("The date format should be " + AppConstants.LOCALDATE_FORMAT);
        }
        LocalDate endDate = startDate.withDayOfMonth(startDate.lengthOfMonth());

        List<ContactReportInfo> contactReportInfos = contactInfoRepository.findByContactDtBetweenAndIsActive(startDate, endDate, IsActiveEnum.YES.getValue());

        Map<String, List<Object>> summaryMap = new HashMap<>();
        return contactReportInfos.stream().map(cr -> {
            if (summaryMap.containsKey(cr.getDlrCd())) {
                List<Object> obj = summaryMap.get(cr.getDlrCd());
                List<Object> ob1 = new ArrayList<>();
                ob1.add(cr.getDlrCd());
                ob1.add(cr.getContactReportId());
                ob1.add(cr.getContactAuthor());
                obj.add(ob1);
                summaryMap.put(cr.getDlrCd(), obj);
            } else {
                List<Object> ob1 = new ArrayList<>();
                ob1.add(cr.getDlrCd());
                ob1.add(cr.getContactReportId());
                ob1.add(cr.getContactAuthor());
                summaryMap.put(cr.getDlrCd(), ob1);
            }
            return summaryMap;
        }).collect(Collectors.toList());
    }


    public List<SummaryByContactStatusDto> filterSummaryByCurrentStatusUsingIssues(FilterCriteria filter, MFPUser mfpUser) {
        List<SummaryByContactStatusDto> finalListData = new ArrayList<>();
        List<ContactReportInfo> contactReports = contactInfoRepository.findByCurrentIssuesNotNullAndIsActiveAndContactDtBetween(IsActiveEnum.YES.getValue(), filter.getStartDate(), filter.getEndDate());
        contactReports = dataOperationFilter.filterContactReportsByLocation(filter, contactReports, mfpUser);
        Map<String, List<ContactReportInfo>> finalData = new HashMap<>();
        contactReports.forEach(cr -> {
        	 Arrays.asList(cr.getCurrentIssues().split("\\|")).forEach(issue -> {
        		 List<ContactReportInfo> tempCr = new ArrayList<>();
                 if (finalData.containsKey(issue)) {
                     List<ContactReportInfo> existingData = finalData.get(issue);
                     existingData.add(cr);
                     finalData.put(issue, existingData);
                 } else {
                	 tempCr.add(cr);
                     finalData.put(issue, tempCr);
                 }
             });
        });
        finalData.forEach((key, value) -> {
            Map<Integer, List<ContactReportInfo>> mapValue = value.stream().collect(Collectors.groupingBy(ContactReportInfo::getContactStatus));
            long submittedCount = mapValue.containsKey(ContactReportEnum.SUBMITTED.getStatusCode()) ? mapValue.get(ContactReportEnum.SUBMITTED.getStatusCode()).size() : 0L;
            long reviewedCount = mapValue.containsKey(ContactReportEnum.REVIEWED.getStatusCode()) ? mapValue.get(ContactReportEnum.REVIEWED.getStatusCode()).size() : 0L;
            long draftCount = mapValue.containsKey(ContactReportEnum.DRAFT.getStatusCode()) ? mapValue.get(ContactReportEnum.DRAFT.getStatusCode()).size() : 0L;
            long discussionReqCount = mapValue.containsKey(ContactReportEnum.DISCUSSION_REQUESTED.getStatusCode()) ? mapValue.get(ContactReportEnum.DISCUSSION_REQUESTED.getStatusCode()).size() : 0L;
            finalListData.add(SummaryByContactStatusDto.builder().issue(key).draftCount(draftCount).pendingReviewCount(submittedCount + discussionReqCount).reviewCount(reviewedCount).total(draftCount + submittedCount + discussionReqCount + reviewedCount).build());
        });

        return finalListData;
    }


    /**
     * Summary By Current Status
     * @param filterCriteria FilterCriteria
     * @param mfpUser MFPUser
     * @return
     */
    public List<SummaryByDealerListDto> summaryByCurrentStatusOfDealershipList(FilterCriteria filterCriteria, MFPUser mfpUser) {
        String issue = filterCriteria.getIssuesFilter().get(0);
        List<ContactReportInfo> contactReportInfoList = contactInfoRepository.findByCurrentIssuesContainingAndIsActiveAndContactDtBetween(issue, IsActiveEnum.YES.getValue(), AppConstants.MIN_DB_DATE, AppConstants.MAX_DB_DATE);
        contactReportInfoList = dataOperationFilter.filterContactReportsByLocation(filterCriteria, contactReportInfoList, mfpUser);
        Set<Dealers> dealerList = contactReportInfoList.stream().map(ContactReportInfo::getDealers).collect(Collectors.toSet());
        return dealerList.stream().map(dlr -> SummaryByDealerListDto.builder().cityName(dlr.getCityNm()).dealerCode(dlr.getDlrCd()).dealerName(dlr.getDbaNm()).issue(issue).zipCode(dlr.getZipCd()).stateName(dlr.getStCd()).build()).collect(Collectors.toList());

    }

    public List<ContactReportExecutionCoverageDto> reportExecutionCoverageByReportTime(String date) {
        LocalDate startDate;
        try {
            startDate = LocalDate.parse(date).withDayOfMonth(1);
        } catch (Exception e) {
            throw new IllegalArgumentException("The date format should be " + AppConstants.LOCALDATE_FORMAT);
        }
        LocalDate endDate = startDate.withDayOfMonth(startDate.lengthOfMonth());
        List<ContactReportExecutionCoverageDto> contactReportExecCoverageList = new ArrayList<>(0);
        List<ContactReportInfo> contactReportInfos = contactInfoRepository.findByContactDtBetweenAndContactStatusGreaterThanAndIsActive(startDate, endDate, ContactReportEnum.DRAFT.getStatusCode(), IsActiveEnum.YES.getValue());
        Map<Dealers, Map<String, List<ContactReportInfo>>> reportMap = contactReportInfos.stream().collect(Collectors.groupingBy(ContactReportInfo::getDealers, Collectors.groupingBy(ContactReportInfo::getContactAuthor)));

        reportMap.forEach((key, value) -> {
            List<ContactReportExecutionCoverageAuthorDto> authorDetailsDto = new ArrayList<>(0);
            value.forEach((key1, value1) -> {
                boolean isDealerDefeciencyIdentified = value1.stream().anyMatch(cr -> cr.getCurrentIssues().contains("Dealer Dev Deficiencies Identifed"));
                boolean isServiceRetentionFysl = value1.stream().anyMatch(cr -> cr.getCurrentIssues().contains("Service Retention/FYSL"));
                authorDetailsDto.add(ContactReportExecutionCoverageAuthorDto.builder().author(key1).reportCount(value1.size()).isDealerDefeciencyIdentified(isDealerDefeciencyIdentified).isServiceRetentionFysl(isServiceRetentionFysl).isSales(value1.stream().anyMatch(cr -> cr.getContactType().contains("SALES"))).isOthers(value1.stream().anyMatch(cr -> cr.getContactType().contains("SERVICE"))).isService(value1.stream().anyMatch(cr -> cr.getContactType().contains("OTHER"))).coveredBySalesService(value1.stream().anyMatch(cr -> (cr.getContactType().equalsIgnoreCase("SALES,SERVICE") || cr.getContactType().equalsIgnoreCase("SALES,SERVICE,OTHER")))).build());

            });

            contactReportExecCoverageList.add(ContactReportExecutionCoverageDto.builder().dealerCode(key.getDlrCd()).dealerName(key.getDbaNm()).reportCount(authorDetailsDto.stream().mapToLong(ContactReportExecutionCoverageAuthorDto::getReportCount).sum()).authorDtos(authorDetailsDto).author(authorDetailsDto.stream().map(ContactReportExecutionCoverageAuthorDto::getAuthor).collect(Collectors.joining(","))).isDealerDefeciencyIdentified(authorDetailsDto.stream().anyMatch(ContactReportExecutionCoverageAuthorDto::isDealerDefeciencyIdentified)).isServiceRetentionFysl(authorDetailsDto.stream().anyMatch(ContactReportExecutionCoverageAuthorDto::isServiceRetentionFysl)).coverage(authorDetailsDto.stream().anyMatch(ContactReportExecutionCoverageAuthorDto::isCoveredBySalesService) ? "100" : "50").isSales(authorDetailsDto.stream().anyMatch(ContactReportExecutionCoverageAuthorDto::isSales)).isOthers(authorDetailsDto.stream().anyMatch(ContactReportExecutionCoverageAuthorDto::isOthers)).isService(authorDetailsDto.stream().anyMatch(ContactReportExecutionCoverageAuthorDto::isService)).build());
        });
        return contactReportExecCoverageList;
    }
}
