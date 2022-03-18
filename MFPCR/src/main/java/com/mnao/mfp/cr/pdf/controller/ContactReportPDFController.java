package com.mnao.mfp.cr.pdf.controller;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Path;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.SessionAttribute;

import com.mnao.mfp.common.controller.MfpKPIControllerBase;
import com.mnao.mfp.common.dao.DealerFilter;
import com.mnao.mfp.common.util.AppConstants;
import com.mnao.mfp.cr.entity.ContactReportDealerPersonnel;
import com.mnao.mfp.cr.entity.ContactReportInfo;
import com.mnao.mfp.cr.pdf.dao.DealerEmployeeInfo;
import com.mnao.mfp.cr.pdf.dao.DealerInfo;
import com.mnao.mfp.cr.pdf.dao.ReviewerEmployeeInfo;
import com.mnao.mfp.cr.pdf.generate.PDFCRMain;
import com.mnao.mfp.cr.pdf.service.PDFService;
import com.mnao.mfp.list.controller.ListController;
import com.mnao.mfp.list.service.ListService;
import com.mnao.mfp.list.service.MMAListService;
import com.mnao.mfp.user.dao.MFPUser;
import com.mnao.mfp.user.service.UserDetailsService;

@RestController
@RequestMapping(path = "/ContactReport")
public class ContactReportPDFController extends MfpKPIControllerBase {
	//
	private static final Logger log = LoggerFactory.getLogger(ContactReportPDFController.class);

	@GetMapping(value = "/downloadPDF")
	public ResponseEntity<Resource> createPDF(@SessionAttribute(name = "mfpUser") MFPUser mfpUser,
			@RequestBody ContactReportInfo report, HttpServletRequest request) {
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

}
