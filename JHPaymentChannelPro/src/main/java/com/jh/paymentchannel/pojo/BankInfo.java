package com.jh.paymentchannel.pojo;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonFormat;

public class BankInfo implements Serializable{

	private static final long serialVersionUID = 1L;
	
	private int id;

	private long userId;
	
	private String bankBranchName;

	private String province;

	private String city;
	
	public String getBankBranchName() {
		return bankBranchName;
	}


	public void setBankBranchName(String bankBranchName) {
		this.bankBranchName = bankBranchName;
	}


	public String getProvince() {
		return province;
	}


	public void setProvince(String province) {
		this.province = province;
	}


	public String getCity() {
		return city;
	}


	public void setCity(String city) {
		this.city = city;
	}


	private String userName;
	
	private String bankName;
	    
	private String bankBrand;

	private String cardNo;

	private String lineNo;

	private String securityCode;

	private String expiredTime;//有效期

	/**预留手机号码*/
	private String phone;

	/**身份证号*/
	private String idcard;
	
	
	/**卡名称*/
	private String cardType;
	
	
	/**对私还是对公帐户*/
	private String priOrPub = "0";
	
	/**
	 * 区别
	 * **/
	private String nature;
	

	/**使用状态*/
	private String state;
	 
	
	/**是否默认结算卡*/
	private String idDef;
	
	
	/**logo*/
	private String logo;
	
	/***绑卡类型**/
	private String type;
	
	/**创建时间*/
	private Date   createTime;
	
	private String useState="0";

	
	public int getId() {
		return id;
	}


	public void setId(int id) {
		this.id = id;
	}


	public long getUserId() {
		return userId;
	}


	public void setUserId(long userId) {
		this.userId = userId;
	}


	public String getBankName() {
		return bankName;
	}


	public void setBankName(String bankName) {
		this.bankName = bankName;
	}


	public String getBankBrand() {
		return bankBrand;
	}


	public void setBankBrand(String bankBrand) {
		this.bankBrand = bankBrand;
	}


	public String getCardNo() {
		return cardNo;
	}


	public void setCardNo(String cardNo) {
		this.cardNo = cardNo;
	}


	public String getLineNo() {
		return lineNo;
	}


	public void setLineNo(String lineNo) {
		this.lineNo = lineNo;
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


	public String getCardType() {
		return cardType;
	}


	public void setCardType(String cardType) {
		this.cardType = cardType;
	}


	public String getNature() {
		return nature;
	}


	public void setNature(String nature) {
		this.nature = nature;
	}


	public String getState() {
		return state;
	}


	public void setState(String state) {
		this.state = state;
	}


	public Date getCreateTime() {
		return createTime;
	}


	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}


	public String getIdcard() {
		return idcard;
	}


	public void setIdcard(String idcard) {
		this.idcard = idcard;
	}


	public String getIdDef() {
		return idDef;
	}


	public void setIdDef(String idDef) {
		this.idDef = idDef;
	}


	public String getLogo() {
		return logo;
	}


	public void setLogo(String logo) {
		this.logo = logo;
	}


	public String getUserName() {
		return userName;
	}


	public void setUserName(String userName) {
		this.userName = userName;
	}


	public String getPriOrPub() {
		return priOrPub;
	}


	public void setPriOrPub(String priOrPub) {
		this.priOrPub = priOrPub;
	}


	public String getType() {
		return type;
	}


	public void setType(String type) {
		this.type = type;
	}


	public String getUseState() {
		return useState;
	}


	public void setUseState(String useState) {
		this.useState = useState;
	}
	
	private int billDay;

	private int repaymentDay;

	private BigDecimal creditBlance;

	public int getBillDay() {
		return billDay;
	}

	public void setBillDay(int billDay) {
		this.billDay = billDay;
	}

	public int getRepaymentDay() {
		return repaymentDay;
	}

	public void setRepaymentDay(int repaymentDay) {
		this.repaymentDay = repaymentDay;
	}

	public BigDecimal getCreditBlance() {
		return creditBlance;
	}

	public void setCreditBlance(BigDecimal creditBlance) {
		this.creditBlance = creditBlance;
	}
}
