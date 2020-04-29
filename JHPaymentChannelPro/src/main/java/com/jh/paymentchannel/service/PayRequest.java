package com.jh.paymentchannel.service;

import com.jh.paymentchannel.util.WithDrawOrder;


public interface PayRequest {

	public  WithDrawOrder payRequest(String ordercode,String cardno, String username, String amount, String bankname,  String phone, String priOrpub,String notifyURL,String returnURL);
	
	public WithDrawOrder  queryPay(String ordercode);
	
}
