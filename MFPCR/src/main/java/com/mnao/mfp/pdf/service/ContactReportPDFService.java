package com.mnao.mfp.pdf.service;

import com.lowagie.text.DocumentException;
import com.mnao.mfp.common.datafilters.FilterCriteria;
import com.mnao.mfp.cr.entity.ContactReportInfo;
import com.mnao.mfp.user.dao.MFPUser;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;

import java.io.FileNotFoundException;
import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

public interface ContactReportPDFService {
	ResponseEntity<Resource> createBulkPdfByFilterCriteria(FilterCriteria filterCriteria, MFPUser mfpUser,
			HttpServletRequest request) throws DocumentException, FileNotFoundException, IOException;

	ResponseEntity<Resource> createBulkExcelReportByFilterCriteria(FilterCriteria filterCriteria, MFPUser mfpUser,
			HttpServletRequest request);

	ResponseEntity<Resource> createPdf(MFPUser mfpUser, HttpServletRequest request, ContactReportInfo report)
			throws DocumentException, FileNotFoundException, IOException;
}
