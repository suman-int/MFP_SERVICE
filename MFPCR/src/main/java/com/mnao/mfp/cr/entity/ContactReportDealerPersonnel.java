package com.mnao.mfp.cr.entity;

import java.time.LocalDate;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
//@Transactional
@Table(name = "mfp_contact_report_dealerpersonnel")
public class ContactReportDealerPersonnel extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long personnelId;

	@NotNull
	private String personnelIdCd;
	
	@PrePersist()
	public void preSave() {
		this.setCreatedBy("ADMIN");
		this.setCreatedDt(LocalDate.now());
		this.setIsActive("Y");
	}

	@PreUpdate()
	public void preUpdate() {
		this.setUpdatedBy("ADMIN");
		this.setUpdatedDt(LocalDate.now());
	}

}
