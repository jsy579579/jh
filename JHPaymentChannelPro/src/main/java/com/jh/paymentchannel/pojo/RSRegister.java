package com.jh.paymentchannel.pojo;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonFormat;

@Entity
@Table(name = "t_rs_register")
public class RSRegister implements Serializable{

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "id")
	private long id;
	
	@Column(name = "userid")
	private String userid;
	
	@Column(name = "product")
	private String product;//产品id

	@Column(name = "phone")
	private String phone;
	
	@Column(name = "fixed")
	private String fixed;//手续费

	@Column(name = "id_card")
	private String idCard;//身份证号码
	
	@Column(name = "contact_name")
	private String contactName;//开户名
	
	public String getFixed() {
		return fixed;
	}

	public void setFixed(String fixed) {
		this.fixed = fixed;
	}
	@Column(name = "bank_account_type")
	private String bankAccountType;//银行卡类型  统一对私:PRIVATE_ACCOUNT
	
	@Column(name = "bank_account_no")
	private String bankAccountNo;//银行卡
	
	@Column(name = "merchant_name")
	private String merchantName;//商户名称，必填
	
	@Column(name = "merchant_code")
	private String merchantCode;//商户编码
	
	@Column(name = "rate")
	private String rate;//费率
	
	@Column(name = "extra_fee")
	private String extraFee;

	@Column(name="create_time")
	@JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
	private Date createTime;
	
	@Column(name="change_time")
	@JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
	private Date changeTime;

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getUserid() {
		return userid;
	}

	public void setUserid(String userid) {
		this.userid = userid;
	}

	public String getProduct() {
		return product;
	}

	public void setProduct(String product) {
		this.product = product;
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

	public String getContactName() {
		return contactName;
	}

	public void setContactName(String contactName) {
		this.contactName = contactName;
	}

	public String getBankAccountType() {
		return bankAccountType;
	}

	public void setBankAccountType(String bankAccountType) {
		this.bankAccountType = bankAccountType;
	}

	public String getBankAccountNo() {
		return bankAccountNo;
	}

	public void setBankAccountNo(String bankAccountNo) {
		this.bankAccountNo = bankAccountNo;
	}

	public String getMerchantName() {
		return merchantName;
	}

	public void setMerchantName(String merchantName) {
		this.merchantName = merchantName;
	}

	public String getMerchantCode() {
		return merchantCode;
	}

	public void setMerchantCode(String merchantCode) {
		this.merchantCode = merchantCode;
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

	public Date getChangeTime() {
		return changeTime;
	}

	public void setChangeTime(Date changeTime) {
		this.changeTime = changeTime;
	}
	@Override
	public String toString() {
		return "RSRegister [id=" + id + ", userid=" + userid + ", product=" + product + ", phone=" + phone + ", fixed="
				+ fixed + ", idCard=" + idCard + ", contactName=" + contactName + ", bankAccountType=" + bankAccountType
				+ ", bankAccountNo=" + bankAccountNo + ", merchantName=" + merchantName + ", merchantCode="
				+ merchantCode + ", rate=" + rate + ", extraFee=" + extraFee + ", createTime=" + createTime
				+ ", changeTime=" + changeTime + "]";
	}
	
}
