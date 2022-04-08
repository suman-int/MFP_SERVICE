package com.mnao.mfp.pdf.service;

import com.mnao.mfp.common.datafilters.FilterCriteria;
import com.mnao.mfp.cr.entity.ContactReportInfo;
import com.mnao.mfp.cr.repository.ContactInfoRepository;
import com.mnao.mfp.cr.service.ContactReportSummaryService;
import com.mnao.mfp.cr.service.impl.ContactInfoServiceImpl;
import com.mnao.mfp.cr.util.DataOperationFilter;
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
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.servlet.http.HttpServletRequest;

import static com.mnao.mfp.common.util.Utils.isNotNullOrEmpty;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.List;

@Service
public class ContactReportPDFServiceImpl implements ContactReportPDFService {

	private static final Logger log = LoggerFactory.getLogger(ContactReportPDFServiceImpl.class);

	@Autowired
	private ContactInfoRepository contactInfoRepository;

	@Autowired
	private DataOperationFilter dataOperationFilter;

	@Override
	public ResponseEntity<Resource> createBulkPdfByFilterCriteria(FilterCriteria filter, MFPUser mfpUser,
			HttpServletRequest request) {
		List<ContactReportInfo> contactReports = contactInfoRepository.findAll();
		if (!CollectionUtils.isEmpty(filter.getIssuesFilter())) {
			contactReports = dataOperationFilter.filterContactReportsByIssues(filter, contactReports);
		}
		if (isNotNullOrEmpty(filter.getStartDate()) && isNotNullOrEmpty(filter.getEndDate())) {
			contactReports = dataOperationFilter.filterContactReportsByDateRange(filter, contactReports);
		}
		if (isNotNullOrEmpty(filter.getRgnCd()) || isNotNullOrEmpty(filter.getZoneCd())
				|| isNotNullOrEmpty(filter.getDistrictCd()) || isNotNullOrEmpty(filter.getDlrCd())) {
			contactReports = dataOperationFilter.filterContactReportsByLocation(filter, contactReports, mfpUser);
		}
		PDFService service = new PDFService();
		Resource pdfRes = service.createBulkPDFResource(mfpUser, contactReports);
		if (pdfRes != null) {
			String contentType = null;
			try {
				contentType = request.getServletContext().getMimeType(pdfRes.getFile().getAbsolutePath());
			} catch (IOException ex) {
				log.error("Could not determine file type.");
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

	@Override
	public ResponseEntity<Resource> createBulkExcelReportByFilterCriteria(FilterCriteria filter, MFPUser mfpUser,
			HttpServletRequest request) {
		List<ContactReportInfo> contactReports = contactInfoRepository.findAll();
		if (!CollectionUtils.isEmpty(filter.getIssuesFilter())) {
			contactReports = dataOperationFilter.filterContactReportsByIssues(filter, contactReports);
		}
		if (isNotNullOrEmpty(filter.getStartDate()) && isNotNullOrEmpty(filter.getEndDate())) {
			contactReports = dataOperationFilter.filterContactReportsByDateRange(filter, contactReports);
		}
		PDFService service = new PDFService();
		Resource pdfRes = null;
		try {
			pdfRes = service.createXLSFResource(mfpUser, contactReports);
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
				log.error("Could not determine file type.");
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
