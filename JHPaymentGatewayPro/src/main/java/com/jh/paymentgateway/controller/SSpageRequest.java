package com.jh.paymentgateway.controller;

import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.TreeMap;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.httpclient.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.config.RequestConfig;
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
import org.springframework.ui.Model;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestTemplate;

import com.jh.paymentgateway.basechannel.BaseChannel;
import com.jh.paymentgateway.business.TopupPayChannelBusiness;
import com.jh.paymentgateway.common.ChannelUtils;
import com.jh.paymentgateway.config.RedisUtil;
import com.jh.paymentgateway.pojo.BankNumCode;
import com.jh.paymentgateway.pojo.HQBBindCard;
import com.jh.paymentgateway.pojo.MCCpo;
import com.jh.paymentgateway.pojo.PaymentRequestParameter;
import com.jh.paymentgateway.pojo.SSBindCard;
import com.jh.paymentgateway.util.Util;
import com.jh.paymentgateway.util.hqb.CommonBean;
import com.jh.paymentgateway.util.hqb.CommonUtil;
import com.jh.paymentgateway.util.hqb.RepayPlanList;
import com.jh.paymentgateway.util.hqb.TransUtil;
import com.jh.paymentgateway.util.rhjf.Md5Util;

import cn.jh.common.tools.ResultWrap;
import cn.jh.common.utils.CommonConstants;
import cn.jh.common.utils.ExceptionUtil;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

@Controller
@EnableAutoConfiguration
public class SSpageRequest extends BaseChannel{
	private static final Logger LOG = LoggerFactory.getLogger(SSpageRequest.class);

	@Autowired
	private RedisUtil redisUtil;

	@Autowired
	private TopupPayChannelBusiness topupPayChannelBusiness;

	@Value("${payment.ipAddress}")
	private String ip;

	public final static String DATE_YYYYMMDDHHMMSS = "yyyyMMddHHmmss";

	private static String merchantNo = "2253553";
	private static String key = "26D5EF2B610ED8113148656EFB5DE66C";
	private String Url = "https://pos.yeahka.com/";
	//private String Url = "http://pospre.yeahka.com/";
	
	// 跟还款对接的接口
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/topup/ss/torepayment")
	public @ResponseBody Object SSToRepayMent(HttpServletRequest request,
			@RequestParam(value = "bankCard") String bankCard, @RequestParam(value = "idCard") String idCard,
			@RequestParam(value = "phone") String phone, @RequestParam(value = "userName") String userName,
			@RequestParam(value = "bankName") String bankName) throws Exception {

		Map<String, Object> map = new HashMap<String, Object>();

		SSBindCard ssBindCardByBankCard = topupPayChannelBusiness.getSSBindCardByBankCard(bankCard);

		if (ssBindCardByBankCard == null) {
			map.put(CommonConstants.RESP_CODE, "999996");
			map.put(CommonConstants.RESULT,
					ip + "/v1.0/paymentgateway/topup/toss/bindcard?bankName=" + URLEncoder.encode(bankName, "UTF-8")
							+ "&idCard=" + idCard + "&bankCard=" + bankCard + "&phone=" + phone + "&userName="
							+ userName + "&ipAddress=" + ip);
			map.put(CommonConstants.RESP_MESSAGE, "用户需要进行绑卡授权操作");

			return map;
		} else if (!"1".equals(ssBindCardByBankCard.getStatus())) {

			Map<String, Object> queryTieCardId = (Map<String, Object>) queryBind(bankCard);

			LOG.info("queryTieCardId======" + queryTieCardId);

			String respCode = (String) queryTieCardId.get("resp_code");

			if ("000000".equals(respCode)) {

				ssBindCardByBankCard.setStatus("1");

				topupPayChannelBusiness.createSSBindCard(ssBindCardByBankCard);

				return ResultWrap.init(CommonConstants.SUCCESS, "已完成绑卡");
			} else {
				map.put(CommonConstants.RESP_CODE, "999996");
				map.put(CommonConstants.RESULT,
						ip + "/v1.0/paymentgateway/topup/toss/bindcard?bankName="
								+ URLEncoder.encode(bankName, "UTF-8") + "&idCard=" + idCard + "&bankCard=" + bankCard
								+ "&phone=" + phone + "&userName=" + userName + "&ipAddress=" + ip);
				map.put(CommonConstants.RESP_MESSAGE, "用户需要进行绑卡授权操作");

				return map;
			}

		} else {

			return ResultWrap.init(CommonConstants.SUCCESS, "已完成绑卡");
		}

	}

	// 绑卡申请接口
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/topup/ss/bindcard")
	public @ResponseBody Object SSBindCard(@RequestParam(value = "bankCard") String bankCard,
			@RequestParam(value = "idCard") String idCard, @RequestParam(value = "phone") String phone,
			@RequestParam(value = "userName") String userName, @RequestParam(value = "bankName") String bankName)
			throws Exception {
		LOG.info("开始进入绑卡申请接口========");
		Map<String, String> treeMap = new TreeMap<String, String>();

		Random random = new Random();
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < 10; i++) {
			int nextInt = random.nextInt(10);
			sb.append(nextInt);
		}
		String userId = sb.toString();
		
		treeMap.put("agentId", merchantNo);
		treeMap.put("userId", userId);
		treeMap.put("dateTime", new SimpleDateFormat("yyyyMMddHHmm").format(new Date()));
		treeMap.put("userName", userName);
		treeMap.put("mobile", phone);
		treeMap.put("bankcardNumb", bankCard);
		treeMap.put("bankcardName", bankName);
		treeMap.put("idCardNo", idCard);
		treeMap.put("longitude", "121.47");
		treeMap.put("latitude", "31.23");
		treeMap.put("callBackUrl", ip + "/v1.0/paymentgateway/topup/ss/bindcard/notify_call");

		Set<String> keySet = treeMap.keySet();
		Iterator<String> it = keySet.iterator();

		StringBuffer sb1 = new StringBuffer();
		while (it.hasNext()) {
			String next = it.next();
			sb1.append(treeMap.get(next));
		}

		LOG.info("加签参数======" + sb1);

		String sign1 = Md5Util.md5(sb1.toString());
		String lowerCase = sign1.toLowerCase();
		String sign2 = Md5Util.MD5(lowerCase + key);
		//LOG.info("加密结果sign1======" + sign1);
		//LOG.info("加密结果sign2======" + sign2);

		treeMap.put("version", "1.0");
		treeMap.put("sign", sign2.toUpperCase());

		LOG.info("申请绑卡的请求报文======" + treeMap);

		LOG.info("请求地址======" + Url + "leposweb/creditcard/api/bindUnionCardNaked.do");
		String postForObject = doPost(Url + "leposweb/creditcard/api/bindUnionCardNaked.do", treeMap);

		LOG.info("请求申请绑卡返回的postForObject======" + postForObject);

		JSONObject jsonObj = JSONObject.fromObject(postForObject);
		String errorCode = jsonObj.getString("error_code");
		String errorMessage = jsonObj.getString("error_tip");
		if ("-151".equals(errorCode)) {
			String pageJumpUrl = jsonObj.getString("pageJumpUrl");
			String bindId = jsonObj.getString("tieCardId");
			String bindOrderId = jsonObj.getString("bindOrderId");
			
			SSBindCard ssBindCard = topupPayChannelBusiness.getSSBindCardByBankCard(bankCard);
			if(ssBindCard != null) {
				ssBindCard.setBindId(bindId);
				ssBindCard.setOrderCode(bindOrderId);
				ssBindCard.setUserId(userId);
				ssBindCard.setStatus("0");
				
				topupPayChannelBusiness.createSSBindCard(ssBindCard);
			
			} else {
				SSBindCard ssbc = new SSBindCard();
				ssbc.setBankCard(bankCard);
				ssbc.setBindId(bindId);
				ssbc.setIdCard(idCard);
				ssbc.setOrderCode(bindOrderId);
				ssbc.setPhone(phone);
				ssbc.setUserId(userId);
				ssbc.setStatus("0");
				
				topupPayChannelBusiness.createSSBindCard(ssbc);
			}
			
			return ResultWrap.init(CommonConstants.SUCCESS, "请求成功", pageJumpUrl);
		} else {
			
			return ResultWrap.init(CommonConstants.FALIED, errorMessage);
		}

	}

	
	//查询落地商户支持省份的接口
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/topup/ss/queryprovince")
	public @ResponseBody Object queryProvince(@RequestParam(value = "bankCard") String bankCard
			) throws IOException {
		
		SSBindCard ssBindCardByBankCard = topupPayChannelBusiness.getSSBindCardByBankCard(bankCard);
		
		Map<String, String> treeMap = new TreeMap<String, String>();
		treeMap.put("agentId", merchantNo);
		treeMap.put("userId", ssBindCardByBankCard.getUserId());
		treeMap.put("dateTime", new SimpleDateFormat("yyyyMMddHHmm").format(new Date()));
		Set<String> keySet = treeMap.keySet();
		Iterator<String> it = keySet.iterator();

		StringBuffer sb1 = new StringBuffer();
		while (it.hasNext()) {
			String next = it.next();
			sb1.append(treeMap.get(next));
		}

		LOG.info("加签参数======" + sb1);

		String sign1 = Md5Util.md5(sb1.toString());
		String lowerCase = sign1.toLowerCase();
		String sign2 = Md5Util.MD5(lowerCase + key);

		treeMap.put("version", "1.0");
		treeMap.put("sign", sign2.toUpperCase());

		LOG.info("查询省市请求报文======" + treeMap);

		String postForObject = doPost(Url + "leposweb/creditcard/api/getProvinAndCitys.do", treeMap);

		LOG.info("请求查询省市返回的postForObject======" + postForObject);
		JSONObject jsonObj = JSONObject.fromObject(postForObject);
		String errorCode = jsonObj.getString("error_code");
		String errorMessage = jsonObj.getString("error_tip");

		if("0".equals(errorCode)) {
			JSONArray jsonArray = jsonObj.getJSONArray("province");
			
			return ResultWrap.init(CommonConstants.SUCCESS, "查询成功!", jsonArray);
			
		}else {
			
			return ResultWrap.init(CommonConstants.FALIED, errorMessage);
		}
		
	}

	
	// 查询落地商户支持城市的接口
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/topup/ss/querycity")
	public @ResponseBody Object ssQueryCity(@RequestParam(value = "bankCard") String bankCard,
			@RequestParam(value = "parentId") String parentId) throws Exception {
		LOG.info("开始进入查询落地商户支持城市的接口======");

		SSBindCard ssBindCardByBankCard = topupPayChannelBusiness.getSSBindCardByBankCard(bankCard);

		Map<String, String> treeMap = new TreeMap<String, String>();
		treeMap.put("agentId", merchantNo);
		treeMap.put("userId", ssBindCardByBankCard.getUserId());
		treeMap.put("dateTime", new SimpleDateFormat("yyyyMMddHHmm").format(new Date()));
		Set<String> keySet = treeMap.keySet();
		Iterator<String> it = keySet.iterator();

		StringBuffer sb1 = new StringBuffer();
		while (it.hasNext()) {
			String next = it.next();
			sb1.append(treeMap.get(next));
		}

		LOG.info("加签参数======" + sb1);

		String sign1 = Md5Util.md5(sb1.toString());
		String lowerCase = sign1.toLowerCase();
		String sign2 = Md5Util.MD5(lowerCase + key);

		treeMap.put("version", "1.0");
		treeMap.put("sign", sign2.toUpperCase());

		LOG.info("查询省市请求报文======" + treeMap);

		String postForObject = doPost(Url + "leposweb/creditcard/api/getProvinAndCitys.do", treeMap);

		LOG.info("请求查询省市返回的postForObject======" + postForObject);
		JSONObject jsonObj = JSONObject.fromObject(postForObject);
		String errorCode = jsonObj.getString("error_code");
		String errorMessage = jsonObj.getString("error_tip");

		List<Object> list = new ArrayList<Object>();
		if ("0".equals(errorCode)) {
			JSONArray city = jsonObj.getJSONArray("city");

			for (int i = 0; i < city.size(); i++) {
				JSONObject object = (JSONObject) city.get(i);

				if (parentId.equals(object.getString("parentId"))) {
					list.add(object);
				} else {
					continue;
				}
			}

			return ResultWrap.init(CommonConstants.SUCCESS, "查询成功!", list);

		} else {

			return ResultWrap.init(CommonConstants.FALIED, errorMessage);
		}

	}

	
	//查询绑卡ID的接口
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/topup/ss/queryBind")
	public @ResponseBody Object queryBind(@RequestParam(value = "bankCard") String bankCard
			) throws IOException {

		SSBindCard ssBindCard = topupPayChannelBusiness.getSSBindCardByBankCard(bankCard);
		
		Map<String, String> treeMap = new TreeMap<String, String>();

		treeMap.put("agentId", merchantNo);
		treeMap.put("userId", ssBindCard.getUserId());
		treeMap.put("dateTime", new SimpleDateFormat("yyyyMMddHHmm").format(new Date()));
		Set<String> keySet = treeMap.keySet();
		Iterator<String> it = keySet.iterator();

		StringBuffer sb1 = new StringBuffer();
		while (it.hasNext()) {
			String next = it.next();
			sb1.append(treeMap.get(next));
		}

		LOG.info("加签参数======" + sb1);

		String sign1 = Md5Util.md5(sb1.toString());
		String lowerCase = sign1.toLowerCase();
		String sign2 = Md5Util.MD5(lowerCase + key);

		treeMap.put("version", "1.0");
		treeMap.put("sign", sign2.toUpperCase());

		LOG.info("查询绑卡的请求报文======" + treeMap);

		String postForObject = doPost(Url + "leposweb/creditcard/api/queryTieCardId.do", treeMap);

		LOG.info("查询绑卡返回的postForObject======" + postForObject);
		
		JSONObject jsonObj = JSONObject.fromObject(postForObject);
		String errorCode = jsonObj.getString("error_code");
		String errorMessage = jsonObj.getString("error_tip");
		int j = 0;
		String tieCardId = null;
		if("0".equals(errorCode)) {
			if(jsonObj.containsKey("resultList")) {
				JSONArray jsonArray = jsonObj.getJSONArray("resultList");
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
					return ResultWrap.init(CommonConstants.SUCCESS, errorMessage, tieCardId);
				}else {
					return ResultWrap.init("999998", "暂未绑卡成功!"); 
				}
			}else {
				return ResultWrap.init("999998", "暂未绑卡成功!"); 
			}
		}else {
			
			return ResultWrap.init(CommonConstants.FALIED, errorMessage);
		}
		
	}

	// 制定代还计划的接口
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/topup/ss/createrepayment/task")
	public @ResponseBody Object createRepayPlan(@RequestParam(value = "extra") String extra,
			@RequestParam(value = "bankCard") String bankCard, @RequestParam(value = "bankName") String bankName,
			@RequestParam(value = "userName") String userName, @RequestParam(value = "phone") String phone,
			@RequestParam(value = "rate") String rate, @RequestParam(value = "extraFee") String extraFee,
			@RequestParam(value = "orderCode") String orderCode, @RequestParam(value = "ipAddress") String ipAddress
			)
			throws Exception {
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
		if (descriptionXF1.contains("-") && descriptionXF1.split("-").length == 3) {
			province = descriptionXF1.substring(0, descriptionXF1.indexOf("-"));
			city = descriptionXF1.substring(descriptionXF1.indexOf("-") + 1, descriptionXF1.lastIndexOf("-"));
			String type = descriptionXF1.substring(descriptionXF1.lastIndexOf("-") + 1);
			MCCpo mcCpoByType = topupPayChannelBusiness.getMCCpoByType(type.trim());

			mcc = mcCpoByType.getMcc();

		} else if (descriptionXF1.contains("-") && descriptionXF1.split("-").length == 2) {

			province = descriptionXF1.substring(0, descriptionXF1.indexOf("-"));
			city = descriptionXF1.substring(descriptionXF1.indexOf("-") + 1);

			List<String> list = new ArrayList<String>();
			try {
				List<MCCpo> mcCpo = topupPayChannelBusiness.getMCCpo();
				for (MCCpo mp : mcCpo) {
					String mc = mp.getMcc();
					list.add(mc);
				}

				mcc = list.get(random.nextInt(list.size()));
			} catch (Exception e) {
				e.printStackTrace();
				mcc = "14";
			}

		} else {
			List<String> list = new ArrayList<String>();
			try {
				List<MCCpo> mcCpo = topupPayChannelBusiness.getMCCpo();
				for (MCCpo mp : mcCpo) {
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
		if (descriptionXF2.contains("-") && descriptionXF2.split("-").length == 3) {
			String type = descriptionXF2.substring(descriptionXF2.lastIndexOf("-") + 1);
			MCCpo mcCpoByType = topupPayChannelBusiness.getMCCpoByType(type.trim());

			mcc1 = mcCpoByType.getMcc();

		} else {
			List<String> list = new ArrayList<String>();
			try {
				List<MCCpo> mcCpo = topupPayChannelBusiness.getMCCpo();
				for (MCCpo mp : mcCpo) {
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

		BankNumCode bankNumCodeByBankName = topupPayChannelBusiness
				.getBankNumCodeByBankName(Util.queryBankNameByBranchName(bankName));

		String bankCode = "MIX";
		
		if(bankNumCodeByBankName != null) {
			bankCode = bankNumCodeByBankName.getBankCode();

			if (bankName.contains("平安")) {
				bankCode = "PINGAN";
			}
			if (bankName.contains("交通")) {
				bankCode = "BOCOM";
			}
			if (bankName.contains("恒丰")) {
				bankCode = "HFBA";
			}
		}
		
		

		SSBindCard ssBindCard = topupPayChannelBusiness.getSSBindCardByBankCard(bankCard);

		Map<String, String> treeMap = new TreeMap<String, String>();

		treeMap.put("agentId", merchantNo);
		treeMap.put("userId", ssBindCard.getUserId());
		treeMap.put("dateTime", new SimpleDateFormat("yyyyMMddHHmm").format(new Date()));
		
		treeMap.put("mobile", phone);
		treeMap.put("bankcardNumb", bankCard);
		treeMap.put("bankcardName", bankName);
		treeMap.put("bankcardCode", bankCode);
		
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < 10; i++) {
			int nextInt = random.nextInt(10);
			sb.append(nextInt);
		}
		
		BigDecimal fixAmount = new BigDecimal(extraFee).divide(new BigDecimal(2));
		String bigRate = new BigDecimal(rate).multiply(new BigDecimal("100")).setScale(2).stripTrailingZeros()
				.toPlainString();

		String bigServiceChargeXF1 = new BigDecimal(serviceChargeXF1).multiply(new BigDecimal("100")).setScale(0)
				.toString();

		String bigServiceChargeXF2 = new BigDecimal(serviceChargeXF2).multiply(new BigDecimal("100")).setScale(0)
				.toString();
		
		String order = sb.toString();
		String userOrderId = "WF" + merchantNo + order;
		treeMap.put("userOrderId", userOrderId);
		treeMap.put("tieCardId",ssBindCard.getBindId() );
		treeMap.put("longitude", "121.47");
		treeMap.put("latitude", "31.23");
		treeMap.put("rate", bigRate);
		treeMap.put("cost", fixAmount.multiply(new BigDecimal("100")).setScale(0).toString());
		treeMap.put("channelType", "1");
		treeMap.put("provName", province.trim());
		treeMap.put("cityName", city.trim());
		treeMap.put("deviceId", "iphone");
		treeMap.put("repayModeFlag", "2");
		
		//treeMap.put("callBackUrl", ip + "/v1.0/paymentgateway/topup/ss/bindcard/notify_call");
		
		List<RepayPlanList> List = new ArrayList<RepayPlanList>();

		RepayPlanList repayPlanList1 = new RepayPlanList();
		repayPlanList1.setTradeTime(executeDateTimeXF1);
		repayPlanList1.setTransferTime(executeDateTimeXF1);
		repayPlanList1.setTradeMoney(new BigDecimal(amountXF1).multiply(new BigDecimal("100")).setScale(0)
				.add(new BigDecimal(bigServiceChargeXF1)).toString());
		repayPlanList1
				.setTransferMoney(new BigDecimal(amountXF1).multiply(new BigDecimal("100")).setScale(0).toString());
		repayPlanList1.setRateMoney(bigServiceChargeXF1);
		repayPlanList1.setRepayOrderFlag("1");
		repayPlanList1.setRepayOrderId(orderCodeXF1);
		repayPlanList1.setTransferRepayOrderId(orderCodeHK);
		repayPlanList1.setMcc(mcc);

		RepayPlanList repayPlanList2 = new RepayPlanList();
		repayPlanList2.setTradeTime(executeDateTimeXF2);
		repayPlanList2.setTransferTime(executeDateTimeXF2);
		repayPlanList2.setTradeMoney(new BigDecimal(amountXF2).multiply(new BigDecimal("100")).setScale(0)
				.add(new BigDecimal(bigServiceChargeXF2)).toString());
		repayPlanList2
				.setTransferMoney(new BigDecimal(amountXF2).multiply(new BigDecimal("100")).setScale(0).toString());
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

		LOG.info("List======" + List);
		
		String str = com.alibaba.fastjson.JSONObject.toJSONString(List);
		
		LOG.info("转json串:" +str.toString());
		String data=base64(str.getBytes());
		
		treeMap.put("repayPlanJson",data);
		
		Set<String> keySet = treeMap.keySet();
		Iterator<String> it = keySet.iterator();
		StringBuffer sb1 = new StringBuffer();
		while (it.hasNext()) {
			String next = it.next();
			sb1.append(treeMap.get(next));
		}

		LOG.info("加签参数======" + sb1);
		String sign1 = Md5Util.md5(sb1.toString());
		String lowerCase = sign1.toLowerCase();
		String sign2 = Md5Util.MD5(lowerCase + key);

		treeMap.put("version", "1.0");
		treeMap.put("sign", sign2.toUpperCase());

		LOG.info("制定代还计划的请求报文======" + treeMap);

		String postForObject = doPost(Url + "leposweb/creditcard/api/comfirmRepayPlan.do", treeMap);

		LOG.info("请求制定代还计划返回的postForObject======" + postForObject);
		
		JSONObject fromObject = JSONObject.fromObject(postForObject);
		
		String error_tip = fromObject.getString("error_tip");
		String error_code = fromObject.getString("error_code");
		
		this.updatePaymentOrderThirdOrder(ipAddress, orderCode, userOrderId);
		
		if("0".equals(error_code)) {
			
			return ResultWrap.init("999998", "等待计划执行!");
		}else {
			
			return ResultWrap.init(CommonConstants.FALIED, error_tip);
		}
		
	}

	// 停止代还计划的接口
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/topup/ss/stoprepayplan")
	public @ResponseBody Object stopRepayPlan(@RequestParam(value = "orderCode") String orderCode,
			@RequestParam(value = "bankCard") String bankCard) throws Exception {

		HQBBindCard hqbBindCard = topupPayChannelBusiness.getHQBBindCardByBankCard(bankCard);

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

		if ("0000".equals(returnCode)) {

			return ResultWrap.init(CommonConstants.SUCCESS, "任务终止成功!");
		} else {

			return ResultWrap.init(CommonConstants.FALIED, errText);
		}

	}

	// 查询代还计划单笔状态的接口
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/topup/ss/queryrepayitemstatus")
	public @ResponseBody Object QueryRepayItemStatus(@RequestParam(value = "orderCode") String orderCode,
			@RequestParam(value = "bankCard") String bankCard) throws Exception {

		HQBBindCard hqbBindCardByBankCard = topupPayChannelBusiness.getHQBBindCardByBankCard(bankCard);

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

	// 订单查询的接口
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/topup/ss/orderquery")
	public @ResponseBody Object QuickOpen(@RequestParam(value = "orderCode") String orderCode,
			@RequestParam(value = "bankCard") String bankCard)
			throws Exception {
		
		SSBindCard ssBindCard = topupPayChannelBusiness.getSSBindCardByBankCard(bankCard);
		
		Map<String, String> treeMap = new TreeMap<String, String>();
		treeMap.put("agentId", merchantNo);
		treeMap.put("userId", ssBindCard.getUserId());
		treeMap.put("dateTime", new SimpleDateFormat("yyyyMMddHHmm").format(new Date()));
		treeMap.put("merchantOrderId", orderCode);
		Set<String> keySet = treeMap.keySet();
		Iterator<String> it = keySet.iterator();

		StringBuffer sb1 = new StringBuffer();
		while (it.hasNext()) {
			String next = it.next();
			sb1.append(treeMap.get(next));
		}

		LOG.info("加签参数======" + sb1);

		String sign1 = Md5Util.md5(sb1.toString());
		String lowerCase = sign1.toLowerCase();
		String sign2 = Md5Util.MD5(lowerCase + key);

		treeMap.put("version", "1.0");
		treeMap.put("sign", sign2.toUpperCase());

		LOG.info("订单查询的请求报文======" + treeMap);

		String postForObject = doPost(Url + "leposweb/creditcard/api/queryRepayStatus.do", treeMap);

		LOG.info("请求查询订单状态返回的postForObject======" + postForObject);
		
		JSONObject fromObject = JSONObject.fromObject(postForObject);
		String error_code = fromObject.getString("error_code");
		String error_tip = fromObject.getString("error_tip");
		Object has_errorMsg=fromObject.get("errorMsg");
		String errorMsg = has_errorMsg!=null?fromObject.getString("errorMsg"):"";
		
		if("0".equals(error_code)) {
			String repayStatus = fromObject.getString("repayStatus");
			if("3".equals(repayStatus) || "6".equals(repayStatus) || "9".equals(repayStatus)) {
				
				return ResultWrap.init(CommonConstants.SUCCESS, error_tip);
			}else if("4".equals(repayStatus)) {
				
				return ResultWrap.init(CommonConstants.FALIED, errorMsg);
			}else if("7".equals(repayStatus) || "8".equals(repayStatus)) {
				
				return ResultWrap.init(CommonConstants.FALIED, errorMsg);
			}else{
				
				return ResultWrap.init("999998", "等待订单最终状态!");
			}			
		}else {
			
			return ResultWrap.init(CommonConstants.FALIED, error_tip);
		}
		
	}

	
	// 绑卡异步通知接口
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/topup/ss/bindcard/notify_call")
	public void ssBindCardNotifyCallback(HttpServletRequest request, HttpServletResponse response) throws Exception {
		LOG.info("绑卡异步通知进来了=======");

		Map<String, String[]> parameterMap = request.getParameterMap();
		Set<String> keySet = parameterMap.keySet();
		for (String key : keySet) {
			String[] strings = parameterMap.get(key);
			for (String s : strings) {
				LOG.info(key + "=============" + s);
			}
		}
		
		
		/*if("00".equals(respCode)) {
			String orderId = request.getParameter("orderId");
			
			topupPayChannelBusiness.getSSBindCardByBindId("18" + orderId);
			
		}*/
		
	}

	// 异步通知接口
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/topup/ss/fastpay/notify_call")
	public void ssFastPayNotifyCallback(HttpServletRequest request, HttpServletResponse response) throws Exception {
		LOG.info("异步通知进来了=======");

		Map<String, String[]> parameterMap = request.getParameterMap();
		Set<String> keySet = parameterMap.keySet();
		for (String key : keySet) {
			String[] strings = parameterMap.get(key);
			for (String s : strings) {
				LOG.info(key + "=============" + s);
			}
		}

		String merchantOrderId = request.getParameter("merchantOrderId");
		String repayStatus = request.getParameter("repayStatus");
		
	    if ("3".equals(repayStatus) || "9".equals(repayStatus)) {

				PaymentRequestParameter prp = redisUtil.getPaymentRequestParameter(merchantOrderId);

				String ipAddress = prp.getIpAddress();

				RestTemplate restTemplate = new RestTemplate();

				String url = ipAddress + "/v1.0/creditcardmanager/update/taskstatus/by/ordercode";
				MultiValueMap<String, String> requestEntity = new LinkedMultiValueMap<String, String>();
				requestEntity.add("orderCode", merchantOrderId);
				requestEntity.add("version", "11");
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
				requestEntity.add("order_code", merchantOrderId);
				requestEntity.add("third_code", "");
				try {
					result = restTemplate.postForObject(url, requestEntity, String.class);
				} catch (Exception e) {
					e.printStackTrace();
					LOG.error("",e);
				}

				LOG.info("订单状态修改成功===================" + merchantOrderId + "====================" + result);

				PrintWriter writer = response.getWriter();
				writer.print("success");
				writer.close();

			}

	}

	// 跳转绑卡界面
	@RequestMapping(method = RequestMethod.GET, value = "/v1.0/paymentgateway/topup/jump/bindcard")
	public @ResponseBody Object jumpBindCard(@RequestParam(value = "bankName") String bankName,
			@RequestParam(value = "bankCard") String bankCard, @RequestParam(value = "idCard") String idCard,
			@RequestParam(value = "phone") String phone, @RequestParam(value = "userName") String userName)
			throws Exception {
		Map<String, Object> maps = new HashMap<String, Object>();
		maps.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
		maps.put(CommonConstants.RESULT,
				ip + "/v1.0/paymentgateway/topup/toss/bindcard?bankName=" + URLEncoder.encode(bankName, "UTF-8")
						+ "&idCard=" + idCard + "&bankCard=" + bankCard + "&phone=" + phone + "&userName=" + userName
						+ "&ipAddress=" + ip);
		return maps;

	}

	
	@RequestMapping(method = RequestMethod.GET, value = "/v1.0/paymentgateway/topup/toss/bindcard")
	public String returnBindCard(HttpServletRequest request, HttpServletResponse response, Model model)
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

		return "ssbindcard";
	}
	
	

	private static String doPost(String url, Map<String, String> treeMap) throws IOException {
		RequestConfig requestConfig = RequestConfig.custom().setSocketTimeout(60000) // 服务器返回数据(response)的时间，超过该时间抛出read
																						// timeout
				.setConnectTimeout(5000)// 连接上服务器(握手成功)的时间，超出该时间抛出connect
										// timeout
				.setConnectionRequestTimeout(1000)// 从连接池中获取连接的超时时间，超过该时间未拿到可用连接，会抛出org.apache.http.conn.ConnectionPoolTimeoutException:
													// Timeout waiting for
													// connection from pool
				.build();

		String result = null;
		CloseableHttpClient httpClient = HttpClients.createDefault();
		CloseableHttpResponse response = null;
		HttpPost httpPost = new HttpPost(url);
		httpPost.setConfig(requestConfig);
		try {
			if (treeMap != null) {
				// 设置2个post参数
				List<NameValuePair> parameters = new ArrayList<NameValuePair>();
				for (String key : treeMap.keySet()) {
					parameters.add(new BasicNameValuePair(key, (String) treeMap.get(key)));
				}
				// 构造一个form表单式的实体
				UrlEncodedFormEntity formEntity = new UrlEncodedFormEntity(parameters, "UTF-8");
				// 将请求实体设置到httpPost对象中
				httpPost.setEntity(formEntity);
			}

			response = httpClient.execute(httpPost);

			int statusCode = response.getStatusLine().getStatusCode();
			LOG.info("请求响应码statusCode======" + statusCode);
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

	
	public static String base64(byte[] plainBytes) {
		byte[] b = plainBytes;
		
		org.apache.commons.codec.binary.Base64 base64 = new org.apache.commons.codec.binary.Base64();
		b = base64.encode(b);
		String s = new String(b, StandardCharsets.UTF_8);
		return s;
	}
	
	
}
