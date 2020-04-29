package com.jh.paymentgateway.pojo;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonFormat;

@Entity
@Table(name = "t_jfx_register")
public class JFXRegister {

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

	@Column(name = "merchant_no")
	private String merchantNo;

	@Column(name = "rate_one")
	private String rateOne;

	@Column(name = "extra_fee_one")
	private String extraFeeOne;

	@Column(name = "rate_two")
	private String rateTwo;

	@Column(name = "extra_fee_two")
	private String extraFeeTwo;
	
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

	public String getMerchantNo() {
		return merchantNo;
	}

	public void setMerchantNo(String merchantNo) {
		this.merchantNo = merchantNo;
	}

	public String getRateOne() {
		return rateOne;
	}

	public void setRateOne(String rateOne) {
		this.rateOne = rateOne;
	}

	public String getExtraFeeOne() {
		return extraFeeOne;
	}

	public void setExtraFeeOne(String extraFeeOne) {
		this.extraFeeOne = extraFeeOne;
	}

	public String getRateTwo() {
		return rateTwo;
	}

	public void setRateTwo(String rateTwo) {
		this.rateTwo = rateTwo;
	}

	public String getExtraFeeTwo() {
		return extraFeeTwo;
	}

	public void setExtraFeeTwo(String extraFeeTwo) {
		this.extraFeeTwo = extraFeeTwo;
	}

	public Date getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}

	

}
