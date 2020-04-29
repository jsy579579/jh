package com.jh.paymentchannel.service;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.URI;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.servlet.http.HttpServletRequest;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import com.jh.paymentchannel.business.BrandManageBusiness;
import com.jh.paymentchannel.pojo.PaymentOrder;
import com.jh.paymentchannel.pojo.SwiftBrandMerchant;
import com.jh.paymentchannel.util.SignUtils;
import com.jh.paymentchannel.util.SwiftMD5;
import com.jh.paymentchannel.util.SwiftSignUtils;
import com.jh.paymentchannel.util.Util;
import com.jh.paymentchannel.util.XmlUtils;

import cn.jh.common.utils.ExceptionUtil;
import net.sf.json.JSONObject;


/**威富通支付请求**/
@Service
public class SwiftTopupRequest  implements TopupRequest{

	private static final Logger LOG = LoggerFactory.getLogger(SwiftTopupRequest.class);
	
	@Value("${swiftpass.req_url}")
	private String reqURL;
	
	@Value("${swiftpass.download_url}")
	private String downloadURL;
		
	/*@Value("${swiftpass.mercode}")
	private String merCode;
	
	@Value("${swiftpass.merkey}")
	private String merKey;*/
	
	@Autowired
	Util util;
	
	private static final Logger log = LoggerFactory.getLogger(SwiftTopupRequest.class);
	
	@Autowired
	private BrandManageBusiness brandManageBusiness;  
	
	@Override
	public Map<String, String> topupRequest(Map<String,Object> params)throws UnsupportedEncodingException {
		PaymentOrder paymentOrder = (PaymentOrder) params.get("paymentOrder");
		HttpServletRequest request = (HttpServletRequest) params.get("request");
		String extra = (String) params.get("extra");

		String ordercode = paymentOrder.getOrdercode();
		String amount = paymentOrder.getAmount().toString();
		String orderdesc = paymentOrder.getDesc();
		String notifyurl = (String) params.get("notifyURL");
		String returnurl = (String) params.get("returnURL");
		String channelParam = (String) params.get("channelParams");
		
		 Map<String, String>  map = new HashMap<String, String>();
		 CloseableHttpClient httpClient = null;
		try {
			httpClient = HttpClients.createDefault();
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		HttpPost postMethod = new HttpPost(reqURL);
        List<BasicNameValuePair> nvps = new ArrayList<BasicNameValuePair>();
        nvps.add(new BasicNameValuePair("service", channelParam));
        //nvps.add(new BasicNameValuePair("version", "V1.0"));
        URI uri = util.getServiceUrl("transactionclear", "error url request!");
		String url = uri.toString() + "/v1.0/transactionclear/payment/query/ordercode";
		
		RestTemplate restTemplate=new RestTemplate();
		MultiValueMap<String, String> requestEntity  = new LinkedMultiValueMap<String, String>();
		requestEntity.add("order_code",  ordercode);
		String result = restTemplate.postForObject(url, requestEntity, String.class);
		
		JSONObject jsonObject =  JSONObject.fromObject(result);
		JSONObject resultObj  =  jsonObject.getJSONObject("result");
		String brandid  =  resultObj.getString("brandid");
        
		SwiftBrandMerchant brandmerchant = brandManageBusiness.getSwiftBrandMerchant(brandid);
        nvps.add(new BasicNameValuePair("mch_id", brandmerchant.getSubMerchantid()));
        //nvps.add(new BasicNameValuePair("transId", "10"));
        //nvps.add(new BasicNameValuePair("merNo", merCode));
        nvps.add(new BasicNameValuePair("out_trade_no", ordercode));
        nvps.add(new BasicNameValuePair("body", orderdesc));
        nvps.add(new BasicNameValuePair("attach",  extra));
        nvps.add(new BasicNameValuePair("notify_url", notifyurl));
        nvps.add(new BasicNameValuePair("total_fee", new BigDecimal(amount).multiply(new BigDecimal("100")).toString()));
        nvps.add(new BasicNameValuePair("mch_create_ip", request.getRemoteAddr()));
        String timeStart = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
        nvps.add(new BasicNameValuePair("time_start", timeStart));
        String nonce_str = String.valueOf(new Date().getTime());
        nvps.add(new BasicNameValuePair("nonce_str", nonce_str));
        String openid = "";
        
        
        
        if(channelParam.equalsIgnoreCase("pay.alipay.jspay")){
	        
	        openid  = resultObj.getString("openid");
	        nvps.add(new BasicNameValuePair("buyer_id", openid));
	       
        }
        
        
        
        String res = null;
        try {
        String sign = 	SignUtils.signMD5Data(nvps, brandmerchant.getSubMerchantKey());
		//nvps.add(new BasicNameValuePair("sign", ));
		
		SortedMap<String,String> paramsmap  =  new TreeMap<String,String>() ;
		
		if(channelParam.equalsIgnoreCase("pay.alipay.jspay")){
		 paramsmap.put("buyer_id", openid);
		}
		paramsmap.put("service", channelParam);
		paramsmap.put("mch_id", brandmerchant.getSubMerchantid());
		paramsmap.put("out_trade_no", ordercode);
		paramsmap.put("body", orderdesc);
		paramsmap.put("attach",  extra);
		paramsmap.put("notify_url", notifyurl);
		paramsmap.put("total_fee", new BigDecimal(amount).multiply(new BigDecimal("100")).toString());
		paramsmap.put("mch_create_ip", request.getRemoteAddr());
		paramsmap.put("time_start", timeStart);
		paramsmap.put("nonce_str", nonce_str);
		paramsmap.put("sign", sign);
		  log.info("威富通请求数据：" + paramsmap);
		StringEntity entityParams = new StringEntity(XmlUtils.parseXML(paramsmap),"utf-8");
		
		
        postMethod.setEntity(entityParams);
        CloseableHttpResponse response = httpClient.execute(postMethod);
            
            if(response != null && response.getEntity() != null){
                Map<String,String> resultMap = XmlUtils.toMap(EntityUtils.toByteArray(response.getEntity()), "UTF-8");
                res = XmlUtils.toXml(resultMap);
                
                log.info("威富通请求结果：" + res);
                
                if(resultMap.containsKey("sign")){
                    if(!SwiftSignUtils.checkParam(resultMap, brandmerchant.getSubMerchantKey())){
                        res = "验证签名不通过";
                        map.put("resp_code", "failed");
                    }else{
                        if("0".equals(resultMap.get("status")) && "0".equals(resultMap.get("result_code"))){
                            /*if(orderResult == null){
                                orderResult = new HashMap<String,String>();
                            }
                            orderResult.put(map.get("out_trade_no"), "0");//初始状态
                            
                            String code_img_url = resultMap.get("code_img_url");
                            //System.out.println("code_img_url"+code_img_url);
                            req.setAttribute("code_img_url", code_img_url);
                            req.setAttribute("out_trade_no", map.get("out_trade_no"));
                            req.setAttribute("total_fee", map.get("total_fee"));
                            req.setAttribute("body", map.get("body"));
                            req.getRequestDispatcher("index-pay-result.jsp").forward(req, resp);*/
                        	
                        	 map.put("resp_code", "success");	
                             map.put("channel_type", "quick");
                             
                             if(channelParam.equalsIgnoreCase("pay.alipay.jspay") || channelParam.equalsIgnoreCase("pay.tenpay.wappay")){
                            	 
                            	 map.put("redirect_url", resultMap.get("pay_info"));	
                            	 
                             }else if(channelParam.equalsIgnoreCase("pay.tenpay.jspay")){
                            	 map.put("redirect_url", resultMap.get("pay_url"));	
                             }else{
                            	 
                            	 map.put("redirect_url", resultMap.get("code_url"));	
                             	
                            	 
                             }
                             
                             
                            
                        	
                        }else{
                        	 map.put("resp_code", "failed");
                        }
                    }
                } else {
                	 map.put("resp_code", "failed");
                }
            }else{
                res = "操作失败";
                map.put("resp_code", "failed");
            }
            
            
            
            /*String str = EntityUtils.toString(resp.getEntity(), "UTF-8");
            int statusCode = resp.getStatusLine().getStatusCode();
            if (200 == statusCode) {
                boolean signFlag = SignUtils.verferSignData(str, pubKeyPath);
               if (!signFlag) {
                   
            	   map.put("resp_code", "failed");
            	   
                }else{
                	
                	String data[] = str.split("&");
                    StringBuffer buf = new StringBuffer();
                    
                    *//*
                    String codeURL = "";
                    *//*
                    String imgURL  = "";
                    
                    for (int i = 0; i < data.length; i++) {
                        String tmp[] = data[i].split("=", 2);
                        if ("codeUrl".equals(tmp[0])) {
                        	codeURL = tmp[1];
                        }
                        
                        if("imgUrl".equals(tmp[0])){
                        	imgURL = tmp[1];
                        }
                    }
                   	
                   map.put("resp_code", "success");	
                   map.put("channel_type", "weixin");
                   map.put("code_url", codeURL);	
                   map.put("img_url", imgURL);	
                }
                
                
            }else{
            	
            	map.put("resp_code", "failed");
            }*/
           // System.out.println("返回错误码:" + statusCode);
        } catch (Exception e) {
            
        
        	e.printStackTrace();LOG.error("",e);
        	return null;
        }
        
        
        return map;
	}

	
	/*public static void main(String[] args) throws Exception {
		downloadSwiftpass(null, "", "", "", "", "", "", "");
		
	}*/
	
	public static Map<String, String> downloadSwiftpass(HttpServletRequest request,
			String amount, String ordercode, String orderdesc, String extra,
			String notifyurl, String returnurl, String channelParam)
			throws UnsupportedEncodingException {
		SortedMap<String, String>  map = new TreeMap<String,String>();
		 map.put("service", "pay.bill.merchant");
		 map.put("bill_date", "20171001");
		 map.put("bill_type", "ALL");
		 map.put("mch_id", "129530011256");
		 map.put("nonce_str", "1409196838");
		 Map<String,String> params = paraFilter(map);
		 StringBuilder buf = new StringBuilder((params.size() +1) * 10);
		 buildPayParams(buf,params,false);
		 String preStr = buf.toString();
	     String sign = SwiftMD5.sign(preStr, "&key=" + "31fc79f8fd564aa55ae40e2cb698f24c", "utf-8");
	     map.put("sign", sign);
	     CloseableHttpResponse response = null;
	     CloseableHttpClient client = null;
	     try {
	            HttpPost httpPost = new HttpPost("https://download.swiftpass.cn/gateway");
	            StringEntity entityParams = new StringEntity(XmlUtils.parseXML(map),"utf-8");
	            httpPost.setEntity(entityParams);
	            httpPost.setHeader("Content-Type", "text/xml;charset=ISO-8859-1");
	            client = HttpClients.createDefault();
	            response = client.execute(httpPost);
	            if(response != null && response.getEntity() != null){
	                Map<String,String> resultMap = XmlUtils.toMap(EntityUtils.toByteArray(response.getEntity()), "utf-8");
	            }else{
	            }
	        } catch (Exception e) {
	            e.printStackTrace();LOG.error("",e);
	        } finally {
	            if(response != null){
	            }
	            if(client != null){
	            }
	        }
	     
	     
	     
	     
        return map;
	}
	 public static Map<String, String> paraFilter(Map<String, String> sArray) {
	        Map<String, String> result = new HashMap<String, String>(sArray.size());
	        if (sArray == null || sArray.size() <= 0) {
	            return result;
	        }
	        for (String key : sArray.keySet()) {
	            String value = sArray.get(key);
	            if (value == null || value.equals("") || key.equalsIgnoreCase("sign")) {
	                continue;
	            }
	            result.put(key, value);
	        }
	        return result;
	    }
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
	            return URLEncoder.encode(str, "UTF-8");
	        } catch (Throwable e) {
	            return str;
	        } 
	    }
}
