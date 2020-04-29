package com.jh.paymentgateway.controller;

import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.util.ArrayList;
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
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestTemplate;

import com.jh.paymentgateway.business.MHTopupPayChannelBusiness;
import com.jh.paymentgateway.common.ChannelUtils;
import com.jh.paymentgateway.config.RedisUtil;
import com.jh.paymentgateway.pojo.BankNumCode;
import com.jh.paymentgateway.pojo.MCCpo;
import com.jh.paymentgateway.pojo.MHHQBBindCard;
import com.jh.paymentgateway.pojo.PaymentRequestParameter;
import com.jh.paymentgateway.util.Util;
import com.jh.paymentgateway.util.hq.JsonUtils;
import com.jh.paymentgateway.util.hq.SignUtil;
import com.jh.paymentgateway.util.hqb.CommonBean;
import com.jh.paymentgateway.util.hqb.CommonUtil;
import com.jh.paymentgateway.util.hqb.RepayPlanList;
import com.jh.paymentgateway.util.hqb.TransUtil;

import cn.jh.common.tools.ResultWrap;
import cn.jh.common.utils.CommonConstants;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;


@Controller
@EnableAutoConfiguration
public class MHHQBpageRequest {
	private static final Logger LOG = LoggerFactory.getLogger(MHHQBpageRequest.class);
	static ObjectMapper mapper = new ObjectMapper();

	static {
		mapper.configure(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		mapper.setSerializationInclusion(Inclusion.NON_NULL);
	}

	@Autowired
	private RedisUtil redisUtil;

	@Autowired
	private MHTopupPayChannelBusiness topupPayChannelBusiness;

	@Value("${payment.ipAddress}")
	private String ip;

	public final static String DATE_YYYYMMDDHHMMSS = "yyyyMMddHHmmss";
	private static String merchantNo = "xydz20181203150";
	private static String key = "dac7533a";
	private String Url = "http://pay.huanqiuhuiju.com/authsys/smartrepay/execute.do";

	private static String Url1 = "http://pay.huanqiuhuiju.com/authsys/api/auth/execute.do";
	
	// 跟还款对接的接口
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/topup/mhhqb/torepayment")
	public @ResponseBody Object HQBToRepayMent(HttpServletRequest request,
			@RequestParam(value = "bankCard") String bankCard, @RequestParam(value = "idCard") String idCard,
			@RequestParam(value = "phone") String phone, @RequestParam(value = "userName") String userName,
			@RequestParam(value = "bankName") String bankName) throws Exception {

		Map<String, Object> map = new HashMap<String, Object>();

		MHHQBBindCard hqbBindCardByBankCard = topupPayChannelBusiness.getMHHQBBindCardByBankCard(bankCard);

		if (hqbBindCardByBankCard == null) {
				map.put(CommonConstants.RESP_CODE, "999996");
				map.put(CommonConstants.RESULT,
						ip + "/v1.0/paymentgateway/topup/tomhhqb/bindcard?bankName=" + URLEncoder.encode(bankName, "UTF-8")
								+ "&idCard=" + idCard + "&bankCard=" + bankCard + "&phone=" + phone + "&userName="
								+ userName + "&ipAddress=" + ip);
				map.put(CommonConstants.RESP_MESSAGE, "用户需要进行绑卡授权操作");

				return map;
		}else if(!"1".equals(hqbBindCardByBankCard.getStatus())) {
			
			Map<String, Object> queryTieCardId = (Map<String, Object>) queryTieCardId(bankCard);
			
			LOG.info("queryTieCardId======" + queryTieCardId);
			
			String respCode = (String) queryTieCardId.get("resp_code");
			
			if("000000".equals(respCode)) {
				String bindId = (String) queryTieCardId.get("result");
				
				hqbBindCardByBankCard.setBindId(bindId);
				hqbBindCardByBankCard.setStatus("1");
				
				topupPayChannelBusiness.createMHHQBBindCard(hqbBindCardByBankCard);
				
				return ResultWrap.init(CommonConstants.SUCCESS, "已完成绑卡");
			}else {
				map.put(CommonConstants.RESP_CODE, "999996");
				map.put(CommonConstants.RESULT,
						ip + "/v1.0/paymentgateway/topup/tomhhqb/bindcard?bankName=" + URLEncoder.encode(bankName, "UTF-8")
								+ "&idCard=" + idCard + "&bankCard=" + bankCard + "&phone=" + phone + "&userName="
								+ userName + "&ipAddress=" + ip);
				map.put(CommonConstants.RESP_MESSAGE, "用户需要进行绑卡授权操作");

				return map;
			}
			
		} else {
			
			return ResultWrap.init(CommonConstants.SUCCESS, "已完成绑卡");
		}

	}


	// 绑卡申请接口
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/topup/mhhqb/bindcard")
	public @ResponseBody Object HQBBindCard(@RequestParam(value = "bankCard") String bankCard,
			@RequestParam(value = "idCard") String idCard, 
			@RequestParam(value = "phone") String phone,
			@RequestParam(value = "userName") String userName,
			@RequestParam(value = "bankName") String bankName) throws Exception {
		LOG.info("开始进入绑卡申请接口========");

		CommonBean trans = new CommonBean();   
		trans.setMethodname("BindUnionCardNaked");      
		trans.setTranscode("032");     
		trans.setVersion("0100");  
		trans.setOrdersn(UUID.randomUUID().toString().replace("-", ""));
		String dsOrderId = UUID.randomUUID().toString().replace("-", "");    
		trans.setMerchno(merchantNo);    
		trans.setDsorderid(dsOrderId);
		trans.setNotifyUrl(ip + "/v1.0/paymentgateway/topup/mhhqb/bindcard/notify_call");
		trans.setUsername(userName); 
		trans.setMobile(phone);
		trans.setIdCard(idCard);
		trans.setBankCardNo(bankCard); 
		trans.setBankcardName(bankName);
		
		Random random = new Random();
		StringBuffer sb = new StringBuffer();
		for(int i = 0; i<10; i++) {
			int nextInt = random.nextInt(10);
			sb.append(nextInt);
		}
		String userId = sb.toString();
		trans.setUserId(userId);  
		TransUtil tu = new TransUtil();    
		byte[] reponse = tu.packet(trans, key); 
		
		String post = CommonUtil.post(Url, reponse);
		
		LOG.info("请求返回的post======" + post);
		
		JSONObject fromObject = JSONObject.fromObject(post);
		
		String returncode = fromObject.getString("returncode");
		String errtext = fromObject.getString("errtext");
		
		if("0000".equals(returncode)) {
			String returnUrl = fromObject.getString("returnUrl");
			String bindId = fromObject.getString("bindId");
			
			MHHQBBindCard hqbBindCardByBankCard = topupPayChannelBusiness.getMHHQBBindCardByBankCard(bankCard);
			
			if(hqbBindCardByBankCard != null) {
				
				//hqbBindCardByBankCard.setBindId(bindId);
				hqbBindCardByBankCard.setOrderCode(dsOrderId);
				hqbBindCardByBankCard.setUserId(userId);
				hqbBindCardByBankCard.setStatus("0");
				hqbBindCardByBankCard.setOrderCode(dsOrderId);
				
				topupPayChannelBusiness.createMHHQBBindCard(hqbBindCardByBankCard);
				
			}else {
				
				MHHQBBindCard hqbBindCard = new MHHQBBindCard();
				hqbBindCard.setPhone(phone);
				hqbBindCard.setBankCard(bankCard);
				hqbBindCard.setIdCard(idCard);
				hqbBindCard.setUserId(userId);
				//hqbBindCard.setBindId(bindId);
				hqbBindCard.setStatus("0");
				hqbBindCard.setOrderCode(dsOrderId);
				
				topupPayChannelBusiness.createMHHQBBindCard(hqbBindCard);
				
			}
			
			return ResultWrap.init(CommonConstants.SUCCESS, "请求成功", returnUrl);
			
		}else {
			
			return ResultWrap.init(CommonConstants.FALIED, errtext);
		}
		
	}



	// 制定代还计划的接口
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/topup/mhhqb/createrepayment/task")
	public @ResponseBody Object createRepayPlan(@RequestParam(value = "extra") String extra,
			@RequestParam(value = "bankCard") String bankCard,
			@RequestParam(value = "bankName") String bankName,
			@RequestParam(value = "userName") String userName,
			@RequestParam(value = "phone") String phone,
			@RequestParam(value = "rate") String rate,
			@RequestParam(value = "extraFee") String extraFee
			) throws Exception {
		LOG.info("开始进入制定代换计划接口======");

		JSONObject extraJson = JSONObject.fromObject(extra);
		
		JSONObject XFTask1 = extraJson.getJSONObject("XFTask1");
		JSONObject XFTask2 = extraJson.getJSONObject("XFTask2");
		JSONObject HKTask = extraJson.getJSONObject("HKTask");
		
		String orderCodeXF1 = XFTask1.getString("consumeTaskId");
		String amountXF1 = XFTask1.getString("amount");
		String serviceChargeXF1 = XFTask1.getString("serviceCharge");
		String executeDateTimeXF1 = XFTask1.getString("executeDateTime");
		String descriptionXF1 = XFTask1.getString("description");
		
		descriptionXF1 = descriptionXF1.substring(descriptionXF1.indexOf("|") + 1);
		String mcc = "14";
		String province = "上海市";
		String city = "上海市";
		Random random = new Random();
		if(descriptionXF1.contains("-") && descriptionXF1.split("-").length == 3) {
			province = descriptionXF1.substring(0, descriptionXF1.indexOf("-"));
			city = descriptionXF1.substring(descriptionXF1.indexOf("-") + 1, descriptionXF1.lastIndexOf("-"));
			String type = descriptionXF1.substring(descriptionXF1.lastIndexOf("-") + 1);
			MCCpo mcCpoByType = topupPayChannelBusiness.getMCCpoByType(type.trim());
			
			mcc = mcCpoByType.getMcc();
			
		}else if(descriptionXF1.contains("-") && descriptionXF1.split("-").length == 2){
			
			province = descriptionXF1.substring(0, descriptionXF1.indexOf("-"));
			city = descriptionXF1.substring(descriptionXF1.indexOf("-") + 1);
			
			List<String> list = new ArrayList<String>();
			try {
				List<MCCpo> mcCpo = topupPayChannelBusiness.getMCCpo();
				for(MCCpo mp : mcCpo) {
					String mc = mp.getMcc();
					list.add(mc);
				}
				
				mcc = list.get(random.nextInt(list.size()));
			} catch (Exception e) {
				e.printStackTrace();
				mcc = "14";
			}
			
		}else {
			List<String> list = new ArrayList<String>();
			try {
				List<MCCpo> mcCpo = topupPayChannelBusiness.getMCCpo();
				for(MCCpo mp : mcCpo) {
					String mc = mp.getMcc();
					list.add(mc);
				}
				
				mcc = list.get(random.nextInt(list.size()));
			} catch (Exception e) {
				e.printStackTrace();
				mcc = "14";
			}
			
		}
		
		String orderCodeXF2 = XFTask2.getString("consumeTaskId");
		String amountXF2 = XFTask2.getString("amount");
		String serviceChargeXF2 = XFTask2.getString("serviceCharge");
		String executeDateTimeXF2 = XFTask2.getString("executeDateTime");
		String descriptionXF2 = XFTask2.getString("description");
		
		descriptionXF2 = descriptionXF2.substring(descriptionXF2.indexOf("|") + 1);
		String mcc1 = "13";
		if(descriptionXF2.contains("-") && descriptionXF2.split("-").length == 3) {
			String type = descriptionXF2.substring(descriptionXF2.lastIndexOf("-") + 1);
			MCCpo mcCpoByType = topupPayChannelBusiness.getMCCpoByType(type.trim());
			
			mcc1 = mcCpoByType.getMcc();
			
		}else {
			List<String> list = new ArrayList<String>();
			try {
				List<MCCpo> mcCpo = topupPayChannelBusiness.getMCCpo();
				for(MCCpo mp : mcCpo) {
					String mc = mp.getMcc();
					list.add(mc);
				}
				
				mcc1 = list.get(random.nextInt(list.size()));
			} catch (Exception e) {
				e.printStackTrace();
				mcc1 = "13";
			}
			
			
		}
		
		String orderCodeHK = HKTask.getString("repaymentTaskId");
		String executeDateTimeHK = HKTask.getString("executeDateTime");
		
		BankNumCode bankNumCodeByBankName = topupPayChannelBusiness.getBankNumCodeByBankName(Util.queryBankNameByBranchName(bankName));
		
		String bankCode = "MIX";
		
		if(bankNumCodeByBankName != null){
			
			bankCode = bankNumCodeByBankName.getBankCode();
			
			if(bankName.contains("平安")) {
				bankCode = "PINGAN";
			}
			if(bankName.contains("交通")) {
				bankCode = "BOCOM";
			}
			if(bankName.contains("恒丰")) {
				bankCode = "HFBA";
			}
		}
		
		MHHQBBindCard hqbBindCard = topupPayChannelBusiness.getMHHQBBindCardByBankCard(bankCard);
		
		CommonBean trans = new CommonBean(); 
		trans.setTranscode("032");  
		trans.setVersion("0100");    
		trans.setDsorderid(UUID.randomUUID().toString().replaceAll("-", ""));
		trans.setMerchno(merchantNo);  
		trans.setMethodname("CreateRepayPlan"); 
		trans.setUserId(hqbBindCard.getUserId());
		trans.setUsername(userName);  
		trans.setMobile(phone);
		trans.setBankCardNo(bankCard);
		trans.setBankcardName(bankName);
		trans.setBankcardCode(bankCode);  
		trans.setBindId(hqbBindCard.getBindId());
		
		//BigDecimal fixAmount = new BigDecimal(extraFee).divide(new BigDecimal(2));
		BigDecimal fixAmount = new BigDecimal(extraFee);
		
		trans.setFixAmount(fixAmount.multiply(new BigDecimal("100")).setScale(0).toString());
		trans.setNotifyUrl(ip + "/v1.0/paymentgateway/topup/mhhqb/fastpay/notify_call");

		String bigRate = new BigDecimal(rate).multiply(new BigDecimal("100")).setScale(2).stripTrailingZeros().toPlainString();
		
		String bigServiceChargeXF1 = new BigDecimal(serviceChargeXF1).multiply(new BigDecimal("100")).setScale(0).toString();
		
		String bigServiceChargeXF2 = new BigDecimal(serviceChargeXF2).multiply(new BigDecimal("100")).setScale(0).toString();
		
		trans.setFutureRateValue(bigRate); 
//		目前支持0和1,不传默认走老通道 
//		1-新通道,小额,支持所有银行,  
//		0-老通道,大额,支持部分银行; 
		trans.setChantype("1"); 
		trans.setProvince(province);
		trans.setCity(city);
		trans.setDeviceType("iphone");
		//代还模式,1.一扣一还,2.多扣一还；默认1
		trans.setRepayMode("2");
		List<RepayPlanList> List = new ArrayList<RepayPlanList>();
		 
		RepayPlanList repayPlanList1 = new RepayPlanList();
		repayPlanList1.setTradeTime(executeDateTimeXF1);
		repayPlanList1.setTransferTime(executeDateTimeXF1);
		repayPlanList1.setTradeMoney(new BigDecimal(amountXF1).multiply(new BigDecimal("100")).setScale(0).add(new BigDecimal(bigServiceChargeXF1)).toString());
		repayPlanList1.setTransferMoney(new BigDecimal(amountXF1).multiply(new BigDecimal("100")).setScale(0).toString()); 
		repayPlanList1.setRateMoney(bigServiceChargeXF1);
		repayPlanList1.setRepayOrderFlag("1");
		repayPlanList1.setRepayOrderId(orderCodeXF1);
		repayPlanList1.setTransferRepayOrderId(orderCodeHK);
		repayPlanList1.setMcc(mcc);
		
		
		RepayPlanList repayPlanList2 = new RepayPlanList();
		repayPlanList2.setTradeTime(executeDateTimeXF2);
		repayPlanList2.setTransferTime(executeDateTimeXF2);
		repayPlanList2.setTradeMoney(new BigDecimal(amountXF2).multiply(new BigDecimal("100")).setScale(0).add(new BigDecimal(bigServiceChargeXF2)).toString());
		repayPlanList2.setTransferMoney(new BigDecimal(amountXF2).multiply(new BigDecimal("100")).setScale(0).toString());
		repayPlanList2.setRateMoney(bigServiceChargeXF2);
		repayPlanList2.setRepayOrderFlag("1");
		repayPlanList2.setRepayOrderId(orderCodeXF2);
		repayPlanList2.setTransferRepayOrderId(orderCodeHK);
		repayPlanList2.setMcc(mcc1);
		
		RepayPlanList repayPlanList3 = new RepayPlanList();
		repayPlanList3.setTradeTime(executeDateTimeHK);
		repayPlanList3.setTransferTime(executeDateTimeHK);
		repayPlanList3.setTradeMoney("0"); 
		repayPlanList3.setTransferMoney("0");
		repayPlanList3.setRateMoney("0");
		repayPlanList3.setRepayOrderFlag("2");
		repayPlanList3.setRepayOrderId(orderCodeHK);
		repayPlanList3.setTransferRepayOrderId(orderCodeHK);
		repayPlanList3.setMcc(mcc1); 
		
		List.add(repayPlanList1);
		List.add(repayPlanList2);
		List.add(repayPlanList3);
		
		trans.setRepayPlanList(List);
		 
		TransUtil tu = new TransUtil(); 
		byte[] reponse = tu.packet(trans, key);   
		String response = CommonUtil.post(Url, reponse);
		
		LOG.info("请求制定代换计划返回的response======" + response);
		
		JSONObject fromObject = JSONObject.fromObject(response);
		String returnCode = fromObject.getString("returncode");
		String errText = fromObject.getString("errtext");
		
		if("0000".equals(returnCode)) {
			
			return ResultWrap.init("999998", "等待计划执行!");
		}else {
			
			return ResultWrap.init(CommonConstants.FALIED, errText);
		}
		
	}

	
	//停止代还计划的接口
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/topup/mhhqb/stoprepayplan")
	public @ResponseBody Object stopRepayPlan(@RequestParam(value = "orderCode") String orderCode,
			@RequestParam(value = "bankCard") String bankCard
			)throws Exception {
		
		MHHQBBindCard hqbBindCard = topupPayChannelBusiness.getMHHQBBindCardByBankCard(bankCard);
		
		CommonBean trans = new CommonBean();
		trans.setTranscode("032");
		trans.setVersion("0100");
		trans.setOrdersn(UUID.randomUUID().toString().replaceAll("-", ""));
		trans.setMerchno(merchantNo); 
		trans.setMethodname("StopRepayPlan");   
		trans.setDsorderid(orderCode);
		trans.setUserId(hqbBindCard.getUserId());
		TransUtil tu = new TransUtil(); 
		byte[] reponse = tu.packet(trans, key);  
		String response = CommonUtil.post(Url, reponse);
	
		LOG.info("请求停止代还计划返回的response======" + response);
		
		JSONObject fromObject = JSONObject.fromObject(response);
		String returnCode = fromObject.getString("returncode");
		String errText = fromObject.getString("errtext");
		
		if("0000".equals(returnCode)) {
			
			return ResultWrap.init(CommonConstants.SUCCESS, "任务终止成功!");
		}else {
			
			return ResultWrap.init(CommonConstants.FALIED, errText);
		}
		
	}
	
	
	
	//查询代还计划单笔状态的接口
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/topup/mhhqb/queryrepayitemstatus")
	public @ResponseBody Object QueryRepayItemStatus(@RequestParam(value = "orderCode") String orderCode,
			@RequestParam(value = "bankCard") String bankCard
			)throws Exception {
		
		MHHQBBindCard hqbBindCardByBankCard = topupPayChannelBusiness.getMHHQBBindCardByBankCard(bankCard);
		
		CommonBean trans = new CommonBean();
		trans.setTranscode("032");
		trans.setVersion("0100"); 
		trans.setOrdersn(UUID.randomUUID().toString().replaceAll("-", "")); 
		trans.setMerchno(merchantNo);   
		trans.setMethodname("QueryRepayItemStatus"); 
		trans.setRepayOrderId(orderCode);
		trans.setUserId(hqbBindCardByBankCard.getUserId());
		TransUtil tu = new TransUtil();  
		byte[] reponse = tu.packet(trans, key);  
		String response = CommonUtil.post(Url, reponse);
		
		LOG.info("请求查询代还计划单笔状态返回的response======" + response);
		
		return null;
	}
	
	
	//订单查询的接口
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/topup/mhhqb/orderquery")
	public @ResponseBody Object QuickOpen(@RequestParam(value = "orderCode") String orderCode,
			@RequestParam(value = "transType", required = false, defaultValue = "63") String transType
			) throws Exception {
		Map<String, Object> maps = new HashMap<String, Object>();
		CommonBean trans = new CommonBean();
		trans.setVersion("0100");
		trans.setOrdersn(UUID.randomUUID().toString().replaceAll("-", ""));
		trans.setMerchno(merchantNo);
		trans.setDsorderid(orderCode);
		trans.setTranscode("902");
		trans.setTranstype(transType);

		String resp = TransUtil.object2String(trans);
		Map<String, String> resMap = mapper.readValue(resp, Map.class);
		String sign = SignUtil.getSign(resMap, key);
		trans.setSign(sign);

		String result = send1(trans);
		
		LOG.info("请求订单查询返回的result======" + result);
		
		JSONObject jsonobj = JSONObject.fromObject(result);
		
		String returnCode = jsonobj.getString("returncode");
		
		String message = null;
		if(jsonobj.containsKey("message")) {
			
			message = jsonobj.getString("message");
			
		}
		
		if("0000".equals(returnCode)) {
			String status = jsonobj.getString("status");
			if("00".equals(status)) {
				
				maps.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
				maps.put(CommonConstants.RESP_MESSAGE, message);
				
				return maps;
			}else if("01".equals(status)){
				
				maps.put(CommonConstants.RESP_CODE, "999998");
				maps.put(CommonConstants.RESP_MESSAGE, message);
				
				return maps;
			}else if("99".equals(status)){
				
				maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
				maps.put(CommonConstants.RESP_MESSAGE, "订单号不存在");
				
				return maps;
			}else if("04".equals(status)){
				maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
				maps.put(CommonConstants.RESP_MESSAGE, "订单关闭");
				
				return maps;
			}else {
				
				maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
				maps.put(CommonConstants.RESP_MESSAGE, message);
				
				return maps;
			}
			
		}else {
			
			maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			maps.put(CommonConstants.RESP_MESSAGE, message);
			
			return maps;
		}
		
	}
	
	
	
	// 查询落地商户支持省份的接口
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/topup/mhhqb/queryprovince")
	public @ResponseBody Object hqQueryArea(@RequestParam(value = "bankCard") String bankCard
			)throws Exception {
		LOG.info("开始进入查询落地商户支持省份的接口======");
		
		MHHQBBindCard hqbBindCard = topupPayChannelBusiness.getMHHQBBindCardByBankCard(bankCard);
		
		CommonBean trans = new CommonBean();
		trans.setTranscode("032");
		trans.setVersion("0100"); 
		trans.setOrdersn(UUID.randomUUID().toString().replaceAll("-", ""));
		trans.setMerchno(merchantNo); 
		trans.setMethodname("QueryArea"); 
		trans.setUserId(hqbBindCard.getUserId());
		TransUtil tu = new TransUtil(); 
		byte[] reponse = tu.packet(trans, key);  
		
		String response = CommonUtil.post(Url, reponse);
		
		//LOG.info("请求查询落地商户支持区域接口返回的 response======" + response);
		
		JSONObject fromObject = JSONObject.fromObject(response);
		
		String returncode = fromObject.getString("returncode");
		String errtext = fromObject.getString("errtext");
		
		if("0000".equals(returncode)) {
			
			JSONArray provinceItem = fromObject.getJSONArray("provinceItem");
				
			return ResultWrap.init(CommonConstants.SUCCESS, "查询成功!", provinceItem);
			
		}else {
			
			return ResultWrap.init(CommonConstants.FALIED, errtext);
		}
		
	}


	//查询落地商户支持城市的接口
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/topup/mhhqb/querycity")
	public @ResponseBody Object hqQueryCity(@RequestParam(value = "bankCard") String bankCard,
			@RequestParam(value = "parentId") String parentId
			)throws Exception {
		LOG.info("开始进入查询落地商户支持城市的接口======");
		
		MHHQBBindCard hqbBindCard = topupPayChannelBusiness.getMHHQBBindCardByBankCard(bankCard);
		
		CommonBean trans = new CommonBean();
		trans.setTranscode("032");
		trans.setVersion("0100"); 
		trans.setOrdersn(UUID.randomUUID().toString().replaceAll("-", ""));
		trans.setMerchno(merchantNo); 
		trans.setMethodname("QueryArea"); 
		trans.setUserId(hqbBindCard.getUserId());
		TransUtil tu = new TransUtil(); 
		byte[] reponse = tu.packet(trans, key);  
		
		String response = CommonUtil.post(Url, reponse);
		
		//LOG.info("请求查询落地商户支持区域接口返回的 response======" + response);
		
		JSONObject fromObject = JSONObject.fromObject(response);
		
		String returncode = fromObject.getString("returncode");
		String errtext = fromObject.getString("errtext");
		
		List<Object> list = new ArrayList<Object>();
		if("0000".equals(returncode)) {
			
			JSONArray cityItem = fromObject.getJSONArray("cityItem");
			
			for(int i = 0; i<cityItem.size(); i++) {
				JSONObject object = (JSONObject) cityItem.get(i);
				
				if(parentId.equals(object.getString("parentId"))) {
					list.add(object);
				}else {
					continue;
				}
				
			}
			
			return ResultWrap.init(CommonConstants.SUCCESS, "查询成功!", list);
			
		}else {
			
			return ResultWrap.init(CommonConstants.FALIED, errtext);
		}
		
	}

	
	
	//查询自选行业mcc的接口
/*	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/topup/hqb/querymcc")
	public @ResponseBody Object hqbQuerymcc(
			)throws Exception {
		
		List<MCCpo> mcCpo = topupPayChannelBusiness.getMCCpo();
		
		return ResultWrap.init(CommonConstants.SUCCESS, "查询成功!", mcCpo);
	}*/
	
	
	
	//查询银行额度限制的接口
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/topup/mhhqb/querybanklimit")
	public @ResponseBody Object QueryBankLimit(@RequestParam(value = "bankCard") String bankCard,
			@RequestParam(value = "bankName") String bankName
			)throws Exception {
		
		MHHQBBindCard hqbBindCard = topupPayChannelBusiness.getMHHQBBindCardByBankCard(bankCard);
		
		BankNumCode bankNumCodeByBankName = topupPayChannelBusiness.getBankNumCodeByBankName(Util.queryBankNameByBranchName(bankName));
		
		String bankCode = bankNumCodeByBankName.getBankCode();
		
		if(bankName.contains("平安")) {
			bankCode = "PINGAN";
		}
		if(bankName.contains("交通")) {
			bankCode = "BOCOM";
		}
		
		CommonBean trans = new CommonBean();
		trans.setTranscode("032");  
		trans.setVersion("0100");
		trans.setOrdersn(UUID.randomUUID().toString().replaceAll("-", ""));
		trans.setMerchno(merchantNo); 
		trans.setMethodname("QueryBankLimit"); 
		trans.setBankcardCode(bankCode);
		trans.setUserId(hqbBindCard.getUserId());
		TransUtil tu = new TransUtil(); 
		byte[] reponse = tu.packet(trans, key);  
		//URL换成相应的环境  即可
		String response = CommonUtil.post(Url, reponse);
		
		LOG.info("请求查询银行额度限制返回的response======" + response);
		
		JSONObject fromObject = JSONObject.fromObject(response);
		
		String returnCode = fromObject.getString("returncode");
		String errText = fromObject.getString("errtext");
		
		if("0000".equals(returnCode)) {
			
			String singleMaxLimit = fromObject.getString("singleMaxLimit");
			String dayMaxLimit = fromObject.getString("dayMaxLimit");
			
			Map<String,Object> map = new HashMap<String, Object>();
			
			map.put("singleMaxLimit", singleMaxLimit);
			map.put("dayMaxLimit", dayMaxLimit);
			
			return ResultWrap.init(CommonConstants.SUCCESS, "查询成功!", map);
			
		}else {
			
			return ResultWrap.init(CommonConstants.FALIED, errText);
		}
		
	}
	
	
	
	//查询绑卡Id的接口
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/topup/mhhqb/querytiecardid")
	public @ResponseBody Object queryTieCardId(@RequestParam(value = "bankCard") String bankCard
			) throws Exception {

		MHHQBBindCard hqbBindCard = topupPayChannelBusiness.getMHHQBBindCardByBankCard(bankCard);
		
		CommonBean trans = new CommonBean();
		trans.setTranscode("032");
		trans.setVersion("0100");
		trans.setOrdersn(UUID.randomUUID().toString().replaceAll("-", "")); 
		trans.setMerchno(merchantNo);   
		trans.setMethodname("QueryTieCardId"); 
		trans.setUserId(hqbBindCard.getUserId());
		TransUtil tu = new TransUtil(); 
		byte[] reponse = tu.packet(trans, key);  
		String response = CommonUtil.post(Url, reponse);
		
		LOG.info("response======" + response);
		
		JSONObject fromObject = JSONObject.fromObject(response);
		
		String returncode = fromObject.getString("returncode");
		String errtext = fromObject.getString("errtext");
		int j = 0;
		String tieCardId = null;
		if("0000".equals(returncode)) {
			if(fromObject.containsKey("resultList")) {
				JSONArray jsonArray = fromObject.getJSONArray("resultList");
				for(int i = 0; i<jsonArray.size(); i++) {
					JSONObject object = (JSONObject) jsonArray.get(i);
					String bankcardNumb = object.getString("bankcardNumb");
					String bankcardNumbbefore = bankcardNumb.substring(0, 3);
					String bankcardNumbafter = bankcardNumb.substring(bankcardNumb.length()-4, bankcardNumb.length());
					
					String subbefore = bankCard.substring(0, 3);
					String subafter = bankCard.substring(bankCard.length()-4, bankCard.length());
					
					if(subbefore.equals(bankcardNumbbefore) && subafter.equals(bankcardNumbafter)) {
						tieCardId = object.getString("tieCardId");
						j = 1;
					}
					
				}
				
				if(j == 1) {
					return ResultWrap.init(CommonConstants.SUCCESS, errtext, tieCardId);
				}else {
					return ResultWrap.init("999998", "暂未绑卡成功!"); 
				}
			}else {
				return ResultWrap.init("999998", "暂未绑卡成功!"); 
			}
		}else {
			
			return ResultWrap.init(CommonConstants.FALIED, errtext);
		}
		
	}
	
	
	// 绑卡异步通知接口
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/topup/mhhqb/bindcard/notify_call")
	public void hqBindCardNotifyCallback(HttpServletRequest request, HttpServletResponse response) throws Exception {
		LOG.info("绑卡异步通知进来了=======");

		Map<String, String[]> parameterMap = request.getParameterMap();
		Set<String> keySet = parameterMap.keySet();
		for (String key : keySet) {
			String[] strings = parameterMap.get(key);
			for (String s : strings) {
				LOG.info(key + "=============" + s);
			}
		}

		String respCode = request.getParameter("respCode");
		//String respMsg = request.getParameter("respMsg");
		
		if("00".equals(respCode)) {
			String userId = request.getParameter("userId");
			String bindId = request.getParameter("bindId");

			MHHQBBindCard hqbBindCardByUserId = topupPayChannelBusiness.getMHHQBBindCardByUserId(userId);
			
			hqbBindCardByUserId.setBindId(bindId);
			hqbBindCardByUserId.setStatus("1");
			
			topupPayChannelBusiness.createMHHQBBindCard(hqbBindCardByUserId);
			
			PrintWriter pw = response.getWriter();
			pw.print("SUCCESS");
			pw.close();
		}
		
	}

	// 异步通知接口
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/topup/mhhqb/fastpay/notify_call")
	public void hqFastPayNotifyCallback(HttpServletRequest request, HttpServletResponse response) throws Exception {
		LOG.info("异步通知进来了=======");

		Map<String, String[]> parameterMap = request.getParameterMap();
		Set<String> keySet = parameterMap.keySet();
		for (String key : keySet) {
			String[] strings = parameterMap.get(key);
			for (String s : strings) {
				LOG.info(key + "=============" + s);
			}
		}

		//String dsorderid = request.getParameter("dsorderid");
		//String respMsg = request.getParameter("respMsg");
		String repayOrderId = request.getParameter("repayOrderId");
		String respCode = request.getParameter("respCode");
		String status = request.getParameter("status");
		
		if("00".equals(respCode)) {
			if("3".equals(status) || "9".equals(status)) {
				
				PaymentRequestParameter prp = redisUtil.getPaymentRequestParameter(repayOrderId);
				
				String ipAddress = prp.getIpAddress();
				
				RestTemplate restTemplate = new RestTemplate();
				
				String url = ipAddress + "/v1.0/creditcardmanager/update/taskstatus/by/ordercode";
				MultiValueMap<String, String>  requestEntity = new LinkedMultiValueMap<String, String>();
				requestEntity.add("orderCode", repayOrderId);
				requestEntity.add("version", "10");
				String result = null;
				JSONObject jsonObject;
				JSONObject resultObj;
				try {
					result = restTemplate.postForObject(url, requestEntity, String.class);
					LOG.info("RESULT================" + result);
					jsonObject = JSONObject.fromObject(result);
					resultObj = jsonObject.getJSONObject("result");
				} catch (Exception e) {
					e.printStackTrace();
					LOG.error("",e);
				}

				Thread.sleep(1000);
				
				url = prp.getIpAddress()+ChannelUtils.getCallBackUrl(prp.getIpAddress());
				//url = ipAddress + "/v1.0/transactionclear/payment/update";
				requestEntity = new LinkedMultiValueMap<String, String>();
				requestEntity.add("status", "1");
				requestEntity.add("order_code", repayOrderId);
				requestEntity.add("third_code", "");
				try {
					result = restTemplate.postForObject(url, requestEntity, String.class);
				} catch (Exception e) {
					e.printStackTrace();
					LOG.error("",e);
				}
				
				LOG.info("订单状态修改成功===================" + repayOrderId + "====================" + result);

				LOG.info("订单已支付!");
				
				PrintWriter writer = response.getWriter();
				writer.print("success");
				writer.close();
				
			}
			
		}
		
	}


	// 跳转到绑卡页面
	@RequestMapping(method = RequestMethod.GET, value = "/v1.0/paymentgateway/topup/tomhhqb/bindcard")
	public String returnWMYKNewBindCard(HttpServletRequest request, HttpServletResponse response, Model model)
			throws IOException {

		request.setCharacterEncoding("utf-8");
		response.setCharacterEncoding("utf-8");
		response.setContentType("text/html;charset=utf-8");

		String bankName = request.getParameter("bankName");
		String bankCard = request.getParameter("bankCard");
		String idCard = request.getParameter("idCard");
		String phone = request.getParameter("phone");
		String userName = request.getParameter("userName");
		String ipAddress = request.getParameter("ipAddress");

		model.addAttribute("bankName", bankName);
		model.addAttribute("bankCard", bankCard);
		model.addAttribute("idCard", idCard);
		model.addAttribute("phone", phone);
		model.addAttribute("userName", userName);
		model.addAttribute("ipAddress", ipAddress);

		return "mhhqbbindcard";
	}
	
	
	
	public static String send1(CommonBean trans) {
		String response = null;
		try {
			System.out.println("上传参数===>" + JsonUtils.objectToJson(trans));
			TransUtil tu = new TransUtil();
			byte[] reponse = tu.packet(trans, key);
			response = CommonUtil.post(Url1, reponse);
			System.out.println("返回参数===>" + response);
			Map<String, String> resMap = mapper.readValue(response, Map.class);
			String sign;
			sign = SignUtil.getSign(resMap, key);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return response;
	}
	
	
}
