package com.jh.paymentgateway.controller;

import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSON;
import com.jh.paymentgateway.basechannel.BaseChannel;
import com.jh.paymentgateway.business.TopupPayChannelBusiness;
import com.jh.paymentgateway.config.RedisUtil;
import com.jh.paymentgateway.pojo.PaymentRequestParameter;
import com.jh.paymentgateway.pojo.RYTBindCard;
import com.jh.paymentgateway.pojo.RYTProvinceCity;
import com.jh.paymentgateway.pojo.RYTRegister;
import com.jh.paymentgateway.util.ryt.EffersonPayService;
import com.jh.paymentgateway.util.ryt.PlatBase64Utils;
import com.jh.paymentgateway.util.ryt.PlatKeyGenerator;

import cn.jh.common.tools.ResultWrap;
import cn.jh.common.utils.CommonConstants;
import cn.jh.common.utils.ExceptionUtil;
import net.sf.json.JSONObject;



@Controller
@EnableAutoConfiguration
public class RYTpageRequest extends BaseChannel {
	private static final Logger LOG = LoggerFactory.getLogger(RYTpageRequest.class);


	@Autowired
	private RedisUtil redisUtil;

	@Autowired
	private TopupPayChannelBusiness topupPayChannelBusiness;

	@Value("${payment.ipAddress}")
	private String ip;
	
	//平台请求域名
	@Value("${ryt.url}")
	private String rytUrl="http://unionpay.rytpay.com.cn";
	
	//商户编号
	@Value("${ryt.appId}")
	private String appId="4028e4a268309c20016830a13ef10002";
	
	//商户秘钥
	@Value("${ryt.pub_key}")
	private String pub_key="MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQDWm/45OvxOQVVrfPaz7ppIK0RQznsG+J5+DbYAqnqinO8Jhxir8MRn2k3DXfMfYUfreRDFlC/W7KUNkr6b0OVXoV4L+r7awAKm7qA+kwLlV6jKSKWc6Pyqlj/z8kxW0NUbMde7cytCqu0+JFKWHV/El+h0/oExFoG5S509LdatgQIDAQAB";

	public final static String DATE_YYYYMMDDHHMMSS = "yyyyMMddHHmmss";
	
	
	/***
	 * 绑卡请求
	 * 1、注册
	 * 2、开户
	 * 3、授权
	 * **/
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/topup/ryt/bindcard/p")
	public @ResponseBody Object RYTBindCardPost(HttpServletRequest request, HttpServletResponse response,
			@RequestParam(value = "order_code") String orderCode,
			@RequestParam(value = "userName") String userName,
			@RequestParam(value = "idcard") String idcard,
			@RequestParam(value = "rate") String rate,
			@RequestParam(value = "bankNo") String bankNo,
			@RequestParam(value = "phone") String phone){
		Map<String, Object> map =new HashMap<String,Object>();
		Map<String, String> mapbank =new HashMap<String,String>();
		RYTRegister rytRegister= topupPayChannelBusiness.getRYTRegisterByIdcard(idcard);
		
		RYTBindCard rytBindCard = topupPayChannelBusiness.getRYTBindCardByBankCard(bankNo);
		
		if(rytRegister!=null&&rytRegister.getStatus().equals("1")&&rytBindCard!=null&&rytBindCard.getStatus().equals("1")){
			if(!rytRegister.getRate().equals(rate)){
				mapbank=this.CreditRHChangeRate(rytRegister.getMerchantCode(), rate);
				if(mapbank.get("respCode").equals("0000")||mapbank.get("respCode").equals("C00013")){
					rytRegister.setRate(rate);
					rytRegister.setStatus("1");
				}
				topupPayChannelBusiness.createRYTRegister(rytRegister);
			}
			return ResultWrap.init(CommonConstants.SUCCESS, "已完成绑卡");
		}
		 orderCode=new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
		 map=ResultWrap.init("999996", "需要绑卡",ip+"/v1.0/paymentgateway/topup/ryt/bindcard?order_code="+orderCode+
				 "&userName="+userName+"&idcard="+idcard+"&rate="+rate+"&bankNo="+bankNo+"&phone="+phone );
		return map;
	}
	
	
	
	/***
	 * 绑卡请求
	 * 1、注册
	 * 2、开户
	 * 3、授权
	 * **/
	@RequestMapping(method = RequestMethod.GET, value = "/v1.0/paymentgateway/topup/ryt/bindcard")
	public String RYTBindCardGet(HttpServletRequest request, HttpServletResponse response,
			@RequestParam(value = "order_code") String orderCode,
			@RequestParam(value = "userName") String userName,
			@RequestParam(value = "idcard") String idcard,
			@RequestParam(value = "rate") String rate,
			@RequestParam(value = "bankNo") String bankNo,
			@RequestParam(value = "phone") String phone)
			throws Exception {
		Map<String, String> map =new HashMap<String,String>();
		//判定是否注册开户
		RYTRegister rytRegister= topupPayChannelBusiness.getRYTRegisterByIdcard(idcard);
		if(rytRegister==null||rytRegister.getStatus().equals("0")){
			if(rytRegister==null){
				rytRegister=new RYTRegister();
				rytRegister.setCreateTime(new Date());
			}
			rytRegister.setIdCard(idcard);
			rytRegister.setPhone(phone);
			rytRegister.setBankCard(bankNo);
			rytRegister.setRate(rate);
			//发起进件
			map=this.CreditRHRegist(bankNo, phone, userName, idcard);
			if(map.get("respCode").equals("0000")||map.get("respCode").equals("C0006")){
				rytRegister.setMerchantCode(map.get("merchantNo"));
				rytRegister.setStatus("2");
			}else{
				rytRegister.setStatus("0");
			}
			topupPayChannelBusiness.createRYTRegister(rytRegister);
		}
		//判定进行开户
		rytRegister= topupPayChannelBusiness.getRYTRegisterByIdcard(idcard);
		if(rytRegister.getStatus().equals("2")||!rytRegister.getRate().equals(rate)){
			//判定进行开户/修改
			if(rytRegister.getStatus().equals("2")){
				map=this.CreditRHOpenProduct(rytRegister.getMerchantCode(), rate);
			}else{
				map=this.CreditRHChangeRate(rytRegister.getMerchantCode(), rate);
			}
			if(map.get("respCode").equals("0000")||map.get("respCode").equals("C00013")){
				rytRegister.setRate(rate);
				rytRegister.setStatus("1");
			}
			topupPayChannelBusiness.createRYTRegister(rytRegister);
		}
		//绑卡请求
		RYTBindCard rytBindCard = topupPayChannelBusiness.getRYTBindCardByBankCard(bankNo);
		String josnreturn=null;
		if(rytBindCard==null||!rytBindCard.getStatus().equals("1")){
			if(rytBindCard==null){
				rytBindCard=new RYTBindCard();
				rytBindCard.setCreateTime(new Date());
			}
			rytBindCard.setBankCard(bankNo);
			rytBindCard.setIdCard(idcard);
			rytBindCard.setPhone(phone);
			rytBindCard.setMerchantCode(rytRegister.getMerchantCode());
			rytBindCard.setStatus("0");
			topupPayChannelBusiness.createRYTBindCard(rytBindCard);
			josnreturn=this.CreditRHBinding(orderCode, rytRegister.getMerchantCode(), bankNo, phone, userName, idcard, 
					ip+"/v1.0/paymentgateway/topup/ryt/bindcard/callback",
					ip+"/v1.0/paymentgateway/topup/ryt/bindcard/front");
		}
		if(josnreturn.contains("<")){
			response.setContentType("text/html");
			PrintWriter out = response.getWriter();
			out.println(josnreturn);
			out.flush();
			out.close();
		}else {
			JSONObject  jasonObject = JSONObject.fromObject(josnreturn);
			map = (Map)jasonObject;
			if(map.get("respCode").equals("0000")&&map.get("respMsg").equals("已开通无卡快捷支付")){
				rytBindCard = topupPayChannelBusiness.getRYTBindCardByBankCard(bankNo);
				rytBindCard.setStatus("1");
				topupPayChannelBusiness.createRYTBindCard(rytBindCard);
				return "bqdhbindcardsuccess";
			}
		}
		return "bqdhbindcardf";
	}
	
	
	/***
	 * 绑卡请求异步
	 * **/
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/topup/ryt/bindcard/callback")
	public void RYTBindCardCallback(HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		LOG.info("Form4GatewayConsumeT0Callback start-------------------------------------------RYTPAY CALLBACK:\r\n");
		response.setHeader("Content-type", "text/html;charset=UTF-8"); 
        String sign = request.getParameter("signature");
        LOG.info("sign:"+sign);
        String encryptedData = request.getParameter("data");
        LOG.info("encryptedData:"+encryptedData);
        
        String returnMsg = "failed";
        try {
        	//解密报文
			byte[] data = PlatKeyGenerator.decryptByPublicKey(PlatBase64Utils.decode(encryptedData), pub_key);
			//验签
			boolean result = PlatKeyGenerator.verify(data, pub_key, sign);
			if(result){
				returnMsg = "success";
				Map<String,String> resultMap = JSON.parseObject(new String(data), Map.class);
				LOG.info("/v1.0/paymentgateway/topup/ryt/bindcard/callback"+resultMap);
				if(resultMap.get("respCode").equals("0000")){
					String accNo=resultMap.get("accNo");
					String merchantNo=resultMap.get("merchantNo");
					RYTBindCard rytBindCard = topupPayChannelBusiness.getRYTBindCardByBankCard(accNo);
					rytBindCard.setStatus("1");
					rytBindCard.setMerchantCode(merchantNo);
					topupPayChannelBusiness.createRYTBindCard(rytBindCard);
				}
			}else{
				System.out.println("验签失败");
			}
			 LOG.info("Form4GatewayConsumeT0Callback End-------------------------------------------");
		} catch (Exception e) {
			 LOG.info(e.getMessage(), e);
		}
        response.getWriter().write(returnMsg);
	}
	
	/***
	 * 绑卡请求同步
	 * **/
	@RequestMapping(method = RequestMethod.GET, value = "/v1.0/paymentgateway/topup/ryt/bindcard/front")
	public String RYTBindCardFrontGET(HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		 LOG.info("RYTBindCardFrontGET -------------------------------------------");
		return "bqdhbindcardsuccess";
	}
	/***
	 * 绑卡请求同步
	 * **/
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/topup/ryt/bindcard/front")
	public String RYTBindCardFrontPOST(HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		 LOG.info("RYTBindCardFrontPOST -------------------------------------------");
		return "bqdhbindcardsuccess";
	}
	/***
	 * 请求消费
	 * **/
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/topup/ryt/consume")
	public @ResponseBody Object rytConsume(HttpServletRequest request,
			@RequestParam(value = "order_code") String orderCode)
			throws Exception {
		//获取订单数据
		PaymentRequestParameter prp = redisUtil.getPaymentRequestParameter(orderCode);
		Map<String, String> map = new HashMap<String, String>();
		Map<String,Object> mapob=new HashMap<String,Object>(); 
		//用户名称
		String userName=prp.getUserName();
		//身份证号
		String idcard=prp.getIdCard();
		//获取费率 
		String rate=prp.getRate();
		//获取卡号
		String bankNo=prp.getBankCard();
		//手机号
		String phone=prp.getCreditCardPhone();
		//获取省市
		String extra =prp.getExtra();
		String province="上海市";
		String city="上海市";
		//消费计划|福建省-泉州市
		if(extra.contains("|")&&extra.contains("-")){
			String[] pc= extra.split("\\|");
			if(pc.length==2){
				pc=pc[1].split("-");
				if(pc.length==2){
					province=pc[0];
					city=pc[1];
				}
			}
		}
		RYTRegister rytRegister= topupPayChannelBusiness.getRYTRegisterByIdcard(idcard);
		try {
			map=this.CreditRHConsume(orderCode, rytRegister.getMerchantCode(), prp.getAmount(), bankNo, phone, userName, idcard, rate,
					province, city, ip+"/v1.0/paymentgateway/topup/ryt/pay/callback", this.getRemoteIP(request));
			String code = map.get("respCode");
			String message = map.get("respMsg");
			if(code.equals("0000")){
				mapob.put(CommonConstants.RESP_CODE, "999998");
				mapob.put(CommonConstants.RESP_MESSAGE, message);
			}else{
				mapob.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
				mapob.put(CommonConstants.RESP_MESSAGE, message);
				this.addOrderCauseOfFailure(orderCode, message, prp.getIpAddress());
			}
			LOG.info("ryt消费报文"+orderCode+":"+map.toString());
		} catch (Exception e) {
			LOG.error("商户侧消费接口出现异常======", e);
			mapob.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			mapob.put(CommonConstants.RESP_MESSAGE, "消费失败");
		}
		return mapob;
	}
	
	/***
	 * 请求代付
	 * **/
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/topup/ryt/advance")
	public @ResponseBody Object  rytAdvance(HttpServletRequest request, 
			@RequestParam(value = "order_code") String orderCode)
			throws Exception {
		//获取订单数据
		PaymentRequestParameter prp = redisUtil.getPaymentRequestParameter(orderCode);
		Map<String, String> map = new HashMap<String, String>();
		Map<String,Object> mapob=new HashMap<String,Object>(); 
		//用户名称
		String userName=prp.getUserName();
		//身份证号
		String idcard=prp.getIdCard();
		//获取卡号
		String bankNo=prp.getBankCard();
		
		RYTRegister rytRegister= topupPayChannelBusiness.getRYTRegisterByIdcard(idcard);
		
		
		try {
			map=this.CreditRHAdvance(orderCode, rytRegister.getMerchantCode(), prp.getRealAmount(), prp.getExtraFee(), bankNo, prp.getCreditCardBankName(), 
					userName, idcard, ip+"/v1.0/paymentgateway/topup/ryt/pay/callback");
			String code = map.get("respCode");
			String message = map.get("respMsg");
			if(code.equals("0000")){
				mapob.put(CommonConstants.RESP_CODE, "999998");
				mapob.put(CommonConstants.RESP_MESSAGE, message);
			}else{
				mapob.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
				mapob.put(CommonConstants.RESP_MESSAGE, message);
				this.addOrderCauseOfFailure(orderCode, message, prp.getIpAddress());
			}
			LOG.info("ryt代付报文"+orderCode+":"+map.toString());
		} catch (Exception e) {
			LOG.error("商户侧消费接口出现异常======", e);
			mapob.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			mapob.put(CommonConstants.RESP_MESSAGE, "消费失败");
		}
		
		return mapob;
	}
	
	
	/***
	 * 支付异步回调
	 * @throws IOException 
	 * **/
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/topup/ryt/pay/callback")
	public void RYTpayback(HttpServletRequest request, HttpServletResponse response) throws IOException{
		LOG.info("Form4GatewayConsumeT0Callback start-------------------------------------------RYTPAY CALLBACK:\r\n");
		response.setHeader("Content-type", "text/html;charset=UTF-8"); 
        String sign = request.getParameter("signature");
        LOG.info("sign:"+sign);
        String encryptedData = request.getParameter("data");
        LOG.info("encryptedData:"+encryptedData);
        
        String returnMsg = "failed";
        try {
        	//解密报文
			byte[] data = PlatKeyGenerator.decryptByPublicKey(PlatBase64Utils.decode(encryptedData), pub_key);
			//验签
			boolean result = PlatKeyGenerator.verify(data, pub_key, sign);
			if(result){
				returnMsg = "success";
				Map<String,String> resultMap = JSON.parseObject(new String(data), Map.class);
				if(resultMap.get("respCode").equals("0000")){
					String orderNo=resultMap.get("orderNo");
					String respMsg=resultMap.get("respMsg");
					PaymentRequestParameter bean = redisUtil.getPaymentRequestParameter(orderNo);
					if(resultMap.get("planState").equals("SUCCESS")){
						this.updateSuccessPaymentOrder(bean.getIpAddress(), orderNo,"");
					}else{
						this.addOrderCauseOfFailure(orderNo, respMsg, bean.getIpAddress());
					}
				}else{
					returnMsg = "no";
				}
			}else{
				LOG.info("ryt验签失败");
			}
			 LOG.info("Form4GatewayConsumeT0Callback End-------------------------------------------");
		} catch (Exception e) {
			 LOG.info(e.getMessage(), e);
		}
        response.getWriter().write(returnMsg);
		
	}
	
	
	// 消费结果查询
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/topup/ryt/consumeQuery")
	public @ResponseBody Object rytConsumeQuery(@RequestParam(value = "orderCode") String orderCode
			)
			throws Exception {
		Map<String, Object> map =new HashMap<String,Object>();
		Map<String, String> mapquery =new HashMap<String,String>();
		try {
			PaymentRequestParameter prp = redisUtil.getPaymentRequestParameter(orderCode);
			
			if (prp == null) {
				return ResultWrap.init(CommonConstants.FALIED, "请求支付失败");
			}
			//身份证号
			String idcard=prp.getIdCard();
			
			RYTRegister rytRegister= topupPayChannelBusiness.getRYTRegisterByIdcard(idcard);
			mapquery=this.CreditRHQuery( orderCode , rytRegister.getMerchantCode(), "1" );
			if(mapquery.get("respCode").equals("0000")){
				if(mapquery.get("planState").equals("SUCCESS")){
					
					map=ResultWrap.init(CommonConstants.SUCCESS, mapquery.get("respMsg"));
				}else{
					map=ResultWrap.init(CommonConstants.FALIED, mapquery.get("respMsg"));
				}
			}else{
				map=ResultWrap.init(CommonConstants.FALIED, mapquery.get("respMsg"));
			}
			
		} catch (Exception e) {
			LOG.error(ExceptionUtil.errInfo(e));
			map=ResultWrap.init(CommonConstants.FALIED, "查询失败");
		}
		return map;
	}
	
	
	// 代付结果查询
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/topup/ryt/withdrawQuery")
	public @ResponseBody Object rytWithdrawQuery(@RequestParam(value = "orderCode") String orderCode)
			throws Exception {
		Map<String, Object> map =new HashMap<String,Object>();
		Map<String, String> mapquery =new HashMap<String,String>();
		try {
			PaymentRequestParameter prp = redisUtil.getPaymentRequestParameter(orderCode);
			if (prp == null) {
				return ResultWrap.init(CommonConstants.FALIED, "请求支付失败");
			}
			//身份证号
			String idcard=prp.getIdCard();
			
			RYTRegister rytRegister= topupPayChannelBusiness.getRYTRegisterByIdcard(idcard);
			mapquery=this.CreditRHQuery( orderCode , rytRegister.getMerchantCode(), "2" );
			if(mapquery.get("respCode").equals("0000")){
				if(mapquery.get("planState").equals("SUCCESS")){
					map=ResultWrap.init(CommonConstants.SUCCESS, mapquery.get("respMsg"));
				}else{
					map=ResultWrap.init(CommonConstants.FALIED, mapquery.get("respMsg"));
				}
			}else{
				map=ResultWrap.init(CommonConstants.FALIED, mapquery.get("respMsg"));
			}
			
		} catch (Exception e) {
			map=ResultWrap.init(CommonConstants.FALIED, "查询失败");
		}
		return map;
	}
	// 商户余额
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/topup/ryt/creditRHBalanceQuery")
	public @ResponseBody Object CreditRHBalanceQueryPost(@RequestParam(value = "idCard") String idCard)
			throws Exception {
		Map<String, Object> map =new HashMap<String,Object>();
		Map<String, String> mapquery =new HashMap<String,String>();
		try {
			//身份证号
			
			RYTRegister rytRegister= topupPayChannelBusiness.getRYTRegisterByIdcard(idCard);
			mapquery=this.CreditRHBalanceQuery(rytRegister.getMerchantCode());
			if(mapquery.get("respCode").equals("0000")){
				String amount =mapquery.get("balance");
				String canbalance =mapquery.get("canbalance");
				return ResultWrap.init(CommonConstants.SUCCESS, "余额为： " + amount +"可用余额为："+canbalance);
			}else{
				map=ResultWrap.init(CommonConstants.FALIED, mapquery.get("respMsg"));
			}
			
		} catch (Exception e) {
			map=ResultWrap.init(CommonConstants.FALIED, "查询失败");
		}
		return map;
	}
	
	
	
    /*1
     * Ryt平台商户注册
     * /rytpay-business/creditcard/api/rh.do?regist
     * */
	public Map<String, String> CreditRHRegist(String toBankNo ,String toPhoneNo,String realName,String certNo){
		Map<String, String> param = new HashMap<String, String>();
		Map<String, String> map = new HashMap<String, String>();
		param.put("toBankNo", toBankNo);
		param.put("toPhoneNo", toPhoneNo);
		param.put("realName", realName);	
		param.put("certNo", certNo);	
		String jsonString = JSON.toJSONString(param);
		LOG.info("jsonString:" + jsonString);
		String data = "";
		try {
			// 加密
			byte[] encodeData = PlatKeyGenerator.encryptByPublicKey(jsonString.getBytes("UTF-8"), pub_key);
			data = PlatBase64Utils.encode(encodeData);
			// 测试Post
			Map<String, String> params = new HashMap<String, String>();
			params.put("data", data);
			params.put("appId", appId);
			String registUrl=rytUrl+ "/rytpay-business/creditcard/api/rh.do?regist";
			String returnJson = EffersonPayService.postAsString(params, registUrl,
					"UTF-8");
			JSONObject  jasonObject = JSONObject.fromObject(returnJson);
			map = (Map)jasonObject;
			LOG.info("请求返回参数："+returnJson);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return map;
	}
	
	
    /*2
     * Ryt平台商户产品开通
     * /rytpay-business/creditcard/api/rh.do?openproduct
     * */
	public Map<String, String> CreditRHOpenProduct(String merchantNo,String rate ){
		Map<String, String> param = new HashMap<String, String>();
		Map<String, String> map = new HashMap<String, String>();
		
		param.put("merchantNo", merchantNo);
		param.put("rate", rate);	
		String jsonString = JSON.toJSONString(param);
		LOG.info("CreditRHOpenProductjsonString:" + jsonString);
		String data = "";
		try {
			// 加密
			byte[] encodeData = PlatKeyGenerator.encryptByPublicKey(jsonString.getBytes("UTF-8"), pub_key);
			data = PlatBase64Utils.encode(encodeData);
			// 测试Post
			Map<String, String> params = new HashMap<String, String>();

			params.put("data", data);
			params.put("appId", appId);
			String openProductUrl=rytUrl+ "/rytpay-business/creditcard/api/rh.do?openproduct";
			String returnJson = EffersonPayService.postAsString(params, openProductUrl,
					"UTF-8");
			
			LOG.info("请求返回参数："+returnJson);
			JSONObject  jasonObject = JSONObject.fromObject(returnJson);
			map = (Map)jasonObject;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return map;
	}
	/*3
	 * Ryt平台信用卡绑卡开通
	 * /rytpay-business/creditcard/api/rh.do?binding
	 * */
	public String CreditRHBinding(String orderNo,String merchantNo,String accNo,String phoneNo,String realName ,String certNo,String backUrl,String frontUrl){
		Map<String, String> param = new HashMap<String, String>();
		
		Map<String, String> map = new HashMap<String, String>();
		
		param.put("orderNo", orderNo);
		param.put("merchantNo", merchantNo);
		param.put("accNo", accNo);
		param.put("phoneNo", phoneNo);
		param.put("realName", realName);	
		param.put("certNo", certNo);
		param.put("backUrl", backUrl);
		param.put("frontUrl", frontUrl);

		String jsonString = JSON.toJSONString(param);
		LOG.info("CreditRHBindingjsonString:" + jsonString);
		String data = "";
		String returnJson=null;
		try {
			// 加密
			byte[] encodeData = PlatKeyGenerator.encryptByPublicKey(jsonString.getBytes("UTF-8"), pub_key);
			data = PlatBase64Utils.encode(encodeData);
			// 测试Post
			Map<String, String> params = new HashMap<String, String>();

			params.put("data", data);
			params.put("appId", appId);
			String CreditRHBindingUrl=rytUrl+"/rytpay-business/creditcard/api/rh.do?binding";
			returnJson = EffersonPayService.postAsString(params, CreditRHBindingUrl,
					"UTF-8");
			LOG.info("请求返回参数："+returnJson);
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return returnJson;
		
	}
	
	/*4
	 * Ryt平台信用卡消费计划
	 * /rytpay-business/creditcard/api/rh.do?plan/consume
	 * */
	public Map<String, String> CreditRHConsume(String orderNo ,String merchantNo,String txnAmt,String accNo,String phoneNo,String realName,String certNo,
			String rate,String province,String city,String backUrl,String orderIp){
		
		Map<String, String> param = new HashMap<String, String>();
		Map<String, String> map = new HashMap<String, String>();
		param.put("orderNo", orderNo);
		param.put("merchantNo", merchantNo);
		param.put("txnAmt", txnAmt);
		param.put("accNo", accNo);
		param.put("phoneNo", phoneNo);
		param.put("realName", realName);	
		param.put("certNo", certNo);	
		param.put("rate", rate);
		param.put("province", province);
		param.put("city", city);
		param.put("orderIp", orderIp);
		param.put("backUrl", backUrl);
		String jsonString = JSON.toJSONString(param);
		LOG.info("CreditRHConsumejsonString:" + jsonString);
		String data = "";
		try {
			// 加密
			byte[] encodeData = PlatKeyGenerator.encryptByPublicKey(jsonString.getBytes("UTF-8"), pub_key);
			data = PlatBase64Utils.encode(encodeData);
			// 测试Post
			Map<String, String> params = new HashMap<String, String>();

			params.put("data", data);
			params.put("appId", appId);
			String CreditRHConsumeUrl=rytUrl+"/rytpay-business/creditcard/api/rh.do?plan/consume";
			
			String returnJson = EffersonPayService.postAsString(params, CreditRHConsumeUrl,
					"UTF-8");
			LOG.info("请求返回参数："+returnJson);
			JSONObject  jasonObject = JSONObject.fromObject(returnJson);
			map = (Map)jasonObject;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return map;
		
	}
	
    /*5
     * Ryt平台信用卡代付计划
     * /rytpay-business/creditcard/api/rh.do?plan/advance
     * */
	public Map<String, String>  CreditRHAdvance(String orderNo,String merchantNo,String txnAmt ,String advanceFee ,String bankNo,String bankName,String realName,String certNo,String backUrl){
		
		Map<String, String> param = new HashMap<String, String>();
		Map<String, String> map = new HashMap<String, String>();
		advanceFee= new BigDecimal(advanceFee).subtract(new BigDecimal("0.5")).setScale(2, BigDecimal.ROUND_DOWN).toString();
		param.put("orderNo", orderNo);
		param.put("merchantNo", merchantNo);
		param.put("txnAmt", txnAmt);
		param.put("advanceFee", advanceFee);
		param.put("bankNo", bankNo);
		param.put("bankName", bankName);
		param.put("realName", realName);	
		param.put("certNo", certNo);	
		param.put("backUrl", backUrl);

		String jsonString = JSON.toJSONString(param);
		LOG.info("CreditRHAdvancejsonString:" + jsonString);
		String data = "";
		try {
			// 加密
			byte[] encodeData = PlatKeyGenerator.encryptByPublicKey(jsonString.getBytes("UTF-8"), pub_key);
			data = PlatBase64Utils.encode(encodeData);
			// 测试Post
			Map<String, String> params = new HashMap<String, String>();

			params.put("data", data);
			params.put("appId", appId);
			String CreditRHAdvanceUrl=rytUrl+"/rytpay-business/creditcard/api/rh.do?plan/advance";
			String returnJson = EffersonPayService.postAsString(params, CreditRHAdvanceUrl,
					"UTF-8");
			LOG.info("请求返回参数："+returnJson);
			JSONObject  jasonObject = JSONObject.fromObject(returnJson);
			map = (Map)jasonObject;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return map;
	}
    /*6
     * Ryt平台计划查询
     * /rytpay-business/creditcard/api/rh.do?plan/query
     * */
	public Map<String, String> CreditRHQuery(String orderNo ,String merchantNo,String queryType ){
		
		Map<String, String> param = new HashMap<String, String>();
		Map<String, String> map = new HashMap<String, String>();
		param.put("orderNo", orderNo);
		param.put("merchantNo", merchantNo);
		param.put("queryType", queryType);
		

		String jsonString = JSON.toJSONString(param);
		LOG.info("CreditRHQueryjsonString:" + jsonString);
		String data = "";
		try {
			// 加密
			byte[] encodeData = PlatKeyGenerator.encryptByPublicKey(jsonString.getBytes("UTF-8"), pub_key);
			data = PlatBase64Utils.encode(encodeData);
			// 测试Post
			Map<String, String> params = new HashMap<String, String>();

			params.put("data", data);
			params.put("appId", appId);
			String CreditRHQueryUrl =rytUrl+"/rytpay-business/creditcard/api/rh.do?plan/query";
			String returnJson = EffersonPayService.postAsString(params, CreditRHQueryUrl,
					"UTF-8");
			LOG.info("请求返回参数："+returnJson);
			JSONObject  jasonObject = JSONObject.fromObject(returnJson);
			map = (Map)jasonObject;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return map;
		
	}
	
    /*7
     * Ryt平台商户余额查询
     * /rytpay-business/creditcard/api/rh.do?balance/query
     * */
	public Map<String, String> CreditRHBalanceQuery(String merchantNo ){
		
		Map<String, String> param = new HashMap<String, String>();
		
		Map<String, String> map = new HashMap<String, String>();
			
		param.put("merchantNo", merchantNo);	

		String jsonString = JSON.toJSONString(param);
		LOG.info("CreditRHBalanceQueryjsonString:" + jsonString);
		String data = "";
		try {
			// 加密
			byte[] encodeData = PlatKeyGenerator.encryptByPublicKey(jsonString.getBytes("UTF-8"), pub_key);
			data = PlatBase64Utils.encode(encodeData);
			// 测试Post
			Map<String, String> params = new HashMap<String, String>();

			params.put("data", data);
			params.put("appId", appId);
			String CreditRHBalanceQueryUrl=rytUrl+"/rytpay-business/creditcard/api/rh.do?balance/query";
			String returnJson = EffersonPayService.postAsString(params,CreditRHBalanceQueryUrl,
					"UTF-8");
			JSONObject  jasonObject = JSONObject.fromObject(returnJson);
			map = (Map)jasonObject;
			LOG.info("请求返回参数："+returnJson);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return map;
	}
	
    /*8
     * Ryt平台商户费率修改
     * /rytpay-business/creditcard/api/rh.do?change/rate
     * */
	public Map<String, String> CreditRHChangeRate(String merchantNo,String rate){
		
		Map<String, String> param = new HashMap<String, String>();
		Map<String, String> map = new HashMap<String, String>();
		
		param.put("merchantNo", merchantNo);
		param.put("rate", rate);	

		String jsonString = JSON.toJSONString(param);
		LOG.info("CreditRHChangeRatejsonString:" + jsonString);
		String data = "";
		try {
			// 加密
			byte[] encodeData = PlatKeyGenerator.encryptByPublicKey(jsonString.getBytes("UTF-8"), pub_key);
			data = PlatBase64Utils.encode(encodeData);
			// 测试Post
			Map<String, String> params = new HashMap<String, String>();

			params.put("data", data);
			params.put("appId", appId);
			String CreditRHChangeRateUrl=rytUrl+"/rytpay-business/creditcard/api/rh.do?change/rate";
			String returnJson = EffersonPayService.postAsString(params, CreditRHChangeRateUrl,
					"UTF-8");
			LOG.info("请求返回参数："+returnJson);
			JSONObject  jasonObject = JSONObject.fromObject(returnJson);
			map = (Map)jasonObject;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return map;
	}
	
	/*9
	 * Ryt平台商户银行卡解绑
	 * /rytpay-business/creditcard/api/rh.do?unbinding
	 * */
	public Map<String,String> CreditRHunbind(String merchantNo,String accNo){
		
		Map<String, String> param = new HashMap<String, String>();
		Map<String, String> map = new HashMap<String, String>();
		
		param.put("merchantNo", merchantNo);
		param.put("accNo", accNo);	

		String jsonString = JSON.toJSONString(param);
		LOG.info("CreditRHunbindjsonString:" + jsonString);
		String data = "";
		try {
			// 加密
			byte[] encodeData = PlatKeyGenerator.encryptByPublicKey(jsonString.getBytes("UTF-8"), pub_key);
			data = PlatBase64Utils.encode(encodeData);
			// 测试Post
			Map<String, String> params = new HashMap<String, String>();
			params.put("data", data);
			params.put("appId", appId);
			String CreditRHunbindUrl=rytUrl+"/rytpay-business/creditcard/api/rh.do?unbinding";
			String returnJson = EffersonPayService.postAsString(params, CreditRHunbindUrl,
					"UTF-8");
			LOG.info("请求返回参数："+returnJson);
			JSONObject  jasonObject = JSONObject.fromObject(returnJson);
			map = (Map)jasonObject;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return map;
	}
	
	/**
	 * 省市表
	 * 省
	 * **/
	// 代付结果查询
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/topup/ryt/getprovince")
	public @ResponseBody Object rytProvinceCityByprovince(HttpServletRequest request){
		Map<String,Object> map =new HashMap<String,Object>();
		try {
			map=ResultWrap.init(CommonConstants.SUCCESS, "查询成功",topupPayChannelBusiness.getRYTProvinceCityByprovince());
		} catch (Exception e) {
			map=ResultWrap.init(CommonConstants.FALIED, "查询失败");
		}
		return map;
	}
	/**
	 * 省市表
	 * 市
	 * **/
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/topup/ryt/getcity")
	public  @ResponseBody  Object rytProvinceCityGroupByCity(HttpServletRequest request,
			@RequestParam(value = "province") String province){
		Map<String,Object> map =new HashMap<String,Object>();
		try {
			map=ResultWrap.init(CommonConstants.SUCCESS, "查询成功",topupPayChannelBusiness.getRYTProvinceCityGroupByCity(province));
		} catch (Exception e) {
			map=ResultWrap.init(CommonConstants.FALIED, "查询失败");
		}
		return map;
	}
	
	/**
	 * 获取ip
	 * **/
	public String getRemoteIP(HttpServletRequest request) {  
		String ipAddress = request.getHeader("x-forwarded-for");  
        if (ipAddress == null || ipAddress.length() == 0 || "unknown".equalsIgnoreCase(ipAddress)) {  
            ipAddress = request.getHeader("Proxy-Client-ipAddress");  
        }  
        if (ipAddress == null || ipAddress.length() == 0 || "unknown".equalsIgnoreCase(ipAddress)) {  
            ipAddress = request.getHeader("WL-Proxy-Client-ipAddress");  
        }  
        if (ipAddress == null || ipAddress.length() == 0 || "unknown".equalsIgnoreCase(ipAddress)) {  
            ipAddress = request.getHeader("HTTP_CLIENT_IP");  
        }  
        if (ipAddress == null || ipAddress.length() == 0 || "unknown".equalsIgnoreCase(ipAddress)) {  
            ipAddress = request.getHeader("HTTP_X_FORWARDED_FOR");  
        }  
        if (ipAddress == null || ipAddress.length() == 0 || "unknown".equalsIgnoreCase(ipAddress)) {  
            ipAddress = request.getRemoteAddr();  
        }
        String[] strs = ipAddress.split(",");
        if (strs.length > 0) {
        	ipAddress = strs[0];
		}
        return ipAddress.trim();  
    }  
	
}
