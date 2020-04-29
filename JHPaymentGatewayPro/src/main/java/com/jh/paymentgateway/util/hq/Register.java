package com.jh.paymentgateway.util.hq;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

/**
 * @author yanghuabang
 * 子商户报备信息
 */
@JsonInclude(Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class Register {
	
	private String subMerType;//子商户类型1：企业2：个体工商户3：个人

	private String subMerName;
	private String merShortName;
	private String merMobile;
	private String contacts;//联系人
	private String merLicense;//营业执照
	private String licenseValidity;
	
	
	private String legalName;
	private String legalMobile;
	private String legalIdNo;//法人身份证号
	private String legalValidity;//证件号码有效期
	private String legalValidityFlag;//证件号码是否长期 1:是2:否

	private String legalBankCard;
	private String legalBankName;
	private String legalBankCardType;
	private String legalBankBranchName;
	private String legalBankOpenProvince;
	private String legalBankOpenCity;
	
	
	private String merBankNo;
	private String merBankName;
	private String merBankBranchName;
	private String merBankCardType;
	
	
	private String merOpenName;
	private String merBankOpenProvince;
	private String merBankOpenCity;
	private String regCapital;//注册资金，可空
	private String minSettleAmout;
	private String riskDeposit;//风险保证金，可空
	private String email;//可空
	private String companyAddress;
	private String businessScope;//可空
	
	private String feeRate;
	private String	fixFee;
	
	
	public String getFeeRate() {
		return feeRate;
	}
	public void setFeeRate(String feeRate) {
		this.feeRate = feeRate;
	}
	public String getFixFee() {
		return fixFee;
	}
	public void setFixFee(String fixFee) {
		this.fixFee = fixFee;
	}
	public String getSubMerType() {
		return subMerType;
	}
	public void setSubMerType(String subMerType) {
		this.subMerType = subMerType;
	}
	public String getSubMerName() {
		return subMerName;
	}
	public void setSubMerName(String subMerName) {
		this.subMerName = subMerName;
	}
	public String getMerShortName() {
		return merShortName;
	}
	public void setMerShortName(String merShortName) {
		this.merShortName = merShortName;
	}
	public String getMerMobile() {
		return merMobile;
	}
	public void setMerMobile(String merMobile) {
		this.merMobile = merMobile;
	}
	public String getContacts() {
		return contacts;
	}
	public void setContacts(String contacts) {
		this.contacts = contacts;
	}
	public String getMerLicense() {
		return merLicense;
	}
	public void setMerLicense(String merLicense) {
		this.merLicense = merLicense;
	}
	public String getLicenseValidity() {
		return licenseValidity;
	}
	public void setLicenseValidity(String licenseValidity) {
		this.licenseValidity = licenseValidity;
	}
	public String getLegalName() {
		return legalName;
	}
	public void setLegalName(String legalName) {
		this.legalName = legalName;
	}
	public String getLegalMobile() {
		return legalMobile;
	}
	public void setLegalMobile(String legalMobile) {
		this.legalMobile = legalMobile;
	}
	public String getLegalIdNo() {
		return legalIdNo;
	}
	public void setLegalIdNo(String legalIdNo) {
		this.legalIdNo = legalIdNo;
	}
	public String getLegalValidity() {
		return legalValidity;
	}
	public void setLegalValidity(String legalValidity) {
		this.legalValidity = legalValidity;
	}
	public String getLegalValidityFlag() {
		return legalValidityFlag;
	}
	public void setLegalValidityFlag(String legalValidityFlag) {
		this.legalValidityFlag = legalValidityFlag;
	}
	public String getLegalBankCard() {
		return legalBankCard;
	}
	public void setLegalBankCard(String legalBankCard) {
		this.legalBankCard = legalBankCard;
	}
	public String getLegalBankName() {
		return legalBankName;
	}
	public void setLegalBankName(String legalBankName) {
		this.legalBankName = legalBankName;
	}
	public String getLegalBankCardType() {
		return legalBankCardType;
	}
	public void setLegalBankCardType(String legalBankCardType) {
		this.legalBankCardType = legalBankCardType;
	}
	public String getLegalBankBranchName() {
		return legalBankBranchName;
	}
	public void setLegalBankBranchName(String legalBankBranchName) {
		this.legalBankBranchName = legalBankBranchName;
	}
	public String getLegalBankOpenProvince() {
		return legalBankOpenProvince;
	}
	public void setLegalBankOpenProvince(String legalBankOpenProvince) {
		this.legalBankOpenProvince = legalBankOpenProvince;
	}
	public String getLegalBankOpenCity() {
		return legalBankOpenCity;
	}
	public void setLegalBankOpenCity(String legalBankOpenCity) {
		this.legalBankOpenCity = legalBankOpenCity;
	}
	public String getMerBankNo() {
		return merBankNo;
	}
	public void setMerBankNo(String merBankNo) {
		this.merBankNo = merBankNo;
	}
	public String getMerBankName() {
		return merBankName;
	}
	public void setMerBankName(String merBankName) {
		this.merBankName = merBankName;
	}
	public String getMerBankBranchName() {
		return merBankBranchName;
	}
	public void setMerBankBranchName(String merBankBranchName) {
		this.merBankBranchName = merBankBranchName;
	}
	public String getMerBankCardType() {
		return merBankCardType;
	}
	public void setMerBankCardType(String merBankCardType) {
		this.merBankCardType = merBankCardType;
	}
	public String getMerOpenName() {
		return merOpenName;
	}
	public void setMerOpenName(String merOpenName) {
		this.merOpenName = merOpenName;
	}
	public String getMerBankOpenProvince() {
		return merBankOpenProvince;
	}
	public void setMerBankOpenProvince(String merBankOpenProvince) {
		this.merBankOpenProvince = merBankOpenProvince;
	}
	public String getMerBankOpenCity() {
		return merBankOpenCity;
	}
	public void setMerBankOpenCity(String merBankOpenCity) {
		this.merBankOpenCity = merBankOpenCity;
	}
	public String getRegCapital() {
		return regCapital;
	}
	public void setRegCapital(String regCapital) {
		this.regCapital = regCapital;
	}
	public String getMinSettleAmout() {
		return minSettleAmout;
	}
	public void setMinSettleAmout(String minSettleAmout) {
		this.minSettleAmout = minSettleAmout;
	}
	public String getRiskDeposit() {
		return riskDeposit;
	}
	public void setRiskDeposit(String riskDeposit) {
		this.riskDeposit = riskDeposit;
	}
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	public String getCompanyAddress() {
		return companyAddress;
	}
	public void setCompanyAddress(String companyAddress) {
		this.companyAddress = companyAddress;
	}
	public String getBusinessScope() {
		return businessScope;
	}
	public void setBusinessScope(String businessScope) {
		this.businessScope = businessScope;
	}
	
	
}
