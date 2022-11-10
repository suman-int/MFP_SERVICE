package com.mnao.mfp.list.service;

import java.text.ParseException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mnao.mfp.common.controller.MfpKPIControllerBase;
import com.mnao.mfp.common.dao.DealerFilter;
import com.mnao.mfp.common.dao.DealerInfo;
import com.mnao.mfp.common.util.AppConstants;
import com.mnao.mfp.common.util.Utils;
import com.mnao.mfp.cr.dto.RegionZoneReviewer;
import com.mnao.mfp.cr.entity.ContactReportInfo;
import com.mnao.mfp.cr.repository.ContactInfoRepository;
import com.mnao.mfp.cr.util.ContactReportEnum;
import com.mnao.mfp.list.cache.AllActiveEmployeesCache;
import com.mnao.mfp.list.cache.AllDealersCache;
import com.mnao.mfp.list.cache.CheckDealerChanges;
import com.mnao.mfp.list.cache.CheckEmployeeChanges;
import com.mnao.mfp.list.dao.ListPersonnel;
import com.mnao.mfp.user.dao.MFPUser;

//
@Service
public class ListEmployeeDataService extends MfpKPIControllerBase {
	//
	private static final Logger log = LoggerFactory.getLogger(ListEmployeeDataService.class);
	//
	@Autowired
	AllActiveEmployeesCache allEmployeesCache;
	@Autowired
	AllDealersCache allDealersCache;
	@Autowired
	CheckEmployeeChanges checkEmployeeChanges;
	@Autowired
	CheckDealerChanges checkDealerChanges;
	@Autowired
	private ContactInfoRepository contactInfoRepository;

	public List<ListPersonnel> getListOfReviewers(String dlrCd, Long contactReportId, MFPUser mfpUser, String rgnCd,
			String zoneCd, String districtCd, String mdaCd) {
		List<ListPersonnel> retRows = null;
		MMAListService<ListPersonnel> service = new MMAListService<ListPersonnel>();
		boolean isCurrent = true;
		if (contactReportId != null && contactReportId < 0) {
			isCurrent = false;
			ContactReportInfo reportDb = contactInfoRepository.getById(contactReportId);
			String rev = reportDb.getContactReviewer();
			List<String> ids = Arrays.asList(rev);
			String sqlName = getKPIQueryFilePath(AppConstants.SQL_LIST_ALL_EMPLOYEES);
			retRows = service.getEmpDataAllEmployees(sqlName, ListPersonnel.class, "A.PRSN_ID_CD", ids);
		}
		if (isCurrent) {
			String sqlName = getKPIQueryFilePath(AppConstants.SQL_LIST_REVIEWER_EMPLOYEES);
			String rgnSqlName = getKPIQueryFilePath(AppConstants.SQL_LIST_REVIEWER_REGIONAL_EMPLOYEES);
			String reviewerJobCodesByAuthor = Utils.getAppProperty(AppConstants.CR_REVIEWER_JOB_CODES_BY_AUTHOR,
					"false");
			String reviewerJobCodes = Utils.getAppProperty(AppConstants.CR_REVIEWER_JOB_CODES);
			String[] jobCodes = reviewerJobCodes.split("[,]");
			DealerFilter df = new DealerFilter(null, dlrCd, rgnCd, zoneCd, districtCd, mdaCd);
			try {
				// DealerInfo dlrInfo = getDealerInfo(null, dlrCd);
				DealerInfo dlrInfo = allDealersCache.getDealerInfo(dlrCd);
				if (dlrInfo != null) {
					if (reviewerJobCodesByAuthor.equalsIgnoreCase("true")) {
						retRows = getReviewers(sqlName, rgnSqlName, mfpUser, service, df, dlrInfo);
					} else {
						retRows = getReviewers(sqlName, rgnSqlName, jobCodes, 0, service, df, dlrInfo);
					}
				}
			} catch (InstantiationException | IllegalAccessException | ParseException e) {
				log.error("ERROR retrieving list of Employees:", e);
			}
			checkEmployeeChanges.checkEmpChanges(retRows);
		}
		return retRows;
	}

	private List<ListPersonnel> getReviewers(String sqlName, String rgnSqlName, MFPUser mfpUser,
			MMAListService<ListPersonnel> service, DealerFilter df, DealerInfo dlrInfo)
			throws InstantiationException, IllegalAccessException, ParseException {
		String reviewerJobCodes = Utils.getAppProperty(AppConstants.CR_REVIEWER_JOB_CODES);
		String[] jobCodes = reviewerJobCodes.split("[,]");
		String revStart = "MZ11";
		List<ListPersonnel> retRows = null;
		if (mfpUser.getCorporatePerson() || mfpUser.getCorpPerson() || mfpUser.getLoctnCd().equalsIgnoreCase("MA92")
				|| " MF11 MG11 ".indexOf(mfpUser.getPrimJobCd().toUpperCase()) > 0) {
			revStart = "MG11";
		} else if ("MZ11".equalsIgnoreCase(mfpUser.getPrimJobCd())) {
			revStart = "MF11";
		} else {
			revStart = "MZ11";
		}
		for (int i = 0; i < jobCodes.length; i++) {
			if (revStart.equals(jobCodes[i])) {
				retRows = getReviewers(sqlName, rgnSqlName, jobCodes, i, service, df, dlrInfo);
				break;
			}
		}
		return retRows;
	}

	private List<ListPersonnel> getReviewers(String sqlName, String rgnSqlName, String[] jobCodes, int stIdx,
			MMAListService<ListPersonnel> service, DealerFilter df, DealerInfo dlrInfo)
			throws ParseException, InstantiationException, IllegalAccessException {
		List<ListPersonnel> retRows = null;
		for (int i = stIdx; i < jobCodes.length; i++) {
			if (i == 0) {
				retRows = service.getListData(sqlName, ListPersonnel.class, df, dlrInfo.getRgnCd(),
						dlrInfo.getZoneCd());
			} else {
				retRows = service.getListData(rgnSqlName, ListPersonnel.class, df, jobCodes[i], dlrInfo.getRgnCd());
			}
			if ((retRows != null) && retRows.size() > 0) {
				log.info("Found Reviewer(s) with JOB_CD = " + jobCodes[i]);
				break;
			}
		}
		return retRows;
	}

	public List<ListPersonnel> getListOfCorporateEmployeesFiltered(String rgnCd, String zoneCd, String districtCd,
			String mdaCd, String dlrCd, Long id, MFPUser mfpUser) {
		MMAListService<ListPersonnel> service = new MMAListService<ListPersonnel>();
		List<ListPersonnel> retRows = null;
		boolean isCurrent = true;
		if (id != null && id < 0) {
			isCurrent = false;
			ContactReportInfo reportDb = contactInfoRepository.getById(id);
			String corps = reportDb.getCorporateReps();
			List<String> ids = Arrays.asList(corps.split("[|]"));
			String sqlName = getKPIQueryFilePath(AppConstants.SQL_LIST_ALL_EMPLOYEES);
			retRows = service.getEmpDataAllEmployees(sqlName, ListPersonnel.class, "A.PRSN_ID_CD", ids);
		}
		if (isCurrent) {
			String sqlName = getKPIQueryFilePath(AppConstants.SQL_LIST_CORPORATE_EMPLOYEES);
			DealerFilter df = new DealerFilter(mfpUser, dlrCd, rgnCd, zoneCd, districtCd, mdaCd);
			try {
				// DealerInfo dlrInfo = getDealerInfo(null, dlrCd);
				DealerInfo dlrInfo = allDealersCache.getDealerInfo(dlrCd);
				if (dlrInfo != null) {
					retRows = service.getListData(sqlName, ListPersonnel.class, df, dlrInfo.getRgnCd(),
							dlrInfo.getZoneCd(), dlrInfo.getDistrictCd(), dlrInfo.getRgnCd());
				}
			} catch (InstantiationException | IllegalAccessException | ParseException e) {
				log.error("ERROR retrieving list of Employees:", e);
			}
			checkEmployeeChanges.checkEmpChanges(retRows);
		}
		return retRows;
	}

	public List<ListPersonnel> getListOfCorporateEmployeesAll(String rgnCd, String zoneCd, String districtCd,
			String mdaCd, String dlrCd, Long id, String contactDateStr, MFPUser mfpUser) {
		MMAListService<ListPersonnel> service = new MMAListService<ListPersonnel>();
		List<ListPersonnel> retRows = null;
		LocalDate crDate = LocalDate.now();
		boolean isCurrent = true;
		if ((id != null) && (id != 0)) {
			isCurrent = false;
			ContactReportInfo reportDb = contactInfoRepository.getById(id);
			if ((reportDb == null)) {
				isCurrent = true;
			} else if ((id > 0) && (reportDb.getContactStatus() != ContactReportEnum.REVIEWED.getStatusCode())) {
				crDate = reportDb.getContactDt();
				isCurrent = true;
			} else {
				String corps = reportDb.getCorporateReps();
				List<String> ids = Arrays.asList(corps.split("[|]"));
				String sqlName = getKPIQueryFilePath(AppConstants.SQL_LIST_ALL_EMPLOYEES);
				retRows = service.getEmpDataAllEmployees(sqlName, ListPersonnel.class, "A.PRSN_ID_CD", ids);
			}
		}
		if (isCurrent) {
			DealerFilter df = new DealerFilter(mfpUser, dlrCd, rgnCd, zoneCd, districtCd, mdaCd);
			if (contactDateStr != null) {
				crDate = LocalDate.parse(contactDateStr.trim(),
						DateTimeFormatter.ofPattern(AppConstants.LOCALDATE_FORMAT));
			}
			try {
				String sqlName = getKPIQueryFilePath(AppConstants.SQL_LIST_ALL_ACTIVE_EMPLOYEES);
				DealerInfo dlrInfo = allDealersCache.getDealerInfo(dlrCd);
				if (dlrInfo != null) {
					String strCrDate = crDate.format(DateTimeFormatter.ofPattern(AppConstants.LOCALDATE_FORMAT));
					// retRows = service.getListData(sqlName, ListPersonnel.class, df,
					// dlrInfo.getRgnCd(),
					// dlrInfo.getZoneCd(), dlrInfo.getDistrictCd(), dlrInfo.getRgnCd(), strCrDate);
					retRows = service.getListData(sqlName, ListPersonnel.class, df, strCrDate);
					log.info("Returning {} Employee Information", retRows.size());
				}
			} catch (InstantiationException | IllegalAccessException | ParseException e) {
				log.error("ERROR retrieving list of Employees:", e);
			}
			checkEmployeeChanges.checkEmpChanges(retRows);
		}
		return retRows;
	}

	public List<ListPersonnel> getListOfAllReviewers(MFPUser mfpUser) {
		MMAListService<ListPersonnel> service = new MMAListService<ListPersonnel>();
		String sqlName = getKPIQueryFilePath(AppConstants.SQL_LIST_REVIEWER_EMPLOYEES_ALL);
		DealerFilter df = new DealerFilter(mfpUser, null, null, null, null, null);
		List<ListPersonnel> retRows = null;
		try {
			retRows = service.getListData(sqlName, ListPersonnel.class, df);
		} catch (InstantiationException | IllegalAccessException | ParseException e) {
			log.error("ERROR retrieving list of ALL Reviewers:", e);
		}
		return retRows;
	}

	public boolean validateReviewer(MFPUser mfpUser, Map<String, RegionZoneReviewer> rzReviewer,
			long contactReportId, int contactStatus, String contactAuthor, String contactReviewer, String dlrCd,
			String rgnCd, String zoneCd) {
		boolean matched = false;
		if (contactReportId > 0
				&& (contactStatus == ContactReportEnum.SUBMITTED.getStatusCode()
						|| contactStatus == ContactReportEnum.DISCUSSION_REQUESTED.getStatusCode())
				&& contactAuthor.equalsIgnoreCase(mfpUser.getUserid())) {
			RegionZoneReviewer rzrCR = new RegionZoneReviewer(rgnCd, zoneCd, null);
			RegionZoneReviewer rzr = rzReviewer.get(rzrCR.getRegionZone());
			List<ListPersonnel> reviewers = null;
			if (rzr != null) {
				reviewers = rzr.getReviewers();
			} else {
				rzrCR.setZone("");
				rzr = rzReviewer.get(rzrCR.getRegionZone());
				if (rzr != null) {
					reviewers = rzr.getReviewers();
				} else {
					reviewers = getListOfReviewers(dlrCd, contactReportId, mfpUser, null, null,
							null, null);
					rzrCR.setReviewers(reviewers);
					rzReviewer.put(rzrCR.getRegionZone(), rzrCR);
				}
			}
			for (ListPersonnel lp : reviewers) {
				if (lp.getPrsnIdCd().equals(contactReviewer)) {
					matched = true;
					break;
				}
			}
		}
		return matched;
	}

	public Map<String, RegionZoneReviewer> loadAllReviewer(MFPUser mfpUser) {
		Map<String, RegionZoneReviewer> allReviewers = new HashMap<>();
		List<ListPersonnel> reviewers = getListOfAllReviewers(mfpUser);
		for (ListPersonnel lp : reviewers) {
			RegionZoneReviewer rzr = new RegionZoneReviewer(lp.getRgnCd(), lp.getZoneCd(), null);
			RegionZoneReviewer allRrzr = allReviewers.get(rzr.getRegionZone());
			if (allRrzr != null) {
				allRrzr.getReviewers().add(lp);
			} else {
				List<ListPersonnel> lstRzr = new ArrayList<>();
				lstRzr.add(lp);
				rzr = new RegionZoneReviewer(lp.getRgnCd(), lp.getZoneCd(), lstRzr);
				allReviewers.put(rzr.getRegionZone(), rzr);
			}
		}
		return allReviewers;
	}

}
