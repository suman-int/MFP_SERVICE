package com.mnao.mfp.pdf.controller;

import com.mnao.mfp.common.controller.MfpKPIControllerBase;
import com.mnao.mfp.common.datafilters.FilterCriteria;
import com.mnao.mfp.cr.entity.ContactReportInfo;
import com.mnao.mfp.cr.service.ContactReportSummaryService;
import com.mnao.mfp.pdf.service.ContactReportPDFService;
import com.mnao.mfp.pdf.service.PDFService;
import com.mnao.mfp.cr.service.impl.ContactInfoServiceImpl;
import com.mnao.mfp.cr.util.FilterCriteriaBuilder;
import com.mnao.mfp.user.dao.MFPUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping(value = "/ContactReport")
public class ContactReportPDFController extends MfpKPIControllerBase {
	private static final Logger log = LoggerFactory.getLogger(ContactReportPDFController.class);

	@Autowired
	ContactInfoServiceImpl cInfoServ;

	@Autowired
	ContactReportPDFService contactReportPDFService;
	
	@Autowired
	PDFService prfService;

	@PostMapping(value = "/downloadPDF")
	public ResponseEntity<Resource> createPDF(@SessionAttribute(name = "mfpUser") MFPUser mfpUser,
			@Valid @RequestBody ContactReportInfo report, HttpServletRequest request) {
		try {
			return contactReportPDFService.createPdf(mfpUser, request, report);
		} catch (Exception exp) {
			return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@PostMapping(value = "/downloadBulkPDF")
	public ResponseEntity<Resource> createBulkPDF(@SessionAttribute(name = "mfpUser") MFPUser mfpUser,
			@RequestBody FilterCriteria filterCriteria, HttpServletRequest request,
			@RequestParam(required = false) String regionId, @RequestParam(required = false) String zoneId,
			@RequestParam(required = false) String districtId, @RequestParam(required = false) String dealerId,
			@RequestParam(required = false) String issues, @RequestParam(required = false) String startOf,
			@RequestParam(required = false) String endOf) {
		try {
			FilterCriteria filterCriteria1 = FilterCriteriaBuilder.buildFilterByLocationAndIssueAndTiming(regionId, zoneId, districtId, dealerId, issues, startOf, endOf);
			return contactReportPDFService.createBulkPdfByFilterCriteria(filterCriteria1, mfpUser, request);
		} catch (Exception exp) {
			return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@PostMapping(value = "/downloadStatusXLS")
	public ResponseEntity<Resource> downloadStatusXLSF(@SessionAttribute(name = "mfpUser") MFPUser mfpUser,
			@RequestBody FilterCriteria filterCriteria, @RequestParam(required = false) String issues,
			@RequestParam(required = false) String startOf, @RequestParam(required = false) String endOf,
			HttpServletRequest request) {
		try {
			FilterCriteria filterCriteria1 = FilterCriteriaBuilder.buildFilterByIssueAndTiming(issues, startOf, endOf);

			return contactReportPDFService.createBulkExcelReportByFilterCriteria(filterCriteria1, mfpUser, request);
		} catch (Exception exp) {
			return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
}
