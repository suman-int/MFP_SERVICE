package com.mnao.mfp.cr.pdf.generate;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.geom.Rectangle;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.borders.SolidBorder;
import com.itextpdf.layout.element.AreaBreak;
import com.itextpdf.layout.element.BlockElement;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.IBlockElement;
import com.itextpdf.layout.element.Image;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.layout.LayoutArea;
import com.itextpdf.layout.layout.LayoutContext;
import com.itextpdf.layout.layout.LayoutResult;
import com.itextpdf.layout.properties.HorizontalAlignment;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.itextpdf.layout.renderer.ParagraphRenderer;

public class PDFReport {
	private String pdfName;
	private PageSize pageSize = PageSize.A4; // .rotate();
	private static int widthMM = 210;
	private Document pdfDoc = null;
	private ByteArrayOutputStream baos = null;
	private boolean closed = true;
	private String disclaimer = "";
	// private String headerText = "";
	private Paragraph headerPara = new Paragraph();
	private String footerText = "";
	private PDFHeaderFooter headerfooter = null;

	public PDFReport(String disclaimer, String headerText, String footerText) {
		this(disclaimer, new Paragraph(headerText), footerText);
	}

	public PDFReport(String disclaimer, Paragraph headerP, String footerText) {
		super();
		this.disclaimer = disclaimer;
		this.footerText = footerText;
		this.headerPara = headerP;
	}

	public double getPixelsPerMM() {
		return pageSize.getWidth() / widthMM;
	}

	public PageSize getPageSize() {
		return pageSize;
	}

	public void setPageSize(PageSize pageSize) {
		this.pageSize = pageSize;
	}

	public void openPdf() throws Exception {
		baos = new ByteArrayOutputStream();
		openPdf(new PdfWriter(baos));
	}

	public void openPdf(String pdfName) throws Exception {
		this.pdfName = pdfName;
		openPdf(new PdfWriter(pdfName));
	}

	private void openPdf(PdfWriter pdfW) {
		PdfDocument pdf = new PdfDocument(pdfW);
		pdfDoc = new Document(pdf, getPageSize());
		closed = false;
		setBottomMargin();
		headerfooter = new PDFHeaderFooter(pdf, pdfDoc, disclaimer, headerPara, footerText);
		addDisclaimer();
	}

	private void setBottomMargin() {

		float ht = getParagraphHeight(getDisclaimerPara());
		ht += getParagraphHeight(getFooterPara(3, 3));
		ht += pdfDoc.getBottomMargin();
		pdfDoc.setBottomMargin(ht);
	}

	public void closePdf() {
		headerfooter.writeTotalPages();
		pdfDoc.close();
		closed = true;
	}

	public boolean isClosed() {
		return closed;
	}

	public byte[] getBytes() {
		byte[] bytes = null;
		if (!isClosed()) {
			closePdf();
		}
		if (baos != null)
			bytes = baos.toByteArray();
		return bytes;
	}

	public void addPageBreak() {
		pdfDoc.add(new AreaBreak());
		addDisclaimer();
		// addFooter(true);
	}

	public void addToReport(BlockElement blkElem) {
		blkElem.setKeepTogether(true);
		addBlockElemToPdf(blkElem);
	}

	public void addToReport(Image image) {
		addBlockElemToPdf(image);
	}

	public void addToReport(BlockElement blkElem, boolean keepTogether, boolean keepWithNext) {
		blkElem.setKeepTogether(keepTogether);
		blkElem.setKeepWithNext(keepWithNext);
		addBlockElemToPdf(blkElem);
	}

	private void addBlockElemToPdf(IBlockElement blockElem) {
		pdfDoc.add(blockElem);
	}

	private void addBlockElemToPdf(Image image) {
		pdfDoc.add(image);
	}

	private float getParagraphHeight(Paragraph para) {
		if (para != null) {
			ByteArrayOutputStream b = new ByteArrayOutputStream();
			PdfDocument p = new PdfDocument(new PdfWriter(b));
			Document doc = new Document(p, getPageSize());
			// Create renderer tree
			ParagraphRenderer renderer = (ParagraphRenderer) para.getRenderer();
			// Do not forget setParent(). Set the dimensions of the viewport as needed
			Rectangle rect = doc.getPageEffectiveArea(getPageSize());
			LayoutArea la = new LayoutArea(1, rect);
			LayoutContext lc = new LayoutContext(la);
			LayoutResult result = renderer.setParent(doc.getRenderer()).layout(lc);
			// LayoutResult#getOccupiedArea() contains the information you need
			la = result.getOccupiedArea();
			return la.getBBox().getHeight();
		} else
			return (float) 0.0;
	}

	private Paragraph getDisclaimerPara() {
		Paragraph para = null;
		if ((disclaimer != null) && (disclaimer.trim().length() > 0)) {
			try {
				para = new Paragraph(disclaimer).setFont(PdfFontFactory.createFont(StandardFonts.HELVETICA_OBLIQUE))
						.setFontSize(6).setFontColor(ColorConstants.BLACK)
						.setWidth(new UnitValue(UnitValue.PERCENT, 100));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return para;
	}

	private Paragraph getFooterPara(int pg, int totPg) {
		Paragraph para = null;
		Table t = new Table(2).setWidth(pdfDoc.getPageEffectiveArea(getPageSize()).getWidth());
		t.setBorder(Border.NO_BORDER);
		Cell c1 = new Cell().add(new Paragraph(footerText).setTextAlignment(TextAlignment.LEFT))
				.setTextAlignment(TextAlignment.LEFT);
		c1.setBorder(Border.NO_BORDER);
		Cell c2 = new Cell().add(new Paragraph("Page " + pg + " of " + totPg).setTextAlignment(TextAlignment.RIGHT))
				.setTextAlignment(TextAlignment.RIGHT);
		c2.setBorder(Border.NO_BORDER);
		t.addCell(c1);
		t.addCell(c2);
		para = new Paragraph();
		para.add(t);
		return para;
	}

	private void addDisclaimer() {
		Paragraph disPara = getDisclaimerPara();
		if (disPara != null) {
			disPara = disPara.setHorizontalAlignment(HorizontalAlignment.CENTER);
			disPara.setFixedPosition(pdfDoc.getLeftMargin(), pdfDoc.getBottomMargin(),
					pdfDoc.getPageEffectiveArea(getPageSize()).getWidth());
			pdfDoc.add(disPara);
		}
	}

}
