package com.jh.paymentgateway.pojo.qj;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;

/**
 * @author zy
 * @date 2019/6/12
 * @description 钱嘉绑卡
 */
@Entity
@Table(name = "t_qj_bindcard")
public class QJBindCard  implements Serializable {


	private static final long serialVersionUID = -7565858511994624531L;
	@Id
	@Column(name = "id")
	private String id;


	@Column(name ="kft_order_no")
	private String qjOrderNo;

	@Column(name ="bank_card")
	private String bankCard;

	@Column(name ="branch_code")
	private String branchCode;

	@Column(name ="phone")
	private String phone;

	@Column(name ="id_card")
	private String idCard;

	/**商户进行代扣操作的协议号，最长为32位纯数字,用于后续发起协议代扣。*/
	@Column(name = "treaty_id")
	private String treatyId;

	@Column(name ="user_name")
	private String userName;

	@Column(name ="expired_time")
	private String expiredTime;

	@Column(name ="security_code")
	private String securityCode;

	@Column(name ="extra_fee")
	private String extraFee;

	@Column(name ="status")
	private String status;

	@Column(name ="create_time")
	private String createTime;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getQjOrderNo() {
		return qjOrderNo;
	}

	public void setQjOrderNo(String qjOrderNo) {
		this.qjOrderNo = qjOrderNo;
	}

	public String getBankCard() {
		return bankCard;
	}

	public void setBankCard(String bankCard) {
		this.bankCard = bankCard;
	}

	public String getBranchCode() {
		return branchCode;
	}

	public void setBranchCode(String branchCode) {
		this.branchCode = branchCode;
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

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
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

	public String getExtraFee() {
		return extraFee;
	}

	public void setExtraFee(String extraFee) {
		this.extraFee = extraFee;
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

	public String getTreatyId() {
		return treatyId;
	}

	public void setTreatyId(String treatyId) {
		this.treatyId = treatyId;
	}
}
