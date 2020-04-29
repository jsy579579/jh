package com.jh.paymentchannel.service;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
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
import com.jh.paymentchannel.util.SwiftSignUtils;
import com.jh.paymentchannel.util.Util;
import com.jh.paymentchannel.util.XmlUtils;

import cn.jh.common.utils.ExceptionUtil;
import net.sf.json.JSONObject;

@Service
public class SwiftGongZhongHaoRequest implements TopupRequest{
	
	private final Logger LOG = LoggerFactory.getLogger(getClass());

	@Value("${swiftpass.req_url}")
	private String reqURL;
		
/*	@Value("${swiftpass.mercode}")
	private String merCode;
	
	@Value("${swiftpass.merkey}")
	private String merKey;*/
	
	@Autowired
	Util util;
	
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
		/**判断是否有外放的通道的处理， 如果有那么继续回调外放哦*/
		URI uri = util.getServiceUrl("transactionclear", "error url request!");
		String url = uri.toString() + "/v1.0/transactionclear/payment/query/ordercode";
		
		RestTemplate restTemplate=new RestTemplate();
		MultiValueMap<String, String> requestEntity  = new LinkedMultiValueMap<String, String>();
		requestEntity.add("order_code",  ordercode);
		String result = restTemplate.postForObject(url, requestEntity, String.class);
		
		JSONObject jsonObject =  JSONObject.fromObject(result);
		JSONObject resultObj  =  jsonObject.getJSONObject("result");
		String openid  =  resultObj.getString("openid");
		String brandid  =  resultObj.getString("brandid");
		SwiftBrandMerchant brandmerchant = brandManageBusiness.getSwiftBrandMerchant(brandid);
		
		
        List<BasicNameValuePair> nvps = new ArrayList<BasicNameValuePair>();
        nvps.add(new BasicNameValuePair("service", channelParam));
        //nvps.add(new BasicNameValuePair("version", "V1.0"));
        nvps.add(new BasicNameValuePair("mch_id", brandmerchant.getSubMerchantid()));
        nvps.add(new BasicNameValuePair("is_raw", "1"));
        //nvps.add(new BasicNameValuePair("transId", "10"));
        //nvps.add(new BasicNameValuePair("merNo", merCode));
        nvps.add(new BasicNameValuePair("out_trade_no", ordercode));
        nvps.add(new BasicNameValuePair("body", orderdesc));
        
        
        nvps.add(new BasicNameValuePair("sub_openid", openid));
       
        nvps.add(new BasicNameValuePair("sub_appid", "wx964293b158c6e6af"));
        //nvps.add(new BasicNameValuePair("sub_appid", "wx2ebb475f2cea25e2"));
        nvps.add(new BasicNameValuePair("attach",  extra));
        nvps.add(new BasicNameValuePair("notify_url", notifyurl));
        nvps.add(new BasicNameValuePair("callback_url", returnurl));
        nvps.add(new BasicNameValuePair("total_fee", new BigDecimal(amount).multiply(new BigDecimal("100")).toString()));
        nvps.add(new BasicNameValuePair("mch_create_ip", request.getRemoteAddr()));
        String timeStart = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
        nvps.add(new BasicNameValuePair("time_start", timeStart));
        String nonce_str = String.valueOf(new Date().getTime());
        nvps.add(new BasicNameValuePair("nonce_str", nonce_str));
        String res = null;
        
        
        
        try {
        String sign = 	SignUtils.signMD5Data(nvps, brandmerchant.getSubMerchantKey());
		//nvps.add(new BasicNameValuePair("sign", ));
		
		SortedMap<String,String> paramsmap  =  new TreeMap<String,String>() ;
		paramsmap.put("service", channelParam);
		paramsmap.put("mch_id", brandmerchant.getSubMerchantid());
		paramsmap.put("out_trade_no", ordercode);
		paramsmap.put("body", orderdesc);
		paramsmap.put("sub_openid", openid);
		paramsmap.put("sub_appid", "wx964293b158c6e6af");
		//paramsmap.put("sub_appid", "wx2ebb475f2cea25e2");
		paramsmap.put("attach",  extra);
		paramsmap.put("is_raw",  "1");
		paramsmap.put("notify_url", notifyurl);
		paramsmap.put("callback_url", returnurl);
		paramsmap.put("total_fee", new BigDecimal(amount).multiply(new BigDecimal("100")).toString());
		paramsmap.put("mch_create_ip", request.getRemoteAddr());
		paramsmap.put("time_start", timeStart);
		paramsmap.put("nonce_str", nonce_str);
		paramsmap.put("sign", sign);
		
		StringEntity entityParams = new StringEntity(XmlUtils.parseXML(paramsmap),"utf-8");
		
		
        postMethod.setEntity(entityParams);
        CloseableHttpResponse response = httpClient.execute(postMethod);
            
            if(response != null && response.getEntity() != null){
                Map<String,String> resultMap = XmlUtils.toMap(EntityUtils.toByteArray(response.getEntity()), "UTF-8");
                res = XmlUtils.toXml(resultMap);
                System.out.println("请求结果：" + res);
                
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
                             String payinfo  = resultMap.get("pay_info");
                             
                             map.put("redirect_url", payinfo);	
                        	
                        	
                        }else{
                        	 map.put("resp_code", "failed");
                        }
                    }
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

}

