package com.mnao.mfp.cr.dto;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class ContactReportExecutionCoverageAuthorDto {
    private String author;
    private int reportsCreatedByAuthor;
    private boolean isDealerDefeciencyIdentified;
    private boolean isServiceRetentionFysl;
}
