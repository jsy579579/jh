package com.jh.mircomall.bean;

import java.io.Serializable;
import java.util.Date;

public class GoodsParent implements Serializable {

	private static final long serialVersionUID = 1L;

	private Integer id;

	private String goodsParentName;

	private Date createTime;

	private Date changeTime;

	private Integer status;

	private Integer businessId;

	public Integer getBusinessId() {
		return businessId;
	}

	public void setBusinessId(Integer businessId) {
		this.businessId = businessId;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getGoodsParentName() {
		return goodsParentName;
	}

	public void setGoodsParentName(String goodsParentName) {
		this.goodsParentName = goodsParentName == null ? null : goodsParentName.trim();
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

	public Integer getStatus() {
		return status;
	}

	public void setStatus(Integer status) {
		this.status = status;
	}
}