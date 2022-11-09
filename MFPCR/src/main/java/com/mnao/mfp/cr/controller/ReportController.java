package com.mnao.mfp.cr.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.SessionAttribute;
import org.springframework.web.multipart.MultipartFile;

import com.mnao.mfp.common.dto.CommonResponse;
import com.mnao.mfp.common.service.AbstractService;
import com.mnao.mfp.common.util.HttpUtils;
import com.mnao.mfp.common.util.NullCheck;
import com.mnao.mfp.cr.dto.ContactInfoAttachmentDto;
import com.mnao.mfp.cr.dto.ContactReportInfoDto;
import com.mnao.mfp.cr.dto.ContactReportTopicDto;
import com.mnao.mfp.cr.model.ContactReportResponse;
import com.mnao.mfp.cr.service.ContactReportService;
import com.mnao.mfp.cr.service.GenericResponseWrapper;
import com.mnao.mfp.cr.service.impl.FileHandlingServiceImpl;
import com.mnao.mfp.user.dao.MFPUser;

@RestController
@RequestMapping(value = "ContactReport")
public class ReportController {
	//
	private static final Logger log = LoggerFactory.getLogger(ReportController.class);
	//

	@Autowired()
	private ContactReportService contactReportService;

	@Autowired()
	private FileHandlingServiceImpl fileHandlingService;

	@PostMapping(value = "submitReport")
	public CommonResponse<String> submitReportData(@Valid @RequestBody ContactReportInfoDto report,
			@SessionAttribute(name = "mfpUser") MFPUser mfpUser, HttpServletRequest request) {
		try {
			String currHost = HttpUtils.getRequestIP(request);
//			currHost = currHost.substring(0, currHost.indexOf("ContactReport"));
			String reportDataV2 = contactReportService.submitReportDataV2(report, mfpUser, currHost);
			return AbstractService.httpPostSuccess(reportDataV2, "Success");
		} catch (Exception e) {
			return AbstractService.httpPostError(e);
		}
	}

	@PostMapping(value = "deleteReportById")
	public CommonResponse<String> deleteReportById(@RequestBody long contactReportId,
			@SessionAttribute(name = "mfpUser") MFPUser mfpUser, HttpServletRequest request) {
		try {
			String status = contactReportService.deleteReportById(contactReportId, mfpUser);
			return AbstractService.httpPostSuccess(status, "Success");
		} catch (Exception e) {
			return AbstractService.httpPostError(e);
		}
	}

	@PostMapping(value = "deleteAttachmentById/{attachmentId}")
	public CommonResponse<String> deleteReportAttachmentById(@PathVariable long attachmentId) {
		String response = fileHandlingService.deleteAttachmentById(attachmentId);
		return AbstractService.httpPostSuccess(response, "Success");
	}

	@RequestMapping(value = "deleteAttachmentByPath", method = RequestMethod.POST)
	public CommonResponse<String> deleteReportAttachmentByPath(@RequestBody Map<String, String> inputObject) {
		String response = fileHandlingService.deleteAttachmentByAttachmentPath(inputObject.get("fileName"));
		return AbstractService.httpPostSuccess(response, "Success");
	}

	@GetMapping(value = "getReportById/{contactReportId}")
	public ContactReportResponse getReportById(@PathVariable long contactReportId,
			@SessionAttribute(name = "mfpUser") MFPUser mfpUser) {
		return GenericResponseWrapper.contactReportResponseFunction
				.apply(contactReportService.findByContactReportId(contactReportId, mfpUser), null);

	}

	@GetMapping(value = "getReportsByUserID/{userId}", produces = MediaType.APPLICATION_JSON_VALUE)
	public CommonResponse<Map<String, List<ContactReportInfoDto>>> getMyContactReport(
			@PathVariable("userId") String userId, @RequestParam(required = false) boolean showUsersDraft,
			@SessionAttribute(name = "mfpUser") MFPUser mfpUser) {

		try {
			Map<String, List<ContactReportInfoDto>> response = contactReportService.getMyContactReport(mfpUser,
					showUsersDraft);
			return AbstractService.httpPostSuccess(response, "Success Response");

		} catch (Exception e) {
			return AbstractService.httpPostError(e);
		}
	}

	@PostMapping(value = "uploadFile")
	public CommonResponse<List<ContactInfoAttachmentDto>> uploadFile(@RequestParam("files") MultipartFile[] files,
			HttpServletRequest request) {

		List<ContactInfoAttachmentDto> retRows = fileHandlingService.doUpload(files, request);
		return AbstractService.httpPostSuccess(retRows, "Success");
//    	return fileHandlingService.doUpload(files,request);
	}

	@GetMapping(value = "downloadFileByPath/**")
	public ResponseEntity<Resource> downloadFileUsingFileName(HttpServletRequest request) {
		String uri = request.getRequestURI();
		String fnm = uri.split("/downloadFileByPath/")[1];
		return downloadFileUsingFileName(fnm, request);
	}

//    @GetMapping(value = "downloadFileByPath/{fileName:.+}")
//    public ResponseEntity<Resource> downloadFileUsingFileName(@PathVariable String fileName,
//                                                              HttpServletRequest request) {
	public ResponseEntity<Resource> downloadFileUsingFileName(String fileName, HttpServletRequest request) {
		// Load file as Resource
		Resource resource = fileHandlingService.loadFileAsResource(fileName);

		// Try to determine file's content type
		String contentType = null;
		if (resource != null) {
			try {
				contentType = request.getServletContext().getMimeType(resource.getFile().getAbsolutePath());
			} catch (IOException ex) {
				log.debug("Could not determine file type.");
			}

			// Fallback to the default content type if type could not be determined
			if (contentType == null) {
				contentType = "application/octet-stream";
			}
			String attchName = resource.getFilename();
			if (attchName.startsWith("-")) {
				attchName = attchName.replace('-', 'm');
			}
			return ResponseEntity.ok().contentType(MediaType.parseMediaType(contentType))
					.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + attchName + "\"")
					.body(resource);

		} else {
			return new ResponseEntity<>(HttpStatus.UNPROCESSABLE_ENTITY);
		}
	}

	@GetMapping(value = "downloadFileById/{attachmentId}")
	public ResponseEntity<Resource> downloadFileUsingId(@PathVariable long attachmentId, HttpServletRequest request) {
		// Load file as Resource
		Resource resource = fileHandlingService.loadFileAsResource(attachmentId);

		// Try to determine file's content type
		String contentType = null;
		if (resource != null) {
			try {
				contentType = request.getServletContext().getMimeType(resource.getFile().getAbsolutePath());
			} catch (IOException ex) {
				log.debug("Could not determine file type.");
			}

			// Fallback to the default content type if type could not be determined
			if (contentType == null) {
				contentType = "application/octet-stream";
			}

			return ResponseEntity.ok().contentType(MediaType.parseMediaType(contentType))
					.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
					.body(resource);

		} else {
			return new ResponseEntity<>(HttpStatus.UNPROCESSABLE_ENTITY);
		}
	}

	@GetMapping(value = "/contact-type")
	public CommonResponse<List<ContactReportTopicDto>> fetchSalesServiceOthers(
			@RequestParam(name = "type", required = false) String contactType) {
		try {
			List<String> contactTypeList = new ArrayList<>(0);
			if (!new NullCheck<String>(contactType).isNotNullOrEmpty()) {
				contactTypeList.addAll(Arrays.asList("sales", "service", "other"));
			} else {
				contactTypeList.addAll(Arrays.asList(contactType.split(",")));
			}
			List<ContactReportTopicDto> result = contactReportService
					.fetchSalesServiceOthersBasedOnTypes(contactTypeList);
			return AbstractService.httpPostSuccess(result, "Success");
		} catch (Exception exp) {
			return AbstractService.httpPostError(exp);
		}
	}

}