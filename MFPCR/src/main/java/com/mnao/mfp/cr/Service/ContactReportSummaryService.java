package com.mnao.mfp.cr.Service;


import com.mnao.mfp.cr.entity.ContactReportInfo;
import com.mnao.mfp.cr.entity.Dealers;
import com.mnao.mfp.cr.repository.ContactInfoRepository;
import com.mnao.mfp.cr.util.ContactReportEnum;
import com.mnao.mfp.cr.util.IssueType;
import com.mnao.mfp.cr.util.LocationEnum;
import com.mnao.mfp.cr.util.TriFunction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.Predicate;
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
        //return contactReportInfos.stream().map(contactReportInfo -> {
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
        //}).collect(Collectors.toList());
    }

//    public List<Map<String, String>> getSummaryByMonth(String type,
//                                                       String value,
//                                                       BiFunction<List<ContactReportInfo>, String, List<ContactReportInfo>> filteredByMonth) {
//        List<Dealers> dealers = dealerService.findAll();
//        List<ContactReportInfo> contactReportInfos;
//        List<Map<String, String>> summaryList;
//        if (type.equalsIgnoreCase(LocationEnum.DISTRICT.name())) {
//            dealers = filterByDistrict.apply(value);
//            Map<String, List<Dealers>> dealersByDealer = dealers.stream().collect(Collectors.groupingBy(Dealers::getDlrCd));
//            Set<String> dealerSet = dealersByDealer.keySet();
//            summaryList = dealerSet.stream().map(r -> {
//                List<ContactReportInfo> contactReportInfoList = contactInfoRepository
//                        .findByDlrCdInAndContactStatusNot(extractDealerCodes.apply(dealersByDealer.get(r)), ContactReportEnum.DRAFT.getStatusCode());
//                return calcMetrics(contactReportInfoList, months, LocationEnum.DEALER.getLocationText(), r,filteredByMonth);
//            }).collect(Collectors.toList());
//        } else if (type.equalsIgnoreCase(LocationEnum.ZONE.name())) {
//            dealers = filterByZone.apply(value);
//            Map<String, List<Dealers>> dealersByDistrict = dealers.stream().collect(Collectors.groupingBy(Dealers::getDistrictCd));
//            Set<String> districts = dealersByDistrict.keySet();
//            summaryList = districts.stream().map(r -> {
//                List<ContactReportInfo> contactReportInfoList = contactInfoRepository
//                        .findByDlrCdInAndContactStatusNot(extractDealerCodes.apply(dealersByDistrict.get(r)), ContactReportEnum.DRAFT.getStatusCode());
//                return calcMetrics(contactReportInfoList, months, LocationEnum.DISTRICT.getLocationText(), r,filteredByMonth);
//            }).collect(Collectors.toList());
//        } else if (type.equalsIgnoreCase(LocationEnum.REGION.name())) {
//            dealers = filterByRegion.apply(value);
//            Map<String, List<Dealers>> dealersByZone = dealers.stream().collect(Collectors.groupingBy(Dealers::getZoneCd));
//            Set<String> zones = dealersByZone.keySet();
//            summaryList = zones.stream().map(r -> {
//                List<ContactReportInfo> contactReportInfoList = contactInfoRepository
//                        .findByDlrCdInAndContactStatusNot(extractDealerCodes.apply(dealersByZone.get(r)), ContactReportEnum.DRAFT.getStatusCode());
//                return calcMetrics(contactReportInfoList, months, LocationEnum.ZONE.getLocationText(), r,filteredByMonth);
//            }).collect(Collectors.toList());
//        } else if (type.equalsIgnoreCase(LocationEnum.DEALER.name())) {
//            dealers = filterByDealer.apply(value);
//            Map<String, List<Dealers>> dealersByRegion = dealers.stream().collect(Collectors.groupingBy(Dealers::getRgnCd));
//            Set<String> regions = dealersByRegion.keySet();
//            summaryList = regions.stream().map(r -> {
//                List<ContactReportInfo> contactReportInfoList = contactInfoRepository
//                        .findByDlrCdInAndContactStatusNot(extractDealerCodes.apply(dealersByRegion.get(r)), ContactReportEnum.DRAFT.getStatusCode());
//                return calcMetrics(contactReportInfoList, months, LocationEnum.REGION.getLocationText(), r,filteredByMonth);
//            }).collect(Collectors.toList());
//        } else {
//            Map<String, List<Dealers>> dealersByRegion = dealerService.findAll().stream().collect(Collectors.groupingBy(Dealers::getRgnCd));
//            Set<String> regions = dealersByRegion.keySet();
//            summaryList = regions.stream().map(r -> {
//                List<ContactReportInfo> contactReportInfoList = contactInfoRepository
//                        .findByDlrCdInAndContactStatusNot(extractDealerCodes.apply(dealersByRegion.get(r)), ContactReportEnum.DRAFT.getStatusCode());
//                return calcMetrics(contactReportInfoList, months, LocationEnum.REGION.getLocationText(), r,filteredByMonth);
//            }).collect(Collectors.toList());
//        }
//        return summaryList;
//    }

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
}
