package com.jh.paymentchannel.service;

import java.io.IOException;
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
import org.springframework.web.client.RestClientException;
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

import cn.jh.common.tools.ResultWrap;
import cn.jh.common.utils.CommonConstants;
import net.sf.json.JSONObject;

@Controller
@EnableAutoConfiguration
public class ALIPAYCallBackService extends BaseChannel {

	private static final Logger LOG = LoggerFactory.getLogger(ALIPAYCallBackService.class);

	@Autowired
	private TopupPayChannelBusiness topupPayChannelBusiness;

	@Autowired
	private Util util;

	@Autowired
	private RestTemplate restTemplate;

	@Value("${payment.ipAddress}")
	private String ipAddress;

	private static final Charset UTF_8 = StandardCharsets.UTF_8;

	// 微信公众号异步通知接口
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentchannel/topup/spgzh/notify_callasd")
	public void spGZHNotifyCallback(HttpServletRequest request, HttpServletResponse response) throws Exception {
		LOG.info("微信公众号异步通知进来了=======");

		Map<String, String[]> parameterMap = request.getParameterMap();
		Set<String> keySet = parameterMap.keySet();
		for (String key : keySet) {
			String[] strings = parameterMap.get(key);
			for (String s : strings) {
				LOG.info(key + "=============" + s);
			}
		}

		String orderCode = request.getParameter("order_no");
		String isSuccess = request.getParameter("is_success");
		
		if("true".equalsIgnoreCase(isSuccess)) {
			LOG.info("异步回调订单已成功======");
			
			RestTemplate restTemplate=new RestTemplate();
			
			URI uri = util.getServiceUrl("transactionclear", "error url request!");
			String url = uri.toString() + "/v1.0/transactionclear/payment/update";
			
			/**根据的用户手机号码查询用户的基本信息*/
			MultiValueMap<String, String> requestEntity  = new LinkedMultiValueMap<String, String>();
			requestEntity.add("status", "1");
			requestEntity.add("order_code", orderCode);
			String result = restTemplate.postForObject(url, requestEntity, String.class);
			
			OutputStream outStr = response.getOutputStream();
			
			outStr.write("SUCCESS".getBytes());
		}

	}

	
	@RequestMapping(method=RequestMethod.GET,value="/v1.0/paymentchannel/topup/spgzh/return_callasd")
	public  String spGZHReturnCallback(HttpServletRequest request, HttpServletResponse response, Model model)throws IOException {
		// 设置编码
		request.setCharacterEncoding("utf-8");
		response.setCharacterEncoding("utf-8");
		response.setContentType("text/html;charset=utf-8");
	
		Map<String, String[]> parameterMap = request.getParameterMap();
		Set<String> keySet = parameterMap.keySet();
		for (String key : keySet) {
			String[] strings = parameterMap.get(key);
			for (String s : strings) {
				LOG.info(key + "=============" + s);
			}
		}
		
		return "sdjsuccess";
	}
	
	
	// 扫码异步通知接口
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentchannel/topup/spqrcode/notify_callasd")
	public void spQRCODENotifyCallback(HttpServletRequest request, HttpServletResponse response) throws Exception {
		LOG.info("扫码异步通知进来了=======");

		Map<String, String[]> parameterMap = request.getParameterMap();
		Set<String> keySet = parameterMap.keySet();
		for (String key : keySet) {
			String[] strings = parameterMap.get(key);
			for (String s : strings) {
				LOG.info(key + "=============" + s);
			}
		}

		
		String orderCode = request.getParameter("order_no");
		String isSuccess = request.getParameter("is_success");
		
		if("true".equalsIgnoreCase(isSuccess)) {
			LOG.info("异步回调订单已成功======");
			
			RestTemplate restTemplate=new RestTemplate();
			
			URI uri = util.getServiceUrl("transactionclear", "error url request!");
			String url = uri.toString() + "/v1.0/transactionclear/payment/update";
			
			/**根据的用户手机号码查询用户的基本信息*/
			MultiValueMap<String, String> requestEntity  = new LinkedMultiValueMap<String, String>();
			requestEntity.add("status", "1");
			requestEntity.add("order_code", orderCode);
			String result = restTemplate.postForObject(url, requestEntity, String.class);
			
			OutputStream outStr = response.getOutputStream();
			
			outStr.write("SUCCESS".getBytes());
		}
		
	}

	
	//支付宝异步回调接口
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentchannel/topup/alipay/notify_call")
    public @ResponseBody Object aliPayWapNotify(HttpServletRequest request) {
 		//获取支付宝POST过来反馈信息
 		Map<String,String> params = new HashMap<String,String>();
 		Map requestParams = request.getParameterMap();
 		
 		for (Iterator iter = requestParams.keySet().iterator(); iter.hasNext();) {
 		    String name = (String) iter.next();
 		    String[] values = (String[]) requestParams.get(name);
 		    String valueStr = "";
 		    for (int i = 0; i < values.length; i++) {
 		        valueStr = (i == values.length - 1) ? valueStr + values[i]
 		                    : valueStr + values[i] + ",";
 		  	}

 			params.put(name, valueStr);
 		}
 		LOG.info("支付宝WAP回调进来了============params:"+params);
 		
 		String realChannelOrderCode = params.get("trade_no");
 		String orderCode = params.get("out_trade_no");
 		String amount = params.get("total_amount");
 		String tradeStatus = params.get("trade_status");
 		
 		Map<String, Object> queryOrdercode = this.queryOrdercode(orderCode);
 		Object object = queryOrdercode.get("result");
		JSONObject fromObject = JSONObject.fromObject(object);
		JSONObject resultObj = fromObject.getJSONObject("result");
 		
 		/*JSONObject resultJSONObject = this.getOrderByOrderCode(orderCode);
		
 		if(resultJSONObject == null || !resultJSONObject.containsKey(Constss.RESP_CODE) || Constss.FALIED.equals(resultJSONObject.getString(Constss.RESP_CODE))) {
			return ResultWrap.err(LOG, Constss.ORDER_FALIED, "获取订单失败");
		}
 		
 		PaymentOrder paymentOrder = (PaymentOrder) JSONObject.toBean(resultJSONObject.getJSONObject(Constss.RESULT), PaymentOrder.class);
 		String realType = paymentOrder.getRealtype();*/
 		
 		//切记alipaypublickey是支付宝的公钥，请去open.alipay.com对应应用下查看。
 		//boolean AlipaySignature.rsaCheckV1(Map<String, String> params, String publicKey, String charset, String sign_type)
 		//ALiMerchant aLiMerchant = aLiMerchantBusiness.findALiMerchantByAppidAndrealType(params.get("app_id"), realType);
		//AlipayClient alipayClient = AlipayAPIClientFactory.getAlipayClient();
		AlipayServiceEnvConstants alipayClient = new AlipayServiceEnvConstants();
		boolean flag = false;
 		if(alipayClient != null) {
		 		try {
					flag = AlipaySignature.rsaCheckV1(params,AlipayServiceEnvConstants.ALIPAY_PUBLIC_KEY, AlipayServiceEnvConstants.CHARSET,"RSA2");
				} catch (AlipayApiException e) {
					e.printStackTrace();
		 			return ResultWrap.err(LOG, CommonConstants.FALIED, "验签异常");
				}
 		}else{
 			return ResultWrap.err(LOG, CommonConstants.FALIED, "验签失败,无支付宝密钥配置");
 		}
 		if(!flag) {
 			return ResultWrap.err(LOG, CommonConstants.FALIED, "验签失败");
 		}
		if(!"TRADE_SUCCESS".equalsIgnoreCase(tradeStatus) && !"TRADE_FINISHED".equalsIgnoreCase(tradeStatus)) {
			return ResultWrap.err(LOG, CommonConstants.FALIED, "非成功回调");
		}
		if(new BigDecimal(resultObj.getString("amount")).compareTo(new BigDecimal(amount)) != 0) {
			return ResultWrap.err(LOG, CommonConstants.FALIED, "验证金额失败");
		}
		if("1".equals(resultObj.getString("status"))) {
			return ResultWrap.err(LOG, CommonConstants.FALIED, "订单已处理");
		}
        RestTemplate restTemplate = null;
        URI uri = null;
        String url = null;
        MultiValueMap<String, String> requestEntity = null;
        String result = null;
        try {
            restTemplate = new RestTemplate();
            uri = util.getServiceUrl("transactionclear", "error url request!");
            url = uri.toString() + "/v1.0/transactionclear/payment/update";
            // **根据的用户手机号码查询用户的基本信息*//*
            requestEntity = new LinkedMultiValueMap<String, String>();
            requestEntity.add("status", "1");
            requestEntity.add("third_code", "");
            requestEntity.add("order_code", orderCode);
            result = restTemplate.postForObject(url, requestEntity, String.class);
            LOG.info("更新transactionclear订单失败：==========" + result);
        } catch (RestClientException e) {
            LOG.info("更新transactionclear订单失败：==========" + e);
        }
        try {
            // 修改商城订单状态
            restTemplate=new RestTemplate();
            uri = util.getServiceUrl("good", "error url request!");
            url = uri.toString() +  "/v1.0/good/order/update";
            requestEntity  = new LinkedMultiValueMap<String, String>();
            requestEntity.add("status", "2");
            requestEntity.add("order_code",orderCode);
            result = restTemplate.postForObject(url, requestEntity, String.class);
            LOG.info("更新商城订单成功：==========" + result);
        } catch (RestClientException e) {
            LOG.info("更新商城订单失败：==========" + e);
        }
 		return "SUCCESS";
 	}
	
	
	
	
}