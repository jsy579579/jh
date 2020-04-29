package com.jh.paymentchannel.pojo;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonFormat;

@Entity
@Table(name = "t_car_province")
public class CarSupportProvince implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "id")
	private long id;

	@Column(name = "province")
	private String province;
	
	@Column(name = "province_abbr")
	private String province_abbr;
	
	@Column(name="create_time")
	@JsonFormat(timezone = "GMT+8", pattern="yyyy-MM-dd HH:mm:ss")
	private Date createTime;

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getProvince() {
		return province;
	}

	public void setProvince(String province) {
		this.province = province;
	}

	public String getProvince_abbr() {
		return province_abbr;
	}

	public void setProvince_abbr(String province_abbr) {
		this.province_abbr = province_abbr;
	}

	public Date getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}

	@Override
	public String toString() {
		return "CarSupportProvince [id=" + id + ", province=" + province + ", province_abbr=" + province_abbr
				+ ", createTime=" + createTime + "]";
	}

	

	
	
}
