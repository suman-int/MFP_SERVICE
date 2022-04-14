package com.mnao.mfp.cr.entity;

import java.time.LocalDate;

import javax.persistence.Basic;
import javax.persistence.Convert;
import javax.persistence.MappedSuperclass;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.mnao.mfp.common.util.AppConstants;
import com.mnao.mfp.cr.converter.IsActiveConverter;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@MappedSuperclass()
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class BaseEntity {
	@JsonFormat(pattern = AppConstants.LOCALDATE_FORMAT)
	private LocalDate createdDt;

	@JsonFormat(pattern = AppConstants.LOCALDATE_FORMAT)
	private LocalDate updatedDt;

	private String updatedBy;

	private String createdBy;

	private String isActive;
}
