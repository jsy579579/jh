package com.jh.user.pojo;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonFormat;

@Entity
@Table(name="t_bank_info")
public class UserBankInfo implements Serializable{

	private static final long serialVersionUID = 1L;

	
	@Id
	@Column(name="id")
	private int id;
	
	@Column(name="user_id")
	private long userId;
	
	/**支行名称*/
	@Column(name="bankbranch_name")
	private String bankBranchName;
	
	@Column(name="province")
	private String province;
	
	@Column(name="city")
	private String city;
	
	@Column(name="bill_day")
	private int billDay = 0;
	
	@Column(name="repayment_day")
	private int repaymentDay = 0;
	
	@Column(name="credit_blance")
	private BigDecimal creditBlance = BigDecimal.ZERO;

	@Column(name="user_name")
	private String userName;
	
	/**银行卡名字*/
	@Column(name="bank_name")
	private String bankName;
	    
	/**银行卡品牌*/
	@Column(name="bank_brand")
	private String bankBrand;

	/**卡的号码*/
	@Column(name="card_no")
	private String cardNo;

	/**联行号*/
	@Column(name="line_no")
	private String lineNo;

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
	private String idcard;
	
	
	/**卡名称*/
	@Column(name="card_type")
	private String cardType;
	
	
	/**对私还是对公帐户*/
	@Column(name="pri_or_pub")
	private String priOrPub = "0";
	
	/**
	 * 区别
	 * **/
	@Column(name="nature")
	private String nature;
	

	/**使用状态*/
	@Column(name="state")
	private String state;
	 
	
	/**是否默认结算卡*/
	@Column(name="is_def")
	private String idDef;
	
	
	/**logo*/
	@Column(name="logo")
	private String logo;
	
	/***绑卡类型**/
	@Column(name="type")
	private String type;
	
	/**创建时间*/
	@Column(name="create_time")
	@JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
	private Date   createTime;


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


	public BigDecimal getCreditBlance() {
		return creditBlance;
	}


	public void setCreditBlance(BigDecimal creditBlance) {
		this.creditBlance = creditBlance;
	}


	public static long getSerialversionuid() {
		return serialVersionUID;
	}


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


	@Override
	public String toString() {
		return "UserBankInfo [id=" + id + ", userId=" + userId + ", bankBranchName=" + bankBranchName + ", province="
				+ province + ", city=" + city + ", billDay=" + billDay + ", repaymentDay=" + repaymentDay
				+ ", creditBlance=" + creditBlance + ", userName=" + userName + ", bankName=" + bankName
				+ ", bankBrand=" + bankBrand + ", cardNo=" + cardNo + ", lineNo=" + lineNo + ", securityCode="
				+ securityCode + ", expiredTime=" + expiredTime + ", phone=" + phone + ", idcard=" + idcard
				+ ", cardType=" + cardType + ", priOrPub=" + priOrPub + ", nature=" + nature + ", state=" + state
				+ ", idDef=" + idDef + ", logo=" + logo + ", type=" + type + ", createTime=" + createTime + "]";
	}
	
	
	
	
	
}
