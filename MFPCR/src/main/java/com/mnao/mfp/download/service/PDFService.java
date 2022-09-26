package com.mnao.mfp.download.service;

import java.nio.file.Path;
import java.util.List;

import javax.validation.Valid;

import org.springframework.core.io.Resource;

import com.mnao.mfp.common.dao.DealerInfo;
import com.mnao.mfp.cr.entity.ContactReportDealerPersonnel;
import com.mnao.mfp.cr.entity.ContactReportInfo;
import com.mnao.mfp.cr.entity.Dealers;
import com.mnao.mfp.download.dao.DealerEmployeeInfo;
import com.mnao.mfp.download.dao.ReviewerEmployeeInfo;
import com.mnao.mfp.user.dao.MFPUser;

public interface PDFService {

	ReviewerEmployeeInfo getReviewerEmployeeInfos(MFPUser mfpUser, String contactReviewer, DealerInfo dInfo);

	List<DealerEmployeeInfo> getDealerEmployeeInfos(MFPUser mfpUser, String dlrCd,
			List<ContactReportDealerPersonnel> dPers);

	Resource createXLSXResource(MFPUser mfpUser, List<ContactReportInfo> contactReports) throws Exception;

	Resource createPDFResource(MFPUser mfpUser, @Valid ContactReportInfo report);

	Path createXLSXFile(MFPUser mfpUser, List<ContactReportInfo> contactReports) throws Exception;

	Path createPDFFile(MFPUser mfpUser, @Valid ContactReportInfo report);

	Path getTmpFilePath(MFPUser mfpUser, String string, String string2, String string3);

	ReviewerEmployeeInfo getReviewerEmployeeInfos(MFPUser mfpUser, String contactReviewer, Dealers dInfo);
	
	MFPUser getUDSUser(String wslid);

}