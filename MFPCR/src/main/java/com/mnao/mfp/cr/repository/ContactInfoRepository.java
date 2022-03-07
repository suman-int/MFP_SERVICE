package com.mnao.mfp.cr.repository;

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
public interface ContactInfoRepository extends JpaRepository<ContactReportInfo, Long>{

//	@Modifying
//	@Query(value = "update cr from ContactReportInfo cr set cr.contactDt='2021-12-13' where cr.contactReportId=:contactReportId and cr.contactStatus=0")
//	public String updateContactReportById(@Param("report") ContactReportInfo report,@Param("contactReportId") long contactReportId);

	public ContactReportInfo findByContactReportId(@Param("contactReportId") long contactReportId);

@Query(value = "SELECT new com.mnao.mfp.cr.dto.ReportByDealershipDto" +
		"(d.rgnCd, d.zoneCd, d.districtCd, cr.dlrCd, d.dlrNm, cr.contactReportId, cr.contactDt, cr.contactAuthor,cr.contactStatus,cr.currentIssues) " +
		"FROM Dealers d JOIN d.CRI cr WHERE cr.currentIssues IN :currentIssues AND cr.dlrCd=:dlrCd")
	public List<ReportByDealershipDto> findByDlrCd(@Param("dlrCd") String dlrCd, @Param("currentIssues") List<String> currentIssues);

	public void deleteByContactReportIdAndContactStatus(@Param("contactReportId") long contactReportId, int contactStatus);

	public List<ContactReportInfo> findByDlrCd(String dlrCd);

	public List<ContactReportInfo> findByDlrCdIn(List<String> dlrCd);

	List<ContactReportInfo> findByContactAuthor(String authorId);

}
	
