package com.jh.paymentchannel.util;

import java.io.Serializable;

public class WithDrawOrder implements Serializable{

	private static final long serialVersionUID = 1L;

	private String reqcode;
	
	
	private String rescode;
	
	private String resurl;
	
	private String resmsg;
	
	private String thirdordercode;


	

	public String getThirdordercode() {
		return thirdordercode;
	}


	public void setThirdordercode(String thirdordercode) {
		this.thirdordercode = thirdordercode;
	}


	public String getReqcode() {
		return reqcode;
	}


	public void setReqcode(String reqcode) {
		this.reqcode = reqcode;
	}


	public String getRescode() {
		return rescode;
	}


	public void setRescode(String rescode) {
		this.rescode = rescode;
	}


	public String getResurl() {
		return resurl;
	}


	public void setResurl(String resurl) {
		this.resurl = resurl;
	}


	public String getResmsg() {
		return resmsg;
	}


	public void setResmsg(String resmsg) {
		this.resmsg = resmsg;
	}
	
	
	
	
}
