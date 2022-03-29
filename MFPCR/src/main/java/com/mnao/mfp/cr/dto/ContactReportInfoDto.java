package com.mnao.mfp.cr.dto;

import java.time.LocalDate;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.mnao.mfp.common.util.AppConstants;
import com.mnao.mfp.cr.entity.ContactReportAttachment;
import com.mnao.mfp.cr.entity.ContactReportDealerPersonnel;
import com.mnao.mfp.cr.entity.ContactReportDiscussion;
import com.mnao.mfp.cr.entity.Dealers;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Getter
@Setter
public class ContactReportInfoDto {

	private long contactReportId;

	private String dlrCd;

	@JsonFormat(pattern = AppConstants.LOCALDATE_FORMAT)
	private LocalDate contactDt;

	@NotNull
	private String contactLocation;

	@NotNull
	private String contactType;

	private String currentIssues;

	@NotNull
	private String contactAuthor;

	@NotNull
	private String contactReviewer;

	@NotNull
	private Integer contactStatus;

	private String corporateReps;

	private List<ContactReportDealerPersonnel> dealerPersonnels;

	private List<ContactReportDiscussion> discussions;

	private List<ContactReportAttachment> attachment;

	@NotNull
	private Dealers dealers;
	
	@JsonFormat(pattern = AppConstants.LOCALDATE_FORMAT)
	private LocalDate updatedDt;

}
