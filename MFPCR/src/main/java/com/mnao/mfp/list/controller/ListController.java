package com.mnao.mfp.list.controller;

import java.text.ParseException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.SessionAttribute;

import com.mnao.mfp.common.controller.MfpKPIControllerBase;
import com.mnao.mfp.common.dao.DealerFilter;
import com.mnao.mfp.common.dao.DealerInfo;
import com.mnao.mfp.common.dto.CommonResponse;
import com.mnao.mfp.common.service.AbstractService;
import com.mnao.mfp.common.util.AppConstants;
import com.mnao.mfp.common.util.Utils;
import com.mnao.mfp.cr.entity.ContactReportDealerPersonnel;
import com.mnao.mfp.cr.entity.ContactReportInfo;
import com.mnao.mfp.cr.repository.ContactInfoRepository;
import com.mnao.mfp.list.cache.AllActiveEmployeesCache;
import com.mnao.mfp.list.cache.AllDealersCache;
import com.mnao.mfp.list.cache.CheckDealerChanges;
import com.mnao.mfp.list.cache.CheckEmployeeChanges;
import com.mnao.mfp.list.dao.ListDistrict;
import com.mnao.mfp.list.dao.ListMarket;
import com.mnao.mfp.list.dao.ListPersonnel;
import com.mnao.mfp.list.dao.ListRegion;
import com.mnao.mfp.list.dao.ListZone;
import com.mnao.mfp.list.service.ListService;
import com.mnao.mfp.list.service.MMAListService;
import com.mnao.mfp.user.dao.MFPUser;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/Lists/")
public class ListController extends MfpKPIControllerBase {
	//
	private static final Logger log = LoggerFactory.getLogger(ListController.class);
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

	//
	@PostMapping("/ListDealers")
	public CommonResponse<List<DealerInfo>> listDealers(@RequestParam(value = "rgnCd", defaultValue = "") String rgnCd,
			@RequestParam(value = "zoneCd", defaultValue = "") String zoneCd,
			@RequestParam(value = "districtCd", defaultValue = "") String districtCd,
			@RequestParam(value = "mdaCd", defaultValue = "") String mdaCd,
			@SessionAttribute(name = "mfpUser") MFPUser mfpUser) {
		String sqlName = getKPIQueryFilePath(AppConstants.SQL_LIST_DEALERS_UDB);
		MMAListService<DealerInfo> service = new MMAListService<DealerInfo>();
		List<DealerInfo> retRows = null;
		DealerFilter df = new DealerFilter(mfpUser, null, rgnCd, zoneCd, districtCd, mdaCd);
		try {
			retRows = service.getListData(sqlName, DealerInfo.class, df);
		} catch (InstantiationException | IllegalAccessException | ParseException e) {
			log.error("ERROR retrieving list of Dealers:", e);
		}
		checkDealerChanges.checkDealerChanges(retRows);
		return AbstractService.httpPostSuccess(retRows, "Success");
	}

	//
	@PostMapping("/ListDealersLike")
	public CommonResponse<List<DealerInfo>> listDealersLike(
			@RequestParam(value = "rgnCd", defaultValue = "") String rgnCd,
			@RequestParam(value = "zoneCd", defaultValue = "") String zoneCd,
			@RequestParam(value = "districtCd", defaultValue = "") String districtCd,
			@RequestParam(value = "mdaCd", defaultValue = "") String mdaCd,
			@RequestParam(value = "like", defaultValue = "") String like,
			@SessionAttribute(name = "mfpUser") MFPUser mfpUser) {
		if (like == null || like.trim().length() == 0) {
			return listDealers(rgnCd, zoneCd, districtCd, mdaCd, mfpUser);
		} else {
			String sqlName = getKPIQueryFilePath(AppConstants.SQL_LIST_DEALERS_LIKE_UDB);
			MMAListService<DealerInfo> service = new MMAListService<DealerInfo>();
			List<DealerInfo> retRows = null;
			DealerFilter df = new DealerFilter(mfpUser, null, rgnCd, zoneCd, districtCd, mdaCd);
			if (like.length() > 30)
				like = like.substring(0, 30);
			like = like.trim();
			String likePat = "%" + like.toUpperCase() + "%";
			try {
				retRows = service.getListData(sqlName, DealerInfo.class, df, likePat, likePat);
			} catch (InstantiationException | IllegalAccessException | ParseException e) {
				log.error("ERROR retrieving list of Dealers:", e);
			}
			checkDealerChanges.checkDealerChanges(retRows);
			return AbstractService.httpPostSuccess(retRows, "Success");
		}
	}

	//
	@PostMapping("/ListDistricts")
	public CommonResponse<List<ListDistrict>> listDistricts(
			@RequestParam(value = "rgnCd", defaultValue = "") String rgnCd,
			@RequestParam(value = "zoneCd", defaultValue = "") String zoneCd,
			@SessionAttribute(name = "mfpUser") MFPUser mfpUser) {
		String sqlName = getKPIQueryFilePath(AppConstants.SQL_LIST_DISTRICTS);
		ListService<ListDistrict> service = new ListService<ListDistrict>();
		List<ListDistrict> retRows = null;
		DealerFilter df = new DealerFilter(mfpUser, null, rgnCd, zoneCd, null, null);
		try {
			retRows = service.getListData(sqlName, ListDistrict.class, df, rgnCd, zoneCd);
		} catch (InstantiationException | IllegalAccessException | ParseException e) {
			log.error("ERROR retrieving list of Districts:", e);
		}
		return AbstractService.httpPostSuccess(retRows, "Success");
	}

	//
	@PostMapping("/ListZones")
	public CommonResponse<List<ListZone>> listZones(@RequestParam(value = "rgnCd", defaultValue = "") String rgnCd,
			@SessionAttribute(name = "mfpUser") MFPUser mfpUser) {
		String sqlName = getKPIQueryFilePath(AppConstants.SQL_LIST_ZONES);
		ListService<ListZone> service = new ListService<ListZone>();
		List<ListZone> retRows = null;
		DealerFilter df = new DealerFilter(mfpUser, null, rgnCd, null, null, null);
		try {
			retRows = service.getListData(sqlName, ListZone.class, df, rgnCd);
		} catch (InstantiationException | IllegalAccessException | ParseException e) {
			log.error("ERROR retrieving list of Zones:", e);
		}
		return AbstractService.httpPostSuccess(retRows, "Success");
	}

	//
	@PostMapping("/ListRegions")
	public CommonResponse<List<ListRegion>> listRegions(@SessionAttribute(name = "mfpUser") MFPUser mfpUser) {
		String sqlName = getKPIQueryFilePath(AppConstants.SQL_LIST_REGIONS);
		ListService<ListRegion> service = new ListService<ListRegion>();
		List<ListRegion> retRows = null;
		DealerFilter df = new DealerFilter(mfpUser, null, null, null, null, null);
		try {
			retRows = service.getListData(sqlName, ListRegion.class, df);
		} catch (InstantiationException | IllegalAccessException | ParseException e) {
			log.error("ERROR retrieving list of Regions:", e);
		}
		return AbstractService.httpPostSuccess(retRows, "Success");
	}

	//
	@PostMapping("/ListMarkets")
	public CommonResponse<List<ListMarket>> listMarkets(@SessionAttribute(name = "mfpUser") MFPUser mfpUser) {
		String sqlName = getKPIQueryFilePath(AppConstants.SQL_LIST_MARKETS);
		ListService<ListMarket> service = new ListService<ListMarket>();
		List<ListMarket> retRows = null;
		DealerFilter df = new DealerFilter(mfpUser, null, null, null, null, null);
		try {
			retRows = service.getListData(sqlName, ListMarket.class, df);
		} catch (InstantiationException | IllegalAccessException | ParseException e) {
			log.error("ERROR retrieving list of Markets:", e);
		}
		return AbstractService.httpPostSuccess(retRows, "Success");
	}

	//
	@PostMapping("/ListDealerEmployees")
	public CommonResponse<List<ListPersonnel>> listDealerEmployees(
			@RequestParam(value = "rgnCd", defaultValue = "") String rgnCd,
			@RequestParam(value = "zoneCd", defaultValue = "") String zoneCd,
			@RequestParam(value = "districtCd", defaultValue = "") String districtCd,
			@RequestParam(value = "mdaCd", defaultValue = "") String mdaCd,
			@RequestParam(value = "dlrCd", defaultValue = "") String dlrCd,
			@RequestParam(value = "id", defaultValue = "0") Long id,
			@SessionAttribute(name = "mfpUser") MFPUser mfpUser) {
		List<ListPersonnel> retRows = null;
		MMAListService<ListPersonnel> service = new MMAListService<>();
		if (id != null && id < 0) {
			ContactReportInfo reportDb = contactInfoRepository.getById(id);
			List<ContactReportDealerPersonnel> dps = reportDb.getDealerPersonnels();
			List<String> ids = dps.stream().map(ContactReportDealerPersonnel::getPersonnelIdCd)
					.collect(Collectors.toList());
			String sqlName = getKPIQueryFilePath(AppConstants.SQL_LIST_ALL_EMPLOYEES);
			retRows = service.getEmpDataAllEmployees(sqlName, ListPersonnel.class, "A.PRSN_ID_CD", ids);
		} else {
			String sqlName = getKPIQueryFilePath(AppConstants.SQL_LIST_DEALER_EMPLOYEES);
			DealerFilter df = new DealerFilter(mfpUser, dlrCd, rgnCd, zoneCd, districtCd, mdaCd);
			try {
				retRows = service.getListData(sqlName, ListPersonnel.class, df, df.getDlrCd());
			} catch (InstantiationException | IllegalAccessException | ParseException e) {
				log.error("ERROR retrieving list of Employees:", e);
			}
		}
		return AbstractService.httpPostSuccess(retRows, "Success");
	}

	//
	@PostMapping("/ListReviewerEmployees")
	public CommonResponse<List<ListPersonnel>> listReviewerEmployees(
			@RequestParam(value = "rgnCd", defaultValue = "") String rgnCd,
			@RequestParam(value = "zoneCd", defaultValue = "") String zoneCd,
			@RequestParam(value = "districtCd", defaultValue = "") String districtCd,
			@RequestParam(value = "mdaCd", defaultValue = "") String mdaCd,
			@RequestParam(value = "dlrCd", defaultValue = "") String dlrCd,
			@RequestParam(value = "id", defaultValue = "0") Long id,
			@SessionAttribute(name = "mfpUser") MFPUser mfpUser) {
		List<ListPersonnel> retRows = null;
		MMAListService<ListPersonnel> service = new MMAListService<ListPersonnel>();
		if (id != null && id < 0) {
			ContactReportInfo reportDb = contactInfoRepository.getById(id);
			String rev = reportDb.getReviewedBy();
			List<String> ids = Arrays.asList(rev);
			String sqlName = getKPIQueryFilePath(AppConstants.SQL_LIST_ALL_EMPLOYEES);
			retRows = service.getEmpDataAllEmployees(sqlName, ListPersonnel.class, "A.PRSN_ID_CD", ids);
		} else {
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
		return AbstractService.httpPostSuccess(retRows, "Success");
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

	//
	@PostMapping("/ListCorporateEmployeesFiltered")
	public CommonResponse<List<ListPersonnel>> listCorporateEmployeesFiltered(
			@RequestParam(value = "rgnCd", defaultValue = "") String rgnCd,
			@RequestParam(value = "zoneCd", defaultValue = "") String zoneCd,
			@RequestParam(value = "districtCd", defaultValue = "") String districtCd,
			@RequestParam(value = "mdaCd", defaultValue = "") String mdaCd,
			@RequestParam(value = "dlrCd", defaultValue = "") String dlrCd,
			@RequestParam(value = "id", defaultValue = "0") Long id,
			@SessionAttribute(name = "mfpUser") MFPUser mfpUser) {
		String sqlName = getKPIQueryFilePath(AppConstants.SQL_LIST_CORPORATE_EMPLOYEES);
		MMAListService<ListPersonnel> service = new MMAListService<ListPersonnel>();
		List<ListPersonnel> retRows = null;
		DealerFilter df = new DealerFilter(mfpUser, dlrCd, rgnCd, zoneCd, districtCd, mdaCd);
		try {
			// DealerInfo dlrInfo = getDealerInfo(null, dlrCd);
			DealerInfo dlrInfo = allDealersCache.getDealerInfo(dlrCd);
			if (dlrInfo != null) {
				retRows = service.getListData(sqlName, ListPersonnel.class, df, dlrInfo.getRgnCd(), dlrInfo.getZoneCd(),
						dlrInfo.getDistrictCd(), dlrInfo.getRgnCd());
			}
		} catch (InstantiationException | IllegalAccessException | ParseException e) {
			log.error("ERROR retrieving list of Employees:", e);
		}
		checkEmployeeChanges.checkEmpChanges(retRows);
		return AbstractService.httpPostSuccess(retRows, "Success");
	}

	//
	//
	@PostMapping("/ListCorporateEmployees")
	public CommonResponse<List<ListPersonnel>> listCorporateEmployeesAll(
			@RequestParam(value = "rgnCd", defaultValue = "") String rgnCd,
			@RequestParam(value = "zoneCd", defaultValue = "") String zoneCd,
			@RequestParam(value = "districtCd", defaultValue = "") String districtCd,
			@RequestParam(value = "mdaCd", defaultValue = "") String mdaCd,
			@RequestParam(value = "dlrCd", defaultValue = "") String dlrCd,
			@RequestParam(value = "id", defaultValue = "0") Long id,
			@SessionAttribute(name = "mfpUser") MFPUser mfpUser) {
		MMAListService<ListPersonnel> service = new MMAListService<ListPersonnel>();
		List<ListPersonnel> retRows = null;
		if (id != null && id < 0) {
			ContactReportInfo reportDb = contactInfoRepository.getById(id);
			String corps = reportDb.getCorporateReps();
			List<String> ids = Arrays.asList(corps.split("[|]"));
			String sqlName = getKPIQueryFilePath(AppConstants.SQL_LIST_ALL_EMPLOYEES);
			retRows = service.getEmpDataAllEmployees(sqlName, ListPersonnel.class, "A.PRSN_ID_CD", ids);
		} else {
		DealerFilter df = new DealerFilter(mfpUser, dlrCd, rgnCd, zoneCd, districtCd, mdaCd);
		try {
			String sqlName = getKPIQueryFilePath(AppConstants.SQL_LIST_ALL_ACTIVE_EMPLOYEES);
			DealerInfo dlrInfo = allDealersCache.getDealerInfo(dlrCd);
			if (dlrInfo != null) {
				retRows = service.getListData(sqlName, ListPersonnel.class, df, dlrInfo.getRgnCd(), dlrInfo.getZoneCd(),
						dlrInfo.getDistrictCd(), dlrInfo.getRgnCd());
				log.info("Returning {} Employee Information", retRows.size());
			}
		} catch (InstantiationException | IllegalAccessException | ParseException e) {
			log.error("ERROR retrieving list of Employees:", e);
		}
		checkEmployeeChanges.checkEmpChanges(retRows);
		}
		return AbstractService.httpPostSuccess(retRows, "Success");
	}

	//
	@PostMapping("/GetDealerInfo")
	public CommonResponse<DealerInfo> getDealerInfo(@RequestParam(required = true, value = "dlrCd") String dlrCd,
			@SessionAttribute(name = "mfpUser") MFPUser mfpUser) {
//        DealerInfo dlrInfo = getDealerInfo(mfpUser, dlrCd);
		DealerInfo dlrInfo = allDealersCache.getDealerInfo(dlrCd);
		return AbstractService.httpPostSuccess(dlrInfo, "Success");
	}

	//
	@PostMapping("/GetEmployeeInfo")
	public CommonResponse<ListPersonnel> getEmployeeInfo(@RequestParam(required = true, value = "empCd") String empCd,
			@SessionAttribute(name = "mfpUser") MFPUser mfpUser) {
		ListPersonnel empInfo = allEmployeesCache.getByPrsnIdCd(empCd);
		return AbstractService.httpPostSuccess(empInfo, "Success");
	}

}
