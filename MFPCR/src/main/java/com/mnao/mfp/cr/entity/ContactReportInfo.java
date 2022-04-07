package com.mnao.mfp.cr.entity;

import java.time.LocalDate;
import java.util.List;

import javax.persistence.*;
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
public class ContactReportInfo {

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

	private String updatedBy;

	private String createdBy;

	@JsonFormat(pattern = AppConstants.LOCALDATE_FORMAT)
	private LocalDate createdDt;

	@JsonFormat(pattern = AppConstants.LOCALDATE_FORMAT)
	private LocalDate updatedDt;


	@OneToMany(targetEntity = ContactReportDealerPersonnel.class, cascade = CascadeType.ALL, orphanRemoval = true)
	@JoinColumn(name="contactReportIdFk", referencedColumnName="contactReportId", nullable = false)
	@NotNull
	private List<ContactReportDealerPersonnel> dealerPersonnels;

//	@OneToMany(targetEntity = ContactReportMetrics.class, cascade = CascadeType.ALL)
//	@JoinColumn(name="contactReportIdFk", referencedColumnName="contactReportId", nullable = false)
//	@NotNull
//	private List<ContactReportMetrics> metrics;
	
	@OneToMany(targetEntity = ContactReportDiscussion.class, cascade = CascadeType.ALL)
	@JoinColumn(name="contactReportIdFk", referencedColumnName="contactReportId")
	private List<ContactReportDiscussion> discussions;
	
	@OneToMany(targetEntity = ContactReportAttachment.class, cascade = CascadeType.ALL)
	@JoinColumn(name = "contactReportIdFk", referencedColumnName="contactReportId")
	private List<ContactReportAttachment> attachment;

	@ManyToOne(targetEntity = Dealers.class, cascade = CascadeType.ALL)
	@JoinColumn(name="dlrCd", updatable = false, insertable = false)
	@NotNull
	private Dealers dealers;
	//
//	public void doUpdate(ContactReportInfo newCr) {
//		if(newCr.dlrCd != null)  	dlrCd =  newCr.dlrCd;
//		if(newCr.contactDt != null)  	contactDt =  newCr.contactDt;
//		if(newCr.contactLocation != null)  	contactLocation =  newCr.contactLocation;
//		if(newCr.contactType != null)  	contactType =  newCr.contactType;
//		if(newCr.currentIssues != null)  	currentIssues =  newCr.currentIssues;
//		if(newCr.contactAuthor != null)  	contactAuthor =  newCr.contactAuthor;
//		if(newCr.contactReviewer != null)  	contactReviewer =  newCr.contactReviewer;
//		if(newCr.contactStatus != 0)  	contactStatus =  newCr.contactStatus;
//		if(newCr.corporateReps != null)  	corporateReps =  newCr.corporateReps;
//		if(newCr.updatedBy != null)  	updatedBy =  newCr.updatedBy;
//		if(newCr.createdBy != null)  	createdBy =  newCr.createdBy;
//		if(newCr.createdDt != null)  	createdDt =  newCr.createdDt;
//		if(newCr.updatedDt != null)  	updatedDt =  newCr.updatedDt;
//		if( dealerPersonnels != null ) {
//			for( ContactReportDealerPersonnel dp: dealerPersonnels) {
//				
//			}
//		}
//	}
	
	@PrePersist()
	public void preSave() {
		this.createdBy = this.contactAuthor;
		this.createdDt = LocalDate.now();
	}
	
	@PreUpdate()
	public void preUpdate() {
		this.updatedBy = this.contactAuthor;
		this.updatedDt = LocalDate.now();
	}
 }
