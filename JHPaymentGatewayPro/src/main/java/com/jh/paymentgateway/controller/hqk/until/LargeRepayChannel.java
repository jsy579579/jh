package com.jh.paymentgateway.controller.hqk.until;


// 聚合大额的
public class LargeRepayChannel extends AbstractChannel  {


	public static final String postUrl = "http://pay.huanqiuhuiju.com/authsys/api/sdj/pay/execute.do";

	public static final String transcode="053";

	public LargeRepayChannel() {
		super(transcode, postUrl);
		// TODO Auto-generated constructor stub
	}
	
}
	

