package com.jh.user.pojo;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonFormat;


/**
 * 这个费率是用户在平台消费的费率
 * */
@Entity
@Table(name="t_channel_rate")
public class ChannelRate implements Serializable{

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name="id")
	private long id; 
	
	/**用户id*/
	@Column(name="user_id")
	private long userId;
	
	/**所属贴牌， 如果userId为空的时候， 那么就是该平台的默认费率*/
	@Column(name="brand_id")
	private long brandId;
	
	/**通道的Id*/
	@Column(name="channel_id")
	private long channelId;
	
	/**通道的交易费率*/
	@Column(name="rate")
	private BigDecimal rate;
	
	/**单笔额外收费*/
	@Column(name="extra_fee")
	private BigDecimal extraFee;
	
	
	/***提现费用*/
	@Column(name="with_draw_fee")
	private BigDecimal withdrawFee;
	
	
	/**创建时间*/
	@Column(name="create_time")
	@JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
	private Date createTime;

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public long getUserId() {
		return userId;
	}

	public void setUserId(long userId) {
		this.userId = userId;
	}

	public long getChannelId() {
		return channelId;
	}

	public void setChannelId(long channelId) {
		this.channelId = channelId;
	}

	public BigDecimal getRate() {
		return rate;
	}

	public void setRate(BigDecimal rate) {
		this.rate = rate;
	}

	public BigDecimal getExtraFee() {
		return extraFee;
	}

	public void setExtraFee(BigDecimal extraFee) {
		this.extraFee = extraFee;
	}

	public Date getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}

	public long getBrandId() {
		return brandId;
	}

	public void setBrandId(long brandId) {
		this.brandId = brandId;
	}

	public BigDecimal getWithdrawFee() {
		return withdrawFee;
	}

	public void setWithdrawFee(BigDecimal withdrawFee) {
		this.withdrawFee = withdrawFee;
	}

	@Override
	public String toString() {
		return "ChannelRate [id=" + id + ", userId=" + userId + ", brandId=" + brandId + ", channelId=" + channelId
				+ ", rate=" + rate + ", extraFee=" + extraFee + ", withdrawFee=" + withdrawFee + ", createTime="
				+ createTime + "]";
	}
	
	
}
