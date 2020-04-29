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
@Table(name="t_credit_coin_exchange")
public class CreditCoinExchangeOrder implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	@Id
	@Column(name="id")
	private long id;
	@Column(name="order_code")
	private String orderCode;
	@Column(name="user_id")
	private String userId;
	@Column(name="phone")
	private String phone;
	@Column(name="exchange_grade")
	private String exchangeGrade;
	@Column(name="bank_name")
	private String bankName;
	@Column(name="bank_code")
	private String bankCode;
	@Column(name="brand_id")
	private String brandId;
	@Column(name="exchange_key")
	private String exchangeKey;
	@Column(name="exchange_coin")
	private String exchangeCoin = "0";
	@Column(name="exchange_money")
	private BigDecimal exchangeMoney = BigDecimal.ZERO;
	@Column(name="exchange_type")
	private int exchangeType = 0;
	@Column(name="order_type")
	private String orderType;
	@Column(name="remark")
	private String remark;
	@Column(name="order_status")
	private int orderStatus = 0;
	@Column(name = "update_time")
	@JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
	private Date updateTime;
	@Column(name = "create_time")
	@JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
	private Date createTime;
	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getOrderCode() {
		return orderCode;
	}

	public void setOrderCode(String orderCode) {
		this.orderCode = orderCode;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getBankName() {
		return bankName;
	}

	public void setBankName(String bankName) {
		this.bankName = bankName;
	}

	public String getExchangeKey() {
		return exchangeKey;
	}

	public void setExchangeKey(String exchangeKey) {
		this.exchangeKey = exchangeKey;
	}

	public String getOrderType() {
		return orderType;
	}

	public void setOrderType(String orderType) {
		this.orderType = orderType;
	}

	public String getRemark() {
		return remark;
	}

	public void setRemark(String remark) {
		this.remark = remark;
	}

	public int getOrderStatus() {
		return orderStatus;
	}

	public void setOrderStatus(int orderStatus) {
		this.orderStatus = orderStatus;
	}

	public String getExchangeGrade() {
		return exchangeGrade;
	}

	public void setExchangeGrade(String exchangeGrade) {
		this.exchangeGrade = exchangeGrade;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public String getExchangeCoin() {
		return exchangeCoin;
	}

	public void setExchangeCoin(String exchangeCoin) {
		this.exchangeCoin = exchangeCoin;
	}

	public Date getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}

	public BigDecimal getExchangeMoney() {
		return exchangeMoney;
	}

	public Date getUpdateTime() {
		return updateTime;
	}

	public void setExchangeMoney(BigDecimal exchangeMoney) {
		this.exchangeMoney = exchangeMoney;
	}

	public void setUpdateTime(Date updateTime) {
		this.updateTime = updateTime;
	}

	public String getBankCode() {
		return bankCode;
	}

	public void setBankCode(String bankCode) {
		this.bankCode = bankCode;
	}

	public int getExchangeType() {
		return exchangeType;
	}

	public void setExchangeType(int exchangeType) {
		this.exchangeType = exchangeType;
	}

	public String getBrandId() {
		return brandId;
	}

	public void setBrandId(String brandId) {
		this.brandId = brandId;
	}

	
}
