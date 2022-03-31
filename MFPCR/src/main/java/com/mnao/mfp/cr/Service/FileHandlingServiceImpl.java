package com.mnao.mfp.cr.Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.mnao.mfp.common.util.AppConstants;
import com.mnao.mfp.common.util.Utils;
import com.mnao.mfp.cr.dto.ContactInfoAttachmentDto;
import com.mnao.mfp.cr.entity.ContactReportAttachment;
import com.mnao.mfp.cr.entity.ContactReportInfo;
import com.mnao.mfp.cr.repository.ContactReportAttachmentRepository;

import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.file.DirectoryNotEmptyException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

@Service
public class FileHandlingServiceImpl implements FileHandlingService {
	private static final String TEMP_LOC_PREFIX = "_TEMP_";
	private String sessionUploadedFiles = "AttachmentUpload";
	private Path fileStorageLocation;

	@Autowired
	ContactReportAttachmentRepository attachmentRepository;

	@Override
	public List<ContactInfoAttachmentDto> doUpload(MultipartFile[] files, HttpServletRequest request) {
		try {
			List<ContactReportAttachment> savedFiles = (List<ContactReportAttachment>) request.getSession()
					.getAttribute(sessionUploadedFiles);
			if (savedFiles == null) {
				savedFiles = new ArrayList<>();
			}

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
			request.getSession().setAttribute(sessionUploadedFiles, savedFiles);
			return dtos;
		} catch (Exception e) {
			return null;
		}
	}

	private String saveFile(MultipartFile file) {
		String filePath = "";
		String fileName = "Something went wrong";
		try {
//             filePath=getTemporaryFilePath(file);
			fileName = getTemporaryFileName(file);
			filePath = getTemporaryFilePath(fileName);
			Path path = Paths.get(filePath);
			try (InputStream inputStream = file.getInputStream()) {
				Files.copy(inputStream, path, StandardCopyOption.REPLACE_EXISTING);
			}

			return fileName;
		} catch (Exception e) {
			System.out.print(e.getMessage());
		}
		return fileName;
	}

	private String getStorageFilePath(ContactReportAttachment attachment, String dealerCode) {
//        String fpath=AppConstants.permanent_file_storage_location;
		String fpath = Utils.getAppProperty("permanent.file.storage.location");
		if (!fpath.endsWith("\\"))
			fpath += "\\";
		fpath += String.format(AppConstants.file_storage_format, attachment.getAttachmentId(), dealerCode,
				attachment.getAttachmentName());
		return fpath;
	}

	private String getTemporaryFilePath(MultipartFile file) {
//        String fpath= AppConstants.temp_file_storage_location;
		String fpath = Utils.getAppProperty("temp.file.storage.location");
		if (!fpath.endsWith("\\"))
			fpath += "\\";
		fpath += TEMP_LOC_PREFIX + getTemporaryFileName(file);
		return fpath;
	}

	private String getTemporaryFilePath(String fileName) {
//        String fpath= AppConstants.temp_file_storage_location;
		String fpath = Utils.getAppProperty("temp.file.storage.location");
		if (!fpath.endsWith("\\"))
			fpath += "\\";
		fpath += fileName;
		return fpath;
	}

	private String getTemporaryFileName(MultipartFile file) {
		String fpath = TEMP_LOC_PREFIX + UUID.randomUUID().toString() + "__" + file.getOriginalFilename();
		return fpath;
	}

	private void updateSavedFiles(List<ContactReportAttachment> savedFiles, ContactReportAttachment attachment) {
		boolean doAdd = true;
		for (int i = 0; i < savedFiles.size(); i++) {
			ContactReportAttachment savedAttachment = savedFiles.get(i);

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
		if( attachments == null )
			return responsetext;
		for (ContactReportAttachment attachment : attachments) {
			String sourcePath = getTemporaryFilePath(attachment.getAttachmentPath());
			String destinationPath = getStorageFilePath(attachment, contactReportId);
			attachment.setContactReport(report);
//            if(getContactReport(attachment.getAttachmentName(),report) == null) {
			if (moveFiles(sourcePath, destinationPath)) {
				attachment.setAttachmentPath(destinationPath);
				attachment.setStatus(AppConstants.StatusSubmit);
				attachmentRepository.save(attachment);
			} else {
				flag = false;
				flaggedAttachment = attachment;
				break;
			}

//            }

		}
		if (!flag) {
			responsetext = "Failed to Transfer Attachment  with attachment id= " + flaggedAttachment.getAttachmentId();
		}
		return responsetext;
	}

	private boolean moveFiles(String sourcePath, String destinationPath) {
		boolean flag = false;
		try {
			Path temp = Files.move(Paths.get(sourcePath), Paths.get(destinationPath));
			if (temp != null) {
				System.out.println("File renamed and moved successfully");
				flag = true;
			} else {
				System.out.println("Failed to move the file");
			}
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
		return flag;
	}

	@Transactional
	public String deleteAttachmentById(long attachmentId) {
		final int Status = 0;
		String response = "";
		ContactReportAttachment attachment = attachmentRepository.findByAttachmentId(attachmentId);
		if (attachment != null) {
			if (attachment.getStatus() == 0 && attachment.getContactReport().getContactStatus() == 0) {
				attachment.setStatus(AppConstants.StatusDeleted);
				attachmentRepository.save(attachment);
				response = "File deletion successful";
			} else {
				response = "File once submitted cannot be deleted.";
			}

		} else {
			response = "File Attachmentment not found.";
		}
		return response;
	}

	@Transactional
	public String deleteAttachmentByAttachmentPath(String attachmentName) {
		String filePath = "";
		String response = "";
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
		ContactReportAttachment attachment = attachmentRepository.findByAttachmentId(attachmentId);
		Path filePath = Paths.get(attachment.getAttachmentPath());
		return downloadResource(filePath);

	}

	public Resource downloadResource(Path filePath) {
		try {
			Resource resource = new UrlResource(filePath.toUri());
			if (resource.exists()) {
				return resource;
			} else {
				System.out.printf("No such file : %s\n", filePath);
			}
		} catch (MalformedURLException ex) {
			System.out.printf("No such file :\n");
		}
		return null;
	}
}
