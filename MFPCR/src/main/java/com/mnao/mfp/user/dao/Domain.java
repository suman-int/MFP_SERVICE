package com.mnao.mfp.user.dao;

import java.util.ArrayList;

public class Domain {
	private Region region;
	private Zone zone;
	private District district;
	private ArrayList<String> regions;
	private ArrayList<String> districts;
	private ArrayList<String> zones;
	public Region getRegion() {
		return region;
	}
	public void setRegion(Region region) {
		this.region = region;
	}
	public Zone getZone() {
		return zone;
	}
	public void setZone(Zone zone) {
		this.zone = zone;
	}
	public District getDistrict() {
		return district;
	}
	public void setDistrict(District district) {
		this.district = district;
	}
	public ArrayList<String> getRegions() {
		return regions;
	}
	public void setRegions(ArrayList<String> regions) {
		this.regions = regions;
	}
	public ArrayList<String> getDistricts() {
		return districts;
	}
	public void setDistricts(ArrayList<String> districts) {
		this.districts = districts;
	}
	public ArrayList<String> getZones() {
		return zones;
	}
	public void setZones(ArrayList<String> zones) {
		this.zones = zones;
	}
	@Override
	public String toString() {
		return "Domain [region=" + region + ", zone=" + zone + ", district=" + district + ", regions=" + regions
				+ ", districts=" + districts + ", zones=" + zones + "]";
	}
}
