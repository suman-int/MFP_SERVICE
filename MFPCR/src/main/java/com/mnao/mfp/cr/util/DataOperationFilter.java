package com.mnao.mfp.cr.util;

import com.mnao.mfp.common.datafilters.FilterCriteria;
import com.mnao.mfp.common.util.NullCheck;
import com.mnao.mfp.cr.entity.ContactReportInfo;
import com.mnao.mfp.user.dao.Domain;
import com.mnao.mfp.user.dao.MFPUser;

import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component()
public class DataOperationFilter {

    public Map<String, Map<String, List<ContactReportInfo>>> filterContactReportsByLocationAndGroupingByDealer(
            FilterCriteria filter,
            List<ContactReportInfo> contactReports,
            MFPUser mfpUser
    ) {
        Map<String, Map<String, List<ContactReportInfo>>> reports;
        if (filter.forLocation() == LocationEnum.DISTRICT) {
            reports = filterContactReportByDistrict(contactReports, filter, mfpUser)
                    .collect(Collectors.groupingBy(group -> group.getDealers().getDlrCd(),
                            Collectors.groupingBy(ContactReportInfo::getCurrentIssues)));
        } else if (filter.forLocation() == LocationEnum.ZONE) {
            reports = filterContactReportByZone(contactReports, filter, mfpUser)
                    .collect(Collectors.groupingBy(group -> group.getDealers().getDistrictCd(),
                            Collectors.groupingBy(ContactReportInfo::getCurrentIssues)));
        } else if (filter.forLocation() == LocationEnum.DEALER) {
            reports = contactReports.stream().collect(Collectors.groupingBy(group -> group.getDealers().getDlrCd(),
                    Collectors.groupingBy(ContactReportInfo::getCurrentIssues)));
        } else if (filter.forLocation() == LocationEnum.REGION) {
            reports = filterContactReportByRegion(contactReports, filter.getRgnCd(), mfpUser)
                    .collect(Collectors.groupingBy(group -> group.getDealers().getZoneCd(),
                            Collectors.groupingBy(ContactReportInfo::getCurrentIssues)));
        } else {
            reports = getFilteredRegionByUser(mfpUser, contactReports).stream().collect(Collectors.groupingBy(group -> group.getDealers().getRgnCd(),
                    Collectors.groupingBy(ContactReportInfo::getCurrentIssues)));
        }
        return reports;
    }

    public Stream<ContactReportInfo> filterContactReportByRegion(List<ContactReportInfo> contactReports,
                                                                 String filter, MFPUser mfpUser) {
        return getFilteredRegionByUser(mfpUser, contactReports).stream().filter(cr -> filter.equalsIgnoreCase(cr.getDealers().getRgnCd()));
    }

    public Stream<ContactReportInfo> filterContactReportByZone(List<ContactReportInfo> contactReports,
                                                               FilterCriteria filter, MFPUser mfpUser) {
        return filterContactReportByRegion(contactReports, filter.getRgnCd(), mfpUser)
                .filter(cr -> filter.getZoneCd().equalsIgnoreCase(cr.getDealers().getZoneCd()));
    }



    public Stream<ContactReportInfo> filterContactReportByDistrict(List<ContactReportInfo> contactReports,
                                                                   FilterCriteria filter, MFPUser mfpUser) {
        return filterContactReportByZone(contactReports, filter, mfpUser)
                .filter(cr -> filter.getDistrictCd().equalsIgnoreCase(cr.getDealers().getDistrictCd()));
    }

    public Stream<ContactReportInfo> filterContactReportByDealer(List<ContactReportInfo> contactReports,
                                                                   FilterCriteria filter, MFPUser mfpUser) {
        return filterContactReportByDistrict(contactReports, filter, mfpUser)
                .filter(cr -> filter.getDlrCd().equalsIgnoreCase(cr.getDealers().getDlrCd().trim()));
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
            , MFPUser mfpUser
    ) {
        if (filter.forLocation() == LocationEnum.DISTRICT) {
            return filterContactReportByDistrict(contactReports, filter, mfpUser).collect(Collectors.toList());
        } else if (filter.forLocation() == LocationEnum.ZONE) {
            return filterContactReportByZone(contactReports, filter, mfpUser).collect(Collectors.toList());
        } else if (filter.forLocation() == LocationEnum.DEALER) {
           return filterContactReportByDealer(contactReports, filter, mfpUser).collect(Collectors.toList());
        } else if (filter.forLocation() == LocationEnum.REGION) {
            return filterContactReportByRegion(contactReports, filter.getRgnCd(), mfpUser).collect(Collectors.toList());
        }
        return contactReports;
    }
    
    public List<ContactReportInfo> getFilteredRegionByUser(MFPUser user, List<ContactReportInfo> contactReports) {
    	Optional<List<String>> userRegions = filterBasedOnCurrentUser(user);
    	if(userRegions.isPresent()) {
    		List<String> regionList = userRegions.get();
    		if (!CollectionUtils.isEmpty(regionList)) {
    			return contactReports.stream().filter(cr -> regionList.contains(cr.getDealers().getRgnCd())).collect(Collectors.toList());
    		}
    		else return Collections.emptyList();
    	}
    	 return Collections.emptyList();
    }
    
    private Optional<List<String>> filterBasedOnCurrentUser(MFPUser user) {
    	if (new NullCheck<MFPUser>(user).with(MFPUser::getDomain).with(Domain::getRegions).isNotNull()) {
    		return Optional.of(user.getDomain().getRegions());
    	} else if (new NullCheck<MFPUser>(user).with(MFPUser::getDomain).with(Domain::getRegion).isNotNull()) {
    		return Optional.of(Collections.singletonList(user.getDomain().getRegion().getCode()));
    	}
    	return Optional.of(new ArrayList<>());
    }
}
