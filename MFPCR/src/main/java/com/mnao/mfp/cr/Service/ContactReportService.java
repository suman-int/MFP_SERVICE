package com.mnao.mfp.cr.Service;

import com.mnao.mfp.cr.dto.ContactReportDto;

public interface ContactReportService {

     String submitReportData(ContactReportDto report);

     ContactReportDto findByContactReportId(long ContactreporId);

     void deleteReportById(long contactReportId);
}
