package com.jh.paymentchannel.pojo;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonFormat;

@Entity
@Table(name = "t_register_auth")
public class RegisterAuth {

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "id")
	private long id;

	// 请求码
	@Column(name = "request_id")
	private String requestId;

	// 手机号
	@Column(name = "mobile")
	private String mobile;

	// 身份证号
	@Column(name = "id_card")
	private String idCard;

	// 法人姓名
	@Column(name = "legal_person")
	private String legalPerson;

	// 单笔最小限额
	@Column(name = "min_settle_amoun")
	private String minSettleAmoun;

	// 结算周期
	@Column(name = "risk_reserve_day")
	private String riskReserveDay;

	// 开户行卡号
	@Column(name = "bank_account_number")
	private String bankAccountNumber;

	// 开户行名称
	@Column(name = "bank_name")
	private String bankName;

	// 子商户编号
	@Column(name = "customer_number")
	private String customerNumber;
	// 手续费
	@Column(name = "charge")
	private String charge;

	@Column(name = "status")
	private String status;

	@Column(name = "rate")
	private String rate;

	@Column(name = "create_time")
	@JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
	private Date createTime;

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getRequestId() {
		return requestId;
	}

	public void setRequestId(String requestId) {
		this.requestId = requestId;
	}

	public String getMobile() {
		return mobile;
	}

	public void setMobile(String mobile) {
		this.mobile = mobile;
	}

	public String getIdCard() {
		return idCard;
	}

	public void setIdCard(String idCard) {
		this.idCard = idCard;
	}

	public String getLegalPerson() {
		return legalPerson;
	}

	public void setLegalPerson(String legalPerson) {
		this.legalPerson = legalPerson;
	}

	public String getMinSettleAmoun() {
		return minSettleAmoun;
	}

	public void setMinSettleAmoun(String minSettleAmoun) {
		this.minSettleAmoun = minSettleAmoun;
	}

	public String getRiskReserveDay() {
		return riskReserveDay;
	}

	public void setRiskReserveDay(String riskReserveDay) {
		this.riskReserveDay = riskReserveDay;
	}

	public String getBankAccountNumber() {
		return bankAccountNumber;
	}

	public void setBankAccountNumber(String bankAccountNumber) {
		this.bankAccountNumber = bankAccountNumber;
	}

	public String getBankName() {
		return bankName;
	}

	public void setBankName(String bankName) {
		this.bankName = bankName;
	}

	public String getCustomerNumber() {
		return customerNumber;
	}

	public void setCustomerNumber(String customerNumber) {
		this.customerNumber = customerNumber;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getRate() {
		return rate;
	}

	public void setRate(String rate) {
		this.rate = rate;
	}

	public Date getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}

	public String getCharge() {
		return charge;
	}

	public void setCharge(String charge) {
		this.charge = charge;
	}

}
