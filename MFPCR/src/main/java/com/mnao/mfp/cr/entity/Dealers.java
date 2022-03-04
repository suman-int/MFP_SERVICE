package com.mnao.mfp.cr.entity;

import java.time.LocalDate;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "dealers")

public class Dealers {
	
	@Id
	@Column(name = "DLR_CD")
	private String dlrCd;
	@Column(name = "DBA_NM")
	private String dlrNm;
	@Column(name = "RGN_CD")
	private String rgnCd;
	@Column(name = "CITY_NM")
	private String cityNm;
	@Column(name = "ZIP1_CD")
	private int zipCd;
	@Column(name = "ZONE_CD")
	private String zoneCd;
	@Column(name = "DISTRICT_CD")
	private String districtCd ;
//	private String STATUS_CD;
//	private String STATUS_DT;
//	private String CNTY_CD;
//	private String ST_CD;
//	private String MDA_CD;
//	private String SOA_NM;
//	private LocalDate APPT_DT;
//	private LocalDate TERM_DT;
//	private String PREV_DLR_CD ;
//	private String NXT_DLR_CD;
//	private String TIME_ZONE_CD;
//	private String SOA_CD;
//	private String USED_CAR_FL ;
//	private String CNTRY_CD;
//	private String ZIP2_CD;
//	private String DLR_INACTV_DT;
//	private String SVC_ONLY_FL ;
//	private String SVC_ONLY_DT ;
//	private String MDA_NM;
//	private double LAT ;
//	private double LON ;
//	private String DEALERSTRTEND;
//	private String DEALEREFFEND;
//	private String FACILITY_TYPE;
//	private String SHOWROOM_TYPE;
//	private String RGN_NM ;
//	private String DEALERSHIP_FLAG;
//	private LocalDate W_UPDT_DT;
	
	@OneToMany(targetEntity = ContactReportInfo.class, cascade = CascadeType.ALL)
	@JoinColumn(name="dlrCd", updatable = false, insertable = false)
	@NotNull
	private List<ContactReportInfo> CRI;

}
