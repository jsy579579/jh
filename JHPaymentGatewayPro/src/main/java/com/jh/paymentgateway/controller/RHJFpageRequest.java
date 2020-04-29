package com.jh.paymentgateway.controller;

import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
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
import com.alibaba.fastjson.JSON;
import com.jh.paymentgateway.basechannel.BaseChannel;
import com.jh.paymentgateway.business.TopupPayChannelBusiness;
import com.jh.paymentgateway.common.ChannelUtils;
import com.jh.paymentgateway.config.RedisUtil;
import com.jh.paymentgateway.pojo.BankNumCode;
import com.jh.paymentgateway.pojo.JFRegister;
import com.jh.paymentgateway.pojo.PaymentRequestParameter;
import com.jh.paymentgateway.pojo.RHJFBindCard;
import com.jh.paymentgateway.pojo.RHJFRegister;
import com.jh.paymentgateway.pojo.WFBindCard;
import com.jh.paymentgateway.pojo.WFRegister;
import com.jh.paymentgateway.util.rhjf.Base64;
import com.jh.paymentgateway.util.rhjf.Des3Encryption;
import com.jh.paymentgateway.util.rhjf.Disguiser;
import com.jh.paymentgateway.util.rhjf.HTTP;
import com.jh.paymentgateway.util.rhjf.HttpRequest;
import com.jh.paymentgateway.util.rhjf.Md5Util;
import cn.jh.common.utils.CommonConstants;
import cn.jh.common.utils.ExceptionUtil;
import cn.jh.common.utils.IpAddressUtil;
import net.sf.json.JSONObject;

@Controller
@EnableAutoConfiguration
public class RHJFpageRequest extends BaseChannel {

	private static final Logger LOG = LoggerFactory.getLogger(RHJFpageRequest.class);

	@Value("${payment.ipAddress}")
	private String ip;

	@Autowired
	private TopupPayChannelBusiness topupPayChannelBusiness;

	/** 编码字符集 **/
	private static String CHAR_ENCODING = "UTF-8";

	private String deskey = "2VKvLg0ca3r57R80D8M44Q28";

	private String signkey = "on370552j04C809b97gqP5sz6S7Z0w2N";

	@Autowired
	private RestTemplate restTemplate;

	@Autowired
	private RedisUtil redisUtil;

	// 跟还款对接的接口
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/topup/rhjf/torepayment")
	public @ResponseBody Object RHLJCRegister(HttpServletRequest request,
			@RequestParam(value = "bankCard") String bankCard, @RequestParam(value = "idCard") String idCard,
			@RequestParam(value = "phone") String phone, @RequestParam(value = "userName") String userName,
			@RequestParam(value = "bankName") String bankName, @RequestParam(value = "rate") String rate,
			@RequestParam(value = "securityCode") String securityCode

	) throws Exception {

		Map<String, Object> map = new HashMap<String, Object>();

		RHJFRegister rhjfRegister = topupPayChannelBusiness.getRHJFRegisterByIdCard(idCard);
		RHJFBindCard rhjfBindCard = topupPayChannelBusiness.getRHJFBindCardByBankCard(bankCard,"1");

		if (rhjfRegister == null) {

			map = (Map<String, Object>) rhjfRegister(bankCard, idCard, phone, userName, bankName);
			Object respCode = map.get("resp_code");
			Object respMessage = map.get("resp_message");
			LOG.info("respCode=====" + respCode);

			if ("000000".equals(respCode.toString())) {
				map = (Map<String, Object>) rhjfopenProduct(idCard,rate);

				Object respCode1 = map.get("resp_code");
				Object respMessage1 = map.get("resp_message");
				LOG.info("respCode=====" + respCode1);

				if ("000000".equals(respCode1.toString())) {

					map = (Map<String, Object>) rhjfBindCard(bankCard, idCard, phone, securityCode);
					Object respCode2 = map.get("resp_code");
					Object respMessage2 = map.get("resp_message");
					LOG.info("respCode=====" + respCode2);

					if ("000000".equals(respCode2.toString())) {

						map.put(CommonConstants.RESP_CODE, "999996");
						map.put(CommonConstants.RESULT,
								ip+"/v1.0/paymentgateway/topup/rhjf/toBindCard?bankCard=" + bankCard
								+ "&idCard=" + idCard
								+ "&phone=" + phone
								+ "&ipAddress=" + ip
								+ "&securityCode=" + securityCode);
						map.put(CommonConstants.RESP_MESSAGE, "用户需要进行绑卡授权操作");

						return map;
					} else {

						map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
						map.put(CommonConstants.RESP_MESSAGE, "绑卡失败");
						return map;

					}

				} else {

					map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
					map.put(CommonConstants.RESP_MESSAGE, "开通产品失败");
					return map;

				}

			} else {

				map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
				map.put(CommonConstants.RESP_MESSAGE, "商户进件失败");
				return map;

			}

		} else {

			if (rhjfBindCard == null || !"1".equals(rhjfBindCard.getStatus())) {

				map.put(CommonConstants.RESP_CODE, "999996");
				map.put(CommonConstants.RESULT,
						ip+"/v1.0/paymentgateway/topup/rhjf/toBindCard?bankCard=" + bankCard
						+ "&idCard=" + idCard
						+ "&phone=" + phone
						+ "&ipAddress=" + ip
						+ "&securityCode=" + securityCode);
				map.put(CommonConstants.RESP_MESSAGE, "用户需要进行绑卡授权操作");

				return map;

			} else {

				map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
				map.put(CommonConstants.RESP_MESSAGE, "已完成绑卡");

				return map;

			}
		}
	}	
	
	

	// 商户入网进件 的接口
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/topup/rhjf/register")
	public @ResponseBody Object rhjfRegister(@RequestParam(value = "bankCard") String bankCard,
			@RequestParam(value = "idCard") String idCard, @RequestParam(value = "phone") String phone,
			@RequestParam(value = "userName") String userName, @RequestParam(value = "bankName") String bankName)
			throws Exception {

		Map<String, String> maps = new HashMap<String, String>();
		Map<String, String> map = new HashMap<String, String>();

		maps.put("channelName", "上海莘丽网络信息技术有限公司");
		maps.put("channelNo", "8934038053");
		maps.put("merchantName", "上海莘丽网络");
		maps.put("merchantBillName", "莘丽");
		maps.put("installProvince", "上海");
		maps.put("installCity", "上海");
		maps.put("installCounty", "宝山");
		maps.put("operateAddress", "上海宝山区逸仙路2816号");
		maps.put("merchantType", "PERSON");
		maps.put("legalPersonName", userName);
		maps.put("legalPersonID", idCard);
		maps.put("merchantPersonName", userName);
		maps.put("merchantPersonPhone", phone);
		maps.put("bankType", "TOPRIVATE");
		maps.put("accountName", userName);
		String accountNo = encode(deskey, bankCard);
		maps.put("accountNo", accountNo);
		maps.put("bankBranch", bankName);
		BankNumCode bankNumCode = topupPayChannelBusiness.getBankNumCodeByBankName(bankName);
		String bankCode = bankNumCode.getBankBranchcode();
		LOG.info("发送入网请求的联行号" + bankCode);
		maps.put("bankCode", bankCode);
		LOG.info("发送入网的请求报文" + JSON.toJSONString(maps));
		String sign = Md5Util.MD5(JSON.toJSONString(maps) + signkey);
		LOG.info("发送入网请求的报文" + sign);
		maps.put("sign", sign);

		String sendPost = HttpRequest.sendPost("http://portal.ronghuijinfubj.com/middlepayportal/merchant/in",
				JSON.toJSONString(maps));
		LOG.info("请求返回的 post======" + JSON.toJSONString(sendPost));
		JSONObject fromObject = JSONObject.fromObject(sendPost);
		String respCode = fromObject.getString("respCode");
		String respMsg = fromObject.getString("respMsg");
		if ("0000".equals(respCode)) {
			String merchantCode = fromObject.getString("merchantNo");
			String queryKey = fromObject.getString("queryKey");
			RHJFRegister rhjfRegister = new RHJFRegister();
			rhjfRegister.setPhone(phone);
			rhjfRegister.setBankCard(bankCard);
			rhjfRegister.setIdCard(idCard);
			rhjfRegister.setMerchantNo(merchantCode);
			rhjfRegister.setQuerykey(queryKey);
			topupPayChannelBusiness.createRHJFRegister(rhjfRegister);

			map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
			map.put("channel_type", "rhjf");
			map.put(CommonConstants.RESP_MESSAGE, respMsg);
			return map;
		} else {
			LOG.info("请求失败====");
			map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			map.put("channel_type", "rhjf");
			map.put(CommonConstants.RESP_MESSAGE, respMsg);
			return map;

		}

	}

	// 开通产品
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/topup/rhjf/openProduct")
	public @ResponseBody Object rhjfopenProduct(@RequestParam(value = "idCard") String idCard,
			@RequestParam(value = "rateFee") String rateFee) throws Exception {

		// 百分制  rateFee
		String bigRate = new BigDecimal(rateFee).multiply(new BigDecimal("100")).setScale(2).toString();
		LOG.info("bigRate---费率："+bigRate);
		RHJFRegister rhjfRegister = topupPayChannelBusiness.getRHJFRegisterByIdCard(idCard);
		String merchantNo = rhjfRegister.getMerchantNo();

		Map<String, String> map = new HashMap<String, String>();
		Map<String, String> maps = new HashMap<String, String>();
		map.put("productType", "UNIONPAY_QRCODE_DH");
		map.put("merchantNo", merchantNo);
		map.put("t0Fee", bigRate);
		map.put("t1Fee", bigRate);
		LOG.info("===================发送产品开通的报文:" + JSON.toJSONString(map));
		String sign1 = Md5Util.MD5(JSON.toJSONString(map) + signkey);
		LOG.info(sign1);
		map.put("sign", sign1);

		String post = HttpRequest.sendPost("http://portal.ronghuijinfubj.com/middlepayportal/merchant/openProduct",
				JSON.toJSONString(map));
		LOG.info("===================产品开通返回的信息:" + post + merchantNo);
		JSONObject fromObject1 = JSONObject.fromObject(post);
		String respCode = fromObject1.getString("respCode");
		String respMsg = fromObject1.getString("respMsg");
		String signkey1 = fromObject1.getString("signKey");
		String deskey1 = fromObject1.getString("desKey");
		if ("0000".equals(respCode)) {
			RHJFRegister rhjfRegister1 = topupPayChannelBusiness.getRHJFRegisterByIdCard(idCard);

			rhjfRegister1.setRate("0.38");
			rhjfRegister1.setSignkey(signkey1);
			rhjfRegister1.setDeskey(deskey1);
			topupPayChannelBusiness.createRHJFRegister(rhjfRegister1);
			LOG.info("===================开通产品的商户编号:" + merchantNo);

			maps.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
			maps.put("respMsg", respMsg);
			maps.put("channel_type", "rhjf");
			maps.put(CommonConstants.RESP_MESSAGE, "开通商品成功");
			return maps;
		} else {

			LOG.info("请求失败====");
			maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			maps.put("channel_type", "rhjf");
			maps.put(CommonConstants.RESP_MESSAGE, respMsg);
			return maps;
		}

	}

	// 变更费率
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/topup/rhjf/changeMerchantRate")
	public @ResponseBody Object rhjfchangeMerChantRate(@RequestParam(value = "rate") String rate,
			@RequestParam(value = "idCard") String idCard) throws Exception {

		// 百分制
		String bigRate = new BigDecimal(rate).multiply(new BigDecimal("100")).setScale(2).toString();
		RHJFRegister rhjfRegister = topupPayChannelBusiness.getRHJFRegisterByIdCard(idCard);
		String merchantNo = rhjfRegister.getMerchantNo();

		LOG.info("开始进入变更费率接口=================");
		Map<String, String> maps = new HashMap<String, String>();
		Map<String, String> map = new HashMap<String, String>();
		maps.put("merchantNo", merchantNo);
		maps.put("changeType", "1");
		maps.put("productType", "UNIONPAY_QRCODE_DH");
		maps.put("t0Fee", bigRate);
		maps.put("t1Fee", bigRate);

		LOG.info("===================发送变更费率的报文:" + JSON.toJSONString(maps));

		String sign = Md5Util.MD5(JSON.toJSONString(maps) + signkey);

		LOG.info("===================发送变更费率的MD5签名:" + sign);

		maps.put("sign", sign);

		String aa = HttpRequest.sendPost("http://portal.ronghuijinfubj.com/middlepayportal/merchant/changeMerchantInfo",
				JSON.toJSONString(maps));
		LOG.info("===================变更费率返回的信息:" + aa);
		JSONObject fromObject = JSONObject.fromObject(aa);

		String respCode = fromObject.getString("respCode");
		String respMsg = fromObject.getString("respMsg");

		if ("0000".equals(respCode)) {

			rhjfRegister.setRate(rate);

			topupPayChannelBusiness.createRHJFRegister(rhjfRegister);
			LOG.info("===================修改后的费率为:" + rate);

			map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
			map.put("channel_type", "rhjf");
			map.put(CommonConstants.RESP_MESSAGE, "变更费率成功");
			return map;

		} else {
			map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			map.put("channel_type", "rhjf");
			map.put(CommonConstants.RESP_MESSAGE, respMsg);
			return map;

		}

	}

	// 变更结算卡
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/topup/rhjf/changeMerchantBankcard")
	public @ResponseBody Object rhjfchangeMerChantBankcard(@RequestParam(value = "accountNo") String accountNo,
			@RequestParam(value = "idCard") String idCard, @RequestParam(value = "accountName") String accountName,
			@RequestParam(value = "bankName") String bankName) throws Exception {

		RHJFRegister rhjfRegister = topupPayChannelBusiness.getRHJFRegisterByIdCard(idCard);
		String merchantNo = rhjfRegister.getMerchantNo();
		BankNumCode bankNumCode = topupPayChannelBusiness.getBankNumCodeByBankName(bankName);
		String bankCode = bankNumCode.getBankBranchcode();

		LOG.info("开始进入变更结算卡接口=================");
		Map<String, String> maps = new HashMap<String, String>();
		Map<String, String> map = new HashMap<String, String>();

		maps.put("merchantNo", merchantNo);
		maps.put("changeType", "2");
		String accountNo1 = encode(deskey, accountNo);
		maps.put("accountNo", accountNo1);
		maps.put("accountName", accountName);
		maps.put("bankCode", bankCode);
		maps.put("settleBankType", "TOPRIVATE");

		LOG.info("===================发送变更结算卡的报文:" + JSON.toJSONString(maps));

		String sign = Md5Util.MD5(JSON.toJSONString(maps) + signkey);

		LOG.info("===================发送变更结算卡的MD5签名:" + sign);

		maps.put("sign", sign);

		String aa = HttpRequest.sendPost("http://portal.ronghuijinfubj.com/middlepayportal/merchant/changeMerchantInfo",
				JSON.toJSONString(maps));
		LOG.info("===================变更费率返回的信息:" + aa);
		JSONObject fromObject = JSONObject.fromObject(aa);

		String respCode = fromObject.getString("respCode");
		String respMsg = fromObject.getString("respMsg");

		if ("0000".equals(respCode)) {

			rhjfRegister.setBankCard(accountNo);

			topupPayChannelBusiness.createRHJFRegister(rhjfRegister);
			LOG.info("===================修改后的结算卡为:" + accountNo);

			map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
			map.put("channel_type", "rhjf");
			map.put(CommonConstants.RESP_MESSAGE, "修改结算卡成功");
			return map;

		} else {
			map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			map.put("channel_type", "rhjf");
			map.put(CommonConstants.RESP_MESSAGE, "修改结算卡失败");
			return map;

		}

	}

	// 银行卡预请求绑定接口
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/topup/rhjf/bindcard")
	public @ResponseBody Object rhjfBindCard(@RequestParam(value = "bankCard") String bankCard,
			@RequestParam(value = "idCard") String idCard, @RequestParam(value = "phone") String phone,
			@RequestParam(value = "securityCode") String securityCode) throws Exception {
		LOG.info("开始进入绑卡申请接口========");
		
		Map<String, String> maps = new HashMap<String, String>();
		Map<String, String> map = new HashMap<String, String>();
		
		RHJFRegister rhjfRegister = topupPayChannelBusiness.getRHJFRegisterByIdCard(idCard);
		if (rhjfRegister!=null) {
			String merchantNo = rhjfRegister.getMerchantNo();
			LOG.info("sinkry1进来了=======================" + signkey);
			LOG.info("desky1进来了========================" + deskey);
			String orderNo = UUID.randomUUID().toString().substring(0, 16).replace("-", "");
			
			String accno = encode(deskey, bankCard);
			maps.put("accNo", accno);
			maps.put("merchantNo", merchantNo);
			maps.put("orderNum", orderNo);
			maps.put("encrypt", "T0");
			maps.put("type", "0000");
			maps.put("phone", phone);
			maps.put("cvn2", securityCode);
			maps.put("callBackUrl", ip + "/v1.0/paymentgateway/bindcard/rhjf/return_call?bankCard=" + bankCard);
			maps.put("serverCallBackUrl", ip + "/v1.0/paymentgateway/topup/rhjf/bindcard/notify_call?bankCard="+bankCard);

			String sign = Md5Util.MD5(JSON.toJSONString(maps) + signkey);
			LOG.info("MD5签名进来了=======================" + sign);
			maps.put("sign", sign);

			LOG.info("===================发送银行卡绑定接口的请求报文:" + JSON.toJSONString(maps));
			String aa = HttpRequest.sendPost("http://portal.ronghuijinfubj.com/middlepayportal/merchant/openUnionPay",
					JSON.toJSONString(maps));

			LOG.info("请求返回的aa======" + aa);
			String replaceAll = aa.replace("\n", "");
			JSONObject fromObject = JSONObject.fromObject(aa);

			String respCode = fromObject.getString("respCode");
			String respMsg = fromObject.getString("respMsg");
			String html = fromObject.getString("html");

			LOG.info("html======" + html);
			if ("0000".equals(respCode)) {
				RHJFBindCard rhjfBindCard = topupPayChannelBusiness.getRHJFBindCardByBankCard(bankCard,"0");
				if (rhjfBindCard==null) {
					RHJFBindCard rhjf = new RHJFBindCard();
					rhjf.setPhone(phone);
					rhjf.setIdCard(idCard);
					rhjf.setBankCard(bankCard);
					rhjf.setStatus("0");
					rhjf.setOrderCode(orderNo);
					topupPayChannelBusiness.createRHJFBindCard(rhjf);     
				}

				/* 找出指定的2个字符在 该字符串里面的 位置 */
				String strStart = "<form";
				String strEnd = "</body>";
				int strStartIndex = html.indexOf(strStart);
				int strEndIndex = html.indexOf(strEnd);
				String result = html.substring(strStartIndex, strEndIndex);
				LOG.info("截取后的form开始--------------------------");
				System.out.println(result);
				LOG.info("截取后的form结束--------------------------");
				   
				map.put(CommonConstants.RESULT, ip+"/v1.0/paymentgateway/topup/rhjf/toBindCard?bankCard=" + bankCard
						+ "&idCard=" + idCard
						+ "&phone=" + phone
						+ "&ipAddress=" + ip
						+ "&securityCode=" + securityCode
//						+ "&pageContent=" + result
						);
				map.put("channel_type", "rhjf");
				map.put("pageContent", result);
				map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
				map.put(CommonConstants.RESP_MESSAGE, "跳转绑卡界面");

				return map;
			} else {
				LOG.info("请求失败====");
				map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
				map.put("channel_type", "rhjf");
				map.put(CommonConstants.RESP_MESSAGE, "绑定银行卡失败");
				return map;
			}
		}else{
			LOG.info("请求失败====");
			map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			map.put("channel_type", "rhjf");
			map.put(CommonConstants.RESP_MESSAGE, "未进件");
			return map;
		}

	}

	// 银行卡开通页面回调
	@RequestMapping(method = RequestMethod.GET, value = "/v1.0/paymentgateway/bindcard/rhjf/return_call")
	public void returnRHJFNewBindCard(HttpServletRequest request, HttpServletResponse response, Model model)
			throws IOException {

		request.setCharacterEncoding("utf-8");
		response.setCharacterEncoding("utf-8");
		response.setContentType("text/html;charset=utf-8");

	}

	// 银行卡开通回调
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/topup/rhjf/bindcard/notify_call")
	public void rhjfBindCardNotifyCallback(HttpServletRequest request, HttpServletResponse response) throws Exception {
		LOG.info("银行卡开卡回调进来了=======");

		String respCode = request.getParameter("respCode");
		String respMsg = request.getParameter("respMsg");
		String token = request.getParameter("token");
		String orderNum = request.getParameter("orderNum");
		String bankCard = request.getParameter("bankCard");

		Map<String, String> map = new HashMap<String, String>();
		
		if ("0000".equals(respCode)) {
			LOG.info("银行卡绑定成功");
			
			RHJFBindCard rhjfBindCard = topupPayChannelBusiness.getRHJFBindCardByBankCard(bankCard,"0");
			rhjfBindCard.setStatus("1");
			topupPayChannelBusiness.createRHJFBindCard(rhjfBindCard);
			LOG.info("修改绑卡状态成功");
			LOG.info("跳转绑卡成功页面-----------------------------");

		}
		
		PrintWriter pw = response.getWriter();
		pw.print("SUCCESS");
		pw.close();
	}

	// 跳转到绑卡页面
	@RequestMapping(method = RequestMethod.GET, value = "/v1.0/paymentgateway/topup/rhjf/toBindCard")
	public String returnRHJFBindCard(HttpServletRequest request, HttpServletResponse response, Model model)
			throws IOException {

		request.setCharacterEncoding("utf-8");
		response.setCharacterEncoding("utf-8");
		response.setContentType("text/html;charset=utf-8");

		String bankCard = request.getParameter("bankCard");
//		String pageContent = request.getParameter("pageContent");
		String idCard = request.getParameter("idCard");
		String expiredTime = request.getParameter("expiredTime");
		String securityCode = request.getParameter("securityCode");
		String phone = request.getParameter("phone");
		String ipAddress = request.getParameter("ipAddress");

		model.addAttribute("bankCard", bankCard);
		model.addAttribute("idCard", idCard);  
		model.addAttribute("expiredTime", expiredTime);
		model.addAttribute("securityCode", securityCode);
		model.addAttribute("phone", phone);
		model.addAttribute("ipAddress", ipAddress);
//		model.addAttribute("pageContent", pageContent);

		return "rhjfbindcard";
	}

	// 银联在线支付
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/topup/rhjf/onlinepay")
	public @ResponseBody Object rhjfFastPay(HttpServletRequest request,
			@RequestParam(value = "orderCode") String orderCode) throws Exception {
		LOG.info("开始进入快捷支付接口======");

		PaymentRequestParameter prp = redisUtil.getPaymentRequestParameter(orderCode);
		new IpAddressUtil().getRemoteIP(request);

		RHJFRegister rhjfRegister = topupPayChannelBusiness.getRHJFRegisterByIdCard(prp.getIdCard());
		String deskey1 = rhjfRegister.getDeskey();
		String signkey1 = rhjfRegister.getSignkey();
		LOG.info("======================================发送银联在线支付的deskey1:" + deskey1);
		LOG.info("======================================发送银联在线支付的signkey1:" + signkey1);
		Map<String, String> maps = new HashMap<String, String>();
		Map<String, String> map = new HashMap<String, String>();

		String trxType = "UNIONPAY_QRCODE_DH";
		String merchantNo = rhjfRegister.getMerchantNo();
		String orderNum = orderCode;
		String amount = prp.getRealAmount();
		String goodsName = "捷普游泳套装";
		String callbackUrl = ip + "/v1.0/paymentgateway/fast/rhjf/return_call.com";
		String serverCallbackUrl = ip + "/v1.0/paymentchannel/topup/rhjf/fastpay/notify_call.com";
		String orderIp = new IpAddressUtil().getRemoteIP(request);
		String cardno = prp.getBankCard();
		String phoneNumber = prp.getCreditCardPhone();
		String province = "上海市";
		String city = "上海市";
		String cardNo = Des3Encryption.encode(deskey1, cardno);

		// 生成签名原串source
		String source = "#" + trxType + "#" + merchantNo + "#" + orderNum + "#" + amount + "#" + goodsName + "#"
				+ callbackUrl + "#" + serverCallbackUrl + "#" + orderIp + "#" + cardNo + "#" + phoneNumber + "#"
				+ province + "#" + city + "#" + signkey1;
		String generateSign = Disguiser.disguiseMD5(source);

		LOG.info("======================================发送银联在线支付的签名原串:" + source);

		maps.put("trxType", trxType);
		maps.put("merchantNo", merchantNo);
		maps.put("orderNum", orderNum);
		maps.put("amount", amount);
		maps.put("goodsName", goodsName);
		maps.put("callbackUrl", callbackUrl);
		maps.put("serverCallbackUrl", serverCallbackUrl);
		maps.put("orderIp", orderIp);
		maps.put("cardNo", URLEncoder.encode(cardNo));
		maps.put("phoneNumber", phoneNumber);
		maps.put("province", province);
		maps.put("city", city);
		maps.put("sign", generateSign);
		// System.out.println(JSON.toJSONString(maps));
		LOG.info("======================================发送银联在线支付的请求报文:" + maps);

		HTTP http = new HTTP();
		Map aa = http.sendPost("http://trx.ronghuijinfubj.com/middlepaytrx/unionPay/unionPayDH", maps);
		LOG.info("======================================银联在线支付的返回信息ss:" + aa);
		String respCode = (String) aa.get("respCode");
		String respMsg = (String) aa.get("respMsg");
		if ("0000".equals(respCode)) {
			LOG.info("交易支付返回结果------"+respMsg);

			map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
			map.put(CommonConstants.RESP_MESSAGE, respMsg);
			map.put("channel_type", "rhjf");
			return map;

		} else {
			LOG.info("交易支付返回结果------"+respMsg);
			
			map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			map.put(CommonConstants.RESP_MESSAGE, respMsg);
			map.put("channel_type", "rhjf");
			return map;

		}

	}
	// 银联在线支付的返回信息ss:{amount=11.95, orderNumber=e74fcfc81bdc45c1ba8a916cec5853,
	// qrCode=00, trxType=UNIONPAY_QRCODE_DH, retCode=1000, retMsg=T0订单处理成功,
	// merchantNo=B100713332}

	// 银联在线支付页面通知地址
	@RequestMapping(method = RequestMethod.GET, value = "/v1.0/paymentgateway/fast/rhjf/return_call.com")
	public void returnRHJFFast(HttpServletRequest request, HttpServletResponse response, Model model)
			throws IOException {

		request.setCharacterEncoding("utf-8");
		response.setCharacterEncoding("utf-8");
		response.setContentType("text/html;charset=utf-8");

		String respMsg = request.getParameter("respMsg");//

		PrintWriter writer = response.getWriter();
		writer.print(respMsg);

		writer.close();

	}
	

	// 银联在线支付异步通知交易结果
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentchannel/topup/rhjf/fastpay/notify_call.com")
	public void rhjfFastPayNotifyCallback(HttpServletRequest request, HttpServletResponse response) throws Exception {
		LOG.info("银联在线支付异步通知交易结果进来了=======");
		LOG.info("异步返回request:" + request);
		
		Map<String, String[]> parameterMap = request.getParameterMap();
		Set<String> keySet = parameterMap.keySet();
		for (String key : keySet) {
			String[] strings = parameterMap.get(key);
			for (String s : strings) {
				LOG.info(key + "=============" + s);
			}
		}
		
		String retCode = request.getParameter("retCode");
		String r2_orderNumber = request.getParameter("r2_orderNumber");
		String r1_merchantNo = request.getParameter("r1_merchantNo");
		String sinkey = request.getParameter("sinkey");
		
		RHJFRegister rhjfRegister = topupPayChannelBusiness.getRHJFRegisterByMerchantNo(r1_merchantNo);
		String signkey2 = rhjfRegister.getSignkey();
		PaymentRequestParameter prp = redisUtil.getPaymentRequestParameter(r2_orderNumber);

		if (signkey2.equals(sinkey)) {
			LOG.info("银联在线支付成功");
			
			RestTemplate restTemplate = new RestTemplate();
			
			String url = prp.getIpAddress() + "/v1.0/creditcardmanager/update/taskstatus/by/ordercode";
			MultiValueMap<String, String> requestEntity = new LinkedMultiValueMap<String, String>();
			requestEntity.add("orderCode", r2_orderNumber);
			requestEntity.add("version", "8");
			String result = null;
			JSONObject jsonObject;
			JSONObject resultObj;
			
			try {
				result = restTemplate.postForObject(url, requestEntity, String.class);
				LOG.info("RESULT================" + result);
				jsonObject = JSONObject.fromObject(result);
				resultObj = jsonObject.getJSONObject("result");
			} catch (Exception e) {
				e.printStackTrace();LOG.error("",e);
			}

			url = prp.getIpAddress()+ChannelUtils.getCallBackUrl(prp.getIpAddress());
			//url = prp.getIpAddress() + "/v1.0/transactionclear/payment/update";

			requestEntity = new LinkedMultiValueMap<String, String>();
			requestEntity.add("status", "1");
			requestEntity.add("order_code", r2_orderNumber);
			requestEntity.add("third_code", "");
			try {
				result = restTemplate.postForObject(url, requestEntity, String.class);
			} catch (Exception e) {
				e.printStackTrace();LOG.error("",e);
			}

			LOG.info("订单状态修改成功==================="+r2_orderNumber+"====================" + result);

			LOG.info("订单已支付!");
			
		}
			
			
			PrintWriter pw = response.getWriter();
			pw.print("SUCCESS");
			pw.close();

		}
	

	// 查询支付接口
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/topup/rhjf/payquery")
	public @ResponseBody Object PayQuery(@RequestParam(value = "orderCode") String orderCode) throws Exception {
		LOG.info("开始进入订单查询接口======");
		RestTemplate rt = new RestTemplate();

		PaymentRequestParameter prp = redisUtil.getPaymentRequestParameter(orderCode);
		RHJFRegister rhjfRegister = topupPayChannelBusiness.getRHJFRegisterByIdCard(prp.getIdCard());

		String signkey = rhjfRegister.getQuerykey();
		String trxType = "OnlineQuery";
		String r1_merchantNo = rhjfRegister.getMerchantNo();
		String r2_orderNumber = orderCode;

		String source = "#" + trxType + "#" + r1_merchantNo + "#" + r2_orderNumber + "#" + signkey;

		String generateSign = Disguiser.disguiseMD5(source);
		LOG.info("======================================查询支付接口的签名原签:" + generateSign);

		Map<String, String> maps = new HashMap<String, String>();
		Map<String, String> map = new HashMap<String, String>();
		
		maps.put("sign", generateSign);
		maps.put("trxType", trxType);
		maps.put("r1_merchantNo", r1_merchantNo);
		maps.put("r2_orderNumber", r2_orderNumber);
		LOG.info("======================================查询支付接口的请求报文:" + JSON.toJSONString(maps));
		HTTP http = new HTTP();
		Map aa = http.sendPost("http://trx.ronghuijinfubj.com/middlepaytrx/online/query", maps);

		LOG.info("======================================查询支付接口的返回结果ss:" + aa);
		String retCode = (String) aa.get("retCode");
		String retMsg = (String) aa.get("retMsg");

		if ("0000".equals(retCode)) {
			LOG.info("支付查询返回结果------"+retMsg);
			
			map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
			map.put(CommonConstants.RESP_MESSAGE, retMsg);
			map.put("channel_type", "rhjf");
			return map;

		} else {
			LOG.info("支付查询返回结果------"+retMsg);

			map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			map.put(CommonConstants.RESP_MESSAGE, retMsg);
			map.put("channel_type", "rhjf");
			return map;
		}

	}

	// 商户余额查询
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/topup/rhjf/balancequery")
	public @ResponseBody Object balanceQuery(@RequestParam(value = "idCard") String idCard) throws Exception {
		LOG.info("开始进入账户余额查询接口======");

		RHJFRegister rhjfRegister = topupPayChannelBusiness.getRHJFRegisterByIdCard(idCard);

		String deskey1 = rhjfRegister.getDeskey();
		String signkey1 = rhjfRegister.getSignkey();

		String trxType = "MERCHANT_QUERYBALANCE";
		String merchantNo = rhjfRegister.getMerchantNo();
		String accountType = "UnionPay";

		String source = "#" + trxType + "#" + merchantNo + "#" + accountType + "#" + signkey1;
		String generateSign = Disguiser.disguiseMD5(source);

		LOG.info("======================================发送商户余额查询的签名原串:" + generateSign);

		Map<String, String> maps = new HashMap<String, String>();
		Map<String, String> map = new HashMap<String, String>();
		
		maps.put("sign", generateSign);
		maps.put("trxType", trxType);
		maps.put("merchantNo", merchantNo);
		maps.put("accountType", accountType);
		LOG.info("======================================发送商户余额查询的报文:" + JSON.toJSONString(maps));

		HTTP http = new HTTP();
		Map aa = http.sendPost("http://trx.ronghuijinfubj.com/middlepaytrx/online/withdraw/queryMerchantBalance", maps);
		LOG.info("======================================商户余额查询返回的信息ss:" + aa);
		JSONObject fromObject = JSONObject.fromObject(aa);
		String respCode = fromObject.getString("respCode");
		String respMsg = fromObject.getString("respMsg");
		String balance = fromObject.getString("balance");
		if ("0000".equals(respCode)) {
			LOG.info("查询余额返回结果----------"+respMsg);
			
			map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
			map.put(CommonConstants.RESP_MESSAGE, "余额为: " + balance);
			return map;

		} else {
			LOG.info("查询余额返回结果----------"+respMsg);
			
			map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			map.put(CommonConstants.RESP_MESSAGE, respMsg);
			return map;
		}

	}
	// ss{canbalance=4.98, balance=4.98, retCode=0000, retMsg=成功,
	// merchantNo=B100713332}

	// 单笔代付接口
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/topup/rhjf/transfer")
	public @ResponseBody Object rhjfTransfer(@RequestParam(value = "orderCode") String orderCode) throws Exception {

		LOG.info("开始进入代付接口======");

		PaymentRequestParameter prp = redisUtil.getPaymentRequestParameter(orderCode);

		RHJFRegister rhjfRegister = topupPayChannelBusiness.getRHJFRegisterByIdCard(prp.getIdCard());

		String deskey1 = rhjfRegister.getDeskey();
		String signkey1 = rhjfRegister.getSignkey();

		LOG.info("deskey1===============" + deskey1);
		LOG.info("deskey1===============" + deskey1);

		String trxType = "MERCHANT_WITHDRAW";
		String merchantNo = rhjfRegister.getMerchantNo();
		String orderNum = orderCode;
		String amount = prp.getRealAmount();
		String t0Fee = "2.0";
		String cardno = prp.getBankCard();
		String idCardNo = prp.getIdCard();
		String payername = prp.getUserName();
		String bankName = prp.getCreditCardBankName();
		String accountType = "UnionPay";
		String cardNo = Des3Encryption.encode(deskey1, cardno);
		String payerName = Des3Encryption.encode(deskey1, payername);

		// 生成签名原串source
		String source = "#" + trxType + "#" + merchantNo + "#" + orderNum + "#" + amount + "#" + t0Fee + "#" + cardNo
				+ "#" + idCardNo + "#" + payerName + "#" + bankName + "#" + accountType + "#" + signkey1;

		String generateSign = Disguiser.disguiseMD5(source);
		LOG.info("======================================发送单笔代付的签名原串:" + source);
		Map<String, String> maps = new HashMap<String, String>();
		Map<String, String> map = new HashMap<String, String>();
		
		maps.put("sign", generateSign);
		maps.put("trxType", trxType);
		maps.put("merchantNo", merchantNo);
		maps.put("orderNum", orderNum);
		maps.put("amount", amount);
		maps.put("t0Fee", t0Fee);
		maps.put("cardNo", cardNo);
		maps.put("idCardNo", idCardNo);
		maps.put("payerName", payerName);
		maps.put("bankName", bankName);
		maps.put("accountType", accountType);

		LOG.info("======================================发送单笔代付的报文:" + JSON.toJSONString(maps));
		HTTP http = new HTTP();
		Map aa = http.sendPost("http://trx.ronghuijinfubj.com/middlepaytrx/online/withdraw/merchantWithDraw", maps);

		LOG.info("======================================单笔代付返回的信息ss:" + aa);

		String retCode = (String) aa.get("retCode");
		String retMsg = (String) aa.get("retMsg");

		if ("0000".equals(retCode)) {
			map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
			map.put(CommonConstants.RESP_MESSAGE, retMsg);
			return map;
		} else {
			map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			map.put(CommonConstants.RESP_MESSAGE, retMsg);
			return map;
		}

	}
	// ss{amount=10.0, t0Fee=2.0, orderNum=123459788889004, retCode=0000,
	// retMsg=成功-成功, merchantNo=B100713332}

	// 代付结果查询
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/topup/rhjf/transferquery")
	public @ResponseBody Object rhjftransferquery(@RequestParam(value = "orderCode") String orderCode)
			throws Exception {
		LOG.info("开始进入订单查询接口======");

		PaymentRequestParameter prp = redisUtil.getPaymentRequestParameter(orderCode);
		RHJFRegister rhjfRegister = topupPayChannelBusiness.getRHJFRegisterByIdCard(prp.getIdCard());

		String signkey = rhjfRegister.getSignkey();// 签名密钥

		String trxType = "MERCHANT_WITHDRAWQUERY";
		String merchantNo = rhjfRegister.getMerchantNo();
		String orderNum = orderCode;

		String source = "#" + trxType + "#" + merchantNo + "#" + orderNum + "#" + signkey;
		String generateSign = Disguiser.disguiseMD5(source);

		LOG.info("======================================发送代付结果查询的签名原串:" + generateSign);
		Map<String, String> maps = new HashMap<String, String>();
		Map<String, String> map = new HashMap<String, String>();
		
		maps.put("sign", generateSign);
		maps.put("trxType", trxType);
		maps.put("merchantNo", merchantNo);
		maps.put("orderNum", orderNum);
		LOG.info("======================================发送代付结果查询的报文:" + JSON.toJSONString(maps));
		System.out.println(JSON.toJSONString(maps));
		HTTP http = new HTTP();
		Map aa = http.sendPost("http://trx.ronghuijinfubj.com/middlepaytrx/online/withdraw/queryMerchantWithDrawOrder",
				maps);

		LOG.info("======================================代付结果查询返回的信息ss:" + aa);

		String retCode = (String) aa.get("retCode");
		String retMsg = (String) aa.get("retMsg");
		if ("0000".equals(retCode)) {
			LOG.info("代付查询结果------------------------------------"+retMsg);
			
			map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
			map.put(CommonConstants.RESP_MESSAGE, retMsg);
			return map;

		} else {
			LOG.info("代付查询结果------------------------------------"+retMsg);
			
			map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			map.put(CommonConstants.RESP_MESSAGE, retMsg);
			return map;
		}

	}
	// ss{orderNum=123459788889004, withDrawCode=success, retCode=0000,
	// retMsg=成功, withDrawMsg=成功-成功}

	/**
	 * Base64编码
	 * 
	 * @param key
	 * @param data
	 * @return
	 */
	public static String encode(String key, String data) {
		try {
			byte[] keyByte = key.getBytes(CHAR_ENCODING);
			byte[] dataByte = data.getBytes(CHAR_ENCODING);
			byte[] valueByte = RHJFpageRequest.des3Encryption(keyByte, dataByte);
			String value = new String(Base64.encode(valueByte), CHAR_ENCODING);
			return value;
		} catch (Exception e) {
			return null;
		}
	}

	/**
	 * des3Encryption加密
	 * 
	 * @param key
	 * @param data
	 * @return
	 */
	public static byte[] des3Encryption(byte[] key, byte[] data)
			throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, BadPaddingException,
			IllegalBlockSizeException, IllegalStateException {
		final String Algorithm = "DESede";

		SecretKey deskey = new SecretKeySpec(key, Algorithm);

		Cipher c1 = Cipher.getInstance(Algorithm);
		c1.init(Cipher.ENCRYPT_MODE, deskey);
		return c1.doFinal(data);
	}
}

