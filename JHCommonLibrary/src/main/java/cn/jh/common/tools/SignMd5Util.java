package cn.jh.common.tools;

import java.util.List;
import java.util.TreeMap;

import org.apache.commons.lang.StringUtils;
import org.apache.http.message.BasicNameValuePair;

import cn.jh.common.utils.Md5Util;

public class SignMd5Util {

	
	 public static String signData(List<BasicNameValuePair> nvps, String privatekey) throws Exception {
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
	       //String signData = RSAUtil.signByPrivate(signatureStr, RSAUtil.readFile(privatekeyPath, "UTF-8"), "UTF-8");
	        //System.out.println("请求数据：" + signatureStr + "&signature=" + signData);
	        return Md5Util.getMD5(signatureStr+privatekey);
	    }
	
	
}
