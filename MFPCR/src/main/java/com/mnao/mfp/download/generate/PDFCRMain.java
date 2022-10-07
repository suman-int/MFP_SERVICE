package com.mnao.mfp.download.generate;

import java.time.format.DateTimeFormatter;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.element.Text;
import com.itextpdf.layout.properties.UnitValue;
import com.mnao.mfp.common.dao.DealerInfo;
import com.mnao.mfp.common.util.Utils;
import com.mnao.mfp.cr.entity.ContactReportDealerPersonnel;
import com.mnao.mfp.cr.entity.ContactReportDiscussion;
import com.mnao.mfp.cr.entity.ContactReportInfo;
import com.mnao.mfp.cr.util.ContactReportEnum;
import com.mnao.mfp.download.dao.ReviewerEmployeeInfo;
import com.mnao.mfp.list.cache.AllActiveEmployeesCache;
import com.mnao.mfp.list.dao.ListPersonnel;
import com.mnao.mfp.user.dao.MFPUser;

public class PDFCRMain {
	//
	private static final Logger log = LoggerFactory.getLogger(PDFCRMain.class);
	//
//	public void createPdfFile(Path p, ContactReportInfo crInfo, MFPUser author, DealerInfo dInfo,
//			List<DealerEmployeeInfo> dEmpInfos, ReviewerEmployeeInfo revEmpInfo) throws IOException {
//		byte[] b = createPDF(crInfo, author, dInfo, dEmpInfos, revEmpInfo);
//		Files.write(p, b, StandardOpenOption.CREATE);
//	}
//

	public void createPDF(PDFReport report, ContactReportInfo crInfo, MFPUser author, DealerInfo dInfo,
			List<ListPersonnel> dEmpInfos, ReviewerEmployeeInfo revEmpInfo) {
		try {
			addHeadPortion(report, crInfo, dInfo, author);
			addPersonnel(report, crInfo, dEmpInfos, revEmpInfo, author);
			addDiscussions(report, crInfo);
		} catch (Exception e) {
			log.error("", e);
		}
		return;
	}

	private void addDiscussions(PDFReport report, ContactReportInfo crInfo) {
		List<ContactReportDiscussion> discs = crInfo.getDiscussions();
		if (discs != null) {
			Paragraph discPara = new Paragraph();
			discPara.setWidth(UnitValue.createPercentValue(100));
			Paragraph p = new Paragraph();
			p.setWidth(UnitValue.createPercentValue(100));
			p.setFontSize(14);
			p.setBold();
			p.add("DISCUSSIONS");
			discPara.add(p);
			for (ContactReportDiscussion crd : discs) {
				p = new Paragraph();
				p.setPaddingLeft(50);
				p.setWidth(UnitValue.createPercentValue(100));
				p.add(new Text(crd.getTopic()).setBold().setUnderline());
				discPara.add(p);
				p = new Paragraph();
				p.setPaddingLeft(50);
				p.setWidth(UnitValue.createPercentValue(100));
				p.add(new Text(crd.getDiscussion()));
				discPara.add(p);
			}
			report.addToReport(discPara);
		}
	}

	private void addPersonnel(PDFReport report, ContactReportInfo crInfo, List<ListPersonnel> dEmpInfos,
			ReviewerEmployeeInfo revEmpInfo, MFPUser author) {
		Paragraph p = new Paragraph();
		Text txt = new Text("Personnel");
		txt.setBold();
		p.add(txt);
		p.setWidth(UnitValue.createPercentValue(100));
		report.addToReport(p);
		Table tbl = new Table(4);
		tbl.setWidth(UnitValue.createPercentValue(100));
		addCell(tbl, "AUTHOR");
		if (author == null) {
			addCell(tbl, crInfo.getContactAuthor());
		} else {
			String auth = Utils.getNameString(author.getFirstName(), author.getLastName());
			String jDesc = author.getHrJobName().trim();
			if ((jDesc != null) && (jDesc.trim().length() > 0))
				auth += ", " + jDesc;
			addCell(tbl, auth);
		}
		addCell(tbl, "REVIEWER");
		if (revEmpInfo == null) {
			addCell(tbl, crInfo.getContactReviewer());
		} else {
			String person = Utils.getNameString(revEmpInfo.getFirstNm(), revEmpInfo.getMidlNm(),
					revEmpInfo.getLastNm());
			String jDesc = revEmpInfo.getJobTitleTx().trim();
			if ((jDesc != null) && (jDesc.trim().length() > 0)) {
				person += ", " + jDesc;
			}
			addCell(tbl, person);
		}
		addCell(tbl, "DEALERSHIP CONTACTS", 1, 2);
		String addDP = crInfo.getAddDealerPersonnel();
		List<ContactReportDealerPersonnel> dps = crInfo.getDealerPersonnels();
		StringBuilder dpers = new StringBuilder();
		if (dps != null) {
			if (dEmpInfos != null) {
				for (ListPersonnel dei : dEmpInfos) {
					String person = Utils.getNameString(dei.getFirstNm(), dei.getMidlNm(), dei.getLastNm());
					person += ", " + dei.getJobTitleFx().trim();
					dpers.append(person);
					dpers.append("\n");
				}
			} else {
				for (ContactReportDealerPersonnel dp : dps) {
					dpers.append(dp.getPersonnelIdCd());
					dpers.append("\n");
				}
			}
		}
		if (addDP != null && addDP.trim().length() > 0) {
			String[] addDPs = addDP.split("[,;]");
			for (String dp : addDPs) {
				dpers.append(dp);
				dpers.append("\n");
			}
		}
		if (dpers.length() == 0) {
			dpers.append(" ");
		}
		addCell(tbl, dpers.toString(), 1, 2);
		if (crInfo.getCorporateReps() != null && crInfo.getCorporateReps().trim().length() > 0) {
			addCell(tbl, "CORPORATE REPRESENTATIVES", 1, 2);
			String[] cRepStr = crInfo.getCorporateReps().split("[,]");
			StringBuilder cReps = new StringBuilder();
			AllActiveEmployeesCache allEmployeesCache = new AllActiveEmployeesCache();
			for (int i = 0; i < cRepStr.length; i++) {
				if (allEmployeesCache != null) {
					ListPersonnel lp = allEmployeesCache.getByPrsnIdCd(cRepStr[i]);
					if (lp != null) {
						String person = Utils.getNameString(lp.getFirstNm(), lp.getMidlNm(), lp.getLastNm());
						cReps.append(person);
					} else {
						cReps.append(cRepStr[i]);
					}
					cReps.append("\n");
				}
				else {
					cReps.append(cRepStr[i]);
					cReps.append("\n");
				}
			}
			if( cReps.length() == 0 ) {
				cReps.append(" ");
			}
			addCell(tbl, cReps.toString(), 1, 2);
		}
		report.addToReport(tbl);
	}

	private void addHeadPortion(PDFReport report, ContactReportInfo crInfo, DealerInfo dInfo, MFPUser author) {
		Table tbl = new Table(4);
		tbl.setWidth(UnitValue.createPercentValue(100));
		//
		addCell(tbl, "DEALERSHIP:");
		if (dInfo != null) {
			addCell(tbl, dInfo.getDbaNm() + " - " + crInfo.getDlrCd());
		} else {
			addCell(tbl, crInfo.getDlrCd());
		}
		addCell(tbl, "STATUS:");
		// addCell(tbl, "" + crInfo.getContactStatus());
		addCell(tbl, "" + ContactReportEnum.valueByStatus(crInfo.getContactStatus()));
		//
		addCell(tbl, "ADDRESS:");
		if (dInfo != null) {
			addCell(tbl, dInfo.getCityNm() + ", " + dInfo.getStCd() + " " + dInfo.getZip1Cd());
		} else if (crInfo.getDealers() != null) {
			addCell(tbl, crInfo.getDealers().getCityNm() + " - " + crInfo.getDealers().getZipCd());
		} else {
			addCell(tbl, " ");
		}
		addCell(tbl, "CONTACT DATE:");
		if (crInfo.getContactDt() != null) {
			DateTimeFormatter dtf = DateTimeFormatter.ofPattern("MM-dd-YYYY");
			addCell(tbl, "" + crInfo.getContactDt().format(dtf));
		} else {
			addCell(tbl, " ");
		}
		//
		addCell(tbl, "PHONE:");
		addCell(tbl, " ");
		addCell(tbl, "AUTHOR:");
		if (author == null) {
			addCell(tbl, crInfo.getContactAuthor());
		} else {
			String auth = Utils.getNameString(author.getFirstName(), author.getLastName());
			addCell(tbl, auth);
		}
		//
		addCell(tbl, "CONTACT LOCATION:");
		addCell(tbl, crInfo.getContactLocation());
		addCell(tbl, " ");
		addCell(tbl, " ");
		report.addToReport(tbl);
	}

	private void addCell(Table tbl, String str) {
		addCell(tbl, str, 1, 1);
	}

	private void addCell(Table tbl, String str, int row, int col) {
		Cell c = new Cell(row, col);
		if (str == null) {
			c.add(new Paragraph(" "));
		} else {
			c.add(new Paragraph(str));
		}
		tbl.addCell(c);
	}
}
