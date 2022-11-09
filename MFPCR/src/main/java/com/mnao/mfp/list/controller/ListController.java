package com.mnao.mfp.list.controller;

import java.text.ParseException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
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
import com.mnao.mfp.cr.entity.ContactReportDealerPersonnel;
import com.mnao.mfp.cr.entity.ContactReportInfo;
import com.mnao.mfp.cr.repository.ContactInfoRepository;
import com.mnao.mfp.cr.util.ContactReportEnum;
import com.mnao.mfp.list.cache.AllActiveEmployeesCache;
import com.mnao.mfp.list.cache.AllDealersCache;
import com.mnao.mfp.list.cache.CheckDealerChanges;
import com.mnao.mfp.list.dao.ListDistrict;
import com.mnao.mfp.list.dao.ListMarket;
import com.mnao.mfp.list.dao.ListPersonnel;
import com.mnao.mfp.list.dao.ListRegion;
import com.mnao.mfp.list.dao.ListZone;
import com.mnao.mfp.list.service.ListEmployeeDataService;
import com.mnao.mfp.list.service.ListService;
import com.mnao.mfp.list.service.MMAListService;
import com.mnao.mfp.user.dao.MFPUser;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/Lists/")
public class ListController extends MfpKPIControllerBase {
	//
	public static final Logger log = LoggerFactory.getLogger(ListController.class);
	//
	@Autowired
	AllActiveEmployeesCache allEmployeesCache;
	@Autowired
	private AllDealersCache allDealersCache;
	@Autowired
	private CheckDealerChanges checkDealerChanges;
	@Autowired
	private ContactInfoRepository contactInfoRepository;
	@Autowired
	private ListEmployeeDataService employeeDataService;

	//
	@PostMapping("/ListDealers")
	public CommonResponse<List<DealerInfo>> listDealers(@RequestParam(value = "rgnCd", defaultValue = "") String rgnCd,
			@RequestParam(value = "zoneCd", defaultValue = "") String zoneCd,
			@RequestParam(value = "districtCd", defaultValue = "") String districtCd,
			@RequestParam(value = "mdaCd", defaultValue = "") String mdaCd,
			@RequestParam(value = "onlyActive", defaultValue = "1") Integer onlyActive,
			@SessionAttribute(name = "mfpUser") MFPUser mfpUser) {
		String sqlName = getKPIQueryFilePath(AppConstants.SQL_LIST_DEALERS_UDB_ACTIVE);
		if (onlyActive != 1) {
			sqlName = getKPIQueryFilePath(AppConstants.SQL_LIST_DEALERS_UDB_ALL);
		}
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
			@RequestParam(value = "onlyActive", defaultValue = "1") Integer onlyActive,
			@SessionAttribute(name = "mfpUser") MFPUser mfpUser) {
		if (like == null || like.trim().length() == 0) {
			return listDealers(rgnCd, zoneCd, districtCd, mdaCd, onlyActive, mfpUser);
		} else {
			String sqlName = getKPIQueryFilePath(AppConstants.SQL_LIST_DEALERS_LIKE_UDB_ACTIVE);
			if (onlyActive != 1) {
				sqlName = getKPIQueryFilePath(AppConstants.SQL_LIST_DEALERS_LIKE_UDB_ALL);
			}
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
			@RequestParam(value = "onlyActive", defaultValue = "1") Integer onlyActive,
			@SessionAttribute(name = "mfpUser") MFPUser mfpUser) {
		String sqlName = getKPIQueryFilePath(AppConstants.SQL_LIST_DISTRICTS_ACTIVE);
		if (onlyActive != 1) {
			sqlName = getKPIQueryFilePath(AppConstants.SQL_LIST_DISTRICTS_ALL);
		}
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
			@RequestParam(value = "onlyActive", defaultValue = "1") Integer onlyActive,
			@SessionAttribute(name = "mfpUser") MFPUser mfpUser) {
		String sqlName = getKPIQueryFilePath(AppConstants.SQL_LIST_ZONES_ACTIVE);
		if (onlyActive != 1) {
			sqlName = getKPIQueryFilePath(AppConstants.SQL_LIST_ZONES_ALL);
		}
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
	public CommonResponse<List<ListRegion>> listRegions(
			@RequestParam(value = "onlyActive", defaultValue = "1") Integer onlyActive,
			@SessionAttribute(name = "mfpUser") MFPUser mfpUser) {
		String sqlName = getKPIQueryFilePath(AppConstants.SQL_LIST_REGIONS_ACTIVE);
		if (onlyActive != 1) {
			sqlName = getKPIQueryFilePath(AppConstants.SQL_LIST_REGIONS_ALL);
		}
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
	public CommonResponse<List<ListMarket>> listMarkets(
			@RequestParam(value = "onlyActive", defaultValue = "1") Integer onlyActive,
			@SessionAttribute(name = "mfpUser") MFPUser mfpUser) {
		String sqlName = getKPIQueryFilePath(AppConstants.SQL_LIST_MARKETS_ACTIVE);
		if (onlyActive != 1) {
			sqlName = getKPIQueryFilePath(AppConstants.SQL_LIST_MARKETS_ACTIVE);
		}
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
			@RequestParam(value = "contactDate", defaultValue = "") String contactDateStr,
			@SessionAttribute(name = "mfpUser") MFPUser mfpUser) {
		List<ListPersonnel> retRows = null;
		MMAListService<ListPersonnel> service = new MMAListService<>();
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
				List<ContactReportDealerPersonnel> dps = reportDb.getDealerPersonnels();
				List<String> ids = dps.stream().map(ContactReportDealerPersonnel::getPersonnelIdCd)
						.collect(Collectors.toList());
				String sqlName = getKPIQueryFilePath(AppConstants.SQL_LIST_ALL_EMPLOYEES);
				retRows = service.getEmpDataAllEmployees(sqlName, ListPersonnel.class, "A.PRSN_ID_CD", ids);
			}
		}
		if (isCurrent) {
			String sqlName = getKPIQueryFilePath(AppConstants.SQL_LIST_DEALER_EMPLOYEES);
			DealerFilter df = new DealerFilter(mfpUser, dlrCd, rgnCd, zoneCd, districtCd, mdaCd);
			if ((contactDateStr != null) && (contactDateStr.trim().length() == 10)) {
				crDate = LocalDate.parse(contactDateStr.trim(),
						DateTimeFormatter.ofPattern(AppConstants.LOCALDATE_FORMAT));
			}
			try {
				String strCrDate = crDate.format(DateTimeFormatter.ofPattern(AppConstants.LOCALDATE_FORMAT));
				retRows = service.getListData(sqlName, ListPersonnel.class, df, df.getDlrCd(), strCrDate);
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
		List<ListPersonnel> retRows = employeeDataService.getListOfReviewers(dlrCd, id, mfpUser, rgnCd, zoneCd,
				districtCd, mdaCd);
		return AbstractService.httpPostSuccess(retRows, "Success");
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
		List<ListPersonnel> retRows = employeeDataService.getListOfCorporateEmployeesFiltered(rgnCd, zoneCd, districtCd,
				mdaCd, dlrCd, id, mfpUser);
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
			@RequestParam(value = "contactDate", defaultValue = "") String contactDateStr,
			@SessionAttribute(name = "mfpUser") MFPUser mfpUser) {
		List<ListPersonnel> retRows = employeeDataService.getListOfCorporateEmployeesAll(rgnCd, zoneCd, districtCd,
				mdaCd, dlrCd, id, contactDateStr, mfpUser);
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
