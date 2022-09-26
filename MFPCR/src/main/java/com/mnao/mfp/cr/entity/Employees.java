package com.mnao.mfp.cr.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.List;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class Employees {

    private String CITY_AD;
    private String CNTRY_CD;
    private String FRST_NM;
    private String HIRE_CD;
    private LocalDate HIRE_DT;
    private String JOB_CD;
    private LocalDate JOB_START_DT;
    private String JOB_TITLE_TX;
    private String JOB_TYPE_CD;
    private String LAST_NM;
    private String LOCTN_CD;
    private String MIDL_NM;
    @Id
    @Column(name = "PRSN_ID_CD")
    private String personnelIdCd;
    private String PRSN_TYPE_CD;
    private String ST_CD;
    private String STATUS_CD;
    private String STR1_AD;
    private String STR2_AD;
    private String STR3_AD;
    private LocalDate TRMNTN_DT;
    private LocalDate W_UPDT_DT;
    private String ZIP_CD;

//    @OneToMany(targetEntity = ContactReportDealerPersonnel.class, cascade = CascadeType.ALL)
//    @JoinColumn(name="personnelIdCd")
//    @NotNull
//    private List<ContactReportDealerPersonnel> dealerPersonnels;
}
