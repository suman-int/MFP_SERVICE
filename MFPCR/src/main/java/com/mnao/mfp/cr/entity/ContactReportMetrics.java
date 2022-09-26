package com.mnao.mfp.cr.entity;

import java.time.LocalDate;
import java.util.List;

import javax.persistence.*;
import javax.transaction.Transactional;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.mnao.mfp.common.util.AppConstants;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Transactional
@Table(name = "mfp_contact_report_metrics")
public class ContactReportMetrics extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
	private int metrics_id;
    
    @NotNull
	private String metricsName;
    
    @NotNull
	private double metricsValue;
    
    @NotNull
    @JsonFormat(pattern = AppConstants.LOCALDATE_FORMAT)
	private LocalDate metricsDt;
    
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
