package com.mnao.mfp.cr.service.impl;

import com.mnao.mfp.cr.entity.ContactReportDiscussion;
import com.mnao.mfp.cr.model.DealersByIssue;
import com.mnao.mfp.cr.service.ContactReportService;
import com.mnao.mfp.cr.util.ContactReportEnum;
import com.mnao.mfp.user.dao.MFPUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

//import com.mnao.mfp.cr.Mapper.ContactInfoMapper;
import com.mnao.mfp.cr.dto.ContactReportDto;
import com.mnao.mfp.cr.dto.ContactReportInfoDto;
import com.mnao.mfp.cr.entity.ContactReportAttachment;
import com.mnao.mfp.cr.entity.ContactReportDealerPersonnel;
import com.mnao.mfp.cr.entity.ContactReportInfo;
import com.mnao.mfp.cr.repository.ContactInfoRepository;
import com.mnao.mfp.cr.repository.ContactReportDealerPersonnelRepository;

import javax.transaction.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class ContactReportServiceImpl implements ContactReportService {

	@Autowired
	private ContactInfoRepository contactInfoRepository;

	@Autowired
	private FileHandlingServiceImpl fileHandlingService;

	@Autowired
	private ContactReportDealerPersonnelRepository contactReportDealerPersonnelRepository;

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
	public String submitReportDataV2(ContactReportInfoDto report, MFPUser mfpUser) throws Exception {
		String submission = "Unable to save contact report";
		try {
			ContactReportInfo reportInfo = new ContactReportInfo();
			boolean isDealerUpdated = false;
			if (report != null && report.getContactReportId() > 0) {
				reportInfo = contactInfoRepository.getById(report.getContactReportId());
				if (reportInfo.getContactStatus() == ContactReportEnum.DRAFT.getStatusCode()
						&& report.getContactStatus() == ContactReportEnum.CANCELLED.getStatusCode()) {
					contactInfoRepository.delete(reportInfo);
					submission = "Report removed successfully";
					return submission;
				} else if (report.getDealers() != null) {
					isDealerUpdated = !reportInfo.getDealers().getDlrCd().trim()
							.equalsIgnoreCase(report.getDealers().getDlrCd().trim());
				}
			}
			if (isDealerUpdated && report != null && report.getContactReportId() > 0) {
				reportInfo.setDealers(null);
			}
			//Update Author only if DRAFT
			if (reportInfo.getContactStatus() == ContactReportEnum.DRAFT.getStatusCode()) {
				reportInfo.setContactAuthor(
						report.getContactAuthor() != null ? report.getContactAuthor() : reportInfo.getContactAuthor());
			}
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
					reps != null ? (reps.length() > 250 ? reps.substring(0, 250) : reps) : reportInfo.getCorporateReps());
			// Sandip: Does discussion changes and deletes need to be handled? 
			if (!CollectionUtils.isEmpty(report.getDiscussions())) {
				reportInfo.setDiscussions(report.getDiscussions());
				reportInfo.setCurrentIssues(
						report.getDiscussions().stream().filter(val -> Objects.nonNull(val.getTopic()))
								.map(ContactReportDiscussion::getTopic).collect(Collectors.joining("|")));
			}
			reportInfo.setDlrCd(report.getDlrCd() != null ? report.getDlrCd() : reportInfo.getDlrCd());

			reportInfo.setDealers(report.getDealers() != null ? report.getDealers() : reportInfo.getDealers());
			// Addition and Deletion of Dealer Personnel
			if (!CollectionUtils.isEmpty(report.getDealerPersonnels()) && !CollectionUtils.isEmpty(reportInfo.getDealerPersonnels())) {
				// Deletions
				List<Integer> removedList = new ArrayList<>();
				List<ContactReportDealerPersonnel> newPers = report.getDealerPersonnels();
				List<ContactReportDealerPersonnel> currentPers = reportInfo.getDealerPersonnels();
				for(int i = 0 ; i < currentPers.size(); i++) {
					ContactReportDealerPersonnel existingDp = currentPers.get(i);
					boolean found = false;
					for(int j = 0 ; j < newPers.size(); j++ ) {
						ContactReportDealerPersonnel newDp = newPers.get(j);
						if( newDp.getPersonnelIdCd().equalsIgnoreCase(existingDp.getPersonnelIdCd())) {
							found = true;
							break;
						}
					}
					if( ! found ) {
						removedList.add(i);
					}
				}
				// Remove the persons from Current, if any
				for(int i = removedList.size() - 1; i >= 0; i--) {
					currentPers.remove(removedList.get(i).intValue());
				}
				// Additions
				List<ContactReportDealerPersonnel> newList = new ArrayList<>();
				for(int i = 0 ; i < newPers.size(); i++) {
					ContactReportDealerPersonnel newDp = newPers.get(i);
					boolean found = false;
					for(int j = 0 ; j < currentPers.size(); j++ ) {
						ContactReportDealerPersonnel existingDp = currentPers.get(j);
						if( newDp.getPersonnelIdCd().equalsIgnoreCase(existingDp.getPersonnelIdCd())) {
							found = true;
							break;
						}
					}
					if( ! found ) {
						newList.add(newDp);
					}
				}
				if( newList.size() > 0) {
					currentPers.addAll(newList);
				}
			} else 
			//
			if (!CollectionUtils.isEmpty(report.getDealerPersonnels())) {
				reportInfo.setDealerPersonnels(report.getDealerPersonnels());
			}
			duplicateAttachmentChecker(report.getAttachment());
			// Need to check for Additions and Deletions here as well
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

	/*
	 * New Save ContactReport
	 */
	public String submitReportData(ContactReportInfoDto report, MFPUser mfpUser) throws Exception {
		String submission = "Unable to save contact report";
		try {
			ContactReportInfo reportInfo = new ContactReportInfo();
			boolean isDealerUpdated = false;
			if (report != null && report.getContactReportId() > 0) {
				reportInfo = contactInfoRepository.getById(report.getContactReportId());
				if (reportInfo.getContactStatus() == ContactReportEnum.DRAFT.getStatusCode()
						&& report.getContactStatus() == ContactReportEnum.CANCELLED.getStatusCode()) {
					contactInfoRepository.delete(reportInfo);
				}
				if (report.getDealers() != null) {
					isDealerUpdated = !reportInfo.getDealers().getDlrCd()
							.equalsIgnoreCase(report.getDealers().getDlrCd());
				}
			}
			if (isDealerUpdated && report != null && report.getContactReportId() > 0) {
//				reportInfo.setDealers(null);;
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
								.map(ContactReportDiscussion::getTopic).collect(Collectors.joining("|")));
			}
			reportInfo.setDlrCd(report.getDlrCd() != null ? report.getDlrCd() : reportInfo.getDlrCd());

			reportInfo.setDealers(report.getDealers() != null ? report.getDealers() : reportInfo.getDealers());
//			if (!CollectionUtils.isEmpty(report.getDealerPersonnels()) && !CollectionUtils.isEmpty(reportInfo.getDealerPersonnels())) {
//				List<ContactReportDealerPersonnel> removedList = new ArrayList<>();
//				for(int i = 0 ; i < reportInfo.getDealerPersonnels().size(); i++) {
//					ContactReportDealerPersonnel existingDp = reportInfo.getDealerPersonnels().get(i);
//					boolean found = false;
//					for(int j = 0 ; j < report.getDealerPersonnels().size(); j++ ) {
//						ContactReportDealerPersonnel newDp = report.getDealerPersonnels().get(j);
//						if( newDp.getPersonnelIdCd().equalsIgnoreCase(existingDp.getPersonnelIdCd())) {
//							found = true;
//							break;
//						}
//					}
//					if( ! found ) {
//						existingDp.setPersonnelId(i);
//						removedList.add(existingDp);
//					}
//				}
//				if (!CollectionUtils.isEmpty(removedList)) {
//					contactReportDealerPersonnelRepository.saveAll(removedList);
//				}
//			} else 
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

		return contactReportInfos.stream()
				.map(reportInfo -> ContactReportInfoDto.builder().contactReportId(reportInfo.getContactReportId())
						.contactDt(reportInfo.getContactDt()).dealers(reportInfo.getDealers())
						.contactStatus(reportInfo.getContactStatus())
						.updatedDt(reportInfo.getUpdatedDt() != null ? reportInfo.getUpdatedDt()
								: reportInfo.getCreatedDt())
						.build())
				.collect(Collectors.groupingBy(
						element -> ContactReportEnum.valueByStatus(element.getContactStatus()).getDisplayText()));
	}

	@Transactional
	public void deleteReportById(long contactReportId) {
		final int contactStatus = 0; // contactStatus 0 makes sure that the report is still a draft
		contactInfoRepository.deleteByContactReportIdAndContactStatus(contactReportId, contactStatus);
	}
}
