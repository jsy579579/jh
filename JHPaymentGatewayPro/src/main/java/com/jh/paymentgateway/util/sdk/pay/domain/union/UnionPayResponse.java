package com.jh.paymentgateway.util.sdk.pay.domain.union;

import com.jh.paymentgateway.util.sdk.domain.Response;

public class UnionPayResponse extends Response {
	private String outTradeNo;
	private Long amount;
	private String respMsg;
	public String getOutTradeNo() {
		return outTradeNo;
	}
	public void setOutTradeNo(String outTradeNo) {
		this.outTradeNo = outTradeNo;
	}
	public Long getAmount() {
		return amount;
	}
	public void setAmount(Long amount) {
		this.amount = amount;
	}
	public String getRespMsg() {
		return respMsg;
	}
	public void setRespMsg(String respMsg) {
		this.respMsg = respMsg;
	}
	
}
