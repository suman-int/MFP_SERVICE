package com.mnao.mfp.cr.Service;


import com.mnao.mfp.cr.entity.ContactReportInfo;
import com.mnao.mfp.cr.entity.Dealers;
import com.mnao.mfp.cr.repository.ContactInfoRepository;
import com.mnao.mfp.cr.util.ContactReportEnum;
import com.mnao.mfp.cr.util.IssueType;
import com.mnao.mfp.cr.util.LocationEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.BiFunction;
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

    private List<String> zonesList = Arrays.asList("");

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

    private List<Map<String, String>> issueMetrics(List<ContactReportInfo> contactReportInfos, List<String> issues, String type, String value) {
        return contactReportInfos.stream().map(contactReportInfo -> {
            Map<String, String> map = new HashMap<>();
            map.put(type, value);
            issues.stream().forEach(i -> {
                List<ContactReportInfo> filterd = contactReportInfos.stream()
                        .filter(c -> Objects.nonNull(c.getCurrentIssues()))
                        .filter(d -> d.getCurrentIssues().equals(i))
                        .collect(Collectors.toList());
                long reviewed = filterd.stream()
                        .filter(f -> f.getContactStatus() == ContactReportEnum.REVIEWED.getStatusCode())
                        .count();

                map.put(i, String.format("%d/%d", reviewed, filterd.size()));
            });
            return map;
        }).collect(Collectors.toList());
    }


    public List<Map<String, String>> getSummaryByLocation(String type, String value, String category) {
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
                        .findByDlrCdIn(extractDealerCodes.apply(dealersByDealer.get(r)));
                return issueMetrics(contactReportInfoList, issueTypes,LocationEnum.DEALER.getLocationText(),r);
            }).flatMap(List::stream).collect(Collectors.toList());
        } else if (type.equalsIgnoreCase(LocationEnum.ZONE.name())) {
            dealers = filterByZone.apply(value);
            Map<String, List<Dealers>> dealersByDistrict = dealers.stream().collect(Collectors.groupingBy(Dealers::getDistrictCd));
            Set<String> districts = dealersByDistrict.keySet();
            summaryList = districts.stream().map(r -> {
                List<ContactReportInfo> contactReportInfoList = contactInfoRepository
                        .findByDlrCdIn(extractDealerCodes.apply(dealersByDistrict.get(r)));
                return issueMetrics(contactReportInfoList, issueTypes,LocationEnum.DISTRICT.getLocationText(),r);
            }).flatMap(List::stream).collect(Collectors.toList());
        } else if (type.equalsIgnoreCase(LocationEnum.REGION.name())) {
            dealers = filterByRegion.apply(value);
            Map<String, List<Dealers>> dealersByZone = dealers.stream().collect(Collectors.groupingBy(Dealers::getZoneCd));
            Set<String> zones = dealersByZone.keySet();
            summaryList = zones.stream().map(r -> {
                List<ContactReportInfo> contactReportInfoList = contactInfoRepository
                        .findByDlrCdIn(extractDealerCodes.apply(dealersByZone.get(r)));
                return issueMetrics(contactReportInfoList, issueTypes,LocationEnum.ZONE.getLocationText(),r);
            }).flatMap(List::stream).collect(Collectors.toList());
        } else if (type.equalsIgnoreCase(LocationEnum.DEALER.name())) {
            dealers = filterByDealer.apply(value);
            Map<String, List<Dealers>> dealersByRegion = dealers.stream().collect(Collectors.groupingBy(Dealers::getRgnCd));
            Set<String> regions = dealersByRegion.keySet();
            summaryList = regions.stream().map(r -> {
                List<ContactReportInfo> contactReportInfoList = contactInfoRepository
                        .findByDlrCdIn(extractDealerCodes.apply(dealersByRegion.get(r)));
                return issueMetrics(contactReportInfoList, issueTypes,LocationEnum.REGION.getLocationText(),r);
            }).flatMap(List::stream).collect(Collectors.toList());
        } else {
            summaryList = Collections.emptyList();
        }
        return summaryList;
    }
}
