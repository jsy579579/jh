package com.jh.paymentgateway.pojo;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonFormat;

@Entity
@Table(name = "t_hqdh_register")
public class HQDHRegister implements Serializable {
	private static final long serialVersionUID = 114L;

	@Id
	@Column(name = "id")
	private long id;

	@Column(name = "phone")
	private String phone;

	@Column(name = "idcard")
	private String idCard;

	@Column(name = "merchant_code")
	private String merchantCode;

	@Column(name = "merchant_order")
	private String merchantOrder;
	
	@Column(name = "status")
	private String status;
	
	@Column(name = "create_time")
	@JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
	private Date createTime;

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public String getIdCard() {
		return idCard;
	}

	public void setIdCard(String idCard) {
		this.idCard = idCard;
	}

	public String getMerchantCode() {
		return merchantCode;
	}

	public void setMerchantCode(String merchantCode) {
		this.merchantCode = merchantCode;
	}

	public String getMerchantOrder() {
		return merchantOrder;
	}

	public void setMerchantOrder(String merchantOrder) {
		this.merchantOrder = merchantOrder;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public Date getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}

	@Override
	public String toString() {
		return "HQDHRegister [id=" + id + ", phone=" + phone + ", idCard=" + idCard + ", merchantCode=" + merchantCode
				+ ", merchantOrder=" + merchantOrder + ", status=" + status + ", createTime=" + createTime + "]";
	}

	

}
