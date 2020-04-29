package com.jh.paymentgateway.util.sdk.pay.domain.split;

import com.jh.paymentgateway.util.sdk.domain.Response;

import java.util.UUID;

/**
 * 分账响应
 *
 */
public class SplitResponse extends Response {
	 private String outTradeNo;
	private String nonceStr = UUID.randomUUID().toString().replaceAll("-", "");

	public String getOutTradeNo() {
		return outTradeNo;
	}
	public void setOutTradeNo(String outTradeNo) {
		this.outTradeNo = outTradeNo;
	}
	public String getNonceStr() { return nonceStr; }
	public void setNonceStr(String nonceStr) {
		this.nonceStr = nonceStr;
	}
}
