package com.jh.paymentgateway.pojo;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonFormat;

@Entity
@Table(name = "t_kb_bindcard")
public class KBBindCard {

	private static final long serialVersionUID = 114L;

	@Id 
	@Column(name = "id")
	private long id;

	@Column(name = "phone")
	private String phone;

	@Column(name = "bank_card")
	private String bankCard;

	@Column(name = "idcard")
	private String idCard;
	
	@Column(name = "expired_time")
	private String expiredTime;
	
	@Column(name = "security_code")
	private String securityCode;

	@Column(name = "order_code")
	private String orderCode;

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

	public String getBankCard() {
		return bankCard;
	}

	public void setBankCard(String bankCard) {
		this.bankCard = bankCard;
	}

	public String getIdCard() {
		return idCard;
	}

	public void setIdCard(String idCard) {
		this.idCard = idCard;
	}

	public String getOrderCode() {
		return orderCode;
	}

	public void setOrderCode(String orderCode) {
		this.orderCode = orderCode;
	}

	public Date getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getExpiredTime() {
		return expiredTime;
	}

	public void setExpiredTime(String expiredTime) {
		this.expiredTime = expiredTime;
	}

	public String getSecurityCode() {
		return securityCode;
	}

	public void setSecurityCode(String securityCode) {
		this.securityCode = securityCode;
	}

	@Override
	public String toString() {
		return "KBBindCard [id=" + id + ", phone=" + phone + ", bankCard=" + bankCard + ", idCard=" + idCard
				+ ", expiredTime=" + expiredTime + ", securityCode=" + securityCode + ", orderCode=" + orderCode
				+ ", status=" + status + ", createTime=" + createTime + "]";
	}

	
}
