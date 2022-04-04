package com.mnao.mfp.cr.util;

import com.mnao.mfp.common.datafilters.FilterCriteria;
import com.mnao.mfp.cr.entity.ContactReportInfo;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component()
public class DataOperationFilter {

    public Map<String, Map<String, List<ContactReportInfo>>> filterContactReportsByLocationAndGroupingByDealer(
            FilterCriteria filter,
            List<ContactReportInfo> contactReports
    ) {
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

    public Stream<ContactReportInfo> filterContactReportByRegion(List<ContactReportInfo> contactReports,
                                                                 String filter) {
        return contactReports.stream().filter(cr -> filter.equalsIgnoreCase(cr.getDealers().getRgnCd()));
    }

    public Stream<ContactReportInfo> filterContactReportByZone(List<ContactReportInfo> contactReports,
                                                               FilterCriteria filter) {
        return filterContactReportByRegion(contactReports, filter.getRgnCd())
                .filter(cr -> filter.getZoneCd().equalsIgnoreCase(cr.getDealers().getZoneCd()));
    }



    public Stream<ContactReportInfo> filterContactReportByDistrict(List<ContactReportInfo> contactReports,
                                                                   FilterCriteria filter) {
        return filterContactReportByZone(contactReports, filter)
                .filter(cr -> filter.getDistrictCd().equalsIgnoreCase(cr.getDealers().getDistrictCd()));
    }

    public Stream<ContactReportInfo> filterContactReportByDealer(List<ContactReportInfo> contactReports,
                                                                   FilterCriteria filter) {
        return filterContactReportByDistrict(contactReports, filter)
                .filter(cr -> filter.getDlrCd().equalsIgnoreCase(cr.getDealers().getDlrCd()));
    }

    public List<ContactReportInfo> filterContactReportsByDateRange(FilterCriteria filter,
                                                                   List<ContactReportInfo> contactReports) {
        return contactReports.stream().filter(cr -> Objects.nonNull(cr.getContactDt())).filter(cr -> cr.getContactDt().isAfter(filter.getStartDate().minusDays(1))
                && cr.getContactDt().isBefore(filter.getEndDate().plusDays(1))).collect(Collectors.toList());
    }

    public  List<ContactReportInfo> filterContactReportsByIssues(FilterCriteria filter,
                                                                List<ContactReportInfo> contactReports) {
        return contactReports.stream()
        		.filter(cr -> Objects.nonNull(cr.getCurrentIssues()))
        		.filter(cr -> filter.getIssuesFilter().stream().anyMatch(value -> cr.getCurrentIssues().contains(value)))
                .collect(Collectors.toList());
    }

    public List<ContactReportInfo> filterContactReportsByLocation(
            FilterCriteria filter,
            List<ContactReportInfo> contactReports
    ) {
        if (filter.forLocation() == LocationEnum.DISTRICT) {
            return filterContactReportByDistrict(contactReports, filter).collect(Collectors.toList());
        } else if (filter.forLocation() == LocationEnum.ZONE) {
            return filterContactReportByZone(contactReports, filter).collect(Collectors.toList());
        } else if (filter.forLocation() == LocationEnum.DEALER) {
           return filterContactReportByDealer(contactReports, filter).collect(Collectors.toList());
        } else if (filter.forLocation() == LocationEnum.REGION) {
            return filterContactReportByRegion(contactReports, filter.getRgnCd()).collect(Collectors.toList());
        }
        return contactReports;
    }
}
