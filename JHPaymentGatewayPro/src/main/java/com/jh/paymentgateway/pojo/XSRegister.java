package com.jh.paymentgateway.pojo;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonFormat;

@Entity
@Table(name="t_xs_register")
public class XSRegister implements Serializable {

	/**
	 * <p>Description: </p>
	 */
	private static final long serialVersionUID = 1L;
	
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@Column(name="id")
	private Long id;
	/**
	 * 我们系统用户编号
	 */
	@Column(name="user_id")
	private String userId;
	/**
	 * 使用手机号
	 */
	@Column(name="user_phone")
	private String userPhone;
	/**
	 * 帐号姓名
	 */
	@Column(name="user_account")
	private String userAccount;
	/**
	 * 身份证号码
	 */
	@Column(name="id_card")
	private String idCard;
	/**
	 * 绑定结算卡号
	 */
	@Column(name="settle_bank_no")
	private String settleBankNo;
	/**
	 * 绑定结算卡手机号
	 */
	@Column(name="settle_bank_phone")
	private String settleBankPhone;
	/**
	 * 套现费率
	 */
	@Column(name="tx_rate")
	private BigDecimal txRate = BigDecimal.ZERO;
	/**
	 * 套现手续费
	 */
	@Column(name="tx_charge")
	private BigDecimal txCharge = BigDecimal.ZERO;
	/**
	 * 还款费率
	 */
	@Column(name="hk_rate")
	private BigDecimal hkRate = BigDecimal.ZERO;
	/**
	 * 还款手续费
	 */
	@Column(name="hk_charge")
	private BigDecimal hkCharge = BigDecimal.ZERO;
	/**
	 * 通道商户编号
	 */
	@Column(name="user_code")
	private String userCode;
	/**
	 * 通道商户密钥
	 */
	@Column(name="user_key")
	private String userKey;
	@Column(name="create_time")
	@JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
	private Date createTime;
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getUserId() {
		return userId;
	}
	public void setUserId(String userId) {
		this.userId = userId;
	}
	public String getUserPhone() {
		return userPhone;
	}
	public void setUserPhone(String userPhone) {
		this.userPhone = userPhone;
	}
	public String getUserAccount() {
		return userAccount;
	}
	public void setUserAccount(String userAccount) {
		this.userAccount = userAccount;
	}
	public String getIdCard() {
		return idCard;
	}
	public void setIdCard(String idCard) {
		this.idCard = idCard;
	}
	public String getSettleBankNo() {
		return settleBankNo;
	}
	public void setSettleBankNo(String settleBankNo) {
		this.settleBankNo = settleBankNo;
	}
	public String getSettleBankPhone() {
		return settleBankPhone;
	}
	public void setSettleBankPhone(String settleBankPhone) {
		this.settleBankPhone = settleBankPhone;
	}
	public String getUserCode() {
		return userCode;
	}
	public void setUserCode(String userCode) {
		this.userCode = userCode;
	}
	public String getUserKey() {
		return userKey;
	}
	public void setUserKey(String userKey) {
		this.userKey = userKey;
	}
	public Date getCreateTime() {
		return createTime;
	}
	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}
	public static long getSerialversionuid() {
		return serialVersionUID;
	}
	public BigDecimal getTxRate() {
		return txRate;
	}
	public void setTxRate(BigDecimal txRate) {
		this.txRate = txRate;
	}
	public BigDecimal getHkRate() {
		return hkRate;
	}
	public void setHkRate(BigDecimal hkRate) {
		this.hkRate = hkRate;
	}
	public BigDecimal getTxCharge() {
		return txCharge;
	}
	public void setTxCharge(BigDecimal txCharge) {
		this.txCharge = txCharge;
	}
	public BigDecimal getHkCharge() {
		return hkCharge;
	}
	public void setHkCharge(BigDecimal hkCharge) {
		this.hkCharge = hkCharge;
	}
}
