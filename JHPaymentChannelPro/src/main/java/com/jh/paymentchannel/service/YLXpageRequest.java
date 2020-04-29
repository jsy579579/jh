package com.jh.paymentchannel.service;

import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.lang.StringUtils;
import org.apache.http.NameValuePair;
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
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.stereotype.Controller;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestTemplate;

import com.jh.paymentchannel.basechannel.BaseChannel;
import com.jh.paymentchannel.business.BranchbankBussiness;
import com.jh.paymentchannel.business.TopupPayChannelBusiness;
import com.jh.paymentchannel.pojo.BranchNo;
import com.jh.paymentchannel.pojo.HLJCBindCard;
import com.jh.paymentchannel.pojo.HLJCRegister;
import com.jh.paymentchannel.util.Util;
import com.jh.paymentchannel.util.hljc.Base64;
import com.jh.paymentchannel.util.hljc.RSAUtils;

import cn.jh.common.utils.CommonConstants;
import cn.jh.common.utils.ExceptionUtil;
import cn.jh.common.utils.Md5Util;
import net.sf.json.JSONObject;

@Controller
@EnableAutoConfiguration
public class YLXpageRequest extends BaseChannel {

	private static final Logger LOG = LoggerFactory.getLogger(YLXpageRequest.class);

	@Autowired
	private TopupPayChannelBusiness topupPayChannelBusiness;

	@Autowired
	private BranchbankBussiness branchbankBussiness;

	@Autowired
	private Util util;

	@Autowired
	private RestTemplate restTemplate;

	@Value("${payment.ipAddress}")
	private String ipAddress;

	private String customerNo = "gl00033679";

	private String appId = "7aee88cd4b654e2692664851b9603acf";

	private String key = "c740a4e131b74e9aa2ca191d60d61bf4f1601e59505a4bd6b5f753bfa6eaaf01";

	private String Url = "http://39.108.137.8:8099/v1.0/facade/repay";

	private static final Charset UTF_8 = StandardCharsets.UTF_8;

	// 快捷支付接口
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentchannel/topup/ylx/fastpay")
	public @ResponseBody Object ylxFastPay(@RequestParam(value = "ordercode") String ordercode) throws Exception {
		LOG.info("开始进入快捷支付接口======");

		Map<String, Object> maps = new HashMap<String, Object>();

		Map<String, Object> queryOrdercode = this.queryOrdercode(ordercode);
		Object object = queryOrdercode.get("result");
		JSONObject fromObject = JSONObject.fromObject(object);
		JSONObject resultObj = fromObject.getJSONObject("result");

		String amount = resultObj.getString("amount");
		
		Map<String, String> map = new TreeMap<String, String>();

		map.put("appId", appId);
		map.put("custNo", customerNo);
		map.put("payChannel", "05");
		map.put("money", amount);
		map.put("attach", "fastPay");
		map.put("callBackUrl", ipAddress + "/v1.0/paymentchannel/topup/ylx/fastpay/notify_call");
		map.put("mchOrderNo", ordercode);

		Set<String> keySet = map.keySet();
		Iterator<String> it = keySet.iterator();
		StringBuffer sb = new StringBuffer();

		while (it.hasNext()) {
			String next = it.next();
			sb.append(next + "=" + map.get(next) + "&");
		}

		String param = sb.substring(0, sb.length() - 1);
		param = param + key;
		String md5 = Md5Util.getMD5(param);
		map.put("sign", md5);

		LOG.info("快捷支付的请求报文======" + map);

		String post = doPost("https://open.goodluckchina.net/open/pay/scanCodePayChannel", map);

		LOG.info("请求返回的post======" + post);

		fromObject = JSONObject.fromObject(post);

		String code = fromObject.getString("code");

		if ("1".equals(code)) {
			String payUrl = fromObject.getString("pay_url");
			maps.put("resp_code", "success");
			maps.put("channel_type", "jf");
			maps.put("redirect_url", payUrl);

		} else {
			String msg = fromObject.getString("msg");
			maps.put("resp_code", "failed");
			maps.put("channel_type", "jf");
			maps.put("resp_message", msg);

		}

		return maps;

	}

	// 订单查询接口
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentchannel/topup/ylx/ordercodequery")
	public @ResponseBody Object orderCodeQuery(HttpServletRequest request,
			@RequestParam(value = "ordercode") String ordercode) throws Exception {
		LOG.info("开始进入订单查询接口======");
		Map<String, String> maps = new HashMap<String, String>();

		Map<String, String> map = new TreeMap<String, String>();
		map.put("appId", appId);
		map.put("custNo", customerNo);
		map.put("mchOrderNo", ordercode);
		
		Set<String> keySet = map.keySet();
		Iterator<String> it = keySet.iterator();
		StringBuffer sb = new StringBuffer();
		
		while(it.hasNext()) {
			String next = it.next();
			sb.append(next + "=" + map.get(next) + "&");
		}
		
		String param = sb.substring(0, sb.length()-1);
		
		param = param + key;

		String md5 = Md5Util.getMD5(param);
		
		map.put("sign", md5);
		
		LOG.info("查询订单的请求报文======" + map);

		String doPost = doPost("https://open.goodluckchina.net/open/pay/orderStatusQuery", map);

		LOG.info("请求查询订单返回的doPost====" + doPost);

		JSONObject fromObject = JSONObject.fromObject(doPost);

		String code = fromObject.getString("code");
		String msg = fromObject.getString("msg");
		
		if("1".equals(code)) {
			JSONObject jsonObject = fromObject.getJSONObject("data");
			String status = jsonObject.getString("status");
			
			if("01".equals(status)) {
				LOG.info("支付成功======");
				maps.put("resp_code", "success");
				maps.put("resp_message", "支付成功");
			}else if("00".equals(status)) {
				LOG.info("订单不存在======");
				maps.put("resp_code", "failed");
				maps.put("resp_message", "订单不存在");
			}else if("03".equals(status)) {
				LOG.info("处理中======");
				maps.put("resp_code", "success");
				maps.put("resp_message", "处理中");
			}else {
				LOG.info("支付失败======");
				maps.put("resp_code", "success");
				maps.put("resp_message", "支付失败");
			}
			
		}else {
			
			
		}

		return maps;
	}

	// 快捷支付异步通知接口
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentchannel/topup/ylx/fastpay/notify_call")
	public String hljcFastPayNotifyCallback(HttpServletRequest request, HttpServletResponse response) throws Exception {
		LOG.info("快捷支付异步通知进来了=======");

		Map<String, String[]> parameterMap = request.getParameterMap();
		Set<String> keySet = parameterMap.keySet();
		for (String key : keySet) {
			String[] strings = parameterMap.get(key);
			for (String s : strings) {
				LOG.info(key + "=============" + s);
			}
		}
		
		String return_msg = request.getParameter("return_msg");
		String pay_time = request.getParameter("pay_time");
		String pay_status = request.getParameter("pay_status");
		String money = request.getParameter("money");
		String cust_no = request.getParameter("cust_no");
		String trade_no = request.getParameter("trade_no");
		String pay_channel = request.getParameter("pay_channel");
		String attach = request.getParameter("attach");
		String order_id = request.getParameter("order_id");
		String return_code = request.getParameter("return_code");
		String mch_order_no = request.getParameter("mch_order_no");
		
		String sign = request.getParameter("sign");
		
		
		Map<String,Object> map = new TreeMap<String, Object>();
		map.put("return_msg", return_msg);
		map.put("pay_time", pay_time);
		map.put("pay_status", pay_status);
		map.put("money", money);
		map.put("cust_no", cust_no);
		map.put("trade_no", trade_no);
		map.put("pay_channel", pay_channel);
		map.put("attach", attach);
		map.put("order_id", order_id);
		map.put("return_code", return_code);
		map.put("mch_order_no", mch_order_no);
		
		Set<String> keySet2 = map.keySet();
		Iterator<String> it = keySet2.iterator();
		StringBuffer sb = new StringBuffer();
		
		while(it.hasNext()) {
			String next = it.next();
			sb.append(next + "=" + map.get(next) + "&");
		}
		
		String param = sb.substring(0, sb.length()-1);
		
		param  = param + key;
		
		String md5 = Md5Util.getMD5(param);
		LOG.info("md5======"+md5);
		
		if(md5.toLowerCase().equals(sign)) {
			LOG.info("验签成功======");
			
			if("success".equals(pay_status) && "SUCCESS".equals(return_code)) {
				
				RestTemplate restTemplate = new RestTemplate();

				URI uri = util.getServiceUrl("transactionclear", "error url request!");
				String url = uri.toString() + "/v1.0/transactionclear/payment/update";

				// **根据的用户手机号码查询用户的基本信息*//*
				MultiValueMap<String, String> requestEntity = new LinkedMultiValueMap<String, String>();
				requestEntity.add("status", "1");
				requestEntity.add("third_code", order_id);
				requestEntity.add("order_code", mch_order_no);
				String result = restTemplate.postForObject(url, requestEntity, String.class);

				LOG.info("订单状态修改成功===================");

				LOG.info("订单已支付!");
				
				Map<String, Object> queryOrdercode = this.queryOrdercode(mch_order_no);
				Object object = queryOrdercode.get("result");
				JSONObject fromObject = JSONObject.fromObject(object);
				JSONObject resultObj = fromObject.getJSONObject("result");

				String outMerOrdercode = resultObj.getString("outMerOrdercode");
				String orderdesc = resultObj.getString("desc");
				String phone = resultObj.getString("phone");
				String tranamount = resultObj.getString("amount");
				String channelTag = resultObj.getString("channelTag");
				String notifyURL = resultObj.getString("outNotifyUrl");
				if (outMerOrdercode != null && !outMerOrdercode.equalsIgnoreCase("")) {
					uri = util.getServiceUrl("channel", "error url request!");
					url = uri.toString() + "/v1.0/channel/callback/yilian/notify_call";
					requestEntity = new LinkedMultiValueMap<String, String>();
					requestEntity.add("merchant_no", phone);
					requestEntity.add("amount", tranamount);
					requestEntity.add("channel_tag", channelTag);
					requestEntity.add("order_desc", URLEncoder.encode(orderdesc, "UTF-8"));
					requestEntity.add("order_code", outMerOrdercode);
					requestEntity.add("sys_order", mch_order_no);
					requestEntity.add("notify_url", URLEncoder.encode(notifyURL, "UTF-8"));
					result = restTemplate.postForObject(url, requestEntity, String.class);
				}
				
				return "\"success\"";
			}else {
				
				return null;
			}
			
		}else {
			LOG.info("验签失败======");
			return null;
		}

	}

	
	
	// 请求方式
	private static String doPost(String url, Map<String, String> map) throws IOException {
		String result = null;
		CloseableHttpClient httpClient = HttpClients.createDefault();
		CloseableHttpResponse response = null;
		HttpPost httpPost = new HttpPost(url);
		try {
			if (map != null) {
				// 设置2个post参数
				List<NameValuePair> parameters = new ArrayList<NameValuePair>();
				for (String key : map.keySet()) {
					parameters.add(new BasicNameValuePair(key, (String) map.get(key)));
				}
				// 构造一个form表单式的实体
				UrlEncodedFormEntity formEntity = new UrlEncodedFormEntity(parameters, "UTF-8");
				// 将请求实体设置到httpPost对象中
				httpPost.setEntity(formEntity);
			}

			response = httpClient.execute(httpPost);

			int statusCode = response.getStatusLine().getStatusCode();
			if (statusCode == HttpStatus.SC_OK) {
				result = EntityUtils.toString(response.getEntity(), "UTF-8");
			}
		} catch (Exception e) {
			e.printStackTrace();
			LOG.error("",e);
		} finally {
			response.close();
		}
		return result;
	}

}