package com.mnao.mfp.cr.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;


@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class ReportByIssuesDto {

    private String rgnCd;

    private String zoneCd;

    private String districtCd;

    private String dlrCd;

    private String dlrName;

    private long contactReportId;

    private String currentIssues;


}
