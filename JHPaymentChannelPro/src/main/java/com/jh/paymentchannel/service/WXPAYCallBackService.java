package com.jh.paymentchannel.service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.net.URI;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestTemplate;

import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.internal.util.AlipaySignature;
import com.jh.paymentchannel.basechannel.BaseChannel;
import com.jh.paymentchannel.business.TopupPayChannelBusiness;
import com.jh.paymentchannel.pojo.PaymentOrder;
import com.jh.paymentchannel.util.AlipayAPIClientFactory;
import com.jh.paymentchannel.util.AlipayServiceEnvConstants;
import com.jh.paymentchannel.util.Util;
import com.jh.paymentchannel.util.wxwap.WXPayUtil;
import com.jh.paymentchannel.util.wxwap.WXPayConstants;
import cn.jh.common.tools.ResultWrap;
import cn.jh.common.utils.CommonConstants;
import net.sf.json.JSONObject;

@Controller
@EnableAutoConfiguration
public class WXPAYCallBackService extends BaseChannel {

	private static final Logger LOG = LoggerFactory.getLogger(WXPAYCallBackService.class);

	@Autowired
	private TopupPayChannelBusiness topupPayChannelBusiness;

	@Autowired
	private Util util;

	@Autowired
	private RestTemplate restTemplate;

	@Value("${payment.ipAddress}")
	private String ipAddress;
	
	@Value("${wx.mid}")
	private String Mid;
	
	@Value("${wx.AppSecret}")
	private String AppSecret;
	
	@Value("${wx.AppID}")
	private String AppID;


	private static final Charset UTF_8 = StandardCharsets.UTF_8;
	
	
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentchannel/topup/wxpay/notify_call")
	public @ResponseBody Object wexinPayWapNotify(HttpServletRequest request) throws Exception {
		InputStream inStream = request.getInputStream();
		ByteArrayOutputStream outSteam = new ByteArrayOutputStream();
		byte[] buffer = new byte[1024];
		int len = 0;
		while ((len = inStream.read(buffer)) != -1) {
			outSteam.write(buffer, 0, len);
		}
		outSteam.close();
		inStream.close();
		/** 获取微信调用notify_url的返回XML信息 */
		String result = new String(outSteam.toByteArray(), "utf-8");
		Map<String, String> resultMap = WXPayUtil.xmlToMap(result);
 		LOG.info("微信WAP回调进来了============resultMap:"+resultMap);
		if (!"SUCCESS".equals(resultMap.get("return_code"))) {
 			return ResultWrap.err(LOG, "非成功回调",resultMap.toString());
		}
		
		String orderCode = resultMap.get("out_trade_no");
		String realChannelOrderCode = resultMap.get("transaction_id");
		String resultCode = resultMap.get("result_code");
		
		boolean flag = false;
		flag = WXPayUtil.isSignatureValid(resultMap, AppSecret,WXPayConstants.SignType.HMACSHA256);
		if (!flag) {
 			return ResultWrap.err(LOG, CommonConstants.ERROR_SIGN_NOVALID,resultMap.toString());
		}
		

		if (!"SUCCESS".equals(resultCode)) {
 			return ResultWrap.err(LOG, "非成功回调",resultMap.toString());
		}
		RestTemplate restTemplate=new RestTemplate();
		
		URI uri = util.getServiceUrl("transactionclear", "error url request!");
		String url = uri.toString() + "/v1.0/transactionclear/payment/update";
		
		/**根据的用户手机号码查询用户的基本信息*/
		MultiValueMap<String, String> requestEntity  = new LinkedMultiValueMap<String, String>();
		requestEntity.add("status", "1");
		requestEntity.add("third_code", realChannelOrderCode);
		requestEntity.add("order_code", orderCode);
		result = restTemplate.postForObject(url, requestEntity, String.class);		
		
		LOG.info("订单状态修改成功==================="+orderCode);

		LOG.info("订单已支付!");
		
		Map<String, String> returnMap = new HashMap<>();
		returnMap.put("return_code", "SUCCESS");
		return WXPayUtil.mapToXml(returnMap);
	}
		
}