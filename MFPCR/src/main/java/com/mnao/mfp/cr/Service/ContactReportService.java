package com.mnao.mfp.cr.Service;

import java.util.List;
import java.util.Map;

import com.mnao.mfp.cr.dto.ContactReportDto;
import com.mnao.mfp.cr.dto.ContactReportInfoDto;
import com.mnao.mfp.cr.entity.ContactReportInfo;
import com.mnao.mfp.cr.model.DealersByIssue;
import com.mnao.mfp.user.dao.MFPUser;

public interface ContactReportService {

     String submitReportData(ContactReportInfoDto report, MFPUser mfpUser) throws Exception;

     ContactReportDto findByContactReportId(long ContactreporId);

     void deleteReportById(long contactReportId);

	List<DealersByIssue> getAllDealersByIssue();

	Map<String, List<ContactReportInfoDto>> getMyContactReport(String userId);
}
