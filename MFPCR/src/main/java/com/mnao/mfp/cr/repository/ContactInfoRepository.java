package com.mnao.mfp.cr.repository;

import com.mnao.mfp.cr.dto.ReportByDealershipDto;
import com.mnao.mfp.cr.entity.ContactReportInfo;
import org.springframework.context.annotation.PropertySource;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
@PropertySource("classpath:/appSql.properties")
public interface ContactInfoRepository extends JpaRepository<ContactReportInfo, Long> {


    ContactReportInfo findByContactReportIdAndIsActive(@Param("contactReportId") long contactReportId, String isActive);

    @Query(value = "SELECT new com.mnao.mfp.cr.dto.ReportByDealershipDto"
            + "(d.rgnCd, d.zoneCd, d.districtCd, cr.dlrCd, d.dbaNm, cr.contactReportId, cr.contactDt, cr.contactAuthor,cr.contactStatus,cr.currentIssues) "
            + "FROM Dealers d JOIN d.CRI cr WHERE cr.currentIssues IN :currentIssues AND cr.dlrCd=:dlrCd AND cr.isActive='Y'")
    List<ReportByDealershipDto> findByDlrCd(@Param("dlrCd") String dlrCd,
                                            @Param("currentIssues") List<String> currentIssues);

    @Query(value = "SELECT new com.mnao.mfp.cr.dto.ReportByDealershipDto"
            + "(d.rgnCd, d.zoneCd, d.districtCd, cr.dlrCd, d.dbaNm, cr.contactReportId, cr.contactDt, cr.contactAuthor,cr.contactStatus,cr.currentIssues) "
            + "FROM Dealers d JOIN d.CRI cr WHERE cr.dlrCd=:dlrCd AND cr.isActive='Y'")
    List<ReportByDealershipDto> findCurrentIssuesByDlrCd(@Param("dlrCd") String dlrCd);

    void deleteByContactReportIdAndContactStatusAndIsActive(@Param("contactReportId") long contactReportId,
                                                            int contactStatus, String isActive);

    List<ContactReportInfo> findByDlrCdAndIsActive(String dlrCd, String isActive);

    List<ContactReportInfo> findByDlrCdInAndContactStatusNotAndIsActive(List<String> dlrCd, int status, String isActive);

    List<ContactReportInfo> findByContactAuthorAndIsActive(String authorId, String isActive);

    List<ContactReportInfo> findByContactDtBetweenAndIsActive(LocalDate startDate, LocalDate endDate, String isActive);

    List<ContactReportInfo> findByCurrentIssuesContainingAndIsActive(String issue, String isActive);

    List<ContactReportInfo> findByCurrentIssuesNotNullAndIsActive(String isActive);

    List<ContactReportInfo> findByCurrentIssuesNotNullAndContactDtNotNullAndIsActive(String isActive);

    List<ContactReportInfo> findByContactDtBetweenAndContactStatusGreaterThanAndIsActive(LocalDate startDate, LocalDate endDate, Integer status, String isActive);

	List<ContactReportInfo> findByIsActive(String isActive);

}
