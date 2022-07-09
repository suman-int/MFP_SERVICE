package com.mnao.mfp.download.service;

import static com.mnao.mfp.common.util.Utils.isNotNullOrEmpty;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import javax.mail.MessagingException;
import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import com.lowagie.text.DocumentException;
import com.mnao.mfp.common.datafilters.FilterCriteria;
import com.mnao.mfp.common.util.AppConstants;
import com.mnao.mfp.common.util.IsActiveEnum;
import com.mnao.mfp.common.util.Utils;
import com.mnao.mfp.cr.entity.ContactReportInfo;
import com.mnao.mfp.cr.repository.ContactInfoRepository;
import com.mnao.mfp.cr.util.ContactReportEnum;
import com.mnao.mfp.cr.util.DataOperationFilter;
import com.mnao.mfp.download.util.PdfGenerateUtil;
import com.mnao.mfp.email.EMazdamailsender;
import com.mnao.mfp.user.dao.MFPUser;

@Service
@Transactional(readOnly = true)
public class ContactReportPDFServiceImpl implements ContactReportPDFService {

	private static final Logger log = LoggerFactory.getLogger(ContactReportPDFServiceImpl.class);

	@Autowired
	private ContactInfoRepository contactInfoRepository;

	@Autowired
	private DataOperationFilter dataOperationFilter;

	@Autowired
	private PdfGenerateUtil pdfGenerateUtil;

	@Autowired
	private PdfNeoService neoService;

	@Autowired
	PDFService pdfService;

	@Override
	public Path createBulkPdfByFilterCriteria(FilterCriteria filter, MFPUser mfpUser)
			throws DocumentException, FileNotFoundException, IOException {
		Path filePath = null;
		List<ContactReportInfo> contactReports = contactInfoRepository.findByIsActive(IsActiveEnum.YES.getValue());
		contactReports = contactReports.stream()
				.filter(cr -> cr.getContactStatus() != ContactReportEnum.DRAFT.getStatusCode())
				.collect(Collectors.toList());
		if (!CollectionUtils.isEmpty(filter.getIssuesFilter())) {
			contactReports = dataOperationFilter.filterContactReportsByIssues(filter, contactReports);
		}
		if (isNotNullOrEmpty(filter.getStartDate()) && isNotNullOrEmpty(filter.getEndDate())) {
			contactReports = dataOperationFilter.filterContactReportsByDateRange(filter, contactReports);
		}
		contactReports = dataOperationFilter.filterContactReportsByLocation(filter, contactReports, mfpUser);
		contactReports.forEach(cr -> {
			cr.setDealerPersonnels(cr.getDealerPersonnels().stream()
					.filter(dp -> IsActiveEnum.YES.getValue().equalsIgnoreCase(dp.getIsActive()))
					.collect(Collectors.toList()));
		});
		if (contactReports.size() > 0) {
			filePath = generatePdfByReports(contactReports, mfpUser);
		}
		return filePath;
	}

	@Override
	public ResponseEntity<Resource> createBulkPdfByFilterCriteria(FilterCriteria filter, MFPUser mfpUser,
			HttpServletRequest request) throws DocumentException, FileNotFoundException, IOException {
		Path filePath = createBulkPdfByFilterCriteria(filter, mfpUser);
		return getResourceFromPath(filePath, request);
	}

	private Path generatePdfByReports(List<ContactReportInfo> contactReports, MFPUser mfpUser)
			throws DocumentException, FileNotFoundException, IOException {
		List<String> fullHtmlWithData = pdfGenerateUtil.replaceStringWithData(contactReports, mfpUser);
		List<InputStream> multiplePdf = new ArrayList<>();
		fullHtmlWithData.forEach(val -> {
			String transformedXml = neoService.htmlToXhtml(val);
			Path outputPath = neoService.getTmpFilePath(mfpUser, "contact_report_", "_BULK", ".pdf");
			neoService.xhtmlToPdf(transformedXml, outputPath);
			try {
				multiplePdf.add(new FileInputStream(outputPath.toFile()));
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				log.error("", e);
			}
		});
		Path outputPath = neoService.getTmpFilePath(mfpUser, "contact_report_", "_FINAL_BULK_", ".pdf");
		PdfNeoService.doMerge(multiplePdf, new FileOutputStream(outputPath.toFile()));
		//
		return outputPath;
	}

	@Override
	public Path createBulkExcelReportByFilterCriteria(FilterCriteria filter, MFPUser mfpUser) {
		Path filePath = null;
		List<ContactReportInfo> contactReports = contactInfoRepository.findByIsActive(IsActiveEnum.YES.getValue());
		if (!CollectionUtils.isEmpty(filter.getIssuesFilter())) {
			contactReports = dataOperationFilter.filterContactReportsByIssues(filter, contactReports);
		}
		if (isNotNullOrEmpty(filter.getStartDate()) && isNotNullOrEmpty(filter.getEndDate())) {
			contactReports = dataOperationFilter.filterContactReportsByDateRange(filter, contactReports);
		}
		try {
			filePath = pdfService.createXLSXFile(mfpUser, contactReports);
		} catch (Exception e) {
			log.error("", e);
			filePath = pdfService.getTmpFilePath(mfpUser, "ERROR_", "ExcelConversion", ".txt");
			try {
				Files.write(filePath, Arrays.toString(e.getStackTrace()).getBytes(), StandardOpenOption.WRITE);
			} catch (IOException e1) {
				log.error("", e1);
			}
		}
		return filePath;
	}

	// TO GO TO BACKGROUND
	@Override
	public ResponseEntity<Resource> createBulkExcelReportByFilterCriteria(FilterCriteria filter, MFPUser mfpUser,
			HttpServletRequest request) throws MalformedURLException {
		Path filePath = createBulkExcelReportByFilterCriteria(filter, mfpUser);
		return getResourceFromPath(filePath, request);
	}

	@Override
	public ResponseEntity<Resource> createPdf(MFPUser mfpUser, HttpServletRequest request, ContactReportInfo report)
			throws DocumentException, FileNotFoundException, IOException {
		List<ContactReportInfo> newList = new ArrayList<>(0);
		newList.add(report);
		Path filePath = generatePdfByReports(newList, mfpUser);
		return getResourceFromPath(filePath, request);
	}

	private ResponseEntity<Resource> getResourceFromPath(Path filePath, HttpServletRequest request)
			throws MalformedURLException {
		Resource pdfRes = new UrlResource(filePath.toUri());
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

	}

	@Override
	public void emailBulkPdfByFilterCriteria(FilterCriteria filterCriteria, MFPUser mfpUser) {
		Path fileName = null;
		try {
			fileName = createBulkPdfByFilterCriteria(filterCriteria, mfpUser);
			String resp = emailFileAttachment(mfpUser, fileName);
			log.info(resp);
		} catch (DocumentException | IOException | MessagingException e) {
			Path filePath = pdfService.getTmpFilePath(mfpUser, "ERROR_", "PDFConversion", ".txt");
			try {
				Files.write(filePath, Arrays.toString(e.getStackTrace()).getBytes(), StandardOpenOption.WRITE);
				String resp = emailFileAttachment(mfpUser, filePath);
				log.info(resp);
			} catch (IOException | MessagingException e1) {
				log.error("", e1);
			}

			log.error(
					"ERROR sending email to " + mfpUser.getEmail() + " with attachment " + fileName == null ? "UNKNOWN"
							: fileName.toString(),
					e);
		}
	}

	@Override
	public Path emailBulkExcelReportByFilterCriteria(FilterCriteria filterCriteria, MFPUser mfpUser) {
		Path fileName = createBulkExcelReportByFilterCriteria(filterCriteria, mfpUser);
		try {
			String resp = emailFileAttachment(mfpUser, fileName);
			log.info(resp);
		} catch (MessagingException e) {
			log.error("ERROR sending email to " + mfpUser.getEmail() + " with attachment " + fileName.toString(), e);
		}
		return null;
	}

	public String emailFileAttachment(MFPUser mfpUser, Path filePath) throws MessagingException {
		String resp = "OK";
		EMazdamailsender objEMazdamailsender = new EMazdamailsender();
		objEMazdamailsender.set_mimeType("text/html");
		String emailFrom = Utils.getAppProperty(AppConstants.REVIEW_MAIL_FROM);
		String subject = "Your requested download from Dealer Contact Report.";
		String body = "Please find attached your requested download from Dealer Contact Report: " ;
		if( filePath == null ) {
			body += "NO DATA AVAILABLE MATCHING YOUR SELECTION CRITERIA";
			resp = "NO File Created!";
		}
		else {
			body += filePath.getFileName();
		}
		String emailTo = mfpUser.getEmail();
		String[] to = new String[] { emailTo };
		String[] cc = new String[0];
		String[] bcc = new String[0];
		String[] att = null ;
		if( filePath != null ) {
			att = new String[] { filePath.toString() };
		}
		objEMazdamailsender.SendMazdaMail(emailFrom, to, cc, bcc, subject, body, att);
		return resp;
	}

}
