package com.jh.paymentgateway.util.sdk.domain;

/**
 * @Author : Author
 * @Date : 2018/1/29 14:48
 * @Description : 下单返回参数封装
 */
public class Response {

	/**
	 * 返回状态码; 0000：处理成功;0000：处理成功
	 */
	private String returnCode;

	/**
	 * 打印控制台信息。注意：此属性仅用于页面显示调试信息，实际交易中并不存在
	 */
	private String consoleMsg;

	/**
	 * 信息
	 */
	private String returnMsg;

	private String inputJSON;
	
	private String outputJSON;
	
	public String getReturnCode() {
		return returnCode;
	}

	public void setReturnCode(String returnCode) {
		this.returnCode = returnCode;
	}

	public String getReturnMsg() {
		return returnMsg;
	}

	public void setReturnMsg(String returnMsg) {
		this.returnMsg = returnMsg;
	}

	public String getConsoleMsg() {
		return consoleMsg;
	}

	public void setConsoleMsg(String consoleMsg) {
		// this.consoleMsg = consoleMsg;
	}

	public String getInputJSON() {
		return inputJSON;
	}

	public void setInputJSON(String inputJSON) {
		this.inputJSON = inputJSON;
	}

	public String getOutputJSON() {
		return outputJSON;
	}

	public void setOutputJSON(String outputJSON) {
		this.outputJSON = outputJSON;
	}
}
