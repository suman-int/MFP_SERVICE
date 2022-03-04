package com.mnao.mfp.cr.dto;



import com.mnao.mfp.cr.entity.ContactReportInfo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class ContactReportDto {

	private ContactReportInfo contactReport;
}
