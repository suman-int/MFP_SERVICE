package com.mnao.mfp.cr.service.impl;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.mnao.mfp.common.util.AppConstants;
import com.mnao.mfp.common.util.IsActiveEnum;
import com.mnao.mfp.common.util.NullCheck;
import com.mnao.mfp.common.util.Utils;
import com.mnao.mfp.cr.dto.ContactInfoAttachmentDto;
import com.mnao.mfp.cr.entity.ContactReportAttachment;
import com.mnao.mfp.cr.entity.ContactReportInfo;
import com.mnao.mfp.cr.repository.ContactReportAttachmentRepository;
import com.mnao.mfp.cr.service.FileHandlingService;

@Service
public class FileHandlingServiceImpl implements FileHandlingService {
	private static final Logger log = LoggerFactory.getLogger(FileHandlingServiceImpl.class);
	private static final String TEMP_LOC_PREFIX = "_TEMP_";
	private static final String SESSION_UPLOADED_FILES = "AttachmentUpload";
	private String storageMountName = "AppData";

	//
	@Autowired
	ContactReportAttachmentRepository attachmentRepository;

	public FileHandlingServiceImpl() {
		super();
		this.storageMountName = Utils.getAppProperty(AppConstants.STORAGE_MOUNT_NAME);
	}

	@Override
	public List<ContactInfoAttachmentDto> doUpload(MultipartFile[] files, HttpServletRequest request) {
		try {
			List<ContactReportAttachment> savedFiles = new NullCheck<>(
					(List<ContactReportAttachment>) request.getSession().getAttribute(SESSION_UPLOADED_FILES))
					.orElseList(new ArrayList<>(0));

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
			log.error(e.getMessage());
		}
		return fileName;
	}

	private String getStorageFilePath(ContactReportAttachment attachment, String dealerCode) {
		String fpath = Utils.getAppProperty("permanent.file.storage.location");
		fpath = getPathStr(fpath);
		checkCreatePath(fpath);
		fpath += String.format(AppConstants.file_storage_format, attachment.getAttachmentId(), dealerCode,
				attachment.getAttachmentName());
		return fpath;
	}

	private String getTemporaryFilePath(String fileName) {
		String fpath = Utils.getAppProperty("temp.file.storage.location");
		fpath = getPathStr(fpath);
		checkCreatePath(fpath);
		fpath += fileName;
		return fpath;
	}

	private String getPathStr(String fpath) {
		if (!fpath.endsWith("/"))
			fpath += "/";
		try {
			URI uri = new URI(fpath);
			fpath = uri.getPath();
		} catch (URISyntaxException e) {
			log.error("URI Syntax Exception Error ", e);
		}
		return fpath;
	}

	private String getTemporaryFileName(MultipartFile file) {
		return getTemporaryFileName(file.getOriginalFilename());
	}

	private String getTemporaryFileName(String fileName) {
		return TEMP_LOC_PREFIX + UUID.randomUUID() + "__" + fileName;
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
		log.info("Saving attachments for CR {}", contactReportId);
		String responsetext = "Files moved successfully";
		String failedSaveAttachments = "";
		if (attachments == null)
			return responsetext;
		for (ContactReportAttachment attachment : attachments) {
			if (attachment.getIsActive().equalsIgnoreCase(IsActiveEnum.YES.getValue())) {
				if (attachment.getAttachmentPath().startsWith("_TEMP")) {
					String sourcePath = getTemporaryFilePath(attachment.getAttachmentPath());
					log.info("Aattachment Temp path {}", sourcePath);
					if (Paths.get(sourcePath).toFile().exists()) {
						String destinationPath = getStorageFilePath(attachment, contactReportId);
						log.info("Aattachment Dest path {}", destinationPath);
						attachment.setContactReport(report);
						if (moveFiles(sourcePath, destinationPath)) {
							attachment.setAttachmentPath(destinationPath);
							attachment.setStatus(AppConstants.StatusSubmit);
							// attachmentRepository.save(attachment); // DO NOT SAVE - it will get
							// automatically saved with CR
						} else {
							flag = false;
							failedSaveAttachments += attachment.getAttachmentName() + " ";
						}
					}
				}
			}
		}
		if (!flag) {
			responsetext = "Failed to Transfer Attachment  with attachment id(s)= " + failedSaveAttachments;
		}
		return responsetext;
	}

	private boolean moveFiles(String sourcePath, String destinationPath) {
		boolean flag = false;
		log.info("Moving file From {} to {}", sourcePath, destinationPath);
		try {
			Files.move(Paths.get(sourcePath), Paths.get(destinationPath));
			log.info("File renamed and moved successfully");
			flag = true;
		} catch (Exception e) {
			log.error("Exception occurred", e);
		}
		return flag;
	}

	private boolean copyFile(String sourcePath, String destinationPath) {
		boolean flag = false;
		log.info("Copying file From {} to {}", sourcePath, destinationPath);
		try {
			Files.copy(Paths.get(sourcePath), Paths.get(destinationPath));
			log.info("File copied successfully");
			flag = true;
		} catch (Exception e) {
			log.error("Exception occurred", e);
		}
		return flag;
	}

	@Transactional
	public String deleteAttachmentById(long attachmentId) {

		String response = "";
		ContactReportAttachment attachment = attachmentRepository.findByAttachmentIdAndIsActive(attachmentId,
				IsActiveEnum.YES.getValue());
		if (attachment != null) {
			if (attachment.getStatus() == 0 && attachment.getContactReport().getContactStatus() == 0) {
				attachment.setStatus(AppConstants.StatusDeleted);
				attachment.setIsActive(IsActiveEnum.NO.getValue());
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
		Path filePath = Paths.get(fileName);
		if (fileName.startsWith("_TEMP")) {
			filePath = Paths.get(getTemporaryFilePath(fileName));
		}
		return downloadResource(filePath);
	}

	public Resource loadFileAsResource(long attachmentId) {
		ContactReportAttachment attachment = attachmentRepository.findByAttachmentIdAndIsActive(attachmentId,
				IsActiveEnum.YES.getValue());
		Path filePath = Paths.get(attachment.getAttachmentPath());
		String currentFileName = filePath.getFileName().toString();
		if (currentFileName.startsWith("_TEMP")) {
			filePath = Paths.get(getTemporaryFilePath(currentFileName));
		}
		filePath = checkCorrect(attachment, filePath);
		log.info("Returning file {} ", filePath.toString());
		return downloadResource(filePath);

	}

	public Resource downloadResource(Path filePath) {
		if (filePath != null) {
			Path fPath = filePath;
			try {
				if ((fPath.toString() != null) && fPath.toString().startsWith(storageMountName)) {
					fPath = Paths.get("/" + filePath.toString());
				}
				URI fURI = fPath.toUri();
				Resource resource = new UrlResource(fURI);
				if (resource.exists()) {
					return resource;
				} else {
					log.warn("No such file : {}", fURI);
				}
			} catch (MalformedURLException ex) {
				log.error("No such file :", ex);
			}
		}
		return null;
	}

	private void checkCreatePath(String locPath) {
		File folder = new File(locPath);
		if (!folder.exists() || !folder.isDirectory()) {
			try {
				Files.createDirectories(folder.toPath());
			} catch (IOException e) {
				log.error("FAILED TO CREATE FOLDER" + locPath, e);
			}
		}
	}

	private Path checkCorrect(ContactReportAttachment attachment, Path filePath) {
		String fnm = attachment.getAttachmentName();
		String prmFpath = getPathStr(Utils.getAppProperty("permanent.file.storage.location"));
		String tmpFpath = getPathStr(Utils.getAppProperty("temp.file.storage.location"));
		if (Files.exists(filePath)) {
			Path copiedPath = checkFileNameCopy(attachment, filePath);
//			if (!copiedPath.equals(filePath)) {
			if (!pathsEqual(copiedPath, filePath)) {
				attachment.setAttachmentPath(copiedPath.toString());
				updateAttachmentRecord(attachment);
			}
			return copiedPath;
		}
		Optional<Path> foundPath = null;
		if (attachment.getAttachmentId() == 0) {
			foundPath = findFile(fnm, Paths.get(tmpFpath));
			if ((foundPath == null) || (!foundPath.isPresent())) {
				foundPath = findFile(fnm, Paths.get(prmFpath));
			}
		} else {
			foundPath = findFile(fnm, Paths.get(prmFpath));
			if (!foundPath.isPresent()) {
				foundPath = findFile(fnm, Paths.get(tmpFpath));
			}
		}
		if ((foundPath != null) && (foundPath.isPresent())) {
			Path fpath = foundPath.get();
			try {
				Path correctedPath = doDBCorrection(attachment, fpath);
				return correctedPath;
			} catch (IOException e) {
				log.error("ERROR Correcting attachment storage: ", e);
			}
		}
		return filePath;
	}

	private boolean pathsEqual(Path copiedPath, Path filePath) {
		boolean rv = true;
		if (copiedPath.getNameCount() != filePath.getNameCount()) {
			rv = false;
		} else {
			for (int i = 0; i < copiedPath.getNameCount(); i++) {
				if (!copiedPath.getName(i).toString().equalsIgnoreCase(filePath.getName(i).toString())) {
					rv = false;
					break;
				}
			}
		}
		return rv;
	}

	private Path checkFileNameCopy(ContactReportAttachment attachment, Path filePath) {
		if (attachment.getAttachmentId() != 0) {
			String crID = Long.toString(attachment.getContactReport().getContactReportId());
			String tgtFile = String.format(AppConstants.file_storage_format, attachment.getAttachmentId(), crID,
					attachment.getAttachmentName());
			String tgtPath = getStorageFilePath(attachment, crID);
			if (!tgtFile.equalsIgnoreCase(filePath.getFileName().toString())) {
				if (copyFile(filePath.toString(), tgtPath)) {
					return Paths.get(tgtPath);
				}
			}
		}
		return filePath;
	}

	private Path doDBCorrection(ContactReportAttachment attachment, Path fpath) throws IOException {
		String prmFpath = getPathStr(Utils.getAppProperty("permanent.file.storage.location"));
		String tmpFpath = getPathStr(Utils.getAppProperty("temp.file.storage.location"));
		boolean foundInTmp = fpath.startsWith(tmpFpath);
		Path retPath = fpath;
		if (attachment.getAttachmentId() == 0) {
			// The Contact Report has not been saved yet
			if (foundInTmp) {
				// Just update the filename. DO NOT UPDATE DB.
				// It will automatically get updated when
				// saving CR
				attachment.setAttachmentPath(fpath.getFileName().toString());
				retPath = fpath;
			} else {
				// COPY file from storage to tmp
				// DO NOT MOVE
				String tgtFName = getTemporaryFileName(fpath.getFileName().toString());
				String tgtPathName = getTemporaryFilePath(tgtFName);
				Path tgtPath = Paths.get(tgtPathName);
				Files.copy(fpath, tgtPath, StandardCopyOption.REPLACE_EXISTING);
				attachment.setAttachmentPath(tgtFName);
				retPath = tgtPath;
			}
		} else {
			// Contact Report has been saved and
			// both Contact Report ID and attachment ID has been generated
			if (foundInTmp) {
				// COPY file from tmp to storage
				// DO NOT MOVE
				// Update attachmentPath
				Path srcPath = fpath;
				String crID = Long.toString(attachment.getContactReport().getContactReportId());
				String tgtPathName = getStorageFilePath(attachment, crID);
				Path tgtPath = Paths.get(tgtPathName);
				Files.copy(srcPath, tgtPath, StandardCopyOption.REPLACE_EXISTING);
				attachment.setAttachmentPath(tgtPathName);
				retPath = tgtPath;
			} else {
				Path copiedPath = checkFileNameCopy(attachment, fpath);
				// Update attachmentPath
				attachment.setAttachmentPath(copiedPath.toString());
				retPath = copiedPath;
			}
			// Update DB
			updateAttachmentRecord(attachment);
		}
		return retPath;
	}

	private boolean updateAttachmentRecord(ContactReportAttachment attachment) {
		boolean rv = false;
		attachment.setStatus(attachment.getStatus() | AppConstants.StatusDBUpdated);
		attachmentRepository.save(attachment);
		return rv;
	}

	private Optional<Path> findFile(String fnm, Path folder) {
		try (Stream<Path> allFiles = Files.list(folder)) {
			return allFiles.filter(fp -> fp.getFileName().toString().endsWith(fnm)).findFirst();
		} catch (IOException e) {
			log.error("ERROR retrieving list of files from " + folder.toString(), e);
		}
		return null;
	}
}
