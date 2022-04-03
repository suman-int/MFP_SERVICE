package com.mnao.mfp.pdf.service;

import com.mnao.mfp.common.datafilters.FilterCriteria;
import com.mnao.mfp.user.dao.MFPUser;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;

import javax.servlet.http.HttpServletRequest;

public interface ContactReportPDFService {
    ResponseEntity<Resource> createBulkPdfByFilterCriteria(FilterCriteria filterCriteria, MFPUser mfpUser, HttpServletRequest request);

    ResponseEntity<Resource> createBulkExcelReportByFilterCriteria(FilterCriteria filterCriteria, MFPUser mfpUser, HttpServletRequest request);
}
