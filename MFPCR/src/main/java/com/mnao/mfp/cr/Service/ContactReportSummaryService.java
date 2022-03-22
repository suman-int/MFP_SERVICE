package com.mnao.mfp.cr.Service;


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

import java.time.LocalDate;
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

    private List<String> months = Arrays.asList("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec");

    Function<List<Dealers>, List<String>> extractDealerCodes = dealers -> dealers
            .stream().map(Dealers::getDlrCd)
            .collect(Collectors.toList());
    Function<String, List<Dealers>> filterByRegion = i -> dealerService.findAll().stream()
            .filter(dealers -> dealers.getRgnCd().equals(i))
            .collect(Collectors.toList());
    Function<String, List<Dealers>> filterByDistrict = i -> dealerService.findAll().stream()
            .filter(dealers -> dealers.getDistrictCd().equals(i))
            .collect(Collectors.toList());
    Function<String, List<Dealers>> filterByZone = i -> dealerService.findAll().stream()
            .filter(dealers -> dealers.getDistrictCd().equals(i))
            .collect(Collectors.toList());
    Function<String, List<Dealers>> filterByDealer = i -> dealerService.findAll().stream()
            .filter(dealers -> dealers.getDlrCd().equals(i))
            .collect(Collectors.toList());

    private Map<String, String> calcMetrics(List<ContactReportInfo> contactReportInfos,
                                                  List<String> issues,
                                                  String type,
                                                  String value,
                                                  TriFunction<List<ContactReportInfo>,String, Integer, BiPredicate<ContactReportInfo, Integer>> filtered) {
            Map<String, String> map = new HashMap<>();
            map.put(type, value);
            issues.stream().forEach(i -> {
                BiPredicate<ContactReportInfo, Integer> reviewPredicate = (contactReportInfo, statusCode) -> contactReportInfo.getContactStatus() == statusCode;
                BiPredicate<ContactReportInfo, Integer> exceptReviewPredicate = (contactReportInfo, statusCode) -> contactReportInfo.getContactStatus() != statusCode;
                long reviewedCount = filtered.discussionCount(contactReportInfos, i,ContactReportEnum.REVIEWED.getStatusCode(), reviewPredicate);
                long total = filtered.discussionCount(contactReportInfos, i,ContactReportEnum.REVIEWED.getStatusCode(), exceptReviewPredicate);

                map.put(i, String.format("%d/%d", reviewedCount, total));
            });
            return map;
    }

    private Map<String, String> calcMetrics(List<ContactReportInfo> contactReportInfos,
                                            List<String> issues,
                                            String type,
                                            String value,
                                            BiFunction<List<ContactReportInfo>, String, List<ContactReportInfo>> filtered) {
        Map<String, String> map = new HashMap<>();
        map.put(type, value);
        issues.stream().forEach(i -> {
            List<ContactReportInfo> list = filtered.apply(contactReportInfos, i);
            long reviewed = list.stream()
                    .filter(f -> f.getContactStatus() == ContactReportEnum.REVIEWED.getStatusCode())
                    .count();

            map.put(i, String.format("%d/%d", reviewed, list.size()));
        });
        return map;
    }


    public List<Map<String, String>> getSummaryByMonth(String type,
                                                       String value,
                                                       BiFunction<List<ContactReportInfo>, String, List<ContactReportInfo>> filteredByMonth) {
        List<Dealers> dealers = dealerService.findAll();
        List<ContactReportInfo> contactReportInfos;
        List<Map<String, String>> summaryList;
        if (type.equalsIgnoreCase(LocationEnum.DISTRICT.name())) {
            dealers = filterByDistrict.apply(value);
            Map<String, List<Dealers>> dealersByDealer = dealers.stream().collect(Collectors.groupingBy(Dealers::getDlrCd));
            Set<String> dealerSet = dealersByDealer.keySet();
            summaryList = dealerSet.stream().map(r -> {
                List<ContactReportInfo> contactReportInfoList = contactInfoRepository
                        .findByDlrCdInAndContactStatusNot(extractDealerCodes.apply(dealersByDealer.get(r)), ContactReportEnum.DRAFT.getStatusCode());
                return calcMetrics(contactReportInfoList, months, LocationEnum.DEALER.getLocationText(), r,filteredByMonth);
            }).collect(Collectors.toList());
        } else if (type.equalsIgnoreCase(LocationEnum.ZONE.name())) {
            dealers = filterByZone.apply(value);
            Map<String, List<Dealers>> dealersByDistrict = dealers.stream().collect(Collectors.groupingBy(Dealers::getDistrictCd));
            Set<String> districts = dealersByDistrict.keySet();
            summaryList = districts.stream().map(r -> {
                List<ContactReportInfo> contactReportInfoList = contactInfoRepository
                        .findByDlrCdInAndContactStatusNot(extractDealerCodes.apply(dealersByDistrict.get(r)), ContactReportEnum.DRAFT.getStatusCode());
                return calcMetrics(contactReportInfoList, months, LocationEnum.DISTRICT.getLocationText(), r,filteredByMonth);
            }).collect(Collectors.toList());
        } else if (type.equalsIgnoreCase(LocationEnum.REGION.name())) {
            dealers = filterByRegion.apply(value);
            Map<String, List<Dealers>> dealersByZone = dealers.stream().collect(Collectors.groupingBy(Dealers::getZoneCd));
            Set<String> zones = dealersByZone.keySet();
            summaryList = zones.stream().map(r -> {
                List<ContactReportInfo> contactReportInfoList = contactInfoRepository
                        .findByDlrCdInAndContactStatusNot(extractDealerCodes.apply(dealersByZone.get(r)), ContactReportEnum.DRAFT.getStatusCode());
                return calcMetrics(contactReportInfoList, months, LocationEnum.ZONE.getLocationText(), r,filteredByMonth);
            }).collect(Collectors.toList());
        } else if (type.equalsIgnoreCase(LocationEnum.DEALER.name())) {
            dealers = filterByDealer.apply(value);
            Map<String, List<Dealers>> dealersByRegion = dealers.stream().collect(Collectors.groupingBy(Dealers::getRgnCd));
            Set<String> regions = dealersByRegion.keySet();
            summaryList = regions.stream().map(r -> {
                List<ContactReportInfo> contactReportInfoList = contactInfoRepository
                        .findByDlrCdInAndContactStatusNot(extractDealerCodes.apply(dealersByRegion.get(r)), ContactReportEnum.DRAFT.getStatusCode());
                return calcMetrics(contactReportInfoList, months, LocationEnum.REGION.getLocationText(), r,filteredByMonth);
            }).collect(Collectors.toList());
        } else {
            Map<String, List<Dealers>> dealersByRegion = dealerService.findAll().stream().collect(Collectors.groupingBy(Dealers::getRgnCd));
            Set<String> regions = dealersByRegion.keySet();
            summaryList = regions.stream().map(r -> {
                List<ContactReportInfo> contactReportInfoList = contactInfoRepository
                        .findByDlrCdInAndContactStatusNot(extractDealerCodes.apply(dealersByRegion.get(r)), ContactReportEnum.DRAFT.getStatusCode());
                return calcMetrics(contactReportInfoList, months, LocationEnum.REGION.getLocationText(), r,filteredByMonth);
            }).collect(Collectors.toList());
        }
        return summaryList;
    }

    public List<Map<String, String>> getSummaryByLocation(String type,
                                                          String value,
                                                          String category,
                                                          TriFunction<List<ContactReportInfo>,String, Integer, BiPredicate<ContactReportInfo, Integer>> issueCount) {
        List<String> issueTypes = issueType.getIssuesByCategory().get(category.toLowerCase());
        List<Dealers> dealers;
        List<ContactReportInfo> contactReportInfos;
        List<Map<String, String>> summaryList;
        if (type.equalsIgnoreCase(LocationEnum.DISTRICT.name())) {
            dealers = filterByDistrict.apply(value);
            Map<String, List<Dealers>> dealersByDealer = dealers.stream().collect(Collectors.groupingBy(Dealers::getDlrCd));
            Set<String> dealerSet = dealersByDealer.keySet();
            summaryList = dealerSet.stream().map(r -> {
                List<ContactReportInfo> contactReportInfoList = contactInfoRepository
                        .findByDlrCdInAndContactStatusNot(extractDealerCodes.apply(dealersByDealer.get(r)), ContactReportEnum.DRAFT.getStatusCode());

                return calcMetrics(contactReportInfoList,
                        issueTypes, LocationEnum.DEALER.getLocationText(), r, issueCount);

            }).collect(Collectors.toList());
        } else if (type.equalsIgnoreCase(LocationEnum.ZONE.name())) {
            dealers = filterByZone.apply(value);
            Map<String, List<Dealers>> dealersByDistrict = dealers.stream().collect(Collectors.groupingBy(Dealers::getDistrictCd));
            Set<String> districts = dealersByDistrict.keySet();
            summaryList = districts.stream().map(r -> {
                List<ContactReportInfo> contactReportInfoList = contactInfoRepository
                        .findByDlrCdInAndContactStatusNot(extractDealerCodes.apply(dealersByDistrict.get(r)), ContactReportEnum.DRAFT.getStatusCode());
                return calcMetrics(contactReportInfoList, issueTypes,
                        LocationEnum.DISTRICT.getLocationText(), r, issueCount);
            }).collect(Collectors.toList());
        } else if (type.equalsIgnoreCase(LocationEnum.REGION.name())) {
            dealers = filterByRegion.apply(value);
            Map<String, List<Dealers>> dealersByZone = dealers.stream().collect(Collectors.groupingBy(Dealers::getZoneCd));
            Set<String> zones = dealersByZone.keySet();
            summaryList = zones.stream().map(r -> {
                List<ContactReportInfo> contactReportInfoList = contactInfoRepository
                        .findByDlrCdInAndContactStatusNot(extractDealerCodes.apply(dealersByZone.get(r)), ContactReportEnum.DRAFT.getStatusCode());
                return calcMetrics(contactReportInfoList, issueTypes,
                        LocationEnum.ZONE.getLocationText(), r, issueCount);
            }).collect(Collectors.toList());
        } else if (type.equalsIgnoreCase(LocationEnum.DEALER.name())) {
            dealers = filterByDealer.apply(value);
            Map<String, List<Dealers>> dealersByRegion = dealers.stream().collect(Collectors.groupingBy(Dealers::getRgnCd));
            Set<String> regions = dealersByRegion.keySet();
            summaryList = regions.stream().map(r -> {
                List<ContactReportInfo> contactReportInfoList = contactInfoRepository
                        .findByDlrCdInAndContactStatusNot(extractDealerCodes.apply(dealersByRegion.get(r)), ContactReportEnum.DRAFT.getStatusCode());
                return calcMetrics(contactReportInfoList, issueTypes,
                        LocationEnum.REGION.getLocationText(), r, issueCount);
            }).collect(Collectors.toList());
        } else {
            Map<String, List<Dealers>> dealersByRegion = dealerService.findAll().stream().collect(Collectors.groupingBy(Dealers::getRgnCd));
            Set<String> regions = dealersByRegion.keySet();
            summaryList = regions.stream().map(r -> {
                List<ContactReportInfo> contactReportInfoList = contactInfoRepository
                        .findByDlrCdInAndContactStatusNot(extractDealerCodes.apply(dealersByRegion.get(r)), ContactReportEnum.DRAFT.getStatusCode());
                return calcMetrics(contactReportInfoList, issueTypes,
                        LocationEnum.REGION.getLocationText(), r, issueCount);
            }).collect(Collectors.toList());
        }
        return summaryList;
    }

    public List<Map<String, Object>> summaryByCurrentStatus(String category){
        List<String> issueTypes = issueType.getIssuesByCategory().get(category.toLowerCase());
        List<ContactReportInfo> contactReportInfos = contactInfoRepository.findAll();
        return issueTypes.stream().map(issueType->{
            Map<String, Object> summaryMap = new HashMap<>();
            List<ContactReportInfo> contactReportInfoList = contactReportInfos.stream().filter(contactReportInfo -> {
                 Optional<ContactReportDiscussion> optionalContactReportDiscussion = contactReportInfo.getDiscussions()
                        .stream()
                        .filter(contactReportDiscussion ->
                            contactReportDiscussion.getDiscussion().equals(issueType)
                        )
                        .findAny();
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

    public List<Map<String, Object>> summaryByCurrentStatusDealershipList(String issue) {
        List<Dealers> dealers = dealerService.findAll();
        List<Map<String, String>> dealershipList;
        List<Map<String, String>> summaryList;

        List<ContactReportInfo> contactReportInfos = contactInfoRepository.findAll().stream().filter(contactReportInfo -> {
            Optional<ContactReportDiscussion> optionalContactReportDiscussion = contactReportInfo.getDiscussions()
                    .stream()
                    .filter(contactReportDiscussion -> contactReportDiscussion.getDiscussion().equals(issue))
                    .findAny();
            return optionalContactReportDiscussion.isPresent();
        }).collect(Collectors.toList());

        List<String> dlrCdList = contactReportInfos.stream().map(contactReportInfo -> contactReportInfo.getDlrCd()).collect(Collectors.toList());

        return dealers.stream().filter(dealer -> dlrCdList.contains(dealer.getDlrCd())).map(dlr -> {
            Map<String, Object> summaryMap = new HashMap<>();

            summaryMap.put("dealerCode" ,dlr.getDlrCd());
            summaryMap.put("dealerName" ,dlr.getDbaNm());
            summaryMap.put("stateName" ,dlr.getStCd());
            summaryMap.put("cityName",dlr.getCityNm());
            summaryMap.put("zipCode" ,dlr.getZipCd());
            summaryMap.put("issue" ,issue);

            return summaryMap;
        }).collect(Collectors.toList());
    }

    public List<Map<String, Object>> reportExecutionBycoverage(String date) {
        LocalDate startDate = LocalDate.parse(date).withDayOfMonth(1);
        LocalDate endDate = startDate.withDayOfMonth(startDate.lengthOfMonth());

        List<Dealers> dealers = dealerService.findAll();

        List<ContactReportInfo> contactReportInfos = contactInfoRepository.findByContactDtBetween(startDate, endDate);

        List<String> dlrCdList = contactReportInfos.stream().map(
                contactReportInfo -> contactReportInfo.getDlrCd()).collect(Collectors.toList());

        Map<String, Long> reportCount = dlrCdList.stream().collect(Collectors.groupingBy(e -> e, Collectors.counting()));

        return dealers.stream().filter(dealer -> dlrCdList.contains(dealer.getDlrCd())).map(dlr -> {
            Map<String, Object> summaryMap = new HashMap<>();
            for (ContactReportInfo cr:contactReportInfos)
            {
                if(cr.getDlrCd().equals(dlr.getDlrCd()))
                {
                    summaryMap.put("type", cr.getContactType());
                    summaryMap.put("author", cr.getContactAuthor());
                    String contactType = cr.getContactType();
                    if(contactType.equalsIgnoreCase("Sales | Service") || contactType.equalsIgnoreCase("Sales | Service | Other"))
                        summaryMap.put("coverage", "100");
                    else summaryMap.put("coverage","50");
                }
            }
            summaryMap.put("reportCount", reportCount.get(dlr.getDlrCd()));
            summaryMap.put("dealerCode" ,dlr.getDlrCd());
            summaryMap.put("dealerName" ,dlr.getDbaNm());
            return summaryMap;
        }).collect(Collectors.toList());
    }

    public List<Map<String, List<Object>>> reportExecutionByException(String date) {
        LocalDate startDate = LocalDate.parse(date).withDayOfMonth(1);
        LocalDate endDate = startDate.withDayOfMonth(startDate.lengthOfMonth());

        Map<String, String> dlrInfo = dealerService.findAll().stream().collect(Collectors.toMap(Dealers::getDlrCd,Dealers::getDbaNm));

        List<ContactReportInfo> contactReportInfos = contactInfoRepository.findByContactDtBetween(startDate, endDate);

//        Map<String, List<Object>> finalsummaryMap = new HashMap<>();
        Map<String, List<Object>> SummaryMap = new HashMap<>();
        return contactReportInfos.stream().map(cr -> {
            if (SummaryMap.containsKey(cr.getDlrCd())) {
                List<Object> obj = SummaryMap.get(cr.getDlrCd());
                List<Object> ob1 = new ArrayList<>();
                ob1.add(cr.getDlrCd());
                ob1.add(cr.getContactReportId());
                ob1.add(cr.getContactAuthor());
                obj.add(ob1);
                SummaryMap.put(cr.getDlrCd(),obj);
            }
            else{
                List<Object> ob1 = new ArrayList<>();
                ob1.add(cr.getDlrCd());
                ob1.add(cr.getContactReportId());
                ob1.add(cr.getContactAuthor());
               SummaryMap.put(cr.getDlrCd(),ob1);
            }
            return SummaryMap;
    }).collect(Collectors.toList());
}
}
