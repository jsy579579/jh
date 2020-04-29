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

@Entity
@Table(name = "t_channel")
public class Channel implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "id")
	private long id;

	/** 渠道标识 */
	@Column(name = "channel_tag")
	private String channelTag;

	/** 通道备注 */
	@Column(name = "remarks")
	private String remarks;

	/** 通道备注 */
	@Column(name = "extended_field")
	private String extendedField;

	/** 传到渠道的参数, 集成的商家类的 */
	@Column(name = "channel_param")
	private String channelParams;

	/** 渠道商家标号 */
	@Column(name = "channel_no")
	private String channelNo;

	/** 异部回调的url */
	@Column(name = "notify_url")
	private String notifyURL;

	/** 同步回调的url */
	@Column(name = "return_url")
	private String returnURL;

	/** 是否自动清算 */
	@Column(name = "auto_clearing")
	private String autoclearing = "0";

	/**
	 * 通道类型 0 表示 T+0 1表示 T+1 2表示 D+0 3表示 D+1
	 */
	@Column(name = "channel_type")
	private String channelType;

	/** 渠道名字 */
	@Column(name = "name")
	private String name;

	/** 子渠道标识 */
	@Column(name = "sub_channel_tag")
	private String subChannelTag;

	/** 渠道名字 */
	@Column(name = "sub_name")
	private String subName;

	/** 单次最低限额 */
	@Column(name = "single_min_limit")
	private BigDecimal singleMinLimit;

	/** 单次最高额度 */
	@Column(name = "single_max_limit")
	private BigDecimal singleMaxLimit;

	/** 单日最高额度 */
	@Column(name = "every_day_max_limit")
	private BigDecimal everyDayMaxLimit;

	/** 成本提现费 */
	@Column(name = "min_with_draw_fee")
	private BigDecimal minwithdrawFee;

	/** 成本额外费用 */
	@Column(name = "min_extra_fee")
	private BigDecimal minextrafee;

	/** 通道开始可以使用的时间 */
	@Column(name = "start_time")
	// @JsonFormat(timezone = "GMT+8", pattern = "HH:mm:ss")
	private String startTime;

	/** 通道结算的时间 **/
	@Column(name = "end_time")
	// @JsonFormat(timezone = "GMT+8", pattern = "HH:mm:ss")
	private String endTime;
	// 成本费率
	@Column(name = "cost_rate")
	private String costRate;

	@Transient
	private BigDecimal rate = BigDecimal.ZERO;

	@Transient
	private BigDecimal extraFee = BigDecimal.ZERO;

	@Transient
	private BigDecimal withdrawFee = BigDecimal.ZERO;

	/** 通道状态 */
	@Column(name = "status")
	private String status = "1";

	/** 渠道log */
	@Column(name = "log")
	private String log;

	/** 有无积分 */
	@Column(name = "coin_type")
	private String coinType = "0";

	@Column(name = "create_time")
	@JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
	private Date createTime;
	
	//判断是否满额,0为满额,1为正常使用
	@Column(name="payment_status")
	private String paymentStatus;
	
	

	public String getPaymentStatus() {
		return paymentStatus;
	}

	public void setPaymentStatus(String paymentStatus) {
		this.paymentStatus = paymentStatus;
	}

	@Column(name = "sort")
	private String sort;
	
	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getAutoclearing() {
		return autoclearing;
	}

	public void setAutoclearing(String autoclearing) {
		this.autoclearing = autoclearing;
	}

	public String getChannelTag() {
		return channelTag;
	}

	public void setChannelTag(String channelTag) {
		this.channelTag = channelTag;
	}

	public String getChannelNo() {
		return channelNo;
	}

	public void setChannelNo(String channelNo) {
		this.channelNo = channelNo;
	}

	public BigDecimal getMinwithdrawFee() {
		return minwithdrawFee;
	}

	public void setMinwithdrawFee(BigDecimal minwithdrawFee) {
		this.minwithdrawFee = minwithdrawFee;
	}

	public BigDecimal getMinextrafee() {
		return minextrafee;
	}

	public void setMinextrafee(BigDecimal minextrafee) {
		this.minextrafee = minextrafee;
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

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
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

	public Date getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
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

	public BigDecimal getRate() {
		return rate;
	}

	public void setRate(BigDecimal rate) {
		this.rate = rate;
	}

	public String getChannelParams() {
		return channelParams;
	}

	public void setChannelParams(String channelParams) {
		this.channelParams = channelParams;
	}

	public String getNotifyURL() {
		return notifyURL;
	}

	public void setNotifyURL(String notifyURL) {
		this.notifyURL = notifyURL;
	}

	public String getReturnURL() {
		return returnURL;
	}

	public void setReturnURL(String returnURL) {
		this.returnURL = returnURL;
	}

	public String getChannelType() {
		return channelType;
	}

	public void setChannelType(String channelType) {
		this.channelType = channelType;
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

	public String getExtendedField() {
		return extendedField;
	}

	public void setExtendedField(String extendedField) {
		this.extendedField = extendedField;
	}

	public String getRemarks() {
		return remarks;
	}

	public void setRemarks(String remarks) {
		this.remarks = remarks;
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

	public String getCoinType() {
		return coinType;
	}

	public void setCoinType(String coinType) {
		this.coinType = coinType;
	}

	public String getCostRate() {
		return costRate;
	}

	public void setCostRate(String costRate) {
		this.costRate = costRate;
	}

	public String getSort() {
		return sort;
	}

	public void setSort(String sort) {
		this.sort = sort;
	}

	@Override
	public String toString() {
		return "Channel [id=" + id + ", channelTag=" + channelTag + ", remarks=" + remarks + ", extendedField="
				+ extendedField + ", channelParams=" + channelParams + ", channelNo=" + channelNo + ", notifyURL="
				+ notifyURL + ", returnURL=" + returnURL + ", autoclearing=" + autoclearing + ", channelType="
				+ channelType + ", name=" + name + ", subChannelTag=" + subChannelTag + ", subName=" + subName
				+ ", singleMinLimit=" + singleMinLimit + ", singleMaxLimit=" + singleMaxLimit + ", everyDayMaxLimit="
				+ everyDayMaxLimit + ", minwithdrawFee=" + minwithdrawFee + ", minextrafee=" + minextrafee
				+ ", startTime=" + startTime + ", endTime=" + endTime + ", costRate=" + costRate + ", rate=" + rate
				+ ", extraFee=" + extraFee + ", withdrawFee=" + withdrawFee + ", status=" + status + ", log=" + log
				+ ", coinType=" + coinType + ", createTime=" + createTime + ", paymentStatus=" + paymentStatus
				+ ", sort=" + sort + "]";
	}

	
	
}
