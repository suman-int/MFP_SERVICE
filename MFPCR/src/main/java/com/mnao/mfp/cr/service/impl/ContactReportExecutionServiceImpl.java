package com.mnao.mfp.cr.service.impl;

import com.mnao.mfp.common.datafilters.FilterCriteria;
import com.mnao.mfp.common.util.AppConstants;
import com.mnao.mfp.cr.dto.ContactReportExecutionCoverageAuthorDto;
import com.mnao.mfp.cr.dto.ContactReportExecutionCoverageDto;
import com.mnao.mfp.cr.entity.ContactReportInfo;
import com.mnao.mfp.cr.entity.Dealers;
import com.mnao.mfp.cr.repository.ContactInfoRepository;
import com.mnao.mfp.cr.service.ContactReportExecutionService;
import com.mnao.mfp.cr.util.ContactReportEnum;
import com.mnao.mfp.cr.util.DataOperationFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ContactReportExecutionServiceImpl implements ContactReportExecutionService {

    @Autowired
    private ContactInfoRepository contactInfoRepository;

    @Autowired
    private DataOperationFilter dataOperationFilter;

    @Override
    public List<ContactReportExecutionCoverageDto> reportExecutionCoverageByReportTime(FilterCriteria  filterCriteria) {
        List<ContactReportExecutionCoverageDto> contactReportExecCoverageList = new ArrayList<>(0);
        List<ContactReportInfo> contactReportInfoList = contactInfoRepository.findByContactDtBetweenAndContactStatusGreaterThan(
                filterCriteria.getStartDate(), filterCriteria.getEndDate(), ContactReportEnum.DRAFT.getStatusCode());
        contactReportInfoList = dataOperationFilter.filterContactReportsByLocation(filterCriteria, contactReportInfoList);
        Map<Dealers, Map<String, List<ContactReportInfo>>> reportMap = contactReportInfoList.stream().collect(Collectors.groupingBy(ContactReportInfo::getDealers,
                Collectors.groupingBy(ContactReportInfo::getContactAuthor)));

        reportMap.forEach((key, value) -> {
            List<ContactReportExecutionCoverageAuthorDto> authorDetailsDto =  new ArrayList<>(0);
            value.forEach((key1, value1) -> {
                boolean isDealerDefeciencyIdentified = value1.stream().anyMatch(cr -> cr.getCurrentIssues().contains("Dealer Dev Deficiencies Identifed"));
                boolean isServiceRetentionFysl = value1.stream().anyMatch(cr -> cr.getCurrentIssues().contains("Service Retention/FYSL"));
                authorDetailsDto.add(ContactReportExecutionCoverageAuthorDto.builder()
                        .author(key1)
                        .reportCount(value1.size())
                        .isDealerDefeciencyIdentified(isDealerDefeciencyIdentified)
                        .isServiceRetentionFysl(isServiceRetentionFysl)
                        .isSales(value1.stream().anyMatch(cr -> cr.getContactType().contains("SALES")))
                        .isOthers(value1.stream().anyMatch(cr -> cr.getContactType().contains("SERVICE")))
                        .isService(value1.stream().anyMatch(cr -> cr.getContactType().contains("OTHER")))
                        .coveredBySalesService(value1.stream().anyMatch(cr -> (cr.getContactType().equalsIgnoreCase("SALES,SERVICE") || cr.getContactType().equalsIgnoreCase("SALES,SERVICE,OTHER"))))
                        .build());

            });

            contactReportExecCoverageList.add(ContactReportExecutionCoverageDto.builder()
                    .dealerCode(key.getDlrCd())
                    .dealerName(key.getDbaNm())
                    .reportCount(authorDetailsDto.stream().mapToLong(ContactReportExecutionCoverageAuthorDto::getReportCount).sum())
                    .authorDtos(authorDetailsDto)
                    .author(authorDetailsDto.stream().map(ContactReportExecutionCoverageAuthorDto::getAuthor).collect(Collectors.joining(",")))
                    .isDealerDefeciencyIdentified(authorDetailsDto.stream().anyMatch(ContactReportExecutionCoverageAuthorDto::isDealerDefeciencyIdentified))
                    .isServiceRetentionFysl(authorDetailsDto.stream().anyMatch(ContactReportExecutionCoverageAuthorDto::isServiceRetentionFysl))
                    .coverage(authorDetailsDto.stream().anyMatch(ContactReportExecutionCoverageAuthorDto::isCoveredBySalesService) ? "100" : "50")
                    .isSales(authorDetailsDto.stream().anyMatch(ContactReportExecutionCoverageAuthorDto::isSales))
                    .isOthers(authorDetailsDto.stream().anyMatch(ContactReportExecutionCoverageAuthorDto::isOthers))
                    .isService(authorDetailsDto.stream().anyMatch(ContactReportExecutionCoverageAuthorDto::isService))
                    .build());
        });
        return contactReportExecCoverageList;
    }
}
