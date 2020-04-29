package com.jh.paymentgateway.pojo;


import java.util.Date;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import com.fasterxml.jackson.annotation.JsonFormat;

@Entity
@Table(name = "t_rhjf_register")
public class RHJFRegister  {

	private static final long serialVersionUID = 2248686210023671270L;

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
	
	@Column(name = "rate")
	private String rate;
	
	@Column(name = "signkey")
	private String signkey;
	
	@Column(name = "deskey")
	private String deskey;
	
	@Column(name = "extra_fee")
	private String extraFee;
	
	@Column(name = "querykey")
	private String querykey;
	
	@Column(name="create_time")
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

	public String getRate() {
		return rate;
	}

	public void setRate(String rate) {
		this.rate = rate;
	}
	
	public Date getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}

	public String getExtraFee() {
		return extraFee;
	}

	public void setExtraFee(String extraFee) {
		this.extraFee = extraFee;
	}

	public String getMerchantNo() {
		return merchantNo;
	}

	public void setMerchantNo(String merchantNo) {
		this.merchantNo = merchantNo;
	}
	
	public String getSignkey() {
		return signkey;
	}

	public void setSignkey(String signkey) {
		this.signkey = signkey;
	}

	public String getDeskey() {
		return deskey;
	}

	public void setDeskey(String deskey) {
		this.deskey = deskey;
	}
	public String getQuerykey() {
		return querykey;
	}

	public void setQuerykey(String querykey) {
		this.querykey = querykey;
	}
	
}
