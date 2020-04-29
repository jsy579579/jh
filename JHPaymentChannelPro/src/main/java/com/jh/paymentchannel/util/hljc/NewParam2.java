package com.jh.paymentchannel.util.hljc;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;


public class NewParam2 {

	/*public static void main(String[] args) throws Exception {

		String Key = "MIICdwIBADANBgkqhkiG9w0BAQEFAASCAmEwggJdAgEAAoGBAM1i1bY9wqNKPk0ps4USK9pM0BP0bY5n/NwOY2YHAefXJYTLp34YnIuEsZ+uRj72qvMgxHUMwwPlL9xuchU81/up6DDzS30BpbDXQBOdzNMDp+ivXIdqsR+8UwwdzgffatMBZkhGpOMXZWQ6zBK0vsPAVhDE41cj4hOZslm5ewIhAgMBAAECgYAd7PGwrQ0IF9A+E/5pPD2RgDGtRqcT4cjxE1OeURUQ/8Mitz2/XLyqg3oSByWLFQvRgwu89lAP6DvyBVGwEH5zlOyluGl2XM8g8gDiTMA/80c9akk6ZNPz+PA4Lkw/UEbZHNNqoODLv6zTya4eed70b3SHc/iXTskDogQN36+NBQJBAO7ZkDXw7Pf942lqjjusJBu3cb86DcLEjTymeciISIqE8WT8GqRuGih9uBOCcKEOF1W5ZrrnCsg/sX8vW6xaeaMCQQDcIieIv9p6sjze8jm2Bn+uWSmNilebXj+HCp1dVJwrvd8nPq6GG1DKAA5sB1R93o/84iEH+ULpOzXr30yOJdlrAkA0rXsmymoZD7+2IjAYbRDRpBXMLQuX5y2XMMgvOA93rXZn5Uoi9b2DLKcKdnxMqQTwfSFxGz+/hnypJlK7ooCtAkEA0mY2oSa2PJWFRpYAAPGfMdX4uFbkuxRO5dSIaf8HsWsuEcWAa59KDXgWULyEzjVeLBc5+PQONvun4wUvl6GndwJBAOwg0P0Y837h+tpX4hlEiXVFBWUuiukKtWmGCGRELtaqhiS+K0lLIM0oFT3ffMVllpFaQuLp5wpk2c9Gx6G71rY=";
		String Key1 = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQDNYtW2PcKjSj5NKbOFEivaTNAT9G2OZ/zcDmNmBwHn1yWEy6d+GJyLhLGfrkY+9qrzIMR1DMMD5S/cbnIVPNf7qegw80t9AaWw10ATnczTA6for1yHarEfvFMMHc4H32rTAWZIRqTjF2VkOswStL7DwFYQxONXI+ITmbJZuXsCIQIDAQAB";
		
    	Map<String, String> map = new HashMap<String, String>();
		map.put("version", "1.0");
		map.put("charset", "UTF-8");
		map.put("signType", "RSA");
		map.put("agentId", "1001001");
		map.put("nonceStr", "xyThPszsquv123");
    	
		TreeMap<String, String> param = new TreeMap<String, String>(map);
		String signInfo = "";
		for (String pkey : param.keySet()) {
			signInfo += pkey+"="+param.get(pkey)+"&";
		}
		signInfo = signInfo.substring(0, signInfo.length() - 1);
		System.out.println(signInfo);
		
		String publicKey = RSAUtils.readFile("C:\\Users\\hljc\\Desktop\\1001001_pub.pem", "UTF-8");
		String privateKey = RSAUtils.readFile("C:\\Users\\hljc\\Desktop\\1001001_prv.pem", "UTF-8");
		String publicKey = Key1;
		String privateKey = Key;
		String sign = Base64.encode(RSAUtils.encryptByPrivateKey(signInfo.getBytes(), privateKey));
		System.out.println(sign);
		System.out.println(new String(RSAUtils.decryptByPublicKey(Base64.decode(sign), publicKey)));
	}*/

}
