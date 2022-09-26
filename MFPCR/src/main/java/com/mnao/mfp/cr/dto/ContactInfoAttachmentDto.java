package com.mnao.mfp.cr.dto;

import com.mnao.mfp.cr.entity.ContactReportAttachment;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class ContactInfoAttachmentDto {
    private ContactReportAttachment reportAttachment;
    private boolean status;
    private String message;
}
