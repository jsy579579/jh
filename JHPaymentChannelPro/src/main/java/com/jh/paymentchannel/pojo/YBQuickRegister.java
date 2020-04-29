package com.jh.paymentchannel.pojo;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonFormat;

@Entity
@Table(name="t_yb_register")
public class YBQuickRegister implements Serializable{
	
	private static final long serialVersionUID = 147L;
	
	@Id
	@Column(name="id")
	private long id;	
	
	@Column(name="phone")
	private String phone;
	//结算卡卡号
	@Column(name="bank_card")
	private String bankCard;
	
	@Column(name="id_card")
	private String idCard;
	
	//商户编号
	@Column(name="customer_num")
	private String customerNum;
	
	@Column(name="main_customer_num")
	private String mainCustomerNum;
	
	//费率
	@Column(name="rate")
	private String rate;
	
	//额外费用
	@Column(name="extra_fee")
	private String extraFee;
	
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

	public String getCustomerNum() {
		return customerNum;
	}

	public void setCustomerNum(String customerNum) {
		this.customerNum = customerNum;
	}

	public String getMainCustomerNum() {
		return mainCustomerNum;
	}

	public void setMainCustomerNum(String mainCustomerNum) {
		this.mainCustomerNum = mainCustomerNum;
	}

	
	
}
