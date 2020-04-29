package com.jh.paymentchannel.pojo;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonFormat;

@Entity
@Table(name = "t_wmyk_bindcard")
public class WMYKBindCard {

	private static final long serialVersionUID = 154L;

	@Id
	@Column(name = "id")
	private long id;

	@Column(name = "phone")
	private String phone;

	@Column(name = "bank_card")
	private String bankCard;

	@Column(name = "idcard")
	private String idCard;
	
	@Column(name = "plat_userid")
	private String platUserId;
	
	@Column(name = "bindcard_seq")
	private String bindCardSeq;
	
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


	public Date getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}

	public String getBindCardSeq() {
		return bindCardSeq;
	}

	public void setBindCardSeq(String bindCardSeq) {
		this.bindCardSeq = bindCardSeq;
	}

	public String getPlatUserId() {
		return platUserId;
	}

	public void setPlatUserId(String platUserId) {
		this.platUserId = platUserId;
	}

	
}
