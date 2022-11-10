package com.mnao.mfp.cr.dto;

import java.time.LocalDate;

import javax.persistence.Transient;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.mnao.mfp.common.util.AppConstants;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class ReportByDealershipDto {

	private String rgnCd;

	private String zoneCd;

	private String districtCd;

	private String dlrCd;

	private String dbaNm;

	private long contactReportId;

	@JsonFormat(pattern = AppConstants.LOCALDATE_FORMAT)
	private LocalDate contactDt;

	private String contactAuthor;

	private String contactReviewer;

	private int contactStatus;

	private String currentIssues;

	@Transient
	private boolean forcedDraft = false;

	public ReportByDealershipDto(String rgnCd, String zoneCd, String districtCd, String dlrCd, String dbaNm,
			long contactReportId, LocalDate contactDt, String contactAuthor, String contactReviewer, int contactStatus,
			String currentIssues) {
		super();
		this.rgnCd = rgnCd;
		this.zoneCd = zoneCd;
		this.districtCd = districtCd;
		this.dlrCd = dlrCd;
		this.dbaNm = dbaNm;
		this.contactReportId = contactReportId;
		this.contactDt = contactDt;
		this.contactAuthor = contactAuthor;
		this.contactReviewer = contactReviewer;
		this.contactStatus = contactStatus;
		this.currentIssues = currentIssues;
	}

}
