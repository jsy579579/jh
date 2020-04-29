package com.jh.paymentchannel.util.xj;

/**
 * Description
 *
 * @author Zoran
 * @date 2017/12/7
 */
public class BaseDTO {

    private String appId;

    private String sign;

    // 随机字符串
    private String nonceStr;

	public String getAppId() {
		return appId;
	}

	public void setAppId(String appId) {
		this.appId = appId;
	}

	public String getSign() {
		return sign;
	}

	public void setSign(String sign) {
		this.sign = sign;
	}

	public String getNonceStr() {
		return nonceStr;
	}

	public void setNonceStr(String nonceStr) {
		this.nonceStr = nonceStr;
	}
}
