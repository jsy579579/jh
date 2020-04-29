package com.jh.notice.pojo;

import java.io.Serializable;
import java.util.Date;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import com.fasterxml.jackson.annotation.JsonFormat;


@Entity
@Table(name="t_brand_sms_count")
public class BrandSMSCount implements Serializable{

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name="id")
	private int id;
	
	//归属品牌
	@Column(name="brand_id")
	private String brandId;
	
	//发送次数
	@Column(name="sms_count")
	private Long smsCount=0l;
	
	//次数
	@Column(name="reserve_count")
	private Long reserveCount=0l;
	
	//次数
	@Column(name="reserve1_count")
	private Long reserve1Count=0l;
	
	//次数
	@Column(name="reserve2_count")
	private Long reserve12Count=0l;
	
	//次数
	@Column(name="reserve3_count")
	private Long reserve13Count=0l;
	
	
	//创建日期
	@Column(name="create_time")
	@JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
	private Date createTime;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getBrandId() {
		return brandId;
	}

	public void setBrandId(String brandId) {
		this.brandId = brandId;
	}

	public Long getSmsCount() {
		return smsCount;
	}

	public void setSmsCount(Long smsCount) {
		this.smsCount = smsCount;
	}

	public Long getReserveCount() {
		return reserveCount;
	}

	public void setReserveCount(Long reserveCount) {
		this.reserveCount = reserveCount;
	}

	public Long getReserve1Count() {
		return reserve1Count;
	}

	public void setReserve1Count(Long reserve1Count) {
		this.reserve1Count = reserve1Count;
	}

	public Long getReserve12Count() {
		return reserve12Count;
	}

	public void setReserve12Count(Long reserve12Count) {
		this.reserve12Count = reserve12Count;
	}

	public Long getReserve13Count() {
		return reserve13Count;
	}

	public void setReserve13Count(Long reserve13Count) {
		this.reserve13Count = reserve13Count;
	}

	public Date getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}
	
}
