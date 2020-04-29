package com.jh.paymentgateway.util.kb;

import java.math.BigDecimal;


public class FastPayConfirmReq extends AbstractReq {

    private String orgOrderNo;

    private String applyTradeNo;
    
    private String verCode;

	public String getOrgOrderNo() {
		return orgOrderNo;
	}

	public void setOrgOrderNo(String orgOrderNo) {
		this.orgOrderNo = orgOrderNo;
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
		return "FastPayConfirmReq [orgOrderNo=" + orgOrderNo + ", applyTradeNo=" + applyTradeNo + ", verCode=" + verCode
				+ "]";
	}
    
    
    

}
