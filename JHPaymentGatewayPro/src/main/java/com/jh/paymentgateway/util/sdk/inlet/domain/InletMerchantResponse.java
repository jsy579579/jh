package com.jh.paymentgateway.util.sdk.inlet.domain;

import com.jh.paymentgateway.util.sdk.domain.Response;

import java.util.UUID;

/**
 * Created by adm on 2018/1/23.
 */
public class InletMerchantResponse extends Response {

    private String customerInfoId;
    private String memberId;
	public String getMemberId() {
		return memberId;
	}

	public void setMemberId(String memberId) {
		this.memberId = memberId;
	}

	private String nonceStr = UUID.randomUUID().toString().replaceAll("-", "");

    public String getCustomerInfoId() {
        return customerInfoId;
    }

    public void setCustomerInfoId(String customerInfoId) {
        this.customerInfoId = customerInfoId;
    }

    public String getNonceStr() {
        return nonceStr;
    }

    public void setNonceStr(String nonceStr) {
        this.nonceStr = nonceStr;
    }


}
