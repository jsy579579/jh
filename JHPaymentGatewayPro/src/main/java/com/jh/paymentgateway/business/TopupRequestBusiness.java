package com.jh.paymentgateway.business;

import java.util.Map;

public interface TopupRequestBusiness {
	
	public  Map<String, Object> topupRequest(Map<String,Object> params) throws Exception;

}
