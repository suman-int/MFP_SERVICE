package com.mnao.mfp.cr.service.impl;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javax.mail.MessagingException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import com.mnao.mfp.common.util.AppConstants;
import com.mnao.mfp.common.util.IsActiveEnum;
import com.mnao.mfp.common.util.NullCheck;
import com.mnao.mfp.cr.dto.ContactReportDto;
import com.mnao.mfp.cr.dto.ContactReportInfoDto;
import com.mnao.mfp.cr.dto.ContactReportTopicDto;
import com.mnao.mfp.cr.entity.ContactReportAttachment;
import com.mnao.mfp.cr.entity.ContactReportDealerPersonnel;
import com.mnao.mfp.cr.entity.ContactReportDiscussion;
import com.mnao.mfp.cr.entity.ContactReportInfo;
import com.mnao.mfp.cr.model.DealersByIssue;
import com.mnao.mfp.cr.repository.ContactInfoRepository;
import com.mnao.mfp.cr.repository.ContactReportDealerPersonnelRepository;
import com.mnao.mfp.cr.service.ContactReportService;
import com.mnao.mfp.cr.service.EmailService;
import com.mnao.mfp.cr.util.ContactReportEnum;
import com.mnao.mfp.list.cache.AllActiveEmployeesCache;
import com.mnao.mfp.list.dao.ListPersonnel;
import com.mnao.mfp.user.dao.MFPUser;

@Service
public class ContactReportServiceImpl implements ContactReportService {
	//
	private static final Logger log = LoggerFactory.getLogger(ContactReportServiceImpl.class);
	//

	@Autowired
	private ContactInfoRepository contactInfoRepository;

	@Autowired
	private FileHandlingServiceImpl fileHandlingService;

	@Autowired
	private ContactReportDealerPersonnelRepository contactReportDealerPersonnelRepository;

	@Autowired
	private EmailService emailService;

	@Autowired
	private AllActiveEmployeesCache allEmployeesCache;

	@Override
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
	@Override
	public String submitReportDataV2(ContactReportInfoDto reportDto, MFPUser mfpUser, String currURL) throws Exception {
		String submission = "Unable to save contact report";
		if (reportDto == null)
			throw new AssertionError();
		int origCRStatus = -1;
		try {
			ContactReportInfo reportDb = new ContactReportInfo();
			boolean isDealerUpdated = false;
			if (reportDto.getContactReportId() > 0) {
				reportDb = contactInfoRepository.getById(reportDto.getContactReportId());
				if (!mfpUser.getUserid().trim().equalsIgnoreCase(reportDb.getContactAuthor())) {
					ListPersonnel lemp = allEmployeesCache.getByWSLCd(mfpUser.getUserid());
					if (!lemp.isCorporatePerson()) {
						if (!mfpUser.getEmployeeNumber().trim()
								.equalsIgnoreCase(reportDb.getContactReviewer().trim())) {
							submission = "You are not authorized to modify this report.";
							throw new Exception("You are not authorized to modify this report.");
						}
					}
				}
				origCRStatus = reportDb.getContactStatus();
				if (reportDb.getContactStatus() == ContactReportEnum.DRAFT.getStatusCode()
						&& reportDto.getContactStatus() == ContactReportEnum.CANCELLED.getStatusCode()) {
					reportDb.setContactStatus(reportDto.getContactStatus());
					List<ContactReportDealerPersonnel> existingDealerPersonel = reportDb.getDealerPersonnels();
					existingDealerPersonel.forEach(val -> val.setIsActive(IsActiveEnum.NO.getValue()));
					contactReportDealerPersonnelRepository.saveAll(existingDealerPersonel);
					reportDb.setIsActive(IsActiveEnum.NO.getValue());
					contactInfoRepository.save(reportDb);
					submission = "Report removed successfully";
					return submission;
				} else if (reportDto.getDealers() != null) {
					isDealerUpdated = !reportDb.getDealers().getDlrCd().trim()
							.equalsIgnoreCase(reportDto.getDealers().getDlrCd().trim());
				}
			}
			if (new NullCheck<>(reportDto).with(ContactReportInfoDto::getContactStatus)
					.get() == ContactReportEnum.CANCELLED.getStatusCode()) {
				reportDb.setContactStatus(reportDto.getContactStatus());
			} else {
				if (isDealerUpdated && reportDto.getContactReportId() > 0) {
					reportDb.setDealers(null);
				}
				// Update Author only if in DRAFT
				if (reportDto.getContactStatus() == ContactReportEnum.DRAFT.getStatusCode()
						|| reportDto.getContactStatus() == ContactReportEnum.SUBMITTED.getStatusCode()) {
					reportDb.setContactAuthor(
							new NullCheck<>(mfpUser).with(MFPUser::getUserid).orElse(reportDb.getContactAuthor()));
				}
				reportDb.setContactDt(
						reportDto.getContactDt() != null ? reportDto.getContactDt() : reportDb.getContactDt());
				reportDb.setContactLocation(reportDto.getContactLocation() != null ? reportDto.getContactLocation()
						: reportDb.getContactLocation());
				reportDb.setContactReviewer(reportDto.getContactReviewer() != null ? reportDto.getContactReviewer()
						: reportDb.getContactReviewer());

				reportDb.setContactStatus(reportDto.getContactStatus() != null ? reportDto.getContactStatus()
						: reportDb.getContactStatus());
				reportDb.setContactType(
						reportDto.getContactType() != null ? reportDto.getContactType() : reportDb.getContactType());
				String reps = reportDto.getCorporateReps();
				reportDb.setCorporateReps(reps != null ? (reps) : reportDb.getCorporateReps());
				// Sandip: Does discussion changes and deletes need to be handled?
				// 20-Jul-2022 : YES
				processDiscussions(reportDto, reportDb);
				if (!CollectionUtils.isEmpty(reportDb.getDiscussions())) {
//					reportInfo.setDiscussions(report.getDiscussions());
					reportDb.setCurrentIssues(reportDto.getDiscussions().stream().map(ContactReportDiscussion::getTopic)
							.filter(Objects::nonNull).collect(Collectors.joining("|")));
				}
				reportDb.setDlrCd(reportDto.getDlrCd() != null ? reportDto.getDlrCd() : reportDb.getDlrCd());

				reportDb.setDealers(reportDto.getDealers() != null ? reportDto.getDealers() : reportDb.getDealers());
			}

			// Submit and Reviewed Date
			if (reportDb.getContactStatus() != origCRStatus) {
				if (reportDb.getContactStatus() == ContactReportEnum.SUBMITTED.getStatusCode()) {
					reportDb.setSubmittedDt(LocalDate.now());
				} else if (reportDb.getContactStatus() == ContactReportEnum.REVIEWED.getStatusCode()) {
					reportDb.setReviewedDt(LocalDate.now());
					reportDb.setReviewedBy(mfpUser.getUserid());
				}
			}
			// Additional Dealership personnel
			String addPersonnel = reportDto.getDealerPersonnels().stream().filter(dp -> dp.getPersonnelId() == -9999999L)
					.map(ContactReportDealerPersonnel::getPersonnelIdCd).collect(Collectors.joining("|"));
			if (!addPersonnel.isEmpty()) {
				reportDb.setAddDealerPersonnel(addPersonnel);
			} else {
				reportDb.setAddDealerPersonnel(null);
			}
			// Addition and Deletion of Dealer Personnel
			reportDto.setDealerPersonnels(reportDto.getDealerPersonnels().stream()
					.filter(dp -> dp.getPersonnelId() != -9999999L).collect(Collectors.toList()));
			addRemoveDealerPersonnel(reportDto, reportDb);
			duplicateAttachmentChecker(reportDto.getAttachment());
			/**
			 * CHECK FOR Status AS ATTCH RECORD MAY HAVE BEEN UPDATED WHILE CORRECTING
			 * ATTACHMENT LOCATION OR DELETION - SANDIP 24-JUN-2022
			 **/
			processAttachments(reportDto, reportDb);
			//
			ContactReportInfo info = contactInfoRepository.save(reportDb);
			if (info.getAttachment() != null && info.getAttachment().size() > 0) {
				fileHandlingService.copyToPermanentLocation(info);
				info = contactInfoRepository.save(info);
			}
			//
			submission = "Saved Success";
			if (info.getContactStatus() == ContactReportEnum.SUBMITTED.getStatusCode()
					|| info.getContactStatus() == ContactReportEnum.REVIEWED.getStatusCode()
					|| info.getContactStatus() == ContactReportEnum.DISCUSSION_REQUESTED.getStatusCode()) {
				try {
					String emailResp = emailService.sendEmailNotification(reportDb, origCRStatus, mfpUser);
					if (!emailResp.startsWith("OK"))
						submission += "; " + emailResp;
				} catch (MessagingException ex) {
					log.error("Failed to send email.", ex);
					submission += "; Error send email";
				}
			}
		} catch (Exception e) {
			log.error("", e);
			submission = "Failed to save Contact Report.";
			throw new Exception(submission);
		}
		return submission;
	}

	private void processDiscussions(ContactReportInfoDto reportDto, ContactReportInfo reportDb) {
		final List<ContactReportDiscussion> finalDiscussions = new ArrayList<>();
		if ((!CollectionUtils.isEmpty(reportDto.getDiscussions()))
				&& (!CollectionUtils.isEmpty(reportDb.getDiscussions()))) {
			Map<Long, ContactReportDiscussion> dbDiscs = new HashMap<>();
			Map<Long, ContactReportDiscussion> uiDiscs = new HashMap<>();
			reportDb.getDiscussions().forEach(disc -> {
				if( disc.getIsActive() == null )
					disc.setIsActive(IsActiveEnum.NO.getValue());
				if (disc.getIsActive().equalsIgnoreCase(IsActiveEnum.YES.getValue()))
					dbDiscs.put(disc.getDiscussionId(), disc);
				else
					finalDiscussions.add(disc);
			});
			reportDto.getDiscussions().forEach(disc -> {
				if (disc.getDiscussionId() == 0) {
					disc.setIsActive(IsActiveEnum.YES.getValue());
					finalDiscussions.add(disc);
				} else {
					uiDiscs.put(disc.getDiscussionId(), disc);
				}
			});
			uiDiscs.values().forEach(uiDisc -> {
				if (uiDisc.getDiscussionId() == 0) {
					uiDisc.setIsActive(IsActiveEnum.YES.getValue());
					finalDiscussions.add(uiDisc);
				} else if (dbDiscs.containsKey(uiDisc.getDiscussionId())) {
					ContactReportDiscussion dbDisc = dbDiscs.get(uiDisc.getDiscussionId());
					dbDisc.setTopic(uiDisc.getTopic());
					dbDisc.setDiscussion(uiDisc.getDiscussion());
					dbDisc.setUpdatedBy("ADMIN");
					dbDisc.setUpdatedDt(LocalDate.now());
					finalDiscussions.add(dbDisc);
				}
			});
		} else {
			if (!CollectionUtils.isEmpty(reportDto.getDiscussions())) {
				finalDiscussions.addAll(reportDto.getDiscussions());
			} else {
				finalDiscussions.addAll(reportDb.getDiscussions());
			}
			for (ContactReportDiscussion disc : finalDiscussions) {
				disc.setIsActive(IsActiveEnum.NO.getValue());
			}
		}
		reportDb.setDiscussions(finalDiscussions);
	}

	private void processAttachments(ContactReportInfoDto reportDto, ContactReportInfo reportDb) {
		final List<ContactReportAttachment> finalAtts = new ArrayList<>();
		if ((!CollectionUtils.isEmpty(reportDto.getAttachment()))
				&& (!CollectionUtils.isEmpty(reportDb.getAttachment()))) {
			Map<Long, ContactReportAttachment> dbAtts = new HashMap<>();
			Map<Long, ContactReportAttachment> uiAtts = new HashMap<>();
			reportDb.getAttachment().forEach(att -> {
				if (att.getIsActive().equalsIgnoreCase(IsActiveEnum.YES.getValue()))
					dbAtts.put(att.getAttachmentId(), att);
				else
					finalAtts.add(att);
			});
			reportDto.getAttachment().forEach(att -> {
				if (att.getAttachmentId() == 0)
					finalAtts.add(att);
				else
					uiAtts.put(att.getAttachmentId(), att);
			});
			for (ContactReportAttachment newAtt : uiAtts.values()) {
				if (dbAtts.containsKey(newAtt.getAttachmentId())) {
					ContactReportAttachment exAtt = dbAtts.get(newAtt.getAttachmentId());
					ContactReportAttachment fAtt = null;
					if ((exAtt.getStatus() & AppConstants.StatusDBUpdated) == AppConstants.StatusDBUpdated)
						fAtt = exAtt;
					else
						fAtt = newAtt;
					if ((fAtt.getStatus() & AppConstants.StatusDBUpdated) == AppConstants.StatusDBUpdated)
						fAtt.setStatus(fAtt.getStatus() ^ AppConstants.StatusDBUpdated);
					finalAtts.add(fAtt);
				} else {
					finalAtts.add(newAtt);
				}
			}
			// DELETED Attachments
			for (ContactReportAttachment existingAtt : dbAtts.values()) {
				if (!uiAtts.containsKey(existingAtt.getAttachmentId())) {
					existingAtt.setIsActive(IsActiveEnum.NO.getValue());
					finalAtts.add(existingAtt);
				}
			}
		} else {
			if (!CollectionUtils.isEmpty(reportDto.getAttachment())) {
				finalAtts.addAll(reportDto.getAttachment());
			} else {
				List<ContactReportAttachment> existingAttachments = reportDb.getAttachment();
				if (!CollectionUtils.isEmpty(existingAttachments)) {
					existingAttachments.forEach(att -> att.setIsActive(IsActiveEnum.NO.getValue()));
					finalAtts.addAll(existingAttachments);
				}
			}

		}
		reportDb.setAttachment(finalAtts);
	}

	private void addRemoveDealerPersonnel(ContactReportInfoDto report, ContactReportInfo reportInfo) {
		if (!CollectionUtils.isEmpty(report.getDealerPersonnels())
				&& !CollectionUtils.isEmpty(reportInfo.getDealerPersonnels())) {
			// Deletions
			List<Integer> removedList = new ArrayList<>();
			List<ContactReportDealerPersonnel> newPers = report.getDealerPersonnels();
			List<ContactReportDealerPersonnel> currentPers = reportInfo.getDealerPersonnels();
			for (int i = 0; i < currentPers.size(); i++) {
				ContactReportDealerPersonnel existingDp = currentPers.get(i);
				boolean found = false;
				for (ContactReportDealerPersonnel newDp : newPers) {
					if (newDp.getPersonnelIdCd().equalsIgnoreCase(existingDp.getPersonnelIdCd())) {
						found = true;
						break;
					}
				}
				if (!found) {
					removedList.add(i);
				}
			}
			// Remove the persons from Current, if any
			for (int i = removedList.size() - 1; i >= 0; i--) {
				// currentPers.remove(removedList.get(i).intValue());
				currentPers.get(removedList.get(i)).setIsActive("N");
			}
			// Additions
			List<ContactReportDealerPersonnel> newList = new ArrayList<>();
			for (ContactReportDealerPersonnel newDp : newPers) {
				boolean found = false;
				for (ContactReportDealerPersonnel existingDp : currentPers) {
					if (newDp.getPersonnelIdCd().equalsIgnoreCase(existingDp.getPersonnelIdCd())) {
						found = true;
						break;
					}
				}
				if (!found) {
					newList.add(newDp);
				}
			}
			if (!newList.isEmpty()) {
				for (ContactReportDealerPersonnel contactReportDealerPersonnel : newList) {
					contactReportDealerPersonnel.setIsActive("Y");
				}
				currentPers.addAll(newList);
			}
		} else
		//
		if (!CollectionUtils.isEmpty(report.getDealerPersonnels())) {
			List<ContactReportDealerPersonnel> newList = report.getDealerPersonnels();
			for (ContactReportDealerPersonnel contactReportDealerPersonnel : newList) {
				contactReportDealerPersonnel.setIsActive("Y");
			}
//                List<ContactReportDealerPersonnel> contactReportDealerPersonnels = contactReportDealerPersonnelRepository.saveAll(newList);
			reportInfo.setDealerPersonnels(newList);
		}
	}

	/**
	 * @param attachment
	 */
	private void duplicateAttachmentChecker(List<ContactReportAttachment> attachment) {
		if (!CollectionUtils.isEmpty(attachment)) {
			HashSet<String> attFiles = new HashSet<>();
			attachment.forEach(att -> {
				if (attFiles.contains(att.getAttachmentName())) {
					throw new IllegalArgumentException("Duplicate Attachment: " + att.getAttachmentName());
				} else {
					attFiles.add(att.getAttachmentName());
				}
			});

		}
	}

	@Override
	public ContactReportDto findByContactReportId(long contactReportId) {
		ContactReportDto contactReportDto = new ContactReportDto();
		ContactReportInfo crInfo = contactInfoRepository.findByContactReportIdAndIsActive(contactReportId,
				IsActiveEnum.YES.getValue());
		List<ContactReportDealerPersonnel> contactReportDealerPersonnels = crInfo.getDealerPersonnels().stream()
				.filter(dp -> IsActiveEnum.YES.getValue().equalsIgnoreCase(dp.getIsActive()))
				.collect(Collectors.toList());
		List<String> dealerPersonnel = new ArrayList<>(0);
		if (new NullCheck<>(crInfo).with(ContactReportInfo::getAddDealerPersonnel).isNotNull()) {
			dealerPersonnel = Arrays.asList(crInfo.getAddDealerPersonnel().split("\\|"));
		}

		List<ContactReportDealerPersonnel> dealerList = dealerPersonnel.stream().map(dp -> {
			ContactReportDealerPersonnel reportDealerPersonnel = new ContactReportDealerPersonnel();
			reportDealerPersonnel.setPersonnelId(-9999999L);
			reportDealerPersonnel.setPersonnelIdCd(dp);
			return reportDealerPersonnel;
		}).collect(Collectors.toList());
		contactReportDealerPersonnels.addAll(dealerList);
		crInfo.setDealerPersonnels(contactReportDealerPersonnels);
		crInfo.setAttachment(crInfo.getAttachment().stream()
				.filter(cr -> cr.getIsActive().equalsIgnoreCase(IsActiveEnum.YES.getValue()))
				.collect(Collectors.toList()));
		contactReportDto.setContactReport(crInfo);
		return contactReportDto;
	}

	public List<ContactReportInfo> findByDlrCd(String dlrCd) {
		return contactInfoRepository.findByDlrCdAndIsActiveAndContactDtBetween(dlrCd, IsActiveEnum.YES.getValue(), AppConstants.MIN_DB_DATE, AppConstants.MAX_DB_DATE);

	}

	@Override
	public Map<String, List<ContactReportInfoDto>> getMyContactReport(MFPUser mfpUser, boolean showUsersDraft) {
		String userId = mfpUser.getUserid();
		String empCd = mfpUser.getEmployeeNumber();
		Predicate<ContactReportInfo> isSubmitted = cr -> cr.getContactStatus() == ContactReportEnum.SUBMITTED
				.getStatusCode();
		Predicate<ContactReportInfo> isDiscussion = cr -> cr
				.getContactStatus() == ContactReportEnum.DISCUSSION_REQUESTED.getStatusCode();
		Predicate<ContactReportInfo> isReviewed = cr -> cr.getContactStatus() == ContactReportEnum.REVIEWED
				.getStatusCode();
		List<ContactReportInfo> contactReportInfos = contactInfoRepository.findByContactAuthorAndIsActiveAndContactDtBetween(userId,
				IsActiveEnum.YES.getValue(), AppConstants.MIN_DB_DATE, AppConstants.MAX_DB_DATE);
		List<ContactReportInfo> revCntactReportInfos = contactInfoRepository
				.findByContactReviewerAndContactAuthorNotAndIsActiveAndContactDtBetween(empCd, userId, IsActiveEnum.YES.getValue(), AppConstants.MIN_DB_DATE, AppConstants.MAX_DB_DATE);
		revCntactReportInfos = revCntactReportInfos.stream().filter(isSubmitted.or(isDiscussion).or(isReviewed))
				.collect(Collectors.toList());
		contactReportInfos.addAll(revCntactReportInfos);
		Map<String, List<ContactReportInfoDto>> contactReportDtoMaps = new HashMap<>();
		final List<ContactReportInfo> ownDrafts = new ArrayList<>(0);
		if (showUsersDraft) {
			contactReportInfos.stream()
					.sorted((ContactReportInfo cr1, ContactReportInfo cr2) -> compareByUpdatedDate(cr1, cr2))
					.filter(val -> val.getCreatedBy().trim().equalsIgnoreCase(userId)
							&& val.getContactStatus() == ContactReportEnum.DRAFT.getStatusCode())
					.forEach(ownDrafts::add);
		}

		Map<String, List<ContactReportInfoDto>> contactReportDtos = contactReportInfos.stream()
				.sorted((ContactReportInfo cr1, ContactReportInfo cr2) -> compareByUpdatedDate(cr1, cr2))
				.map(reportInfo -> ContactReportInfoDto.builder().contactReportId(reportInfo.getContactReportId())
						.contactDt(reportInfo.getContactDt()).dealers(reportInfo.getDealers())
						.contactStatus(reportInfo.getContactStatus())
						.updatedDt(reportInfo.getUpdatedDt() != null ? reportInfo.getUpdatedDt()
								: reportInfo.getCreatedDt())
						.contactAuthor(reportInfo.getContactAuthor()).contactReviewer(reportInfo.getContactReviewer())
						.build())
				.collect(Collectors.groupingBy(
						element -> ContactReportEnum.valueByStatus(element.getContactStatus()).getDisplayText()));
		contactReportDtos.forEach((key, value) -> {

			if (showUsersDraft && key.equalsIgnoreCase(ContactReportEnum.DRAFT.getDisplayText())) {
				List<ContactReportInfoDto> drafts = ownDrafts.stream()
						.map(reportInfo -> ContactReportInfoDto.builder()
								.contactReportId(reportInfo.getContactReportId()).contactDt(reportInfo.getContactDt())
								.dealers(reportInfo.getDealers()).contactStatus(reportInfo.getContactStatus())
								.updatedDt(reportInfo.getUpdatedDt() != null ? reportInfo.getUpdatedDt()
										: reportInfo.getCreatedDt())
								.contactAuthor(reportInfo.getContactAuthor())
								.contactReviewer(reportInfo.getContactReviewer()).build())
						.collect(Collectors.toList());
				contactReportDtoMaps.put(key, drafts);

			} else {
				contactReportDtoMaps.put(key, value);
			}

		});
		return contactReportDtoMaps;

	}

	@Override
	public String deleteReportById(long contactReportId, MFPUser mfpUser) throws Exception {
		final int contactStatus = ContactReportEnum.DRAFT.getStatusCode(); // contactStatus 0 makes sure that the report
		String submission = "We have ran into some issues, please try again!";
		ContactReportInfo reportInfo = contactInfoRepository.getById(contactReportId);
		if (!mfpUser.getUserid().trim().equalsIgnoreCase(reportInfo.getContactAuthor())) {
			ListPersonnel lemp = allEmployeesCache.getByWSLCd(mfpUser.getUserid());
			if (!lemp.isCorporatePerson()) {
				if (!mfpUser.getEmployeeNumber().trim().equalsIgnoreCase(reportInfo.getContactReviewer().trim())) {
					throw new Exception("You are not authorized to modify this report.");
				}
			}
		}
		if (reportInfo.getContactStatus() == ContactReportEnum.DRAFT.getStatusCode()
				|| reportInfo.getContactStatus() == ContactReportEnum.SUBMITTED.getStatusCode()
				|| reportInfo.getContactStatus() == ContactReportEnum.DISCUSSION_REQUESTED.getStatusCode()) {
			reportInfo.setContactStatus(ContactReportEnum.CANCELLED.getStatusCode());
			List<ContactReportDealerPersonnel> existingDealerPersonel = reportInfo.getDealerPersonnels();
			existingDealerPersonel.forEach(val -> val.setIsActive(IsActiveEnum.NO.getValue()));
			contactReportDealerPersonnelRepository.saveAll(existingDealerPersonel);
			reportInfo.setIsActive(IsActiveEnum.NO.getValue());
			contactInfoRepository.save(reportInfo);
			submission = "Report removed successfully";

		}
		return submission;
	}

	@Override
	public List<ContactReportTopicDto> fetchSalesServiceOthersBasedOnTypes(List<String> contactTypeList) {
		List<ContactReportTopicDto> topicList = new ArrayList<>();
		contactTypeList.forEach(val -> {
			if ("sales".equalsIgnoreCase(val)) {
				List<String> salesList = AppConstants.SALES_TOPIC_LIST;
				Collections.sort(salesList);
				topicList.add(ContactReportTopicDto.builder().groupName("Sales").topics(salesList).build());
			}
			if ("service".equalsIgnoreCase(val)) {
				List<String> serviceList = AppConstants.SERVICE_TOPIC_LIST;
				Collections.sort(serviceList);
				topicList.add(ContactReportTopicDto.builder().groupName("After Sales").topics(serviceList).build());
			}
			if ("other".equalsIgnoreCase(val)) {
				List<String> otherList = AppConstants.OTHER_TOPIC_LIST;
				Collections.sort(otherList);
				topicList.add(ContactReportTopicDto.builder().groupName("Network").topics(otherList).build());
			}
		});
		return topicList;

	}

	private static int compareByUpdatedDate(ContactReportInfo cr, ContactReportInfo cr2) {
		int rv = new NullCheck<>(cr2).with(ContactReportInfo::getContactDt).orElse(cr2.getCreatedDt())
				.compareTo(new NullCheck<>(cr).with(ContactReportInfo::getContactDt).orElse(cr.getCreatedDt()));
		if (rv == 0) {
			rv = new NullCheck<>(cr2).with(ContactReportInfo::getDlrCd).orElse("00000")
					.compareTo(new NullCheck<>(cr).with(ContactReportInfo::getDlrCd).orElse("00000"));
		}
		return rv;
	}

}
