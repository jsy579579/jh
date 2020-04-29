package com.jh.paymentgateway.pojo;

import com.fasterxml.jackson.annotation.JsonFormat;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

@Entity
@Table(name = "t_syb_bindcard")
public class SYBBindCard implements Serializable {


	private static final long serialVersionUID = -2650222392015268745L;
	@Id
	@Column(name = "id")
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private long id;

	@Column(name = "phone")
	private String phone;

	@Column(name = "idcard")
	private String idCard;

	@Column(name = "bank_card")
	private String bankCard;

	@Column(name = "status")
	private String status;
	
	@Column(name = "user_name")
	private String userName;
	
	@Column(name = "agree_id")
	private String agreeId;    //绑卡完成返回协议编号

	@Column(name = "validdate")
	private String validdate;    //有效期

	@Column(name = "cvv")
	private String cvv;     //安全码

	@Column(name = "meruser_id")
	private String meruserId;     //安全码

	@Column(name = "create_time")
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

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getAgreeId() {
		return agreeId;
	}

	public void setAgreeId(String agreeId) {
		this.agreeId = agreeId;
	}

	public String getValiddate() {
		return validdate;
	}

	public void setValiddate(String validdate) {
		this.validdate = validdate;
	}

	public String getCvv() {
		return cvv;
	}

	public void setCvv(String cvv) {
		this.cvv = cvv;
	}

	public String getMeruserId() {
		return meruserId;
	}

	public void setMeruserId(String meruserId) {
		this.meruserId = meruserId;
	}

	public Date getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}

	
	

}
