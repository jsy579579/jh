package com.jh.mircomall.bean;

import java.io.Serializable;
import java.util.Date;

public class LogisticsInfo implements Serializable{

	private static final long serialVersionUID = 1L;

	private Integer id;

    private Integer goodsId;

    private String orderId;

    private String logisticsName;

    private String logisticsNum;

    private Integer userId;

    private String userAddr;

    private String userPhone;

    private String userProvinceId;

    private String userCityId;

    private String userAreasId;

    private String businessName;

    private String businessPhone;

    private String businessProvinceId;

    private String businessCityId;

    private String businessAreasId;

    private Date createTime;

    private Date changeTime;

    private Integer status;

    private Integer isDelete;
    //物流公司集Id
    private Integer shipperCodeId;
    
    
    public Integer getShipperCodeId() {
		return shipperCodeId;
	}

	public void setShipperCodeId(Integer shipperCodeId) {
		this.shipperCodeId = shipperCodeId;
	}

	public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getGoodsId() {
        return goodsId;
    }

    public void setGoodsId(Integer goodsId) {
        this.goodsId = goodsId;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId == null ? null : orderId.trim();
    }

    public String getLogisticsName() {
        return logisticsName;
    }

    public void setLogisticsName(String logisticsName) {
        this.logisticsName = logisticsName == null ? null : logisticsName.trim();
    }

    public String getLogisticsNum() {
        return logisticsNum;
    }

    public void setLogisticsNum(String logisticsNum) {
        this.logisticsNum = logisticsNum == null ? null : logisticsNum.trim();
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public String getUserAddr() {
        return userAddr;
    }

    public void setUserAddr(String userAddr) {
        this.userAddr = userAddr == null ? null : userAddr.trim();
    }

    public String getUserPhone() {
        return userPhone;
    }

    public void setUserPhone(String userPhone) {
        this.userPhone = userPhone == null ? null : userPhone.trim();
    }

    public String getUserProvinceId() {
        return userProvinceId;
    }

    public void setUserProvinceId(String userProvinceId) {
        this.userProvinceId = userProvinceId == null ? null : userProvinceId.trim();
    }

    public String getUserCityId() {
        return userCityId;
    }

    public void setUserCityId(String userCityId) {
        this.userCityId = userCityId == null ? null : userCityId.trim();
    }

    public String getUserAreasId() {
        return userAreasId;
    }

    public void setUserAreasId(String userAreasId) {
        this.userAreasId = userAreasId == null ? null : userAreasId.trim();
    }

    public String getBusinessName() {
        return businessName;
    }

    public void setBusinessName(String businessName) {
        this.businessName = businessName == null ? null : businessName.trim();
    }

    public String getBusinessPhone() {
        return businessPhone;
    }

    public void setBusinessPhone(String businessPhone) {
        this.businessPhone = businessPhone == null ? null : businessPhone.trim();
    }

    public String getBusinessProvinceId() {
        return businessProvinceId;
    }

    public void setBusinessProvinceId(String businessProvinceId) {
        this.businessProvinceId = businessProvinceId == null ? null : businessProvinceId.trim();
    }

    public String getBusinessCityId() {
        return businessCityId;
    }

    public void setBusinessCityId(String businessCityId) {
        this.businessCityId = businessCityId == null ? null : businessCityId.trim();
    }

    public String getBusinessAreasId() {
        return businessAreasId;
    }

    public void setBusinessAreasId(String businessAreasId) {
        this.businessAreasId = businessAreasId == null ? null : businessAreasId.trim();
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

    public Integer getIsDelete() {
        return isDelete;
    }

    public void setIsDelete(Integer isDelete) {
        this.isDelete = isDelete;
    }
}