package com.jh.paymentgateway.util;



import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

import org.apache.commons.lang.StringUtils;
import org.apache.http.message.BasicNameValuePair;

public class SignUtils {

	/***/
	public static String signMD5Data(List<BasicNameValuePair> nvps, String merkey) throws Exception {
        TreeMap<String, String> tempMap = new TreeMap<String, String>();
        for (BasicNameValuePair pair : nvps) {
            if (StringUtils.isNotBlank(pair.getValue())) {
                tempMap.put(pair.getName(), pair.getValue());
            }
        }
        StringBuffer buf = new StringBuffer();
        for (String key : tempMap.keySet()) {
            buf.append(key).append("=").append((String) tempMap.get(key)).append("&");
        }
        String signatureStr = buf.substring(0, buf.length() - 1);
       // KeyInfo keyInfo = RSAUtil.getPFXPrivateKey(privatePfxPath,keypass);
        //String signData = RSAUtil.signByPrivate(signatureStr, keyInfo.getPrivateKey(), "UTF-8");
        String signData = signatureStr+"&key="+merkey;
        return MD5.MD5Encode(signData).toUpperCase();
    }
	
	
    public static String signData(List<BasicNameValuePair> nvps, String privatekeyPath) throws Exception {
        TreeMap<String, String> tempMap = new TreeMap<String, String>();
        for (BasicNameValuePair pair : nvps) {
            if (StringUtils.isNotBlank(pair.getValue())) {
                tempMap.put(pair.getName(), pair.getValue());
            }
        }
        StringBuffer buf = new StringBuffer();
        for (String key : tempMap.keySet()) {
            buf.append(key).append("=").append((String) tempMap.get(key)).append("&");
        }
        String signatureStr = buf.substring(0, buf.length() - 1);
       // KeyInfo keyInfo = RSAUtil.getPFXPrivateKey(privatePfxPath,keypass);
        //String signData = RSAUtil.signByPrivate(signatureStr, keyInfo.getPrivateKey(), "UTF-8");
        String signData = RSAUtil.signByPrivate(signatureStr, RSAUtil.readFile(privatekeyPath, "UTF-8"), "UTF-8");
        System.out.println("请求数据：" + signatureStr + "&signature=" + signData);
        return signData;
    }

    public static boolean verferSignData(String str, String pubkeypath) {
        System.out.println("响应数据：" + str);
        String data[] = str.split("&");
        StringBuffer buf = new StringBuffer();
        String signature = "";
        for (int i = 0; i < data.length; i++) {
            String tmp[] = data[i].split("=", 2);
            if ("signature".equals(tmp[0])) {
                signature = tmp[1];
            } else {
                buf.append(tmp[0]).append("=").append(tmp[1]).append("&");
            }
        }
        String signatureStr = buf.substring(0, buf.length() - 1);
        System.out.println("验签数据：" + signatureStr);
        return RSAUtil.verifyByKeyPath(signatureStr, signature, pubkeypath, "UTF-8");
    }
    
    
   /* public static void main(String[] args){
    	
    	List<BasicNameValuePair> nvps = new ArrayList<BasicNameValuePair>();
	    nvps.add(new BasicNameValuePair("accNo", "4312"));
	    nvps.add(new BasicNameValuePair("accessMode", "01"));
	    nvps.add(new BasicNameValuePair("accessType", "0"));
	    nvps.add(new BasicNameValuePair("backUrl", "http://"));
	    
	    
	    
	    nvps.add(new BasicNameValuePair("bizType", "000000"));
	    nvps.add(new BasicNameValuePair("currency", "CNY"));
	    nvps.add(new BasicNameValuePair("customerInfo", "{'certifTp':'01','certify_id':'','customerNm':'','phoneNo':'13682102678'}"));
	    nvps.add(new BasicNameValuePair("frontUrl", "http://localhost&merId=2000000000003"));
	    
	    
	    nvps.add(new BasicNameValuePair("merOrderId", "P16010716485332N275JG"));
	    nvps.add(new BasicNameValuePair("payType", "0002"));
	    nvps.add(new BasicNameValuePair("txnAmt", "1"));
	    nvps.add(new BasicNameValuePair("txnSubType", "01"));
	    
	    
	    nvps.add(new BasicNameValuePair("txnTime", "20160107164853"));
	    nvps.add(new BasicNameValuePair("txnType", "01"));
	    nvps.add(new BasicNameValuePair("version", "1.0.0"));
	    
	    try {
			System.out.println(signMD5Data(nvps, "88888888"));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
    }*/
    
    
}
