package com.mnao.mfp.cr.service;

import java.util.List;
import java.util.Map;

import javax.validation.Valid;

import com.mnao.mfp.cr.dto.ContactReportDto;
import com.mnao.mfp.cr.dto.ContactReportInfoDto;
import com.mnao.mfp.cr.dto.ContactReportTopicDto;
import com.mnao.mfp.cr.model.DealersByIssue;
import com.mnao.mfp.user.dao.MFPUser;

public interface ContactReportService {

    ContactReportDto findByContactReportId(long contactReportId, MFPUser mfpUser);

    String deleteReportById(long contactReportId, MFPUser mfpUser) throws Exception;

    List<DealersByIssue> getAllDealersByIssue();

    Map<String, List<ContactReportInfoDto>> getMyContactReport(MFPUser mfpUser, boolean showUsersDraft);

	String submitReportDataV2(@Valid ContactReportInfoDto report, MFPUser mfpUser, String currURL) throws Exception;

	List<ContactReportTopicDto> fetchSalesServiceOthersBasedOnTypes(List<String> contactTypeList);
	
}
