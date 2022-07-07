package com.mnao.mfp.download.generate;
import com.itextpdf.kernel.events.Event;
import com.itextpdf.kernel.events.IEventHandler;
import com.itextpdf.kernel.events.PdfDocumentEvent;
import com.itextpdf.kernel.geom.Rectangle;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfPage;
import com.itextpdf.kernel.pdf.canvas.PdfCanvas;
import com.itextpdf.kernel.pdf.xobject.PdfFormXObject;
import com.itextpdf.layout.Canvas;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.properties.TextAlignment;

public class PDFHeaderFooter {

	PdfDocument pdf = null;
	Document doc = null;
	private String disclaimer = "";
	private Paragraph headerPara = new Paragraph();
	private String footerText = "";
	//
	Header headerHandler = null;
	PageXofY footerHandler = null;

	public PDFHeaderFooter(PdfDocument pdf, Document doc, String disclaimer, Paragraph headerP, String footerText) {
		super();
		this.pdf = pdf;
		this.doc = doc;
		this.disclaimer = disclaimer;
		this.headerPara = headerP;
		this.footerText = footerText;
		// Assign event-handlers
		if ((this.headerPara != null) && (!this.headerPara.isEmpty())) {
			this.headerHandler = new Header(headerPara);
			pdf.addEventHandler(PdfDocumentEvent.START_PAGE, this.headerHandler);
		}
		this.footerHandler = new PageXofY();
		pdf.addEventHandler(PdfDocumentEvent.END_PAGE, this.footerHandler);

	}
	
	public void writeTotalPages() {
	       footerHandler.writeTotal(pdf);
	}

	// Header event handler
	protected class Header implements IEventHandler {
		Paragraph header;

		public Header(Paragraph header) {
			this.header = header;
		}

		@Override
		public void handleEvent(Event event) {
			// Retrieve document and
			PdfDocumentEvent docEvent = (PdfDocumentEvent) event;
			PdfDocument pdf = docEvent.getDocument();
			PdfPage page = docEvent.getPage();
			Rectangle pageSize = page.getPageSize();
			PdfCanvas pdfCanvas = new PdfCanvas(page.getLastContentStream(), page.getResources(), pdf);
			Canvas canvas = new Canvas(pdfCanvas, pageSize);
			canvas.setFontSize(18f);
			// Write text at position
			canvas.showTextAligned(header, pageSize.getWidth() / 2, pageSize.getTop() - 30, TextAlignment.CENTER);
		}
	}

	// page X of Y
	protected class PageXofY implements IEventHandler {
		protected PdfFormXObject placeholder;
		protected float side = 20;
		protected float x = 300;
		protected float y = 25;
		protected float space = 4.5f;
		protected float descent = 3;
		//

		public PageXofY() {
			placeholder = new PdfFormXObject(new Rectangle(0, 0, side, side));
		}

		@Override
		public void handleEvent(Event event) {
			PdfDocumentEvent docEvent = (PdfDocumentEvent) event;
			PdfDocument pdf = docEvent.getDocument();
			PdfPage page = docEvent.getPage();
			int pageNumber = pdf.getPageNumber(page);
			Rectangle pageSize = page.getPageSize();
			PdfCanvas pdfCanvas = new PdfCanvas(page.getLastContentStream(), page.getResources(), pdf);
			Canvas canvas = new Canvas(pdfCanvas, pageSize);
			Paragraph p = new Paragraph().add("Page ").add(String.valueOf(pageNumber)).add(" of");
			canvas.showTextAligned(p, x, y, TextAlignment.RIGHT);
			pdfCanvas.addXObject(placeholder, x + space, y - descent);
			pdfCanvas.release();
		}

		public void writeTotal(PdfDocument pdf) {
			Canvas canvas = new Canvas(placeholder, pdf);
			canvas.showTextAligned(String.valueOf(pdf.getNumberOfPages()), 0, descent, TextAlignment.LEFT);
		}
		

	}
}
