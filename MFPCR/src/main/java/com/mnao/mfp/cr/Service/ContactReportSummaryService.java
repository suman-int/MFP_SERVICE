package com.mnao.mfp.cr.Service;


import com.mnao.mfp.cr.entity.ContactReportInfo;
import com.mnao.mfp.cr.entity.Dealers;
import com.mnao.mfp.cr.repository.ContactInfoRepository;
import com.mnao.mfp.cr.util.ContactReportEnum;
import com.mnao.mfp.cr.util.IssueType;
import com.mnao.mfp.cr.util.LocationEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.AbstractMap;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
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

    BiFunction<List<ContactReportInfo>, List<String>, List<AbstractMap.SimpleEntry<String, String>>> issueMetrics = (contactReportInfos, issues) ->
            issues.stream().map(i -> {
                List<ContactReportInfo> filterd = contactReportInfos.stream().filter(d -> d.getCurrentIssues().equals(i))
                        .collect(Collectors.toList());
                long reviewed = filterd.stream()
                        .filter(f -> f.getContactStatus() == ContactReportEnum.REVIEWED.getStatusCode())
                        .count();
                return new AbstractMap.SimpleEntry<String, String>(i, String.format("%d/%d", reviewed, filterd.size()));
            }).collect(Collectors.toList());


    public List<AbstractMap.SimpleEntry<String, String>> getSummaryByLocation(String location, String category) {
        List<String> issueTypes = issueType.getIssuesByCategory().get(category.toLowerCase());
        List<Dealers> dealers;
        List<ContactReportInfo> contactReportInfos;
        List<AbstractMap.SimpleEntry<String, String>> summaryList;
        if (location.equalsIgnoreCase(LocationEnum.DISTRICT.name())) {
            dealers = filterByDistrict.apply(location);
            contactReportInfos = contactInfoRepository
                    .findByDlrCdIn(extractDealerCodes.apply(dealers));
            summaryList = issueMetrics.apply(contactReportInfos, issueTypes);
        } else if (location.equalsIgnoreCase(LocationEnum.ZONE.name())) {
            dealers = filterByZone.apply(location);
            contactReportInfos = contactInfoRepository
                    .findByDlrCdIn(extractDealerCodes.apply(dealers));
            summaryList = issueMetrics.apply(contactReportInfos, issueTypes);
        } else if (location.equalsIgnoreCase(LocationEnum.REGION.name())) {
            dealers = filterByRegion.apply(location);
            contactReportInfos = contactInfoRepository
                    .findByDlrCdIn(extractDealerCodes.apply(dealers));
            summaryList = issueMetrics.apply(contactReportInfos, issueTypes);
        } else {
            summaryList = Collections.emptyList();
        }
        return summaryList;
    }
}
