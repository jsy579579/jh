package com.jh.paymentgateway.util.kb;

import java.math.BigDecimal;


public class FastPayApplyReq extends AbstractReq {

    private String userId;

    private String creditCardNo;
    
    private String creditMobile;
    
    private String cvv2;
    
    private String expiredDate;
    
    private String debitCardNo;
    
    private String debitMobile;
    
    private String productCode;
    
    private String amount;

    private String feeRate;
    
    private String fixFee;
    
    private String backUrl;

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getCreditCardNo() {
		return creditCardNo;
	}

	public void setCreditCardNo(String creditCardNo) {
		this.creditCardNo = creditCardNo;
	}

	public String getCreditMobile() {
		return creditMobile;
	}

	public void setCreditMobile(String creditMobile) {
		this.creditMobile = creditMobile;
	}

	public String getCvv2() {
		return cvv2;
	}

	public void setCvv2(String cvv2) {
		this.cvv2 = cvv2;
	}

	public String getExpiredDate() {
		return expiredDate;
	}

	public void setExpiredDate(String expiredDate) {
		this.expiredDate = expiredDate;
	}

	public String getDebitCardNo() {
		return debitCardNo;
	}

	public void setDebitCardNo(String debitCardNo) {
		this.debitCardNo = debitCardNo;
	}

	public String getDebitMobile() {
		return debitMobile;
	}

	public void setDebitMobile(String debitMobile) {
		this.debitMobile = debitMobile;
	}

	public String getProductCode() {
		return productCode;
	}

	public void setProductCode(String productCode) {
		this.productCode = productCode;
	}

	public String getAmount() {
		return amount;
	}

	public void setAmount(String amount) {
		this.amount = amount;
	}

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

	public String getBackUrl() {
		return backUrl;
	}

	public void setBackUrl(String backUrl) {
		this.backUrl = backUrl;
	}

	@Override
	public String toString() {
		return "FastPayApplyReq [userId=" + userId + ", creditCardNo=" + creditCardNo + ", creditMobile=" + creditMobile
				+ ", cvv2=" + cvv2 + ", expiredDate=" + expiredDate + ", debitCardNo=" + debitCardNo + ", debitMobile="
				+ debitMobile + ", productCode=" + productCode + ", amount=" + amount + ", feeRate=" + feeRate
				+ ", fixFee=" + fixFee + ", backUrl=" + backUrl + "]";
	}

	
    

}
