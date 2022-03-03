package com.mnao.mfp.cr.Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mnao.mfp.cr.Mapper.ContactInfoMapper;
import com.mnao.mfp.cr.dto.ContactReportDto;
import com.mnao.mfp.cr.dto.ReportByDealershipDto;
import com.mnao.mfp.cr.entity.ContactReportInfo;
import com.mnao.mfp.cr.repository.ContactInfoRepository;

import javax.transaction.Transactional;
import java.util.List;

@Service
public class ContactReportServiceImpl implements ContactReportService{

    @Autowired
    private ContactInfoRepository contactInfoRepository;

    
    public String submitReportData(ContactReportDto report) {
        String submission = "Unable to save contact report";
        try {
            if(report.getContactReport().getMetrics().size() > 0 &&
                    report.getContactReport().getDealerpersonnel().size() > 0) {
                contactInfoRepository.save(report.getContactReport());
                submission = "Saved Success";
            }
        } catch (Exception e) {
        	e.printStackTrace();
            submission = "Failed - Metrics | DealerPersonnel is missing";
        }
        return submission;
    }

    public ContactReportDto findByContactReportId(long ContactreporId) {
        ContactReportDto contactReportDto = new ContactReportDto();
        contactReportDto.setContactReport(contactInfoRepository.findByContactReportId(ContactreporId));
        return contactReportDto;
    }

    public List<ReportByDealershipDto> findByDlrCd(String dlrCd) {
        List<ContactReportInfo> DtoList = contactInfoRepository.findByDlrCd(dlrCd);
        return ContactInfoMapper.INSTANCE.CRInfoToCIDtoList(DtoList);
    }
	public Map<String, List<ContactReportInfo>> getMyContactReport(String userId, BiFunction<List<ContactReportInfo>, Integer, List<ContactReportInfo>> contactReportByStatus) {
        List<ContactReportInfo> contactReportInfos = contactInfoRepository.findByContactAuthor(userId);
        Map<String, List<ContactReportInfo>> statusMap = new HashMap<>();
        statusMap.put(ContactReportEnum.COMPLETED.getDisplayText(), contactReportByStatus.apply(contactReportInfos, ContactReportEnum.COMPLETED.getStatusCode()));
        statusMap.put(ContactReportEnum.REVIEW_REQUESTED.getDisplayText(), contactReportByStatus.apply(contactReportInfos, ContactReportEnum.REVIEW_REQUESTED.getStatusCode()));
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
