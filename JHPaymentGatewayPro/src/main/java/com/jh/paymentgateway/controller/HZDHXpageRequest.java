package com.jh.paymentgateway.controller;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.URI;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang.StringUtils;
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

import com.alibaba.fastjson.JSONObject;
import com.jh.paymentgateway.basechannel.BaseChannel;
import com.jh.paymentgateway.business.TopupPayChannelBusiness;
import com.jh.paymentgateway.common.ChannelUtils;
import com.jh.paymentgateway.config.RedisUtil;
import com.jh.paymentgateway.pojo.BankNumCode;
import com.jh.paymentgateway.pojo.HZDHAddress;
import com.jh.paymentgateway.pojo.LMTAddress;
import com.jh.paymentgateway.pojo.PaymentRequestParameter;
import com.jh.paymentgateway.util.hz.Md5;
import com.jh.paymentgateway.util.hz.Md5.Md5Utils;

import cn.jh.common.tools.ResultWrap;
import cn.jh.common.utils.CommonConstants;

@Controller
@EnableAutoConfiguration
public class HZDHXpageRequest extends BaseChannel {

	@Autowired
	private RedisUtil redisUtil;

	@Autowired
	private TopupPayChannelBusiness topupPayChannelBusiness;

	@Value("${payment.ipAddress}")
	private String ip;

	private static final Logger LOG = LoggerFactory.getLogger(HZDHXpageRequest.class);

	private static String key = "b4ca882ff6705721"; // 加密秘钥
	private static String merchantId = "999290048270008"; // 机构号
	
	// 跟还款对接的接口
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/topup/hzdh/torepayment")
	public @ResponseBody Object hzdhToRepayment(HttpServletRequest request,
			@RequestParam(value = "bankCard") String bankCard) throws Exception {
		
		LOG.info("惠至代还小花猫无需绑卡： " + bankCard);

		return ResultWrap.init(CommonConstants.SUCCESS, "无需绑卡!");
	}

	/**
	 * 申请交易   
	 *     
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/quick/hzdh/topay")
	public @ResponseBody Object putPay(@RequestParam(value = "orderCode") String orderCode,
			@RequestParam(value = "areaCode") String areaCode) throws Exception {
		
		LOG.info("============ 进入申请交易 ============");

		PaymentRequestParameter prp = redisUtil.getPaymentRequestParameter(orderCode);

		String userName = prp.getUserName();
		String rate = prp.getRate();
		String idCard = prp.getIdCard();
		String bankCard = prp.getBankCard();
		String cvn2 = prp.getSecurityCode();
		String ExpiredTime = prp.getExpiredTime();
		String phoneC = prp.getCreditCardPhone();
		String amount = prp.getAmount();
		String cbankName = prp.getCreditCardBankName();

		Map<String, Object> maps = new HashMap<String, Object>();

		Map<String, Object> map = new HashMap<String, Object>();
		
		// 获取银行联行号
		BankNumCode ccode = topupPayChannelBusiness.getBankNumCodeByBankName(cbankName);
		String bankChannelNo = ccode.getBankBranchcode();// 交易卡支行号

		map.put("merchantId", merchantId);
		map.put("merOrderId", orderCode);
		map.put("totalFee", getNumber(amount));// 固定值单位：分
		map.put("body", "流行鞋/服饰/包/珠宝/首饰");
		map.put("accType", "CREDIT");// 交易卡类型 CREDIT-信用卡
		map.put("accName", userName);
		map.put("accNo", bankCard);
		map.put("bankCode", bankChannelNo);
		map.put("idType", "01");// 固定值 01 身份证
		map.put("idNo", idCard);
		map.put("cvn2", cvn2);

		String expire = this.expiredTimeToMMYY(ExpiredTime);
		map.put("expire", expire);// 交易卡有效期mmyy 月月年年

		map.put("mobile", phoneC);
		map.put("frontNotifyUrl", "http://106.15.47.73/v1.0/paymentchannel/topup/yldzpaying");// 前端
		map.put("backNotifyUrl", ip + "/v1.0/paymentgateway/topup/hzdh/pay/call-back");// 异步
		map.put("settRate", rate);
		map.put("settAffix", "0");// 固定值单位：分
		map.put("settBankCode", bankChannelNo);
		map.put("settAccNo", bankCard);
		map.put("settAccProvince", "上海");
		map.put("settAccCity", "上海市");
		map.put("settMobile", phoneC);
		map.put("address", "上海市宝山区");
		map.put("transChannel", "hk0");// tj0 特殊商户
									   // hk0  信用卡还款   还款settAffix会被设置成0
									   // fq0 分期付款 当为fq是settRate是分期手续费+刷卡手续费
									   // bh9  商户自选
		
		map.put("mchtType", areaCode);// 商户类型
		SimpleDateFormat s = new SimpleDateFormat("yyyyMMddHHmmss");
		String timeStamp = s.format(new Date());
		map.put("timeStamp", timeStamp);

		Map<String, Object> param = JSONObject.parseObject(JSONObject.toJSONString(map), Map.class);
	    LOG.info("HZDH:" + param.toString());
	    Map<String, String> fs = parseMap(param);

	    fs.put("sign", createPaySign(fs));
	    LOG.info("HZDH交易请求加密参数:" + createPaySign(fs));
	    String url = "http://www.allforbenefit.com:8083/HzGateway/kjv3/FastPay/subOrder.do";
	    String res = Md5.HttpUtil.executePost(url, fs);
	    
	    com.alibaba.fastjson.JSONObject js = com.alibaba.fastjson.JSONObject.parseObject(res);
	    LOG.info("HZDH交易请求返回参数"+ js);
	    
		String resCode = js.getString("resCode");
		String resMsg = js.getString("resMsg");
		if ("0000".equals(resCode)) {
			LOG.info("----------  HZDH交易请求成功   ----------");

			maps.put(CommonConstants.RESP_CODE, "999998");
			maps.put(CommonConstants.RESP_MESSAGE, resMsg);
			return maps;
		} else {
			LOG.info("----------  HZDH交易请求失败   ----------");

			maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			maps.put(CommonConstants.RESP_MESSAGE, resMsg);
			return maps;
		}
	}

	/**
	 * 交易查询
	 * 
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/quick/HZDH/queryOrder")
	public @ResponseBody Object queryOrder(@RequestParam(value = "orderCode") String orderCode) throws Exception {

		LOG.info("============ 进入惠至交易查询接口 ============");
		PaymentRequestParameter prp = redisUtil.getPaymentRequestParameter(orderCode);

		Map<String, Object> maps = new HashMap<String, Object>();

		Map<String, Object> map = new HashMap<String, Object>();

		map.put("merchantId", merchantId);
		map.put("merOrderId", orderCode);
		SimpleDateFormat s = new SimpleDateFormat("yyyyMMddHHmmss");
		String timeStamp = s.format(new Date());
		map.put("timeStamp", timeStamp);

		Map<String, Object> param = JSONObject.parseObject(JSONObject.toJSONString(map), Map.class);
	    LOG.info("HZDH:" + param.toString());
	    Map<String, String> fs = parseMap(param);

	    fs.put("sign", createPaySign(fs));
	    
	    String url = "http://www.allforbenefit.com:8083/HzGateway/kjv3/FastPay/queryOrder.do";
	    
	    String res = Md5.HttpUtil.executePost(url, fs);
	    LOG.info("HZDH交易查询请求参数:" + res);
	    com.alibaba.fastjson.JSONObject js = com.alibaba.fastjson.JSONObject.parseObject(res);
	    LOG.info("HZDH交易查询返回参数"+ js);
		
		String resCode = js.getString("resCode");
		String resMsg = js.getString("resMsg");

		if ("0000".equals(resCode)) {
			LOG.info("----------  HZDH查询交易成功   ----------");
		    String data = js.getString("data");

			LOG.info("data：" + data);

			maps.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
			maps.put(CommonConstants.RESP_MESSAGE, resMsg + ",data:" + data);
			return maps;
		} else {
			LOG.info("----------  HZDH查询交易失败   ----------");

			maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			maps.put(CommonConstants.RESP_MESSAGE, resMsg);
			return maps;
		}
	}
	
	/**
	 * 还款
	 * 
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/quick/hzdh/transfer")
	public @ResponseBody Object transfer(@RequestParam(value = "orderCode") String orderCode) throws Exception {

		LOG.info("============ 进入HZDH还款接口 ============");
		PaymentRequestParameter prp = redisUtil.getPaymentRequestParameter(orderCode);
		String daifuAmt = prp.getRealAmount();
		String daifuFee = prp.getExtraFee();
		String mobile = prp.getCreditCardPhone();
		String cbankName = prp.getCreditCardBankName();
		String idCard = prp.getIdCard();
		String name = prp.getUserName();
		String bankCard = prp.getBankCard();
		
		// 获取银行联行号
		BankNumCode ccode = topupPayChannelBusiness.getBankNumCodeByBankName(cbankName);
		String bankChannelNo = ccode.getBankBranchcode();// 交易卡支行号

		Map<String, Object> maps = new HashMap<String, Object>();

		Map<String, Object> map = new HashMap<String, Object>();

		map.put("merchantId", merchantId);
		map.put("merOrderId", orderCode);
		
		String realA = getNumber(daifuAmt);
		String fee = getNumber(daifuFee);
		int a = Integer.valueOf(realA);
		int e = Integer.valueOf(fee);
		String am = String .valueOf(a + e);
		LOG.info("实际到账金额（分）：" + realA + ",手续费（分）：" + fee + "提现的金额（分，实际金额+手续费）：" + am);
		
		map.put("daifuAmt", am);// 还款金额 单位分
		map.put("daifuFee", fee);// 还款手续费 单位分
		map.put("mobile", mobile);
		map.put("bankCode", bankChannelNo);
		map.put("bankName", cbankName);
		map.put("bankNum", bankCard);
		map.put("idNo", idCard);
		map.put("name", name);
		map.put("daifuType", "0");// 还款类型  0 代付
		map.put("transChannel", "hk0");
		map.put("backNotifyUrl", ip + "/v1.0/paymentgateway/topup/hzdh/putpay/call-back");
		
		SimpleDateFormat s = new SimpleDateFormat("yyyyMMddHHmmss");
		String timeStamp = s.format(new Date());
		map.put("timeStamp", timeStamp);

		Map<String, Object> param = JSONObject.parseObject(JSONObject.toJSONString(map), Map.class);
	    LOG.info("HZDH:" + param.toString());
	    Map<String, String> fs = parseMap(param);

	    fs.put("sign", createPaySign(fs));
	    
	    String url = "http://www.allforbenefit.com:8083/HzGateway/kjv3/HkPay/hk.do";
	    
	    String res = Md5.HttpUtil.executePost(url, fs);
	    LOG.info("HZDH还款请求参数:" + res);
	    com.alibaba.fastjson.JSONObject js = com.alibaba.fastjson.JSONObject.parseObject(res);
	    LOG.info("HZDH还款返回参数"+ js);
		
		String resCode = js.getString("resCode");
		String resMsg = js.getString("resMsg");
		if ("0000".equals(resCode)) {
			LOG.info("----------  HZDH还款成功   ----------");

			maps.put(CommonConstants.RESP_CODE, "999998");
			maps.put(CommonConstants.RESP_MESSAGE, resMsg);
			return maps;
		} else {
			LOG.info("----------  HZDH还款失败   ----------");

			maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			maps.put(CommonConstants.RESP_MESSAGE, resMsg);
			return maps;
		}
	}
	
	/**
	 * 代付查询
	 * 
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/quick/hzdh/queryPutStatus")
	public @ResponseBody Object queryPutStatus(@RequestParam(value = "orderCode") String orderCode) throws Exception {

		LOG.info("============ 进入HZHD还款查询接口 ============");

		Map<String, Object> maps = new HashMap<String, Object>();

		Map<String, Object> map = new HashMap<String, Object>();

		map.put("merchantId", merchantId);
		map.put("merOrderId", orderCode);
		SimpleDateFormat s = new SimpleDateFormat("yyyyMMddHHmmss");
		String timeStamp = s.format(new Date());
		map.put("timeStamp", timeStamp);

		Map<String, Object> param = JSONObject.parseObject(JSONObject.toJSONString(map), Map.class);
	    LOG.info("HZDH:" + param.toString());
	    Map<String, String> fs = parseMap(param);

	    fs.put("sign", createPaySign(fs));
	    
	    String url = "http://www.allforbenefit.com:8083/HzGateway/kjv3/HkPay/hkQry.do";
	    
	    String res = Md5.HttpUtil.executePost(url, fs);
	    LOG.info("HZHD还款查询请求参数:" + res);
	    com.alibaba.fastjson.JSONObject js = com.alibaba.fastjson.JSONObject.parseObject(res);
	    LOG.info("HZHD还款查询返回参数"+ js);
		
		String resCode = js.getString("resCode");
		String resMsg = js.getString("resMsg");
		if ("0000".equals(resCode)) {
			LOG.info("----------  HZHD还款查询成功   ----------");
				
			maps.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
			maps.put(CommonConstants.RESP_MESSAGE, resMsg);
			return maps;
		} else {
			LOG.info("----------  HZHD还款查询失败   ----------");

			maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			maps.put(CommonConstants.RESP_MESSAGE, resMsg);
			return maps;
		}
	}

	/**
	 * 余额查询
	 * 
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/quick/hzdh/balance")
	public @ResponseBody Object balance(@RequestParam(value = "idCard") String idCard,
			@RequestParam(value = "userName") String userName) throws Exception {

		LOG.info("============ 进入HZDH余额查询接口 ============");

		Map<String, Object> maps = new HashMap<String, Object>();

		Map<String, Object> map = new HashMap<String, Object>();
		
		String requestNo = "HZDH" + System.currentTimeMillis();
		LOG.info("===HZDH余额查询请求订单号：" + requestNo);
				
		map.put("merchantId", merchantId);
		map.put("merOrderId", requestNo);
		map.put("idNo", idCard);
		map.put("name", userName);
		map.put("transChannel", "hk0");
		
		SimpleDateFormat s = new SimpleDateFormat("yyyyMMddHHmmss");
		String timeStamp = s.format(new Date());
		map.put("timeStamp", timeStamp);

		Map<String, Object> param = JSONObject.parseObject(JSONObject.toJSONString(map), Map.class);
	    LOG.info("HZDH:" + param.toString());
	    Map<String, String> fs = parseMap(param);

	    fs.put("sign", createPaySign(fs));
	    
	    String url = "http://www.allforbenefit.com:8083/HzGateway/kjv3/HkPay/balance.do";
	    
	    String res = Md5.HttpUtil.executePost(url, fs);
	    LOG.info("HZDH余额查询请求参数:" + res);
	    com.alibaba.fastjson.JSONObject js = com.alibaba.fastjson.JSONObject.parseObject(res);
	    LOG.info("HZDH余额查询返回参数"+ js);
		
		String resCode = js.getString("resCode");
		String resMsg = js.getString("resMsg");
		if ("0000".equals(resCode)) {
			String data = js.getString("data");
			LOG.info("----------  HZDH余额查询成功   ----------");

			maps.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
			maps.put(CommonConstants.RESP_MESSAGE, resMsg + "，余额：" + data + "（分）");
			return maps;
		} else {
			LOG.info("----------  HZDH余额查询失败   ----------");

			maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			maps.put(CommonConstants.RESP_MESSAGE, resMsg);
			return maps;
		}
	}
	
	/**
	 * 交易异步通知
	 * 
	 * @param encryptData
	 * @param signature
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(method = { RequestMethod.POST,
			RequestMethod.GET }, value = "/v1.0/paymentgateway/topup/hzdh/pay/call-back")
	public void openFront(HttpServletRequest request, HttpServletResponse response) throws Exception {
		LOG.info("-------------------  HZDH消费回调   -------------------");
		String merId = request.getParameter("merId");// 商户号
		String orderCode = request.getParameter("merOrderId");
		String orderDate = request.getParameter("orderDate");// 交易时间
		String tranAmt = request.getParameter("tranAmt");// 交易金额，分
		String settStat = request.getParameter("settStat");// 结算状态： 00表示结算成功
		String resCdoe = request.getParameter("resCdoe");// 0000:表示支付成功
		String resMsg = request.getParameter("resMsg");// 返回描述
		
		PaymentRequestParameter prp = redisUtil.getPaymentRequestParameter(orderCode);
		if ("0000".equals(resCdoe)) {
			LOG.info("------------------- HZDH交易成功 -------------------");
			LOG.info("交易订单：" + orderCode);
			LOG.info("交易商户号：" + merId);
			LOG.info("交易时间：" + orderDate);
			LOG.info("交易金额：" + tranAmt + "分");
			RestTemplate restTemplate = new RestTemplate();
			MultiValueMap<String, String> requestEntity = new LinkedMultiValueMap<String, String>();
			String url = null;
			String result = null;
			
			url = prp.getIpAddress()+ChannelUtils.getCallBackUrl(prp.getIpAddress());
			//url = prp.getIpAddress() + "/v1.0/transactionclear/payment/update";

			requestEntity = new LinkedMultiValueMap<String, String>();
			requestEntity.add("status", "1");
			requestEntity.add("order_code", orderCode);
			requestEntity.add("third_code", "");
			try {
				result = restTemplate.postForObject(url, requestEntity, String.class);
			} catch (Exception e) {
				e.printStackTrace();
				LOG.error("",e);
			}
			LOG.info("修改订单状态成功：===================" + orderCode + "====================" + result);
			PrintWriter pw = response.getWriter();
			pw.print("success");
			pw.close();
		}  else {
			LOG.info("===================  交易失败  ===================");
			this.addOrderCauseOfFailure(orderCode, resMsg, prp.getIpAddress());
			
			PrintWriter pw = response.getWriter();
			pw.print("success");
			pw.close();
		}
	}
	
	/**
	 * 代付异步通知
	 * 
	 * @param encryptData
	 * @param signature
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(method = { RequestMethod.POST,
			RequestMethod.GET }, value = "/v1.0/paymentgateway/topup/hzdh/putpay/call-back")
	public void putpay(HttpServletRequest request, HttpServletResponse response) throws Exception {
		LOG.info("-------------------  HZDH代付回调   -------------------");
		String merId = request.getParameter("merId");// 商户号
		String orderCode = request.getParameter("merOrderId");
		String orderDate = request.getParameter("orderDate");// 交易时间
		String tranAmt = request.getParameter("tranAmt");// 交易金额，分
		String settStat = request.getParameter("settStat");// 结算状态： 00表示结算成功
		String resCdoe = request.getParameter("resCdoe");// 0000:表示支付成功
		String resMsg = request.getParameter("resMsg");// 返回描述
		
		PaymentRequestParameter prp = redisUtil.getPaymentRequestParameter(orderCode);
		if ("00".equals(settStat)) {
			LOG.info("------------------- HZDH代付成功 -------------------");
			LOG.info("交易订单：" + orderCode);
			LOG.info("交易商户号：" + merId);
			LOG.info("交易时间：" + orderDate);
			LOG.info("交易金额：" + tranAmt + "分");
			RestTemplate restTemplate = new RestTemplate();
			MultiValueMap<String, String> requestEntity = new LinkedMultiValueMap<String, String>();
			String url = null;
			String result = null;
			
			url = prp.getIpAddress()+ChannelUtils.getCallBackUrl(prp.getIpAddress());
			//url = prp.getIpAddress() + "/v1.0/transactionclear/payment/update";

			requestEntity = new LinkedMultiValueMap<String, String>();
			requestEntity.add("status", "1");
			requestEntity.add("order_code", orderCode);
			requestEntity.add("third_code", "");
			try {
				result = restTemplate.postForObject(url, requestEntity, String.class);
			} catch (Exception e) {
				e.printStackTrace();
				LOG.error("",e);
			}
			LOG.info("修改订单状态成功：===================" + orderCode + "====================" + result);
			PrintWriter pw = response.getWriter();
			pw.print("success");
			pw.close();
		}  else {
			LOG.info("===================  交易失败  ===================");
			this.addOrderCauseOfFailure(orderCode, resMsg, prp.getIpAddress());
			
			PrintWriter pw = response.getWriter();
			pw.print("success");
			pw.close();
		}
	}
	
	/**
	 * 中转页面
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/v1.0/paymentgateway/topup/hzdh/jump/pay")
	public String jumpPay(HttpServletRequest request, HttpServletResponse response, Model model)
			throws IOException {
		LOG.info("跳转到到账卡页面-----------------");

		request.setCharacterEncoding("utf-8");
		response.setCharacterEncoding("utf-8");
		response.setContentType("text/html;charset=utf-8");
		
		String ordercode = request.getParameter("ordercode");
		String ipAddress = request.getParameter("ipAddress");
		String bankName = request.getParameter("bankName");
		String bankCard = request.getParameter("bankCard");
		String amount = request.getParameter("amount");

		model.addAttribute("ordercode", ordercode);
		model.addAttribute("ipAddress", ipAddress);
		model.addAttribute("bankName", bankName);
		model.addAttribute("bankCard", bankCard);
		model.addAttribute("amount", amount);
		model.addAttribute("ipAddress", ipAddress);

		return "hzdhpay";
	}
	
	/**
	 * 根据省份id查询该省份所有的市
	 * 
	 * @param request
	 * @param response
	 * @return
	 * @throws IOException
	 */
	@RequestMapping(method = RequestMethod.POST, value = ("/v1.0/paymentgateway/hzdh/merchant/queryall"))
	public @ResponseBody Object findCity(@RequestParam(value = "provinceName") String provinceName) {
		LOG.info("provinceName---------------------：" + provinceName);
		Map map = new HashMap();
		List<HZDHAddress> list = topupPayChannelBusiness.findHZDHMerchant(provinceName);
		map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
		map.put(CommonConstants.RESULT, list);
		map.put(CommonConstants.RESP_MESSAGE, "成功");
		return map;
	}

	/**
	 * 查询所有省/直辖市/自治区
	 * 
	 * @param request
	 * @param response
	 * @return
	 * @throws IOException
	 */
	@RequestMapping(method = RequestMethod.POST, value = ("/v1.0/paymentgateway/hzdh/province/queryall"))
	public @ResponseBody Object findProvince() {
		Map map = new HashMap();
		List<HZDHAddress> list = topupPayChannelBusiness.findHZDHProvince();
		map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
		map.put(CommonConstants.RESULT, list);
		map.put(CommonConstants.RESP_MESSAGE, "成功");
		return map;
	}
	
	private Map<String, String> parseMap(Map<String, Object> map) {
        Map<String, String> fsMap = new TreeMap<>();
        for (Iterator<Map.Entry<String, Object>> its = map.entrySet().iterator(); its.hasNext();
                ) {
            Map.Entry<String, Object> entry = its.next();
            if (entry.getValue() != null && !StringUtils.isEmpty(entry.getValue().toString())) {
                fsMap.put(entry.getKey(), entry.getValue().toString());
            }
        }
        return fsMap;
    }

    public static String createPaySign(Map<String, String> param) {

        StringBuffer signStr = new StringBuffer();
        int size = param.entrySet().size();
        int i = 1;
        for (Iterator<Map.Entry<String, String>> its = param.entrySet().iterator(); its.hasNext();
                ) {
            Map.Entry<String, String> entry = its.next();
            if (entry.getValue() != null && !StringUtils.isEmpty(entry.getValue().toString())) {
                signStr.append(entry.getKey()).append("=").append(entry.getValue().toString()).append("&");
            }
        }

        signStr.append("key=").append(key);
        System.out.println(signStr);
        String md5Sign = Md5Utils.signature(signStr.toString()).toUpperCase();

        return md5Sign;
    }
    
    /**
	 * 金额/分
	 * 
	 * @param ExtraFee
	 * @return
	 */
	public static String getNumber(String ExtraFee) {
		BigDecimal num1 = new BigDecimal(ExtraFee);
		BigDecimal num2 = new BigDecimal("100");
		BigDecimal rsNum = num1.multiply(num2);
		BigDecimal MS = rsNum.setScale(0, BigDecimal.ROUND_DOWN);
		LOG.info("金额/分：" + MS.toString());
		return MS.toString();
	}
}
