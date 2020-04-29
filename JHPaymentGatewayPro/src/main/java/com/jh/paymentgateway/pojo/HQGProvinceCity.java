package com.jh.paymentgateway.pojo;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonFormat;

@Entity
@Table(name = "t_hqg_province_city")
public class HQGProvinceCity implements Serializable {
	private static final long serialVersionUID = 114L;

	@Id
	@Column(name = "id")
	private long id;

	@Column(name = "value")
	private String value;

	@Column(name = "type")
	private String type;

	@Column(name = "code")
	private String code;

	@Column(name = "name")
	private String name;
	
	@Column(name = "city_code")
	private String cityCode;
	
	@Column(name = "city_name")
	private String cityName;
	
	@Column(name = "province_code")
	private String provinceCode;
	
	@Column(name = "province_name")
	private String provinceName;
	
	@Column(name = "hk_city_code")
	private String hkCityCode;
	
	@Column(name = "hk_city_name")
	private String hkCityName;
	
	@Column(name = "hk_province_code")
	private String hkProvinceCode;
	
	@Column(name = "hk_province_name")
	private String hkProvinceName;
	
	@Column(name = "hk_areacode")
	private String hkAreacode;

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getCityCode() {
		return cityCode;
	}

	public void setCityCode(String cityCode) {
		this.cityCode = cityCode;
	}

	public String getCityName() {
		return cityName;
	}

	public void setCityName(String cityName) {
		this.cityName = cityName;
	}

	public String getProvinceCode() {
		return provinceCode;
	}

	public void setProvinceCode(String provinceCode) {
		this.provinceCode = provinceCode;
	}

	public String getProvinceName() {
		return provinceName;
	}

	public void setProvinceName(String provinceName) {
		this.provinceName = provinceName;
	}

	public String getHkCityCode() {
		return hkCityCode;
	}

	public void setHkCityCode(String hkCityCode) {
		this.hkCityCode = hkCityCode;
	}

	public String getHkCityName() {
		return hkCityName;
	}

	public void setHkCityName(String hkCityName) {
		this.hkCityName = hkCityName;
	}

	public String getHkProvinceCode() {
		return hkProvinceCode;
	}

	public void setHkProvinceCode(String hkProvinceCode) {
		this.hkProvinceCode = hkProvinceCode;
	}

	public String getHkProvinceName() {
		return hkProvinceName;
	}

	public void setHkProvinceName(String hkProvinceName) {
		this.hkProvinceName = hkProvinceName;
	}

	public String getHkAreacode() {
		return hkAreacode;
	}

	public void setHkAreacode(String hkAreacode) {
		this.hkAreacode = hkAreacode;
	}
	


}
