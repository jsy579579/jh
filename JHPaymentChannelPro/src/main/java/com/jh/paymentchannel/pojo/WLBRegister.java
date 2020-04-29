package com.jh.paymentchannel.pojo;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonFormat;

@Entity
@Table(name="t_wlb_register")
public class WLBRegister implements Serializable{
	
	private static final long serialVersionUID = 476L;
	
	@Id
	@Column(name="id")
	private long id;
	
	@Column(name="phone")
	private String phone;
	
	@Column(name="bank_card")
	private String bankCard;
	
	@Column(name="idcard")
	private String idCard;
	
	@Column(name="merchant_no")
	private String merchantNO;
	
	@Column(name="sign_key")
	private String signKey;
	
	@Column(name="des_key")
	private String desKey;
	
	@Column(name="query_key")
	private String queryKey;
	
	@Column(name="rate")
	private String rate;
	
	@Column(name="extrafee")
	private String extraFee;
	
	@Column(name="create_time")
	@JsonFormat(timezone = "GMT+8", pattern="yyyy-MM-dd HH:mm:ss")
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

	public String getMerchantNO() {
		return merchantNO;
	}

	public void setMerchantNO(String merchantNO) {
		this.merchantNO = merchantNO;
	}

	public String getSignKey() {
		return signKey;
	}

	public void setSignKey(String signKey) {
		this.signKey = signKey;
	}

	public String getDesKey() {
		return desKey;
	}

	public void setDesKey(String desKey) {
		this.desKey = desKey;
	}

	public String getQueryKey() {
		return queryKey;
	}

	public void setQueryKey(String queryKey) {
		this.queryKey = queryKey;
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

	public Date getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}

	
}
