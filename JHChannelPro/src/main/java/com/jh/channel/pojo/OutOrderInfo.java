package com.jh.channel.pojo;

import java.io.Serializable;

public class OutOrderInfo implements Serializable{


	private static final long serialVersionUID = 1L;

	
	private String sysOrdercode;
	
	
	private String orderDesc;
	
	private String amount;
	
	private String merNo;
	
	private String rate;
	
	private String extraFee;
	
	private String realAmount;
	
	private String orderCode;
	
	private String orderType;
	
	private String channel;
	
	
	private String orderStatus;
	
	private String time;

	public String getSysOrdercode() {
		return sysOrdercode;
	}

	public void setSysOrdercode(String sysOrdercode) {
		this.sysOrdercode = sysOrdercode;
	}

	public String getOrderDesc() {
		return orderDesc;
	}

	public void setOrderDesc(String orderDesc) {
		this.orderDesc = orderDesc;
	}

	public String getAmount() {
		return amount;
	}

	public void setAmount(String amount) {
		this.amount = amount;
	}

	public String getMerNo() {
		return merNo;
	}

	public void setMerNo(String merNo) {
		this.merNo = merNo;
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

	public String getRealAmount() {
		return realAmount;
	}

	public void setRealAmount(String realAmount) {
		this.realAmount = realAmount;
	}

	public String getOrderType() {
		return orderType;
	}

	public void setOrderType(String orderType) {
		this.orderType = orderType;
	}

	public String getChannel() {
		return channel;
	}

	public void setChannel(String channel) {
		this.channel = channel;
	}

	public String getOrderStatus() {
		return orderStatus;
	}

	public void setOrderStatus(String orderStatus) {
		this.orderStatus = orderStatus;
	}

	public String getTime() {
		return time;
	}

	public void setTime(String time) {
		this.time = time;
	}

	public String getOrderCode() {
		return orderCode;
	}

	public void setOrderCode(String orderCode) {
		this.orderCode = orderCode;
	}
	
	
	
}
