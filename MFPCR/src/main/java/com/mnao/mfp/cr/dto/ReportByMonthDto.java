package com.mnao.mfp.cr.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReportByMonthDto {

    private String rgnCd;

    private String zoneCd;

    private String districtCd;

    private String dlrCd;

    private String dlrName;

    private long contactReportId;

    private LocalDate contactDt;
}
