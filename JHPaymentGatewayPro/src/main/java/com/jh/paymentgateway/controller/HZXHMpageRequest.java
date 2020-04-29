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
import com.jh.paymentgateway.pojo.PaymentRequestParameter;
import com.jh.paymentgateway.util.hz.Md5;
import com.jh.paymentgateway.util.hz.Md5.Md5Utils;

import cn.jh.common.utils.CommonConstants;
import cn.jh.common.utils.ExceptionUtil;

@Controller
@EnableAutoConfiguration
public class HZXHMpageRequest extends BaseChannel {

	@Autowired
	private RedisUtil redisUtil;

	@Autowired
	private TopupPayChannelBusiness topupPayChannelBusiness;

	@Value("${payment.ipAddress}")
	private String ip;

	private static final Logger LOG = LoggerFactory.getLogger(HZXHMpageRequest.class);

	private static String key = "b4ca882ff6705721"; // 加密秘钥
	private static String merchantId = "999290048270008"; // 机构号

	
	/**
	 * 跳转到账卡页面
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/topup/hzxhm/toPayPage")
	public Object toPayPage(@RequestParam(value = "orderCode") String orderCode)
			throws IOException {
		LOG.info("跳转到账卡页面-----------------");
		Map<String, Object> maps = new HashMap<String, Object>();
		PaymentRequestParameter prp = redisUtil.getPaymentRequestParameter(orderCode);
		String cardNo = prp.getDebitCardNo();
		String amount = prp.getAmount();
		String bankName = prp.getDebitBankName();
		String cardName = prp.getCreditCardBankName();
		String rip  = prp.getIpAddress();
		
		if (cardName.contains("邮政")) {
			if (new BigDecimal(amount).compareTo(new BigDecimal("4000")) < 0) {

				maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
				maps.put(CommonConstants.RESP_MESSAGE, "邮政银行卡交易金额限制为2000-5000以内,请核对重新输入金额!");

				this.addOrderCauseOfFailure(orderCode, "邮政银行卡交易金额限制为2000-5000以内,请核对重新输入金额!", rip);

				return maps;

			}
		} else if (cardName.contains("招商")) {
			if (new BigDecimal(amount).compareTo(new BigDecimal("4000")) < 0) {

				maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
				maps.put(CommonConstants.RESP_MESSAGE, "招商银行卡交易金额限制为2000-5000以内,请核对重新输入金额!");

				this.addOrderCauseOfFailure(orderCode, "招商银行卡交易金额限制为2000-5000以内,请核对重新输入金额!", rip);

				return maps;
			}
		} 
  
		maps.put(CommonConstants.RESULT, ip + "/v1.0/paymentgateway/topup/hzxhm/jump/pay?ordercode="+orderCode
				+ "&bankCard=" + cardNo
				+ "&amount=" + amount
				+ "&bankName=" + URLEncoder.encode(bankName, "UTF-8")
				+ "&ipAddress=" + ip);
		maps.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
		maps.put(CommonConstants.RESP_MESSAGE, "跳转到到账卡页面");
		
		return maps;
	}
	
	/**
	 * 申请交易   
	 *     
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/quick/hzxhm/topay")
	public @ResponseBody Object topay(@RequestParam(value = "orderCode") String orderCode) throws Exception {

		LOG.info("============ 进入申请交易 ============");

		PaymentRequestParameter prp = redisUtil.getPaymentRequestParameter(orderCode);

		String userName = prp.getUserName();
		String phoneD = prp.getDebitPhone();
		String rate = prp.getRate();
		String idCard = prp.getIdCard();
		String cardNo = prp.getDebitCardNo();
		String ExtraFee = prp.getExtraFee();
		String bankCard = prp.getBankCard();
		String cvn2 = prp.getSecurityCode();
		String ExpiredTime = prp.getExpiredTime();
		String phoneC = prp.getCreditCardPhone();
		String amount = prp.getAmount();
		String cbankName = prp.getCreditCardBankName();
		String dbankName = prp.getDebitBankName();

		Map<String, Object> maps = new HashMap<String, Object>();

		Map<String, Object> map = new HashMap<String, Object>();
		
		// 获取银行联行号
		BankNumCode ccode = topupPayChannelBusiness.getBankNumCodeByBankName(cbankName);
		String bankChannelNo = ccode.getBankBranchcode();// 交易卡支行号
		
		BankNumCode bcode = topupPayChannelBusiness.getBankNumCodeByBankName(dbankName);
		String settBankCode = bcode.getBankBranchcode();// 到账卡支行号

		map.put("merchantId", merchantId);
		map.put("merOrderId", orderCode);
		map.put("totalFee", getNumber(amount));// 固定值单位：分
		map.put("body", "流行鞋/服饰/包/首饰");
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
		map.put("backNotifyUrl", ip + "/v1.0/paymentgateway/topup/hzxhm/pay/call-back");// 异步
		map.put("settRate", rate);
		map.put("settAffix", getNumber(ExtraFee));// 固定值单位：分
		map.put("settBankCode", settBankCode);
		map.put("settAccNo", cardNo);
		map.put("settAccProvince", "上海");
		map.put("settAccCity", "上海市");
		map.put("settMobile", phoneD);
		map.put("address", "上海市宝山区");
		map.put("transChannel", "tl9");// 交易渠道sl0
										// 商旅(sl1,sl2,sl3,sl5,sl6,sl7,sl8) zb0
										// 珠宝 bh0 百货 (bh1)yc0 烟草 xe0 小额xe1 云闪付大额

		SimpleDateFormat s = new SimpleDateFormat("yyyyMMddHHmmss");
		String timeStamp = s.format(new Date());
		map.put("timeStamp", timeStamp);

		Map<String, Object> param = JSONObject.parseObject(JSONObject.toJSONString(map), Map.class);
	    LOG.info("LD:" + param.toString());
	    Map<String, String> fs = parseMap(param);

	    fs.put("sign", createPaySign(fs));
	    LOG.info("交易请求加密参数:" + createPaySign(fs));
	    String url = "http://www.allforbenefit.com:8083/HzGateway/kjv3/FastPay/subOrder.do";
	    String res = Md5.HttpUtil.executePost(url, fs);
	    
	    com.alibaba.fastjson.JSONObject js = com.alibaba.fastjson.JSONObject.parseObject(res);
	    LOG.info("交易请求返回参数"+ js);
	    String data = js.getString("data");

		LOG.info("html：" + data);
		
		/* 找出指定的2个字符在 该字符串里面的 位置 */
		String strStart = "<body>";
		String strEnd = "</body>";
		int strStartIndex = data.indexOf(strStart);
		int strEndIndex = data.indexOf(strEnd);
		String result = data.substring(strStartIndex + 6, strEndIndex);
		System.out.println("截取后的form开始--------------------------");
		System.out.println(result);
		System.out.println("截取后的form结束--------------------------");

		String resCode = js.getString("resCode");
		String resMsg = js.getString("resMsg");
		if ("0000".equals(resCode)) {
			LOG.info("----------  跳转交易   ----------");

			maps.put("pageContent", result);
			maps.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
			maps.put(CommonConstants.RESP_MESSAGE, resMsg);
			return maps;
		} else {

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
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/quick/hzxhm/queryOrder")
	public @ResponseBody Object queryOrder(@RequestParam(value = "orderCode") String orderCode) throws Exception {

		LOG.info("============ 进入惠至交易查询接口 ============");
		PaymentRequestParameter prp = redisUtil.getPaymentRequestParameter(orderCode);

		Map<String, Object> maps = new HashMap<String, Object>();

		Map<String, Object> map = new HashMap<String, Object>();

		map.put("merchantId", merchantId);
		map.put("merOrderId", orderCode);
		SimpleDateFormat s = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String timeStamp = s.format(new Date());
		map.put("timeStamp", timeStamp);

		Map<String, Object> param = JSONObject.parseObject(JSONObject.toJSONString(map), Map.class);
	    LOG.info("LD:" + param.toString());
	    Map<String, String> fs = parseMap(param);

	    fs.put("sign", createPaySign(fs));
	    
	    String url = "http://www.allforbenefit.com:8083/HzGateway/kjv3/FastPay/queryOrder.do";
	    
	    String res = Md5.HttpUtil.executePost(url, fs);
	    LOG.info("交易查询请求参数:" + res);
	    com.alibaba.fastjson.JSONObject js = com.alibaba.fastjson.JSONObject.parseObject(res);
	    LOG.info("交易查询返回参数"+ js);
	    String data = js.getString("data");

		LOG.info("data：" + data);
		
		String resCode = js.getString("resCode");
		String resMsg = js.getString("resMsg");

		if ("0000".equals(resCode)) {
			LOG.info("----------  交易成功   ----------");

				maps.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
				maps.put(CommonConstants.RESP_MESSAGE, resMsg);
				return maps;
		} else {
			LOG.info("----------  交易失败   ----------");

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
			RequestMethod.GET }, value = "/v1.0/paymentgateway/topup/hzxhm/pay/call-back")
	public void openFront(HttpServletRequest request, HttpServletResponse response) throws Exception {
		LOG.info("-------------------  hzxhm消费回调   -------------------");
		String merId = request.getParameter("merId");// 商户号
		String orderCode = request.getParameter("merOrderId");
		String orderDate = request.getParameter("orderDate");// 交易时间
		String tranAmt = request.getParameter("tranAmt");// 交易金额，分
		String settStat = request.getParameter("settStat");// 结算状态： 00表示结算成功
		String resCdoe = request.getParameter("resCdoe");// 0000:表示支付成功
		String resMsg = request.getParameter("resMsg");// 返回描述
		
		PaymentRequestParameter prp = redisUtil.getPaymentRequestParameter(orderCode);
		if ("0000".equals(resCdoe)) {
			LOG.info("------------------- hzxhm交易成功 -------------------");
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
	@RequestMapping(method = RequestMethod.GET, value = "/v1.0/paymentgateway/topup/hzxhm/jump/pay")
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

		return "hzxhmpay";
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
