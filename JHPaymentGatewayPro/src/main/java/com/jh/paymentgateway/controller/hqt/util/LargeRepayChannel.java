package com.jh.paymentgateway.controller.hqt.util;


// 聚合大额的
public class LargeRepayChannel extends AbstractChannel {
	

//	public static final String postUrl = "http://pay.huanqiuhuiju.com/authsys/api/auth/execute.do";
//
//	public static final String transcode="051";


	public static final String postUrl = "http://pay.huanqiuhuiju.com/authsys/api/channel/pay/execute.do";//小额url


	public static final String transcode="050";  //小额

	public LargeRepayChannel() {
		super(transcode, postUrl);
		// TODO Auto-generated constructor stub
	}
	
}
	

