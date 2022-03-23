package com.mnao.mfp.cr.pdf.generate;

import java.time.format.DateTimeFormatter;
import java.util.List;

import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.element.Text;
import com.itextpdf.layout.properties.UnitValue;
import com.mnao.mfp.common.dao.DealerInfo;
import com.mnao.mfp.cr.entity.ContactReportDealerPersonnel;
import com.mnao.mfp.cr.entity.ContactReportDiscussion;
import com.mnao.mfp.cr.entity.ContactReportInfo;
import com.mnao.mfp.cr.pdf.dao.DealerEmployeeInfo;
import com.mnao.mfp.cr.pdf.dao.ReviewerEmployeeInfo;
import com.mnao.mfp.user.dao.MFPUser;

public class PDFCRMain {
//	public void createPdfFile(Path p, ContactReportInfo crInfo, MFPUser author, DealerInfo dInfo,
//			List<DealerEmployeeInfo> dEmpInfos, ReviewerEmployeeInfo revEmpInfo) throws IOException {
//		byte[] b = createPDF(crInfo, author, dInfo, dEmpInfos, revEmpInfo);
//		Files.write(p, b, StandardOpenOption.CREATE);
//	}
//
	public void createPDF(PDFReport report, ContactReportInfo crInfo, MFPUser author, DealerInfo dInfo,
			List<DealerEmployeeInfo> dEmpInfos, ReviewerEmployeeInfo revEmpInfo) {
		try {
			addHeadPortion(report, crInfo, dInfo, author);
			addPersonnel(report, crInfo, dEmpInfos, revEmpInfo, author);
			addDiscussions(report, crInfo);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return ;
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

	private void addPersonnel(PDFReport report, ContactReportInfo crInfo, List<DealerEmployeeInfo> dEmpInfos,
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
			String auth = getNameString(author.getFirstName(), author.getLastName() );
			String jDesc = author.getHrJobName().trim();
			if ((jDesc != null) && (jDesc.trim().length() > 0))
				auth += ", " + jDesc;
			addCell(tbl, auth);
		}
		addCell(tbl, "REVIEWER");
		if (revEmpInfo == null) {
			addCell(tbl, crInfo.getContactReviewer());
		} else {
			String person = getNameString(revEmpInfo.getFirstNm(), revEmpInfo.getMidlNm(), revEmpInfo.getLastNm() );
			String jDesc = revEmpInfo.getJobTitleTx().trim();
			if ((jDesc != null) && (jDesc.trim().length() > 0)) {
				person += ", " + jDesc;
			}
			addCell(tbl, person);
		}
		addCell(tbl, "DEALERSHIP CONTACTS", 1, 2);
		List<ContactReportDealerPersonnel> dps = crInfo.getDealerPersonnels();
		StringBuilder dpers = new StringBuilder();
		if (dps != null) {
			if (dEmpInfos != null) {
				for (DealerEmployeeInfo dei : dEmpInfos) {
					String person = getNameString(dei.getFirstNm(), dei.getMidlNm(), dei.getLastNm() );
					person += ", " + dei.getJobTitleTx().trim();
					dpers.append(person);
					dpers.append("\n");
				}
			} else {
				for (ContactReportDealerPersonnel dp : dps) {
					dpers.append(dp.getPersonnelIdCd());
					dpers.append("\n");
				}
			}
		} else {
			dpers.append(" ");
		}
		addCell(tbl, dpers.toString(), 1, 2);
		report.addToReport(tbl);
	}
	
	private String getNameString(String ... nParts) {
		StringBuilder sb = new StringBuilder();
		for(int i = 0 ; i < nParts.length; i++) {
			if( (nParts[i] != null) && (nParts[i].trim().length() > 0) ) {
				if( i > 0 )
					sb.append(" ");
				sb.append(nParts[i].trim());
			}
		}
		return sb.toString();
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
		addCell(tbl, "" + crInfo.getContactStatus());
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
			String auth = getNameString(author.getFirstName(),  author.getLastName());
			addCell(tbl, auth);
		}
		//
		addCell(tbl, "CONTACT LOCATION:");
		addCell(tbl, crInfo.getContactLocation());
		addCell(tbl, "REVIEWER:");
		addCell(tbl, " ");
		report.addToReport(tbl);
	}

	private void addCell(Table tbl, String str) {
		addCell(tbl, str, 1, 1);
	}

	private void addCell(Table tbl, String str, int row, int col) {
		Cell c = new Cell(row, col);
		c.add(new Paragraph(str));
		tbl.addCell(c);
	}
}
