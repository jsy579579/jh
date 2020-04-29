package com.jh.paymentchannel.pojo;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonFormat;

@Entity
@Table(name="t_swift_brand_merchant")
public class SwiftBrandMerchant implements Serializable{

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name="id")
	private long id;
	
	@Column(name="brand_id")
	private String brand_id;
	
	@Column(name="sub_merchant_id")
	private String subMerchantid;

	@Column(name="sub_merchant_name")
	private String subMerchantName;
	
	@Column(name="sub_merchant_key")
	private String subMerchantKey;
	
	@Column(name="create_time")
	@JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
	private Date createTime;

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getBrand_id() {
		return brand_id;
	}

	public void setBrand_id(String brand_id) {
		this.brand_id = brand_id;
	}

	public String getSubMerchantid() {
		return subMerchantid;
	}

	public void setSubMerchantid(String subMerchantid) {
		this.subMerchantid = subMerchantid;
	}

	public String getSubMerchantName() {
		return subMerchantName;
	}

	public void setSubMerchantName(String subMerchantName) {
		this.subMerchantName = subMerchantName;
	}

	public String getSubMerchantKey() {
		return subMerchantKey;
	}

	public void setSubMerchantKey(String subMerchantKey) {
		this.subMerchantKey = subMerchantKey;
	}

	public Date getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}

	
	
}
