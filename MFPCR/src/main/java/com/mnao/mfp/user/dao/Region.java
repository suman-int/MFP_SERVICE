package com.mnao.mfp.user.dao;

public class Region {
	private String code;
	private String regionName;
	private String domainCountry;
	public String getCode() {
		return code;
	}
	public void setCode(String code) {
		this.code = code;
	}
	public String getRegionName() {
		return regionName;
	}
	public void setRegionName(String regionName) {
		this.regionName = regionName;
	}
	public String getDomainCountry() {
		return domainCountry;
	}
	public void setDomainCountry(String domainCountry) {
		this.domainCountry = domainCountry;
	}
	@Override
	public String toString() {
		return "Region [code=" + code + ", regionName=" + regionName + ", domainCountry=" + domainCountry + "]";
	}
	
}
