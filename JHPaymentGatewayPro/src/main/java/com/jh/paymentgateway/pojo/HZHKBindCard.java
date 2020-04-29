package com.jh.paymentgateway.pojo;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonFormat;

@Entity
@Table(name = "t_hzhk_bindcard")
public class HZHKBindCard {
	
	private static final long serialVersionUID = 164L;
	
	@Id
	@Column(name = "id")
	private long id;

	@Column(name = "bank_card")
	private String bankCard;
	
	@Column(name = "id_card")
	private String idCard;
	
	@Column(name = "user_name")
	private String userName;
	
	@Column(name = "config_no")
	private String configNo;
	
	@Column(name = "ypt_order_no")
	private String yptOrderno;
	
	@Column(name = "status")
	private String status;
	
	@Column(name="create_time")
	@JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
	private String createTime;

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
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

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}
	
	public String getYptOrderno() {
		return yptOrderno;
	}

	public void setYptOrderno(String yptOrderno) {
		this.yptOrderno = yptOrderno;
	}

	public String getConfigNo() {
		return configNo;
	}

	public void setConfigNo(String configNo) {
		this.configNo = configNo;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getCreateTime() {
		return createTime;
	}

	public void setCreateTime(String createTime) {
		this.createTime = createTime;
	}

	public static long getSerialversionuid() {
		return serialVersionUID;
	}
	
}
