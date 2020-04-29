package com.jh.mircomall.bean;

import java.io.Serializable;
import java.util.Date;

public class Groups implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private Integer id;
	private String groupsName;
	private String brandId;
	private Date createTime;
	private Date changeTime;
	private String status;
	
	public Integer getId() {
		return id;
	}
	public void setId(Integer id) {
		this.id = id;
	}
	public String getBrandId() {
		return brandId;
	}
	public void setBrandId(String brandId) {
		this.brandId = brandId;
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
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public String getGroupsName() {
		return groupsName;
	}
	public void setGroupsName(String groupsName) {
		this.groupsName = groupsName;
	}

}
