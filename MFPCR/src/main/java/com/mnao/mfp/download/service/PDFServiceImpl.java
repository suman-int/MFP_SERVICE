package com.mnao.mfp.download.service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Path;
import java.text.ParseException;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.validation.Valid;

import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.Row;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;

import com.mnao.mfp.common.controller.MfpKPIControllerBase;
import com.mnao.mfp.common.dao.DealerFilter;
import com.mnao.mfp.common.dao.DealerInfo;
import com.mnao.mfp.common.util.AppConstants;
import com.mnao.mfp.cr.entity.ContactReportDealerPersonnel;
import com.mnao.mfp.cr.entity.ContactReportDiscussion;
import com.mnao.mfp.cr.entity.ContactReportInfo;
import com.mnao.mfp.cr.entity.Dealers;
import com.mnao.mfp.cr.util.ContactReportEnum;
import com.mnao.mfp.download.controller.ContactReportPDFController;
import com.mnao.mfp.download.dao.DealerEmployeeInfo;
import com.mnao.mfp.download.dao.ReviewerEmployeeInfo;
import com.mnao.mfp.download.generate.PDFCRMain;
import com.mnao.mfp.download.generate.PDFReport;
import com.mnao.mfp.list.service.MMAListService;
import com.mnao.mfp.user.dao.MFPUser;
import com.mnao.mfp.user.service.UserDetailsService;

@Service
public class PDFServiceImpl extends MfpKPIControllerBase implements PDFService {
	private static final Logger log = LoggerFactory.getLogger(ContactReportPDFController.class);

	private static final String[] xlsHeaders = { "Region", "Area", "District", "Dealership",
			"Issues and Topics Documented", "Required Issues not Documented", "Contact Location", "Contact Date",
			"Author", "Title", "Create Date", "Submitted Date", "Reviewed Date", "Status" };
	private static final int[] xlsColWidths = { 5, 5, 5, 60, 50, 50, 20, 12, 30, 25, 12, 12, 12, 20 };

	private Map<String, MFPUser> udsUsers = new HashMap<>();

	//
	@Override
	public Resource createPDFResource(MFPUser mfpUser, ContactReportInfo report) {
		ArrayList<ContactReportInfo> rpts = new ArrayList<ContactReportInfo>();
		rpts.add(report);
		return createBulkPDFResource(mfpUser, rpts);
	}

	@Override
	public Path createPDFFile(MFPUser mfpUser, @Valid ContactReportInfo report) {
		Path filePath = null;
		return filePath;
	}

	public Resource createBulkPDFResource(MFPUser mfpUser, List<ContactReportInfo> reports) {
		//
		String postFix = "_BULK";
		boolean first = true;
		if (reports.size() == 1)
			postFix = "_" + reports.get(0).getContactReportId();
		Path filePath = getTmpFilePath(mfpUser, "contact_report_", postFix, ".pdf");
		PDFReport pdfReport = new PDFReport("", "Contact Report", "Mazda North America Operations");
		try {
			boolean success = false;
			pdfReport.openPdf(filePath.toString());
			for (ContactReportInfo report : reports) {
				if (success && !first) {
					pdfReport.addPageBreak();
				}
				success = createPDFDocument(pdfReport, report, mfpUser);
				first = false;
			}
			pdfReport.closePdf();
		} catch (Exception e) {
			log.error("", e);
		}
		Resource resource = null;
		try {
			resource = new UrlResource(filePath.toUri());
		} catch (MalformedURLException e) {
			log.error("", e);
		}
		return resource;
	}

	@Override
	public Path createXLSXFile(MFPUser mfpUser, List<ContactReportInfo> contactReports) throws Exception {
		Path fPath = null;
		try (HSSFWorkbook wkbk = new HSSFWorkbook()) {
			HSSFSheet sheet = wkbk.createSheet("Contact Report Summary");
			int row = 0;
			row = printXLSHeaders(sheet, row);
			for (ContactReportInfo report : contactReports) {
				row = printXLSRow(sheet, row, mfpUser, report);
			}
			// Write the workbook in file system
			fPath = getTmpFilePath(mfpUser, "contact_report_summary_", mfpUser.getUserid(), ".xls");
			FileOutputStream out = new FileOutputStream(fPath.toFile());
			wkbk.write(out);
			out.close();
		} catch (Exception e) {
			log.error("", e);
			throw e;
		}
		return fPath;
	}

	@Override
	public Resource createXLSXResource(MFPUser mfpUser, List<ContactReportInfo> reports) throws Exception {
		//
		//
		Resource resource = null;
		Path filePath = createXLSXFile(mfpUser, reports);
		resource = new UrlResource(filePath.toUri());
		return resource;
	}

	private int printXLSHeaders(HSSFSheet sheet, int rCnt) {
		int col = 0;
		Row row = sheet.createRow(rCnt++);
		// Bold Font
		HSSFCellStyle boldStyle = sheet.getWorkbook().createCellStyle();
		HSSFFont bFont = sheet.getWorkbook().createFont();
		bFont.setBold(true);
		boldStyle.setFont(bFont);
		boldStyle.setAlignment(HorizontalAlignment.CENTER);
		for (int i = 0; i < xlsHeaders.length; i++) {
			Cell cell = row.createCell(col++);
			cell.setCellStyle(boldStyle);
			cell.setCellValue(xlsHeaders[i]);
			sheet.autoSizeColumn(i);
			if (xlsColWidths[i] > xlsHeaders[i].length()) {
				sheet.setColumnWidth(i, xlsColWidths[i] * 256);
			}
		}
		return rCnt;

	}

	private int printXLSRow(HSSFSheet sheet, int rCnt, MFPUser mfpUser, ContactReportInfo report) {
		DealerInfo dInfo = getDealerInfo(mfpUser, report.getDlrCd());
		if (dInfo == null) {
			// Dealer is of region different than logged in user.
			return rCnt;
		}
		if (report.getContactStatus() == ContactReportEnum.DRAFT.getStatusCode()
				|| report.getContactStatus() == ContactReportEnum.CANCELLED.getStatusCode()) {
			return rCnt;
		}
		MFPUser uInfo = getAuthorUser(mfpUser, report.getContactAuthor());
		List<String> topics = new ArrayList<String>();
		if (report.getDiscussions() != null && report.getDiscussions().size() > 0) {
			for (ContactReportDiscussion disc : report.getDiscussions()) {
				topics.add(disc.getTopic());
			}
		}
		if (topics.size() == 0) {
			topics.add(" ");
		}
		for (String topic : topics) {
			Row row = sheet.createRow(rCnt++);
			int col = 0;
			col = addXLSCellValue(row, dInfo.getRgnCd(), col);
			col = addXLSCellValue(row, dInfo.getZoneCd(), col);
			col = addXLSCellValue(row, dInfo.getDistrictCd(), col);
			col = addXLSCellValue(row, dInfo.getDbaNm().trim() + " - " + dInfo.getDlrCd(), col);
			col = addXLSCellValue(row, topic, col);
			col = addXLSCellValue(row, "", col);
			col = addXLSCellValue(row, report.getContactLocation(), col);
			String dtVal = "";
			if (report.getContactDt() != null) {
				DateTimeFormatter dtf = DateTimeFormatter.ofPattern("MM-dd-YYYY");
				dtVal = "" + report.getContactDt().format(dtf);
			}
			col = addXLSCellValue(row, dtVal, col);
			if (uInfo != null) {
				col = addXLSCellValue(row, uInfo.getFirstName().trim() + " " + uInfo.getLastName().trim(), col);
				col = addXLSCellValue(row, uInfo.getHrJobName(), col);
			} else {
				col = addXLSCellValue(row, report.getContactAuthor() + " " + report.getContactAuthor(), col);
				col = addXLSCellValue(row, "", col);
			}
			dtVal = "";
			if (report.getCreatedDt() != null) {
				DateTimeFormatter dtf = DateTimeFormatter.ofPattern("MM-dd-YYYY");
				dtVal = "" + report.getCreatedDt().format(dtf);
				col = addXLSCellValue(row, dtVal, col);
			} else {
				col = addXLSCellValue(row, "NOT AVAILABLE", col);
			}
			if (report.getSubmittedDt() != null) {
				DateTimeFormatter dtf = DateTimeFormatter.ofPattern("MM-dd-YYYY");
				dtVal = "" + report.getSubmittedDt().format(dtf);
				col = addXLSCellValue(row, dtVal, col);
			} else {
				col = addXLSCellValue(row, "NOT AVAILABLE", col);
			}
			if (report.getReviewedDt() != null) {
				DateTimeFormatter dtf = DateTimeFormatter.ofPattern("MM-dd-YYYY");
				dtVal = "" + report.getReviewedDt().format(dtf);
				col = addXLSCellValue(row, dtVal, col);
			} else {
				col = addXLSCellValue(row, "NOT AVAILABLE", col);
			}
			ContactReportEnum cre = ContactReportEnum.valueByStatus(report.getContactStatus());
			col = addXLSCellValue(row, "" + cre.getStatusText(), col);
		}
		return rCnt;
	}

	private int addXLSCellValue(Row row, String val, int col) {
		Cell cell = row.createCell(col++);
		cell.setCellValue(val);
		return col;
	}

	private boolean createPDFDocument(PDFReport pdfReport, ContactReportInfo report, MFPUser mfpUser) {
		DealerInfo dInfo = getDealerInfo(mfpUser, report.getDlrCd());
		if (dInfo == null) {
			// Dealer is of region different than logged in user.
			return false;
		}
		PDFCRMain pdfMain = new PDFCRMain();
		List<DealerEmployeeInfo> dEmpInfos = getDealerEmployeeInfos(mfpUser, report.getDlrCd(),
				report.getDealerPersonnels());
		ReviewerEmployeeInfo revEmpInfo = getReviewerEmployeeInfos(mfpUser, report.getContactReviewer(), dInfo);
		MFPUser author = getAuthorUser(mfpUser, report.getContactAuthor());
		pdfMain.createPDF(pdfReport, report, author, dInfo, dEmpInfos, revEmpInfo);
		return true;
	}

	@Override
	public Path getTmpFilePath(MFPUser mfpUser, String prefix, String postfix, String extn) {
		String baseFileName = prefix + postfix;
		Path tmpFilePath = null;
		File tmpFile = null;
		try {
			tmpFile = File.createTempFile(baseFileName, extn);
			tmpFile.deleteOnExit();
			tmpFilePath = tmpFile.toPath();
		} catch (IOException e1) {
			log.error("", e1);
		}
		return tmpFilePath;
	}

	private MFPUser getAuthorUser(MFPUser mfpUser, String contactAuthor) {
		return getUDSUser(contactAuthor);
	}

	@Override
	public MFPUser getUDSUser(String wslid) {
		MFPUser musr = null;
		if (udsUsers.containsKey(wslid)) {
			musr = udsUsers.get(wslid);
		} else {
			UserDetailsService uds = new UserDetailsService();
			musr = uds.getMFPUser(wslid);
			udsUsers.put(wslid, musr);
		}
		return musr;
	}

	@Override
	public ReviewerEmployeeInfo getReviewerEmployeeInfos(MFPUser mfpUser, String contactReviewer, DealerInfo dInfo) {
		ReviewerEmployeeInfo revEmp = null;
		String rgn = dInfo.getRgnCd();
		String zon = dInfo.getZoneCd();
		return getReviewerDetails(mfpUser, contactReviewer, revEmp, rgn, zon);
	}

	@Override
	public ReviewerEmployeeInfo getReviewerEmployeeInfos(MFPUser mfpUser, String contactReviewer, Dealers dInfo) {
		ReviewerEmployeeInfo revEmp = null;
		String rgn = dInfo.getRgnCd();
		String zon = dInfo.getZoneCd();
		return getReviewerDetails(mfpUser, contactReviewer, revEmp, rgn, zon);
	}

	private ReviewerEmployeeInfo getReviewerDetails(MFPUser mfpUser, String contactReviewer,
			ReviewerEmployeeInfo revEmp, String rgn, String zon) {
		if (contactReviewer != null) {
			String sqlName = getKPIQueryFilePath(AppConstants.SQL_LIST_REVIEWER_EMPLOYEES);
			MMAListService<ReviewerEmployeeInfo> service = new MMAListService<ReviewerEmployeeInfo>();
			List<ReviewerEmployeeInfo> retRows = null;
			DealerFilter df = new DealerFilter(mfpUser, null, mfpUser.getRgnCd(), null, null, null);
			try {
				retRows = service.getListData(sqlName, ReviewerEmployeeInfo.class, df, rgn, zon);
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

	@Override
	public List<DealerEmployeeInfo> getDealerEmployeeInfos(MFPUser mfpUser, String dlrCd,
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

}
