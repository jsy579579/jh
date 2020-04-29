package com.jh.paymentgateway.util.sdk.pay.domain.protocol;

import com.jh.paymentgateway.util.sdk.domain.Response;

/**
 * 协议支付绑卡返回
 * @author yjw
 *
 */
public class ProtocolPayBindCardResponse extends Response {

    /**
     * 客户编码
     */
    private String customerCode;
    /**
     * 绑卡流水号
     */
    private String smsNo;
	
    /**
     * 会员编号
     */
    private String memberId;
	private String protocol;
    
    
	public String getCustomerCode() {
		return customerCode;
	}


	public void setCustomerCode(String customerCode) {
		this.customerCode = customerCode;
	}


	public String getSmsNo() {
		return smsNo;
	}


	public void setSmsNo(String smsNo) {
		this.smsNo = smsNo;
	}


	public String getMemberId() {
		return memberId;
	}


	public void setMemberId(String memberId) {
		this.memberId = memberId;
	}


	@Override
	public String toString() {
		return "ProtocolPayBindCardResponse [customerCode=" + customerCode + ", smsNo=" + smsNo + ", memberId="
				+ memberId + "]";
	}


	public String getProtocol() {
		return protocol;
	}
	public void setProtocol(String protocol) {
		this.protocol = protocol;
	}

	
}
