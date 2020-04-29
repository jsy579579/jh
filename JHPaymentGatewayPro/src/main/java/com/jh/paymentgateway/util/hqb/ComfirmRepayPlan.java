package com.jh.paymentgateway.util.hqb;

import java.util.List;

public class ComfirmRepayPlan {
	private String idCard;
	private String dateTime;
	private String joinType;
	private String callBackUrl;

	private String repayPlanJson;
	private String bankcardNumb;
	private String bankcardName;
	private String bankcardCode;
	private String userOrderId;
	private String tieCardId;
	private String channelType;
	private String provName;
	private String cityName;
	private String repayModeFlag;

//	private String tradeTime;
//	private String transferTime;
//	private String tradeMoney;
//	private String transferMoney;
//	private String rateMoney;
//	private String repayOrderId;
//	private String transferRepayOrderId;
//	private String repayOrderFlag;
	
	List<RepayPlanList> planList;

	private String mobile;
	private String longitude;
	private String latitude;
	private String rate;
	private String cost;
	private String deviceId;
	


	public String getIdCard() {
		return idCard;
	}

	public void setIdCard(String idCard) {
		this.idCard = idCard;
	}

	public String getDateTime() {
		return dateTime;
	}

	public void setDateTime(String dateTime) {
		this.dateTime = dateTime;
	}

	public String getJoinType() {
		return joinType;
	}

	public void setJoinType(String joinType) {
		this.joinType = joinType;
	}

	public String getCallBackUrl() {
		return callBackUrl;
	}

	public void setCallBackUrl(String callBackUrl) {
		this.callBackUrl = callBackUrl;
	}

	public String getRepayPlanJson() {
		return repayPlanJson;
	}

	public void setRepayPlanJson(String repayPlanJson) {
		this.repayPlanJson = repayPlanJson;
	}

	public String getBankcardNumb() {
		return bankcardNumb;
	}

	public void setBankcardNumb(String bankcardNumb) {
		this.bankcardNumb = bankcardNumb;
	}

	public String getBankcardName() {
		return bankcardName;
	}

	public void setBankcardName(String bankcardName) {
		this.bankcardName = bankcardName;
	}

	public String getBankcardCode() {
		return bankcardCode;
	}

	public void setBankcardCode(String bankcardCode) {
		this.bankcardCode = bankcardCode;
	}

	public String getUserOrderId() {
		return userOrderId;
	}

	public void setUserOrderId(String userOrderId) {
		this.userOrderId = userOrderId;
	}

	public String getTieCardId() {
		return tieCardId;
	}

	public void setTieCardId(String tieCardId) {
		this.tieCardId = tieCardId;
	}

	public String getChannelType() {
		return channelType;
	}

	public void setChannelType(String channelType) {
		this.channelType = channelType;
	}

	public String getProvName() {
		return provName;
	}

	public void setProvName(String provName) {
		this.provName = provName;
	}

	public String getCityName() {
		return cityName;
	}

	public void setCityName(String cityName) {
		this.cityName = cityName;
	}

	public String getRepayModeFlag() {
		return repayModeFlag;
	}

	public void setRepayModeFlag(String repayModeFlag) {
		this.repayModeFlag = repayModeFlag;
	}


	public String getMobile() {
		return mobile;
	}

	public void setMobile(String mobile) {
		this.mobile = mobile;
	}

	public String getLongitude() {
		return longitude;
	}

	public void setLongitude(String longitude) {
		this.longitude = longitude;
	}

	public String getLatitude() {
		return latitude;
	}

	public void setLatitude(String latitude) {
		this.latitude = latitude;
	}

	public String getRate() {
		return rate;
	}

	public void setRate(String rate) {
		this.rate = rate;
	}

	public String getCost() {
		return cost;
	}

	public void setCost(String cost) {
		this.cost = cost;
	}

	public String getDeviceId() {
		return deviceId;
	}

	public void setDeviceId(String deviceId) {
		this.deviceId = deviceId;
	}

	public List<RepayPlanList> getPlanList() {
		return planList;
	}

	public void setPlanList(List<RepayPlanList> planList) {
		this.planList = planList;
	}

	
}
