package com.jh.paymentgateway.util.ap.model;



/**
 * Created by pthahnil on 2019/6/11.
 */

public class BaseReqModel {

	/**
	 * 商户号
	 */
	private String mchtNo;

	/**
	 * 签名
	 */
	private String sign;

	/**
	 * json串加密后的数据
	 */
	private String data;

	/**
	 * 服务编码
	 */
	private String serviceCode;

	/**
	 * 产品号
	 */
	private String productCode;

	/**
	 * 请求唯一ID
	 */
	private String requestId;

	/**
	 * 请求时间
	 */
	private String reqTime;

	/**
	 * 版本号(默认1.0.0)
	 */
	private String version;

	public String getMchtNo() {
		return mchtNo;
	}

	public void setMchtNo(String mchtNo) {
		this.mchtNo = mchtNo;
	}

	public String getSign() {
		return sign;
	}

	public void setSign(String sign) {
		this.sign = sign;
	}

	public String getData() {
		return data;
	}

	public void setData(String data) {
		this.data = data;
	}

	public String getServiceCode() {
		return serviceCode;
	}

	public void setServiceCode(String serviceCode) {
		this.serviceCode = serviceCode;
	}

	public String getProductCode() {
		return productCode;
	}

	public void setProductCode(String productCode) {
		this.productCode = productCode;
	}

	public String getRequestId() {
		return requestId;
	}

	public void setRequestId(String requestId) {
		this.requestId = requestId;
	}

	public String getReqTime() {
		return reqTime;
	}

	public void setReqTime(String reqTime) {
		this.reqTime = reqTime;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}
}
