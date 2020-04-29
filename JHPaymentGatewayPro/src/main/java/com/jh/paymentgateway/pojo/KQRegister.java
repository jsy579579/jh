package com.jh.paymentgateway.pojo;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonFormat;

@Entity
@Table(name = "t_kq_register")
public class KQRegister {
	private static final long serialVersionUID = 114L;

	@Id
	@Column(name = "id")
	private int id;

	@Column(name = "user_name")
	private String userName;

	@Column(name = "id_card")
	private String idCard;

	@Column(name = "bank_card")
	private String bankCard;

	@Column(name = "phone")
	private String phone;

	@Column(name = "merchant_no")
	private String merchantNo;

	@Column(name = "fss_id1")
	private String fssId1;

	@Column(name = "fss_id2")
	private String fssId2;

	@Column(name = "fss_id3")
	private String fssId3;

	@Column(name = "create_time")
	@JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
	private Date createTime;

	@Column(name = "change_time")
	@JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
	private Date changeTime;
	
	@Column(name="sign_status")
	private String signStatus;
	

	public String getSignStatus() {
		return signStatus;
	}

	public void setSignStatus(String signStatus) {
		this.signStatus = signStatus;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
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

	public String getBankCard() {
		return bankCard;
	}

	public void setBankCard(String bankCard) {
		this.bankCard = bankCard;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public String getMerchantNo() {
		return merchantNo;
	}

	public void setMerchantNo(String merchantNo) {
		this.merchantNo = merchantNo;
	}

	public String getFssId1() {
		return fssId1;
	}

	public void setFssId1(String fssId1) {
		this.fssId1 = fssId1;
	}

	public String getFssId2() {
		return fssId2;
	}

	public void setFssId2(String fssId2) {
		this.fssId2 = fssId2;
	}

	public String getFssId3() {
		return fssId3;
	}

	public void setFssId3(String fssId3) {
		this.fssId3 = fssId3;
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

}
