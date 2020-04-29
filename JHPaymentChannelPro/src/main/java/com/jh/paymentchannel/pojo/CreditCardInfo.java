package com.jh.paymentchannel.pojo;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonFormat;

@Entity
@Table(name="t_credit_card_info")
public class CreditCardInfo implements Serializable{

	private static final long serialVersionUID = 1L;

	
	@Id
	@Column(name="id")
	private long id;
	@Column(name="user_name")
	private String userName;
	/**银行卡名字*/
	@Column(name="bank_name")
	private String bankName;
	/**卡的号码*/
	@Column(name="card_no")
	private String cardNo;
	/**安全码 信用卡后3位*/
	@Column(name="security_code")
	private String securityCode = "0";
	/**有效期*/
	@Column(name="expired_time")
	private String expiredTime = "0";//有效期
	/**预留手机号码*/
	@Column(name="phone")
	private String phone;
	/**身份证号*/
	@Column(name="id_card")
	private String idCard;
	/**创建时间*/
	@Column(name="create_time")
	@JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
	private Date   createTime;
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
	public String getBankName() {
		return bankName;
	}
	public void setBankName(String bankName) {
		this.bankName = bankName;
	}
	public String getCardNo() {
		return cardNo;
	}
	public void setCardNo(String cardNo) {
		this.cardNo = cardNo;
	}
	public String getSecurityCode() {
		return securityCode;
	}
	public void setSecurityCode(String securityCode) {
		this.securityCode = securityCode;
	}
	public String getExpiredTime() {
		return expiredTime;
	}
	public void setExpiredTime(String expiredTime) {
		this.expiredTime = expiredTime;
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
	
}
