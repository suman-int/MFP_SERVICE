package com.mnao.mfp.download.controller;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.SessionAttribute;

import com.mnao.mfp.common.controller.MfpKPIControllerBase;
import com.mnao.mfp.common.datafilters.FilterCriteria;
import com.mnao.mfp.common.dto.CommonResponse;
import com.mnao.mfp.common.service.AbstractService;
import com.mnao.mfp.cr.entity.ContactReportInfo;
import com.mnao.mfp.cr.service.impl.ContactInfoServiceImpl;
import com.mnao.mfp.cr.util.FilterCriteriaBuilder;
import com.mnao.mfp.download.service.BackgroundExecService;
import com.mnao.mfp.download.service.BackgroundExecService.ExportType;
import com.mnao.mfp.download.service.ContactReportPDFService;
import com.mnao.mfp.user.dao.MFPUser;

@RestController
@RequestMapping(value = "/ContactReport")
public class ContactReportPDFController extends MfpKPIControllerBase {
	private static final Logger log = LoggerFactory.getLogger(ContactReportPDFController.class);

	@Autowired
	ContactInfoServiceImpl cInfoServ;

	@Autowired
	ContactReportPDFService contactReportPDFService;
	
	@Autowired
	BackgroundExecService backgroundEmailService;
	
//	@Autowired
//	PDFService prfService;

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
	public CommonResponse<String>  createBulkPDFV2(@SessionAttribute(name = "mfpUser") MFPUser mfpUser,
			@RequestBody FilterCriteria filterCriteria, HttpServletRequest request,
			@RequestParam(required = false) String regionId, @RequestParam(required = false) String zoneId,
			@RequestParam(required = false) String districtId, @RequestParam(required = false) String dealerId,
			@RequestParam(required = false) String issues, @RequestParam(required = false) String startOf,
			@RequestParam(required = false) String endOf) {
		try {
			FilterCriteria filterCriteria1 = FilterCriteriaBuilder.buildFilterByLocationAndIssueAndTiming(regionId, zoneId, districtId, dealerId, issues, startOf, endOf);
			backgroundEmailService.startBackgroundExport(filterCriteria1, mfpUser, ExportType.PDF);
			return AbstractService.httpPostSuccess("Exported PDF will be sent to you by email once it is finished.", "Success");
		} catch (Exception exp) {
			return AbstractService.httpPostError(exp);
		}
	}

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
	public CommonResponse<String>  downloadStatusXLSXV2(@SessionAttribute(name = "mfpUser") MFPUser mfpUser,
			@RequestBody FilterCriteria filterCriteria, @RequestParam(required = false) String issues,
			@RequestParam(required = false) String startOf, @RequestParam(required = false) String endOf,
			HttpServletRequest request) {
		try {
			FilterCriteria filterCriteria1 = FilterCriteriaBuilder.buildFilterByIssueAndTiming(issues, startOf, endOf);
			backgroundEmailService.startBackgroundExport(filterCriteria1, mfpUser, ExportType.EXCEL);
			return AbstractService.httpPostSuccess("Exported EXCEL will be sent to you by email once it is finished.", "Success");
		} catch (Exception exp) {
			return AbstractService.httpPostError(exp);
		}
	}

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
