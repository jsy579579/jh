package com.jh.paymentgateway.pojo;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "t_hqe_region")
public class HQERegion implements Serializable {
	private static final long serialVersionUID = 114L;

	@Id
	@Column(name = "id")
	private long id;

	@Column(name = "region_id")
	private String regionId;

	@Column(name = "region_code")
	private String regionCode;

	@Column(name = "region_name")
	private String regionName;

	@Column(name = "parent_id")
	private String parentId;
	
	@Column(name = "region_type")
	private String regionType;

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getRegionId() {
		return regionId;
	}

	public void setRegionId(String regionId) {
		this.regionId = regionId;
	}

	public String getRegionCode() {
		return regionCode;
	}

	public void setRegionCode(String regionCode) {
		this.regionCode = regionCode;
	}

	public String getRegionName() {
		return regionName;
	}

	public void setRegionName(String regionName) {
		this.regionName = regionName;
	}

	public String getParentId() {
		return parentId;
	}

	public void setParentId(String parentId) {
		this.parentId = parentId;
	}

	public String getRegionType() {
		return regionType;
	}

	public void setRegionType(String regionType) {
		this.regionType = regionType;
	}

	@Override
	public String toString() {
		return "HQERegion [id=" + id + ", regionId=" + regionId + ", regionCode=" + regionCode + ", regionName="
				+ regionName + ", parentId=" + parentId + ", regionType=" + regionType + "]";
	}
	
	

}
