package com.jh.user.pojo;

import java.math.BigDecimal;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonFormat;

@Entity
@Table(name="t_brand_coin")
public class BrandCoin {

	
	@Id
	@Column(name="id")
	private long id;
	
	@Column(name="brand_id")
	private long brandId;
	
	
	/**现金换算成积分的比率*/
	@Column(name="ratio")
	private BigDecimal ratio;
	
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


	public BigDecimal getRatio() {
		return ratio;
	}


	public void setRatio(BigDecimal ratio) {
		this.ratio = ratio;
	}


	public Date getCreateTime() {
		return createTime;
	}


	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}
	
	
	
	
	
}
