package com.mnao.mfp.cr.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.context.annotation.PropertySource;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.mnao.mfp.cr.dto.ReportByDealershipDto;
import com.mnao.mfp.cr.dto.ReportByMonthDto;
import com.mnao.mfp.cr.entity.ContactReportInfo;

@Repository
@PropertySource("classpath:/appSql.properties")
public interface ContactInfoRepository extends JpaRepository<ContactReportInfo, Long> {

//	@Modifying
//	@Query(value = "update cr from ContactReportInfo cr set cr.contactDt='2021-12-13' where cr.contactReportId=:contactReportId and cr.contactStatus=0")
//	public String updateContactReportById(@Param("report") ContactReportInfo report,@Param("contactReportId") long contactReportId);

	public ContactReportInfo findByContactReportIdAndIsActive(@Param("contactReportId") long contactReportId, String isActive);

	@Query(value = "SELECT new com.mnao.mfp.cr.dto.ReportByDealershipDto"
			+ "(d.rgnCd, d.zoneCd, d.districtCd, cr.dlrCd, d.dbaNm, cr.contactReportId, cr.contactDt, cr.contactAuthor,cr.contactStatus,cr.currentIssues) "
			+ "FROM Dealers d JOIN d.CRI cr WHERE cr.currentIssues IN :currentIssues AND cr.dlrCd=:dlrCd")
	public List<ReportByDealershipDto> findByDlrCd(@Param("dlrCd") String dlrCd,
			@Param("currentIssues") List<String> currentIssues);
	
	@Query(value = "SELECT new com.mnao.mfp.cr.dto.ReportByDealershipDto"
			+ "(d.rgnCd, d.zoneCd, d.districtCd, cr.dlrCd, d.dbaNm, cr.contactReportId, cr.contactDt, cr.contactAuthor,cr.contactStatus,cr.currentIssues) "
			+ "FROM Dealers d JOIN d.CRI cr WHERE cr.dlrCd=:dlrCd")
	public List<ReportByDealershipDto> findCurrentIssuesByDlrCd(@Param("dlrCd") String dlrCd);

	public void deleteByContactReportIdAndContactStatus(@Param("contactReportId") long contactReportId,
			int contactStatus);

	public List<ContactReportInfo> findByDlrCd(String dlrCd);

	public List<ContactReportInfo> findByDlrCdInAndContactStatusNot(List<String> dlrCd, int status);

	List<ContactReportInfo> findByContactAuthorAndIsActive(String authorId, String isActive);

	List<ContactReportInfo> findByContactDtBetween(LocalDate startDate, LocalDate endDate);
	
	List<ContactReportInfo> findByCurrentIssuesContaining(String issue);
	
	List<ContactReportInfo> findByCurrentIssuesNotNull();
	
	List<ContactReportInfo> findByCurrentIssuesNotNullAndContactDtNotNull();
	
	List<ContactReportInfo> findByContactDtBetweenAndContactStatusGreaterThan(LocalDate startDate, LocalDate endDate, Integer status);

}
