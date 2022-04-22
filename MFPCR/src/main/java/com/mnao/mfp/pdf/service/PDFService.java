package com.mnao.mfp.pdf.service;

import java.nio.file.Path;
import java.util.List;

import javax.validation.Valid;

import org.springframework.core.io.Resource;

import com.mnao.mfp.common.dao.DealerInfo;
import com.mnao.mfp.cr.entity.ContactReportDealerPersonnel;
import com.mnao.mfp.cr.entity.ContactReportInfo;
import com.mnao.mfp.cr.entity.Dealers;
import com.mnao.mfp.pdf.dao.DealerEmployeeInfo;
import com.mnao.mfp.pdf.dao.ReviewerEmployeeInfo;
import com.mnao.mfp.user.dao.MFPUser;

public interface PDFService {

	ReviewerEmployeeInfo getReviewerEmployeeInfos(MFPUser mfpUser, String contactReviewer, DealerInfo dInfo);

	List<DealerEmployeeInfo> getDealerEmployeeInfos(MFPUser mfpUser, String dlrCd,
			List<ContactReportDealerPersonnel> dPers);

	Resource createXLSFResource(MFPUser mfpUser, List<ContactReportInfo> contactReports) throws Exception;

	Resource createPDFResource(MFPUser mfpUser, @Valid ContactReportInfo report);

	Path getTmpFilePath(MFPUser mfpUser, String string, String string2, String string3);

	ReviewerEmployeeInfo getReviewerEmployeeInfos(MFPUser mfpUser, String contactReviewer, Dealers dInfo);

}