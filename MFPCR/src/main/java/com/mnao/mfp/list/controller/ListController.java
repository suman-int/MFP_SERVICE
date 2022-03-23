package com.mnao.mfp.list.controller;

import com.mnao.mfp.common.controller.MfpKPIControllerBase;
import com.mnao.mfp.common.dao.DealerFilter;
import com.mnao.mfp.common.dao.DealerInfo;
import com.mnao.mfp.common.dto.CommonResponse;
import com.mnao.mfp.common.service.AbstractService;
import com.mnao.mfp.common.util.AppConstants;
import com.mnao.mfp.cr.Service.ContactInfoService;
import com.mnao.mfp.list.dao.*;
import com.mnao.mfp.list.service.ListService;
import com.mnao.mfp.list.service.MMAListService;
import com.mnao.mfp.user.dao.MFPUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.text.ParseException;
import java.util.List;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/Lists/")
public class ListController extends MfpKPIControllerBase {
	//
	private static final Logger log = LoggerFactory.getLogger(ListController.class);
	//
 
	//
	@PostMapping("/ListDealers")
	public CommonResponse<List<ListDealer>> listDealers(@RequestParam(value = "rgnCd", defaultValue = "") String rgnCd,
			@RequestParam(value = "zoneCd", defaultValue = "") String zoneCd,
			@RequestParam(value = "districtCd", defaultValue = "") String districtCd,
			@RequestParam(value = "mdaCd", defaultValue = "") String mdaCd,
			@SessionAttribute(name = "mfpUser") MFPUser mfpUser) {
		String sqlName = getKPIQueryFilePath(AppConstants.SQL_LIST_DEALERS);
		ListService<ListDealer> service = new ListService<ListDealer>();
		List<ListDealer> retRows = null;
		DealerFilter df = new DealerFilter(mfpUser, null, rgnCd, zoneCd, districtCd, mdaCd);
		try {
			retRows = service.getListData(sqlName, ListDealer.class, df);
		} catch (InstantiationException | IllegalAccessException | ParseException e) {
			log.error("ERROR retrieving list of Dealers:", e);
		}
		return AbstractService.httpPostSuccess(retRows, "Success");
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
			@SessionAttribute(name = "mfpUser") MFPUser mfpUser) {
		String sqlName = getKPIQueryFilePath(AppConstants.SQL_LIST_DEALER_EMPLOYEES);
		MMAListService<ListPersonnel> service = new MMAListService<ListPersonnel>();
		List<ListPersonnel> retRows = null;
		DealerFilter df = new DealerFilter(mfpUser, dlrCd, rgnCd, zoneCd, districtCd, mdaCd);
		try {
			retRows = service.getListData(sqlName, ListPersonnel.class, df, df.getDlrCd());
		} catch (InstantiationException | IllegalAccessException | ParseException e) {
			log.error("ERROR retrieving list of Employees:", e);
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
			@SessionAttribute(name = "mfpUser") MFPUser mfpUser) {
		String sqlName = getKPIQueryFilePath(AppConstants.SQL_LIST_REVIEWER_EMPLOYEES);
		MMAListService<ListPersonnel> service = new MMAListService<ListPersonnel>();
		List<ListPersonnel> retRows = null;
		DealerFilter df = new DealerFilter(mfpUser, dlrCd, rgnCd, zoneCd, districtCd, mdaCd);
		try {
			DealerInfo dlrInfo = getDealerInfo(mfpUser, dlrCd);
			retRows = service.getListData(sqlName, ListPersonnel.class, df, dlrInfo.getRgnCd(), dlrInfo.getZoneCd());
		} catch (InstantiationException | IllegalAccessException | ParseException e) {
			log.error("ERROR retrieving list of Employees:", e);
		}
		return AbstractService.httpPostSuccess(retRows, "Success");
	}

	//
	@PostMapping("/ListCorporateEmployees")
	public CommonResponse<List<ListPersonnel>> listCorporateEmployees(
			@RequestParam(value = "rgnCd", defaultValue = "") String rgnCd,
			@RequestParam(value = "zoneCd", defaultValue = "") String zoneCd,
			@RequestParam(value = "districtCd", defaultValue = "") String districtCd,
			@RequestParam(value = "mdaCd", defaultValue = "") String mdaCd,
			@RequestParam(value = "dlrCd", defaultValue = "") String dlrCd,
			@SessionAttribute(name = "mfpUser") MFPUser mfpUser) {
		String sqlName = getKPIQueryFilePath(AppConstants.SQL_LIST_CORPORATE_EMPLOYEES);
		MMAListService<ListPersonnel> service = new MMAListService<ListPersonnel>();
		List<ListPersonnel> retRows = null;
		DealerFilter df = new DealerFilter(mfpUser, dlrCd, rgnCd, zoneCd, districtCd, mdaCd);
		try {
			retRows = service.getListData(sqlName, ListPersonnel.class, df);
		} catch (InstantiationException | IllegalAccessException | ParseException e) {
			log.error("ERROR retrieving list of Employees:", e);
		}
		return AbstractService.httpPostSuccess(retRows, "Success");
	}

}
