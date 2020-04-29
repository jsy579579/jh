package com.jh.paymentgateway.pojo;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonFormat;

@Entity
@Table(name = "t_lmdh_register")
public class LMDHRegister implements Serializable {

	private static final long serialVersionUID = 147L;

	@Id
	@Column(name = "id")
	private long id;

	@Column(name = "phone")
	private String phone;

	@Column(name = "user_name")
	private String userName;

	@Column(name = "id_card")
	private String idCard;

	// 子商户编号
	@Column(name = "customer_num")
	private String customerNum;

	// 商户编号
	@Column(name = "main_customer_num")
	private String mainCustomerNum;

	@Column(name = "create_time")
	@JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
	private Date createTime;

	@Column(name = "change_time")
	@JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
	private Date changeTime;

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

	public String getCustomerNum() {
		return customerNum;
	}

	public void setCustomerNum(String customerNum) {
		this.customerNum = customerNum;
	}

	public String getMainCustomerNum() {
		return mainCustomerNum;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public void setMainCustomerNum(String mainCustomerNum) {
		this.mainCustomerNum = mainCustomerNum;
	}

	public Date getChangeTime() {
		return changeTime;
	}

	public void setChangeTime(Date changeTime) {
		this.changeTime = changeTime;
	}

}
