package com.jh.paymentgateway.util.hqg;

/**
 * @author yanghuabang
 * 子商户
 */
public class SubMer {
	private String subMerType;//子商户类型，个人填3
	//当类型为个人的时候下面的参数可以为空
	/*private String subMerName;
	private String merShortName;
	private String merMobile;
	private String contacts;
	private String merLicense;
	private String licenseValidity;*/
	
	
	private String legalName;
	private String legalMobile;
	private String legalIdNo;//法人身份证号
	/*private String legalValidity;//证件号码有效期
*/	private String legalValidityFlag;//证件号码是否长期 1:是2:否

	private String legalBankCard;
	private String legalBankName;
	private String legalBankCardType;
	/*private String legalBankBranchName;
	private String legalBankOpenProvince;
	private String legalBankOpenCity;*/
	
	//可空
	/*private String merBankNo;
	private String merBankName;
	private String merBankBranchName;
	private String merBankCardType;*/
	
	
	private String merOpenName;
	/*private String merBankOpenProvince;
	private String merBankOpenCity;*/
	//private String regCapital;//注册资金，可空
	private String minSettleAmout;
	//private String riskDeposit;//风险保证金，可空
	//private String email;//可空
	private String companyAddress;
	//private String businessScope;//可空
	
	
	public String getSubMerType() {
		return subMerType;
	}
	
	public void setSubMerType(String subMerType) {
		this.subMerType = subMerType;
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
	
	public String getMerOpenName() {
		return merOpenName;
	}
	public void setMerOpenName(String merOpenName) {
		this.merOpenName = merOpenName;
	}
	
	public String getMinSettleAmout() {
		return minSettleAmout;
	}
	public void setMinSettleAmout(String minSettleAmout) {
		this.minSettleAmout = minSettleAmout;
	}
	
	public String getCompanyAddress() {
		return companyAddress;
	}
	public void setCompanyAddress(String companyAddress) {
		this.companyAddress = companyAddress;
	}
	
}
