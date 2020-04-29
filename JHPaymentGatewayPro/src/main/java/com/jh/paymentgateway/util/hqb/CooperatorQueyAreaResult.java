package com.jh.paymentgateway.util.hqb;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
@JsonIgnoreProperties(ignoreUnknown = true)
public class CooperatorQueyAreaResult {

	private List<AreaInfo> province;
	private List<AreaInfo> city;
	public List<AreaInfo> getProvince() {
		return province;
	}
	public void setProvince(List<AreaInfo> province) {
		this.province = province;
	}
	public List<AreaInfo> getCity() {
		return city;
	}
	public void setCity(List<AreaInfo> city) {
		this.city = city;
	}
	
	
}
