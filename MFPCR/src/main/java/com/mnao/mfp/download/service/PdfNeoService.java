package com.mnao.mfp.download.service;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Path;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.xhtmlrenderer.layout.SharedContext;
import org.xhtmlrenderer.pdf.ITextRenderer;

import com.lowagie.text.DocumentException;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfImportedPage;
import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.PdfWriter;
import com.mnao.mfp.user.dao.MFPUser;

@Component
public class PdfNeoService {
	//
	private static final Logger log = LoggerFactory.getLogger(PdfNeoService.class);
	//
	private Logger logger = LoggerFactory.getLogger(getClass());

	public void xhtmlToPdf(String xhtml, Path outputPath) {
		try (OutputStream os = new FileOutputStream(outputPath.toFile())) {
			ITextRenderer renderer = new ITextRenderer();
			SharedContext sharedContext = renderer.getSharedContext();
			sharedContext.setPrint(true);
			sharedContext.setInteractive(false);
//		    FontResolver resolver = iTextRenderer.getFontResolver();
//		    iTextRenderer.getFontResolver().addFont("MyFont.ttf", true);
			// Register custom ReplacedElementFactory implementation
//		    sharedContext.setReplacedElementFactory(new ReplacedElementFactoryImpl());
			sharedContext.getTextRenderer().setSmoothingThreshold(0);
			// Register additional font
//		    renderer.getFontResolver().addFont(getClass().getClassLoader().getResource("fonts/PRISTINA.ttf").toString(), true);
			// Setting base URL to resolve the relative URLs
			renderer.setDocumentFromString(xhtml);
			renderer.layout();
			renderer.createPDF(os);
			os.close();
		} catch (FileNotFoundException e) {
			log.error("", e);
		} catch (IOException e) {
			log.error("", e);
		}
	}

	public String htmlToXhtml(String inputHTML) {
		logger.info("htmlToXml started with html data");
		inputHTML = inputHTML.replaceAll("[&]{0,1}nbsp[;]{0,1}", " ");
		inputHTML = inputHTML.replace((char) 0x1A, ' ');
		Document document = Jsoup.parse(inputHTML, "UTF-8");
		document.outputSettings().syntax(Document.OutputSettings.Syntax.xml);
		String html = document.html();
		html = html.replaceAll("[&]{0,1}nbsp[;]{0,1}", " ");
		html = html.replace((char) 0x1A, ' ');
		return html;
	}

	public Path getTmpFilePath(MFPUser mfpUser, String prefix, String postfix, String extn) {
		logger.info("getTmpFilePath started with {} and {} and {}", prefix, postfix, extn);
		String baseFileName = prefix + postfix;
		Path tmpFilePath = null;
		File tmpFile = null;
		try {
			tmpFile = File.createTempFile(baseFileName, extn);
			tmpFile.deleteOnExit();
			tmpFilePath = tmpFile.toPath();
			logger.info("Returning TmpPath: " + tmpFilePath.toString());
		} catch (IOException e1) {
			log.error("", e1);
		}
		return tmpFilePath;
	}

	/**
	 * Merge multiple pdf into one pdf
	 * 
	 * @param list         of pdf input stream
	 * @param outputStream output file output stream
	 * @throws DocumentException
	 * @throws IOException
	 */
	public static void doMerge(List<InputStream> list, OutputStream outputStream)
			throws DocumentException, IOException {
		com.lowagie.text.Document document = new com.lowagie.text.Document();
		PdfWriter writer = PdfWriter.getInstance(document, outputStream);
		document.open();
		PdfContentByte cb = writer.getDirectContent();

		for (InputStream in : list) {
			PdfReader reader = new PdfReader(in);
			for (int i = 1; i <= reader.getNumberOfPages(); i++) {
				document.newPage();
				// import the page from source pdf
				PdfImportedPage page = writer.getImportedPage(reader, i);
				// add the page to the destination pdf
				cb.addTemplate(page, 0, 0);
			}
		}

		outputStream.flush();
		document.close();
		outputStream.close();
	}

}
