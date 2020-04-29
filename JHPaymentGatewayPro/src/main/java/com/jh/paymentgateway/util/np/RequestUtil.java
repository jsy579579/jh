package com.jh.paymentgateway.util.np;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;





public class RequestUtil {
	private static final Log logger = LogFactory.getLog(RequestUtil.class);
	static final String encode = "UTF-8";
	public static boolean checkParam(Map<String,String> params,String key){
        boolean result = false;
        if(params.containsKey("sign")){
            String sign = params.get("sign");
            params.remove("sign");
            StringBuilder buf = new StringBuilder((params.size() +1) * 10);
            RequestUtil.buildPayParams(buf,params,false);
            String preStr = buf.toString();
            String signRecieve = MD5.sign(preStr, "&key=" + key, "utf-8");
            result = sign.equalsIgnoreCase(signRecieve);
        }
        return result;
    }
	/**
     * @author 
     * @param payParams
     * @return
     */
    public static void buildPayParams(StringBuilder sb,Map<String, String> payParams,boolean encoding){
        List<String> keys = new ArrayList<String>(payParams.keySet());
        Collections.sort(keys);
        for(String key : keys){
            sb.append(key).append("=");
            if(encoding){
                sb.append(urlEncode(payParams.get(key)));
            }else{
                sb.append(payParams.get(key));
            }
            sb.append("&");
        }
        sb.setLength(sb.length() - 1);
    }
    public static String urlEncode(String str){
        try {
            return URLEncoder.encode(str, "utf-8");
        } catch (Throwable e) {
            return str;
        } 
    }
	/**
	 * 将map转换成url的queryString排好序的字符串
	 * @param packageParams
	 * @return
	 */
	public static String getRequestURL(SortedMap<String, String> packageParams){ 
	
        StringBuffer sb = new StringBuffer();
        sb.append("<xml>");
        Set<Map.Entry<String, String>> es = packageParams.entrySet();
        Iterator<Map.Entry<String, String>> it = es.iterator();
        while(it.hasNext()) {
            Map.Entry<String, String> entry = it.next();
            String k = (String)entry.getKey();
            String v = (String)entry.getValue();
            if ("attach".equalsIgnoreCase(k)||"body".equalsIgnoreCase(k)||"detail".equalsIgnoreCase(k)) {
                sb.append("<"+k+">"+"<![CDATA["+v+"]]></"+k+">");
            }else {
                sb.append("<"+k+">"+v+"</"+k+">");
            }
        }
        sb.append("</xml>");
        logger.info("向威富通发送请求------"+sb);
        return sb.toString();
    }
	/**
	 * 将map转换成url的queryString排好序的字符串(回调用)
	 * @param packageParams
	 * @return
	 */
	public static String getRequestURLToMer(Map<String, String> packageParams){ 
		
        StringBuffer sb = new StringBuffer();
        Set<Map.Entry<String, String>> es = packageParams.entrySet();
        Iterator<Map.Entry<String, String>> it = es.iterator();
        while(it.hasNext()) {
            Map.Entry<String, String> entry = it.next();
            String k = (String)entry.getKey();
            String v = (String)entry.getValue();
                sb.append(k+"="+v+"&");
        }
        sb.deleteCharAt(sb.length()-1);
        logger.info("发送给下游商户------"+sb);
        return sb.toString();
    }
	/**
	 * 对queryString进行签名
	 * @param packageParams
	 * @return
	 */
	public static String getSign(Map<String,String> map,String key){
        ArrayList<String> list = new ArrayList<String>();
        for(Map.Entry<String,String> entry:map.entrySet()){
            if(entry.getValue()!=null && entry.getValue()!=""){
                list.add(entry.getKey().toLowerCase() + "=" + entry.getValue() + "&");
            }
        }
        int size = list.size();
        String [] arrayToSort = list.toArray(new String[size]);
        Arrays.sort(arrayToSort, String.CASE_INSENSITIVE_ORDER);
        StringBuilder sb = new StringBuilder();
        for(int i = 0; i < size; i ++) {
            sb.append(arrayToSort[i]);
        }
        sb.append("key=" + key);
        String result = sb.toString();
        System.out.println("签名串----"+result);
        result = MD5.MD5Encode(result).toUpperCase();
        return result;
    }
	/**
	 * 瀚银签名
	 
	public static String HygetSign(Map<String,String> map,String key){
		StringBuilder sb = new StringBuilder();
		for(Map.Entry<String,String> entry:map.entrySet()){
			sb.append(entry.getValue()).append("|");
        }
        sb.append(key);
        String result = sb.toString();
        logger.warn("---瀚银签名字符串--"+result);
        HYMD5 hymd5 = new HYMD5();
        result = hymd5.getMD5ofStr(result);
        return result;
    }*/
	/**
	 * H5
	 */
	public static String H5getSign(Map<String,String> map,String key){
        ArrayList<String> list = new ArrayList<String>();
        for(Map.Entry<String,String> entry:map.entrySet()){
            if(entry.getValue()!=null && entry.getValue()!=""){
                list.add(entry.getKey().toLowerCase() + "=" + entry.getValue() + "&");
            }
        }
        int size = list.size();
        String [] arrayToSort = list.toArray(new String[size]);
        Arrays.sort(arrayToSort, String.CASE_INSENSITIVE_ORDER);
        StringBuilder sb = new StringBuilder();
        for(int i = 0; i < size; i ++) {
            sb.append(arrayToSort[i]);
        }
        sb.append("secret=" + key);
        String result = sb.toString();
        result = MD5.MD5Encode(result);
        return result;
    }
	
	/**
     * 
     * 方法用途: 对所有传入参数按照字段名的 ASCII 码从小到大排序（字典序）<br>
     * @param paraMap   要排序的Map对象
     * @returns
     */
    public static List<Map.Entry<String, String>> formatParaMap(Map<String, String> paraMap){
        Map<String, String> tmpMap = paraMap;
            List<Map.Entry<String, String>> infoIds = new ArrayList<Map.Entry<String, String>>(tmpMap.entrySet());
            // 对所有传入参数按照字段名的 ASCII 码从小到大排序（字典序）
            Collections.sort(infoIds, new Comparator<Map.Entry<String, String>>()
            {

                @Override
                public int compare(Map.Entry<String, String> o1, Map.Entry<String, String> o2)
                {
                    return (o1.getKey()).toString().compareTo(o2.getKey());
                }
            });
        return infoIds;
    }
}
