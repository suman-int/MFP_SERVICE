package com.mnao.mfp.cr.entity;

import java.time.LocalDate;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.mnao.mfp.common.util.AppConstants;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "mfp_contact_report_attachment")
public class ContactReportAttachment {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long attachmentId;
	
	@NotNull
	private String attachmentName;
	
	@NotNull
	private String attachmentPath;

	@NotNull
	private String attachmentType;
	
	@JsonFormat(pattern = AppConstants.LOCALDATE_FORMAT)
	@NotNull
	private String uploadedBy;
	
	@JsonFormat(pattern = AppConstants.LOCALDATE_FORMAT)
	@NotNull
	private LocalDate uploadDt;
	
	private String deletedBy;
	
	private LocalDate deleteDt;
	
	@NotNull
	private int status;

	@ManyToOne(targetEntity = ContactReportInfo.class,fetch = FetchType.EAGER)
	@JoinColumn(name = "contactReportIdFk", referencedColumnName="contactReportId")
	@JsonIgnore
	private ContactReportInfo contactReport;
	
}
