package com.jh.mircomall.bean;

import java.io.Serializable;
import java.util.Date;

import org.springframework.format.annotation.DateTimeFormat;

import com.fasterxml.jackson.annotation.JsonFormat;

public class ConsigneeAddress implements Serializable {

	private static final long serialVersionUID = 1L;

	private Integer id;

	private Integer userId;

	private String provinceid;

	private String cityid;

	private String areaid;

	private String detailedAddr;

	private String consigneePhone;

	private Integer defaultAddr;
	
	private Integer isDelete;

	public Integer getIsDelete() {
		return isDelete;
	}

	public void setIsDelete(Integer isDelete) {
		this.isDelete = isDelete;
	}

	private Date createTime;

	private Date changeTime;

	private String consigneeName;

	public Provinces getProvinces() {
		return provinces;
	}

	public void setProvinces(Provinces provinces) {
		this.provinces = provinces;
	}

	public Cities getCities() {
		return cities;
	}

	public void setCities(Cities cities) {
		this.cities = cities;
	}

	public Areas getAreas() {
		return areas;
	}

	public void setAreas(Areas areas) {
		this.areas = areas;
	}

	private Provinces provinces;

	private Cities cities;

	private Areas areas;

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public Integer getUserId() {
		return userId;
	}

	public void setUserId(Integer userId) {
		this.userId = userId;
	}

	public String getProvinceid() {
		return provinceid;
	}

	public void setProvinceid(String provinceid) {
		this.provinceid = provinceid == null ? null : provinceid.trim();
	}

	public String getCityid() {
		return cityid;
	}

	public void setCityid(String cityid) {
		this.cityid = cityid == null ? null : cityid.trim();
	}

	public String getAreaid() {
		return areaid;
	}

	public void setAreaid(String areaid) {
		this.areaid = areaid == null ? null : areaid.trim();
	}

	public String getDetailedAddr() {
		return detailedAddr;
	}

	public void setDetailedAddr(String detailedAddr) {
		this.detailedAddr = detailedAddr == null ? null : detailedAddr.trim();
	}

	public String getConsigneePhone() {
		return consigneePhone;
	}

	public void setConsigneePhone(String consigneePhone) {
		this.consigneePhone = consigneePhone == null ? null : consigneePhone.trim();
	}

	public Integer getDefaultAddr() {
		return defaultAddr;
	}

	public void setDefaultAddr(Integer defaultAddr) {
		this.defaultAddr = defaultAddr;
	}

	public Date getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}

	public Date getChangeTime() {
		return changeTime;
	}

	public void setChangeTime(Date changeTime) {
		this.changeTime = changeTime;
	}

	public String getConsigneeName() {
		return consigneeName;
	}

	public void setConsigneeName(String consigneeName) {
		this.consigneeName = consigneeName == null ? null : consigneeName.trim();
	}
}