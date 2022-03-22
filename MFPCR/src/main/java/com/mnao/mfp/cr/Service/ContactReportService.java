package com.mnao.mfp.cr.Service;

import com.mnao.mfp.cr.dto.ContactReportDto;
import com.mnao.mfp.cr.entity.ContactReportInfo;
import com.mnao.mfp.user.dao.MFPUser;

public interface ContactReportService {

     String submitReportData(ContactReportInfo report, MFPUser mfpUser);

     ContactReportDto findByContactReportId(long ContactreporId);

     void deleteReportById(long contactReportId);
}
