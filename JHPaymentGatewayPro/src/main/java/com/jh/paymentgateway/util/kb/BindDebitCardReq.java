package com.jh.paymentgateway.util.kb;

import java.math.BigDecimal;


public class BindDebitCardReq extends AbstractReq {

    private String userId;

    private String mobile;
    
    private String bankCardNo;

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

	@Override
	public String toString() {
		return "BindDebitCardReq [userId=" + userId + ", mobile=" + mobile + ", bankCardNo=" + bankCardNo + "]";
	}
    
	
    
   
}
