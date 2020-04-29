package com.jh.paymentchannel.service;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
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
import com.netflix.discovery.converters.Auto;

import cn.jh.common.tools.ResultWrap;
import cn.jh.common.utils.CommonConstants;
import cn.jh.common.utils.ExceptionUtil;
import cn.jh.common.utils.IpAddressUtil;
import net.sf.json.JSONObject;

@Service
public class ALIPAYTopupPage extends BaseChannel implements TopupRequest {

	private static final Logger LOG = LoggerFactory.getLogger(ALIPAYTopupPage.class);

	@Autowired
	private TopupPayChannelBusiness topupPayChannelBusiness;

	@Autowired
	private Util util;

	@Value("${payment.ipAddress}")
	private String ipAddress;

	/*@Autowired
	private PropertiesConfig propertiesConfig;*/
	
	private static final Charset UTF_8 = StandardCharsets.UTF_8;
	
	@Override
	public Map<String, String> topupRequest(Map<String, Object> params) throws Exception {
		PaymentOrder paymentOrder = (PaymentOrder) params.get("paymentOrder");
		HttpServletRequest request = (HttpServletRequest) params.get("request");
		
		String orderCode = paymentOrder.getOrdercode();
		String amount = paymentOrder.getAmount().toString();
		String outNotifyUrl = paymentOrder.getOutNotifyUrl();
		
		
		Map<String, String> maps = new HashMap<String, String>();
		
		//实例化客户端
		AlipayClient alipayClient = AlipayAPIClientFactory.getAlipayClient();
		//实例化具体API对应的request类,类名称和接口名称对应,当前调用接口名称：alipay.trade.app.pay
		AlipayTradeWapPayRequest alipayRequest = new AlipayTradeWapPayRequest();//创建API对应的request
		//SDK已经封装掉了公共参数，这里只需要传入业务参数。以下方法为sdk的model入参方式(model和biz_content同时存在的情况下取biz_content)。
		AlipayTradeWapPayModel wapModel = new AlipayTradeWapPayModel();
		wapModel.setBody("");
		try {
			wapModel.setSubject(URLEncoder.encode(paymentOrder.getDesc(), AlipayServiceEnvConstants.CHARSET));
		} catch (UnsupportedEncodingException e1) {
			e1.printStackTrace();
			wapModel.setSubject("");
		}
		wapModel.setOutTradeNo(orderCode);
		wapModel.setTotalAmount(amount);
		wapModel.setProductCode("QUICK_WAP_WAY");
		wapModel.setEnablePayChannels("balance,moneyFund,debitCardExpress,bankPay");
		alipayRequest.setBizModel(wapModel);
		/*String returnURL = propertiesConfig.getIpAddress() + "/v1.0/paymentchannel/topup/sdjpaysuccess";
		
		String notifyURL = propertiesConfig.getIpAddress() + "/v1.0/paymentchannel/topup/alipay/notify_call";*/
		
		//String returnURL = "http://139.196.125.48/v1.0/paymentchannel/topup/sdjpaysuccess";
		String returnURL = ipAddress + "/v1.0/paymentchannel/topup/sdjpaysuccess";

		String notifyURL;
		if(outNotifyUrl != null && !"".equals(outNotifyUrl.trim()) && !"null".equals(outNotifyUrl.trim())) {
			
			notifyURL = outNotifyUrl;
		}else {
			
			//notifyURL =  "http://139.196.125.48/v1.0/paymentchannel/topup/alipay/notify_call";
			notifyURL = ipAddress + "/v1.0/paymentchannel/topup/alipay/notify_call";
		}
		
		alipayRequest.setReturnUrl(returnURL);
		alipayRequest.setNotifyUrl(notifyURL);//在公共参数中设置回跳和通知地址
		String from = "";
		try {
		    //这里和普通的接口调用不同，使用的是pageExecute
			from =  alipayClient.pageExecute(alipayRequest).getBody();
		    System.out.println(from);//就是orderString 可以直接给客户端请求，无需再做处理。
		    } catch (Exception e) {
		        e.printStackTrace();
		        
		    	maps.put(CommonConstants.RESP_CODE, "falied");
				maps.put("channel_type", "jf");
				maps.put(CommonConstants.RESP_MESSAGE, "交易排队中,请稍后重试!");
		        
		        return maps;
		}
		
		maps.put(CommonConstants.RESP_CODE, "success");
		maps.put("channel_type", "jf");
		maps.put("redirect_url", from);
		
		return maps;
	}
	
}
