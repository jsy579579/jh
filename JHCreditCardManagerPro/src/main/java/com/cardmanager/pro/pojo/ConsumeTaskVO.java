package com.cardmanager.pro.pojo;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Random;

public class ConsumeTaskVO implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8984205670798467772L;
	
//	private static final String[] consumeTypeName = {"娱乐","其他","通信","交通","住宿","餐饮"};
	
//	public ConsumeTaskVO(){
//		this.consumeType = consumeTypeName[new Random().nextInt(consumeTypeName.length)];
//	}
	
	private String userId;
	
	private String repaymentTaskId;
	
	private String creditCardNumber;
	
	private String consumeTaskId;
	
	private BigDecimal amount = BigDecimal.ZERO;
	
	private BigDecimal realAmount = BigDecimal.ZERO;
	
	private BigDecimal serviceCharge = BigDecimal.ZERO;
	
	private String channelId;
	
	private String channelTag;
	
	private String consumeType;
	
	private String description;
	
	private String executeDate;
	
	private String executeDateTime;
	
	private String createTime;

	public String getRepaymentTaskId() {
		return repaymentTaskId;
	}

	public void setRepaymentTaskId(String repaymentTaskId) {
		this.repaymentTaskId = repaymentTaskId;
	}

	public String getConsumeTaskId() {
		return consumeTaskId;
	}

	public void setConsumeTaskId(String consumeTaskId) {
		this.consumeTaskId = consumeTaskId;
	}

	public BigDecimal getAmount() {
		return amount;
	}

	public void setAmount(BigDecimal amount) {
		this.amount = amount;
	}

	public String getExecuteDate() {
		return executeDate;
	}

	public void setExecuteDate(String executeDate) {
		this.executeDate = executeDate;
	}

	public String getCreateTime() {
		return createTime;
	}

	public void setCreateTime(String createTime) {
		this.createTime = createTime;
	}

	public String getExecuteDateTime() {
		return executeDateTime;
	}

	public void setExecuteDateTime(String executeDateTime) {
		this.executeDateTime = executeDateTime;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public BigDecimal getRealAmount() {
		return realAmount;
	}

	public void setRealAmount(BigDecimal realAmount) {
		this.realAmount = realAmount;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getCreditCardNumber() {
		return creditCardNumber;
	}

	public void setCreditCardNumber(String creditCardNumber) {
		this.creditCardNumber = creditCardNumber;
	}

	public BigDecimal getServiceCharge() {
		return serviceCharge;
	}

	public void setServiceCharge(BigDecimal serviceCharge) {
		this.serviceCharge = serviceCharge;
	}

	public static long getSerialversionuid() {
		return serialVersionUID;
	}

	public String getConsumeType() {
		return consumeType;
	}

	public void setConsumeType(String consumeType) {
		this.consumeType = consumeType;
	}

	public String getChannelId() {
		return channelId;
	}

	public void setChannelId(String channelId) {
		this.channelId = channelId;
	}

	public String getChannelTag() {
		return channelTag;
	}

	public void setChannelTag(String channelTag) {
		this.channelTag = channelTag;
	}

	@Override
	public String toString() {
		return "ConsumeTaskVO [userId=" + userId + ", repaymentTaskId=" + repaymentTaskId + ", creditCardNumber="
				+ creditCardNumber + ", consumeTaskId=" + consumeTaskId + ", amount=" + amount + ", realAmount="
				+ realAmount + ", serviceCharge=" + serviceCharge + ", channelId=" + channelId + ", channelTag="
				+ channelTag + ", consumeType=" + consumeType + ", description=" + description + ", executeDate="
				+ executeDate + ", executeDateTime=" + executeDateTime + ", createTime=" + createTime + "]";
	}
}
