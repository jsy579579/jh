package com.jh.paymentchannel.service;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.URI;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import com.alipay.api.AlipayClient;
import com.alipay.api.domain.AlipayTradeWapPayModel;
import com.alipay.api.request.AlipayTradeWapPayRequest;
import com.jh.paymentchannel.pojo.PaymentOrder;
import com.jh.paymentchannel.util.AlipayAPIClientFactory;
import com.jh.paymentchannel.util.AlipayServiceEnvConstants;
import com.jh.paymentchannel.util.Util;
import com.jh.paymentchannel.util.ump.common.ReqData;
import com.jh.paymentchannel.util.ump.paygate.v40.Mer2Plat_v40;

import cn.jh.common.utils.CommonConstants;
import cn.jh.common.utils.ExceptionUtil;
import net.sf.json.JSONObject;


@Service
public class RechargeToAccountTopupRequest implements TopupRequest{
	private static final Logger LOG = LoggerFactory.getLogger(RechargeToAccountTopupRequest.class);
	
	@Autowired
	Util util;
	
	@Value("${payment.ipAddress}")
	private String ipAddress;
	
	@Override
	public Map<String, String> topupRequest(Map<String,Object> params)throws UnsupportedEncodingException {
		
		PaymentOrder paymentOrder = (PaymentOrder) params.get("paymentOrder");
		HttpServletRequest request = (HttpServletRequest) params.get("request");
		
		String orderCode = paymentOrder.getOrdercode();
		String amount = paymentOrder.getAmount().toString();

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
		
		String returnURL = ipAddress + "/v1.0/paymentchannel/topup/sdjpaysuccess";
		
		String notifyURL = ipAddress + "/v1.0/paymentchannel/topup/rechargetoaccount/notify_call";
		
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
		
		maps.put("resp_code", "success");
		maps.put("channel_type", "jf");
		maps.put("redirect_url", from);
		
		return maps;
		
	}
	
}
