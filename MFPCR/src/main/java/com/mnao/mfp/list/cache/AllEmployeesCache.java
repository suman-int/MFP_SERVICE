package com.mnao.mfp.list.cache;

import java.text.ParseException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.support.CronExpression;
import org.springframework.stereotype.Service;

import com.mnao.mfp.common.controller.MfpKPIControllerBase;
import com.mnao.mfp.common.dao.DealerFilter;
import com.mnao.mfp.common.util.AppConstants;
import com.mnao.mfp.list.dao.ListPersonnel;
import com.mnao.mfp.list.service.MMAListService;
import com.mnao.mfp.user.dao.Domain;
import com.mnao.mfp.user.dao.MFPUser;

//@Component()
@Service
public class AllEmployeesCache extends MfpKPIControllerBase {
	//
	private static final Logger log = LoggerFactory.getLogger(AllEmployeesCache.class);
	//
	@Value("${emp.sync.schedule.cron}")
	private String empSyncCronSetting;
	private static LocalDateTime nextSync = LocalDateTime.now();
	//
	private static final HashMap<String, ListPersonnel> allEmployeesById = new HashMap<>();
	private static final HashMap<String, ListPersonnel> allEmployeesByWSLId = new HashMap<>();
	private static final List<ListPersonnel> allEmployeesList = new ArrayList<>();

	public synchronized HashMap<String, ListPersonnel> getAllEmployees() {
		if (allEmployeesList.size() == 0 || LocalDateTime.now().isAfter(nextSync)) {
			loadAllEmployees();
			if( empSyncCronSetting == null || empSyncCronSetting.trim().length() == 0) {
				empSyncCronSetting = "0 0 5-17 * * MON-FRI";
			}
			CronExpression cronTrigger = CronExpression.parse(empSyncCronSetting);
			if (cronTrigger != null) {
				nextSync = cronTrigger.next(LocalDateTime.now());
				log.debug("Next Emp Sync Execution Time: " + nextSync);
			}
		}
		return allEmployeesById;
	}

	public ListPersonnel getByPrsnIdCd(String prsnIdCd) {
		return getAllEmployees().get(prsnIdCd);
	}

	public ListPersonnel getByWSLCd(String wslId) {
		ListPersonnel lp = null;
		if( getAllEmployees().size() > 0)
			lp = allEmployeesByWSLId.get(wslId.trim().toUpperCase());
		return lp;
	}

	/* Checks for DOmain Change
	 * This is used to flag refresh of in mem cache
	 */
	public boolean checkDomaniChanged(List<ListPersonnel> emps) {
		boolean rv = false;
		if( emps != null && allEmployeesList != null)
			rv = emps.stream().anyMatch(e -> isDomainChanged(e));
		else
			rv = true;
		if( rv )
			allEmployeesList.clear();
		return rv;
	}
	//
	private boolean isDomainChanged(ListPersonnel lp) {
		boolean rv = false;
		// Do not use getAllEmployees()
		if( allEmployeesById.size() > 0 ) {
			ListPersonnel lpch = allEmployeesById.get(lp.getPrsnIdCd());
			if( lpch == null ) {
				// New Employee
				rv = true;
			} else {
				rv = ! (lp.getLoctnCd().equals(lpch.getLoctnCd()) &&
						lp.getRgnCd().equals(lpch.getRgnCd()) && 
						lp.getZoneCd().equals(lpch.getZoneCd()) &&
						lp.getDistrictCd().equals(lpch.getDistrictCd()) ) ;
			}
		}
		return rv;
	}
	//
	public void updateDomain(MFPUser mfpUser) {
		if (!mfpUser.isDbDomainUpdated()) {
			boolean updateEmpID = false;
			ListPersonnel lp = getByPrsnIdCd(mfpUser.getEmployeeNumber());
			if( lp == null ) {
				lp = getByWSLCd(mfpUser.getUserid().toUpperCase());
				updateEmpID = true;
			}
			if (lp != null) {
				if (lp.getLoctnCd().equals("MA92")) {
					mfpUser.setCorporatePerson(true);
					mfpUser.setCorpPerson(true);
				} else {
					mfpUser.setCorporatePerson(false);
					mfpUser.setCorpPerson(false);
					if( updateEmpID )
						mfpUser.setEmployeeNumber(lp.getPrsnIdCd());
					Domain dom = mfpUser.getDomain();
					if (dom == null)
						dom = new Domain();
					String rgn = lp.getRgnCd();
					String zon = lp.getZoneCd();
					String dst = lp.getDistrictCd();
					//
					dom.setRegions(new ArrayList<String>());
					if (rgn != null && rgn.trim().length() > 0) {
						if( rgn.equalsIgnoreCase("SE"))
							rgn = "GU";
						dom.getRegions().add(rgn);
					}
					//
					dom.setZones(new ArrayList<String>());
					if (zon != null && zon.trim().length() > 0) {
						dom.getZones().add(zon);
					}
					//
					dom.setDistricts(new ArrayList<String>());
					if (dst != null && dst.trim().length() > 0) {
						dom.getDistricts().add(dst);
					}
					//
					mfpUser.setDomain(dom);
				}
			}
			mfpUser.setDbDomainUpdated(true);
		}
	}

	private void loadAllEmployees() {
		String sqlName = getKPIQueryFilePath(AppConstants.SQL_LIST_ALL_EMPLOYEES);
		MMAListService<ListPersonnel> service = new MMAListService<ListPersonnel>();
		List<ListPersonnel> retRows = null;
		DealerFilter df = new DealerFilter();
		allEmployeesList.clear();
		allEmployeesById.clear();
		allEmployeesByWSLId.clear();
		try {
			retRows = service.getListData(sqlName, ListPersonnel.class, df);
		} catch (InstantiationException | IllegalAccessException | ParseException e) {
			log.error("ERROR retrieving list of Employees:", e);
		}
		if (retRows != null) {
			for (ListPersonnel le : retRows) {
				allEmployeesList.add(le);
				allEmployeesById.put(le.getPrsnIdCd(), le);
				allEmployeesByWSLId.put(le.getUserId().trim().toUpperCase(), le);
			}
		}
		log.debug("" + allEmployeesById.size() + " Employees loaded to cache.");
	}

}
