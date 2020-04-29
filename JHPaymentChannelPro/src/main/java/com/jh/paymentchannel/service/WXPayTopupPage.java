package com.jh.paymentchannel.service;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.httpclient.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.alipay.api.AlipayClient;
import com.alipay.api.domain.AlipayTradeWapPayModel;
import com.alipay.api.request.AlipayTradeWapPayRequest;
import com.jh.paymentchannel.basechannel.BaseChannel;
import com.jh.paymentchannel.business.TopupPayChannelBusiness;
import com.jh.paymentchannel.config.PropertiesConfig;
import com.jh.paymentchannel.pojo.PaymentOrder;
import com.jh.paymentchannel.util.AlipayAPIClientFactory;
import com.jh.paymentchannel.util.AlipayServiceEnvConstants;
import com.jh.paymentchannel.util.Util;
import com.jh.paymentchannel.util.wxwap.WXPay;
import com.jh.paymentchannel.util.wxwap.WXPayConfigImpl;
import com.netflix.discovery.converters.Auto;

import cn.jh.common.tools.ResultWrap;
import cn.jh.common.utils.CommonConstants;
import cn.jh.common.utils.ExceptionUtil;
import cn.jh.common.utils.IpAddressUtil;
import cn.jh.common.utils.UUIDGenerator;
import net.sf.json.JSONObject;

@Service
public class WXPayTopupPage extends BaseChannel implements TopupRequest {

	private static final Logger LOG = LoggerFactory.getLogger(WXPayTopupPage.class);


	@Autowired
	private Util util;

	@Value("${payment.ipAddress}")
	private String ipAddress;
	
	@Value("${wx.mid}")
	private String Mid;
	
	@Value("${wx.AppSecret}")
	private String AppSecret;
	
	@Value("${wx.AppID}")
	private String AppID;

	/*@Autowired
	private PropertiesConfig propertiesConfig;*/
	
	private static final Charset UTF_8 = StandardCharsets.UTF_8;
	
	@Override
	public Map<String, String> topupRequest(Map<String, Object> params) throws Exception {
		PaymentOrder paymentOrder = (PaymentOrder) params.get("paymentOrder");
		HttpServletRequest request = (HttpServletRequest) params.get("request");
		String orderCode = paymentOrder.getOrdercode();
		Map<String, String> maps = new HashMap<String, String>();
		WXPayConfigImpl config = new WXPayConfigImpl(AppID, Mid, AppSecret);
		BigDecimal amount = paymentOrder.getAmount();
		String returnURL = paymentOrder.getOutReturnUrl();
        WXPay wxpay = null;
		String url = null;
		
		try {
			wxpay = new WXPay(config);
		} catch (Exception e1) {
			e1.printStackTrace();
			throw new RuntimeException("初始化微信配置异常!");
		}
		String remoteIP = this.getRemoteIP(request);
		System.out.println("remoteIP====" + remoteIP);
        Map<String, String> data = new HashMap<String, String>();
        data.put("body", paymentOrder.getDesc());
        data.put("out_trade_no", orderCode);
        data.put("fee_type", "CNY");
        data.put("total_fee", amount.multiply(BigDecimal.valueOf(100)).setScale(0, BigDecimal.ROUND_HALF_UP).toString());
        data.put("spbill_create_ip",remoteIP);
        data.put("notify_url", ipAddress+"/v1.0/paymentchannel/topup/wxpay/notify_call");
        data.put("trade_type", "MWEB");  // 此处指定为H5支付
        data.put("product_id", "01");
        data.put("limit_pay", "no_credit");
        data.put("nonce_str", UUIDGenerator.getUUID());
        
        System.out.println(data);
        Map<String, String> resp = null;
        try {
            resp = wxpay.unifiedOrder(data);
            System.out.println(resp);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("请求微信支付异常!");
        }
        String returnCode = resp.get("return_code");
        if ("SUCCESS".equals(returnCode)) {
	        String resultCode = resp.get("result_code");
	        if ("SUCCESS".equals(resultCode)) {
	        	url = resp.get("mweb_url");
	    		String prepay_id = url.substring(url.indexOf("prepay_id="),url.indexOf("&")).replace("prepay_id=", "");
	    		String packages = url.substring(url.indexOf("package="),url.length()).replace("package=", "");
	        	url = "<form name=\"punchout_form\" method=\"GET\" action=\""+url+"\"><input type=\"hidden\" name=\"prepay_id\" value=\""+prepay_id+"\"><input type=\"hidden\" name=\"package\" value=\""+packages+"\"></form><script>document.forms[0].submit();</script>";
	        	System.out.println("url..............."+url);
	        }
		}
        
    	if (url == null || "".equals(url)) {
    		url = "<form name=\"punchout_form\" method=\"GET\" action=\""+ipAddress + "/v1.0/facade/purchase/tofailepage?resp_message=" + URLEncoder.encode("该支付暂不可用,程序员在努力加班处理中!", "UTF-8")+"\"></form><script>document.forms[0].submit();</script>";
    	}
		
		
		maps.put(CommonConstants.RESP_CODE, "success");
		maps.put("channel_type", "jf");
		maps.put("redirect_url", url);
		
		return maps;
	}
	/*
	public Map<String, Object> wexinRequest(Map<String, Object> params){
		PaymentOrder paymentOrder = (PaymentOrder) params.get("paymentOrder");
		HttpServletRequest request =  (HttpServletRequest) params.get("request");
		WXPayConfigImpl config = new WXPayConfigImpl(AppID, Mid, AppSecret);
		BigDecimal amount = paymentOrder.getAmount();
		String returnURL = paymentOrder.getOutReturnUrl();
        WXPay wxpay = null;
		String url = null;
		
		try {
			wxpay = new WXPay(config);
		} catch (Exception e1) {
			e1.printStackTrace();
			throw new RuntimeException("初始化微信配置异常!");
		}
		String remoteIP = this.getRemoteIP(request);
		System.out.println("remoteIP====" + remoteIP);
        Map<String, String> data = new HashMap<String, String>();
        data.put("body", paymentOrder.getDesc());
        data.put("out_trade_no", paymentOrder.getOrdercode());
        data.put("fee_type", "CNY");
        data.put("total_fee", amount.multiply(BigDecimal.valueOf(100)).setScale(0, BigDecimal.ROUND_HALF_UP).toString());
        data.put("spbill_create_ip",remoteIP);
        data.put("notify_url", ipAddress+"");
        data.put("trade_type", "MWEB");  // 此处指定为H5支付
        data.put("product_id", "01");
        data.put("limit_pay", "no_credit");
        data.put("nonce_str", UUIDGenerator.getUUID());
        
        System.out.println(data);
        Map<String, String> resp = null;
        try {
            resp = wxpay.unifiedOrder(data);
            System.out.println(resp);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("请求微信支付异常!");
        }
        String returnCode = resp.get("return_code");
        if ("SUCCESS".equals(returnCode)) {
	        String resultCode = resp.get("result_code");
	        if ("SUCCESS".equals(resultCode)) {
	        	url = resp.get("mweb_url");
	    		String prepay_id = url.substring(url.indexOf("prepay_id="),url.indexOf("&")).replace("prepay_id=", "");
	    		String packages = url.substring(url.indexOf("package="),url.length()).replace("package=", "");
	        	url = "<form name=\"punchout_form\" method=\"GET\" action=\""+url+"\"><input type=\"hidden\" name=\"prepay_id\" value=\""+prepay_id+"\"><input type=\"hidden\" name=\"package\" value=\""+packages+"\"></form><script>document.forms[0].submit();</script>";
	        	System.out.println("url..............."+url);
	        }
		}
        
    	if (url == null || "".equals(url)) {
    		url = "<form name=\"punchout_form\" method=\"GET\" action=\""+returnURL+"\"></form><script>document.forms[0].submit();</script>";
    	}
    	
		return ResultWrap.init(CommonConstants.SUCCESS, "请求成功", url);
	}
	*/
	
	
	public String getRemoteIP(HttpServletRequest request) {  
		String ipAddress = request.getHeader("x-forwarded-for");  
        if (ipAddress == null || ipAddress.length() == 0 || "unknown".equalsIgnoreCase(ipAddress)) {  
            ipAddress = request.getHeader("Proxy-Client-ipAddress");  
        }  
        if (ipAddress == null || ipAddress.length() == 0 || "unknown".equalsIgnoreCase(ipAddress)) {  
            ipAddress = request.getHeader("WL-Proxy-Client-ipAddress");  
        }  
        if (ipAddress == null || ipAddress.length() == 0 || "unknown".equalsIgnoreCase(ipAddress)) {  
            ipAddress = request.getHeader("HTTP_CLIENT_IP");  
        }  
        if (ipAddress == null || ipAddress.length() == 0 || "unknown".equalsIgnoreCase(ipAddress)) {  
            ipAddress = request.getHeader("HTTP_X_FORWARDED_FOR");  
        }  
        if (ipAddress == null || ipAddress.length() == 0 || "unknown".equalsIgnoreCase(ipAddress)) {  
            ipAddress = request.getRemoteAddr();  
        }
        String[] strs = ipAddress.split(",");
        if (strs.length > 0) {
        	ipAddress = strs[0];
		}
        return ipAddress.trim();  
    }  
}
