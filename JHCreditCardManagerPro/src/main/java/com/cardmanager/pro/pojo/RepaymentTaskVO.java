package com.cardmanager.pro.pojo;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import cn.jh.common.utils.DateUtil;

public class RepaymentTaskVO implements  Comparable<RepaymentTaskVO> {
	

	private String userId;
	
	private String repaymentTaskId;
	
	private String creditCardNumber;
	
	private BigDecimal amount = BigDecimal.ZERO;
	
	private BigDecimal rate = BigDecimal.ZERO;
	
	private BigDecimal serviceCharge = BigDecimal.ZERO;
	
	private BigDecimal totalServiceCharge = BigDecimal.ZERO;
	
	private String channelId;
	
	private String channelTag;
	
	private String executeDate;
	
	private String executeDateTime;
	
	private String description;

//	private ConsumeTaskVO[] consumeTaskVOs = new ConsumeTaskVO[index];
	
	private List<ConsumeTaskVO> consumeTaskVOs = new ArrayList<ConsumeTaskVO>();
	
	private String createTime;

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

	public String getRepaymentTaskId() {
		return repaymentTaskId;
	}

	public void setRepaymentTaskId(String repaymentTaskId) {
		this.repaymentTaskId = repaymentTaskId;
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

	public BigDecimal getServiceCharge() {
		return serviceCharge;
	}

	public void setServiceCharge(BigDecimal serviceCharge) {
		this.serviceCharge = serviceCharge;
	}

	public BigDecimal getTotalServiceCharge() {
		return totalServiceCharge;
	}

	public void setTotalServiceCharge(BigDecimal totalServiceCharge) {
		this.totalServiceCharge = totalServiceCharge;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public BigDecimal getRate() {
		return rate;
	}

	public void setRate(BigDecimal rate) {
		this.rate = rate;
	}

	public String getCreditCardNumber() {
		return creditCardNumber;
	}

	public void setCreditCardNumber(String creditCardNumber) {
		this.creditCardNumber = creditCardNumber;
	}

	public List<ConsumeTaskVO> getConsumeTaskVOs() {
		return consumeTaskVOs;
	}

	public void setConsumeTaskVOs(List<ConsumeTaskVO> consumeTaskVOs) {
		this.consumeTaskVOs = consumeTaskVOs;
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
	public int compareTo(RepaymentTaskVO o) {
		Date dateTime1 = DateUtil.getDateStringConvert(new Date(), this.executeDateTime, "yyyy-MM-dd HH:mm:ss");
		Date dateTime2 = DateUtil.getDateStringConvert(new Date(), o.getExecuteDateTime(), "yyyy-MM-dd HH:mm:ss");
		return dateTime1.compareTo(dateTime2);
	}

	@Override
	public String toString() {
		return "RepaymentTaskVO [userId=" + userId + ", repaymentTaskId=" + repaymentTaskId + ", creditCardNumber="
				+ creditCardNumber + ", amount=" + amount + ", rate=" + rate + ", serviceCharge=" + serviceCharge
				+ ", totalServiceCharge=" + totalServiceCharge + ", channelId=" + channelId + ", channelTag="
				+ channelTag + ", executeDate=" + executeDate + ", executeDateTime=" + executeDateTime
				+ ", description=" + description + ", consumeTaskVOs=" + consumeTaskVOs + ", createTime=" + createTime
				+ "]";
	}
	
	
	
	
}
