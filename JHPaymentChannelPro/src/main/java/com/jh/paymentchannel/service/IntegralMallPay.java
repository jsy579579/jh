package com.jh.paymentchannel.service;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URI;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestTemplate;

import com.alipay.api.AlipayApiException;
import com.alipay.api.internal.util.AlipaySignature;
import com.jh.paymentchannel.basechannel.BaseChannel;
import com.jh.paymentchannel.util.AlipayServiceEnvConstants;
import com.jh.paymentchannel.util.Util;

import cn.jh.common.tools.ResultWrap;
import cn.jh.common.utils.CommonConstants;
import net.sf.json.JSONObject;

@Controller
@EnableAutoConfiguration
public class IntegralMallPay extends BaseChannel {

	private static final Logger LOG = LoggerFactory.getLogger(IntegralMallPay.class);

	@Autowired
	private Util util;

	@Value("${payment.ipAddress}")
	private String ipAddress;

	/**
	 * 支付宝下单接口
	 * 
	 * @param request
	 * @param response
	 * @param brandId
	 * @param phone
	 * @param channeltag
	 * @param orderdesc
	 * @param amount 
	 * @param orderCode
	 * @param model
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/v1.0/paymentchannel/cardevaluationnew/add/IntegralMallPay")
	public @ResponseBody Object addAliPayOrderCodeByCardEvaluation(HttpServletRequest request,
			HttpServletResponse response,
			@RequestParam(value = "brandId", required = false, defaultValue = "-1") String brandId,
			@RequestParam(value = "phone") String phone,
			@RequestParam(value = "channe_tag", required = false, defaultValue = "SPALI_PAY") String channeltag,
			@RequestParam(value = "order_desc", required = false, defaultValue = "积分消费支付宝付款订单") String orderdesc,
			@RequestParam(value = "amount") String amount, @RequestParam(value = "orderCode") String orderCode,
			Model model) throws Exception {
		Map<String, Object> map = new HashMap<String, Object>();

		RestTemplate restTemplate = new RestTemplate();
		String result;
		JSONObject jsonObject;

		URI uri = util.getServiceUrl("transactionclear", "error url request!");
		String url = uri.toString() + "/v1.0/transactionclear/payment/add";

		MultiValueMap<String, String> requestEntity = new LinkedMultiValueMap<String, String>();
		requestEntity.add("type", "0");
		requestEntity.add("phone", phone);
		requestEntity.add("amount", amount);
		requestEntity.add("channel_tag", channeltag);
		requestEntity.add("desc", orderdesc);
		requestEntity.add("orderCode", orderCode);
		requestEntity.add("notify_url",
				ipAddress + "/v1.0/paymentchannel/cardevaluationnew/alipay/integral/notify_call");
		String order;
		try {
			result = restTemplate.postForObject(url, requestEntity, String.class);
			LOG.info("RESULT================purchase" + result);
			jsonObject = JSONObject.fromObject(result);
			JSONObject resultObj = jsonObject.getJSONObject("result");
			order = resultObj.getString("ordercode");
		} catch (Exception e) {
			LOG.error("==========/v1.0/transactionclear/payment/add添加订单异常===========" + e);
			map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			map.put(CommonConstants.RESP_MESSAGE, "请求下单异常,请稍后重试!");
			map.put(CommonConstants.RESULT, ipAddress + "/v1.0/facade/purchase/tofailepage?resp_message="
					+ URLEncoder.encode("请求下单异常,请稍后重试!", "UTF-8"));
			response.sendRedirect(ipAddress + "/v1.0/facade/purchase/tofailepage?resp_message="
					+ URLEncoder.encode("请求下单异常,请稍后重试!", "UTF-8"));
			return map;
		}

		uri = util.getServiceUrl("paymentchannel", "error url request!");
		url = uri.toString() + "/v1.0/paymentchannel/topup/request";

		requestEntity = new LinkedMultiValueMap<String, String>();
		requestEntity.add("amount", amount);
		requestEntity.add("ordercode", order);
		requestEntity.add("brandcode", brandId);
		requestEntity.add("orderdesc", orderdesc);
		requestEntity.add("channel_tag", channeltag);
		try {
			result = restTemplate.postForObject(url, requestEntity, String.class);
			LOG.info("RESULT================purchase" + result);
			jsonObject = JSONObject.fromObject(result);
		} catch (Exception e) {
			LOG.error("==========/v1.0/paymentchannel/topup/request请求支付异常===========" + e);
			map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			map.put(CommonConstants.RESP_MESSAGE, "请求交易异常,请稍后重试!");
			map.put(CommonConstants.RESULT, ipAddress + "/v1.0/facade/purchase/tofailepage?resp_message="
					+ URLEncoder.encode("请求交易异常,请稍后重试!", "UTF-8"));
			response.sendRedirect(ipAddress + "/v1.0/facade/purchase/tofailepage?resp_message="
					+ URLEncoder.encode("请求交易异常,请稍后重试!", "UTF-8"));
			return map;
		}
		String respCode = jsonObject.getString(CommonConstants.RESP_CODE);
		if (!CommonConstants.SUCCESS.equals(respCode)) {
			if ("999990".equals(respCode)) {
				map.put(CommonConstants.RESP_CODE, "999990");
				map.put(CommonConstants.RESP_MESSAGE, jsonObject.getString(CommonConstants.RESP_MESSAGE));
				return map;
			} else {
				map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
				map.put(CommonConstants.RESP_MESSAGE, "请求支付失败,请稍后重试!");
				map.put(CommonConstants.RESULT, ipAddress + "/v1.0/facade/purchase/tofailepage?resp_message="
						+ URLEncoder.encode("请求支付失败,请稍后重试!", "UTF-8"));
				response.sendRedirect(ipAddress + "/v1.0/facade/purchase/tofailepage?resp_message="
						+ URLEncoder.encode("请求支付失败,请稍后重试!", "UTF-8"));
				return map;
			}
		}

		try {
			response.setContentType("text/html;charset=utf-8");
			response.getWriter().println(jsonObject.getString(CommonConstants.RESULT));
			response.getWriter().flush();
			response.getWriter().close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return null;
	}

	// 支付宝异步回调接口
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentchannel/cardevaluationnew/alipay/integral/notify_call")
	public @ResponseBody Object aliPayWapNotify(HttpServletRequest request) {
		// 获取支付宝POST过来反馈信息
		Map<String, String> params = new HashMap<String, String>();
		Map requestParams = request.getParameterMap();

		for (Iterator iter = requestParams.keySet().iterator(); iter.hasNext();) {
			String name = (String) iter.next();
			String[] values = (String[]) requestParams.get(name);
			String valueStr = "";
			for (int i = 0; i < values.length; i++) {
				valueStr = (i == values.length - 1) ? valueStr + values[i] : valueStr + values[i] + ",";
			}

			params.put(name, valueStr);
		}
		LOG.info("支付宝WAP回调进来了============params:" + params);

		String realChannelOrderCode = params.get("trade_no");
		String orderCode = params.get("out_trade_no");
		String amount = params.get("total_amount");
		String tradeStatus = params.get("trade_status");

		Map<String, Object> queryOrdercode = this.queryOrdercode(orderCode);
		Object object = queryOrdercode.get("result");
		JSONObject fromObject = JSONObject.fromObject(object);
		JSONObject resultObj = fromObject.getJSONObject("result");
		LOG.info("resultObj======" + resultObj);
		String userId = resultObj.getString("userid");

		AlipayServiceEnvConstants alipayClient = new AlipayServiceEnvConstants();
		boolean flag = false;
		if (alipayClient != null) {
			try {
				flag = AlipaySignature.rsaCheckV1(params, AlipayServiceEnvConstants.ALIPAY_PUBLIC_KEY,
						AlipayServiceEnvConstants.CHARSET, "RSA2");
			} catch (AlipayApiException e) {
				e.printStackTrace();
				return ResultWrap.err(LOG, CommonConstants.FALIED, "验签异常");
			}
		} else {
			return ResultWrap.err(LOG, CommonConstants.FALIED, "验签失败,无支付宝密钥配置");
		}

		if (!flag) {
			return ResultWrap.err(LOG, CommonConstants.FALIED, "验签失败");
		}

		if (!"TRADE_SUCCESS".equalsIgnoreCase(tradeStatus) && !"TRADE_FINISHED".equalsIgnoreCase(tradeStatus)) {
			return ResultWrap.err(LOG, CommonConstants.FALIED, "非成功回调");
		}

		if (new BigDecimal(resultObj.getString("amount")).compareTo(new BigDecimal(amount)) != 0) {
			return ResultWrap.err(LOG, CommonConstants.FALIED, "验证金额失败");
		}
		if ("1".equals(resultObj.getString("status"))) {
			return ResultWrap.err(LOG, CommonConstants.FALIED, "订单已处理");
		}
		this.updateOrderCodeStatus(orderCode);
		RestTemplate restTemplate = new RestTemplate();
		URI uri = util.getServiceUrl("integralmall", "error url request!");
		String url = uri.toString() + "/v1.0/integralmall/order/updateOrderAndGoods";
		MultiValueMap<String, String> requestEntity = new LinkedMultiValueMap<String, String>();
		requestEntity = new LinkedMultiValueMap<String, String>();
		requestEntity.add("orderCode", orderCode);
		restTemplate = new RestTemplate();
		Object result = null;
		JSONObject jsonObject;
		try {
			result = restTemplate.postForObject(url, requestEntity, Object.class);
			jsonObject = JSONObject.fromObject(result);
			if (jsonObject.getString("resp_code").equals("000000")) {
				LOG.info("============" + orderCode + "================积分消费订单更新成功");
			}
		} catch (Exception e) {
			LOG.info("============" + orderCode + "========积分消费订单更新异常");

		}

		return "SUCCESS";
	}
}
