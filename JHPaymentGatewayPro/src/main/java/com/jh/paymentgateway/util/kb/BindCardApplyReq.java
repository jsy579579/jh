package com.jh.paymentgateway.util.kb;

import java.math.BigDecimal;


public class BindCardApplyReq extends AbstractReq {

    private String userId;

    private String mobile;
    
    private String bankCardNo;
    
    private String expiredDate;

    private String cvv2;

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getMobile() {
		return mobile;
	}

	public void setMobile(String mobile) {
		this.mobile = mobile;
	}

	public String getBankCardNo() {
		return bankCardNo;
	}

	public void setBankCardNo(String bankCardNo) {
		this.bankCardNo = bankCardNo;
	}

	public String getExpiredDate() {
		return expiredDate;
	}

	public void setExpiredDate(String expiredDate) {
		this.expiredDate = expiredDate;
	}

	public String getCvv2() {
		return cvv2;
	}

	public void setCvv2(String cvv2) {
		this.cvv2 = cvv2;
	}

	@Override
	public String toString() {
		return "BindCardReq [userId=" + userId + ", mobile=" + mobile + ", bankCardNo=" + bankCardNo + ", expiredDate="
				+ expiredDate + ", cvv2=" + cvv2 + "]";
	}

    

}
