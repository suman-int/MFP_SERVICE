package com.mnao.mfp.cr.service.impl;

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
import com.mnao.mfp.user.dao.MFPUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.transaction.Transactional;
import java.time.LocalDate;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Service
public class ContactReportServiceImpl implements ContactReportService {

    @Autowired
    private ContactInfoRepository contactInfoRepository;

    @Autowired
    private FileHandlingServiceImpl fileHandlingService;

    @Autowired
    private ContactReportDealerPersonnelRepository contactReportDealerPersonnelRepository;

    @Autowired
    private EmailService emailService;

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
    public String submitReportDataV2(ContactReportInfoDto report, MFPUser mfpUser, String currURL) throws Exception {
        String submission = "Unable to save contact report";
        if (report == null)
            throw new AssertionError();
        int origCRStatus = -1;
        try {
            ContactReportInfo reportInfo = new ContactReportInfo();
            boolean isDealerUpdated = false;
            if (report.getContactReportId() > 0) {
                reportInfo = contactInfoRepository.getById(report.getContactReportId());
                origCRStatus = reportInfo.getContactStatus();
                if (reportInfo.getContactStatus() == ContactReportEnum.DRAFT.getStatusCode()
                        && report.getContactStatus() == ContactReportEnum.CANCELLED.getStatusCode()) {
                    reportInfo.setContactStatus(report.getContactStatus());
                    List<ContactReportDealerPersonnel> existingDealerPersonel = reportInfo.getDealerPersonnels();
                    existingDealerPersonel.forEach(val -> val.setIsActive(IsActiveEnum.NO.getValue()));
                    contactReportDealerPersonnelRepository.saveAll(existingDealerPersonel);
                    reportInfo.setIsActive(IsActiveEnum.NO.getValue());
                    contactInfoRepository.save(reportInfo);
                    submission = "Report removed successfully";
                    return submission;
                } else if (report.getDealers() != null) {
                    isDealerUpdated = !reportInfo.getDealers().getDlrCd().trim()
                            .equalsIgnoreCase(report.getDealers().getDlrCd().trim());
                }
            }
            if (new NullCheck<>(report).with(ContactReportInfoDto::getContactStatus).get() == ContactReportEnum.CANCELLED.getStatusCode()) {
                reportInfo.setContactStatus(report.getContactStatus());
            } else {
                if (isDealerUpdated && report.getContactReportId() > 0) {
                    reportInfo.setDealers(null);
                }
                // Update Author only if in DRAFT
                if (report.getContactStatus() == ContactReportEnum.DRAFT.getStatusCode()
                        || report.getContactStatus() == ContactReportEnum.SUBMITTED.getStatusCode()) {
                    reportInfo.setContactAuthor(new NullCheck<>(mfpUser).with(MFPUser::getUserid)
                            .orElse(reportInfo.getContactAuthor()));
                }
                reportInfo.setContactDt(
                        report.getContactDt() != null ? report.getContactDt() : reportInfo.getContactDt());
                reportInfo.setContactLocation(report.getContactLocation() != null ? report.getContactLocation()
                        : reportInfo.getContactLocation());
                reportInfo.setContactReviewer(report.getContactReviewer() != null ? report.getContactReviewer()
                        : reportInfo.getContactReviewer());

                reportInfo.setContactStatus(
                        report.getContactStatus() != null ? report.getContactStatus() : reportInfo.getContactStatus());
                reportInfo.setContactType(
                        report.getContactType() != null ? report.getContactType() : reportInfo.getContactType());
                String reps = report.getCorporateReps();
                reportInfo.setCorporateReps(reps != null ? (reps) : reportInfo.getCorporateReps());
                // Sandip: Does discussion changes and deletes need to be handled?
                if (!CollectionUtils.isEmpty(report.getDiscussions())) {
                    reportInfo.setDiscussions(report.getDiscussions());
                    reportInfo.setCurrentIssues(report.getDiscussions().stream().map(ContactReportDiscussion::getTopic)
                            .filter(Objects::nonNull).collect(Collectors.joining("|")));
                }
                reportInfo.setDlrCd(report.getDlrCd() != null ? report.getDlrCd() : reportInfo.getDlrCd());

                reportInfo.setDealers(report.getDealers() != null ? report.getDealers() : reportInfo.getDealers());
            }

            // Submit and Reviewed Date
            if (reportInfo.getContactStatus() != origCRStatus) {
                if (reportInfo.getContactStatus() == ContactReportEnum.SUBMITTED.getStatusCode()) {
                    reportInfo.setSubmittedDt(LocalDate.now());
                } else if (reportInfo.getContactStatus() == ContactReportEnum.REVIEWED.getStatusCode()) {
                    reportInfo.setReviewedDt(LocalDate.now());
                    reportInfo.setReviewedBy(mfpUser.getUserid());
                }
            }
         // Additional Dealership personnel
            String addPersonnel = report.getDealerPersonnels().stream().filter(dp -> dp.getPersonnelId() == -999L).map(ContactReportDealerPersonnel::getPersonnelIdCd).collect(Collectors.joining("|"));
            if (!addPersonnel.isEmpty()) {
                reportInfo.setAddDealerPersonnel(addPersonnel);
            } else {
            	reportInfo.setAddDealerPersonnel(null);
            }
            // Addition and Deletion of Dealer Personnel
            report.setDealerPersonnels(report.getDealerPersonnels().stream().filter(dp -> dp.getPersonnelId() != -999L).collect(Collectors.toList()));
            addRemoveDealerPersonnel(report, reportInfo);
            duplicateAttachmentChecker(report.getAttachment());
            
            // Need to check for Additions and Deletions here as well
            if (!CollectionUtils.isEmpty(report.getAttachment())) {
                reportInfo.setAttachment(report.getAttachment());
            }
            //
            ContactReportInfo info = contactInfoRepository.save(reportInfo);
            if (report.getContactStatus() == ContactReportEnum.SUBMITTED.getStatusCode()) {
                fileHandlingService.copyToPermanentLocation(info);
            }

            submission = "Saved Success";
            if (info.getContactStatus() == ContactReportEnum.SUBMITTED.getStatusCode()
                    || info.getContactStatus() == ContactReportEnum.REVIEWED.getStatusCode()
                    || info.getContactStatus() == ContactReportEnum.DISCUSSION_REQUESTED.getStatusCode()) {
                emailService.sendEmailNotification(reportInfo, origCRStatus, mfpUser);
            }
        } catch (Exception e) {
            e.printStackTrace();
            submission = "Failed to save Contact Report. " + e.getMessage();
            throw new Exception(submission);
        }
        return submission;
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

    public ContactReportDto findByContactReportId(long contactReportId) {
        ContactReportDto contactReportDto = new ContactReportDto();
        ContactReportInfo crInfo = contactInfoRepository.findByContactReportIdAndIsActive(contactReportId, IsActiveEnum.YES.getValue());
        List<ContactReportDealerPersonnel> contactReportDealerPersonnels = crInfo.getDealerPersonnels().stream()
                .filter(dp -> IsActiveEnum.YES.getValue().equalsIgnoreCase(dp.getIsActive()))
                .collect(Collectors.toList());
        List<String> dealerPersonnel = new ArrayList<>(0);
        if (new NullCheck<>(crInfo).with(ContactReportInfo::getAddDealerPersonnel).isNotNull()) {
        	dealerPersonnel = Arrays.asList(crInfo.getAddDealerPersonnel().split("\\|"));
        }
         
        List<ContactReportDealerPersonnel> dealerList = dealerPersonnel.stream().map(dp -> {
            ContactReportDealerPersonnel reportDealerPersonnel = new ContactReportDealerPersonnel();
            reportDealerPersonnel.setPersonnelId(-999L);
            reportDealerPersonnel.setPersonnelIdCd(dp);
            return reportDealerPersonnel;
        }).collect(Collectors.toList());
        contactReportDealerPersonnels.addAll(dealerList);
        crInfo.setDealerPersonnels(contactReportDealerPersonnels);
        contactReportDto.setContactReport(crInfo);
        return contactReportDto;
    }

    public List<ContactReportInfo> findByDlrCd(String dlrCd) {
        return contactInfoRepository.findByDlrCdAndIsActive(dlrCd, IsActiveEnum.YES.getValue());

    }

    public Map<String, List<ContactReportInfoDto>> getMyContactReport(MFPUser mfpUser, boolean showUsersDraft) {
        String userId = mfpUser.getUserid();
        String empCd = mfpUser.getEmployeeNumber();
        Predicate<ContactReportInfo> isSubmitted = cr -> cr.getContactStatus() == ContactReportEnum.SUBMITTED
                .getStatusCode();
        Predicate<ContactReportInfo> isDiscussion = cr -> cr
                .getContactStatus() == ContactReportEnum.DISCUSSION_REQUESTED.getStatusCode();
        List<ContactReportInfo> contactReportInfos = contactInfoRepository.findByContactAuthorAndIsActive(userId, IsActiveEnum.YES.getValue());
        List<ContactReportInfo> revCntactReportInfos = contactInfoRepository.findByContactReviewerAndContactAuthorNotAndIsActive(empCd, userId, IsActiveEnum.YES.getValue());
        revCntactReportInfos = revCntactReportInfos.stream()
        		.filter(isSubmitted.or(isDiscussion))
        		.collect(Collectors.toList());
        contactReportInfos.addAll(revCntactReportInfos);
        Map<String, List<ContactReportInfoDto>> contactReportDtoMaps = new HashMap<>();
        final List<ContactReportInfo> ownDrafts = new ArrayList<>(0);
        if (showUsersDraft) {
            contactReportInfos.stream()
                    .filter(val -> val.getCreatedBy().trim().equalsIgnoreCase(userId)
                            && val.getContactStatus() == ContactReportEnum.DRAFT.getStatusCode())
                    .forEach(ownDrafts::add);
        }

        Map<String, List<ContactReportInfoDto>> contactReportDtos = contactReportInfos.stream()
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

    @Transactional
    public void deleteReportById(long contactReportId) {
        final int contactStatus = ContactReportEnum.DRAFT.getStatusCode(); // contactStatus 0 makes sure that the report
        // is still a draft
        contactInfoRepository.deleteByContactReportIdAndContactStatusAndIsActive(contactReportId, contactStatus,
                IsActiveEnum.YES.getValue());
    }

    @Override
    public List<ContactReportTopicDto> fetchSalesServiceOthersBasedOnTypes(List<String> contactTypeList) {
        List<ContactReportTopicDto> topicList = new ArrayList<>();
        contactTypeList.forEach(val -> {
            if ("sales".equalsIgnoreCase(val)) {
                topicList.add(ContactReportTopicDto.builder().groupName("Sales").topics(AppConstants.SALES_TOPIC_LIST)
                        .build());
            }
            if ("service".equalsIgnoreCase(val)) {
                topicList.add(ContactReportTopicDto.builder().groupName("After Sales")
                        .topics(AppConstants.SERVICE_TOPIC_LIST).build());
            }
            if ("other".equalsIgnoreCase(val)) {
                topicList.add(ContactReportTopicDto.builder().groupName("Network").topics(AppConstants.OTHER_TOPIC_LIST)
                        .build());
            }
        });
        return topicList;

    }

}
