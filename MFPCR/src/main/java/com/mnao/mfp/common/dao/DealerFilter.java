package com.mnao.mfp.common.dao;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import org.springframework.util.CollectionUtils;

import com.mnao.mfp.common.util.NullCheck;
import com.mnao.mfp.user.dao.Domain;
import com.mnao.mfp.user.dao.MFPUser;

import lombok.Getter;

@Getter
public class DealerFilter {
	//
	private static final String colDlrCd = "DLR_CD";
	private static final String colRgnCd = "RGN_CD";
	private static final String colZoneCd = "ZONE_CD";
	private static final String colDistrictCd = "DISTRICT_CD";
	private static final String colMdaCd = "MDA_CD";
	private String dlrCd;
	private String rgnCd;
	private boolean rgnMulti = false;
	private String zoneCd;
	private boolean zoneMulti = false;
	private String districtCd;
	private boolean districtMulti = false;
	private String mdaCd;
	private MFPUser mfpUser;

	public class FilterSet {
		public Set<String> dealersSet;
		public Set<String> rgnSet;
		public Set<String> zoneSet;
		public Set<String> districtSet;
		public Set<String> marketSet;
	}
	public DealerFilter() {

	}

	public DealerFilter(MFPUser mfpUser, String dlrCd, String rgnCd, String zoneCd, String districtCd, String mdaCd) {
		super();
		this.dlrCd = dlrCd;
		this.rgnCd = rgnCd;
		this.zoneCd = zoneCd;
		this.districtCd = districtCd;
		this.mdaCd = mdaCd;
		this.mfpUser = mfpUser;
	}

	private void setupUserFilters() {
		if( mfpUser.getCorporatePerson() || mfpUser.getCorpPerson()) {
			return;
		}
		Domain dom = mfpUser.getDomain();
		if (CollectionUtils.isEmpty(dom.getRegions()) && new NullCheck<Domain>(dom).with(Domain::getRegion).isNull() &&
				CollectionUtils.isEmpty(dom.getZones()) && new NullCheck<Domain>(dom).with(Domain::getZone).isNull() &&
				CollectionUtils.isEmpty(dom.getDistricts()) && new NullCheck<Domain>(dom).with(Domain::getDistrict).isNull()) {
			this.rgnCd = "XX";
			this.rgnMulti = false;
			return;
		}
		if ((dom.getRegions() != null) && (!dom.getRegions().isEmpty())) {
			rgnMulti = true;
			if ((rgnCd != null) && rgnCd.trim().length() > 0) {
				if (!dom.getRegions().contains(rgnCd)) {
					rgnCd = null;
				} else {
					rgnMulti = false;
				}
			}
		} else if ((dom.getRegion() != null) && (dom.getRegion().getCode() != null) && (dom.getRegion().getCode().trim().length() > 0)) {
			rgnCd = dom.getRegion().getCode().trim();
		}
		if ((dom.getZones() != null) && (!dom.getZones().isEmpty())) {
			zoneMulti = true;
			if ((zoneCd != null) && zoneCd.trim().length() > 0) {
				if (!dom.getZones().contains(zoneCd)) {
					zoneCd = null;
				} else {
					zoneMulti = false;
				}
			}
		} else if ((dom.getZone() != null) && (dom.getZone().getCode() != null) && (dom.getZone().getCode().trim().length() > 0)) {
			zoneCd = dom.getZone().getCode().trim();
		}
		if ((dom.getDistricts() != null) && (!dom.getDistricts().isEmpty())) {
			districtMulti = true;
			if ((districtCd != null) && districtCd.trim().length() > 0) {
				if (!dom.getDistricts().contains(districtCd)) {
					districtCd = null;
				} else {
					districtMulti = false;
				}
			}
		} else if ((dom.getDistrict() != null) && (dom.getDistrict().getCode() != null) && (dom.getDistrict().getCode().trim().length() > 0)) {
			districtCd = dom.getDistrict().getCode().trim();
		}
	}

	public FilterSet getFilterSet() {
		FilterSet fs = new FilterSet();
		Domain dom = null;
		if (mfpUser != null) {
			setupUserFilters();
			dom = mfpUser.getDomain();
		}
		if ((mdaCd != null) && mdaCd.trim().length() > 0) {
			fs.marketSet = new HashSet<>();
			fs.marketSet.add(mdaCd);
		} else {
			if (rgnMulti) {
				fs.rgnSet = new HashSet<>();
				fs.rgnSet.addAll(dom.getRegions());
			} else if ((rgnCd != null) && rgnCd.trim().length() > 0) {
				fs.rgnSet = new HashSet<>();
				fs.rgnSet.add(rgnCd);
			}
			if (zoneMulti) {
				fs.zoneSet = new HashSet<>();
				fs.zoneSet.addAll(dom.getZones());
			} else if ((zoneCd != null) && zoneCd.trim().length() > 0) {
				fs.zoneSet = new HashSet<>();
				fs.zoneSet.add(zoneCd);
			}
			if (districtMulti) {
				fs.districtSet = new HashSet<>();
				fs.districtSet.addAll(dom.getDistricts());
			} else if ((districtCd != null) && districtCd.trim().length() > 0) {
				fs.districtSet = new HashSet<>();
				fs.districtSet.add(districtCd);
			}
		}
		if ((dlrCd != null) && dlrCd.trim().length() > 0) {
			fs.dealersSet = new HashSet<>();
			fs.dealersSet.add(dlrCd);
		}
		return fs;
	}

	public String getWhereCondition(String tabAlias) {
		StringBuilder sb = new StringBuilder();
		boolean added = false;
		Domain dom = null;
		if (mfpUser != null) {
			setupUserFilters();
			dom = mfpUser.getDomain();
		}
		if ((mdaCd != null) && mdaCd.trim().length() > 0) {
			appendEqualsClause(sb, added, tabAlias + "." + colMdaCd, mdaCd);
			added = true;
		} else {
			if (rgnMulti) {
				appendInClause(sb, added, tabAlias + "." + colRgnCd, dom.getRegions());
				added = true;
			} else if ((rgnCd != null) && rgnCd.trim().length() > 0) {
				appendEqualsClause(sb, added, tabAlias + "." + colRgnCd, rgnCd);
				added = true;
			}
			if (zoneMulti) {
				appendInClause(sb, added, tabAlias + "." + colZoneCd, dom.getZones());
				added = true;
			} else if ((zoneCd != null) && zoneCd.trim().length() > 0) {
				appendEqualsClause(sb, added, tabAlias + "." + colZoneCd, zoneCd);
				added = true;
			}
			if (districtMulti) {
				appendInClause(sb, added, tabAlias + "." + colDistrictCd, dom.getDistricts());
				added = true;
			} else if ((districtCd != null) && districtCd.trim().length() > 0) {
				appendEqualsClause(sb, added, tabAlias + "." + colDistrictCd, districtCd);
				added = true;
			}
		}
		if ((dlrCd != null) && dlrCd.trim().length() > 0) {
			appendEqualsClause(sb, added, tabAlias + "." + colDlrCd, dlrCd);
			added = true;
		}
		String ret = sb.toString();
		if (added)
			ret = "( " + ret + " )";
		return ret;
	}

	private void appendEqualsClause(StringBuilder sb, boolean added, String colID, String val) {
		if (added) {
			sb.append(" AND ");
		}
		sb.append(colID);
		sb.append(" = '");
		sb.append(val.trim());
		sb.append("' ");
	}

	private void appendInClause(StringBuilder sb, boolean added, String colID, ArrayList<String> vals) {
		if (added) {
			sb.append(" AND ");
		}
		sb.append(colID);
		sb.append(" IN (");
		for (int i = 0; i < vals.size(); i++) {
			if (i > 0)
				sb.append(',');
			sb.append("'");
			sb.append(vals.get(i).trim());
			sb.append("'");
		}
		sb.append(") ");
	}

}
