package com.mnao.mfp.list.emp;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;

import com.mnao.mfp.common.controller.MfpKPIControllerBase;
import com.mnao.mfp.common.dao.DealerFilter;
import com.mnao.mfp.common.util.AppConstants;
import com.mnao.mfp.list.controller.ListController;
import com.mnao.mfp.list.dao.ListPersonnel;
import com.mnao.mfp.list.service.MMAListService;
import com.mnao.mfp.user.dao.Domain;
import com.mnao.mfp.user.dao.MFPUser;

@Component()
public class AllEmployeesCache extends MfpKPIControllerBase {
	//
	private static final Logger log = LoggerFactory.getLogger(ListController.class);
	//

	private HashMap<String, ListPersonnel> allEmployees = new HashMap<>();

	public HashMap<String, ListPersonnel> getAllEmployees() {
		if (allEmployees.size() == 0) {
			loadAllEmployees();
		}
		return allEmployees;
	}

	public ListPersonnel getByPrsnIdCd(String prsnIdCd) {
		return getAllEmployees().get(prsnIdCd);
	}

	public void updateDomain(MFPUser mfpUser) {
		ListPersonnel lp = getByPrsnIdCd(mfpUser.getEmployeeNumber());
		if (lp != null) {
			if (lp.getLoctnCd().equals("MA92")) {
				mfpUser.setCorporatePerson(true);
				mfpUser.setCorpPerson(true);
			} else {
				Domain dom = mfpUser.getDomain();
				if (dom == null)
					dom = new Domain();
				String rgn = lp.getRgnCd();
				String zon = lp.getZoneCd();
				String dst = lp.getDistrictCd();
				//
				dom.setRegions(new ArrayList<String>());
				if (rgn != null && rgn.trim().length() > 0) {
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
	}

	private void loadAllEmployees() {
		String sqlName = getKPIQueryFilePath(AppConstants.SQL_LIST_ALL_EMPLOYEES);
		MMAListService<ListPersonnel> service = new MMAListService<ListPersonnel>();
		List<ListPersonnel> retRows = null;
		DealerFilter df = new DealerFilter();
		try {
			retRows = service.getListData(sqlName, ListPersonnel.class, df);
		} catch (InstantiationException | IllegalAccessException | ParseException e) {
			log.error("ERROR retrieving list of Employees:", e);
		}
		if (retRows != null) {
			for (ListPersonnel le : retRows) {
				allEmployees.put(le.getPrsnIdCd(), le);
			}
		}
		System.out.println("" + allEmployees.size() + " Employees loaded to cache.");
	}

}
