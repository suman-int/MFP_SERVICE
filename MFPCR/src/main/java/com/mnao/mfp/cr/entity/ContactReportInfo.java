package com.mnao.mfp.cr.entity;

import java.time.LocalDate;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.mnao.mfp.common.util.AppConstants;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "mfp_contact_report_info")
public class ContactReportInfo extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long contactReportId;

	@NotNull
	private String dlrCd;

	@JsonFormat(pattern = AppConstants.LOCALDATE_FORMAT)
	@NotNull
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
	private int contactStatus;

	private String corporateReps;

	@JsonFormat(pattern = AppConstants.LOCALDATE_FORMAT)
	private LocalDate submittedDt;

	private String reviewedBy;

	@JsonFormat(pattern = AppConstants.LOCALDATE_FORMAT)
	private LocalDate reviewedDt;

	private String lastDiscussionReqBy;

	@JsonFormat(pattern = AppConstants.LOCALDATE_FORMAT)
	@Column(name = "LAST_DISCUSSION_REQ4_DT")
	private LocalDate lastDiscussionReqDt;

	@OneToMany(targetEntity = ContactReportDealerPersonnel.class, cascade = CascadeType.ALL)
	@JoinColumn(name = "contactReportIdFk", referencedColumnName = "contactReportId", nullable = false)
	@NotNull
	private List<ContactReportDealerPersonnel> dealerPersonnels;

//	@OneToMany(targetEntity = ContactReportMetrics.class, cascade = CascadeType.ALL)
//	@JoinColumn(name="contactReportIdFk", referencedColumnName="contactReportId", nullable = false)
//	@NotNull
//	private List<ContactReportMetrics> metrics;

	@OneToMany(targetEntity = ContactReportDiscussion.class, cascade = CascadeType.ALL)
	@JoinColumn(name = "contactReportIdFk", referencedColumnName = "contactReportId")
	private List<ContactReportDiscussion> discussions;

	@OneToMany(targetEntity = ContactReportAttachment.class, cascade = CascadeType.ALL)
	@JoinColumn(name = "contactReportIdFk", referencedColumnName = "contactReportId")
	private List<ContactReportAttachment> attachment;

	@ManyToOne(targetEntity = Dealers.class, cascade = CascadeType.ALL)
	@JoinColumn(name = "dlrCd", updatable = false, insertable = false)
	@NotNull
	private Dealers dealers;

	private String addDealerPersonnel;
	
	@Transient
	private boolean forcedDraft = false;

	@PrePersist()
	public void preSave() {
		this.setCreatedBy(this.contactAuthor);
		this.setCreatedDt(LocalDate.now());
		this.setIsActive("Y");
	}

	@PreUpdate()
	public void preUpdate() {
		this.setUpdatedBy(contactAuthor);
		this.setUpdatedDt(LocalDate.now());
	}
}
