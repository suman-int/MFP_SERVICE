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
import org.springframework.web.bind.annotation.PostMapping;
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
import com.mnao.mfp.list.controller.ListController;
import com.mnao.mfp.list.service.ListService;
import com.mnao.mfp.list.service.MMAListService;
import com.mnao.mfp.user.dao.MFPUser;
import com.mnao.mfp.user.service.UserDetailsService;

@RestController
@RequestMapping(path = "/ContactReport")
public class ContactReportPDFController extends MfpKPIControllerBase {
	//
	private static final Logger log = LoggerFactory.getLogger(ListController.class);

	@PostMapping(value = "/downloadPDF")
	public ResponseEntity<Resource> createPDF(@SessionAttribute(name = "mfpUser") MFPUser mfpUser,
			@RequestBody ContactReportInfo report, HttpServletRequest request) {
		Resource pdfRes = createPDFResource(mfpUser, report);
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

	private Resource createPDFResource(MFPUser mfpUser, ContactReportInfo report) {
		String baseFileName = "contact_report_" + report.getContactReportId();
		File tmpFile = null;
		try {
			tmpFile = File.createTempFile(baseFileName, ".pdf");
		} catch (IOException e1) {
			e1.printStackTrace();
			return null;
		}
		Path filePath = tmpFile.toPath();
		PDFCRMain pdfMain = new PDFCRMain();
		DealerInfo dInfo = getDealerInfo(mfpUser, report.getDlrCd());
		List<DealerEmployeeInfo> dEmpInfos = getDealerEmployeeInfos(mfpUser, report.getDlrCd(),
				report.getDealerPersonnels());
		ReviewerEmployeeInfo revEmpInfo = getReviewerEmployeeInfos(mfpUser, report.getContactReviewer());
		MFPUser author = getAuthorUser(mfpUser, report.getContactAuthor());
		try {
			pdfMain.createPdfFile(filePath, report, author, dInfo, dEmpInfos, revEmpInfo);
		} catch (IOException e) {
			e.printStackTrace();
		}
		Resource resource = null;
		try {
			resource = new UrlResource(filePath.toUri());
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		return resource;
	}

	private MFPUser getAuthorUser(MFPUser mfpUser, String contactAuthor) {
		UserDetailsService uds = new UserDetailsService();
		MFPUser musr = uds.getMFPUser(contactAuthor);
		return musr;
	}

	private ReviewerEmployeeInfo getReviewerEmployeeInfos(MFPUser mfpUser, String contactReviewer) {
		ReviewerEmployeeInfo revEmp = null;
		if (contactReviewer != null) {
			String sqlName = getKPIQueryFilePath(AppConstants.SQL_LIST_REVIEWER_EMPLOYEES);
			MMAListService<ReviewerEmployeeInfo> service = new MMAListService<ReviewerEmployeeInfo>();
			List<ReviewerEmployeeInfo> retRows = null;
			DealerFilter df = new DealerFilter(mfpUser, null, mfpUser.getRgnCd(), null, null, null);
			try {
				retRows = service.getListData(sqlName, ReviewerEmployeeInfo.class, df, mfpUser.getRgnCd());
			} catch (InstantiationException | IllegalAccessException | ParseException e) {
				log.error("ERROR retrieving list of Employees:", e);
			}
			if ((retRows != null) && retRows.size() > 0) {
				revEmp = getReviewerEmployeeInfo(retRows, contactReviewer);
			}
		}
		return revEmp;
	}

	private ReviewerEmployeeInfo getReviewerEmployeeInfo(List<ReviewerEmployeeInfo> retRows, String contactReviewer) {
		for (int i = 0; i < retRows.size(); i++) {
			ReviewerEmployeeInfo rei = retRows.get(i);
			if (rei.getPrsnIdCd().equals(contactReviewer)) {
				return rei;
			}
		}
		return null;
	}

	private List<DealerEmployeeInfo> getDealerEmployeeInfos(MFPUser mfpUser, String dlrCd,
			List<ContactReportDealerPersonnel> dPers) {
		List<DealerEmployeeInfo> dEmpInfos = new ArrayList<DealerEmployeeInfo>();
		if (dPers != null) {
			String sqlName = getKPIQueryFilePath(AppConstants.SQL_LIST_DEALER_EMPLOYEES);
			ListService<DealerEmployeeInfo> service = new ListService<DealerEmployeeInfo>();
			List<DealerEmployeeInfo> retRows = null;
			DealerFilter df = new DealerFilter(mfpUser, dlrCd, null, null, null, null);
			try {
				retRows = service.getListData(sqlName, DealerEmployeeInfo.class, df);
			} catch (InstantiationException | IllegalAccessException | ParseException e) {
				log.error("ERROR retrieving list of Employees:", e);
			}
			if ((retRows != null) && retRows.size() > 0) {
				for (ContactReportDealerPersonnel dp : dPers) {
					DealerEmployeeInfo dei = getDealerEmployeeInfo(retRows, dp);
					if (dei != null) {
						dEmpInfos.add(dei);
					}
				}
			}
		}
		return dEmpInfos;
	}

	private DealerEmployeeInfo getDealerEmployeeInfo(List<DealerEmployeeInfo> retRows,
			ContactReportDealerPersonnel dp) {
		for (int i = 0; i < retRows.size(); i++) {
			DealerEmployeeInfo dei = retRows.get(i);
			if (dei.getPrsnIdCd().equals(dp.getPersonnelIdCd())) {
				return dei;
			}
		}
		return null;
	}

	private DealerInfo getDealerInfo(MFPUser mfpUser, String dlrCd) {
		DealerInfo dInfo = null;
		String sqlName = getKPIQueryFilePath(AppConstants.SQL_LIST_DEALERS);
		ListService<DealerInfo> service = new ListService<DealerInfo>();
		List<DealerInfo> retRows = null;
		DealerFilter df = new DealerFilter(mfpUser, dlrCd, null, null, null, null);
		try {
			retRows = service.getListData(sqlName, DealerInfo.class, df);
		} catch (InstantiationException | IllegalAccessException | ParseException e) {
			log.error("ERROR retrieving list of Dealers:", e);
		}
		if ((retRows != null) && (retRows.size() > 0)) {
			dInfo = retRows.get(0);
		}
		return dInfo;
	}
}
