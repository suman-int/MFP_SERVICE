package com.mnao.mfp.cr.service.impl;

import com.mnao.mfp.common.datafilters.FilterCriteria;
import com.mnao.mfp.common.dto.CommonResponse;
import com.mnao.mfp.common.service.AbstractService;
import com.mnao.mfp.cr.dto.ReportByDealerShipResponse;
import com.mnao.mfp.cr.dto.ReportByDealershipDto;
import com.mnao.mfp.cr.entity.ContactReportInfo;
import com.mnao.mfp.cr.repository.ContactInfoRepository;
import com.mnao.mfp.cr.service.ContactInfoService;
import com.mnao.mfp.cr.util.ContactReportEnum;
import com.mnao.mfp.cr.util.LocationEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiPredicate;
import java.util.stream.Collectors;

@Service
public class ContactInfoServiceImpl implements ContactInfoService {

    @Autowired
    ContactInfoRepository contactInfoRepository;

    private final BiPredicate<ContactReportInfo, FilterCriteria> regionFilter = (c, f) -> {
        if (Objects.nonNull(f.getRgnCd())) {
            return c.getDealers().getRgnCd().equals(f.getRgnCd());
        }
        return true;
    };
    private final BiPredicate<ContactReportInfo, FilterCriteria> zoneFilter = (c, f) -> {
        if (Objects.nonNull(f.getZoneCd())) {
            return c.getDealers().getZoneCd().equals(f.getZoneCd());
        }
        return true;
    };
    private final BiPredicate<ContactReportInfo, FilterCriteria> districtFilter = (c, f) -> {
        if (Objects.nonNull(f.getDistrictCd())) {
            return c.getDealers().getDistrictCd().equals(f.getDistrictCd());
        }
        return true;
    };
    private final BiPredicate<ContactReportInfo, FilterCriteria> dealerFilter = (c, f) -> {
        if (Objects.nonNull(f.getDlrCd())) {
            return c.getDealers().getDlrCd().equals(f.getDlrCd());
        }
        return true;
    };


    public List<ContactReportInfo> filterContactReportsBasedOnFilter(FilterCriteria filterCriteria) {
        List<ContactReportInfo> contactReportInfoList = contactInfoRepository.findAll();
        if (filterCriteria.forLocation() == LocationEnum.REGION) {
            contactReportInfoList = contactReportInfoList.stream()
                    .filter(c -> c.getDealers().getRgnCd().equalsIgnoreCase(filterCriteria.getRgnCd()))
                    .collect(Collectors.toList());
        } else if (filterCriteria.forLocation() == LocationEnum.ZONE) {
            contactReportInfoList = contactReportInfoList.stream()
                    .filter(c -> c.getDealers().getRgnCd().equalsIgnoreCase(filterCriteria.getRgnCd()))
                    .filter(c -> c.getDealers().getZoneCd().equalsIgnoreCase(filterCriteria.getZoneCd()))
                    .collect(Collectors.toList());
        } else if (filterCriteria.forLocation() == LocationEnum.DISTRICT) {
            contactReportInfoList = contactReportInfoList.stream()
                    .filter(c -> c.getDealers().getRgnCd().equalsIgnoreCase(filterCriteria.getRgnCd()))
                    .filter(c -> c.getDealers().getZoneCd().equalsIgnoreCase(filterCriteria.getZoneCd()))
                    .filter(c -> c.getDealers().getDistrictCd().equalsIgnoreCase(filterCriteria.getDistrictCd()))
                    .collect(Collectors.toList());
        } else if (filterCriteria.forLocation() == LocationEnum.DEALER) {
            contactReportInfoList = contactReportInfoList.stream()
                    .filter(c -> c.getDlrCd().equalsIgnoreCase(filterCriteria.getDlrCd()))
                    .collect(Collectors.toList());
        }
        return contactReportInfoList;
    }


    @Override
    public CommonResponse<ReportByDealerShipResponse> byDealership(FilterCriteria filterCriteria) {
        try {
            ReportByDealerShipResponse byDealerShipResponse = new ReportByDealerShipResponse();
            List<ReportByDealershipDto> data = contactInfoRepository.findCurrentIssuesByDlrCd(filterCriteria.getDlrCd());
            if (!filterCriteria.getIssuesFilter().isEmpty()) {
                data = data.stream().filter(value -> filterCriteria.getIssuesFilter().stream().anyMatch(filterQuery -> value.getCurrentIssues().contains(filterQuery))).collect(Collectors.toList());
            }

            Map<Integer, List<ReportByDealershipDto>> groupByStatus = data.stream()
                    .collect(Collectors.groupingBy(ReportByDealershipDto::getContactStatus));
            byDealerShipResponse.setDraft(groupByStatus.getOrDefault(ContactReportEnum.DRAFT.getStatusCode(),
                    new ArrayList<>()));
            byDealerShipResponse.setDiscussionRequested(groupByStatus.getOrDefault(
                    ContactReportEnum.DISCUSSION_REQUESTED.getStatusCode(), new ArrayList<>()));
            byDealerShipResponse.setReviewed(groupByStatus.getOrDefault(ContactReportEnum.REVIEWED.getStatusCode(),
                    new ArrayList<>()));
            byDealerShipResponse.setSubmitted(groupByStatus.getOrDefault(ContactReportEnum.SUBMITTED.getStatusCode(),
                    new ArrayList<>()));
            return AbstractService.httpPostSuccess(byDealerShipResponse, "Success");
        } catch (Exception exp) {
            return AbstractService.httpPostError(exp);
        }
    }

}
