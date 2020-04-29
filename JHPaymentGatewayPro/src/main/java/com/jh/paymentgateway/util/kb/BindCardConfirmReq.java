package com.jh.paymentgateway.util.kb;

import java.math.BigDecimal;


public class BindCardConfirmReq extends AbstractReq {

    private String userId;

    private String applyTradeNo;
    
    private String verCode;

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getApplyTradeNo() {
		return applyTradeNo;
	}

	public void setApplyTradeNo(String applyTradeNo) {
		this.applyTradeNo = applyTradeNo;
	}

	public String getVerCode() {
		return verCode;
	}

	public void setVerCode(String verCode) {
		this.verCode = verCode;
	}

	@Override
	public String toString() {
		return "BindCardConfirmReq [userId=" + userId + ", applyTradeNo=" + applyTradeNo + ", verCode=" + verCode + "]";
	}
    
   
}
