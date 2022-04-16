package com.mnao.mfp.cr.service.impl;

import com.mnao.mfp.common.util.AppConstants;
import com.mnao.mfp.common.util.IsActiveEnum;
import com.mnao.mfp.common.util.NullCheck;
import com.mnao.mfp.common.util.Utils;
import com.mnao.mfp.cr.dto.ContactInfoAttachmentDto;
import com.mnao.mfp.cr.entity.ContactReportAttachment;
import com.mnao.mfp.cr.entity.ContactReportInfo;
import com.mnao.mfp.cr.repository.ContactReportAttachmentRepository;
import com.mnao.mfp.cr.service.FileHandlingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Service
public class FileHandlingServiceImpl implements FileHandlingService {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private static final String TEMP_LOC_PREFIX = "_TEMP_";
    private static final String SESSION_UPLOADED_FILES = "AttachmentUpload";

    @Autowired
    ContactReportAttachmentRepository attachmentRepository;

    @Override
    public List<ContactInfoAttachmentDto> doUpload(MultipartFile[] files, HttpServletRequest request) {
        try {
            List<ContactReportAttachment> savedFiles = new NullCheck<>((List<ContactReportAttachment>) request.getSession().getAttribute(SESSION_UPLOADED_FILES)).orElseList(Collections.emptyList());

            List<ContactInfoAttachmentDto> dtos = new ArrayList<>();
            for (MultipartFile file : files) {

                ContactInfoAttachmentDto dto = new ContactInfoAttachmentDto();
                dto.setMessage("Error Occurred while uploading");
                dto.setStatus(false);
                dto.setReportAttachment(null);
                String updatedFilePath = saveFile(file);
                if (!updatedFilePath.equals("")) {
                    ContactReportAttachment attachment = new ContactReportAttachment();
                    attachment.setAttachmentName(file.getOriginalFilename());
                    attachment.setAttachmentPath(updatedFilePath);
                    attachment.setAttachmentType(file.getContentType());
                    attachment.setStatus(AppConstants.StatusDraft);
                    updateSavedFiles(savedFiles, attachment);

                    dto.setReportAttachment(attachment);
                    dto.setStatus(true);
                    dto.setMessage("File Uploaded Successfully");

                    dtos.add(dto);
                }
            }
            request.getSession().setAttribute(SESSION_UPLOADED_FILES, savedFiles);
            return dtos;
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    private String saveFile(MultipartFile file) {
        String filePath = "";
        String fileName = "Something went wrong";
        try {
            fileName = getTemporaryFileName(file);
            filePath = getTemporaryFilePath(fileName);
            Path path = Paths.get(filePath);
            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, path, StandardCopyOption.REPLACE_EXISTING);
            }

            return fileName;
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
        return fileName;
    }

    private String getStorageFilePath(ContactReportAttachment attachment, String dealerCode) {
        String fpath = Utils.getAppProperty("permanent.file.storage.location");
        if (!fpath.endsWith("/"))
            fpath += "/";
        try {
            URI uri = new URI(fpath);
            fpath = uri.getPath();
        } catch (URISyntaxException e) {
            logger.error("URI Syntax Exception Error ", e);
        }
        checkCreatePath(fpath);
        fpath += String.format(AppConstants.file_storage_format, attachment.getAttachmentId(), dealerCode,
                attachment.getAttachmentName());
        return fpath;
    }

    private String getTemporaryFilePath(String fileName) {
        String fpath = Utils.getAppProperty("temp.file.storage.location");
        if (!fpath.endsWith("/"))
            fpath += "/";
        try {
            URI uri = new URI(fpath);
            fpath = uri.getPath();
        } catch (URISyntaxException e) {
            logger.error("URI Syntax Exception Error ", e);
        }
        checkCreatePath(fpath);
        fpath += fileName;
        return fpath;
    }

    private String getTemporaryFileName(MultipartFile file) {
        return TEMP_LOC_PREFIX + UUID.randomUUID() + "__" + file.getOriginalFilename();
    }

    private void updateSavedFiles(List<ContactReportAttachment> savedFiles, ContactReportAttachment attachment) {
        boolean doAdd = true;
        for (ContactReportAttachment savedAttachment : savedFiles) {
            if (savedAttachment.getAttachmentName().equals(attachment.getAttachmentName())
                    && savedAttachment.getAttachmentType().equals(attachment.getAttachmentType())) {
                doAdd = false;
                break;
            }
        }

        if (doAdd)
            savedFiles.add(attachment);
    }

    public String copyToPermanentLocation(ContactReportInfo report) {
        boolean flag = true;
        List<ContactReportAttachment> attachments = report.getAttachment();
        String contactReportId = Long.toString(report.getContactReportId());
        String responsetext = "Files moved successfully";
        ContactReportAttachment flaggedAttachment = null;
        if (attachments == null)
            return responsetext;
        for (ContactReportAttachment attachment : attachments) {
            String sourcePath = getTemporaryFilePath(attachment.getAttachmentPath());
            String destinationPath = getStorageFilePath(attachment, contactReportId);
            attachment.setContactReport(report);
            if (moveFiles(sourcePath, destinationPath)) {
                attachment.setAttachmentPath(destinationPath);
                attachment.setStatus(AppConstants.StatusSubmit);
                attachmentRepository.save(attachment);
            } else {
                flag = false;
                flaggedAttachment = attachment;
                break;
            }

        }
        if (!flag) {
            responsetext = "Failed to Transfer Attachment  with attachment id= " + flaggedAttachment.getAttachmentId();
        }
        return responsetext;
    }

    private boolean moveFiles(String sourcePath, String destinationPath) {
        boolean flag = false;
        try {
            Files.move(Paths.get(sourcePath), Paths.get(destinationPath));
            logger.info("File renamed and moved successfully");
            flag = true;
        } catch (Exception e) {
            logger.error("Exception occurred", e);
        }
        return flag;
    }

    @Transactional
    public String deleteAttachmentById(long attachmentId) {

        String response = "";
        ContactReportAttachment attachment = attachmentRepository.findByAttachmentIdAndIsActive(attachmentId, IsActiveEnum.YES.getValue());
        if (attachment != null) {
            if (attachment.getStatus() == 0 && attachment.getContactReport().getContactStatus() == 0) {
                attachment.setStatus(AppConstants.StatusDeleted);
                attachmentRepository.save(attachment);
                response = "File deletion successful";
            } else {
                response = "File once submitted cannot be deleted.";
            }

        } else {
            response = "File Attachment not found.";
        }
        return response;
    }

    @Transactional
    public String deleteAttachmentByAttachmentPath(String attachmentName) {
        String filePath;
        String response;
        filePath = getTemporaryFilePath(attachmentName);
        Path path = Paths.get(filePath);
        try {
            Files.delete(path);
            response = "File deletion successful";
        } catch (NoSuchFileException ex) {
            response = "No such file : " + path;
        } catch (IOException ex) {
            response = ex.getMessage();
        }
        return response;
    }

    public Resource loadFileAsResource(String fileName) {
        Path filePath = Paths.get(getTemporaryFilePath(fileName));
        return downloadResource(filePath);
    }

    public Resource loadFileAsResource(long attachmentId) {
        ContactReportAttachment attachment = attachmentRepository.findByAttachmentIdAndIsActive(attachmentId, IsActiveEnum.YES.getValue());
        Path filePath = Paths.get(attachment.getAttachmentPath());
        return downloadResource(filePath);

    }

    public Resource downloadResource(Path filePath) {
        try {
            Resource resource = new UrlResource(filePath.toUri());
            if (resource.exists()) {
                return resource;
            } else {
                logger.warn("No such file : {}", filePath);
            }
        } catch (MalformedURLException ex) {
            logger.error("No such file :", ex);
        }
        return null;
    }

    private void checkCreatePath(String locPath) {
        File folder = new File(locPath);
        if (!folder.exists() || !folder.isDirectory()) {
            try {
                Files.createDirectories(folder.toPath());
            } catch (IOException e) {
                logger.error("FAILED TO CREATE FOLDER" + locPath, e);
            }
        }
    }
}
