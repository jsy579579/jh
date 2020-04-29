package com.jh.paymentgateway.controller;

import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.codec.binary.Base64;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
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

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import com.jh.paymentgateway.basechannel.BaseChannel;
import com.jh.paymentgateway.business.TopupPayChannelBusiness;
import com.jh.paymentgateway.common.ChannelUtils;
import com.jh.paymentgateway.config.RedisUtil;
import com.jh.paymentgateway.pojo.PaymentRequestParameter;
import com.jh.paymentgateway.pojo.YFJRBinkCard;
import com.jh.paymentgateway.pojo.YFJRRegister;
import com.jh.paymentgateway.pojo.YTJFSignCard;
import com.jh.paymentgateway.util.yf.Common;
import com.jh.paymentgateway.util.yf.HttpClientUtils;
import com.jh.paymentgateway.util.yf.JSONUtil;
import com.jh.paymentgateway.util.yf.LocalUtil;
import com.jh.paymentgateway.util.yf.MessageResponse;
import com.jh.paymentgateway.util.yf.RSAUtils2;
import com.jh.paymentgateway.util.yf.RequestJson;
import com.jh.paymentgateway.util.ytjf.CHexConver;
import com.jh.paymentgateway.util.ytjf.HttpClient;
import com.jh.paymentgateway.util.ytjf.MD5;
import com.jh.paymentgateway.util.ytjf.SecurityUtils;

import cn.jh.common.utils.CommonConstants;
import cn.jh.common.utils.DateUtil;
import cn.jh.common.utils.ExceptionUtil;
import net.sf.json.JSONObject;

@Controller
@EnableAutoConfiguration
public class YTJFpageRequest extends BaseChannel {

	private static final Logger log = LoggerFactory.getLogger(YTJFpageRequest.class);

	@Autowired
	private RedisUtil redisUtil;
	
	@Value("${ytjf.merchantId}")
	private String merchantId;
	
	@Value("${ytjf.datekey}")
	private String datekey;
	
	@Value("${ytjf.payUrl}")
	private String payUrl;
	
	@Value("${ytjf.encode}")
	private String ENCODE;
	
	@Value("${ytjf.path}")
	private String path;

	@Value("${payment.ipAddress}")
	private String ip;

	@Autowired
	private TopupPayChannelBusiness topupPayChannelBusiness;

	
	// 跳转到绑卡页面
	@RequestMapping(method = RequestMethod.GET, value = "/v1.0/paymentgateway/topup/ytjf/toBindCard")
	public String returnRHJFBindCard(HttpServletRequest request, HttpServletResponse response, 
			@RequestParam(value = "orderCode") String orderCode, Model model)
			throws IOException {
		PaymentRequestParameter prp = redisUtil.getPaymentRequestParameter(orderCode);
		
		
		String idCard = prp.getIdCard();
		
		String bankCard = prp.getBankCard();
		
		String phone = prp.getCreditCardPhone();
		
		String expired = prp.getExpiredTime().replace("/", "");
		
		String expiredTime = this.expiredTimeToMMYY(expired);
		
		String  securityCode=prp.getSecurityCode();
		
		String ipAddress = ip;

		model.addAttribute("bankCard", bankCard);
		
		model.addAttribute("idCard", idCard);  
		
		model.addAttribute("ordercode", prp.getOrderCode());
		
		model.addAttribute("expiredTime", expiredTime);
		
		model.addAttribute("securityCode", securityCode);
		
		model.addAttribute("phone", phone);
		
		model.addAttribute("ipAddress", ipAddress);
		
		return "ytjfbindcard";
	}

	/**
	 * 预下单接口/获取短信验证码接口
	 * 
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/topup/ytjf/SendMessage")
	public @ResponseBody Object SendMessage(HttpServletRequest request, 
			@RequestParam(value = "orderCode") String orderCode)throws Exception {
		
		Map<String, Object> map = new HashMap<String, Object>();
		
		PaymentRequestParameter prp = redisUtil.getPaymentRequestParameter(orderCode);
		
		String name = prp.getUserName();
		
		String idCard = prp.getIdCard();
		
		String bankCard = prp.getBankCard();
		
		String phone = prp.getCreditCardPhone();
		
		String amount = prp.getAmount();
		
		String expired = prp.getExpiredTime().replace("/", "");
		
		String expiredTime = this.expiredTimeToMMYY(expired);
		
		String  securityCode=prp.getSecurityCode();
		
		// 金额 单位分
		String tranAmt = new BigDecimal(amount).multiply(new BigDecimal("100")).setScale(0).toString();
		
		String userId =prp.getUserId();
		
		YTJFSignCard ytjfSignCard =topupPayChannelBusiness.getYTJFSignCardByBankCard(bankCard);
		if(ytjfSignCard==null){
			ytjfSignCard=new YTJFSignCard();
			ytjfSignCard.setBankCard(bankCard);
			ytjfSignCard.setCreateTime(new Date());
		}
		ytjfSignCard.setIdCard(idCard);
		ytjfSignCard.setPhone(phone);
		ytjfSignCard.setUserName(name);
		ytjfSignCard.setStatus("0");
		//发送地址
		String URL=payUrl+"/NetPay/sendMessage.action";
        HttpClient client = new HttpClient(URL, 80000, 80000);
        Map<String, String> mapData = new HashMap<String, String>();
        mapData.put("merchantId", merchantId);//商编
        mapData.put("bussId", "ONL0022");//业务代码
        mapData.put("userAcctNo", SecurityUtils.encrypt(bankCard,path)); //真实银行卡号
        mapData.put("userName",CHexConver.str2HexStr(name)); //姓名
        mapData.put("userPhone",SecurityUtils.encrypt(phone,path)); //手机号
        mapData.put("certNo", SecurityUtils.encrypt(idCard,path)); //身份证号
        mapData.put("cardCvn2", SecurityUtils.encrypt(securityCode,path)); //cvv2
        mapData.put("cardExpire",SecurityUtils.encrypt(expiredTime,path)); //有效期
        mapData.put("userIp",prp.getIpAddress());
        mapData.put("tranAmt", tranAmt);
        mapData.put("merOrderNum", orderCode);
        mapData.put("userId", userId);
        String txnString = SendMessage(mapData);//签名
        log.info("txnString:" + txnString);
        String signValue = MD5.getInstance().getMD5ofStr(txnString + datekey);
        mapData.put("signValue", signValue);
        log.info("signValue:" + signValue);
        String result=client.send(mapData, ENCODE);
        Map<String, String> res = null;
        try {
            Gson gson = new Gson();
            res = gson.fromJson(result, new TypeToken<Map<String, String>>() {
            }.getType());
            if(res.get("respCode").equals("0000")) {
            	ytjfSignCard.setTransId(res.get("transId"));
            	if(res.containsKey("smsKey")){
            		ytjfSignCard.setSmsKey(res.get("smsKey"));
            	}else{
            		ytjfSignCard.setSmsKey("");
            	}
            	topupPayChannelBusiness.createYTJFSignCard(ytjfSignCard);
            	map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
        		map.put(CommonConstants.RESP_MESSAGE, res.get("respMsg"));
            }else{
            	map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
        		map.put(CommonConstants.RESP_MESSAGE, res.get("respMsg"));
            }
            
        } catch (JsonSyntaxException e) {
        	map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
    		map.put(CommonConstants.RESP_MESSAGE, "发送失败请稍后重试");
        }
		return map;
	}
	/**
	 * 支付签约并支付
	 * 
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/topup/ytjf/QuickSign")
	public @ResponseBody Object QuickSign(HttpServletRequest request, 
			@RequestParam(value = "smsCode") String smsCode,
			@RequestParam(value = "orderCode") String orderCode)throws Exception {
		Map<String, Object> map = new HashMap<String, Object>();
		PaymentRequestParameter prp = redisUtil.getPaymentRequestParameter(orderCode);
		String name = prp.getUserName();
		String idCard = prp.getIdCard();
		String bankCard = prp.getBankCard();
		String phone = prp.getCreditCardPhone();
		String amount = prp.getAmount();
		String expired = prp.getExpiredTime();
		String expiredTime = this.expiredTimeToMMYY(expired);
		String securityCode=prp.getSecurityCode();
		// 金额 单位分
		String tranAmt = new BigDecimal(amount).multiply(new BigDecimal("100")).setScale(0).toString();
		YTJFSignCard ytjfSignCard =topupPayChannelBusiness.getYTJFSignCardByBankCard(bankCard);
		if(ytjfSignCard==null){
			map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
    		map.put(CommonConstants.RESP_MESSAGE, "签约失败");
		}
		//发送地址
		String URL=payUrl+"/NetPay/quickSign.action";
		 HttpClient client=new HttpClient(URL,6000,6000);
        Map<String, String> mapData = new HashMap<String, String>();

        mapData.put("merchantId",merchantId); //商户号
        mapData.put("bussId", "ONL0022");//业务代码
        mapData.put("userAcctNo", SecurityUtils.encrypt(bankCard,path)); //真实银行卡号
        mapData.put("userName",CHexConver.str2HexStr(name)); //姓名
        mapData.put("userPhone",SecurityUtils.encrypt(phone,path)); //手机号
        mapData.put("certNo", SecurityUtils.encrypt(idCard,path)); //身份证号
        mapData.put("cardCvn2", SecurityUtils.encrypt(securityCode,path)); //cvv2
        mapData.put("cardExpire",SecurityUtils.encrypt(expiredTime,path)); //有效期
        mapData.put("tranAmt",tranAmt);
        mapData.put("merOrderNum",orderCode); //和短信接口订单号相同
        mapData.put("frontUrl","http://www.baidu.com");
        mapData.put("smsKey",ytjfSignCard.getSmsKey());
        mapData.put("smsCode",smsCode);
        mapData.put("smsTranId",ytjfSignCard.getTransId());
        String txnString = QuickSign(mapData);//签名
        log.info("txnString:" + txnString);
        String signValue = MD5.getInstance().getMD5ofStr(txnString + datekey);
        mapData.put("signValue", signValue);
        log.info("signValue:" + signValue);
        String result=client.send(mapData, ENCODE);
        Map<String, String> res = null;
        try {
            Gson gson = new Gson();
            res = gson.fromJson(result, new TypeToken<Map<String, String>>() {
            }.getType());
            if(res.get("respCode").equals("0000")) {
            	ytjfSignCard.setProtocolNo(res.get("protocolNo"));
            	ytjfSignCard.setStatus("1");
            	topupPayChannelBusiness.createYTJFSignCard(ytjfSignCard);
            	map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
            	map.put("redirect_url", ip+"/v1.0/paymentgateway/quick/ytjf/SynonymNamePay?orderCode="+orderCode);
        		map.put(CommonConstants.RESP_MESSAGE, res.get("respMsg"));
            }else{
            	map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
        		map.put(CommonConstants.RESP_MESSAGE, res.get("respMsg"));
            }
        } catch (JsonSyntaxException e) {
        	map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
    		map.put(CommonConstants.RESP_MESSAGE, "发送失败请稍后重试");
        }
		return map;
	}
	
	/**
	 * 跳转结算卡页面
	 * 
	 * @param request
	 * @param response
	 * @param model
	 * @return
	 * @throws IOException
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/v1.0/paymentgateway/quick/ytjf/SynonymNamePay")
	public String JumpReceivablesCard(HttpServletRequest request, HttpServletResponse response,
			@RequestParam(value = "orderCode") String orderCode,  Model model)
			throws IOException {
		PaymentRequestParameter prp = redisUtil.getPaymentRequestParameter(orderCode);
		String bankName = prp.getDebitBankName();// 结算卡银行名称
		String bankNo = prp.getDebitCardNo();// 结算卡卡号
		String amount = prp.getAmount();
		String ordercode = prp.getOrderCode();
		String cardType = prp.getDebitCardNature();// 结算卡的卡类型
		model.addAttribute("bankName", bankName);
		model.addAttribute("bankNo", bankNo);
		model.addAttribute("amount", amount);
		model.addAttribute("ordercode", ordercode);
		model.addAttribute("cardType", cardType);
		model.addAttribute("ipAddress", 	ip);
		return "ytjfbankinfo";
	}
	
	
	/**
	 * 快捷付 V2.0 接口（银联新无卡合并通道）
	 * 
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/topup/ytjf/SynonymNamePay")
	public @ResponseBody Object SynonymNamePay(HttpServletRequest request, 
			@RequestParam(value = "orderCode") String orderCode)throws Exception {
		Map<String, Object> map = new HashMap<String, Object>();
		PaymentRequestParameter prp = redisUtil.getPaymentRequestParameter(orderCode);
		String name = prp.getUserName();
		String idCard = prp.getIdCard();
		String bankCard = prp.getBankCard();
		String phone = prp.getCreditCardPhone();
		String amount = prp.getAmount();
		String expired = prp.getExpiredTime();
		String expiredTime = this.expiredTimeToMMYY(expired);
		String  securityCode=prp.getSecurityCode();
		String debitCardNo=prp.getDebitCardNo();
		String debitPhone=prp.getDebitPhone();
		String debitBankName=prp.getDebitBankName();
		// 金额 单位分
		String tranAmt = new BigDecimal(amount).multiply(new BigDecimal("100")).setScale(0).toString();
		String reserver1= (new BigDecimal(amount).subtract(new BigDecimal(prp.getRealAmount()))).multiply(new BigDecimal("100")).setScale(0).toString();
		
		
		String userId =prp.getUserId();
		YTJFSignCard ytjfSignCard =topupPayChannelBusiness.getYTJFSignCardByBankCard(bankCard);
		if(ytjfSignCard==null){
			map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
    		map.put(CommonConstants.RESP_MESSAGE, "签约失败");
		}
		//发送地址
		String URL=payUrl+"/NetPay/SynonymNamePay.action";
		 HttpClient client=new HttpClient(URL,6000,6000);
        Map<String, String> mapData = new HashMap<String, String>();
        mapData.put("version","1.0.0"); //版本号
        mapData.put("transCode","8888"); //交易代码
        mapData.put("merchantId",merchantId); //商户号
        mapData.put("merOrderNum",orderCode); //商户订单号
        mapData.put("bussId","ONL0022");//业务代码
        mapData.put("tranAmt",tranAmt);//交易金额(单位： 分)
        mapData.put("sysTraceNum",orderCode);//商户请求流水号
        mapData.put("tranDateTime",DateUtil.getyyyyMMddHHmmssDateFormat(new Date()));//交易时间YYYYMMDDHHMMSS
        mapData.put("currencyType","156");//货币代码
        mapData.put("merURL",ip+"/v1.0/paymentgateway/topup/ytjf/payreturn");//回调商户地址
        mapData.put("backURL",ip+"/v1.0/paymentgateway/topup/ytjf/payCallback");//回调商户地址
        mapData.put("orderInfo","http://www.baidu.com");//订单信息
        mapData.put("userId","290956168");//订单信息
        mapData.put("userNameHF",CHexConver.str2HexStr(name)); //开户名（姓名）
        mapData.put("quickPayCertNo", idCard); //身份证号
        mapData.put("arrviedAcctNo", debitCardNo); //入账卡卡号
        mapData.put("arrviedPhone",debitPhone); //入账卡绑定手机号
        mapData.put("arrviedBankName",debitBankName); //入账卡开户行
        mapData.put("userAcctNo",bankCard); //交易卡卡号
        mapData.put("userPhoneHF",phone); //交易卡绑定手机号
        mapData.put("cardCvn2",securityCode); //cvv2
        mapData.put("cardExpire",expiredTime); //有效日期
        mapData.put("userIp", ip); //订单用户 IP
        mapData.put("protocolNo",ytjfSignCard.getProtocolNo()); //快捷协议号
        mapData.put("bankId","888880170122900"); //支付方式代码
        mapData.put("stlmId",""); //结算规则代码
        mapData.put("entryType","1"); //入口类型
        mapData.put("attach",""); //附加数据
        mapData.put("reserver1",reserver1); //保留域 1商户上传的手续费， 单位分 10
        mapData.put("reserver2",""); //保留域
        mapData.put("reserver3",""); //保留域
        mapData.put("reserver4","7"); //保留域
        String txnString = SynonymNamePay(mapData);//签名
        log.info("txnString:" + txnString);
        String signValue = MD5.getInstance().getMD5ofStr(txnString + datekey);
        mapData.put("signValue", signValue);
        log.info("signValue:" + signValue);
        String result=client.send(mapData, ENCODE);
        Map<String, String> res = null;
        try {
            Gson gson = new Gson();
            res =client.getUrlParams(result);
            if(res.get("respCode").equals("9999")){
            	map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
        		map.put(CommonConstants.RESP_MESSAGE, hexStr2Str(res.get("reserver3")));
            }else{
            	map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
            	map.put("redirect_url", ip+"/v1.0/paymentgateway/topup/ytjf/payreturn");
            	map.put(CommonConstants.RESP_MESSAGE, "发送成功");
    		}
            
        } catch (JsonSyntaxException e) {
        	map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
    		map.put(CommonConstants.RESP_MESSAGE, "支付失败");
        }
		return map;
	}
	/***
	 * 同步不通知
	 * 
	 * ***/
	@RequestMapping(method = RequestMethod.GET, value = "/v1.0/paymentgateway/topup/ytjf/payreturn")
	public String payreturn(HttpServletRequest request, HttpServletResponse response)
			throws IOException {
		return "ytjfpaying";
	}
	
	
	
	
	/**
	 * 交易回调
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/topup/ytjf/payCallback")
	public void payCallback(HttpServletRequest request, HttpServletResponse response, Model model)
			throws IOException {
		log.info("交易回调回来了-----------------");
		Map<String, Object> map = new HashMap<String, Object>();

		String respCode = request.getParameter("respCode");
		String reserver3 = request.getParameter("reserver3");
		String sysTraceNum = request.getParameter("sysTraceNum");//请求的订单号
		String orderId = request.getParameter("orderId");//返回的订单号
		
		PaymentRequestParameter prp = redisUtil.getPaymentRequestParameter(sysTraceNum);
		
		if ("0000".equals(respCode)) {
			log.info("交易成功-----------------" + reserver3);
			log.info("交易订单号：" + sysTraceNum);
			
			RestTemplate restTemplate = new RestTemplate();
			MultiValueMap<String, String> requestEntity = new LinkedMultiValueMap<String, String>();
			String url = null;
			String result = null;
			log.info("*********************交易成功***********************");
			
			url = prp.getIpAddress()+ChannelUtils.getCallBackUrl(prp.getIpAddress());
			//url = prp.getIpAddress() + "/v1.0/transactionclear/payment/update";
			requestEntity = new LinkedMultiValueMap<String, String>();
			requestEntity.add("status", "1");
			requestEntity.add("order_code", sysTraceNum);
			requestEntity.add("third_code", orderId);
			try {
				result = restTemplate.postForObject(url, requestEntity, String.class);
			} catch (Exception e) {
				e.printStackTrace();
				log.error("",e);
			}

			log.info("订单状态修改成功===================" + sysTraceNum + "====================" + result);

			log.info("订单已交易成功!");

			PrintWriter pw = response.getWriter();
			pw.print("success");
			pw.close();
		} else {
			log.info("交易失败-----------------" + reserver3);
			log.info("交易订单号：" + sysTraceNum);
			
			this.addOrderCauseOfFailure(sysTraceNum, "交易失败", prp.getIpAddress());
			PrintWriter pw = response.getWriter();
			pw.print("success");
			pw.close();
		}

	}
	private final static String mHexStr = "0123456789ABCDEF";
	public static String hexStr2Str(String hexStr){
		hexStr = hexStr.toString().trim().replace(" ","").toUpperCase(Locale.US);
		char[] hexs = hexStr.toCharArray();
		byte[] bytes = new byte[hexStr.length() / 2];
		int iTmp = 0x00;;
		for (int i = 0; i < bytes.length; i++){
		iTmp = mHexStr.indexOf(hexs[2 * i]) << 4;
		iTmp |= mHexStr.indexOf(hexs[2 * i + 1]);
		bytes[i] = (byte) (iTmp & 0xFF);
		}
		return new String(bytes);
	}

	public static String SendMessage(Map<String, String> mapData) {

        return
        		 "bussId=" + mapData.get("bussId") +
                 "&cardCvn2=" + mapData.get("cardCvn2") +
                 "&cardExpire=" + mapData.get("cardExpire") +
                 "&certNo=" + mapData.get("certNo") +
                 "&merchantId=" + mapData.get("merchantId") +
                 "&merOrderNum=" + mapData.get("merOrderNum") +
                 "&tranAmt=" + mapData.get("tranAmt") +
                 "&userAcctNo=" + mapData.get("userAcctNo") +
                 "&userId=" + mapData.get("userId") +
                 "&userIp=" + mapData.get("userIp") +
                 "&userName=" + mapData.get("userName") +
                 "&userPhone=" + mapData.get("userPhone") + "&";
    }
	 public static String QuickSign(Map<String, String> mapData) {

	        return
	        		 "bussId=" + mapData.get("bussId") +
	                 "&certNo=" + mapData.get("certNo") +
	                 "&frontUrl=" + mapData.get("frontUrl") +
	                 "&merchantId=" + mapData.get("merchantId") +
	                 "&merOrderNum=" + mapData.get("merOrderNum") +
	                 "&tranAmt=" + mapData.get("tranAmt") +
	                 "&userAcctNo=" + mapData.get("userAcctNo") +
	                 "&userName=" + mapData.get("userName") +
	                 "&userPhone=" + mapData.get("userPhone") + "&";
	    }
	 public static String SynonymNamePay(Map<String, String> mapData) {

	        return 	mapData.get("version") +
	                "|" + mapData.get("transCode") +
	                "|"+ mapData.get("merchantId") +
	                "|"+ mapData.get("merOrderNum") +
	                "|"+ mapData.get("bussId") +
	                "|"+ mapData.get("tranAmt") +
	                "|"+ mapData.get("sysTraceNum") +
	                "|"+ mapData.get("tranDateTime") +
	                "|"+ mapData.get("currencyType") +
	                "|"+ mapData.get("merURL") +
	                "|"+ mapData.get("backURL") +
	                "|"+ mapData.get("orderInfo") +
	                "|"+ mapData.get("userId") ;
	    }
}
