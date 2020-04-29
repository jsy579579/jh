package com.jh.user.pojo;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonFormat;

@Entity
@Table(name="t_brand_auto_upgrade")
public class BrandAutoUpgrade implements Serializable{
	
	
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


	public Date getCreateTime() {
		return createTime;
	}


	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}
	
	
	
}
