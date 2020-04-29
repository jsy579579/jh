package com.jh.user.pojo;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

import com.fasterxml.jackson.annotation.JsonFormat;

public class BrandAndChannel {

	private Long channelId;
	
	/** 渠道标识 */
	private String channelTag;

	/** 通道备注 */
	private String remarks;

	/** 通道备注 */
	private String extendedField;

	/** 传到渠道的参数, 集成的商家类的 */
	private String channelParams;

	/** 渠道商家标号 */
	private String channelNo;

	/** 是否自动清算 */
	private String autoclearing = "0";

	/**
	 * 通道类型 0 表示 T+0 1表示 T+1 2表示 D+0 3表示 D+1
	 */
	private String channelType;

	/** 渠道名字 */
	private String channelName;

	/** 子渠道标识 */
	private String subChannelTag;

	/** 渠道名字 */
	private String subName;

	/** 单次最低限额 */
	private BigDecimal singleMinLimit;

	/** 单次最高额度 */
	private BigDecimal singleMaxLimit;

	/** 单日最高额度 */
	private BigDecimal everyDayMaxLimit;

	/** 通道开始可以使用的时间 */
	private String startTime;

	/** 通道结算的时间 **/
	private String endTime;

	private BigDecimal rate;
	
	private BigDecimal minRate;

	private BigDecimal extraFee;

	private BigDecimal withdrawFee;

	/** 通道状态 */
	private String status;

	private String log;

	public String getChannelTag() {
		return channelTag;
	}

	public void setChannelTag(String channelTag) {
		this.channelTag = channelTag;
	}

	public String getRemarks() {
		return remarks;
	}

	public void setRemarks(String remarks) {
		this.remarks = remarks;
	}

	public String getExtendedField() {
		return extendedField;
	}

	public void setExtendedField(String extendedField) {
		this.extendedField = extendedField;
	}

	public String getChannelParams() {
		return channelParams;
	}

	public void setChannelParams(String channelParams) {
		this.channelParams = channelParams;
	}

	public String getChannelNo() {
		return channelNo;
	}

	public void setChannelNo(String channelNo) {
		this.channelNo = channelNo;
	}

	public String getAutoclearing() {
		return autoclearing;
	}

	public void setAutoclearing(String autoclearing) {
		this.autoclearing = autoclearing;
	}

	public String getChannelType() {
		return channelType;
	}

	public void setChannelType(String channelType) {
		this.channelType = channelType;
	}

	public String getChannelName() {
		return channelName;
	}

	public void setChannelName(String channelName) {
		this.channelName = channelName;
	}

	public String getSubChannelTag() {
		return subChannelTag;
	}

	public void setSubChannelTag(String subChannelTag) {
		this.subChannelTag = subChannelTag;
	}

	public String getSubName() {
		return subName;
	}

	public void setSubName(String subName) {
		this.subName = subName;
	}

	public BigDecimal getSingleMinLimit() {
		return singleMinLimit;
	}

	public void setSingleMinLimit(BigDecimal singleMinLimit) {
		this.singleMinLimit = singleMinLimit;
	}

	public BigDecimal getSingleMaxLimit() {
		return singleMaxLimit;
	}

	public void setSingleMaxLimit(BigDecimal singleMaxLimit) {
		this.singleMaxLimit = singleMaxLimit;
	}

	public BigDecimal getEveryDayMaxLimit() {
		return everyDayMaxLimit;
	}

	public void setEveryDayMaxLimit(BigDecimal everyDayMaxLimit) {
		this.everyDayMaxLimit = everyDayMaxLimit;
	}

	public String getStartTime() {
		return startTime;
	}

	public void setStartTime(String startTime) {
		this.startTime = startTime;
	}

	public String getEndTime() {
		return endTime;
	}

	public void setEndTime(String endTime) {
		this.endTime = endTime;
	}

	public BigDecimal getRate() {
		return rate;
	}

	public void setRate(BigDecimal rate) {
		this.rate = rate;
	}

	public BigDecimal getMinRate() {
		return minRate;
	}

	public void setMinRate(BigDecimal minRate) {
		this.minRate = minRate;
	}

	public BigDecimal getExtraFee() {
		return extraFee;
	}

	public void setExtraFee(BigDecimal extraFee) {
		this.extraFee = extraFee;
	}

	public BigDecimal getWithdrawFee() {
		return withdrawFee;
	}

	public void setWithdrawFee(BigDecimal withdrawFee) {
		this.withdrawFee = withdrawFee;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getLog() {
		return log;
	}

	public void setLog(String log) {
		this.log = log;
	}

	public Long getChannelId() {
		return channelId;
	}

	public void setChannelId(Long channelId) {
		this.channelId = channelId;
	}

	@Override
	public String toString() {
		return "BrandAndChannel [channelId=" + channelId + ", channelTag=" + channelTag + ", remarks=" + remarks
				+ ", extendedField=" + extendedField + ", channelParams=" + channelParams + ", channelNo=" + channelNo
				+ ", autoclearing=" + autoclearing + ", channelType=" + channelType + ", channelName=" + channelName
				+ ", subChannelTag=" + subChannelTag + ", subName=" + subName + ", singleMinLimit=" + singleMinLimit
				+ ", singleMaxLimit=" + singleMaxLimit + ", everyDayMaxLimit=" + everyDayMaxLimit + ", startTime="
				+ startTime + ", endTime=" + endTime + ", rate=" + rate + ", minRate=" + minRate + ", extraFee="
				+ extraFee + ", withdrawFee=" + withdrawFee + ", status=" + status + ", log=" + log + "]";
	}


	
	
	
}
