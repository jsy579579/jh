package com.jh.paymentgateway.util.sdk.pay.domain.nativePay;

import com.jh.paymentgateway.util.sdk.domain.Response;

import java.util.UUID;

public class NativePayResponse  extends Response {

    /**
     * 扫码URL
     */
    private String codeUrl;
    /**
     * 商户单号
     */
    private String outTradeNo;
    /**
     * 支付金额
     */
    private Long amount;

	private String nonceStr = UUID.randomUUID().toString().replaceAll("-", "");

    public String getCodeUrl() {
        return codeUrl;
    }

    public void setCodeUrl(String codeUrl) {
        this.codeUrl = codeUrl;
    }

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

    public String getNonceStr() {
        return nonceStr;
    }

    public void setNonceStr(String nonceStr) {
        this.nonceStr = nonceStr;
    }
}
