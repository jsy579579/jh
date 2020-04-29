package com.jh.channel.service;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.stereotype.Controller;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestTemplate;

import com.jh.channel.business.OutMerchantSecurityBusiness;
import com.jh.channel.pojo.OutMerchantPayResponse;
import com.jh.channel.pojo.OutMerchantSecurityKey;
import com.jh.channel.pojo.OutMerchantUserRegisterRequest;
import com.jh.channel.util.MD5Util;
import com.jh.channel.util.Util;

import cn.jh.common.utils.CommonConstants;
import cn.jh.common.utils.TokenUtil;
import net.sf.json.JSONObject;

@Controller
@EnableAutoConfiguration
public class OutMemberService {
	
	private static final Logger LOG = LoggerFactory.getLogger(OutMemberService.class);
	
	@Autowired
	Util util;
	
	@Autowired
	private OutMerchantSecurityBusiness securityBusiness;
	
	@RequestMapping(method=RequestMethod.POST,value="/v1.0/channel/user/register")
	public @ResponseBody Object OutRegisterUser(HttpServletRequest request,   		 
			 @RequestParam(value = "merchant_no") String merchantNo,
			 @RequestParam(value = "date") String data,
		 	 @RequestParam(value = "sign") String sign
			 ){
		Map map = new HashMap();
		LOG.info("");
		/**根据商家账号获取商户信息*/
		OutMerchantSecurityKey securityKey = null;
		 String version = "1.0";
         String charset = "UTF-8";
         String sign_type = "MD5";
		try {
		         securityKey = securityBusiness.getOutMerchantSecurityKey(merchantNo);
		         if (securityKey == null) {
	            	return getMerchantErrorMap(version, charset, sign_type, CommonConstants.ERROR_USER_NOT_EXISTED, "商家未开通", merchantNo, "");
	             }
	         	/** 签名验证 **/
	            /** 根据商户号获取商户的私钥 */
	            JSONObject jsonObject = JSONObject.fromObject(data);

	            String createSign = "";
	            try {
	                createSign = MD5Util.getSignature(data, securityKey.getKey());
	            } catch (Exception e) {
	            	LOG.error(e.getMessage());
	            	return getMerchantErrorMap(version, charset, sign_type,CommonConstants.ERROR_SIGN_NOVALID, "签名失败", merchantNo, "");
	            }

	            if (jsonObject.containsKey("version")) {

	                version = jsonObject.getString("version");

	            }

	            if (jsonObject.containsKey("charset")) {

	                charset = jsonObject.getString("charset");

	            }

	            if (jsonObject.containsKey("sign_type")) {

	                sign_type = jsonObject.getString("sign_type");

	            }

	            if (!createSign.equalsIgnoreCase(sign)) {

	            	return getMerchantErrorMap(version, charset, sign_type, "CommonConstants.ERROR_SIGN_NOVALID", "验签失败", merchantNo, securityKey.getKey());
	            }

	            /** 参数验证 */
	            if (!jsonObject.containsKey("service") || !jsonObject.containsKey("idcard")
	            		  || !jsonObject.containsKey("realname")   || !jsonObject.containsKey("version")   || !jsonObject.containsKey("charset")   
	            		  || !jsonObject.containsKey("merchantId")   || !jsonObject.containsKey("signType")  || !jsonObject.containsKey("sign") 
	            		  || !jsonObject.containsKey("debitCardName")   || !jsonObject.containsKey("debitCardNo")   || !jsonObject.containsKey("debitCardMobile")  
	            		  || !jsonObject.containsKey("debitProvince")   || !jsonObject.containsKey("debitCity") || !jsonObject.containsKey("debitAddress") 
	            		  || !jsonObject.containsKey("debitBankBranchId")   || !jsonObject.containsKey("debitBranchName") || !jsonObject.containsKey("premerchantRate")  ) {
	            	return getMerchantErrorMap(version, charset, sign_type,  CommonConstants.ERROR_VERI_CODE, "参数不完整", merchantNo, securityKey.getKey());
	            }
	            /** 重复提交验证，已经处理得订单 */
	            
	            /***赋值**/
	            OutMerchantUserRegisterRequest registeruser=new OutMerchantUserRegisterRequest();
	            registeruser.setService(jsonObject.getString("service"));
	            registeruser.setVersion(jsonObject.getString("version"));
	            registeruser.setCharset(charset);
	            registeruser.setMerchantId(merchantNo);
	            registeruser.setSignType(sign_type);
	            registeruser.setSign(createSign);
	            //借记卡卡号
	            registeruser.setDebitCardNo(jsonObject.getString("debitCardNo"));
	            //借记卡名称
	            registeruser.setDebitCardName(jsonObject.getString("debitCardName"));
	            //预留手机号
	            registeruser.setDebitCardMobile(jsonObject.getString("debitCardMobile"));
	            //开户所在省
	            registeruser.setDebitProvince(jsonObject.getString("debitProvince"));
	            //开户所在市
	            registeruser.setDebitCity(jsonObject.getString("debitCity"));
	            //详细地址
	            registeruser.setDebitAddress(jsonObject.getString("debitAddress"));
	            //联行行号
	            registeruser.setDebitBankBranchId(jsonObject.getString("debitBankBranchId"));
	            //开户支行名称
	            registeruser.setDebitBranchName(jsonObject.getString("debitBranchName"));
	            //刷卡费率
	            registeruser.setPremerchantRate(jsonObject.getString("premerchantRate"));
	            RestTemplate restTemplate = new RestTemplate();
	            URI uri = util.getServiceUrl("user", "error url request!");
	            String url = uri.toString(	) + "/v1.0/user/query/phone";
	            MultiValueMap<String, String> requestEntity = new LinkedMultiValueMap<String, String>();
	            requestEntity.add("phone", jsonObject.getString("mobile"));
	            requestEntity.add("brandId", securityKey.getBrandId());
	            String result = restTemplate.postForObject(url, requestEntity, String.class);
	            JSONObject resultObj = JSONObject.fromObject(result);
	            String brandid = securityKey.getBrandId();
	            String userid = "";
	            if (resultObj.isNullObject()) {
	                /** 创建用户 */
	                restTemplate = new RestTemplate();
	                uri = util.getServiceUrl("user", "error url request!");
	                url = uri.toString() + "/v1.0/user/outchannel/new";
	                requestEntity = new LinkedMultiValueMap<String, String>();
	                requestEntity.add("phone", jsonObject.getString("mobile"));
	                requestEntity.add("pre_phone", securityKey.getPhone());
	                requestEntity.add("brand_id",securityKey.getBrandId());
	                result = restTemplate.postForObject(url, requestEntity, String.class);
	                JSONObject authObject = JSONObject.fromObject(result);
	                resultObj = authObject.getJSONObject("result");
	                brandid = resultObj.getString("brandId");
	                userid = resultObj.getString("id");
	                Map<String,String> dataMap= new HashMap<String,String>();
	                dataMap.put("user_id", userid);
	                String token = TokenUtil.createToken(Long.parseLong(userid), Long.parseLong(brandid), resultObj.getString("phone"));
	                uri = util.getServiceUrl("user", "error url request!");
	                url = uri.toString() + "/v1.0/paymentchannel/realname/auth/"+token;
	                requestEntity = new LinkedMultiValueMap<String, String>();
	                requestEntity.add("realname", jsonObject.getString("realname"));
	                requestEntity.add("idcard", jsonObject.getString("idcard"));
	                result = restTemplate.postForObject(url, requestEntity, String.class);
	                authObject = JSONObject.fromObject(result);
	                resultObj = authObject.getJSONObject("result");
	                if (!authObject.getString("resp_code").equalsIgnoreCase(CommonConstants.SUCCESS)) { 
	                	return getMerchantErrorMap(version, charset, sign_type,resultObj.getString("resp_code"), resultObj.getString("resp_message"), merchantNo, securityKey.getKey()); 
	                }else{
	                	if(resultObj.containsKey("result")&&resultObj.getString("result").trim().length()==1) {
	                		if(resultObj.getString("result").equals("1")) {
	                			  uri = util.getServiceUrl("user", "error url request!");
			    	              url = uri.toString() + "/v1.0/user/updatestatus/status";
			    	              requestEntity = new LinkedMultiValueMap<String, String>();
			    	              requestEntity.add("phone", jsonObject.getString("mobile"));
			    	              requestEntity.add("status", "1");
			    	              requestEntity.add("brandId", securityKey.getBrandId());
			    	              result = restTemplate.postForObject(url, requestEntity, String.class);
			    	              authObject = JSONObject.fromObject(result);
			    	              resultObj = authObject.getJSONObject("result");
	                			 return getMerchantdSuccessMap(version, charset, sign_type, "00000", "添加成功", merchantNo, securityKey.getKey(),dataMap);
	                		}
	                	}
	                	
	                }
	                return getMerchantdSuccessMap(version, charset, sign_type, "00000", "添加成功", merchantNo, securityKey.getKey(),dataMap);
	            } else {
	                brandid = resultObj.getString("brandId");
	                userid = resultObj.getString("id");
	                Map<String,String> dataMap= new HashMap<String,String>();
	                dataMap.put("user_id", userid);
	                return getMerchantdSuccessMap(version, charset, sign_type, "00000", "用户已存在", merchantNo, securityKey.getKey(),dataMap);
	            }
	            
		} catch (Exception e) {
//			LOG.info(e.getMessage());
			return getMerchantErrorMap(version, charset, sign_type,  CommonConstants.ERROR_VERI_CODE, "参数不完整", merchantNo, securityKey.getKey());
		}
	}
	
	@RequestMapping(method=RequestMethod.POST,value="/v1.0/channel/user/bank/add")
	public @ResponseBody Object OutUserBankInfo(HttpServletRequest request,   		 
			 @RequestParam(value = "merchant_no") String merchantNo,
			 @RequestParam(value = "date") String data,
		 	 @RequestParam(value = "sign") String sign
			 ){
		
		Map map = new HashMap();
		/**根据商家账号获取商户信息*/
		OutMerchantSecurityKey securityKey = null;
		 String version = "1.0";
         String charset = "UTF-8";
         String sign_type = "MD5";
		try {
		         securityKey = securityBusiness.getOutMerchantSecurityKey(merchantNo);
		         if (securityKey == null) {
	            	return getMerchantErrorMap(version, charset, sign_type, CommonConstants.ERROR_USER_NOT_EXISTED, "商家未开通", merchantNo, "");
	             }
	         	/** 签名验证 **/
	            /** 根据商户号获取商户的私钥 */
	            JSONObject jsonObject = JSONObject.fromObject(data);

	            String createSign = "";
	            try {
	                createSign = MD5Util.getSignature(data, securityKey.getKey());
	            } catch (Exception e) {
	            	LOG.error(e.getMessage());
	            	return getMerchantErrorMap(version, charset, sign_type,CommonConstants.ERROR_SIGN_NOVALID, "商家未开通", merchantNo, "");
	            }

	            if (jsonObject.containsKey("version")) {

	                version = jsonObject.getString("version");

	            }

	            if (jsonObject.containsKey("charset")) {

	                charset = jsonObject.getString("charset");

	            }

	            if (jsonObject.containsKey("sign_type")) {

	                sign_type = jsonObject.getString("sign_type");

	            }

	            if (!createSign.equalsIgnoreCase(sign)) {

	            	return getMerchantErrorMap(version, charset, sign_type, CommonConstants.ERROR_PARAM, "签名失败", merchantNo, securityKey.getKey());
	            }

	            /** 参数验证 */
	            if (!jsonObject.containsKey("service") || !jsonObject.containsKey("mobile") || !jsonObject.containsKey("card_no")
	                    || !jsonObject.containsKey("bankphone") ) {
	            	return getMerchantErrorMap(version, charset, sign_type, CommonConstants.ERROR_VERI_CODE, "参数不完整", merchantNo, securityKey.getKey());
	            }
	            /** 重复提交验证，已经处理得订单 */
	            RestTemplate restTemplate = new RestTemplate();
	            URI uri = util.getServiceUrl("user", "error url request!");
	            String url = uri.toString() + "/v1.0/user/query/phone";
	            MultiValueMap<String, String> requestEntity = new LinkedMultiValueMap<String, String>();
	            requestEntity.add("phone", jsonObject.getString("mobile"));
	            String result = restTemplate.postForObject(url, requestEntity, String.class);
	            JSONObject resultObj = JSONObject.fromObject(result);
	            String brandid = "2";
	            String userid = "";
	            if (resultObj.isNullObject()) {
	                return getMerchantErrorMap(version, charset, sign_type, CommonConstants.ERROR_USER_NO_REGISTER, "用户未注册", merchantNo, securityKey.getKey());
	            } else {
	            	/****查询用户身份证号*****/
	            	Map<String,String> dataMap= new HashMap<String,String>();
	                brandid = resultObj.getString("brandId");
	                userid = resultObj.getString("id");
	                String token = TokenUtil.createToken(Long.parseLong(userid), Long.parseLong(brandid), resultObj.getString("phone"));
	                /**获取四要素用户身份证姓名**/
	                /** 获取身份证实名信息 */
					uri = util.getServiceUrl("paymentchannel", "error url request!");
					url = uri.toString() + "/v1.0/paymentchannel/realname/userid";
					requestEntity = new LinkedMultiValueMap<String, String>();
					requestEntity.add("userid", userid);
					restTemplate = new RestTemplate();
					result = restTemplate.postForObject(url, requestEntity, String.class);
					LOG.info("RESULT================/v1.0/paymentchannel/realname/userid" + result);
					JSONObject realnameJsonObject = JSONObject.fromObject(result);
					JSONObject authObject = realnameJsonObject.getJSONObject("realname");
	                /** 信用卡四要素验证 **/
	                restTemplate = new RestTemplate();
	                uri = util.getServiceUrl("paymentchannel", "error url request!");
	                url = uri.toString() + "/v1.0/user/bank/add/"+token;
	                requestEntity = new LinkedMultiValueMap<String, String>();
	                requestEntity.add("realname", authObject.getString("realname").trim());
	                requestEntity.add("idcard", authObject.getString("idcard").trim());
	                requestEntity.add("bankcard", jsonObject.getString("card_no"));
	                requestEntity.add("mobile", jsonObject.getString("bankphone"));
	                requestEntity.add("type", "0");
	                result = restTemplate.postForObject(url, requestEntity, String.class);
	                LOG.info("result************/v1.0/user/bank/add/{token}"+result);
	                authObject = JSONObject.fromObject(result);
	                resultObj = authObject.getJSONObject("result");
	                if (!resultObj.getString("resp_code").equalsIgnoreCase(CommonConstants.SUCCESS)) { 
	                	return getMerchantErrorMap(version, charset, sign_type,CommonConstants.ERROR_CARD_FAILED, "信用卡验证无效", merchantNo, securityKey.getKey()); 
	                }
	                
	                return getMerchantdSuccessMap(version, charset, sign_type, CommonConstants.SUCCESS, "添加成功", merchantNo, securityKey.getKey(),dataMap);
	            }
	            
		} catch (Exception e) {
			LOG.info(e.getMessage());
			return getMerchantErrorMap(version, charset, sign_type, "00002", "参数不完整", merchantNo, securityKey.getKey());
		}
	}
	
	private OutMerchantPayResponse getMerchantErrorMap(String version, String charset, String signType, String error, String msg, String merchantid,
            String key) {
        Map tempmap = new HashMap();
        tempmap.put("version", version);
        tempmap.put("charset", charset);
        tempmap.put("sign_type", signType);
        tempmap.put("status", "-1");
        tempmap.put("merchant_no", merchantid);
        tempmap.put("err_code", error);
        tempmap.put("err_msg", msg);
        JSONObject tempJSONObject = JSONObject.fromObject(tempmap);
        String jsonStr = tempJSONObject.toString();
        String responsesign = "";
        OutMerchantPayResponse response = new OutMerchantPayResponse();
        response.setData(jsonStr);
        response.setSign(responsesign);
        response.setMerchant_id(merchantid);
        return response;

     }
	
	private OutMerchantPayResponse getMerchantdSuccessMap(String version, String charset ,String signType, String error, String msg, String merchantid,
            String key ,Map dataMap) {
        Map tempmap = new HashMap();
        tempmap.put("version", version);
        tempmap.put("charset", charset);
        tempmap.put("sign_type", signType);
        tempmap.put("status", "1");
        tempmap.put("merchant_id", merchantid);
        tempmap.put("resp_code", error);
        tempmap.put("resp_message", msg);
        tempmap.putAll(dataMap);
        JSONObject tempJSONObject = JSONObject.fromObject(tempmap);
        String jsonStr = tempJSONObject.toString();
        String responsesign = "";
        OutMerchantPayResponse response = new OutMerchantPayResponse();
        response.setData(jsonStr);
        response.setSign(responsesign);
        response.setMerchant_id(merchantid);
        return response;

    } 
}
