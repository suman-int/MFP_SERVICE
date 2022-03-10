package com.mnao.mfp.cr.Service;

import com.mnao.mfp.cr.dto.ContactReportDto;
import com.mnao.mfp.cr.entity.ContactReportInfo;

public interface ContactReportService {

     ContactReportInfo submitReportData(ContactReportInfo report);

     ContactReportDto findByContactReportId(long ContactreporId);

     void deleteReportById(long contactReportId);
}
