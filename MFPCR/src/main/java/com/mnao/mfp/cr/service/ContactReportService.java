package com.mnao.mfp.cr.service;

import com.mnao.mfp.cr.dto.ContactReportDto;
import com.mnao.mfp.cr.dto.ContactReportInfoDto;
import com.mnao.mfp.cr.dto.ContactReportTopicDto;
import com.mnao.mfp.cr.model.DealersByIssue;
import com.mnao.mfp.user.dao.MFPUser;

import java.util.List;
import java.util.Map;

import javax.mail.MessagingException;
import javax.validation.Valid;

public interface ContactReportService {

    String submitReportData(ContactReportInfoDto report, MFPUser mfpUser) throws Exception;

    ContactReportDto findByContactReportId(long contactReportId);

    void deleteReportById(long contactReportId);

    List<DealersByIssue> getAllDealersByIssue();

    Map<String, List<ContactReportInfoDto>> getMyContactReport(String userId, boolean showUsersDraft);

	String submitReportDataV2(@Valid ContactReportInfoDto report, MFPUser mfpUser, String currURL) throws Exception;

	List<ContactReportTopicDto> fetchSalesServiceOthersBasedOnTypes(List<String> contactTypeList);
	
}
