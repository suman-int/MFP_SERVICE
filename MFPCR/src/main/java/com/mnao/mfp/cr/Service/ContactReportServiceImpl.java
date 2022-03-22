package com.mnao.mfp.cr.Service;

import com.mnao.mfp.cr.model.DealersByIssue;
import com.mnao.mfp.cr.util.ContactReportEnum;
import com.mnao.mfp.user.dao.MFPUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

//import com.mnao.mfp.cr.Mapper.ContactInfoMapper;
import com.mnao.mfp.cr.dto.ContactReportDto;
import com.mnao.mfp.cr.dto.ReportByDealershipDto;
import com.mnao.mfp.cr.entity.ContactReportAttachment;
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
public class ContactReportServiceImpl implements ContactReportService{

    @Autowired
    private ContactInfoRepository contactInfoRepository;
    
    @Autowired
    private FileHandlingServiceImpl fileHandlingService;

    public List<DealersByIssue> getAllDealersByIssue(){
        return contactInfoRepository.findAll().stream().map(contactReportInfo -> {
            DealersByIssue dealersByIssue = new DealersByIssue();
            dealersByIssue.setIssue(contactReportInfo.getCurrentIssues());
            dealersByIssue.setDealership(contactReportInfo.getDlrCd());
            dealersByIssue.setLocation(contactReportInfo.getContactLocation());
            return dealersByIssue;
        }).collect(Collectors.toList());
    }
    
    public String submitReportData(ContactReportInfo report, MFPUser mfpUser) throws Exception  {
        String submission = "Unable to save contact report";
        try {

            //updating the auditing column when draft
            if(report.getContactStatus() == 0){
                report.setUpdatedDt(LocalDate.now());
                report.setCreatedDt(LocalDate.now());
                report.setUpdatedBy(mfpUser.getUserid());
                report.setCreatedBy(mfpUser.getUserid());
            }
            else{
                report.setUpdatedDt(LocalDate.now());
                report.setUpdatedBy(mfpUser.getUserid());
            }

            if(Objects.nonNull(report.getDealerPersonnels()) && report.getDealerPersonnels().size() > 0) {
                String reps = report.getCorporateReps();
                if(reps.length() > 250){
                    report.setCorporateReps(reps.substring(0, 250));
                }
                HashSet<String> attFiles =new HashSet<String>();
                for( int i =0;i<report.getAttachment().size();i++) {
                	ContactReportAttachment  att=report.getAttachment().get(i);
                	if(attFiles.contains(att.getAttachmentName())) {
                		throw new IllegalArgumentException("Duplicate Attachment: "+ att.getAttachmentName());
                	}
                	else {
                		attFiles.add(att.getAttachmentName());
                	
                	}
                }
                if (!report.getDiscussions().isEmpty()) {
                	 report.setCurrentIssues(report.getDiscussions().stream().map(value -> value.getTopic()).collect(Collectors.joining("|")));
                }
               
                ContactReportInfo info= contactInfoRepository.save(report);
                if(report.getContactStatus() == 1)
                    fileHandlingService.copyToPermanentLocation(info);
                submission = "Saved Success";
            }else {
                throw new IllegalArgumentException("Required Dealer personnel");
            }
        } catch (Exception e) {
        	e.printStackTrace();
            submission = "Failed - Metrics | DealerPersonnel is missing";
            throw new Exception (submission);
        }
        return submission;
    }

    public ContactReportDto findByContactReportId(long ContactreporId) {
        ContactReportDto contactReportDto = new ContactReportDto();
        contactReportDto.setContactReport(contactInfoRepository.findByContactReportId(ContactreporId));
        return contactReportDto;
    }

    public List<ContactReportInfo> findByDlrCd(String dlrCd) {
        return contactInfoRepository.findByDlrCd(dlrCd);

    }
	public Map<String, List<ContactReportInfo>> getMyContactReport(String userId, BiFunction<List<ContactReportInfo>, Integer, List<ContactReportInfo>> contactReportByStatus) {
        List<ContactReportInfo> contactReportInfos = contactInfoRepository.findByContactAuthor(userId);
        Map<String, List<ContactReportInfo>> statusMap = new HashMap<>();
        statusMap.put(ContactReportEnum.COMPLETED.getDisplayText(), contactReportByStatus.apply(contactReportInfos, ContactReportEnum.COMPLETED.getStatusCode()));
        statusMap.put(ContactReportEnum.DISCUSSION_REQUESTED.getDisplayText(), contactReportByStatus.apply(contactReportInfos, ContactReportEnum.DISCUSSION_REQUESTED.getStatusCode()));
        statusMap.put(ContactReportEnum.REVIEWED.getDisplayText(), contactReportByStatus.apply(contactReportInfos, ContactReportEnum.REVIEWED.getStatusCode()));
        statusMap.put(ContactReportEnum.DRAFT.getDisplayText(), contactReportByStatus.apply(contactReportInfos, ContactReportEnum.DRAFT.getStatusCode()));
        statusMap.put(ContactReportEnum.SUBMITTED.getDisplayText(), contactReportByStatus.apply(contactReportInfos, ContactReportEnum.SUBMITTED.getStatusCode()));
        return statusMap;
    }
    @Transactional
    public void deleteReportById(long contactReportId){
        final int contactStatus = 0; // contactStatus 0 makes sure that the report is still a draft
        contactInfoRepository.deleteByContactReportIdAndContactStatus(contactReportId, contactStatus);
    }
}
