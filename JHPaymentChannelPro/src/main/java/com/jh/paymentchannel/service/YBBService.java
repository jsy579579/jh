package com.jh.paymentchannel.service;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.PostMethod;
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

import com.jh.paymentchannel.business.RegisterAuthBusiness;
import com.jh.paymentchannel.business.TopupPayChannelBusiness;
import com.jh.paymentchannel.pojo.RegisterAuth;
import com.jh.paymentchannel.pojo.YBQuickRegister;
import com.jh.paymentchannel.util.Util;
import com.jh.paymentchannel.util.yeepay.Conts;
import com.jh.paymentchannel.util.yeepay.Digest;

import cn.jh.common.tools.ResultWrap;
import cn.jh.common.utils.CommonConstants;
import cn.jh.common.utils.ExceptionUtil;
import cn.jh.common.utils.UUIDGenerator;
import net.sf.json.JSONObject;


@Controller
@EnableAutoConfiguration
public class YBBService {

	private static final Logger LOG = LoggerFactory.getLogger(YBBService.class);
	private static String key = Conts.hmacKey; // 商户秘钥
	
	//private static String key = "hf6Kjql0340f2769N82CCAlj0k23570W2uGP8Z2V4qeF9Z2B941hmio7K65w";
	
	private static String Key = "hf6Kjql0340f2769N82CCAlj0k23570W2uGP8Z2V4qeF9Z2B941hmio7K65w";
	
	@Autowired
	private TopupPayChannelBusiness topupPayChannelBusiness;
	
	@Autowired
	private RegisterAuthBusiness registerAuthBusiness;
	
	/**
	 * 请求参数 验签在数组最后
	 *
	 */
	private static NameValuePair[] param = {
			// 出款金额
			new NameValuePair("amount", ""),
			// 小商户编号
			new NameValuePair("customerNumber", ""),
			// 出款订单号
			new NameValuePair("externalNo", ""),
			// 大商户编号
			new NameValuePair("mainCustomerNumber", ""),
			
			// 出款方式
			new NameValuePair("transferWay", ""),
			
			new NameValuePair("callBackUrl",""),
			
			// 签名串
			new NameValuePair("hmac", ""),
			
	};
	
	private static NameValuePair[] param1 = {
			// 大商户编号
			new NameValuePair("mainCustomerNumber", ""),
			// 小商户编号
			new NameValuePair("customerNumber", ""),
			// 出款订单号
			new NameValuePair("balanceType", ""),

			// 签名串
			new NameValuePair("hmac", ""),

	};
	
	@Autowired
	Util util;
	@Autowired
	RegisterAuthService ras;
	
	@Value("${payment.ipAddress}")
	private String ipAddress;
	@RequestMapping(method=RequestMethod.GET,value="/v1.0/paymentchannel/topup/ybbpage")
	public  String returnpay(HttpServletRequest request, HttpServletResponse response, Model model)throws IOException {
			return "zhifuYB";
	}
	
	@RequestMapping(method=RequestMethod.GET,value="/v1.0/paymentchannel/topup/ybnewbpage")
	public  String returnybNewPay(HttpServletRequest request, HttpServletResponse response, Model model)throws IOException {
			return "zhifuYBNew";
	}
	
	
	// 查询余额
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentchannel/topup/yb/balancequery")
	public @ResponseBody Object ybBalanceQuery(HttpServletRequest request, 
			@RequestParam(value = "phone") String phone,
			@RequestParam(value = "brandId") String brandId,
			@RequestParam(value = "channel", required = false, defaultValue = "1") String channel,
			@RequestParam(value = "balanceType", required = false, defaultValue = "3") String balanceType // 1代表T0余额 // 2代表T1余额  // 3代表账户
	) {

		RestTemplate restTemplate = new RestTemplate();
		String url = ipAddress + "/v1.0/user/query/phone";
		MultiValueMap<String, String> requestEntity = new LinkedMultiValueMap<String, String>();
		requestEntity.add("phone", phone);
		requestEntity.add("brandId", brandId);
		String result = restTemplate.postForObject(url, requestEntity, String.class);
		LOG.info("RESULT================" + result);
		JSONObject jsonObject;
		JSONObject resultObj;
		long userId;
		try {
			jsonObject = JSONObject.fromObject(result);
			resultObj = jsonObject.getJSONObject("result");
			userId = resultObj.getLong("id");
		} catch (Exception e) {
			LOG.error("根据手机号查询用户信息失败=============================", e);

			return ResultWrap.init(CommonConstants.FALIED, "根据手机号查询用户信息失败,请确认手机号是否正确!");
		}

		url = ipAddress + "/v1.0/user/bank/default/userid";
		MultiValueMap<String, String> multiValueMap = new LinkedMultiValueMap<String, String>();
		multiValueMap.add("user_id", userId + "");
		result = restTemplate.postForObject(url, multiValueMap, String.class);
		LOG.info("接口/v1.0/user/bank/default/userid====RESULT=========" + result);
		String idCard;
		try {
			jsonObject = JSONObject.fromObject(result);
			String respCode = jsonObject.getString("resp_code");
			if ("000000".equals(respCode)) {
				resultObj = jsonObject.getJSONObject("result");
				idCard = resultObj.getString("idcard");
			} else {

				return ResultWrap.init(CommonConstants.FALIED, "该通道需绑定默认提现借记卡!");
			}
		} catch (Exception e) {
			LOG.error("查询默认结算卡出错======" + e);

			return ResultWrap.init(CommonConstants.FALIED, "查询默认结算卡有误");
		}

		String mainCustomerNumber;
		String keys;
		String customerNumber;
		if ("1".equals(channel)) {
			LOG.info("易宝银联5查询余额======");
			mainCustomerNumber = Conts.customerNumber;
			keys = "oF34lTpB9x9v05D2B0eP1r18EDX71THlT4Go5X0s6V7T85gh2J63j30iPh38";

			RegisterAuth registerAuthByIdCard = registerAuthBusiness.getRegisterAuthByIdCard(idCard);
			customerNumber = registerAuthByIdCard.getCustomerNumber();

		} else {
			LOG.info("易宝银联4查询余额======");
			mainCustomerNumber = "10015053457";
			keys = "hf6Kjql0340f2769N82CCAlj0k23570W2uGP8Z2V4qeF9Z2B941hmio7K65w";

			YBQuickRegister ybQuickRegisterByIdCard = topupPayChannelBusiness.getYBQuickRegisterByIdCard(idCard);
			customerNumber = ybQuickRegisterByIdCard.getCustomerNum();

		}

		param1[0].setValue(mainCustomerNumber);
		param1[1].setValue(customerNumber);
		param1[2].setValue(balanceType);

		StringBuilder hmacStr = new StringBuilder();
		for (NameValuePair nameValuePair : param1) {
			if (nameValuePair.getName().equals("hmac")) {
				continue;
			}
			hmacStr.append(nameValuePair.getValue() == null ? "" : nameValuePair.getValue());

		}

		String hmac = Digest.hmacSign(hmacStr.toString(), keys);

		LOG.info("hmac====" + hmac);

		param1[param1.length - 1].setValue(hmac);
		PostMethod postMethod = new PostMethod("https://skb.yeepay.com/skb-app/customerBalanceQuery.action");
		HttpClient client = new HttpClient();
		postMethod.addRequestHeader("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");

		postMethod.setRequestBody(param1);
		String code = null;
		String balance = null;
		String message = null;
		int status2;
		try {
			status2 = client.executeMethod(postMethod);
			LOG.info("==========status2==========" + status2);
			String response = postMethod.getResponseBodyAsString();
			LOG.info("==========response==========" + response);

			JSONObject fromObject = JSONObject.fromObject(response);

			code = (String) fromObject.get("code");
			balance = (String) fromObject.get("balance");
			message = fromObject.getString("message");
		} catch (IOException e) {
			e.printStackTrace();
			LOG.error("",e);
		}
		String balanceName = "账户余额";
		if ("0000".equals(code)) {

			if ("1".equals(balanceType)) {
				balanceName = "T0 自助结算可用余额";
			}
			if ("2".equals(balanceType)) {
				balanceName = "T1自助结算可用余额";
			}

			Map<String,Object> map = new HashMap<String, Object>();
			map.put("phone", phone);
			map.put("brandId", brandId);
			map.put("channel", channel);
			map.put("balanceType", balanceType);
			map.put("balanceName", balanceName);
			map.put("balance", balance);
			
			return ResultWrap.init(CommonConstants.SUCCESS, "查询成功", map);
		} else {

			return ResultWrap.init(CommonConstants.FALIED, message);
		}

	}
	
	// 易宝新的手动出款接口
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentchannel/topup/ybtransfer/byhand")
	public @ResponseBody Object ybTransferByHand(HttpServletRequest req,
			@RequestParam(value = "realAmount") String realAmount, 
			@RequestParam(value = "phone") String phone,
			@RequestParam(value = "brandId") String brandId,
			@RequestParam(value = "channel", required = false, defaultValue = "1") String channel,
			@RequestParam(value = "balanceType", required = false, defaultValue = "1") String balanceType, 
			@RequestParam(value = "transferWay", required = false, defaultValue = "-1") String transferWay //transferWay为1代表T0出款,transferWay为2代表T1出款
			) throws IOException {

		RestTemplate restTemplate = new RestTemplate();

		String url = ipAddress + "/v1.0/user/query/phone";
		MultiValueMap<String, String> requestEntity = new LinkedMultiValueMap<String, String>();
		requestEntity.add("phone", phone);
		requestEntity.add("brandId", brandId);
		String result = restTemplate.postForObject(url, requestEntity, String.class);
		LOG.info("RESULT================" + result);
		JSONObject jsonObject;
		JSONObject resultObj;
		long userId;
		try {
			jsonObject = JSONObject.fromObject(result);
			resultObj = jsonObject.getJSONObject("result");
			userId = resultObj.getLong("id");
		} catch (Exception e) {
			LOG.error("根据手机号查询用户信息失败=============================", e);

			return ResultWrap.init(CommonConstants.FALIED, "根据手机号查询用户信息失败,请确认手机号是否正确!");
		}

		url = ipAddress + "/v1.0/user/bank/default/userid";
		MultiValueMap<String, String> multiValueMap = new LinkedMultiValueMap<String, String>();
		multiValueMap.add("user_id", userId + "");
		result = restTemplate.postForObject(url, multiValueMap, String.class);
		LOG.info("接口/v1.0/user/bank/default/userid====RESULT=========" + result);
		String idCard;
		try {
			jsonObject = JSONObject.fromObject(result);
			String respCode = jsonObject.getString("resp_code");
			if ("000000".equals(respCode)) {
				resultObj = jsonObject.getJSONObject("result");
				idCard = resultObj.getString("idcard");
			} else {

				return ResultWrap.init(CommonConstants.FALIED, "该通道需绑定默认提现借记卡!");
			}
		} catch (Exception e) {
			LOG.error("查询默认结算卡出错======" + e);

			return ResultWrap.init(CommonConstants.FALIED, "查询默认结算卡有误");
		}

		String mainCustomerNumber;
		String keys;
		String customerNumber;
		if ("1".equals(channel)) {
			LOG.info("易宝银联5出款======");
			mainCustomerNumber = Conts.customerNumber;
			keys = "oF34lTpB9x9v05D2B0eP1r18EDX71THlT4Go5X0s6V7T85gh2J63j30iPh38";
			
			RegisterAuth registerAuthByIdCard = registerAuthBusiness.getRegisterAuthByIdCard(idCard);
			customerNumber = registerAuthByIdCard.getCustomerNumber();
			
		} else {
			LOG.info("易宝银联4出款======");
			mainCustomerNumber = "10015053457";
			keys = "hf6Kjql0340f2769N82CCAlj0k23570W2uGP8Z2V4qeF9Z2B941hmio7K65w";
			
			YBQuickRegister ybQuickRegisterByIdCard = topupPayChannelBusiness.getYBQuickRegisterByIdCard(idCard);
			customerNumber = ybQuickRegisterByIdCard.getCustomerNum();
			
		}

		if("-1".equals(transferWay)) {
			if("3".equals(balanceType)) {
				transferWay = "2";
			}else {
				transferWay = balanceType;
			}
		}
		
		param[0].setValue(realAmount);
		param[1].setValue(customerNumber);
		param[2].setValue(UUIDGenerator.getUUID().substring(0, 20));
		param[3].setValue(mainCustomerNumber);
		param[4].setValue(transferWay);
		param[5].setValue(ipAddress + "/v1.0/paymentchannel/topup/yb/notify_call");
		
		StringBuilder hmacStr = new StringBuilder();
		for (NameValuePair nameValuePair : param) {
			if (nameValuePair.getName().equals("hmac")) {
				continue;
			}
			hmacStr.append(nameValuePair.getValue() == null ? ""
					: nameValuePair.getValue());
			
		}
		
		String hmac = Digest.hmacSign(hmacStr.toString(), keys);
		
		LOG.info("hmac=" + hmac);
		
		param[param.length - 1].setValue(hmac);

		LOG.info("出款的请求报文======" + param);

		PostMethod postMethod = new PostMethod("https://skb.yeepay.com/skb-app/withDrawApi.action");
		HttpClient client = new HttpClient();
		postMethod.addRequestHeader("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
		try {
			postMethod.setRequestBody(param);
			int status2 = client.executeMethod(postMethod);
			LOG.info("==========结算status2==========" + status2);
			String backinfo = postMethod.getResponseBodyAsString();
			LOG.info("==========结算backinfo==========" + backinfo);
			if (status2 == HttpStatus.SC_OK) {
				JSONObject obj = JSONObject.fromObject(backinfo);
				if (obj.getString("code").equals("0000")) {
					return ResultWrap.init(CommonConstants.SUCCESS, "出款成功!");
				} else {
					status2 = client.executeMethod(postMethod);
					backinfo = postMethod.getResponseBodyAsString();
					LOG.info("==========结算backinfo==========" + backinfo);
					obj = JSONObject.fromObject(backinfo);
					if (obj.getString("code").equals("0000")) {
						return ResultWrap.init(CommonConstants.SUCCESS, "出款成功!");
					} else {
						String message = obj.getString("message");

						return ResultWrap.init(CommonConstants.FALIED, message);
					}
				}
			} else {
				return ResultWrap.init(CommonConstants.FALIED, "出款失败!");
			}
		} catch (Exception e) {
			e.printStackTrace();
			LOG.error("",e);
			return ResultWrap.init(CommonConstants.FALIED, "出款失败!");
		} finally {
			// 释放连接
			postMethod.releaseConnection();
		}
	}
	
	
	//银联5出款接口
	@RequestMapping(method=RequestMethod.POST,value="/v1.0/paymentchannel/topup/yb/byhand")
	public @ResponseBody String notifycall(HttpServletRequest req, 
			@RequestParam(value = "realAmount") String realAmount,
			@RequestParam(value = "customerNumber") String customerNumber) throws IOException{
						/*Object result = ras.queryByMobile(req, mobile);
						JSONObject json = JSONObject.fromObject(result);
						System.out.println(json);
						JSONObject objj = json.getJSONObject("result");
						System.out.println(objj);
						String customerNumber = objj.getString("customerNumber");*/
						String mainCustomerNumber = Conts.customerNumber; // 代理商编码
						//String mainCustomerNumber = "10015053457";
						param[0].setValue(realAmount);
						param[1].setValue(customerNumber);
						param[2].setValue(UUIDGenerator.getUUID().substring(0, 20));
						param[3].setValue(mainCustomerNumber);
						param[4].setValue("1");
						param[5].setValue(ipAddress+"/v1.0/paymentchannel/topup/yb/notify_call");
						param[param.length - 1].setValue(hmacSign());
						
						LOG.info("出款的请求报文======" + param);
						
						PostMethod postMethod = new PostMethod("https://skb.yeepay.com/skb-app/withDrawApi.action");
						HttpClient client = new HttpClient();
						postMethod.addRequestHeader("Content-Type","application/x-www-form-urlencoded; charset=UTF-8");
						try {
							postMethod.setRequestBody(param);
							int status2 = client.executeMethod(postMethod);
							LOG.info("==========结算status2=========="+status2);
							String backinfo = postMethod.getResponseBodyAsString();
							LOG.info("==========结算backinfo=========="+backinfo);
							if (status2 == HttpStatus.SC_OK) {
								JSONObject obj = JSONObject.fromObject(backinfo);
								if(obj.getString("code").equals("0000")) {
									return "出款成功";
								}else {
									status2 = client.executeMethod(postMethod);
									backinfo = postMethod.getResponseBodyAsString();
									LOG.info("==========结算backinfo=========="+backinfo);
									obj = JSONObject.fromObject(backinfo);
									if(obj.getString("code").equals("0000")) {
										return "出款成功";
									}else {
										String message = obj.getString("message");
										
										return message;
									}
								}
							} else {
								return "出款失败";
							}
						} catch (Exception e) {
							e.printStackTrace();LOG.error("",e);
							return "出款失败";
						} finally {
							// 释放连接
							postMethod.releaseConnection();
						}
	}
	
	
	//银联4出款接口
	@RequestMapping(method=RequestMethod.POST,value="/v1.0/paymentchannel/topup/ybnew/byhand")
	public @ResponseBody String ybNewPay(HttpServletRequest req, 
			@RequestParam(value = "realAmount") String realAmount,
			@RequestParam(value = "mobile") String mobile) throws IOException{
						
						YBQuickRegister ybQuickRegister = topupPayChannelBusiness.getYBQuickRegisterByPhone(mobile);
		
						String mainCustomerNumber = "10015053457"; // 代理商编码
						param[0].setValue(realAmount);
						param[1].setValue(ybQuickRegister.getCustomerNum());
						param[2].setValue(UUIDGenerator.getUUID().substring(0, 20));
						param[3].setValue(mainCustomerNumber);
						param[4].setValue("1");
						param[5].setValue(ipAddress+"/v1.0/paymentchannel/topup/yb/notify_call");
						param[param.length - 1].setValue(hmacSign2());
						PostMethod postMethod = new PostMethod("https://skb.yeepay.com/skb-app/withDrawApi.action");
						HttpClient client = new HttpClient();
						postMethod.addRequestHeader("Content-Type","application/x-www-form-urlencoded; charset=UTF-8");
						try {
							postMethod.setRequestBody(param);
							int status2 = client.executeMethod(postMethod);
							LOG.info("==========结算status2=========="+status2);
							String backinfo = postMethod.getResponseBodyAsString();
							LOG.info("==========结算backinfo=========="+backinfo);
							if (status2 == HttpStatus.SC_OK) {
								JSONObject obj = JSONObject.fromObject(backinfo);
								if(obj.getString("code").equals("0000")) {
									return "出款成功";
								}else {
									status2 = client.executeMethod(postMethod);
									backinfo = postMethod.getResponseBodyAsString();
									LOG.info("==========结算backinfo=========="+backinfo);
									obj = JSONObject.fromObject(backinfo);
									if(obj.getString("code").equals("0000")) {
										return "出款成功";
									}else {
										return "出款失败";
									}
								}
							} else {
								return "出款失败";
							}
						} catch (Exception e) {
							e.printStackTrace();LOG.error("",e);
							return "出款失败";
						} finally {
							// 释放连接
							postMethod.releaseConnection();
						}
	}
	
	
	/**
	 * 签名
	 *
	 * @return
	 */
	private static String hmacSign() {
		StringBuilder hmacStr = new StringBuilder();
		for (NameValuePair nameValuePair : param) {
			if (nameValuePair.getName().equals("hmac")) {
				continue;
			}
			hmacStr.append(nameValuePair.getValue() == null ? ""
					: nameValuePair.getValue());
			
		}
		
		System.out.println("===============");
		System.out.println("hmacStr.toString()=" + hmacStr.toString());
		System.out.println("===============");
		
		String hmac = Digest.hmacSign(hmacStr.toString(), key);
		
		System.out.println("===============");
		System.out.println("hmac=" + hmac);
		System.out.println("===============");
		
		return hmac;
	}
	
	private static String hmacSign2() {
		StringBuilder hmacStr = new StringBuilder();
		for (NameValuePair nameValuePair : param) {
			if (nameValuePair.getName().equals("hmac")) {
				continue;
			}
			hmacStr.append(nameValuePair.getValue() == null ? ""
					: nameValuePair.getValue());
			
		}
		
		System.out.println("===============");
		System.out.println("hmacStr.toString()=" + hmacStr.toString());
		System.out.println("===============");
		
		String hmac = Digest.hmacSign(hmacStr.toString(), Key);
		
		System.out.println("===============");
		System.out.println("hmac=" + hmac);
		System.out.println("===============");
		
		return hmac;
	}
	
}


