package com.jh.paymentgateway.pojo;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonFormat;

@Entity
@Table(name = "t_hzhk_register")
public class HZHKRegister {
	
	private static final long serialVersionUID = 3628493513761818264L;
	
	@Id
	@Column(name = "id")
	private long id;

	@Column(name = "phone")
	private String phone;

	@Column(name = "bank_card")
	private String bankCard;
	
	@Column(name = "id_card")
	private String idCard;
	
	@Column(name = "user_name")
	private String userName;
	
	@Column(name = "bank_name")
	private String bankName;
	
	@Column(name = "merchant_no")//进件返回的商户号
	private String merchantNo;
	
	@Column(name = "payypt_order_no")//快捷YPT订单号
	private String yptOrderNo;
	
	@Column(name = "repayypt_order_no")//代还YPT订单号
	private String reyptOrderNo;
	
	
	@Column(name = "validity")//有效期
	private String expiredTime;
	
	@Column(name = "cvv")//安全码
	private String securityCode;
	
	@Column(name="bank_code")
	private String bankCode;
	
	@Column(name = "rate")
	private String rate;
	
	@Column(name = "extra_fee")
	private String extraFee;
	
	@Column(name="create_time")
	@JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
	private String createTime;

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

	public String getBankName() {
		return bankName;
	}

	public void setBankName(String bankName) {
		this.bankName = bankName;
	}

	public String getReyptOrderNo() {
		return reyptOrderNo;
	}

	public void setReyptOrderNo(String reyptOrderNo) {
		this.reyptOrderNo = reyptOrderNo;
	}

	public String getBankCard() {
		return bankCard;
	}

	public void setBankCard(String bankCard) {
		this.bankCard = bankCard;
	}
	
	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}
	
	public String getBankCode() {
		return bankCode;
	}

	public void setBankCode(String bankCode) {
		this.bankCode = bankCode;
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

	public String getMerchantNo() {
		return merchantNo;
	}

	public void setMerchantNo(String merchantNo) {
		this.merchantNo = merchantNo;
	}

	public String getIdCard() {
		return idCard;
	}

	public void setIdCard(String idCard) {
		this.idCard = idCard;
	}
	
	public String getYptOrderNo() {
		return yptOrderNo;
	}

	public void setYptOrderNo(String yptOrderNo) {
		this.yptOrderNo = yptOrderNo;
	}

	public String getRate() {
		return rate;
	}

	public void setRate(String rate) {
		this.rate = rate;
	}

	public String getExtraFee() {
		return extraFee;
	}

	public void setExtraFee(String extraFee) {
		this.extraFee = extraFee;
	}

	public String getCreateTime() {
		return createTime;
	}

	public void setCreateTime(String createTime) {
		this.createTime = createTime;
	}

	public static long getSerialversionuid() {
		return serialVersionUID;
	}
	
}