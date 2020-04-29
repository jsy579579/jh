package com.jh.paymentgateway.util.ap.model;



/**
 * Created by pthahnil on 2019/6/12.
 */

public class BaseRespModel {

	/**
	 * 结果码
	 */
	private int resultCode;
	/**
	 * 错误码
	 */
	private String errorCode;
	/**
	 * 错误描述
	 */
	private String errorDesc;

	/**
	 * 商户号
	 */
	private String mchtNo;

	/**
	 * 请求唯一ID
	 */
	private String requestId;

	/**
	 * 返回的加密数据
	 */
	private String data;

	/**
	 * 签名
	 */
	private String sign;

	/**
	 * 请求时间
	 */
	private String respTime;

	/**
	 * 版本号(默认1.0.0)
	 */
	private String version;

	public int getResultCode() {
		return resultCode;
	}

	public void setResultCode(int resultCode) {
		this.resultCode = resultCode;
	}

	public String getErrorCode() {
		return errorCode;
	}

	public void setErrorCode(String errorCode) {
		this.errorCode = errorCode;
	}

	public String getErrorDesc() {
		return errorDesc;
	}

	public void setErrorDesc(String errorDesc) {
		this.errorDesc = errorDesc;
	}

	public String getMchtNo() {
		return mchtNo;
	}

	public void setMchtNo(String mchtNo) {
		this.mchtNo = mchtNo;
	}

	public String getRequestId() {
		return requestId;
	}

	public void setRequestId(String requestId) {
		this.requestId = requestId;
	}

	public String getData() {
		return data;
	}

	public void setData(String data) {
		this.data = data;
	}

	public String getSign() {
		return sign;
	}

	public void setSign(String sign) {
		this.sign = sign;
	}

	public String getRespTime() {
		return respTime;
	}

	public void setRespTime(String respTime) {
		this.respTime = respTime;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}
}
