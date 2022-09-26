package com.mnao.mfp.common.db;

import java.io.Serializable;

public class KPIQuerySpecs implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 3715428597170253029L;
	private String kpi;
	private String kpiMetric;
	private String sqlQueryFile;
	private String baseTable;
	//
	//
	public String getKpi() {
		return kpi;
	}
	public void setKpi(String kpi) {
		this.kpi = kpi;
	}
	public String getKpiMetric() {
		return kpiMetric;
	}
	public void setKpiMetric(String kpiMetric) {
		this.kpiMetric = kpiMetric;
	}
	public String getSqlQueryFile() {
		return sqlQueryFile;
	}
	public void setSqlQueryFile(String sqlQueryFile) {
		this.sqlQueryFile = sqlQueryFile;
	}
	public String getBaseTable() {
		return baseTable;
	}
	public void setBaseTable(String baseTable) {
		this.baseTable = baseTable;
	}
	@Override
	public String toString() {
		return "KPIQuerySpecs [kpi=" + kpi + ", kpiMetric=" + kpiMetric + ", sqlQueryFile=" + sqlQueryFile
				+ ", baseTable=" + baseTable + "]";
	}
}
