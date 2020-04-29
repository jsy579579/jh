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
public class OutObjectService {
	
	private static final Logger LOG = LoggerFactory.getLogger(OutObjectService.class);
	
	@Autowired
	Util util;
	
	@Autowired
	private OutMerchantSecurityBusiness securityBusiness;
	
	@RequestMapping(method=RequestMethod.POST,value="/v1.0/channel/user/registeru")
	public @ResponseBody Object OutRegisterUser(HttpServletRequest request,   		 
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
			     LOG.info("merchantNo="+merchantNo+"\n date="+data+"\n sign="+sign);
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
	            	LOG.info("createSign="+sign);
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
	            		  || !jsonObject.containsKey("merchantId")   || !jsonObject.containsKey("sign_type")   || !jsonObject.containsKey("registerPhone")  ) {
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
	            registeruser.setIdcard(jsonObject.getString("idcard"));
	            registeruser.setRealname(jsonObject.getString("realname"));
	            registeruser.setRegisterPhone(jsonObject.getString("registerPhone"));
	            
	            RestTemplate restTemplate = new RestTemplate();
	            URI uri = util.getServiceUrl("user", "error url request!");
	            String url = uri.toString(	) + "/v1.0/user/query/phone";
	            MultiValueMap<String, String> requestEntity = new LinkedMultiValueMap<String, String>();
	            requestEntity.add("phone",registeruser.getRegisterPhone());
	            String result = restTemplate.postForObject(url, requestEntity, String.class);
	            LOG.info("获取"+registeruser.getRegisterPhone()+"用户数据"+result);
	            JSONObject resultObj = JSONObject.fromObject(result);
	            JSONObject resultbj = resultObj.getJSONObject("result");
	           
	            if (!resultObj.getString(CommonConstants.RESP_CODE).equals(CommonConstants.SUCCESS)) {
	                /** 创建用户 */
	                restTemplate = new RestTemplate();
	                uri = util.getServiceUrl("user", "error url request!");
	                url = uri.toString() + "/v1.0/user/outchannel/new";
	                requestEntity = new LinkedMultiValueMap<String, String>();
	                requestEntity.add("phone", registeruser.getRegisterPhone());
	                requestEntity.add("pre_phone", registeruser.getMerchantId());
	                requestEntity.add("brand_id",securityKey.getBrandId());
	                result = restTemplate.postForObject(url, requestEntity, String.class);
	                LOG.info("创建"+registeruser.getRegisterPhone()+"用户结果"+result);
	                JSONObject authObject = JSONObject.fromObject(result);
	              
	                if(!authObject.getString(CommonConstants.RESP_CODE).equals(CommonConstants.SUCCESS)) {
	                	return getMerchantErrorMap(version, charset, sign_type,resultObj.getString("resp_code"), resultObj.getString("resp_message"), merchantNo, securityKey.getKey()); 
	                }
	                resultObj = authObject.getJSONObject("result");
	                String brandid = resultObj.getString("brandId");
	                String userid = resultObj.getString("id");
	                Map<String,String> dataMap= new HashMap<String,String>();
	                dataMap.put("user_id", userid);
	                String token = TokenUtil.createToken(Long.parseLong(userid), Long.parseLong(brandid), resultObj.getString("phone"));
	                uri = util.getServiceUrl("paymentchannel", "error url request!");
	                url = uri.toString() + "/v1.0/paymentchannel/realname/auth/"+token;
	                requestEntity = new LinkedMultiValueMap<String, String>();
	                requestEntity.add("realname", registeruser.getRealname());
	                requestEntity.add("idcard", registeruser.getIdcard());
	                result = restTemplate.postForObject(url, requestEntity, String.class);
	                LOG.info("上传实名"+registeruser.getRealname()+"户"+registeruser.getIdcard()+"数据"+result);
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
			    	              requestEntity.add("phone", registeruser.getRegisterPhone());
			    	              requestEntity.add("status", "1");
			    	              result = restTemplate.postForObject(url, requestEntity, String.class);
			    	              LOG.info("实名"+registeruser.getRealname()+"户结果"+result);
			    	              authObject = JSONObject.fromObject(result);
			    	              resultObj = authObject.getJSONObject("result");
	                			 return getMerchantdSuccessMap(version, charset, sign_type, "00000", "添加成功", merchantNo, securityKey.getKey(),dataMap);
	                		}
	                	}
	                	
	                }
	                return getMerchantdSuccessMap(version, charset, sign_type, "00000", "添加成功", merchantNo, securityKey.getKey(),dataMap);
	            } else if(resultObj.getString(CommonConstants.RESP_CODE).equals("000000")&&resultbj.getString("brandId").equals(securityKey.getBrandId())&&!resultbj.getString("realnameStatus").equals("1")) {
	                Map<String,String> dataMap= new HashMap<String,String>();
	                String token = TokenUtil.createToken(resultbj.getLong("id"), resultbj.getLong("brandId"), resultbj.getString("phone"));
	                uri = util.getServiceUrl("paymentchannel", "error url request!");
	                url = uri.toString() + "/v1.0/paymentchannel/realname/auth/"+token;
	                requestEntity = new LinkedMultiValueMap<String, String>();
	                requestEntity.add("realname", registeruser.getRealname());
	                requestEntity.add("idcard",  registeruser.getIdcard());
	                result = restTemplate.postForObject(url, requestEntity, String.class);
	                LOG.info("上传实名"+registeruser.getRealname()+"户"+registeruser.getIdcard()+"数据"+result);
	                JSONObject authObject = JSONObject.fromObject(result);
	                resultObj = authObject.getJSONObject("result");
	                if (!authObject.getString("resp_code").equalsIgnoreCase(CommonConstants.SUCCESS)) { 
	                	return getMerchantErrorMap(version, charset, sign_type,resultObj.getString("resp_code"), resultObj.getString("resp_message"), merchantNo, securityKey.getKey()); 
	                }else{
	                	if(resultObj.containsKey("result")&&resultObj.getString("result").trim().length()==1) {
	                		if(resultObj.getString("result").equals("1")) {
	                			  uri = util.getServiceUrl("user", "error url request!");
			    	              url = uri.toString() + "/v1.0/user/updatestatus/status";
			    	              requestEntity = new LinkedMultiValueMap<String, String>();
			    	              requestEntity.add("phone", registeruser.getRegisterPhone());
			    	              requestEntity.add("status", "1");
			    	              result = restTemplate.postForObject(url, requestEntity, String.class);
			    	              LOG.info("实名"+registeruser.getRealname()+"户结果"+result);
			    	              authObject = JSONObject.fromObject(result);
			    	              resultObj = authObject.getJSONObject("result");
	                			 return getMerchantdSuccessMap(version, charset, sign_type, "00000", "添加成功", merchantNo, securityKey.getKey(),dataMap);
	                		}
	                	}
	                	
	                }
	                return getMerchantdSuccessMap(version, charset, sign_type, "00000", "用户已存在", merchantNo, securityKey.getKey(),dataMap);
	            }else {
	            	
	            	return getMerchantErrorMap(version, charset, sign_type,resultObj.getString("resp_code"), "用户已经存在无法注册", merchantNo, securityKey.getKey());
	            }
	            
		} catch (Exception e) {
			LOG.info(e.getMessage());
			return getMerchantErrorMap(version, charset, sign_type,  CommonConstants.ERROR_VERI_CODE, "参数不完整", merchantNo, securityKey.getKey());
		}
	}
	
	@RequestMapping(method=RequestMethod.POST,value="/v1.0/channel/user/bank/addu")
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
				 LOG.info("merchantNo="+merchantNo+"\n date="+data+"\n sign="+sign);
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
	            LOG.info("获取"+jsonObject.getString("mobile")+"户结果"+result);
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
	                uri = util.getServiceUrl("user", "error url request!");
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
	@RequestMapping(method=RequestMethod.POST,value="/v1.0/channel/user/token")
	public @ResponseBody Object OutUserToken(HttpServletRequest request,   		 
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
			 	 LOG.info("merchantNo="+merchantNo+"\n date="+data+"\n sign="+sign);
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
	            if (!jsonObject.containsKey("service") || !jsonObject.containsKey("mobile") ) {
	            	return getMerchantErrorMap(version, charset, sign_type, CommonConstants.ERROR_VERI_CODE, "参数不完整", merchantNo, securityKey.getKey());
	            }
	            /** 重复提交验证，已经处理得订单 */
	            RestTemplate restTemplate = new RestTemplate();
	            URI uri = util.getServiceUrl("user", "error url request!");
	            String url = uri.toString() + "/v1.0/user/query/phone";
	            MultiValueMap<String, String> requestEntity = new LinkedMultiValueMap<String, String>();
	            requestEntity.add("phone", jsonObject.getString("mobile"));
	            String result = restTemplate.postForObject(url, requestEntity, String.class);
	            //result
	            JSONObject authObject = JSONObject.fromObject(result);
	            
	            JSONObject resultObj = authObject.getJSONObject("result");
	            Map<String,String> dataMap= new HashMap<String,String>();
	            if(authObject.getString("resp_code").equals(CommonConstants.SUCCESS)&&resultObj.getString("brandId").equals(securityKey.getBrandId())){
	            	 String token = TokenUtil.createToken(resultObj.getLong("id"), resultObj.getLong("brandId"), resultObj.getString("phone"));
	            	 dataMap.put("Token", token);
	            	 dataMap.put("BrandId", resultObj.getString("brandId"));
	            	 dataMap.put("userId", resultObj.getString("id"));
	            	 return getMerchantdSuccessMap(version, charset, sign_type, CommonConstants.SUCCESS, "查询成功", merchantNo, securityKey.getKey(),dataMap);
	            }else if(authObject.getString("resp_code").equals(CommonConstants.SUCCESS)){
	            	return getMerchantErrorMap(version, charset, sign_type, "00001", "用户已经进件了", merchantNo, securityKey.getKey());
	            }else{
	            	return getMerchantErrorMap(version, charset, sign_type, authObject.getString("resp_code"), authObject.getString("resp_message"), merchantNo, securityKey.getKey());
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
        tempmap.put("status", "1");
        tempmap.put("merchant_no", merchantid);
        tempmap.put("resp_code", error);
        tempmap.put("resp_message", msg);
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
        tempmap.put("status", "0");
        tempmap.put("merchant_no", merchantid);
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
