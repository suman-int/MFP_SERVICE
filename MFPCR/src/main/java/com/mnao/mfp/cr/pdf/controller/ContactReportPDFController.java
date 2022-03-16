package com.mnao.mfp.cr.pdf.controller;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

import javax.servlet.http.HttpServletRequest;

import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.SessionAttribute;

import com.mnao.mfp.common.controller.MfpKPIControllerBase;
import com.mnao.mfp.cr.entity.ContactReportInfo;
import com.mnao.mfp.cr.pdf.generate.PDFCRMain;
import com.mnao.mfp.user.dao.MFPUser;

@RestController
@RequestMapping(path = "/ContactReport")
public class ContactReportPDFController extends MfpKPIControllerBase {
    @GetMapping(value = "/downloadPDF")
	public ResponseEntity<Resource> downloadFileUsingFileName(
			@SessionAttribute(name = "mfpUser") MFPUser mfpUser, 
			@RequestBody ContactReportInfo report,
			HttpServletRequest request) {
    	try {
    		Path filePath = new File("/media/psf/Home/SmWrk/Wrk/MFP/tmp/tmp.pdf").toPath();
    		PDFCRMain pdfMain = new PDFCRMain();
    		pdfMain.createPdfFile(filePath, report);
    		Resource resource = new UrlResource(filePath.toUri());
            if(resource.exists()) {
                String contentType = null;
                if(resource!= null) {
                	try {
                		contentType = request.getServletContext().getMimeType(resource.getFile().getAbsolutePath());
                	} catch (IOException ex) {
                		System.out.println("Could not determine file type.");
                	}
                	
                	// Fallback to the default content type if type could not be determined
                	if(contentType == null) {
                		contentType = "application/octet-stream";
                	}
                	
                	return ResponseEntity.ok()
                			.contentType(MediaType.parseMediaType(contentType))
                			.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                			.body(resource);
                	
                } else {
                	return new ResponseEntity<>(HttpStatus.NOT_FOUND);
                }
            } else {
            	 System.out.printf("No such file : %s\n", filePath);
            }
    	} catch (IOException ex) {
       	 System.out.printf("No such file :\n");
       }
       return null;
	
    }

}
