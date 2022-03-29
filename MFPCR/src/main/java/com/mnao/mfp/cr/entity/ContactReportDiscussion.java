package com.mnao.mfp.cr.entity;

import java.time.LocalDate;

import javax.persistence.*;
import javax.persistence.criteria.Fetch;
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
@Table(name = "mfp_contact_report_discussion")
public class ContactReportDiscussion {
	
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
	private long discussionId;

    @NotNull
	private String topic;

    @NotNull
	private String discussion;
	
    @JsonFormat(pattern = AppConstants.LOCALDATE_FORMAT)
	@NotNull
	private LocalDate disscussionDt;

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name="contactReportId")
	@JsonIgnore
	private ContactReportInfo contactReportInfo;
	
}
