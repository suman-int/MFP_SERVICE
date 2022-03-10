package com.mnao.mfp.cr.entity;

import java.time.LocalDate;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonFormat;

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
	
	@JsonFormat(pattern="yyyy-MM-dd")
	@NotNull
	private String uploadedBy;
	
	@JsonFormat(pattern="yyyy-MM-dd")
	@NotNull
	private LocalDate uploadDt;
	
	private String deletedBy;
	
	private LocalDate deleteDt;
	
	@NotNull
	private int status;

}
