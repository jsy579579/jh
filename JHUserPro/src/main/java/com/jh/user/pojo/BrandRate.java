package com.jh.user.pojo;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonFormat;

/**平台从渠道拿到的结算费率*/
@Entity
@Table(name="t_brand_rate")
public class BrandRate implements Serializable{


	private static final long serialVersionUID = 1L;

	@Id
	@Column(name="id")
	private long id;
	
	@Column(name="brand_id")
	private long brandId;
	
	/**通道的Id*/
	@Column(name="channel_id")
	private long channelId;
	
	/**通道的结算费率  普通用户*/
	@Column(name="rate")
	private BigDecimal rate;
	
	/**上游给的渠道的最低费率*/
	@Column(name="minrate")
	private BigDecimal minrate;
	
	/**单笔额外收费*/
	@Column(name="extra_fee")
	private BigDecimal extraFee;
	
	
	/***提现费用*/
	@Column(name="with_draw_fee")
	private BigDecimal withdrawFee;
	
	/***状态*/
	@Column(name="status")
	private String status;
	
	public String getStatus() {
		return status;
	}


	public void setStatus(String status) {
		this.status = status;
	}


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


	public long getBrandId() {
		return brandId;
	}


	public void setBrandId(long brandId) {
		this.brandId = brandId;
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


	public BigDecimal getMinrate() {
		return minrate;
	}


	public void setMinrate(BigDecimal minrate) {
		this.minrate = minrate;
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


	public BigDecimal getWithdrawFee() {
		return withdrawFee;
	}


	public void setWithdrawFee(BigDecimal withdrawFee) {
		this.withdrawFee = withdrawFee;
	}


	@Override
	public String toString() {
		return "BrandRate [id=" + id + ", brandId=" + brandId + ", channelId=" + channelId + ", rate=" + rate
				+ ", minrate=" + minrate + ", extraFee=" + extraFee + ", withdrawFee=" + withdrawFee + ", status="
				+ status + ", createTime=" + createTime + "]";
	}
	
	
	
	
}
