package com.jh.paymentgateway.pojo.xkdhd;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;
import java.util.Date;

/**
 * @author zy
 * @date 2019/6/12
 * @description 钱嘉绑卡
 */
@Entity
@Table(name = "t_xkdhd_bindcard")
public class XKDHDBindCard implements Serializable {


	@Id
	@Column(name = "id")
	private long id;


	@Column(name ="order_no")
	private String orderNo;

	@Column(name ="sms_seq")
	private String smsSeq;

	@Column(name ="auth_code")
	private String authCode;

	@Column(name ="user_name")
	private String userName;

	@Column(name ="id_card")
	private String idCard;

	@Column(name="bank_card")
	private String bankCard;

	@Column(name="expired_time")
	private String expiredTime;

	@Column(name="security_code")
	private String securityCode;

	/**商户进行代扣操作的协议号，最长为32位纯数字,用于后续发起协议代扣。*/
	@Column(name = "treaty_id")
	private String treatyId;

	@Column(name = "create_time")
	private Date createTime;



	public String getOrderNo() {
		return orderNo;
	}

	public void setOrderNo(String orderNo) {
		this.orderNo = orderNo;
	}

	public String getSmsSeq() {
		return smsSeq;
	}

	public void setSmsSeq(String smsSeq) {
		this.smsSeq = smsSeq;
	}

	public String getAuthCode() {
		return authCode;
	}

	public void setAuthCode(String authCode) {
		this.authCode = authCode;
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

	public String getExpiredTime() {
		return expiredTime;
	}

	public void setExpiredTime(String expiredTime) {
		this.expiredTime = expiredTime;
	}

	public String getSecurityCode() {
		return securityCode;
	}

	public void setSecurityCode(String securityCode) {
		this.securityCode = securityCode;
	}

	public String getTreatyId() {
		return treatyId;
	}

	public void setTreatyId(String treatyId) {
		this.treatyId = treatyId;
	}

	public Date getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}
}
