package com.mnao.mfp.cr.dto;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class ContactReportExecutionCoverageAuthorDto {
    private String author;
    private long reportCount;
    private int reportsCreatedByAuthor;
    private boolean isDealerDefeciencyIdentified;
    private boolean isServiceRetentionFysl;
    private boolean coveredBySalesService;
    private boolean isSales;
    private boolean isService;
    private boolean isOthers;
}
