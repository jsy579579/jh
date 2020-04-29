package com.jh.paymentgateway.util.utils;


// 聚合大额的
public class LargeRepayChannel extends AbstractChannel {
	

	public static final String postUrl = "http://pay.huanqiuhuiju.com/authsys/api/auth/execute.do";
	
	public static final String transcode="051";

	public LargeRepayChannel() {
		super(transcode, postUrl);
		// TODO Auto-generated constructor stub
	}
	
}
	

