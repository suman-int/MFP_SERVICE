package com.mnao.mfp.cr.controller;

import com.mnao.mfp.common.dto.CommonResponse;
import com.mnao.mfp.common.service.AbstractService;
import com.mnao.mfp.cr.Service.ContactReportService;
import com.mnao.mfp.cr.Service.ContactReportServiceImpl;
import com.mnao.mfp.cr.Service.FileHandlingServiceImpl;
import com.mnao.mfp.cr.Service.GenericResponseWrapper;
import com.mnao.mfp.cr.dto.ContactInfoAttachmentDto;
import com.mnao.mfp.cr.dto.ContactReportDto;
import com.mnao.mfp.cr.dto.ContactReportInfoDto;
import com.mnao.mfp.cr.entity.ContactReportInfo;
import com.mnao.mfp.cr.model.ContactReportResponse;
import com.mnao.mfp.cr.util.ContactReportEnum;
import com.mnao.mfp.list.dao.ListDealer;

import com.mnao.mfp.user.dao.MFPUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import java.io.IOException;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

@RestController
@RequestMapping(value = "ContactReport")
public class ReportController {

    @Autowired(required = true)
    private ContactReportService contactReportService;

    @Autowired(required = true)
	private FileHandlingServiceImpl fileHandlingService;

    @PostMapping(value = "submitReport")
    public ContactReportResponse submitReportData(@Valid @RequestBody ContactReportInfoDto report, @SessionAttribute(name = "mfpUser")
	MFPUser mfpUser){
        try {
            return GenericResponseWrapper.contactReportResponseFunction.apply(contactReportService.submitReportData(report, mfpUser), null);
        } catch (Exception e) {
            return GenericResponseWrapper.contactReportResponseFunction.apply(null, e.getMessage());
        }
    }

//	@PostMapping(value ="/updateReport")
//	public String updateReportData(@Valid @RequestBody ContactReportInfoDto report) {
//		return contactReportService.updateDraftReport(report);
//	}

    @PostMapping(value = "deleteReportById")
    public void deleteReportById(@RequestBody long contactReportId) {
        contactReportService.deleteReportById(contactReportId);
    }
    
    @PostMapping(value = "deleteAttachmentById/{attachmentId}")
	public CommonResponse<String> deleteReportAttachmentById(@PathVariable long attachmentId) {
		String response= fileHandlingService.deleteAttachmentById(attachmentId);
		return AbstractService.httpPostSuccess(response, "Success");
	}
    
    @RequestMapping(value = "deleteAttachmentByPath",method = RequestMethod.POST)
	public CommonResponse<String> deleteReportAttachmentByPath(@RequestBody Map<String, String> inputObject) {
		String response=fileHandlingService.deleteAttachmentByAttachmentPath(inputObject.get("fileName"));
		return AbstractService.httpPostSuccess(response, "Success");
	}

    @GetMapping(value = "getReportById/{contactReportId}")
    public ContactReportResponse getReportById(@PathVariable long contactReportId) {
        return GenericResponseWrapper.contactReportResponseFunction.apply(contactReportService.findByContactReportId(contactReportId), null);

    }

    @GetMapping(value = "getReportsByUserID/{userId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public CommonResponse<Map<String,List<ContactReportInfoDto>>> getMyContactReport(@PathVariable("userId") String userId) {

        try {
        	Map<String, List<ContactReportInfoDto>> response = contactReportService.getMyContactReport(userId);
            return AbstractService.httpPostSuccess(response, "Success Response");

        } catch (Exception e) {
            return AbstractService.httpPostError(e);
        }
    }
    
    @PostMapping(value = "uploadFile")
	public CommonResponse<List<ContactInfoAttachmentDto>>uploadFile(@RequestParam("files") MultipartFile[] files, HttpServletRequest request) {
		
    	List<ContactInfoAttachmentDto>retRows= fileHandlingService.doUpload(files,request);
    	return AbstractService.httpPostSuccess(retRows, "Success");
//    	return fileHandlingService.doUpload(files,request);
	}
    
    @GetMapping(value = "downloadFileByPath/{fileName:.+}")
	public ResponseEntity<Resource> downloadFileUsingFileName(@PathVariable String fileName, HttpServletRequest request) {
    	// Load file as Resource
        Resource resource = fileHandlingService.loadFileAsResource(fileName);

        // Try to determine file's content type
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
	}
    @GetMapping(value = "downloadFileById/{attachmentId}")
	public ResponseEntity<Resource> downloadFileUsingId(@PathVariable long attachmentId, HttpServletRequest request) {
    	// Load file as Resource
        Resource resource = fileHandlingService.loadFileAsResource(attachmentId);

        // Try to determine file's content type
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
	}

}