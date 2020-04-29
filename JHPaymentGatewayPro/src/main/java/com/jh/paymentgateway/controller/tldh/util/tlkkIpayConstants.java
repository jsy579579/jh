package com.jh.paymentgateway.controller.tldh.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class tlkkIpayConstants {
	public static final String SYB_API_DOMAIN ="https://ipay.allinpay.com/apiweb";//测试


//	public static final String SYB_ORGAPPID = "0000495";
//	public static final String SYB_ORGID = "201003992003";
//	public static final String SYB_ORGAPPKEY = "c6e4c2daaf920ff804f674c0213b462c";
//	public static final String SYB_CUSID = "101000000134";//5505810445802N1 快捷
//
//


	public static final String SYB_ORGAPPID = "0001124";
	public static final String SYB_ORGID = "201005016547";
	public static final String SYB_ORGAPPKEY = "f232ddcfaca8ec070610aee82921d72c";
	//百也特 通联密钥
	//public static String KEY="qdtaVmtDhIW&ld7%jIBiL@*@5xrgZ@!S";

	public static String KEY;

	@Value("${tl.key}")
	public void setPrivateKey(String key){
		KEY=key;
	}
	public static final String SYB_CUSID = "101000000134";//5505810445802N1 快捷



	public static final String SYB_APIURL_QPAY = SYB_API_DOMAIN+"/qpay";//快捷
}
