package com.mnao.mfp.cr.Service;

import com.mnao.mfp.cr.model.DealersByIssue;
import com.mnao.mfp.cr.util.ContactReportEnum;
import com.mnao.mfp.user.dao.MFPUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

//import com.mnao.mfp.cr.Mapper.ContactInfoMapper;
import com.mnao.mfp.cr.dto.ContactReportDto;
import com.mnao.mfp.cr.dto.ContactReportInfoDto;
import com.mnao.mfp.cr.dto.ReportByDealershipDto;
import com.mnao.mfp.cr.entity.ContactReportAttachment;
import com.mnao.mfp.cr.entity.ContactReportDiscussion;
import com.mnao.mfp.cr.entity.ContactReportInfo;
import com.mnao.mfp.cr.repository.ContactInfoRepository;
import org.springframework.web.bind.annotation.SessionAttribute;

import javax.transaction.Transactional;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

@Service
public class ContactReportServiceImpl implements ContactReportService {

	@Autowired
	private ContactInfoRepository contactInfoRepository;

	@Autowired
	private FileHandlingServiceImpl fileHandlingService;

	public List<DealersByIssue> getAllDealersByIssue() {
		return contactInfoRepository.findAll().stream().map(contactReportInfo -> {
			DealersByIssue dealersByIssue = new DealersByIssue();
			dealersByIssue.setIssue(contactReportInfo.getCurrentIssues());
			dealersByIssue.setDealership(contactReportInfo.getDlrCd());
			dealersByIssue.setLocation(contactReportInfo.getContactLocation());
			return dealersByIssue;
		}).collect(Collectors.toList());
	}

	/**
	 * 
	 */
	public String submitReportData(ContactReportInfoDto report, MFPUser mfpUser) throws Exception {
		String submission = "Unable to save contact report";
		try {
			ContactReportInfo reportInfo = new ContactReportInfo();
			if (report != null && report.getContactReportId() > 0) {
				reportInfo = contactInfoRepository.getById(report.getContactReportId());
			}

			reportInfo.setContactAuthor(
					report.getContactAuthor() != null ? report.getContactAuthor() : reportInfo.getContactAuthor());
			reportInfo.setContactDt(report.getContactDt() != null ? report.getContactDt() : reportInfo.getContactDt());
			reportInfo.setContactLocation(report.getContactLocation() != null ? report.getContactLocation()
					: reportInfo.getContactLocation());
			reportInfo.setContactReviewer(report.getContactReviewer() != null ? report.getContactReviewer()
					: reportInfo.getContactReviewer());

			reportInfo.setContactStatus(
					report.getContactStatus() != null ? report.getContactStatus() : reportInfo.getContactStatus());
			reportInfo.setContactType(
					report.getContactType() != null ? report.getContactType() : reportInfo.getContactType());
			String reps = report.getCorporateReps();
			reportInfo.setCorporateReps(
					reps != null ? (reps.length() > 250 ? reps.substring(0, 250) : reps) : reportInfo.getContactType());
			if (!CollectionUtils.isEmpty(report.getDiscussions())) {
				reportInfo.setDiscussions(report.getDiscussions());
				reportInfo.setCurrentIssues(
						report.getDiscussions().stream().filter(val -> Objects.nonNull(val.getTopic()))
								.map(value -> value.getTopic()).collect(Collectors.joining("|")));
			}
			reportInfo.setDlrCd(report.getDlrCd() != null ? report.getDlrCd() : reportInfo.getDlrCd());

			reportInfo.setDealers(report.getDealers() != null ? report.getDealers() : reportInfo.getDealers());
			if (!CollectionUtils.isEmpty(report.getDealerPersonnels())) {
				reportInfo.setDealerPersonnels(report.getDealerPersonnels());
			}
			duplicateAttachmentChecker(report.getAttachment());
			if (!CollectionUtils.isEmpty(report.getAttachment())) {
				reportInfo.setAttachment(report.getAttachment());
			}

			ContactReportInfo info = contactInfoRepository.save(reportInfo);
			if (report.getContactStatus() == 1) {
				fileHandlingService.copyToPermanentLocation(info);
			}
				
			submission = "Saved Success";
		} catch (Exception e) {
			e.printStackTrace();
			submission = "Failed - Metrics | DealerPersonnel is missing";
			throw new Exception(submission);
		}
		return submission;
	}

	/**
	 * 
	 * @param attachment
	 */
	private void duplicateAttachmentChecker(List<ContactReportAttachment> attachment) {
		if (!CollectionUtils.isEmpty(attachment)) {
			HashSet<String> attFiles = new HashSet<String>();
			attachment.forEach(att -> {
				if (attFiles.contains(att.getAttachmentName())) {
					throw new IllegalArgumentException("Duplicate Attachment: " + att.getAttachmentName());
				} else {
					attFiles.add(att.getAttachmentName());
				}
			});

		}
	}

	public ContactReportDto findByContactReportId(long ContactreporId) {
		ContactReportDto contactReportDto = new ContactReportDto();
		contactReportDto.setContactReport(contactInfoRepository.findByContactReportId(ContactreporId));
		return contactReportDto;
	}

	public List<ContactReportInfo> findByDlrCd(String dlrCd) {
		return contactInfoRepository.findByDlrCd(dlrCd);

	}

	public Map<String, List<ContactReportInfoDto>> getMyContactReport(String userId) {
		List<ContactReportInfo> contactReportInfos = contactInfoRepository.findByContactAuthor(userId);
		Map<String, List<ContactReportInfoDto>> contactReportInfoDtos = contactReportInfos.stream().map(reportInfo -> ContactReportInfoDto.builder()
				.contactReportId(reportInfo.getContactReportId())
				.contactDt(reportInfo.getContactDt())
				.dealers(reportInfo.getDealers())
				.contactStatus(reportInfo.getContactStatus())
				.updatedDt(reportInfo.getUpdatedDt() != null ? reportInfo.getUpdatedDt(): reportInfo.getCreatedDt())
				.build())
				.collect(Collectors.groupingBy( element -> ContactReportEnum.valueByStatus(element.getContactStatus()).getDisplayText()));

		return contactReportInfoDtos;
	}

	@Transactional
	public void deleteReportById(long contactReportId) {
		final int contactStatus = 0; // contactStatus 0 makes sure that the report is still a draft
		contactInfoRepository.deleteByContactReportIdAndContactStatus(contactReportId, contactStatus);
	}
}
