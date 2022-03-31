package com.mnao.mfp.cr.pdf.controller;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.SessionAttribute;

import com.mnao.mfp.common.controller.MfpKPIControllerBase;
import com.mnao.mfp.cr.service.ContactInfoServiceImpl;
import com.mnao.mfp.cr.dto.FilterCriteria;
import com.mnao.mfp.cr.entity.ContactReportInfo;
import com.mnao.mfp.cr.pdf.service.PDFService;
import com.mnao.mfp.user.dao.MFPUser;

@RestController
@RequestMapping(value = "/ContactReport")
public class ContactReportPDFController extends MfpKPIControllerBase {
	//
	private static final Logger log = LoggerFactory.getLogger(ContactReportPDFController.class);
	@Autowired
	ContactInfoServiceImpl cInfoServ;

	@PostMapping(value = "/downloadPDF")
	public ResponseEntity<Resource> createPDF(@SessionAttribute(name = "mfpUser") MFPUser mfpUser,
			@Valid @RequestBody ContactReportInfo report, HttpServletRequest request) {
		PDFService service = new PDFService();
		Resource pdfRes = service.createPDFResource(mfpUser, report);
		if (pdfRes != null) {
			String contentType = null;
			try {
				contentType = request.getServletContext().getMimeType(pdfRes.getFile().getAbsolutePath());
			} catch (IOException ex) {
				System.out.println("Could not determine file type.");
			}
			// Fallback to the default content type if type could not be determined
			if (contentType == null) {
				contentType = "application/octet-stream";
			}
			return ResponseEntity.ok().contentType(MediaType.parseMediaType(contentType))
					.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + pdfRes.getFilename() + "\"")
					.body(pdfRes);
		} else {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
	}

	@PostMapping(value = "/downloadBulkPDF")
	public ResponseEntity<Resource> createBulkPDF(@SessionAttribute(name = "mfpUser") MFPUser mfpUser,
			// @RequestBody List<ContactReportInfo> report, HttpServletRequest request) {
			@RequestBody FilterCriteria filterCriteria, HttpServletRequest request) {
//		ContactInfoServiceImpl cInfoServ = new ContactInfoServiceImpl();
		List<ContactReportInfo> report = cInfoServ.filterContactReportsBasedOnFilter(filterCriteria);
		PDFService service = new PDFService();
		Resource pdfRes = service.createBulkPDFResource(mfpUser, report);
		if (pdfRes != null) {
			String contentType = null;
			try {
				contentType = request.getServletContext().getMimeType(pdfRes.getFile().getAbsolutePath());
			} catch (IOException ex) {
				System.out.println("Could not determine file type.");
			}
			// Fallback to the default content type if type could not be determined
			if (contentType == null) {
				contentType = "application/octet-stream";
			}
			return ResponseEntity.ok().contentType(MediaType.parseMediaType(contentType))
					.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + pdfRes.getFilename() + "\"")
					.body(pdfRes);
		} else {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
	}

	@PostMapping(value = "/downloadStatusXLS")
	public ResponseEntity<Resource> downloadStatusXLSF(@SessionAttribute(name = "mfpUser") MFPUser mfpUser,
			// @RequestBody List<ContactReportInfo> report, HttpServletRequest request) {
			@RequestBody FilterCriteria filterCriteria, HttpServletRequest request) {
//		ContactInfoServiceImpl cInfoServ = new ContactInfoServiceImpl();
		List<ContactReportInfo> report = cInfoServ.filterContactReportsBasedOnFilter(filterCriteria);
		PDFService service = new PDFService();
		Resource pdfRes = null;
		try {
			pdfRes = service.createXLSFResource(mfpUser, report);
		} catch (Exception e) {
			e.printStackTrace();
			Path excFile = service.getTmpFilePath(mfpUser, "ERROR_", "ExcelConversion", "txt");
			try {
				Files.write(excFile, Arrays.toString(e.getStackTrace()).getBytes(), StandardOpenOption.WRITE);
				pdfRes = new UrlResource(excFile.toUri());
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}
		if (pdfRes != null) {
			String contentType = null;
			try {
				contentType = request.getServletContext().getMimeType(pdfRes.getFile().getAbsolutePath());
			} catch (IOException ex) {
				System.out.println("Could not determine file type.");
			}
			// Fallback to the default content type if type could not be determined
			if (contentType == null) {
				contentType = "application/octet-stream";
			}
			return ResponseEntity.ok().contentType(MediaType.parseMediaType(contentType))
					.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + pdfRes.getFilename() + "\"")
					.body(pdfRes);
		} else {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
	}
}
