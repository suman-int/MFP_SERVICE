package com.mnao.mfp.cr.entity;

import javax.persistence.*;
import javax.transaction.Transactional;
import javax.validation.constraints.NotNull;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Entity
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
//@Transactional
@Table(name = "mfp_contact_report_dealerpersonnel")
public class ContactReportDealerPersonnel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
	private long personnelId;

    @NotNull
	private String personnelIdCd;
    
}
