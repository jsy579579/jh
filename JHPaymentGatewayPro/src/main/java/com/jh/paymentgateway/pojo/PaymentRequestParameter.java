package com.jh.paymentgateway.pojo;

import java.io.Serializable;

public class PaymentRequestParameter implements Serializable{

	private static final long serialVersionUID = 1L;
	//订单号
	private String  orderCode;
	//金额
	private String  amount;
	//
	private String  bankCard;
	//实际金额
	private String  realAmount;
	//用户id
	private String  userId;
	//费率
	private String  rate;
	//额外手续费
	private String  extraFee;
   //用户名
	private String  userName;
	//身份证
	private String  idCard;
	//信用卡电话
	private String  creditCardPhone;
	//信用卡银行名
	private String  creditCardBankName;
	
	private String  creditCardNature;
	//信用卡类型
	private String  creditCardCardType;
	//过期时间
	private String  expiredTime;
	//安全码
	private String  securityCode;
	//储蓄卡号
	private String  debitCardNo;
	//储蓄卡手机号
	private String  debitPhone;
    //储蓄卡银行名
	private String  debitBankName;
	//
	private String  debitCardNature;
	//储蓄卡类型
	private String  debitCardCardType;
	//通道类型
	private String  channelTag;
	//订单类型
	private String  orderType;
	//消费地区
	private String  extra;
	//电话
	private String phone;
	//请求ip地址
	private String  ipAddress;

	public String getOrderCode() {
		return orderCode;
	}

	public void setOrderCode(String orderCode) {
		this.orderCode = orderCode;
	}

	public String getAmount() {
		return amount;
	}

	public void setAmount(String amount) {
		this.amount = amount;
	}

	public String getBankCard() {
		return bankCard;
	}

	public void setBankCard(String bankCard) {
		this.bankCard = bankCard;
	}

	public String getRealAmount() {
		return realAmount;
	}

	public void setRealAmount(String realAmount) {
		this.realAmount = realAmount;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getRate() {
		return rate;
	}

	public void setRate(String rate) {
		this.rate = rate;
	}

	public String getExtraFee() {
		return extraFee;
	}

	public void setExtraFee(String extraFee) {
		this.extraFee = extraFee;
	}


	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getIdCard() {
		return idCard;
	}

	public void setIdCard(String idCard) {
		this.idCard = idCard;
	}

	public String getCreditCardPhone() {
		return creditCardPhone;
	}

	public void setCreditCardPhone(String creditCardPhone) {
		this.creditCardPhone = creditCardPhone;
	}

	public String getCreditCardBankName() {
		return creditCardBankName;
	}

	public void setCreditCardBankName(String creditCardBankName) {
		this.creditCardBankName = creditCardBankName;
	}

	public String getCreditCardNature() {
		return creditCardNature;
	}

	public void setCreditCardNature(String creditCardNature) {
		this.creditCardNature = creditCardNature;
	}

	public String getCreditCardCardType() {
		return creditCardCardType;
	}

	public void setCreditCardCardType(String creditCardCardType) {
		this.creditCardCardType = creditCardCardType;
	}

	public String getExpiredTime() {
		return expiredTime;
	}

	public void setExpiredTime(String expiredTime) {
		this.expiredTime = expiredTime;
	}

	public String getSecurityCode() {
		return securityCode;
	}

	public void setSecurityCode(String securityCode) {
		this.securityCode = securityCode;
	}

	public String getDebitCardNo() {
		return debitCardNo;
	}

	public void setDebitCardNo(String debitCardNo) {
		this.debitCardNo = debitCardNo;
	}

	public String getDebitPhone() {
		return debitPhone;
	}

	public void setDebitPhone(String debitPhone) {
		this.debitPhone = debitPhone;
	}

	public String getDebitBankName() {
		return debitBankName;
	}

	public void setDebitBankName(String debitBankName) {
		this.debitBankName = debitBankName;
	}

	public String getDebitCardNature() {
		return debitCardNature;
	}

	public void setDebitCardNature(String debitCardNature) {
		this.debitCardNature = debitCardNature;
	}

	public String getDebitCardCardType() {
		return debitCardCardType;
	}

	public void setDebitCardCardType(String debitCardCardType) {
		this.debitCardCardType = debitCardCardType;
	}

	public String getChannelTag() {
		return channelTag;
	}

	public void setChannelTag(String channelTag) {
		this.channelTag = channelTag;
	}

	public String getIpAddress() {
		return ipAddress;
	}

	public void setIpAddress(String ipAddress) {
		this.ipAddress = ipAddress;
	}

	public String getOrderType() {
		return orderType;
	}

	public void setOrderType(String orderType) {
		this.orderType = orderType;
	}

	public String getExtra() {
		return extra;
	}

	public void setExtra(String extra) {
		this.extra = extra;
	}

	public static long getSerialversionuid() {
		return serialVersionUID;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	@Override
	public String toString() {
		return "PaymentRequestParameter [orderCode=" + orderCode + ", amount=" + amount + ", bankCard=" + bankCard
				+ ", realAmount=" + realAmount + ", userId=" + userId + ", rate=" + rate + ", extraFee=" + extraFee
				+ ", userName=" + userName + ", idCard=" + idCard + ", creditCardPhone=" + creditCardPhone
				+ ", creditCardBankName=" + creditCardBankName + ", creditCardNature=" + creditCardNature
				+ ", creditCardCardType=" + creditCardCardType + ", expiredTime=" + expiredTime + ", securityCode="
				+ securityCode + ", debitCardNo=" + debitCardNo + ", debitPhone=" + debitPhone + ", debitBankName="
				+ debitBankName + ", debitCardNature=" + debitCardNature + ", debitCardCardType=" + debitCardCardType
				+ ", channelTag=" + channelTag + ", orderType=" + orderType + ", extra=" + extra + ", phone=" + phone
				+ ", ipAddress=" + ipAddress + "]";
	}

	
	

	
	
}
