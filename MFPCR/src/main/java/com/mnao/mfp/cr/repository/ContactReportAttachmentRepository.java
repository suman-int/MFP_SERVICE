package com.mnao.mfp.cr.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.mnao.mfp.cr.entity.ContactReportAttachment;
import com.mnao.mfp.cr.entity.ContactReportInfo;

public interface ContactReportAttachmentRepository extends JpaRepository<ContactReportAttachment, Long> {

    public ContactReportAttachment findByAttachmentIdAndIsActive(@Param("attachmentId") long attachmentId, String isActive);
    public ContactReportAttachment findByAttachmentPath(@Param("attachmentPath") String attachmentPath);
//    public ContactReportAttachment findByAttachmentNameAndContactReportAndStatus(@Param("attachmentName") String attachmentName,@Param("contactReport") ContactReportInfo contactReport);
}
