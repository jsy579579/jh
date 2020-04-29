package com.jh.paymentchannel.pojo;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonFormat;

@Entity
@Table(name="t_swift_brand_weixin_merchant")
public class SwiftBrandWeixinMerchant implements Serializable{

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name="id")
	private long id;
	
	@Column(name="brand_id")
	private String brandid;
	
	@Column(name="sub_merhant_id")
	private String submerchantid;
	
	@Column(name="sub_merhant_key")
	private String submerchantkey;
	
	@Column(name="weixin_app_id")
	private String weixinappid;
	
	@Column(name="active_status")
	private String activestatus ="0";
	
	@Column(name="create_time")
	@JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
	private Date createTime;

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getBrandid() {
		return brandid;
	}

	public void setBrandid(String brandid) {
		this.brandid = brandid;
	}

	public String getSubmerchantid() {
		return submerchantid;
	}

	public void setSubmerchantid(String submerchantid) {
		this.submerchantid = submerchantid;
	}

	public String getSubmerchantkey() {
		return submerchantkey;
	}

	public void setSubmerchantkey(String submerchantkey) {
		this.submerchantkey = submerchantkey;
	}



	public String getActivestatus() {
		return activestatus;
	}

	public void setActivestatus(String activestatus) {
		this.activestatus = activestatus;
	}

	public Date getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}

	public String getWeixinappid() {
		return weixinappid;
	}

	public void setWeixinappid(String weixinappid) {
		this.weixinappid = weixinappid;
	}
	
	
	
	
	
}
