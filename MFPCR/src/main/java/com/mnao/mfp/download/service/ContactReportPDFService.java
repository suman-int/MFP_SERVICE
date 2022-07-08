package com.mnao.mfp.download.service;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Path;

import javax.servlet.http.HttpServletRequest;

import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;

import com.lowagie.text.DocumentException;
import com.mnao.mfp.common.datafilters.FilterCriteria;
import com.mnao.mfp.cr.entity.ContactReportInfo;
import com.mnao.mfp.user.dao.MFPUser;

public interface ContactReportPDFService {
	// Bulk PDF
	Path createBulkPdfByFilterCriteria(FilterCriteria filterCriteria, MFPUser mfpUser)
			throws DocumentException, FileNotFoundException, IOException;

	ResponseEntity<Resource> createBulkPdfByFilterCriteria(FilterCriteria filterCriteria, MFPUser mfpUser,
			HttpServletRequest request) throws DocumentException, FileNotFoundException, IOException;

	//Bulk XLSX
	Path createBulkExcelReportByFilterCriteria(FilterCriteria filterCriteria, MFPUser mfpUser);

	ResponseEntity<Resource> createBulkExcelReportByFilterCriteria(FilterCriteria filterCriteria, MFPUser mfpUser,
			HttpServletRequest request) throws MalformedURLException;

	// Single PDF of one CR
	ResponseEntity<Resource> createPdf(MFPUser mfpUser, HttpServletRequest request, ContactReportInfo report)
			throws DocumentException, FileNotFoundException, IOException;
}
