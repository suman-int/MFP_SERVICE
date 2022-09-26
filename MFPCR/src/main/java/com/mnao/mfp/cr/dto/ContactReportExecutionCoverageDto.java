package com.mnao.mfp.cr.dto;

import lombok.*;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class ContactReportExecutionCoverageDto {
    private String type;
    private String author;
    private String coverage;
    private Long reportCount;
    private String dealerCode;
    private boolean isDealerDefeciencyIdentified;
    private boolean isServiceRetentionFysl;
    private List<ContactReportExecutionCoverageAuthorDto> authorDtos;
    private String dealerName;
    private boolean isSales;
    private boolean isService;
    private boolean isOthers;
}
