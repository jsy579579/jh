package com.jh.paymentchannel.util.xj;

/**
 * Description
 *
 * @author Zoran
 * @date 2017/12/11
 */
public class OrderDTO extends BaseDTO {

    private String customerInfo;

    // 交易金额（单位分）
    private String totalFee;

    // 代理商订单编号
    private String agentOrderNo;

    // 回调通知URL
    private String notifyUrl;

    // 商户号
    private String mchId;

    private String returnUrl;

	public String getCustomerInfo() {
		return customerInfo;
	}

	public void setCustomerInfo(String customerInfo) {
		this.customerInfo = customerInfo;
	}

	public String getTotalFee() {
		return totalFee;
	}

	public void setTotalFee(String totalFee) {
		this.totalFee = totalFee;
	}

	public String getAgentOrderNo() {
		return agentOrderNo;
	}

	public void setAgentOrderNo(String agentOrderNo) {
		this.agentOrderNo = agentOrderNo;
	}

	public String getNotifyUrl() {
		return notifyUrl;
	}

	public void setNotifyUrl(String notifyUrl) {
		this.notifyUrl = notifyUrl;
	}

	public String getMchId() {
		return mchId;
	}

	public void setMchId(String mchId) {
		this.mchId = mchId;
	}

	public String getReturnUrl() {
		return returnUrl;
	}

	public void setReturnUrl(String returnUrl) {
		this.returnUrl = returnUrl;
	}
    
    
}
