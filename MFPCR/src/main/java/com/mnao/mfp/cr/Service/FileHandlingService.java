package com.mnao.mfp.cr.Service;

import org.springframework.web.multipart.MultipartFile;

import com.mnao.mfp.cr.dto.ContactInfoAttachmentDto;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

public interface FileHandlingService {

//    String doUpload(List<ContactReportAttachment> attachments);
    List<ContactInfoAttachmentDto> doUpload(MultipartFile[] file, HttpServletRequest request);
}
