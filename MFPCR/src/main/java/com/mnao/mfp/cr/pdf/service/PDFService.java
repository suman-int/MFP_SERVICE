package com.mnao.mfp.cr.pdf.service;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Path;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;

import com.mnao.mfp.common.controller.MfpKPIControllerBase;
import com.mnao.mfp.common.dao.DealerFilter;
import com.mnao.mfp.common.util.AppConstants;
import com.mnao.mfp.cr.entity.ContactReportDealerPersonnel;
import com.mnao.mfp.cr.entity.ContactReportInfo;
import com.mnao.mfp.cr.pdf.controller.ContactReportPDFController;
import com.mnao.mfp.cr.pdf.dao.DealerEmployeeInfo;
import com.mnao.mfp.cr.pdf.dao.DealerInfo;
import com.mnao.mfp.cr.pdf.dao.ReviewerEmployeeInfo;
import com.mnao.mfp.cr.pdf.generate.PDFCRMain;
import com.mnao.mfp.cr.pdf.generate.PDFReport;
import com.mnao.mfp.list.service.ListService;
import com.mnao.mfp.list.service.MMAListService;
import com.mnao.mfp.user.dao.MFPUser;
import com.mnao.mfp.user.service.UserDetailsService;

public class PDFService extends MfpKPIControllerBase {
	private static final Logger log = LoggerFactory.getLogger(ContactReportPDFController.class);

	public Resource createPDFResource(MFPUser mfpUser, ContactReportInfo report) {
		//
		Path filePath = getTmpFilePath(mfpUser, report.getContactReportId());
		PDFReport pdfReport = new PDFReport("", "Contact Report", "Mazda North America Operations");
		try {
			pdfReport.openPdf(filePath.toString());
			createPDFDocument(pdfReport, report, mfpUser);
			pdfReport.closePdf();
		} catch (Exception e) {
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
	

	private void createPDFDocument(PDFReport pdfReport, ContactReportInfo report, MFPUser mfpUser) {
		PDFCRMain pdfMain = new PDFCRMain();
		DealerInfo dInfo = getDealerInfo(mfpUser, report.getDlrCd());
		List<DealerEmployeeInfo> dEmpInfos = getDealerEmployeeInfos(mfpUser, report.getDlrCd(),
				report.getDealerPersonnels());
		ReviewerEmployeeInfo revEmpInfo = getReviewerEmployeeInfos(mfpUser, report.getContactReviewer());
		MFPUser author = getAuthorUser(mfpUser, report.getContactAuthor());
		pdfMain.createPDF(pdfReport, report, author, dInfo, dEmpInfos, revEmpInfo);
	}


	private Path getTmpFilePath(MFPUser mfpUser, long crId) {
		String baseFileName = "contact_report_" + crId;
		Path tmpFilePath = null;
		File tmpFile = null;
		try {
			tmpFile = File.createTempFile(baseFileName, ".pdf");
			tmpFile.deleteOnExit();
			tmpFilePath =  tmpFile.toPath();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		return tmpFilePath;
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
			MMAListService<DealerEmployeeInfo> service = new MMAListService<DealerEmployeeInfo>();
			List<DealerEmployeeInfo> retRows = null;
			DealerFilter df = new DealerFilter(mfpUser, dlrCd, null, null, null, null);
			try {
				retRows = service.getListData(sqlName, DealerEmployeeInfo.class, df, dlrCd);
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
