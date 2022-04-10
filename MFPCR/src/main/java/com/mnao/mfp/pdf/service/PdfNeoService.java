package com.mnao.mfp.pdf.service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Component;
import org.xhtmlrenderer.pdf.ITextRenderer;

@Component
public class PdfNeoService {

	public void xhtmlToPdf(String xhtml, String outFileName) throws IOException {
	    File output = new File(outFileName);
	    ITextRenderer iTextRenderer = new ITextRenderer();
//	    FontResolver resolver = iTextRenderer.getFontResolver();
//	    iTextRenderer.getFontResolver().addFont("MyFont.ttf", true);
	    iTextRenderer.setDocumentFromString(xhtml);
	    iTextRenderer.layout();
	    OutputStream os = new FileOutputStream(output);
	    iTextRenderer.createPDF(os);
	    os.close();
	}
	
	public String htmlToXhtml(String html) {
	    Document document = Jsoup.parse(html);
	    document.outputSettings().syntax(Document.OutputSettings.Syntax.xml);
	    return document.html();
	}

}
