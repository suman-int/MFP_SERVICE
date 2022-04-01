package com.mnao.mfp.pdf.dao;

import com.mnao.mfp.cr.entity.ContactReportInfo;
import com.mnao.mfp.cr.entity.Dealers;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@AllArgsConstructor
@Builder
@Getter
@Setter
@ToString
@NoArgsConstructor
public class DealerAndContactLocationInfo {
	private String regionName;
	private String zoneName;
	private String districtName;
	private Dealers dealer;
	private ContactReportInfo contactReportInfo;
}
