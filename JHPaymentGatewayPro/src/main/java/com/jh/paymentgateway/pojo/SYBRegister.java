package com.jh.paymentgateway.pojo;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "t_syb_register")
public class SYBRegister implements Serializable {


	private static final long serialVersionUID = -7004235881526528214L;
	@Id
	@Column(name = "id")
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Long id;

	@Column(name = "user_name")
	private String userName;

	@Column(name = "phone")
	private String phone;

	@Column(name = "id_card")
	private String idCard;

	@Column(name = "rate")
	private String rate;

	@Column(name = "extra_fee")
	private String extraFee;

	@Column(name = "bank_card")
	private String bankCard;

	@Column(name = "cus_id")
	private String cusId;

	@Column(name = "outcus_id")
	private String outcusId;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
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

	public String getBankCard() {
		return bankCard;
	}

	public void setBankCard(String bankCard) {
		this.bankCard = bankCard;
	}

	public String getCusId() {
		return cusId;
	}

	public void setCusId(String cusId) {
		this.cusId = cusId;
	}

	public String getOutcusId() {
		return outcusId;
	}

	public void setOutcusId(String outcusId) {
		this.outcusId = outcusId;
	}
}
