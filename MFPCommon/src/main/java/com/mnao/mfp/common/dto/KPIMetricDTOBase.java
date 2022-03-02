package com.mnao.mfp.common.dto;

public class KPIMetricDTOBase {
	private boolean success = true;
	private String error = "";
	private boolean pageable = false;
	private boolean sortable = false;
	private int currentPage = 0;
	private String lastUpdateDate;
	//
	public boolean isSuccess() {
		return success;
	}
	public void setSuccess(boolean success) {
		this.success = success;
	}
	public String getError() {
		return error;
	}
	public void setError(String error) {
		this.error = error;
	}
	public boolean isPageable() {
		return pageable;
	}
	public void setPageable(boolean pageable) {
		this.pageable = pageable;
	}
	public boolean isSortable() {
		return sortable;
	}
	public void setSortable(boolean sortable) {
		this.sortable = sortable;
	}
	public int getCurrentPage() {
		return currentPage;
	}
	public void setCurrentPage(int currentPage) {
		this.currentPage = currentPage;
	}
	public String getLastUpdateDate() {
		return lastUpdateDate;
	}
	public void setLastUpdateDate(String lastUpdateDate) {
		this.lastUpdateDate = lastUpdateDate;
	}
	
}
