package com.jh.user.service;

import cn.jh.common.tools.ResultWrap;
import cn.jh.common.utils.AuthorizationHandle;
import cn.jh.common.utils.CommonConstants;
import cn.jh.common.utils.TokenUtil;
import com.jh.user.business.UserBankCardLimitBusiness;
import com.jh.user.business.UserBankInfoBusiness;
import com.jh.user.business.UserLoginRegisterBusiness;
import com.jh.user.pojo.*;
import com.jh.user.util.HttpUtils;
import com.jh.user.util.Util;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.commons.codec.binary.Base64;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URI;
import java.util.*;

@Controller
@EnableAutoConfiguration
public class UserBankInfoService {

	private static final Logger LOG = LoggerFactory.getLogger(UserBankInfoService.class);

	@Autowired
	private UserBankInfoBusiness userBankInfoBusiness;
	@Autowired

	private UserBankCardLimitBusiness userBankCardLimitBusiness;

	@Autowired
	private UserLoginRegisterBusiness userLoginRegisterBusiness;

	@Autowired
	Util util;

	@Autowired
	RestTemplate restTemplate;
	
	@Autowired
	StringRedisTemplate redisTemplate;

	// 根据条件查询出bankName 李梦珂
	@ResponseBody
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/user/payment/query/querysupportbanknamebyparams")
	public Object queryBankNameByUserIdAndCardNo(HttpServletRequest request,
			@RequestParam(value = "user_id") long userId, @RequestParam(value = "card_no") String cardNo,
			@RequestParam(value = "type", defaultValue = "2") String type) {

		Map map = new HashMap();
		UserBankInfo userbankinfo = null;

		userbankinfo = userBankInfoBusiness.queryBankNameByUserIdAndCardNo(userId, cardNo, type);
		if (userbankinfo != null) {
			map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
			map.put(CommonConstants.RESP_MESSAGE, "成功");
			map.put(CommonConstants.RESULT, userbankinfo);

		} else {
			map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			map.put(CommonConstants.RESP_MESSAGE, "未查询到银行卡名称信息！");
		}

		return map;
	}

	/** 根据银行卡号修改用户信息 */
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/user/bank/update/cardno")
	public @ResponseBody void updateUserBankInfobyCardno(HttpServletRequest request,
			@RequestParam("province") String province, @RequestParam("city") String city,
			@RequestParam("bankBranchName") String bankBranchName, @RequestParam("bankBranchId") String lineNo,
			@RequestParam("securityCode") String securityCode, @RequestParam("expiredTime") String expiredTime,
			@RequestParam("bankno") String bankno) {
		userBankInfoBusiness.updateUserBankInfoByCardno(bankBranchName, province, city, lineNo, securityCode,
				expiredTime, bankno);
	}

	// 根据cardNo更改用户信息
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/user/bank/update/bynewcardno")
	public @ResponseBody Object updateUserBankInfoByNewCarno(HttpServletRequest request,
			@RequestParam(value = "cardNo") String cardno,
			@RequestParam(value = "type", defaultValue = "2", required = false) String type,
			@RequestParam(value = "province", required = false) String province,
			@RequestParam(value = "city", required = false) String city,
			@RequestParam(value = "bankBranchName", required = false) String bankBranchName,
			@RequestParam(value = "lineNo", required = false) String lineNo,
			@RequestParam(value = "securityCode", required = false) String securityCode,
			@RequestParam(value = "expiredTime", required = false) String expiredTime) {

		Map<String, Object> map = new HashMap<String, Object>();
		try {
			List<UserBankInfo> bankInfos = userBankInfoBusiness.queryUserBankInfoByCardno(cardno, type);
			for (UserBankInfo bankInfo : bankInfos) {
				if (province == null) {
					bankInfo.setProvince(bankInfo.getProvince());
				} else {
					bankInfo.setProvince(province);
				}

				if (city == null) {
					bankInfo.setCity(bankInfo.getCity());
				} else {
					bankInfo.setCity(city);
				}

				if (bankBranchName == null) {
					bankInfo.setBankBranchName(bankInfo.getBankBranchName());
				} else {
					bankInfo.setBankBranchName(bankBranchName);
				}

				if (lineNo == null) {
					bankInfo.setLineNo(bankInfo.getLineNo());
				} else {
					bankInfo.setLineNo(lineNo);
				}

				if (securityCode == null) {
					bankInfo.setSecurityCode(bankInfo.getSecurityCode());
				} else {
					bankInfo.setSecurityCode(securityCode);
				}

				if (expiredTime == null) {
					bankInfo.setExpiredTime(bankInfo.getExpiredTime());
				} else {
					bankInfo.setExpiredTime(expiredTime);
				}
				userBankInfoBusiness.saveUserBankInfo(bankInfo);
			}
			map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
			map.put(CommonConstants.RESP_MESSAGE, "数据添加成功");
		} catch (Exception e) {
			map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			map.put(CommonConstants.RESP_MESSAGE, "数据添加失败");
			LOG.info(e.getMessage());
		}
		return map;
	}

	/** 根据用户的id获取用户的银行卡信息
     *
     * 2019.10.11 添加了返回账单日和还款日和信用额度
     * */
	@RequestMapping(method = RequestMethod.GET, value = "/v1.0/user/bank/query/userid/{token}")
	public @ResponseBody Object queryBankInfoByUserid(HttpServletRequest request, @PathVariable("token") String token) {

		Map<String, Object> map = new HashMap<String, Object>();
		String channelTag = "";
		String type = "";
		channelTag = request.getParameter("channel_tag");
		type = request.getParameter("type");
		long userId;
		long brandId;
		try {
			userId = TokenUtil.getUserId(token);
			brandId = TokenUtil.getBrandid(token);
		} catch (Exception e) {
			map.put(CommonConstants.RESP_CODE, CommonConstants.ERROR_TOKEN);
			map.put(CommonConstants.RESP_MESSAGE, "token无效");
			return map;
		}
		/*
		 * LOG.info("我是传的参数token=========="+token);
		 * LOG.info("我是传的参数channelTag=========="+channelTag);
		 * LOG.info("我是传的参数type=========="+type);
		 */

		if ((null == channelTag || "".equals(channelTag) || "null".equals(channelTag))
				&& ("".equals(type) || null == type || "null".equals(type))) {
			LOG.info("没有传channelTag和type==============");
			map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
			map.put(CommonConstants.RESP_MESSAGE, "成功");
			map.put(CommonConstants.RESULT, userBankInfoBusiness.queryUserBankInfoByUserid(userId));
		} else {
			LOG.info("传channelTag和type======" + channelTag + "============" + type);
			RestTemplate restTemplate = new RestTemplate();
			URI uri = util.getServiceUrl("paymentchannel", "error url request!");
			String url = uri.toString() + "/v1.0/paymentchannel/pay/query/supportbank";
			MultiValueMap<String, String> requestEntity = new LinkedMultiValueMap<String, String>();
			requestEntity.add("user_id", userId + "");
			requestEntity.add("brand_id", brandId + "");
			requestEntity.add("channel_tag", channelTag);
			requestEntity.add("type", type);
			String result = "";
			JSONArray resultObj = null;
			try {
				result = restTemplate.postForObject(url, requestEntity, String.class);
				JSONObject jsonObject = JSONObject.fromObject(result);

				if (!"000000".equals(jsonObject.getString("resp_code"))) {
					map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
					map.put(CommonConstants.RESP_MESSAGE, jsonObject.containsKey(CommonConstants.RESP_MESSAGE)
							? jsonObject.getString(CommonConstants.RESP_MESSAGE) : "亲,您绑定的卡暂不支持该通道哦!");
					return map;
				}

				resultObj = jsonObject.getJSONArray("result");
			} catch (Exception e) {
				LOG.error("选择通道支持的银行卡出现空值======" + e.getMessage());
				map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
				map.put(CommonConstants.RESP_MESSAGE, "您未添加银行卡信息，请前往添加！！");
			}
			List<Map> listMap = new ArrayList<Map>();
			String oldstr = "\"";
			String newstr = "";
			for (int i = 0; i < resultObj.size(); i++) {
				Map<String, Object> map1 = new HashMap<String, Object>();
				JSONObject jsonObject2 = resultObj.getJSONObject(i);
				map1.put("id", jsonObject2.getString("id").replace(oldstr, newstr));
				map1.put("userId", jsonObject2.getString("userId").replace(oldstr, newstr));
				map1.put("bankBranchName", jsonObject2.getString("bankBranchName").replace(oldstr, newstr));
				map1.put("province", jsonObject2.getString("province").replace(oldstr, newstr));
				map1.put("city", jsonObject2.getString("city").replace(oldstr, newstr));
				map1.put("userName", jsonObject2.getString("userName").replace(oldstr, newstr));
				map1.put("bankName", jsonObject2.getString("bankName").replace(oldstr, newstr));
				map1.put("bankBrand", jsonObject2.getString("bankBrand").replace(oldstr, newstr));
				map1.put("cardNo", jsonObject2.getString("cardNo").replace(oldstr, newstr));
				map1.put("lineNo", jsonObject2.getString("lineNo").replace(oldstr, newstr));
				map1.put("securityCode", jsonObject2.getString("securityCode").replace(oldstr, newstr));
				map1.put("expiredTime", jsonObject2.getString("expiredTime").replace(oldstr, newstr));
				map1.put("phone", jsonObject2.getString("phone").replace(oldstr, newstr));
				map1.put("idcard", jsonObject2.getString("idcard").replace(oldstr, newstr));
				map1.put("cardType", jsonObject2.getString("cardType").replace(oldstr, newstr));
				map1.put("priOrPub", jsonObject2.getString("priOrPub").replace(oldstr, newstr));
				map1.put("nature", jsonObject2.getString("nature").replace(oldstr, newstr));
				map1.put("state", jsonObject2.getString("state").replace(oldstr, newstr));
				map1.put("idDef", jsonObject2.getString("idDef").replace(oldstr, newstr));
				map1.put("logo", jsonObject2.getString("logo").replace(oldstr, newstr));
				map1.put("type", jsonObject2.getString("type").replace(oldstr, newstr));
				map1.put("createTime", jsonObject2.getString("createTime").replace(oldstr, newstr));
				map1.put("useState", jsonObject2.getString("useState").replace(oldstr, newstr));
//				map1.put("billDay", jsonObject2.getString("billDay").replace(oldstr, newstr));
//				map1.put("repaymentDay", jsonObject2.getString("repaymentDay").replace(oldstr, newstr));
//				map1.put("creditBlance", jsonObject2.getString("creditBlance").replace(oldstr, newstr));
				listMap.add(map1);
			}
			map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
			map.put(CommonConstants.RESP_MESSAGE, "成功");
			map.put(CommonConstants.RESULT, listMap);
		}

		return map;
	}

	/** 根据用户的id获取用户的默认卡信息 */
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/user/bank/default/userid")
	public @ResponseBody Object queryDefBankInfoByUserid(HttpServletRequest request,
			@RequestParam(value = "user_id") String userid) {
		Map<String, Object> map = new HashMap<String, Object>();
		UserBankInfo model = null;
		try {
			model = userBankInfoBusiness.queryDefUserBankInfoByUserid(Long.parseLong(userid));
		} catch (Exception e) {
			LOG.error("查询结算卡异常");
			e.printStackTrace();
			LOG.error("", e);
			map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			map.put(CommonConstants.RESP_MESSAGE, "您在系统内没有绑定默认结算卡(储蓄卡),请绑定默认结算卡(储蓄卡)继续交易!");
			return map;
		}
		if (model == null) {
			map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			map.put(CommonConstants.RESP_MESSAGE, "您在系统内没有绑定默认结算卡(储蓄卡),请绑定默认结算卡(储蓄卡)继续交易!");
			return map;
		}

		map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
		map.put(CommonConstants.RESP_MESSAGE, "成功");
		map.put(CommonConstants.RESULT, model);
		return map;

	}

	/** 根据用户的id获取用户的银行卡信息 */
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/user/bank/query/userid/type/{token}")
	public @ResponseBody Object queryBankInfoByUseridType(HttpServletRequest request,
			@PathVariable("token") String token, @RequestParam(value = "type") String type) {
		Map<String, Object> map = new HashMap<String, Object>();
		long userId;
		try {
			userId = TokenUtil.getUserId(token);
		} catch (Exception e) {
			map.put(CommonConstants.RESP_CODE, CommonConstants.ERROR_TOKEN);
			map.put(CommonConstants.RESP_MESSAGE, "token无效");
			return map;
		}
		map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
		map.put(CommonConstants.RESP_MESSAGE, "成功");
		map.put(CommonConstants.RESULT, userBankInfoBusiness.queryUserBankInfoByUserid(userId, type));
		return map;
	}

	/** 根据用户的id获取用户的银行卡信息 */
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/user/bank/query/useridandtype")
	public @ResponseBody Object queryBankInfoByUseridAndType(HttpServletRequest request,
			@RequestParam(value = "userId") String userId, @RequestParam(value = "type") String type) {

		List<UserBankInfo> queryUserBankInfoByUserid = userBankInfoBusiness
				.queryUserBankInfoByUserid(Long.parseLong(userId), type);

		Map map = new HashMap();
		if (queryUserBankInfoByUserid != null) {
			map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
			map.put(CommonConstants.RESP_MESSAGE, "成功");
			map.put(CommonConstants.RESULT, queryUserBankInfoByUserid);
			return map;
		} else {
			map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			map.put(CommonConstants.RESP_MESSAGE, "您未添加银行卡信息，请前往添加！！");
			return map;
		}

	}

	/** 根据卡号信息获取用户提现的银行卡信息 */
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/user/bank/query/cardno/{token}")
	public @ResponseBody Object queryBankInfoByCardno(HttpServletRequest request, @PathVariable("token") String token,
			@RequestParam(value = "type", defaultValue = "2", required = false) String type,
			@RequestParam(value = "cardno") String cardno) {
		Map<String, Object> map = new HashMap<String, Object>();
		long userId;
		try {
			userId = TokenUtil.getUserId(token);
		} catch (Exception e) {
			map.put(CommonConstants.RESP_CODE, CommonConstants.ERROR_TOKEN);
			map.put(CommonConstants.RESP_MESSAGE, "token无效");
			return map;
		}
		UserBankInfo userBankInfo = userBankInfoBusiness.queryBankNameByUserIdAndCardNo(userId, cardno, type);
		map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
		map.put(CommonConstants.RESP_MESSAGE, "成功");
		map.put(CommonConstants.RESULT, userBankInfo);
		return map;

	}

	/** 根据用户的id获取用户的默认提现卡信息 */
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/user/bank/default/cardno")
	public @ResponseBody Object queryDefBankInfoByCardno(HttpServletRequest request,
			@RequestParam(value = "type", defaultValue = "2", required = false) String type,
			@RequestParam(value = "cardno") String cardno) {

		List<UserBankInfo> queryUserBankInfoByCardno = userBankInfoBusiness.queryUserBankInfoByCardno(cardno, type);
		UserBankInfo userBankInfo = null;
		if (queryUserBankInfoByCardno != null && queryUserBankInfoByCardno.size() > 0) {
			userBankInfo = queryUserBankInfoByCardno.get(0);
		}
		Map<String, Object> map = new HashMap<>();
		map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
		map.put(CommonConstants.RESP_MESSAGE, "成功");
		map.put(CommonConstants.RESULT, userBankInfo);
		return map;

	}

	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/user/bank/default/cardnoand/type")
	public @ResponseBody Object queryDefBankInfoByCardnoAndType(HttpServletRequest request,
			@RequestParam(value = "type", defaultValue = "2", required = false) String type,
			@RequestParam(value = "cardno") String cardno,
			@RequestParam(value = "userId", required = false, defaultValue = "0") long userId) {

		List<UserBankInfo> queryUserBankInfoByCardno = userBankInfoBusiness
				.queryUserBankInfoByCardnoAndTypeAndUserId(cardno, type, userId);
		UserBankInfo userBankInfo = null;
		if (queryUserBankInfoByCardno != null && queryUserBankInfoByCardno.size() > 0) {
			userBankInfo = queryUserBankInfoByCardno.get(0);
		}
		Map<String, Object> map = new HashMap<>();
		map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
		map.put(CommonConstants.RESP_MESSAGE, "成功");
		map.put(CommonConstants.RESULT, userBankInfo);
		return map;

	}

	/** 根据联行号信息获取用户的信息 */
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/user/bank/query/lineno/{token}")
	public @ResponseBody Object queryBankInfoByLineno(HttpServletRequest request, @PathVariable("token") String token,
			@RequestParam(value = "lineno") String lineno) {
		Map map = new HashMap();
		long userId;
		try {
			userId = TokenUtil.getUserId(token);
		} catch (Exception e) {
			map.put(CommonConstants.RESP_CODE, CommonConstants.ERROR_TOKEN);
			map.put(CommonConstants.RESP_MESSAGE, "token无效");
			return map;
		}

		map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
		map.put(CommonConstants.RESP_MESSAGE, "成功");
		map.put(CommonConstants.RESULT, userBankInfoBusiness.queryUserBankInfoByLineno(lineno));

		return map;

	}

	/**
	 * 新增一张银行卡并且验卡
	 * 2019.10.10 什么东西
	 *
	 * */
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/user/bank/add/{token}")
	public @ResponseBody Object addBankInfo(HttpServletRequest request, @PathVariable("token") String token,
											@RequestParam(value = "realname") String realname, @RequestParam(value = "idcard") String idcard,
											@RequestParam(value = "bankcard") String bankcard, @RequestParam(value = "mobile") String mobile,
											@RequestParam(value = "type", defaultValue = "2", required = false) String type,
											@RequestParam(value = "expiretime", required = false) String expiretime,
											@RequestParam(value = "securitycode", required = false) String securitycode,
											@RequestParam(value = "billDay", required = false, defaultValue = "-1") String billDayStr,
											@RequestParam(value = "repaymentDay", required = false, defaultValue = "-1") String repaymentDayStr,
											@RequestParam(value = "creditBlance", required = false, defaultValue = "0") String creditBlanceStr,
											@RequestParam(value="round",required = false,defaultValue = "0")String round) {
		LOG.info("添加信用卡请求信息--------realname:" + realname + "--------idcard:" + idcard + "--------bankcard:" + bankcard + "--------mobile:" + mobile
				+ "--------type:" + type + "--------round:" + round
				+ "--------expiretime:" + expiretime + "--------securitycode:" + securitycode + "--------billDayStr:" + billDayStr
				+ "--------repaymentDayStr:" + repaymentDayStr + "--------creditBlanceStr:" + creditBlanceStr);
		Map<String, Object> map = new HashMap<>();
		if ("".equalsIgnoreCase(realname) || realname == null ||
				"".equalsIgnoreCase(idcard) || idcard == null ||
				"".equalsIgnoreCase(bankcard) || bankcard == null ||
				"".equalsIgnoreCase(mobile) || mobile == null ||
				"".equalsIgnoreCase(creditBlanceStr) || creditBlanceStr == null ) {
			map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			map.put(CommonConstants.RESP_MESSAGE, "您输入的信息有误,请重新输入!");
			return map;
		}
		realname = realname.trim();
		idcard = idcard.trim().toUpperCase();
		bankcard = bankcard.trim();
		mobile = mobile.trim();
		JSONObject jsonObject = null;
		Map<String, String> bank = new HashMap<>();
		bank.put("中国光大银行", "光大银行");
		bank.put("中国民生银行", "民生银行");
		bank.put("中国建设银行", "建设银行");
		bank.put("中国工商银行", "工商银行");
		bank.put("中国农业银行", "农业银行");
		bank.put("中国邮政储蓄银行", "邮政储蓄银行");
		bank.put("浦东发展银行", "浦发银行");
		bank.put("上海浦东发展银行", "浦发银行");
		bank.put("中国广发银行", "广发银行");
		bank.put("中国交通银行", "交通银行");
		bank.put("中国平安银行", "平安银行");
		long userId;
		try {
			userId = TokenUtil.getUserId(token);
		} catch (Exception e) {
			map.put(CommonConstants.RESP_CODE, CommonConstants.ERROR_TOKEN);
			map.put(CommonConstants.RESP_MESSAGE, "token无效");
			return map;
		}
		User user = userLoginRegisterBusiness.queryUserById(userId);
		if (!"1".equals(user.getRealnameStatus())) {
			map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			map.put(CommonConstants.RESP_MESSAGE, "绑卡失败,请先进行实名认证!");
			return map;
		}
		{
			String url = "http://paymentchannel/v1.0/paymentchannel/realname/userid";
			MultiValueMap<String, String> requestEntity = new LinkedMultiValueMap<String, String>();
			requestEntity.add("userid", userId+"");
			String result = restTemplate.postForObject(url, requestEntity, String.class);
			LOG.info("RESULT================" + result);
			jsonObject = JSONObject.fromObject(result);
			JSONObject realnameAuth=jsonObject.getJSONObject("realname");
			String realnaemAuth=realnameAuth.getString("realname");
			String idCardAuth=realnameAuth.getString("idcard");
			idCardAuth=idCardAuth.trim().toUpperCase();
			if(!realnaemAuth.equals(realname)||!idCardAuth.equals(idcard)) {
				map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
				map.put(CommonConstants.RESP_MESSAGE, "身份证信息与实名身份信息不一致，请检查！");
				return map;
			}
		}

		int erveyDayBindCardCount = userBankCardLimitBusiness.queryTodayCount(userId);
		int sameBindCardCount = userBankCardLimitBusiness.queryTodySameCount(userId, idcard, bankcard);
		if (erveyDayBindCardCount >= 25) {
			map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			map.put(CommonConstants.RESP_MESSAGE, "绑卡失败,每人每天绑卡次数不能超过25次,请明天再试!");
			return map;
		}
		if (sameBindCardCount >= 5) {
			map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			map.put(CommonConstants.RESP_MESSAGE, "绑卡失败,相同卡号每天只能绑定5次,请明天再试!");
			return map;
		}

		UserBankCardLimit model = new UserBankCardLimit();
		model.setIdcard(idcard);
		model.setUserId(userId);
		model.setBankCard(bankcard);
		model = userBankCardLimitBusiness.save(model);
		/** 先验卡 */
		JSONObject authObject = null;
		JSONObject location = null;
		try {
			if("0".equals(round)){
				// URI uri = util.getServiceUrl("paymentchannel", "error url
				// request!");
				// String url = uri.toString() +
				// "/v1.0/paymentchannel/bankcard4/auth";
				String url = "http://paymentchannel/v1.0/paymentchannel/bankcard4/auth";
				MultiValueMap<String, String> requestEntity = new LinkedMultiValueMap<String, String>();
				requestEntity.add("realname", realname);
				requestEntity.add("idcard", idcard);
				requestEntity.add("bankcard", bankcard);
				requestEntity.add("mobile", mobile);
				requestEntity.add("brandId", user.getBrandId() + "");
				// RestTemplate restTemplate=new RestTemplate();
				String result = restTemplate.postForObject(url, requestEntity, String.class);
				LOG.info("RESULT================" + result);
				jsonObject = JSONObject.fromObject(result);
			}else{//阿里云四要素验证接口
				String url = "http://paymentchannel/v1.0/paymentchannel/bankcard4/auth/backstage";
				MultiValueMap<String, String> requestEntity = new LinkedMultiValueMap<String, String>();
				requestEntity.add("realname", realname);
				requestEntity.add("idcard", idcard);
				requestEntity.add("bankcard", bankcard);
				requestEntity.add("mobile", mobile);
				requestEntity.add("brandId", user.getBrandId() + "");
				// RestTemplate restTemplate=new RestTemplate();
				String result = restTemplate.postForObject(url, requestEntity, String.class);
				LOG.info("RESULT================" + result);
				jsonObject = JSONObject.fromObject(result);
			}
		} catch (Exception e) {
			e.printStackTrace();
			LOG.error("", e);
			map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			map.put(CommonConstants.RESP_MESSAGE, "绑卡失败请稍后重试！！！");
			return map;
		}
		String respCode = jsonObject.getString(CommonConstants.RESP_CODE);
		if (!CommonConstants.SUCCESS.equals(respCode)) {
			LOG.info("绑卡失败了=====================================" + jsonObject);
			map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			map.put(CommonConstants.RESP_MESSAGE, jsonObject.containsKey(CommonConstants.RESP_MESSAGE)
					? jsonObject.getString(CommonConstants.RESP_MESSAGE) : "由于未知原因,验证失败!");
			return map;
		}

		authObject = jsonObject.getJSONObject("auth");
		location = jsonObject.getJSONObject("location");

		if (!location.getString("errorCode").equalsIgnoreCase("0")) {
			map.put(CommonConstants.RESP_CODE, CommonConstants.ERROR_CARD_ERROR);
			map.put(CommonConstants.RESP_MESSAGE,
					location.getString("reason") != null && !location.getString("reason").equals("null")
							? location.getString("reason") : "卡有误");
			return map;
		}

		// bankCard4Auth.setJobid(resObject.getString("jobid"));
		// bankCard4Auth.setMessage(resObject.getString("message"));
		// bankCard4Auth.setResCode(resObject.getInt("res"));
		/** 卡验证有效 */
		if (authObject.getInt("resCode") == 1) {

			if (location.getString("nature").contains("贷")) {
				type = "0";
				if (securitycode != null && (securitycode.length() != 3 || !securitycode.matches("^[0-9]*$"))) {
					map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
					map.put(CommonConstants.RESP_MESSAGE, "输入安全码非法,请重新输入");
					return map;
				}
				try {
					if (expiretime.contains("/") || expiretime.contains("-")) {
						expiretime = expiretime.replace("/", "");
						expiretime = expiretime.replace("-", "");
					}
				} catch (Exception e1) {
					LOG.info("转换信用卡有效期有误======" + e1);
					e1.printStackTrace();
					map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
					map.put(CommonConstants.RESP_MESSAGE, "输入信用卡有效期格式非法,请重新输入!");
					return map;
				}

			}
			/*** 防止重复验卡 */

			/** 根据用户的id设置所有的开都变成非默认 */
			userBankInfoBusiness.updateAllNoDefault(userId, type);
			List<UserBankInfo> userBankInfoTemps = userBankInfoBusiness.queryUserBankInfoByCardnoType(bankcard, type);
			UserBankInfo userBankInfoTemp = null;
			for (UserBankInfo userBankInfo : userBankInfoTemps) {
				if (userId == userBankInfo.getUserId()) {
					userBankInfoTemp = userBankInfo;
				}
			}
			// LOG.info("银行验证有没有存在================"+userBankInfoTemp);
			if (userBankInfoTemp != null && userBankInfoTemp.getCardNo() != null) {
				LOG.info("银行验证有存在进入================");
				userBankInfoTemp.setIdDef("1");
				userBankInfoTemp.setState("0");
				userBankInfoTemp = userBankInfoBusiness.saveUserBankInfo(userBankInfoTemp);
			} else {
				LOG.info("银行验证有不存在进入添加================");
				UserBankInfo bankInfo = new UserBankInfo();
				bankInfo.setCardNo(bankcard);
				bankInfo.setExpiredTime(expiretime);
				bankInfo.setSecurityCode(securitycode);
				bankInfo.setCreateTime(new Date());
				bankInfo.setPhone(mobile);
				bankInfo.setUserId(userId);
				bankInfo.setIdcard(idcard);
				bankInfo.setUserName(realname);
				bankInfo.setType(type);
				bankInfo.setIdDef("1");
				bankInfo.setState("0");
				bankInfo.setCardType(location.getString("type"));

				BigDecimal creditBlance = new BigDecimal(creditBlanceStr);
				int billDate = 0;
				int repaymentDate = 0;

				if (!"-1".equals(billDayStr)) {
					try {
						billDate = Integer.valueOf(billDayStr);
					} catch (NumberFormatException e) {
						map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
						map.put(CommonConstants.RESP_MESSAGE, "输入日期格式非法,请重新输入");
						return map;
					}
					if (!(billDate > 0 && billDate < 32)) {
						map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
						map.put(CommonConstants.RESP_MESSAGE, "输入日期格式非法,请重新输入");
						return map;
					}
				}

				if (!"-1".equals(repaymentDayStr)) {
					try {
						repaymentDate = Integer.valueOf(repaymentDayStr);
					} catch (Exception e) {
						map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
						map.put(CommonConstants.RESP_MESSAGE, "输入日期格式非法,请重新输入");
						return map;
					}
					if (!(repaymentDate > 0 && repaymentDate < 32)) {
						map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
						map.put(CommonConstants.RESP_MESSAGE, "输入日期格式非法,请重新输入");
						return map;
					}
				}

				bankInfo.setBillDay(billDate);
				bankInfo.setRepaymentDay(repaymentDate);
				bankInfo.setCreditBlance(creditBlance);

				if (bank.containsKey(location.getString("bankLocation"))) {
					bankInfo.setBankName((String) bank.get(location.getString("bankLocation")));
				} else {
					bankInfo.setBankName(location.getString("bankLocation"));
				}
				bankInfo.setNature(location.getString("nature"));
				bankInfo.setLogo(location.getString("logo"));
				userBankInfoTemp = userBankInfoBusiness.saveUserBankInfo(bankInfo);
				LOG.info("银行验证有不存在进入添加================完毕");
			}

			map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
			map.put(CommonConstants.RESP_MESSAGE, "卡绑定成功");
			map.put(CommonConstants.RESULT, userBankInfoTemp);
			return map;

		} else {

			map.put(CommonConstants.RESP_CODE, CommonConstants.ERROR_CARD_FAILED);
			map.put(CommonConstants.RESP_MESSAGE,
					authObject.getString("message") != null && !authObject.getString("message").equals("null")
							? authObject.getString("message") : "身份信息不匹配");

			return map;

		}

	}
	
	/**
	 * 新增一张银行卡并且验卡 2019.4.12新增本人手机号码校验
	 * @param request
	 * @param token
	 * @param realname
	 * @param idcard
	 * @param bankcard
	 * @param mobile
	 * @param type
	 * @param expiretime
	 * @param securitycode
	 * @param billDayStr
	 * @param repaymentDayStr
	 * @param creditBlanceStr
	 * @param vericode
	 * @param userPhone
	 * @return
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/user/bank/addNew/{token}")
	public @ResponseBody Object addBankInfoNew(HttpServletRequest request, @PathVariable("token") String token,
			@RequestParam(value = "realname") String realname, @RequestParam(value = "idcard") String idcard,
			@RequestParam(value = "bankcard") String bankcard, @RequestParam(value = "mobile") String mobile,
			@RequestParam(value = "type", defaultValue = "2", required = false) String type,
			@RequestParam(value = "expiretime", required = false) String expiretime,
			@RequestParam(value = "securitycode", required = false) String securitycode,
			@RequestParam(value = "billDay", required = false, defaultValue = "-1") String billDayStr,
			@RequestParam(value = "repaymentDay", required = false, defaultValue = "-1") String repaymentDayStr,
			@RequestParam(value = "creditBlance", required = false, defaultValue = "0") String creditBlanceStr,
			@RequestParam(value = "vericode") String vericode,
			//userPhone 用户登录账号
			@RequestParam(value = "userPhone") String userPhone ) {
		Map<String, Object> map = new HashMap<>();
		if ("".equalsIgnoreCase(realname) || realname == null || "".equalsIgnoreCase(idcard) || idcard == null
				|| "".equalsIgnoreCase(bankcard) || bankcard == null || "".equalsIgnoreCase(mobile) || mobile == null) {
			map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			map.put(CommonConstants.RESP_MESSAGE, "您输入的信息有误,请重新输入!");
			return map;
		}
		realname = realname.trim();
		idcard = idcard.trim().toUpperCase();
		bankcard = bankcard.trim();
		mobile = mobile.trim();
		JSONObject jsonObject = null;
		Map<String, String> bank = new HashMap<>();
		bank.put("中国光大银行", "光大银行");
		bank.put("中国民生银行", "民生银行");
		bank.put("中国建设银行", "建设银行");
		bank.put("中国工商银行", "工商银行");
		bank.put("中国农业银行", "农业银行");
		bank.put("中国邮政储蓄银行", "邮政储蓄银行");
		bank.put("浦东发展银行", "浦发银行");
		bank.put("上海浦东发展银行", "浦发银行");
		bank.put("中国广发银行", "广发银行");
		bank.put("中国交通银行", "交通银行");
		bank.put("中国平安银行", "平安银行");
		long userId;
		try {
			userId = TokenUtil.getUserId(token);
		} catch (Exception e) {
			map.put(CommonConstants.RESP_CODE, CommonConstants.ERROR_TOKEN);
			map.put(CommonConstants.RESP_MESSAGE, "token无效");
			return map;
		}

		//绑卡短信验证码校验 2019.4.12
		/*String vericodeUrl="http://notice/v1.0/notice/sms/vericode?phone="+userPhone; 
		String vericodeResult=restTemplate.getForObject(vericodeUrl, String.class);
		JSONObject jsonObject1=null;
		jsonObject1=JSONObject.fromObject(vericodeResult); 
		//jsonObject1.get(CommonConstants.RESULT);
		String vCode=jsonObject1.getString(CommonConstants.RESULT);
		System.out.println(vCode);
		if(!vCode.equals(vericode)||vCode==null || vCode=="") {
			LOG.info("验证码输入错误=====================================" + vCode);
			map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			map.put(CommonConstants.RESP_MESSAGE, "验证码输入有误"); 
			return map;
		}
		System.out.println("验证成功");
		*/
		//到redis缓存中获取对应用户的验证码
		String vCode=String.valueOf(redisTemplate.opsForValue().get(userPhone));
		LOG.info("验证码===================================>"+vCode);
		if(!vCode.equals(vericode)||vCode==null || vCode=="") {
			LOG.info("验证码输入错误=====================================" + vCode);
			map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			map.put(CommonConstants.RESP_MESSAGE, "验证码输入有误"); 
			return map;
		}
		User user = userLoginRegisterBusiness.queryUserById(userId);
		LOG.info("user="+user);

		if (!"1".equals(user.getRealnameStatus())) {
			map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			map.put(CommonConstants.RESP_MESSAGE, "绑卡失败,请先进行实名认证!");
			return map;
		}
		{
			String url = "http://paymentchannel/v1.0/paymentchannel/realname/userid";
			MultiValueMap<String, String> requestEntity = new LinkedMultiValueMap<String, String>();
			requestEntity.add("userid", userId+"");
			String result = restTemplate.postForObject(url, requestEntity, String.class);
			LOG.info("RESULT================" + result);
			jsonObject = JSONObject.fromObject(result);
			JSONObject realnameAuth=jsonObject.getJSONObject("realname");
			String realnaemAuth=realnameAuth.getString("realname");
			String idCardAuth=realnameAuth.getString("idcard");
			idCardAuth=idCardAuth.trim().toUpperCase();
			if(!realnaemAuth.equals(realname)||!idCardAuth.equals(idcard)) {
				map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
				map.put(CommonConstants.RESP_MESSAGE, "身份证信息与实名身份信息不一致，请检查！");
				return map;
			}
		}

		int erveyDayBindCardCount = userBankCardLimitBusiness.queryTodayCount(userId);
		int sameBindCardCount = userBankCardLimitBusiness.queryTodySameCount(userId, idcard, bankcard);
		if (erveyDayBindCardCount >= 25) {
			map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			map.put(CommonConstants.RESP_MESSAGE, "绑卡失败,每人每天绑卡次数不能超过25次,请明天再试!");
			return map;
		}
		if (sameBindCardCount >= 5) {
			map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			map.put(CommonConstants.RESP_MESSAGE, "绑卡失败,相同卡号每天只能绑定5次,请明天再试!");
			return map;
		}



		//绑卡短信验证码校验 2019.4.11 
		/*SMSRecord send=new SMSRecord();
		//从redis缓存中获取保存的临时验证码
		send= (SMSRecord) redisTemplate.opsForValue().get(userPhone);
		String smsVeriCode=send.getVeriCode();
		if("".equals(vericode)||vericode==null) {
			map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			map.put(CommonConstants.RESP_MESSAGE, "验证码不能为空");
			return map;
		}
		if(!smsVeriCode.equals(vericode)) {
			map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			map.put(CommonConstants.RESP_MESSAGE, "验证码输入错误");
			return map;
		}*/

		UserBankCardLimit model = new UserBankCardLimit();
		model.setIdcard(idcard);
		model.setUserId(userId);
		model.setBankCard(bankcard);
		model = userBankCardLimitBusiness.save(model);
		/** 先验卡 */
		JSONObject authObject = null;
		JSONObject location = null;
		try {
			// URI uri = util.getServiceUrl("paymentchannel", "error url
			// request!");
			// String url = uri.toString() +
			// "/v1.0/paymentchannel/bankcard4/auth";
			String url = "http://paymentchannel/v1.0/paymentchannel/bankcard4/auth";
			MultiValueMap<String, String> requestEntity = new LinkedMultiValueMap<String, String>();
			requestEntity.add("realname", realname);
			requestEntity.add("idcard", idcard);
			requestEntity.add("bankcard", bankcard);
			requestEntity.add("mobile", mobile);
			requestEntity.add("brandId", user.getBrandId() + "");
			// RestTemplate restTemplate=new RestTemplate();
			String result = restTemplate.postForObject(url, requestEntity, String.class);
			LOG.info("RESULT================" + result);
			jsonObject = JSONObject.fromObject(result);
		} catch (Exception e) {
			e.printStackTrace();
			LOG.error("", e);
			map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			map.put(CommonConstants.RESP_MESSAGE, "绑卡失败请稍后重试！！！");
			return map;
		}
		String respCode = jsonObject.getString(CommonConstants.RESP_CODE);
		if (!CommonConstants.SUCCESS.equals(respCode)) {
			LOG.info("绑卡失败了=====================================" + jsonObject);
			map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			map.put(CommonConstants.RESP_MESSAGE, jsonObject.containsKey(CommonConstants.RESP_MESSAGE)
					? jsonObject.getString(CommonConstants.RESP_MESSAGE) : "由于未知原因,验证失败!");
			return map;
		}

		authObject = jsonObject.getJSONObject("auth");
		location = jsonObject.getJSONObject("location");

		if (!location.getString("errorCode").equalsIgnoreCase("0")) {
			map.put(CommonConstants.RESP_CODE, CommonConstants.ERROR_CARD_ERROR);
			map.put(CommonConstants.RESP_MESSAGE,
					location.getString("reason") != null && !location.getString("reason").equals("null")
					? location.getString("reason") : "卡有误");
			return map;
		}

		// bankCard4Auth.setJobid(resObject.getString("jobid"));
		// bankCard4Auth.setMessage(resObject.getString("message"));
		// bankCard4Auth.setResCode(resObject.getInt("res"));
		/** 卡验证有效 */
		if (authObject.getInt("resCode") == 1) {

			if (location.getString("nature").contains("贷")) {
				type = "0";
				if (securitycode != null && (securitycode.length() != 3 || !securitycode.matches("^[0-9]*$"))) {
					map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
					map.put(CommonConstants.RESP_MESSAGE, "输入安全码非法,请重新输入");
					return map;
				}
				try {
					if (expiretime.contains("/") || expiretime.contains("-")) {
						expiretime = expiretime.replace("/", "");
						expiretime = expiretime.replace("-", "");
					}
				} catch (Exception e1) {
					LOG.info("转换信用卡有效期有误======" + e1);
					e1.printStackTrace();
					map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
					map.put(CommonConstants.RESP_MESSAGE, "输入信用卡有效期格式非法,请重新输入!");
					return map;
				}

			}
			/*** 防止重复验卡 */

			/** 根据用户的id设置所有的开都变成非默认 */
			userBankInfoBusiness.updateAllNoDefault(userId, type);
			List<UserBankInfo> userBankInfoTemps = userBankInfoBusiness.queryUserBankInfoByCardnoType(bankcard, type);
			UserBankInfo userBankInfoTemp = null;
			for (UserBankInfo userBankInfo : userBankInfoTemps) {
				if (userId == userBankInfo.getUserId()) {
					userBankInfoTemp = userBankInfo;
				}
			}
			// LOG.info("银行验证有没有存在================"+userBankInfoTemp);
			if (userBankInfoTemp != null && userBankInfoTemp.getCardNo() != null) {
				LOG.info("银行验证有存在进入================");
				userBankInfoTemp.setIdDef("1");
				userBankInfoTemp.setState("0");
				userBankInfoTemp = userBankInfoBusiness.saveUserBankInfo(userBankInfoTemp);
			} else {
				LOG.info("银行验证有不存在进入添加================");
				UserBankInfo bankInfo = new UserBankInfo();
				bankInfo.setCardNo(bankcard);

				bankInfo.setExpiredTime(expiretime);
				bankInfo.setSecurityCode(securitycode);
				bankInfo.setCreateTime(new Date());
				bankInfo.setPhone(mobile);
				bankInfo.setUserId(userId);
				bankInfo.setIdcard(idcard);
				bankInfo.setUserName(realname);
				bankInfo.setType(type);
				bankInfo.setIdDef("1");
				bankInfo.setState("0");
				bankInfo.setCardType(location.getString("type"));

				BigDecimal creditBlance = new BigDecimal(creditBlanceStr);
				int billDate = 0;
				int repaymentDate = 0;

				if (!"-1".equals(billDayStr)) {
					try {
						billDate = Integer.valueOf(billDayStr);
					} catch (NumberFormatException e) {
						map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
						map.put(CommonConstants.RESP_MESSAGE, "输入日期格式非法,请重新输入");
						return map;
					}
					if (!(billDate > 0 && billDate < 32)) {
						map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
						map.put(CommonConstants.RESP_MESSAGE, "输入日期格式非法,请重新输入");
						return map;
					}
				}

				if (!"-1".equals(repaymentDayStr)) {
					try {
						repaymentDate = Integer.valueOf(repaymentDayStr);
					} catch (Exception e) {
						map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
						map.put(CommonConstants.RESP_MESSAGE, "输入日期格式非法,请重新输入");
						return map;
					}
					if (!(repaymentDate > 0 && repaymentDate < 32)) {
						map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
						map.put(CommonConstants.RESP_MESSAGE, "输入日期格式非法,请重新输入");
						return map;
					}
				}

				bankInfo.setBillDay(billDate);
				bankInfo.setRepaymentDay(repaymentDate);
				bankInfo.setCreditBlance(creditBlance);

				if (bank.containsKey(location.getString("bankLocation"))) {
					bankInfo.setBankName((String) bank.get(location.getString("bankLocation")));
				} else {
					bankInfo.setBankName(location.getString("bankLocation"));
				}
				bankInfo.setNature(location.getString("nature"));
				bankInfo.setLogo(location.getString("logo"));
				userBankInfoTemp = userBankInfoBusiness.saveUserBankInfo(bankInfo);
				LOG.info("银行验证有不存在进入添加================完毕");
			}

			map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
			map.put(CommonConstants.RESP_MESSAGE, "卡绑定成功");
			map.put(CommonConstants.RESULT, userBankInfoTemp);
			return map;

		} else {

			map.put(CommonConstants.RESP_CODE, CommonConstants.ERROR_CARD_FAILED);
			map.put(CommonConstants.RESP_MESSAGE,
					authObject.getString("message") != null && !authObject.getString("message").equals("null")
					? authObject.getString("message") : "身份信息不匹配");

			return map;

		}

	}
	/** 删除一张银行卡 */
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/user/bank/del/{token}")
	public @ResponseBody Object delBankInfo(HttpServletRequest request, @PathVariable("token") String token,
			@RequestParam(value = "cardno") String cardno,
			@RequestParam(value = "type", defaultValue = "2", required = false) String type) {
		Map<String, Object> map = new HashMap<>();
		long userId;
		try {
			userId = TokenUtil.getUserId(token);
		} catch (Exception e) {
			map.put(CommonConstants.RESP_CODE, CommonConstants.ERROR_TOKEN);
			map.put(CommonConstants.RESP_MESSAGE, "token无效");
			return map;
		}

		List<UserBankInfo> userBankInfos = userBankInfoBusiness.queryUserBankInfoByCardnoType(cardno, type);
		for (UserBankInfo userBankInfo : userBankInfos) {
			if (userId == userBankInfo.getUserId()) {
				userBankInfo.setState("1");
				userBankInfo.setCreateTime(new Date());
				userBankInfoBusiness.saveUserBankInfo(userBankInfo);
			}
		}
		map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
		map.put(CommonConstants.RESP_MESSAGE, "删除成功");
		return map;
	}

	/**
	 * 删除一张银行卡 2019.4.12更新手机号码校验
	 * @param request
	 * @param token
	 * @param cardno
	 * @param type
	 * @param userPhone
	 * @param vericode
	 * @return
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/user/bank/delNew/{token}")
	public @ResponseBody Object delBankInfoNew(HttpServletRequest request, @PathVariable("token") String token,
			@RequestParam(value = "cardno") String cardno,
			@RequestParam(value = "type", defaultValue = "2", required = false) String type,
			@RequestParam(value = "userPhone", required = false) String userPhone,
			@RequestParam(value = "vericode", required = false) String vericode) {
		Map<String, Object> map = new HashMap<>();
		long userId;
		try {
			userId = TokenUtil.getUserId(token);
		} catch (Exception e) {
			map.put(CommonConstants.RESP_CODE, CommonConstants.ERROR_TOKEN);
			map.put(CommonConstants.RESP_MESSAGE, "token无效");
			return map;
		}
		/*
		//绑卡短信验证码校验 2019.4.12
		String vericodeUrl="http://notice/v1.0/notice/sms/vericode?phone="+userPhone; 
		String vericodeResult=restTemplate.getForObject(vericodeUrl, String.class);
		JSONObject jsonObject1=null;
		jsonObject1=JSONObject.fromObject(vericodeResult); 
		//jsonObject1.get(CommonConstants.RESULT);
		String vCode=jsonObject1.getString(CommonConstants.RESULT);
		System.out.println(vCode);
		if(vCode==null || vCode==""||!vCode.equals(vericode)) {
			LOG.info("验证码输入错误=====================================" + vCode);
			map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			map.put(CommonConstants.RESP_MESSAGE, "验证码输入有误"); 
			return map;
		}
		System.out.println("验证成功");
		*/
		//到redis缓存中获取该号码对应的验证码
		String vCode=String.valueOf(redisTemplate.opsForValue().get(userPhone));
		LOG.info("验证码===================================>"+vCode);
		if(!vCode.equals(vericode)||vCode==null || vCode=="") {
			LOG.info("验证码输入错误=====================================" + vCode);
			map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			map.put(CommonConstants.RESP_MESSAGE, "验证码输入有误"); 
			return map;
		}
		List<UserBankInfo> userBankInfos = userBankInfoBusiness.queryUserBankInfoByCardnoType(cardno, type);
		for (UserBankInfo userBankInfo : userBankInfos) {
			if (userId == userBankInfo.getUserId()) {
				userBankInfo.setState("1");
				userBankInfo.setCreateTime(new Date());
				userBankInfoBusiness.saveUserBankInfo(userBankInfo);
			}
		}

		map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
		map.put(CommonConstants.RESP_MESSAGE, "删除成功");
		return map;
	}
	
	/** 设置一张默认结算卡 */
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/user/bank/default/{token}")
	public @ResponseBody Object setDefaultBankInfo(HttpServletRequest request, @PathVariable("token") String token,
			@RequestParam(value = "cardno") String cardno) {
		Map map = new HashMap();
		long userId;
		try {
			userId = TokenUtil.getUserId(token);
		} catch (Exception e) {
			map.put(CommonConstants.RESP_CODE, CommonConstants.ERROR_TOKEN);
			map.put(CommonConstants.RESP_MESSAGE, "token无效");
			return map;
		}

		map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
		map.put(CommonConstants.RESULT, userBankInfoBusiness.setDefaultBank(userId, cardno));
		map.put(CommonConstants.RESP_MESSAGE, "设置成功");
		return map;
	}

	/** 根据银行名称获取银行编号 */
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/user/bank/number/name")
	public @ResponseBody Object delBankNumber(HttpServletRequest request,
			@RequestParam(value = "bank_name") String bankName) {
		Map map = new HashMap();

		BankNumber bankNumber = userBankInfoBusiness.queryBankNumberByBankName(bankName);
		map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
		map.put(CommonConstants.RESULT, bankNumber);
		map.put(CommonConstants.RESP_MESSAGE, "查询成功");
		return map;
	}

	/** 根据银行名称获取银行缩写 */
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/user/bank/acronym/name")
	public @ResponseBody Object delBankAcronym(HttpServletRequest request,
			@RequestParam(value = "bank_name") String bankName) {
		Map map = new HashMap();

		BankAcronym bankAcronym = userBankInfoBusiness.queryBankAcronymByBankName(bankName);
		map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
		map.put(CommonConstants.RESULT, bankAcronym);
		map.put(CommonConstants.RESP_MESSAGE, "查询成功");
		return map;
	}

	/**
	 * 根据userId查询用户贷记卡UserBankInfo
	 * 
	 * @param request
	 * @param userId
	 * @return
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/user/bank/find/nature")
	public @ResponseBody Object findBankNatureByUserId(HttpServletRequest request,
			@RequestParam(value = "userId") long userId) {
		Map map = new HashMap();
		List<UserBankInfo> userBankInfos = userBankInfoBusiness.findNatureByUserId(userId);
		if (userBankInfos == null || userBankInfos.size() < 1) {
			map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			map.put(CommonConstants.RESP_MESSAGE, "查询不到该用户相关贷记卡");
		} else {
			map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
			map.put(CommonConstants.RESULT, userBankInfos);
			map.put(CommonConstants.RESP_MESSAGE, "查询成功");
		}
		return map;
	}

	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/user/bank/find/bankphone")
	public @ResponseBody Object findBankPhoneByUserIdAndBankNo(HttpServletRequest request,
			@RequestParam(value = "userId") long userId, @RequestParam(value = "cardNo") String cardNo) {
		Map<String, Object> map = new HashMap<>();
		List<UserBankInfo> userBankInfos = null;
		try {
			userBankInfos = userBankInfoBusiness.findUserBankInfoByUseridAndCardno(userId, cardNo);
		} catch (Exception e) {
			e.printStackTrace();
			LOG.error("", e);
			map.put(CommonConstants.RESP_CODE, CommonConstants.ERROR_PARAM);
			map.put(CommonConstants.RESP_MESSAGE, "查询数据异常");
			return map;
		}
		if (userBankInfos == null || userBankInfos.size() == 0) {
			map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			map.put(CommonConstants.RESP_MESSAGE, "查询不到手机号记录");
		} else {
			map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
			map.put(CommonConstants.RESP_MESSAGE, "查询成功");
			map.put(CommonConstants.RESULT, userBankInfos.get(0));
		}
		return map;
	}

	/**
	 * 截取银行支行名
	 * 
	 * @param request
	 * @param bankbranchName
	 * @return
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/user/bank/find/bybankIntercept")
	public @ResponseBody Object queryBankNumberByBankName(HttpServletRequest request,
			@RequestParam(value = "card_no") String cardNo,
			@RequestParam(value = "bankbranch_name", required = false) String bankbranchName) {
		Map map = new HashMap();
		List list = new ArrayList();
		// 判断支行名是否为空
		if (bankbranchName.equals("")) {
			map.put(CommonConstants.RESP_CODE, CommonConstants.ERROR_PARAM);
			map.put(CommonConstants.RESP_MESSAGE, "支行信息不存在");
			return map;
			// 支行名为空
		} else {
			// 根据支行名和银行卡号查询获取用户银行卡信息
			UserBankInfo bankInfo = userBankInfoBusiness.findBankNumberBybankbranchname(bankbranchName, cardNo);
			// 获取支行名
			bankbranchName = bankInfo.getBankBranchName();
			// 判断支行是中国银行执行
			if (bankbranchName.equals("中国银行")) {
				list.add(0, bankInfo.getId());
				list.add(1, bankInfo.getUserId());
				list.add(2, bankInfo.getBankBranchName());
				// 判断不是中国银行执行
			} else {
				// 支行名是包含中国
				if (bankbranchName.contains("中国")) {
					// 支行名包含中国银行
					if (bankbranchName.contains("中国银行")) {
						int Front = bankbranchName.indexOf("中国");
						int after = bankbranchName.indexOf("银行");
						bankbranchName = bankbranchName.substring(0, after + 2);
						list.add(0, bankInfo.getId());
						list.add(1, bankInfo.getUserId());
						list.add(2, bankbranchName);
						// 支行名不包含中国银行
					} else {
						int Front = bankbranchName.indexOf("中国");
						int after = bankbranchName.indexOf("银行");
						bankbranchName = bankbranchName.substring(Front + 2, after + 2);
						list.add(0, bankInfo.getId());
						list.add(1, bankInfo.getUserId());
						list.add(2, bankbranchName);
					}
					// 支行名包含银行
				} else if (bankbranchName.contains("银行")) {
					// 支行名包含浦发银行或者广发银行
					if (bankbranchName.contains("浦发银行") || bankbranchName.contains("广发银行")
							|| bankbranchName.contains("邮储银行")) {
						// 支行名包含浦发银行
						if (bankbranchName.contains("浦发银行")) {
							bankInfo.setBankBranchName("浦东发展银行");
							list.add(0, bankInfo.getId());
							list.add(1, bankInfo.getUserId());
							list.add(2, bankInfo.getBankBranchName());
							// 支行名包含广发银行
						}
						if (bankbranchName.contains("广发银行")) {
							bankInfo.setBankBranchName("广东发展银行");
							list.add(0, bankInfo.getId());
							list.add(1, bankInfo.getUserId());
							list.add(2, bankInfo.getBankBranchName());
						}
						// 支行名包含邮储银行
						if (bankbranchName.contains("邮储银行")) {
							bankInfo.setBankBranchName("邮政储蓄银行");
							list.add(0, bankInfo.getId());
							list.add(1, bankInfo.getUserId());
							list.add(2, bankInfo.getBankBranchName());
						}
						// 支行名不包含浦发和广发和邮储
					} else {
						int after = bankbranchName.indexOf("银行");
						bankbranchName = bankbranchName.substring(0, after + 2);
						list.add(0, bankInfo.getId());
						list.add(1, bankInfo.getUserId());
						list.add(2, bankbranchName);
					}
					// 支行名不包含中国和银行
				} else {
					list.add(0, bankInfo.getId());
					list.add(1, bankInfo.getUserId());
					list.add(2, bankInfo.getBankBranchName());
				}
			}
		}
		map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
		map.put(CommonConstants.RESP_MESSAGE, "成功");
		map.put(CommonConstants.RESULT, list);
		return map;
	}

	// 根据userId和卡号验证是否是可用的指定类型的卡
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/user/bank/verify/isuseable")
	public @ResponseBody Object verifyBankCard(HttpServletRequest request,
			@RequestParam(value = "userId") String userIdStr,
			@RequestParam(value = "bankCardNumber") String bankCardNumber,
			// 卡类型,1为信用卡,0为储蓄卡
			@RequestParam(value = "cardType", required = false, defaultValue = "1") String cardType) {
		Map<String, Object> map = new HashMap<>();
		userIdStr = userIdStr.trim();
		bankCardNumber = bankCardNumber.trim();
		cardType = cardType.trim();

		// int count =
		// userBankInfoBusiness.queryCountByUserIdAndCardNoAndCardType(Long.valueOf(userIdStr),bankCardNumber,cardType);
		UserBankInfo userBankInfo = userBankInfoBusiness.queryByUserIdAndCardNoAndCardType(Long.valueOf(userIdStr),
				bankCardNumber, cardType);
		if (userBankInfo == null) {
			map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			map.put(CommonConstants.RESP_MESSAGE, "验证失败,该卡不可用,请更换!");
		} else {
			map = ResultWrap.init(CommonConstants.SUCCESS, "验证成功", userBankInfo);
		}
		return map;
	}

	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/user/bank/verify/cecuritycode/expiredtime")
	public @ResponseBody Object verifyCreditCardDoesHaveSecurityCodeAndExpiredTime(HttpServletRequest request,
			@RequestParam(value = "userId") String userId,
			@RequestParam(value = "creditCardNumber") String creditCardNumber

	) {
		Map<String, Object> map = new HashMap<>();
		Map<String, Object> verifyStringFiledIsNull = AuthorizationHandle.verifyStringFiledIsNull(userId,
				creditCardNumber);
		if (!CommonConstants.SUCCESS.equals(verifyStringFiledIsNull.get(CommonConstants.RESP_CODE))) {
			return verifyStringFiledIsNull;
		}
		UserBankInfo userBankInfo = userBankInfoBusiness.findUserBankInfoByUserIdAndCardNoAndState(Long.valueOf(userId),
				creditCardNumber, "0", "0");
		if (userBankInfo == null) {
			map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			map.put(CommonConstants.RESP_MESSAGE, "无该卡数据!");
			return map;
		}

		String securityCode = userBankInfo.getSecurityCode();
		String expiredTime = userBankInfo.getExpiredTime();
		verifyStringFiledIsNull = AuthorizationHandle.verifyStringFiledIsNull(securityCode, expiredTime);
		if (!CommonConstants.SUCCESS.equals(verifyStringFiledIsNull.get(CommonConstants.RESP_CODE))) {
			return verifyStringFiledIsNull;
		}

		map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
		map.put(CommonConstants.RESP_MESSAGE, "验证成功!");
		map.put(CommonConstants.RESULT, userBankInfo);
		return map;
	}

	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/user/bank/set/bankinfo")
	public @ResponseBody Object setBankCardInfo(HttpServletRequest request,
			@RequestParam(value = "userId") String userId,
			@RequestParam(value = "bankCardNumber") String bankCardNumber,
			@RequestParam(value = "securityCode") String securityCode,
			@RequestParam(value = "expiredTime") String expiredTime,
			@RequestParam(value = "version", required = false, defaultValue = "1") String version,
			@RequestParam(value = "billDay", required = false, defaultValue = "") String billDayStr,
			@RequestParam(value = "repaymentDay", required = false, defaultValue = "") String repaymentDayStr,
			@RequestParam(value = "creditBlance", required = false, defaultValue = "0") String creditBlanceStr) {
		Map<String, Object> map = new HashMap<>();
		Map<String, Object> verifyStringFiledIsNull = AuthorizationHandle.verifyStringFiledIsNull(userId,
				bankCardNumber, securityCode, expiredTime);
		if (!CommonConstants.SUCCESS.equals(verifyStringFiledIsNull.get(CommonConstants.RESP_CODE))) {
			return verifyStringFiledIsNull;
		}

		Map<String, Object> verifyMoney = AuthorizationHandle.verifyMoney(creditBlanceStr, 2, BigDecimal.ROUND_HALF_UP);
		if (!CommonConstants.SUCCESS.equals(verifyMoney.get(CommonConstants.RESP_CODE))) {
			return verifyMoney;
		}

		UserBankInfo userBankInfo = userBankInfoBusiness.findUserBankInfoByUserIdAndCardNoAndState(Long.valueOf(userId),
				bankCardNumber, "0", "0");
		if (userBankInfo == null) {
			map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			map.put(CommonConstants.RESP_MESSAGE, "无该卡数据,请先添加银行卡!");
			return map;
		}

		BigDecimal creditBlance = new BigDecimal(creditBlanceStr);
		int billDate = 0;
		int repaymentDate = 0;

		if (billDayStr != null && !"".equals(billDayStr)) {
			try {
				billDate = Integer.valueOf(billDayStr);
			} catch (NumberFormatException e) {
				map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
				map.put(CommonConstants.RESP_MESSAGE, "输入日期格式非法,请重新输入");
				return map;
			}
			if (!(billDate > 0 && billDate < 32)) {
				map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
				map.put(CommonConstants.RESP_MESSAGE, "输入日期格式非法,请重新输入");
				return map;
			}
		}

		if (repaymentDayStr != null && !"".equals(repaymentDayStr)) {
			try {
				repaymentDate = Integer.valueOf(repaymentDayStr);
			} catch (Exception e) {
				map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
				map.put(CommonConstants.RESP_MESSAGE, "输入日期格式非法,请重新输入");
				return map;
			}
			if (!(repaymentDate > 0 && repaymentDate < 32)) {
				map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
				map.put(CommonConstants.RESP_MESSAGE, "输入日期格式非法,请重新输入");
				return map;
			}
		}

		if (securityCode.length() != 3 || !securityCode.matches("^[0-9]*$")) {
			map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			map.put(CommonConstants.RESP_MESSAGE, "输入安全码非法,请重新输入");
			return map;
		}

		userBankInfo.setSecurityCode(securityCode);
		userBankInfo.setExpiredTime(expiredTime);
		userBankInfo.setBillDay(billDate);
		userBankInfo.setRepaymentDay(repaymentDate);
		userBankInfo.setCreditBlance(creditBlance);
		try {
			userBankInfo = userBankInfoBusiness.saveUserBankInfo(userBankInfo);
		} catch (Exception e1) {
			e1.printStackTrace();
			map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			map.put(CommonConstants.RESP_MESSAGE, "修改失败!");
			return map;
		}

		if (!"".equals(billDayStr) && !"".equals(repaymentDayStr)) {
			LinkedMultiValueMap<String, String> requestEntity = new LinkedMultiValueMap<String, String>();
			requestEntity.add("userId", userId);
			requestEntity.add("creditCardNumber", bankCardNumber);
			requestEntity.add("creditBlance", creditBlance.toString());
			requestEntity.add("billDate", billDate + "");
			requestEntity.add("repaymentDate", repaymentDate + "");
			Map<String, Object> restTemplateDoPost;
			try {
				restTemplateDoPost = util.restTemplateDoPost("creditcardmanager",
						"/v1.0/creditcardmanager/set/creditcardaccount", requestEntity);
			} catch (Exception e) {
				e.printStackTrace();
				LOG.error("", e);
				map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
				map.put(CommonConstants.RESP_MESSAGE, "修改异常!");
				return map;
			}
			if (!CommonConstants.SUCCESS.equals(restTemplateDoPost.get(CommonConstants.RESP_CODE))) {
				return restTemplateDoPost;
			}
		}

		map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
		map.put(CommonConstants.RESP_MESSAGE, "修改成功!");
		map.put(CommonConstants.RESULT, userBankInfo);
		return map;
	}

	// 给赵欢欢写的一个银行图标接口
	@RequestMapping(method = RequestMethod.GET, value = "/v1.0/user/bank/get/bankpicture")
	public @ResponseBody Object getBankPicture() {

		List<BankIcon> bankIcon = userBankInfoBusiness.getBankIcon();

		return ResultWrap.init(CommonConstants.SUCCESS, "查询成功", bankIcon);

	}

	// 查询用户的银行卡信息
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/user/bank/query/byuseridandtype/andnature")
	public @ResponseBody Object queryBankInfoByUserIdAndTypeAndNatureAndState(HttpServletRequest request,
			@RequestParam(value = "userId") long userId,
			@RequestParam(value = "type", required = false, defaultValue = "0") String type,
			@RequestParam(value = "nature", required = false, defaultValue = "0") String nature,
			@RequestParam(value = "state", required = false, defaultValue = "0") String state,
			@RequestParam(value = "isDefault", required = false, defaultValue = "0") String isDefault) {

		if ("0".equals(nature)) {
			nature = "贷记";
		} else {
			nature = "借记";
		}

		List<UserBankInfo> queryUserBankInfoByUserid;
		List<UserBankInfo> queryUserBankInfoByUserId = new ArrayList<UserBankInfo>();
		if ("1".equals(isDefault)) {

			String[] isDef = { "1" };

			queryUserBankInfoByUserid = userBankInfoBusiness.getUserBankInfoByUserIdAndTypeAndNatureAndState(userId,
					type, nature, state, isDef);

			if (queryUserBankInfoByUserid != null && queryUserBankInfoByUserid.size() > 0) {

				return ResultWrap.init(CommonConstants.SUCCESS, "查询成功!", queryUserBankInfoByUserid);
			} else {

				String[] isDef1 = { "0" };

				queryUserBankInfoByUserid = userBankInfoBusiness.getUserBankInfoByUserIdAndTypeAndNatureAndState(userId,
						type, nature, state, isDef1);

				if (queryUserBankInfoByUserid != null && queryUserBankInfoByUserid.size() > 0) {

					UserBankInfo userBankInfo = queryUserBankInfoByUserid.get(0);
					queryUserBankInfoByUserId.add(userBankInfo);

					return ResultWrap.init(CommonConstants.SUCCESS, "查询成功!", queryUserBankInfoByUserId);
				} else {

					return ResultWrap.init("666666", "您暂未在系统内添加银行卡信息，请前往添加!");
				}

			}

		} else {

			String[] isDef = { "0", "1" };

			queryUserBankInfoByUserid = userBankInfoBusiness.getUserBankInfoByUserIdAndTypeAndNatureAndState(userId,
					type, nature, state, isDef);

			if (queryUserBankInfoByUserid != null && queryUserBankInfoByUserid.size() > 0) {

				return ResultWrap.init(CommonConstants.SUCCESS, "查询成功!", queryUserBankInfoByUserid);
			} else {

				return ResultWrap.init("666666", "您暂未在系统内添加银行卡信息，请前往添加!");
			}

		}

	}

	// 设置默认银行卡的接口
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/user/bankcard//create/isdefault")
	public @ResponseBody Object createIsDefault(HttpServletRequest request, @RequestParam(value = "userId") long userId,
			@RequestParam(value = "bankCard") String bankCard,
			@RequestParam(value = "type", required = false, defaultValue = "0") String type) {

		try {
			userBankInfoBusiness.setDefaultBankByUserIdAndBankCardAndType(userId, bankCard, type);
		} catch (Exception e) {
			LOG.error("设置默认银行卡卡信息异常======", e);

			return ResultWrap.init(CommonConstants.FALIED, "系统检测您绑定的银行卡信息异常,请及时联系客服或者相关人员!");
		}

		return ResultWrap.init(CommonConstants.SUCCESS, "设置默认卡信息成功!");
	}

	// 银行卡ocr识别
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/user/upload/ocr/bankcard")
	public @ResponseBody Object ocr_bank_card(HttpServletRequest request) {
		Map<String, Object> resultMap = new HashMap<String, Object>();
		MultipartHttpServletRequest multipartRequest;

		List<MultipartFile> files = null;
		try {
			multipartRequest = (MultipartHttpServletRequest) request;
			files = multipartRequest.getFiles("image");

		} catch (Exception e1) {
			LOG.info(e1.getMessage());
		}
		String faceimgBase64 = "";
		try {
			byte[] content = files.get(0).getBytes();
			faceimgBase64 = new String(Base64.encodeBase64(content));
		} catch (IOException e1) {
			LOG.error(e1.getMessage());
			resultMap.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			resultMap.put(CommonConstants.RESP_MESSAGE, "图片编码时出现异常");
			return resultMap;
		}
		String host = "https://yhk.market.alicloudapi.com";
		String path = "/rest/160601/ocr/ocr_bank_card.json";
		String method = "POST";
		String appcode = "a5d1104a6f73467f85a372562ea69d55";
		Map<String, String> headers = new HashMap<String, String>();
		// 最后在header中的格式(中间是英文空格)为Authorization:APPCODE
		// 83359fd73fe94948385f570e3c139105
		headers.put("Authorization", "APPCODE " + appcode);
		// 根据API的要求，定义相对应的Content-Type
		headers.put("Content-Type", "application/json; charset=UTF-8");
		Map<String, String> querys = new HashMap<String, String>();
		String bodys = "{\"image\":\"" + faceimgBase64 + "\"}";
		LOG.info(bodys.toString());
		try {
			/**
			 * 重要提示如下: HttpUtils请从
			 * https://github.com/aliyun/api-gateway-demo-sign-java/blob/master/
			 * src/main/java/com/aliyun/api/gateway/demo/util/HttpUtils.java 下载
			 *
			 * 相应的依赖请参照
			 * https://github.com/aliyun/api-gateway-demo-sign-java/blob/master/
			 * pom.xml
			 */
			HttpResponse response = HttpUtils.doPost(host, path, method, headers, querys, bodys);
			LOG.info(response.toString());
			// 获取response的body
			String resp = EntityUtils.toString(response.getEntity());
			LOG.info("银行卡识别返回内容：" + resp.toString());
			JSONObject respJSON = JSONObject.fromObject(resp);
			String cardNum = respJSON.getString("card_num");
			if (cardNum.length() > 6) {
				resultMap.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
				resultMap.put(CommonConstants.RESP_MESSAGE, "银行卡识别成功");
				resultMap.put(CommonConstants.RESULT, cardNum);
			} else {
				resultMap.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
				resultMap.put(CommonConstants.RESP_MESSAGE, "您好，请稍后重新识别银行卡");
			}
		} catch (Exception e) {
			e.printStackTrace();
			resultMap.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			resultMap.put(CommonConstants.RESP_MESSAGE, "您好，请稍后重新识别银行卡");
		}
		return resultMap;
	}
	// 根据银行卡号获取绑卡信息
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/user/query/bankName")
	public @ResponseBody Object getbankInfoByCardNo(HttpServletRequest request, @RequestParam(value = "card_no") String cardNo) {
		UserBankInfo userBankInfo = new UserBankInfo();
		Map<String,String> map = new HashMap<>();
		try{
			 userBankInfo = userBankInfoBusiness.findUserBankInfoByCardno(cardNo);
		}catch (Exception e){
			LOG.info("查询银行卡信息失败，详情："+e.getMessage());
		}
		if(userBankInfo == null){
			map.put("respcode","999999");
			map.put("respMessage","未查到银行卡信息");
			return map;
		}
		LOG.info(userBankInfo.getIdcard()+"====="+userBankInfo.getPhone());
		map.put("respcode","000000");
		map.put("respMessage","查询成功");
		map.put("idcard", userBankInfo.getIdcard());
		map.put("phone", userBankInfo.getPhone());
		return map;
	}

}
