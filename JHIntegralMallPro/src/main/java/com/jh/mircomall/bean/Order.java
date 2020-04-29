package com.jh.mircomall.bean;

import java.io.Serializable;
import java.util.Date;

public class Order implements Serializable {

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

	private Integer shipperId;

	public Integer getShipperId() {
		return shipperId;
	}

	public void setShipperId(Integer shipperId) {
		this.shipperId = shipperId;
	}

	// 商品
	private Goods goods;
	// 商户
	private Business business;
	// 地址
	private ConsigneeAddress consigneeAddress;

	private User user;

	private Provinces provinces;

	private Areas areas;

	private ShipperCode shipperCode;

	private Groups groups;

	public Groups getGroups() {
		return groups;
	}

	public void setGroups(Groups groups) {
		this.groups = groups;
	}

	public ShipperCode getShipperCode() {
		return shipperCode;
	}

	public void setShipperCode(ShipperCode shipperCode) {
		this.shipperCode = shipperCode;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public Provinces getProvinces() {
		return provinces;
	}

	public void setProvinces(Provinces provinces) {
		this.provinces = provinces;
	}

	public Areas getAreas() {
		return areas;
	}

	public void setAreas(Areas areas) {
		this.areas = areas;
	}

	public Cities getCities() {
		return cities;
	}

	public void setCities(Cities cities) {
		this.cities = cities;
	}

	private Cities cities;
	// 物流轨迹
	private String track;
	// 订单购买的商品数量
	private Integer goodsNum;

	public Integer getGoodsNum() {
		return goodsNum;
	}

	public void setGoodsNum(Integer goodsNum) {
		this.goodsNum = goodsNum;
	}

	public String getTrack() {
		return track;
	}

	public void setTrack(String track) {
		this.track = track;
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
		this.orderCode = orderCode;
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
		this.logisticsNum = logisticsNum;
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
		this.outOfPocket = outOfPocket;
	}

	public String getGoodsLogo() {
		return goodsLogo;
	}

	public void setGoodsLogo(String goodsLogo) {
		this.goodsLogo = goodsLogo;
	}

	public String getGoodsUrl() {
		return goodsUrl;
	}

	public void setGoodsUrl(String goodsUrl) {
		this.goodsUrl = goodsUrl;
	}

	public Integer getStatus() {
		return status;
	}

	public void setStatus(Integer status) {
		this.status = status;
	}

	public Integer getIsDelete() {
		return isDelete;
	}

	public void setIsDelete(Integer isDelete) {
		this.isDelete = isDelete;
	}

}