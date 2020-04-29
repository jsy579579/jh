package com.jh.paymentgateway.controller.hqk.until;


import java.util.Map;


/**
 * 小额聚合
 * 
 * 实际调用的接口在抽象类里面。
 * 
 * 
 * */ 
 
public class SmartRepayChannel extends AbstractChannel {

	public static final String postUrl = "http://pay.huanqiuhuiju.com/authsys/api/sdj/pay/execute.do"; // 请求地址
	
	public static final String transcode="053";// 小额的交易码

	public SmartRepayChannel() {
		super(transcode, postUrl);
		// TODO Auto-generated constructor stub
	}


}
