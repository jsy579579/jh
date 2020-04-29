package com.jh.mircomall.bean;

import java.io.Serializable;
import java.util.Date;

public class BusinessOrder implements Serializable {

	private static final long serialVersionUID = 1L;

	private Integer id;

	private String orderCode;

	private Integer goodsId;

	private Integer userId;

	private Integer businessId;

	private Integer logisticsId;

	private String logisticsNum;

	private Date createTime;

	private Date changeTime;

	private Integer consigneeId;

	private String outOfPocket;

	private String goodsLogo;

	private String goodsUrl;

	private Integer status;

	private Integer isDelete;
	// 用户
	private User user;
	// 商品
	private Goods goods;
	// 商户
	private Business business;
	// 地址
	private ConsigneeAddress consigneeAddress;

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public Goods getGoods() {
		return goods;
	}

	public void setGoods(Goods goods) {
		this.goods = goods;
	}

	public Business getBusiness() {
		return business;
	}

	public void setBusiness(Business business) {
		this.business = business;
	}

	public ConsigneeAddress getConsigneeAddress() {
		return consigneeAddress;
	}

	public void setConsigneeAddress(ConsigneeAddress consigneeAddress) {
		this.consigneeAddress = consigneeAddress;
	}

	public Integer getIsDelete() {
		return isDelete;
	}

	public void setIsDelete(Integer isDelete) {
		this.isDelete = isDelete;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getOrderCode() {
		return orderCode;
	}

	public void setOrderCode(String orderCode) {
		this.orderCode = orderCode == null ? null : orderCode.trim();
	}

	public Integer getGoodsId() {
		return goodsId;
	}

	public void setGoodsId(Integer goodsId) {
		this.goodsId = goodsId;
	}

	public Integer getUserId() {
		return userId;
	}

	public void setUserId(Integer userId) {
		this.userId = userId;
	}

	public Integer getBusinessId() {
		return businessId;
	}

	public void setBusinessId(Integer businessId) {
		this.businessId = businessId;
	}

	public Integer getLogisticsId() {
		return logisticsId;
	}

	public void setLogisticsId(Integer logisticsId) {
		this.logisticsId = logisticsId;
	}

	public String getLogisticsNum() {
		return logisticsNum;
	}

	public void setLogisticsNum(String logisticsNum) {
		this.logisticsNum = logisticsNum == null ? null : logisticsNum.trim();
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

	public Integer getConsigneeId() {
		return consigneeId;
	}

	public void setConsigneeId(Integer consigneeId) {
		this.consigneeId = consigneeId;
	}

	public String getOutOfPocket() {
		return outOfPocket;
	}

	public void setOutOfPocket(String outOfPocket) {
		this.outOfPocket = outOfPocket == null ? null : outOfPocket.trim();
	}

	public String getGoodsLogo() {
		return goodsLogo;
	}

	public void setGoodsLogo(String goodsLogo) {
		this.goodsLogo = goodsLogo == null ? null : goodsLogo.trim();
	}

	public String getGoodsUrl() {
		return goodsUrl;
	}

	public void setGoodsUrl(String goodsUrl) {
		this.goodsUrl = goodsUrl == null ? null : goodsUrl.trim();
	}

	public Integer getStatus() {
		return status;
	}

	public void setStatus(Integer status) {
		this.status = status;
	}
}