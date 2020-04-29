package com.jh.paymentgateway.pojo;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * @author 作者:lirui
 * @version 创建时间：2019年6月1日 下午5:41:02 类说明
 */
@Entity
@Table(name = "t_cjhk_lr_register")
public class CJHKLRRegister implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2535609244471721151L;

	@Id
	@Column(name = "id")
	private long id;
	@Column(name = "user_name")
	private String userName;

	@Column(name = "id_card")
	private String idCard;

	@Column(name = "rate")
	private String rate;

	@Column(name = "rate2")
	private String rate2;

	@Column(name = "rate3")
	private String rate3;

	@Column(name = "extra_fee")
	private String extraFee;

	@Column(name = "phone")
	private String phone;

	@Column(name = "merchant_code")
	private String merchantCode;

	@Column(name = "bank_card")
	private String bankCard;

	@Column(name = "create_time")
	private Date createTime;

	@Column(name = "update_time")
	private Date updateTime;

	@Column(name = "status")
	private String status;

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
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

	public String getRate2() {
		return rate2;
	}

	public void setRate2(String rate2) {
		this.rate2 = rate2;
	}

	public String getExtraFee() {
		return extraFee;
	}

	public void setExtraFee(String extraFee) {
		this.extraFee = extraFee;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public String getMerchantCode() {
		return merchantCode;
	}

	public void setMerchantCode(String merchantCode) {
		this.merchantCode = merchantCode;
	}

	public String getBankCard() {
		return bankCard;
	}

	public void setBankCard(String bankCard) {
		this.bankCard = bankCard;
	}

	public Date getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}

	public Date getUpdateTime() {
		return updateTime;
	}

	public void setUpdateTime(Date updateTime) {
		this.updateTime = updateTime;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getRate3() {
		return rate3;
	}

	public void setRate3(String rate3) {
		this.rate3 = rate3;
	}
}
