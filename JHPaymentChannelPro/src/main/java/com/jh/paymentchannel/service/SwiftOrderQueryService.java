package com.jh.paymentchannel.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.servlet.http.HttpServletRequest;

import org.apache.http.client.ClientProtocolException;
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
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.jh.paymentchannel.business.BrandManageBusiness;
import com.jh.paymentchannel.pojo.SwiftBrandMerchant;
import com.jh.paymentchannel.util.SignUtils;
import com.jh.paymentchannel.util.XmlUtils;

import cn.jh.common.utils.ExceptionUtil;


@Controller
@EnableAutoConfiguration
public class SwiftOrderQueryService {

	
	private static final Logger LOG = LoggerFactory.getLogger(SwiftOrderQueryService.class);
	
	@Autowired
	private BrandManageBusiness brandManageBusiness;  
	
	/**发起请求 请求url, 并返回跳装的url*/
	@RequestMapping(method=RequestMethod.POST,value="/v1.0/paymentchannel/swift/order/query")
	public @ResponseBody Object getOrder(HttpServletRequest request, 
			@RequestParam(value = "mch_id") String mchId,
			@RequestParam(value = "bill_date") String billdate
			){
		CloseableHttpClient httpClient = null;
		try {
			httpClient = HttpClients.createDefault();
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		HttpPost postMethod = new HttpPost("https://download.swiftpass.cn/gateway");
		SwiftBrandMerchant brandMerchant = brandManageBusiness.getSwiftBrandMerchantByMchId(mchId);
		List<BasicNameValuePair> nvps = new ArrayList<BasicNameValuePair>();
        nvps.add(new BasicNameValuePair("service", "pay.bill.merchant"));
        nvps.add(new BasicNameValuePair("mch_id", mchId));
        nvps.add(new BasicNameValuePair("bill_type", "ALL"));
        nvps.add(new BasicNameValuePair("bill_date", billdate));
        String nonce_str = String.valueOf(new Date().getTime());
        nvps.add(new BasicNameValuePair("nonce_str", nonce_str));
        String sign = "";
        String res = null;
        try {
			  sign = 	SignUtils.signMD5Data(nvps, brandMerchant.getSubMerchantKey());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();LOG.error("",e);
		}
        
        
        
        SortedMap<String,String> paramsmap  =  new TreeMap<String,String>() ;
		
		paramsmap.put("service", "pay.bill.merchant");
		paramsmap.put("mch_id", mchId);
		nvps.add(new BasicNameValuePair("bill_type", "ALL"));
        nvps.add(new BasicNameValuePair("bill_date", billdate));
		paramsmap.put("nonce_str", nonce_str);
		paramsmap.put("sign", sign);
		
		StringEntity entityParams = new StringEntity(XmlUtils.parseXML(paramsmap),"utf-8");
		
		
        postMethod.setEntity(entityParams);
        CloseableHttpResponse response = null;
		try {
			response = httpClient.execute(postMethod);
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();LOG.error("",e);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();LOG.error("",e);
		}
            
        if(response != null && response.getEntity() != null){
                Map<String, String> resultMap = null;
				try {
					resultMap = XmlUtils.toMap(EntityUtils.toByteArray(response.getEntity()), "UTF-8");
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();LOG.error("",e);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();LOG.error("",e);
				}
                res = XmlUtils.toXml(resultMap);
                System.out.println("请求结果：" + res);
                
               /* if(resultMap.containsKey("sign")){
                    if(!SwiftSignUtils.checkParam(resultMap, brandmerchant.getSubMerchantKey())){
                        res = "验证签名不通过";
                        map.put("resp_code", "failed");
                    }else{
                        if("0".equals(resultMap.get("status")) && "0".equals(resultMap.get("result_code"))){
                            if(orderResult == null){
                                orderResult = new HashMap<String,String>();
                            }
                            orderResult.put(map.get("out_trade_no"), "0");//初始状态
                            
                            String code_img_url = resultMap.get("code_img_url");
                            //System.out.println("code_img_url"+code_img_url);
                            req.setAttribute("code_img_url", code_img_url);
                            req.setAttribute("out_trade_no", map.get("out_trade_no"));
                            req.setAttribute("total_fee", map.get("total_fee"));
                            req.setAttribute("body", map.get("body"));
                            req.getRequestDispatcher("index-pay-result.jsp").forward(req, resp);
                        	
                        	 map.put("resp_code", "success");	
                             map.put("channel_type", "quick");
                             
                             if(channelParam.equalsIgnoreCase("pay.alipay.jspay")){
                            	 
                            	 map.put("redirect_url", resultMap.get("pay_info"));	
                            	 
                             }else{
                            	 
                            	 map.put("redirect_url", resultMap.get("code_url"));	
                             	
                            	 
                             }
                             
                             
                            
                        	
                        }else{
                        	 map.put("resp_code", "failed");
                        }
                    }
                } */
            }else{
                res = "操作失败";
                //map.put("resp_code", "failed");
            }
        
 
        
		return null;
	}
	
}
