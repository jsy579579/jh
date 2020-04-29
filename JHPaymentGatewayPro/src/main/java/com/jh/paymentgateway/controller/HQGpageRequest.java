package com.jh.paymentgateway.controller;

import java.io.PrintWriter;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.annotate.JsonSerialize.Inclusion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.jh.paymentgateway.basechannel.BaseChannel;
import com.jh.paymentgateway.business.TopupPayChannelBusiness;
import com.jh.paymentgateway.config.RedisUtil;
import com.jh.paymentgateway.pojo.HQERegion;
import com.jh.paymentgateway.pojo.HQGBindCard;
import com.jh.paymentgateway.pojo.HQGRegister;
import com.jh.paymentgateway.pojo.PaymentRequestParameter;
import com.jh.paymentgateway.util.hqg.CommonBean;
import com.jh.paymentgateway.util.hqg.CommonUtil;
import com.jh.paymentgateway.util.hqg.HQGMcc;
import com.jh.paymentgateway.util.hqg.JsonUtils;
import com.jh.paymentgateway.util.hqg.SignUtil;
import com.jh.paymentgateway.util.hqg.TransUtil;

import cn.jh.common.tools.ResultWrap;
import cn.jh.common.utils.CommonConstants;
import cn.jh.common.utils.ExceptionUtil;
import net.sf.json.JSONObject;


@Controller
@EnableAutoConfiguration
public class HQGpageRequest  extends BaseChannel {
	private static final Logger LOG = LoggerFactory.getLogger(HQGpageRequest.class);
	static ObjectMapper mapper = new ObjectMapper();

	static {
		mapper.configure(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		mapper.setSerializationInclusion(Inclusion.NON_NULL);
	}

	@Autowired
	private RedisUtil redisUtil;

	@Autowired
	private TopupPayChannelBusiness topupPayChannelBusiness;

	@Value("${payment.ipAddress}")
	private String ip;

	public final static String DATE_YYYYMMDDHHMMSS = "yyyyMMddHHmmss";
	private static String merchno = "sl2018080218221";
	private static String dskey = "043b1eaa";
	private static String url = "http://pay.huanqiuhuiju.com/authsys/api/hq/pay/execute.do";
	private static String url1 = "http://pay.huanqiuhuiju.com/authsys/api/auth/execute.do";

	
	// 跟还款对接的接口
		@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/topup/hqg/torepayment")
		public @ResponseBody Object HLJCRegister(HttpServletRequest request,HttpServletResponse response,
				@RequestParam(value = "bankCard") String bankCard,
				@RequestParam(value = "idCard") String idCard,
				@RequestParam(value = "phone") String phone, 
				@RequestParam(value = "userName") String userName,
				@RequestParam(value = "cardType") String cardType,
				@RequestParam(value = "rate") String rate,
				@RequestParam(value = "extraFee") String extraFee,
				@RequestParam(value = "bankName") String bankName) throws Exception {

			Map<String, Object> map = new HashMap<String, Object>();

			HQGRegister hqRegister = topupPayChannelBusiness.getHQGRegisterByIdCard(idCard);

			HQGBindCard hqBindCard = topupPayChannelBusiness.getHQGBindCardByBankCard(bankCard);
			
			if (hqRegister == null||!hqRegister.getStatus().equals("1")) {
				return ResultWrap.init("999996", "需要开户",ip+"/v1.0/paymentgateway/topup/hqg/registetpage?bankCard="+bankCard+
						 "&idCard="+idCard+"&phone="+phone+"&userName="+userName+"&rate="+rate+"&bankName="+bankName+"&cardType="+cardType+"&extraFee="+extraFee );

			} else {
				if (hqBindCard == null || !"1".equals(hqBindCard.getStatus())) {
					map=ResultWrap.init("999996", "需要绑卡",ip+ "/v1.0/paymentgateway/topup/hqg/bindCard?bankCard="+bankCard
								+"&idCard="+idCard+"&phone="+phone+"&userName="+userName );
					return map;
				}else{
					map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
					map.put(CommonConstants.RESP_MESSAGE, "已完成绑卡");
					return map;
				}
			}
		}
		
	// 跳转到开户页面
	@RequestMapping(method = RequestMethod.GET, value = "/v1.0/paymentgateway/topup/hqg/registetpage")
	public String  	HQGRegisterpage(HttpServletRequest request, HttpServletResponse response,
			@RequestParam(value = "bankCard") String bankCard,
			@RequestParam(value = "idCard") String idCard,
			@RequestParam(value = "phone") String phone, 
			@RequestParam(value = "userName") String userName,
			@RequestParam(value = "cardType") String cardType,
			@RequestParam(value = "rate") String rate,
			@RequestParam(value = "extraFee") String extraFee,
			@RequestParam(value = "bankName") String bankName,
			Model model){
		model.addAttribute("ip", ip);
		model.addAttribute("bankCard", bankCard);
		model.addAttribute("cardType", cardType);
		model.addAttribute("idCard", idCard);
		model.addAttribute("phone", phone);
		model.addAttribute("userName", userName);
		model.addAttribute("rate", rate);
		model.addAttribute("extraFee",extraFee);
		model.addAttribute("bankName", bankName);
		return "hqgregistet";
	}
	
	//开户接口
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/topup/hqg/registet")
	public  @ResponseBody Object  	HQGRegister(HttpServletRequest request, HttpServletResponse response,
			@RequestParam(value = "bankCard") String bankCard,
			@RequestParam(value = "idCard") String idCard,
			@RequestParam(value = "phone") String phone, 
			@RequestParam(value = "userName") String userName,
			@RequestParam(value = "bankName") String bankName,
			@RequestParam(value = "rate") String rate,
			@RequestParam(value = "extraFee") String extraFee,
			@RequestParam(value = "cityName") String cityName,
			@RequestParam(value = "cityCode") String cityCode){
		Map<String ,String > register=new HashMap<String,String>();
		Map<String ,Object > returnMap=new HashMap<String,Object>();
		HQGRegister hqRegister = topupPayChannelBusiness.getHQGRegisterByIdCard(idCard);
		HQGBindCard hqBindCard = topupPayChannelBusiness.getHQGBindCardByBankCard(bankCard);
		if(hqRegister==null||!hqRegister.getStatus().equals("1")){
			register =HQGRegister(bankCard, userName, idCard, phone, cityName, cityCode, rate, extraFee);
			String returncode=register.get("returncode");
			if(returncode.equals("0000")){
				if(hqRegister==null){
					hqRegister=new HQGRegister();
					hqRegister.setCreateTime(new Date());
				}
				hqRegister.setBankCard(bankCard);
				hqRegister.setCityCode(cityCode);
				hqRegister.setCityName(cityName);
				hqRegister.setIdCard(idCard);
				hqRegister.setPhone(phone);
				hqRegister.setRate(rate);
				hqRegister.setExtraFee(extraFee);
				hqRegister.setMerchantCode(register.get("subMerchantNo"));
				hqRegister.setStatus("1");
				topupPayChannelBusiness.createHQGRegister(hqRegister);
			}else{
				if(hqRegister==null){
					hqRegister=new HQGRegister();
					hqRegister.setCreateTime(new Date());
				}
				hqRegister.setBankCard(bankCard);
				hqRegister.setCityCode(cityCode);
				hqRegister.setCityName(cityName);
				hqRegister.setIdCard(idCard);
				hqRegister.setPhone(phone);
				hqRegister.setRate(rate);
				hqRegister.setExtraFee(extraFee);
				hqRegister.setMerchantCode(register.get("subMerchantNo"));
				hqRegister.setStatus("0");
				returnMap=ResultWrap.init(CommonConstants.FALIED,register.get("errtext"));
			}
		}
		hqRegister = topupPayChannelBusiness.getHQGRegisterByIdCard(idCard);
		if(hqRegister!=null &&hqRegister.getStatus().equals("1")){
			if(hqBindCard==null||!hqBindCard.getStatus().equals("1")){
				returnMap=ResultWrap.init(CommonConstants.SUCCESS,"请求成功" ,
						ip+ "/v1.0/paymentgateway/topup/hqg/bindCard?bankCard="+bankCard+"&idCard="+idCard+"&phone="+phone+"&userName="+userName )   ;
			}
		}else{
			returnMap=ResultWrap.init(CommonConstants.FALIED,"开户失败");
		}
		return returnMap;
	}
	
	//修改费率
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/topup/hqg/registetpage")
	public  Object HQGUpdatePost( 
			@RequestParam(value = "idCard") String idCard,
			@RequestParam(value = "rate") String rate,
			@RequestParam(value = "extraFee") String extraFee
			){
		Map<String ,Object > returnMap=new HashMap<String,Object>();
		HQGRegister hqRegister = topupPayChannelBusiness.getHQGRegisterByIdCard(idCard);
		
		if(hqRegister!=null&&hqRegister.getStatus().equals("1")){
			Map<String,String> hqgUpdate=HQGUpdate(hqRegister.getMerchantCode(), rate, extraFee);
			String returncode=hqgUpdate.get("returncode");
			if(returncode.equals("0000")){
				hqRegister.setExtraFee(extraFee);
				hqRegister.setRate(rate);
				topupPayChannelBusiness.createHQGRegister(hqRegister);
				returnMap=ResultWrap.init(CommonConstants.SUCCESS,"修改成功");
			}else{
				returnMap=ResultWrap.init(CommonConstants.SUCCESS,"修改失败");
			}
		}
		return returnMap;
	}
	
	//绑卡接口
	@RequestMapping(method = RequestMethod.GET, value = "/v1.0/paymentgateway/topup/hqg/bindCard")
	public String HQGbindCard(HttpServletRequest request, HttpServletResponse response,
			@RequestParam(value = "bankCard") String bankCard,
			@RequestParam(value = "idCard") String idCard,
			@RequestParam(value = "phone") String phone, 
			@RequestParam(value = "userName") String userName,
			Model model){
		Map<String,Object> returnMap=new HashMap<String,Object>();
		String userIP=getRemoteIP(request);
		HQGRegister hqRegister = topupPayChannelBusiness.getHQGRegisterByIdCard(idCard);
		HQGBindCard hqBindCard = topupPayChannelBusiness.getHQGBindCardByBankCard(bankCard);
		String merchantOrder= UUID.randomUUID().toString().replaceAll("-", "");
		try {
			Map<String,String> bindCard=HQGbindCard(merchantOrder,hqRegister.getMerchantCode(), bankCard, userName, idCard, phone, userIP, 
					ip+"/v1.0/paymentgateway/topup/hqg/bindcard/front", ip+"/v1.0/paymentgateway/topup/hqg/opencard/notifyurl");
			String returncode=bindCard.get("returncode");
			if(returncode.equals("0000")){
				if(hqBindCard==null){
					hqBindCard=new HQGBindCard();
					hqBindCard.setCreateTime(new Date());
				}
				hqBindCard.setBankCard(bankCard);
				hqBindCard.setMerchantOrder(merchantOrder);
				hqBindCard.setIdCard(idCard);
				hqBindCard.setPhone(phone);
				hqBindCard.setStatus("0");
				topupPayChannelBusiness.createHQGBindCard(hqBindCard);
				String bindUrl=bindCard.get("bindUrl");
				response.setContentType("text/html");
				PrintWriter out = response.getWriter();
				out.println(bindUrl);
				out.flush();
				out.close();
			}else{
				returnMap=ResultWrap.init(CommonConstants.FALIED, bindCard.get("errtext"));
				return "bqdhbindcardf";
			}
		} catch (Exception e) {
		}
		
		return "bqdhbindcardf";
	}

	//消费接口
	@RequestMapping(method = RequestMethod.GET, value = "/v1.0/paymentgateway/topup/hqg/fastpay")
	public @ResponseBody Object hqFastPay(HttpServletRequest request,@RequestParam(value = "orderCode") String orderCode) {
		Map<String, Object> maps = new HashMap<String, Object>();
		CommonBean trans = new CommonBean();
		PaymentRequestParameter prp = redisUtil.getPaymentRequestParameter(orderCode);
		String amount = prp.getAmount();
		String idCard = prp.getIdCard();
		String bankCard = prp.getBankCard();
		String extra = prp.getExtra(); //消费计划|福建省-泉州市
		//String cityName = extra.substring(extra.indexOf("-") + 1, extra.lastIndexOf("-"));
		String cityName = extra.substring(extra.indexOf("-") + 1);
		LOG.info("=======================================消费城市：" + cityName);
		String provinceCode = null;
		try {
			List<HQERegion> hqe = topupPayChannelBusiness.getHQERegionByParentName(cityName);
			provinceCode = hqe.get(0).getRegionCode();
			LOG.info("=======================================HQG消费城市编码：" + provinceCode);
		} catch (Exception e) {
			e.printStackTrace();
			LOG.info("=======================================消费城市：" + cityName + "未匹配");
		}
		
		HQGRegister hqRegister = topupPayChannelBusiness.getHQGRegisterByIdCard(idCard);
		
		//行业
		String mcc=mccget();
		if(extra.contains("|")&&extra.contains("-")){
			String[] pc= extra.split("\\|");
			if(pc.length==2){
				pc=pc[1].split("-");
				if(pc.length==2){
					mcc=pc[1];
				}
			}
		}
		String userIP=getRemoteIP(request);
		String deviceId= getMACAddress(userIP);
		if(deviceId.length()==0){
			deviceId=userIP;
		}
		Map<String,String> hqgPay=HQGPay(orderCode,hqRegister.getMerchantCode(), amount, bankCard, deviceId, userIP, mcc, provinceCode,ip + "/v1.0/paymentgateway/topup/hqg/fastpay/notifyurl");
		String returnCode = hqgPay.get("returncode");
		String errtext = hqgPay.get("errtext");
		if("0000".equals(returnCode) || "0002".equals(returnCode) || "0003".equals(returnCode) ) {
			return ResultWrap.init("999998", "等待银行扣款中");
		}else {
			return ResultWrap.init(CommonConstants.FALIED, errtext);
		}
	}
	public  String mccget(){
		Random ra =new Random();
		int num=ra.nextInt(5)+1;
		String mcc="0001";
		switch(num){
			case 1:
				mcc="0001";
			    break;
			case 2:
				mcc="0002";
			    break;
			case 3:
				mcc="0003";
			    break;
			case 4:
				mcc="0004";
				break;
			case 5:
				mcc="0005";
				break;
				
			default:
				mcc="0003";
			    break;
		}
		return mcc;
	}

	
	//代付接口
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/topup/hqg/transfer")
	public @ResponseBody Object transfer(HttpServletRequest request,@RequestParam(value = "orderCode") String orderCode) {
		PaymentRequestParameter prp = redisUtil.getPaymentRequestParameter(orderCode);
		String realAmount = prp.getRealAmount();
		String bankCard = prp.getBankCard();
		String bankName = prp.getCreditCardBankName();
		String idCard = prp.getIdCard();
		String extra = prp.getExtra();
		String handleType="Y";
		if(extra.equals("T1")){
			handleType="N";
		}
		HQGRegister hqRegister = topupPayChannelBusiness.getHQGRegisterByIdCard(idCard);
		Map<String ,String>  withDraw=HQGWithDraw(orderCode,hqRegister.getMerchantCode(), realAmount, bankCard, bankName,handleType);
		
		String returnCode = withDraw.get("returncode");
		
		String errtext = withDraw.get("errtext");
		
		if("0000".equals(returnCode) || "0003".equals(returnCode)) {
			
			return ResultWrap.init("999998", "等待银行出款中");
		}else {
			return ResultWrap.init(CommonConstants.FALIED, errtext);
		}
		

	}

	
	//余额查询接口
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/topup/hqg/balancequery")
	public @ResponseBody Object balanceQuery(
			@RequestParam(value = "idCard") String idCard
			) throws Exception {
		HQGRegister hqRegister = topupPayChannelBusiness.getHQGRegisterByIdCard(idCard);
		Map<String ,String > balance= HQGQueryBalance(hqRegister.getMerchantCode());
		String returncode=balance.get("returncode");
		if("0000".equals(returncode)) {
			String balanceAmount=balance.get("balanceAmount");
			String d0Balance=balance.get("d0Balance");
			String t1Balance=balance.get("t1Balance");
			return ResultWrap.init(CommonConstants.SUCCESS, "商户余额为： " + balanceAmount+"----Do商户余额："+d0Balance+"----T1商户余额："+t1Balance);
		}else {
			return ResultWrap.init(CommonConstants.FALIED, "查询失败");
		}
	}
	
	//订单查询接口
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/topup/hqg/orderquery")
	public @ResponseBody Object HQGQueryOrderPost(
			@RequestParam(value = "orderCode") String orderCode,
			@RequestParam(value = "orderType") String orderType
			) throws Exception {
		String type=orderType;
		// 105: 交易;106: 代付
		String transtype="105";
		if(type.trim().equals("11")){
			transtype="106";
		}
		
		Map<String,String> queryOrder=HQGQueryOrder(orderCode, transtype);
		String status= queryOrder.get("status");
		String orderNo=queryOrder.get("dsorderid");
		String respMsg=queryOrder.get("message");
		PaymentRequestParameter prp =null;
		try {
			prp = redisUtil.getPaymentRequestParameter(orderCode);
		} catch (Exception e) {
			LOG.error(ExceptionUtil.errInfo(e));
		}
		
		if (prp == null) {
			return ResultWrap.init(CommonConstants.FALIED, "请求支付失败");
		}
		
		if("00".equals(status)) {
			String orderid=queryOrder.get("orderid");
			if(type.trim().equals("11")&&prp!=null){
				this.updateSuccessPaymentOrder(prp.getIpAddress(), orderNo,orderid);
			}
			return ResultWrap.init(CommonConstants.SUCCESS, respMsg);
		}else if(status.equals("02")||status.equals("04")) {
			if(type.trim().equals("11")&&prp!=null){
				this.addOrderCauseOfFailure(orderNo, respMsg, prp.getIpAddress());
			}
			return ResultWrap.init(CommonConstants.FALIED, respMsg);
		}else {
			return ResultWrap.init("999998", respMsg);
		}
		
	}
	
	//行业查询接口
	/*0001超市加油类
	0002服装百货类
	0003酒店餐饮类
	0004珠宝娱乐类
	0005汽车消费类*/
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/topup/hqg/getmcc")
	public @ResponseBody  Object getmcc(){
		Map<String,String> mccMap  =new HashMap<String,String>();
		mccMap.put("0001", "超市加油类");
		mccMap.put("0002", "服装百货类");
		mccMap.put("0003", "酒店餐饮类");
		mccMap.put("0004", "珠宝娱乐类");
		mccMap.put("0005", "汽车消费类");
		Set<String> keySet = mccMap.keySet();
		List<HQGMcc> mccs=new ArrayList<>();
		for (String key : keySet) {
			String strings = mccMap.get(key);
			HQGMcc mcc =new HQGMcc();
			mcc.setMccCode(key);
			mcc.setMccName(strings);
			mccs.add(mcc);
		}
		return ResultWrap.init(CommonConstants.SUCCESS, "查询成功",mccs);
	}
	
	
	/***
	 * 绑卡请求同步
	 * **/
	@RequestMapping(method ={ RequestMethod.POST,
			RequestMethod.GET }, value = "/v1.0/paymentgateway/topup/hqg/bindcard/front")
	public String BindCardFrontGET(HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		 LOG.info("RYTBindCardFrontGET -------------------------------------------");
		return "bqdhbindcardsuccess";
	}
	
	//同名卡开卡异步通知
	@RequestMapping(method = { RequestMethod.POST,
			RequestMethod.GET }, value = "/v1.0/paymentgateway/topup/hqg/opencard/notifyurl")
	public void OpenCardBack(HttpServletRequest request, HttpServletResponse response) throws Exception {

		LOG.info("同名卡开卡异步回调进来了======");
		
		Map<String, String[]> parameterMap = request.getParameterMap();
		Set<String> keySet = parameterMap.keySet();
		for (String key : keySet) {
			String[] strings = parameterMap.get(key);
			for (String s : strings) {
				LOG.info(key + "=============" + s);
			}
		}
		
		String status = request.getParameter("status");
		String dsorderid = request.getParameter("dsorderid");
		HQGBindCard hqBindCard = topupPayChannelBusiness.getHQGBindCardbyMerchantOrder(dsorderid);
		if (status.equals("00")) {
			hqBindCard.setStatus("1");
			topupPayChannelBusiness.createHQGBindCard(hqBindCard);
			LOG.info("同名卡开卡成功");
		}else if(status.equals("02")){
			hqBindCard.setStatus("0");
			topupPayChannelBusiness.createHQGBindCard(hqBindCard);
			LOG.info("同名卡开卡失败");
		}
		PrintWriter pw = response.getWriter();
		pw.print("success");
		pw.close();

	}
	
	//同名卡交易异步通知
	@RequestMapping(method = { RequestMethod.POST,
			RequestMethod.GET }, value = "/v1.0/paymentgateway/topup/hqg/fastpay/notifyurl")
	public void tradeBack(HttpServletRequest request, HttpServletResponse response) throws Exception {

		LOG.info("快捷支付异步回调进来了======");
		
		Map<String, String[]> parameterMap = request.getParameterMap();
		Set<String> keySet = parameterMap.keySet();
		for (String key : keySet) {
			String[] strings = parameterMap.get(key);
			for (String s : strings) {
				LOG.info(key + "=============" + s);
			}
		}
		
		String dsorderid = request.getParameter("dsorderid");
		String orderid = request.getParameter("orderid");
		String status = request.getParameter("status");
		String message = request.getParameter("message");
		
		PaymentRequestParameter prp = redisUtil.getPaymentRequestParameter(dsorderid);
		
		if("00".equals(status)) {
			this.updateSuccessPaymentOrder(prp.getIpAddress(), dsorderid,orderid);
		}else {
			this.addOrderCauseOfFailure(dsorderid, message, prp.getIpAddress());
		}
		PrintWriter pw = response.getWriter();
		pw.print("success");
		pw.close();
	}

	/**
	 * 省市表
	 * 省
	 * **/
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/topup/hqg/getprovince")
	public @ResponseBody Object ProvinceCityByprovince(HttpServletRequest request){
		Map<String,Object> map =new HashMap<String,Object>();
		try {
			map=ResultWrap.init(CommonConstants.SUCCESS, "查询成功",topupPayChannelBusiness.getHQGProvinceCityByHkProvinceCode());
		} catch (Exception e) {
			map=ResultWrap.init(CommonConstants.FALIED, "查询失败");
		}
		return map;
	}
	/**
	 * 省市表
	 * 市
	 * **/
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/topup/hqg/getcity")
	public  @ResponseBody  Object ProvinceCityGroupByCity(HttpServletRequest request,
			@RequestParam(value = "province") String province){
		Map<String,Object> map =new HashMap<String,Object>();
		String cityCode=province.substring(0,2);
		try {
			map=ResultWrap.init(CommonConstants.SUCCESS, "查询成功",topupPayChannelBusiness.getHQGProvinceCityGroupByCity(cityCode));
		} catch (Exception e) {
			map=ResultWrap.init(CommonConstants.FALIED, "查询失败");
		}
		return map;
	}

	//商户入驻register
	public Map<String, String> HQGRegister(String bankcard,String username,String idcard,String mobile,
			String cityName,String cityCode,String rate,String extraFee){
		Map<String, String>  register=new HashMap<String, String>();
		CommonBean trans = new CommonBean();
		rate=new BigDecimal(rate).multiply(new BigDecimal("100")).stripTrailingZeros().toString();
//		extraFee=new BigDecimal(extraFee).subtract(new BigDecimal("0.5")).multiply(new BigDecimal("100")).setScale(0, BigDecimal.ROUND_DOWN).toString();
		extraFee=new BigDecimal(extraFee).multiply(new BigDecimal("100")).setScale(0, BigDecimal.ROUND_DOWN).toString();
		trans.setMethodname("register");
		trans.setTranscode("036");
		trans.setVersion("0100");
		String orderSn = UUID.randomUUID().toString().replaceAll("-", "");
		trans.setOrdersn(orderSn);
		String merchantOrder = UUID.randomUUID().toString().replaceAll("-", "");
		trans.setDsorderid(merchantOrder);
		trans.setMerchno(merchno);
		trans.setBankcard(bankcard);
		trans.setMerchantName(username);
		trans.setUsername(username);
		trans.setIdcard(idcard);
		trans.setMobile(mobile);
		trans.setCityName(cityName);
		trans.setCityCode(cityCode);
		trans.setFutureRateType("1");
		trans.setFutureRateValue(rate);
		trans.setFixAmount(extraFee);
		LOG.info("商户入驻请求报文======" + trans);
		String result = send(trans);
		LOG.info("请求商户入驻返回的result======" + result);
		JSONObject jsonobj = JSONObject.fromObject(result);
		register=jsonobj;
		return  register;
	}
	
	//修改商户费率update
	public Map<String, String> HQGUpdate(String subMerchantNo,String rate ,String extraFee ){
		Map<String, String>  update=new HashMap<String, String>();
		CommonBean trans = new CommonBean();
		rate=new BigDecimal(rate).multiply(new BigDecimal("100")).stripTrailingZeros().toString();
//		extraFee=new BigDecimal(extraFee).subtract(new BigDecimal("0.5")).multiply(new BigDecimal("100")).setScale(0, BigDecimal.ROUND_DOWN).toString();
		extraFee=new BigDecimal(extraFee).multiply(new BigDecimal("100")).setScale(0, BigDecimal.ROUND_DOWN).toString();
		trans.setMethodname("update");
		trans.setTranscode("036");
		trans.setVersion("0100");
		String orderSn = UUID.randomUUID().toString().replaceAll("-", "");
		trans.setOrdersn(orderSn);
		String merchantOrder = UUID.randomUUID().toString().replaceAll("-", "");
		trans.setDsorderid(merchantOrder);
		trans.setMerchno(merchno);
		trans.setSubMerchantNo(subMerchantNo);
		trans.setFutureRateType("1");
		trans.setFutureRateValue(rate);
		trans.setFixAmount(extraFee);
		LOG.info("修改费率请求报文======" + trans);
		String result = send(trans);
		LOG.info("请求修改费率返回的result======" + result);
		JSONObject jsonobj = JSONObject.fromObject(result);
		update=jsonobj;
		return  update;
	}
	
	//绑卡bindCard
	public Map<String, String> HQGbindCard(String merchantOrder,String subMerchantNo,String bankcard,String username,String idcard,String mobile,
			String userIP,String returnUrl,String notifyUrl){
		Map<String, String>  bindCard=new HashMap<String, String>();
		CommonBean trans = new CommonBean();
		trans.setMethodname("bindCard");
		trans.setTranscode("036");
		trans.setVersion("0100");
		String orderSn = UUID.randomUUID().toString().replaceAll("-", "");
		trans.setOrdersn(orderSn);
		trans.setDsorderid(merchantOrder);
		trans.setMerchno(merchno);
		trans.setSubMerchantNo(subMerchantNo);
		trans.setBankcard(bankcard);
		trans.setUsername(username);
		trans.setIdcard(idcard);
		trans.setMobile(mobile);
		trans.setUserIP(userIP);
		trans.setReturnUrl(returnUrl);
		trans.setFutureRateType("1");
		trans.setNotifyUrl(notifyUrl);
		LOG.info("绑卡请求报文======" + trans);
		String result = send(trans);
		LOG.info("请求绑卡返回的result======" + result);
		JSONObject jsonobj = JSONObject.fromObject(result);
		bindCard=jsonobj;
		return  bindCard;
	}
	
	//支付pay
	public Map<String, String> HQGPay(String merchantOrder,String subMerchantNo,String amount,String bankcard,String deviceId,String userIP,
			String mcc,String provinceCode,String notifyUrl){
		Map<String, String>  pay=new HashMap<String, String>();
		CommonBean trans = new CommonBean();
		amount=new BigDecimal(amount).multiply(new BigDecimal("100")).setScale(0, BigDecimal.ROUND_DOWN).toString();
		trans.setMethodname("pay");
		trans.setTranscode("036");
		trans.setVersion("0100");
		String orderSn = UUID.randomUUID().toString().replaceAll("-", "");
		trans.setOrdersn(orderSn);
//		String merchantOrder = UUID.randomUUID().toString().replaceAll("-", "");
		trans.setDsorderid(merchantOrder);
		trans.setMerchno(merchno);
		trans.setSubMerchantNo(subMerchantNo);
		trans.setAmount(amount);
		trans.setBankcard(bankcard);
		trans.setDeviceId(deviceId);
		trans.setDeviceType("1");
		trans.setUserIP(userIP);
		if (provinceCode != null) {
			trans.setProvince(provinceCode);
		}
		trans.setMcc(mcc);
		trans.setNotifyUrl(notifyUrl);
		LOG.info("支付pay请求报文======" + trans);
		String result = send(trans);
		LOG.info("请求支付pay返回的result======" + result);
		JSONObject jsonobj = JSONObject.fromObject(result);
		pay=jsonobj;
		return  pay;
	}
	

	//手动代付
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/topup/hqg/transfer/manual")
	public @ResponseBody Object transfer(HttpServletRequest request,
			//订单号
			@RequestParam(value = "orderCode") String orderCode,
			//金额
			@RequestParam(value = "real_account") String realAmount,
			//银行卡号
			@RequestParam(value = "bank_card") String bankCard,
			//银行名称
			@RequestParam(value = "bank_name") String bankName,
			//身份证号
			@RequestParam(value = "idcard") String idCard,
			//代付类型
			@RequestParam(value = "extra") String extra
			) {
		String handleType="Y";
		if(extra.equals("T1")){
			handleType="N";
		}
		HQGRegister hqRegister = topupPayChannelBusiness.getHQGRegisterByIdCard(idCard);
		Map<String ,String>  withDraw=HQGWithDraw(orderCode,hqRegister.getMerchantCode(), realAmount, bankCard, bankName,handleType);
		
		String returnCode = withDraw.get("returncode");
		
		String errtext = withDraw.get("errtext");
		
		if("0000".equals(returnCode) || "0003".equals(returnCode)) {
			
			return ResultWrap.init("999998", "等待银行出款中");
		}else {
			return ResultWrap.init(CommonConstants.FALIED, errtext);
		}
		

	}
	
	
	//代付withDraw
	public Map<String, String> HQGWithDraw(String merchantOrder,String subMerchantNo,String amount,String bankcard,String bankName,String handleType){
		Map<String, String>  withDraw=new HashMap<String, String>();
		CommonBean trans = new CommonBean();
		amount=new BigDecimal(amount).multiply(new BigDecimal("100")).setScale(0, BigDecimal.ROUND_DOWN).toString();
		trans.setMethodname("withDraw");
		trans.setTranscode("036");
		trans.setVersion("0100");
		String orderSn = UUID.randomUUID().toString().replaceAll("-", "");
		trans.setOrdersn(orderSn);
//		String merchantOrder = UUID.randomUUID().toString().replaceAll("-", "");
		trans.setDsorderid(merchantOrder);
		trans.setMerchno(merchno);
		trans.setSubMerchantNo(subMerchantNo);
		trans.setAmount(amount);
		trans.setBankcard(bankcard);
		trans.setBankName(bankName);
		trans.setHandleType(handleType);
		LOG.info("进件请求报文======" + trans);
		String result = send(trans);
		LOG.info("请求进件返回的result======" + result);
		JSONObject jsonobj = JSONObject.fromObject(result);
		withDraw=jsonobj;
		return  withDraw;
	}
	
	//查询商户余额
	public Map<String, String> HQGQueryBalance(String subMerchantNo){
		Map<String, String>  checkBalance=new HashMap<String, String>();
		CommonBean trans = new CommonBean();
		trans.setMethodname("queryBalance");
		trans.setTranscode("036");
		trans.setVersion("0100");
		String orderSn = UUID.randomUUID().toString().replaceAll("-", "");
		trans.setOrdersn(orderSn);
		String merchantOrder = UUID.randomUUID().toString().replaceAll("-", "");
		trans.setDsorderid(merchantOrder);
		trans.setMerchno(merchno);
		trans.setSubMerchantNo(subMerchantNo);
		LOG.info("代付withDraw请求报文======" + trans);
		String result = send(trans);
		LOG.info("请求代付withDraw返回的result======" + result);
		JSONObject jsonobj = JSONObject.fromObject(result);
		checkBalance=jsonobj;
		return  checkBalance;
	}
	
	//查询接口 105: 交易;106: 代付
	public Map<String, String> HQGQueryOrder(String dsorderid,String transtype){
		Map<String, String>  queryOrder=new HashMap<String, String>();
		CommonBean trans = new CommonBean();
		trans.setTranscode("902");
		trans.setVersion("0100");
		String orderSn = UUID.randomUUID().toString().replaceAll("-", "");
		trans.setOrdersn(orderSn);
		trans.setDsorderid(dsorderid);
		trans.setMerchno(merchno);
		trans.setDsorderid(dsorderid);
		trans.setTranstype(transtype);
		
		LOG.info("查询接口请求报文======" + trans);
		String result = send1(trans);
		LOG.info("请求查询接口返回的result======" + result);
		JSONObject jsonobj = JSONObject.fromObject(result);
		queryOrder=jsonobj;
		return  queryOrder;
	}
	
	public String getUserFee(String rate, String amount, String extraFee) {
		BigDecimal b1 = new BigDecimal(rate);
		BigDecimal b2 = new BigDecimal(amount);
		BigDecimal b3 = new BigDecimal(extraFee);
		BigDecimal num1 = b1.multiply(b2).setScale(2, BigDecimal.ROUND_UP);
		LOG.info("本金*费率=" + num1);
		BigDecimal num2 = num1.add(b3);
		LOG.info("额外手续费：" + num1);
		String fee = num2.toString();
		return fee;

	}
//	public static void main(String[] args) throws Exception {
//		HQGpageRequest hqgpageRequest =new HQGpageRequest();
////		hqgpageRequest.HQGPay("105108100005149", "50", "6225768681617732", "deviceId", "127.0.0.1", "127.0.0.1", "http://106.15.56.208/v1.0/paymentgateway/topup/hqg/fastpay/notifyurl");
//		hqgpageRequest.HQGQueryBalance("105108100005149");
//		Random ra =new Random();
//		for(int i=0;i<100;i++){
//			System.out.println(ra.nextInt(5)+1);
//		}
//		String province="";
//		String city="";
//		String extra="消费计划|福建省-泉州市";
//		
//		
//		
//		System.out.println(province+"========="+city);
//	}
	
	public String getUserFee1(String rate, String amount) {
		BigDecimal b1 = new BigDecimal(rate);
		BigDecimal b2 = new BigDecimal(amount);
		BigDecimal num1 = b1.multiply(b2).setScale(2, BigDecimal.ROUND_UP);
		LOG.info("手续费======" + num1);
		String fee = num1.toString();
		return fee;

	}

	public String getAmount(String fee, String amount) {
		BigDecimal b1 = new BigDecimal(fee);
		BigDecimal b2 = new BigDecimal(amount);
		String pay_amount = b1.add(b2).toString();
		return pay_amount;
	}

	
	public static String send(CommonBean trans) {
		String response = null;
		try {
			System.out.println("上传参数===>" + JsonUtils.objectToJson(trans));
			TransUtil tu = new TransUtil();
			byte[] reponse = tu.packet(trans, dskey);
			response = CommonUtil.post(url, reponse);
			System.out.println("返回参数===>" + response);
			Map<String, String> resMap = mapper.readValue(response, Map.class);
			String sign;
			sign = SignUtil.getSign(resMap, dskey);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return response;
	}
	
	public static String send1(CommonBean trans) {
		String response = null;
		try {
			System.out.println("上传参数===>" + JsonUtils.objectToJson(trans));
			TransUtil tu = new TransUtil();
			byte[] reponse = tu.packet(trans, dskey);
			response = CommonUtil.post(url1, reponse);
			System.out.println("返回参数===>" + response);
			Map<String, String> resMap = mapper.readValue(response, Map.class);
			String sign;
			sign = SignUtil.getSign(resMap, dskey);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return response;
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
	
	public String getMACAddress(String ip) {
        String str = "";
        String macAddress = "";
        /*try {
            Process p = Runtime.getRuntime().exec("nbtstat -A " + ip);
            InputStreamReader ir = new InputStreamReader(p.getInputStream());
            LineNumberReader input = new LineNumberReader(ir);
            for (int i = 1; i < 100; i++) {
                str = input.readLine();
                if (str != null) {
                    if (str.indexOf("MAC Address") > 1) {
                        macAddress = str.substring(
                                str.indexOf("MAC Address") + 14, str.length());
                        break;
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace(System.out);
        }*/
        return macAddress;
    }
	
	
}
