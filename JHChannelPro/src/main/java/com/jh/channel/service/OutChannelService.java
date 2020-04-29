package com.jh.channel.service;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import net.sf.json.JSONObject;

import org.apache.http.message.BasicNameValuePair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.stereotype.Controller;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestTemplate;

import cn.jh.common.tools.SignMd5Util;
import cn.jh.common.tools.Tools;
import cn.jh.common.utils.CommonConstants;
import cn.jh.common.utils.ExceptionUtil;
import cn.jh.common.utils.Md5Util;
import cn.jh.common.utils.TokenUtil;

import com.jh.channel.business.OutMerchantSecurityBusiness;
import com.jh.channel.pojo.OutMerchantPayRequest;
import com.jh.channel.pojo.OutMerchantPayResponse;
import com.jh.channel.pojo.OutMerchantSecurityKey;
import com.jh.channel.pojo.OutOrderInfo;
import com.jh.channel.util.HttpRequest;
import com.jh.channel.util.MD5Util;
import com.jh.channel.util.Util;

@Controller
@EnableAutoConfiguration
public class OutChannelService {

private static final Logger LOG = LoggerFactory.getLogger(OutChannelService.class);
	
	@Autowired
	Util util;
	
	@Autowired
	private OutMerchantSecurityBusiness securityBusiness;
	
	@Value("${pay.ipAddress}")
	private String ipAddress;
	
	@RequestMapping(method=RequestMethod.POST,value="/v1.0/channel/order/query/code")
	public @ResponseBody Object queryOrderBycode(HttpServletRequest request,   		 
			 @RequestParam(value = "merchant_no") String userid,
			 @RequestParam(value = "order_code") String merorder,			 
			 @RequestParam(value = "version") String version,
		 	 @RequestParam(value = "sign") String sign
			 ){
		Map map = new HashMap();
		/**根据商家的订单号获取提现的订单*/
		OutMerchantSecurityKey securityKey = securityBusiness.findOutMerchantSecurityKeyByUid(Long.parseLong(userid));
		
		List<BasicNameValuePair> nvps = new ArrayList<BasicNameValuePair>();
	    nvps.add(new BasicNameValuePair("merchant_no", userid));
	    nvps.add(new BasicNameValuePair("order_code", merorder));
	    nvps.add(new BasicNameValuePair("version", version));
	  
	    try {
			String createsign = SignMd5Util.signData(nvps, securityKey.getKey());
			
			if(!createsign.equalsIgnoreCase(sign)){
				
				map.put(CommonConstants.RESP_CODE,CommonConstants.ERROR_SIGN_NOVALID);
				map.put(CommonConstants.RESP_MESSAGE, "签名无效");
				return map;
			}
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	    
	    RestTemplate restTemplate=new RestTemplate();
		URI uri = util.getServiceUrl("transactionclear", "error url request!");
		String url = uri.toString() + "/v1.0/transactionclear/payment/query/outordercode";
		MultiValueMap<String, String> requestEntity  = new LinkedMultiValueMap<String, String>();
		requestEntity.add("order_code", merorder);
		String result = restTemplate.postForObject(url, requestEntity, String.class);
		LOG.info("RESULT================"+result);
		JSONObject jsonObject =  JSONObject.fromObject(result);
		JSONObject resultObj  =  jsonObject.getJSONObject("result");
		//String  thirdOrdercode  = resultObj.getString("thirdOrdercode");
		OutOrderInfo  orderInfo = new OutOrderInfo();
		orderInfo.setSysOrdercode(resultObj.getString("ordercode"));
		orderInfo.setOrderDesc(resultObj.getString("desc"));
		orderInfo.setAmount(resultObj.getString("amount"));
		orderInfo.setChannel(resultObj.getString("channelTag"));
		orderInfo.setExtraFee(resultObj.getString("extraFee"));
		orderInfo.setMerNo(resultObj.getString("phone"));
		orderInfo.setOrderCode(resultObj.getString("outMerOrdercode"));
		orderInfo.setOrderStatus(resultObj.getString("status"));
		orderInfo.setOrderType(resultObj.getString("type"));
		orderInfo.setRate(resultObj.getString("rate"));
		orderInfo.setRealAmount(resultObj.getString("realAmount"));
		orderInfo.setTime(resultObj.getString("updateTime"));
		
		map.put(CommonConstants.RESP_CODE,CommonConstants.SUCCESS);
		map.put(CommonConstants.RESP_MESSAGE, "查询成功");
		map.put(CommonConstants.RESULT, orderInfo);
		return map;
	}
	
	
	private OutMerchantPayResponse getMerchantErrorMap(String version, String charset, String signType, String error, String msg, String merchantid,
            String key) {

        Map tempmap = new HashMap();
        tempmap.put("version", version);
        tempmap.put("charset", charset);
        tempmap.put("sign_type", signType);
        tempmap.put("status", "-1");
        tempmap.put("merchant_id", merchantid);
        tempmap.put("err_code", error);
        tempmap.put("err_msg", msg);
        JSONObject tempJSONObject = JSONObject.fromObject(tempmap);
        String jsonStr = tempJSONObject.toString();
        String responsesign="";
		try {
			responsesign = MD5Util.getSignature(jsonStr, key);
		} catch (Exception e) {
//			e.printStackTrace();
			responsesign="";
		}
        OutMerchantPayResponse response = new OutMerchantPayResponse();
        response.setData(jsonStr);
        response.setSign(responsesign);
        response.setMerchant_id(merchantid);
        return response;

    }
	
	 private OutMerchantPayResponse getErrorMap(String version, String charset, String signType, String error, String msg, String merchantid,
	            String key) {
	        Map tempmap = new HashMap();
	        tempmap.put("version", version);
	        tempmap.put("charset", charset);
	        tempmap.put("sign_type", signType);
	        tempmap.put("status", "-1");
	        tempmap.put("merchant_id", merchantid);
	        tempmap.put("err_code", error);
	        tempmap.put("err_msg", msg);
	        JSONObject tempJSONObject = JSONObject.fromObject(tempmap);
	        String jsonStr = tempJSONObject.toString();
	        String responsesign = "";
	        try {
	            responsesign = MD5Util.getSignature(jsonStr, key);
	        } catch (Exception e) {
	        	responsesign="";
	        }
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
	        try {
	            responsesign = MD5Util.getSignature(jsonStr, key);
	        } catch (Exception e) {
	        	responsesign="";
	        }
	        OutMerchantPayResponse response = new OutMerchantPayResponse();
	        response.setData(jsonStr);
	        response.setSign(responsesign);
	        response.setMerchant_id(merchantid);
	        return response;

	    } 
	 
	
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/channel/pay/gateway")
    public @ResponseBody Object paymentRequest(HttpServletRequest request, 
    		@RequestParam(value = "data") String data,
            @RequestParam(value = "sign") String sign, 
            @RequestParam(value = "merchant_id") String merchantid) {
        
		OutMerchantSecurityKey securityKey = null;
		Map<String,String> dataMap= new HashMap<String,String>();
        try {

            String version = "1.0";
            String charset = "UTF-8";
            String sign_type = "MD5";
            securityKey = securityBusiness.findOutMerchantSecurityKeyByUid(Long.parseLong(merchantid));

            if (securityKey == null) {
            	return getMerchantErrorMap(version, charset, sign_type, "00017", "商家未开通", merchantid, "");
            }

            /** 签名验证 **/
            /** 根据商户号获取商户的私钥 */
            JSONObject jsonObject = JSONObject.fromObject(data);

            String createSign = "";
            try {
                createSign = MD5Util.getSignature(data, securityKey.getKey());
            } catch (Exception e) {
            	LOG.info("自身签名有误"+e.getMessage());
            	return getErrorMap(version, charset, sign_type, "00001", "签名失败", merchantid, securityKey.getKey());
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

            	return getErrorMap(version, charset, sign_type, "00001", "签名失败", merchantid, securityKey.getKey());
            }

            /** 参数验证 */
            if (!jsonObject.containsKey("service") || !jsonObject.containsKey("notify_url") || !jsonObject.containsKey("merchant_id")
                    || !jsonObject.containsKey("trade_no") || !jsonObject.containsKey("body") || !jsonObject.containsKey("total_fee")
                    || !jsonObject.containsKey("debit_card_identity_no")|| !jsonObject.containsKey("debit_card_no")
                    || !jsonObject.containsKey("debit_card_mobile") ||!jsonObject.containsKey("credit_card_name")
                    || !jsonObject.containsKey("credit_card_identity_no")|| !jsonObject.containsKey("credit_card_no")
                    || !jsonObject.containsKey("credit_card_mobile") || !jsonObject.containsKey("credit_card_expire_date")
                    || !jsonObject.containsKey("credit_card_cvn")||!jsonObject.containsKey("user_name")) {

            	return getErrorMap(version, charset, sign_type, "00002", "参数不完整", merchantid, securityKey.getKey());
            }

            /** 解构参数 */
            OutMerchantPayRequest payRequest = new OutMerchantPayRequest();
            payRequest.setBody(jsonObject.getString("body"));
            payRequest.setCharset(charset);
            payRequest.setService(jsonObject.getString("service"));
            payRequest.setVersion(version);
            payRequest.setMerchantId(jsonObject.getString("merchant_id"));
            if (jsonObject.containsKey("return_url")) {
                payRequest.setReturnURL(jsonObject.getString("return_url"));
            }
            payRequest.setNotifyURL(jsonObject.getString("notify_url"));
            payRequest.setSignType(sign_type);
            payRequest.setTradeNo(jsonObject.getString("trade_no"));
            if (jsonObject.containsKey("extra_param")) {
                payRequest.setExtraParam(jsonObject.getString("extra_param"));
            }
            payRequest.setTotalFee(jsonObject.getString("total_fee"));
            if (jsonObject.containsKey("fee_type")) {
                payRequest.setFeeType(jsonObject.getString("fee_type"));
            }

            /** 信息卡信息 */
            payRequest.setCreditCardCvn(jsonObject.getString("credit_card_cvn"));
            payRequest.setCreditCardExpireDate(jsonObject.getString("credit_card_expire_date"));
            payRequest.setCreditCardIdentityNo(jsonObject.getString("credit_card_identity_no"));
            payRequest.setCreditCardMobile(jsonObject.getString("credit_card_mobile"));
            payRequest.setUserName(jsonObject.getString("user_name"));
            payRequest.setCreditCardNo(jsonObject.getString("credit_card_no"));

            /** 借记卡信息 */
            payRequest.setDebitCardIdentityNo(jsonObject.getString("debit_card_identity_no"));
            payRequest.setDebitCardMobile(jsonObject.getString("credit_card_mobile"));
            payRequest.setDebitCardNo(jsonObject.getString("debit_card_no"));
            /***录入卡信息**/
            RestTemplate restTemplate = new RestTemplate();
            URI uri = util.getServiceUrl("paymentchannel", "error url request!");
            String url = uri.toString() + "/v1.0/paymentchannel/add/creditcardinfo";
            MultiValueMap<String, String> requestEntity = new LinkedMultiValueMap<String, String>();
            requestEntity.add("cardNo", payRequest.getCreditCardNo());
            requestEntity.add("userName", payRequest.getUserName());
            requestEntity.add("phone", payRequest.getCreditCardMobile());
            requestEntity.add("idCard", payRequest.getCreditCardIdentityNo());
            requestEntity.add("securityCode", payRequest.getCreditCardCvn());
            requestEntity.add("expiredTime", payRequest.getCreditCardExpireDate());
            requestEntity.add("bankName", payRequest.getCreditCardName());
            String result = restTemplate.postForObject(url, requestEntity, String.class);
            
            LOG.info("/v1.0/paymentchannel/add/creditcardinfo"+result);
            JSONObject tempjsonObject = JSONObject.fromObject(result);
            if (!tempjsonObject.getString("resp_code").equalsIgnoreCase(CommonConstants.SUCCESS)) {

                return getErrorMap(payRequest.getVersion(), payRequest.getCharset(), payRequest.getSignType(), tempjsonObject.getString("resp_code"), 
                		tempjsonObject.getString("resp_message"), merchantid,securityKey.getKey());
            }
            
            
            

            /** 提交验订单 */
            restTemplate = new RestTemplate();
            uri = util.getServiceUrl("transactionclear", "error url request!");
            url = uri.toString() + "/v1.0/transactionclear/create/outpaymentorder";
            requestEntity = new LinkedMultiValueMap<String, String>();
            requestEntity.add("userId", payRequest.getMerchantId());
            requestEntity.add("amount", payRequest.getTotalFee());
            requestEntity.add("channelTag", payRequest.getService());
            requestEntity.add("bankCard", payRequest.getCreditCardNo());
            requestEntity.add("debitBankCard", payRequest.getDebitCardNo());
            requestEntity.add("description", payRequest.getBody());
            requestEntity.add("outNotifyUrl", payRequest.getNotifyURL());
            requestEntity.add("outReturnUrl", payRequest.getReturnURL());
            requestEntity.add("outOrderCode", payRequest.getTradeNo());
            requestEntity.add("remark", payRequest.getBody());
            result = restTemplate.postForObject(url, requestEntity, String.class);
            LOG.info("/v1.0/transactionclear/create/outpaymentorder"+result);
            tempjsonObject = JSONObject.fromObject(result);
            JSONObject resultObject = tempjsonObject.getJSONObject(CommonConstants.RESULT);
            if (!tempjsonObject.getString("resp_code").equalsIgnoreCase(CommonConstants.SUCCESS)) {

            return getErrorMap(payRequest.getVersion(), payRequest.getCharset(), payRequest.getSignType(), tempjsonObject.getString("resp_code"), 
            		tempjsonObject.getString("resp_message"), merchantid,securityKey.getKey());
            }
            dataMap.put("order_code", resultObject.getString("ordercode"));
            dataMap.put("trade_no", payRequest.getTradeNo());
            dataMap.put("pay_url",ipAddress+"/v1.0/paymentchannel/out/topup/request?orderCode="+resultObject.getString("ordercode"));
            return getMerchantdSuccessMap(version, charset, payRequest.getSignType(), CommonConstants.SUCCESS, tempjsonObject.getString("resp_message"), merchantid, securityKey.getKey(), dataMap);
            	

        } catch (Exception e) {
        	e.printStackTrace();
        	ExceptionUtil.errInfo(e);
        	  return getErrorMap("1.0", "UTF-8", "MD5", "00016", "参数传递有误", merchantid, securityKey.getKey());
        	   
        }
    }
	
	
	
	@RequestMapping(method=RequestMethod.POST,value="/v1.0/channel/account/query/balance")
	public @ResponseBody Object queryBalance(HttpServletRequest request,   		 
			 @RequestParam(value = "merchant_no") String phone,
			 @RequestParam(value = "version") String version,
			 @RequestParam(value = "sign") String sign
			 ){
		
		Map map = new HashMap();
		/**根据商家的订单号获取提现的订单*/
		OutMerchantSecurityKey securityKey = securityBusiness.getOutMerchantSecurityKey(phone);
		
		List<BasicNameValuePair> nvps = new ArrayList<BasicNameValuePair>();
	    nvps.add(new BasicNameValuePair("merchant_no", phone));
	    //nvps.add(new BasicNameValuePair("order_code", merorder));
	    nvps.add(new BasicNameValuePair("version", version));
	  
	    try {
			String createsign = SignMd5Util.signData(nvps, securityKey.getKey());
			
			if(!createsign.equalsIgnoreCase(sign)){
				
				map.put(CommonConstants.RESP_CODE,CommonConstants.ERROR_SIGN_NOVALID);
				map.put(CommonConstants.RESP_MESSAGE, "签名无效");
				return map;
			}
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	    RestTemplate restTemplate=new RestTemplate();
		URI uri = util.getServiceUrl("user", "error url request!");
		String url = uri.toString() + "/v1.0/user/account/query/phone";
		MultiValueMap<String, String> requestEntity  = new LinkedMultiValueMap<String, String>();
		requestEntity.add("phone", phone);
		String result = restTemplate.postForObject(url, requestEntity, String.class);
		LOG.info("RESULT================"+result);
		JSONObject jsonObject =  JSONObject.fromObject(result);
		JSONObject resultObj  =  jsonObject.getJSONObject("result");
		String balance  =  resultObj.getString("balance");
	    
		map.put(CommonConstants.RESP_CODE,CommonConstants.SUCCESS);
		map.put(CommonConstants.RESP_MESSAGE, "查询成功");
		map.put(CommonConstants.RESULT, balance);
		
		return map;
	}
	
	@RequestMapping(method=RequestMethod.POST,value="/v1.0/channel/account/query/balance/history")
	public @ResponseBody Object queryBalanceHistory(HttpServletRequest request,   		 
			 @RequestParam(value = "merchant_no") String phone,
			 @RequestParam(value = "version") String version,
			 @RequestParam(value = "start_time") String startTime,
			 @RequestParam(value = "end_time") String endTime,
			 @RequestParam(value = "sign") String sign
			 ){
		
		Map map = new HashMap();
		/**根据商家的订单号获取提现的订单*/
		OutMerchantSecurityKey securityKey = securityBusiness.getOutMerchantSecurityKey(phone);
		
		List<BasicNameValuePair> nvps = new ArrayList<BasicNameValuePair>();
	    nvps.add(new BasicNameValuePair("merchant_no", phone));
	    nvps.add(new BasicNameValuePair("start_time", startTime));
	    nvps.add(new BasicNameValuePair("end_time", endTime));
	    nvps.add(new BasicNameValuePair("version", version));
	  
	    try {
			String createsign = SignMd5Util.signData(nvps, securityKey.getKey());
			
			if(!createsign.equalsIgnoreCase(sign)){
				
				map.put(CommonConstants.RESP_CODE,CommonConstants.ERROR_SIGN_NOVALID);
				map.put(CommonConstants.RESP_MESSAGE, "签名无效");
				return map;
			}
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		
		
		return null;
	}
	
	
	@RequestMapping(method=RequestMethod.POST,value="/v1.0/channel/order/query/time")
	public @ResponseBody Object queryOrderPage(HttpServletRequest request,   		 
			 @RequestParam(value = "merchant_no") String phone,
			 @RequestParam(value = "version") String version,
			 @RequestParam(value = "start_time") String startTime,
			 @RequestParam(value = "end_time") String endTime,
			 @RequestParam(value = "sign") String sign
			 ){
		
		Map map = new HashMap();
		/**根据商家的订单号获取提现的订单*/
		OutMerchantSecurityKey securityKey = securityBusiness.getOutMerchantSecurityKey(phone);
		
		List<BasicNameValuePair> nvps = new ArrayList<BasicNameValuePair>();
	    nvps.add(new BasicNameValuePair("merchant_no", phone));
	    nvps.add(new BasicNameValuePair("start_time", startTime));
	    nvps.add(new BasicNameValuePair("end_time", endTime));
	    nvps.add(new BasicNameValuePair("version", version));
	  
	    try {
			String createsign = SignMd5Util.signData(nvps, securityKey.getKey());
			
			if(!createsign.equalsIgnoreCase(sign)){
				
				map.put(CommonConstants.RESP_CODE,CommonConstants.ERROR_SIGN_NOVALID);
				map.put(CommonConstants.RESP_MESSAGE, "签名无效");
				return map;
			}
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return null;
	}
	
	
	@RequestMapping(method=RequestMethod.POST,value="/v1.0/channel/withdraw/query")
	public @ResponseBody Object withdrawQuery(HttpServletRequest request,   		 
			 @RequestParam(value = "merchant_no") String phone,
			 @RequestParam(value = "order_code") String merorder,			 
			 @RequestParam(value = "version") String version,
		 	 @RequestParam(value = "sign") String sign
			 ){
		Map map = new HashMap();
		/**根据商家的订单号获取提现的订单*/
		OutMerchantSecurityKey securityKey = securityBusiness.getOutMerchantSecurityKey(phone);
		
		List<BasicNameValuePair> nvps = new ArrayList<BasicNameValuePair>();
	    nvps.add(new BasicNameValuePair("merchant_no", phone));
	    nvps.add(new BasicNameValuePair("order_code", merorder));
	    nvps.add(new BasicNameValuePair("version", version));
	  
	    try {
			String createsign = SignMd5Util.signData(nvps, securityKey.getKey());
			
			if(!createsign.equalsIgnoreCase(sign)){
				
				map.put(CommonConstants.RESP_CODE,CommonConstants.ERROR_SIGN_NOVALID);
				map.put(CommonConstants.RESP_MESSAGE, "签名无效");
				return map;
			}
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	    RestTemplate restTemplate=new RestTemplate();
		URI uri = util.getServiceUrl("transactionclear", "error url request!");
		String url = uri.toString() + "/v1.0/transactionclear/payment/query/outordercode";
		MultiValueMap<String, String> requestEntity  = new LinkedMultiValueMap<String, String>();
		requestEntity.add("order_code", merorder);
		String result = restTemplate.postForObject(url, requestEntity, String.class);
		LOG.info("RESULT================"+result);
		JSONObject jsonObject =  JSONObject.fromObject(result);
		JSONObject resultObj  =  jsonObject.getJSONObject("result");
		String  thirdOrdercode  = resultObj.getString("thirdOrdercode");
		String  channeltag      = resultObj.getString("channelTag");
		long  brandid  = resultObj.getLong("brandid");	
		
		/**直接调用**/
		uri = util.getServiceUrl("paymentchannel", "error url request!");
		url = uri.toString() + "/v1.0/paymentchannel/pay/query";
		
		requestEntity  = new LinkedMultiValueMap<String, String>();
		requestEntity.add("ordercode", thirdOrdercode);
		requestEntity.add("brandcode", brandid+"");
		requestEntity.add("channel_type", "2");
		requestEntity.add("channel_tag", channeltag);
		result = restTemplate.postForObject(url, requestEntity, String.class);
		jsonObject =  JSONObject.fromObject(result);
		resultObj  =  jsonObject.getJSONObject("result");
		String respcode =  resultObj.getString("rescode");
		String reqcode  =  resultObj.getString("reqcode");
		
		if(reqcode.equalsIgnoreCase(CommonConstants.SUCCESS)){ 
			
			
			if(respcode.equalsIgnoreCase(CommonConstants.SUCCESS)){
				
				map.put(CommonConstants.RESP_CODE,CommonConstants.SUCCESS);
				map.put(CommonConstants.RESP_MESSAGE, "代付成功");
				
				
			}else if(respcode.equalsIgnoreCase(CommonConstants.WAIT_CHECK)){
			
				
				map.put(CommonConstants.RESP_CODE,CommonConstants.WAIT_CHECK);
				map.put(CommonConstants.RESP_MESSAGE, "等待处理");
			}else{
				
				
				map.put(CommonConstants.RESP_CODE,CommonConstants.FALIED);
				map.put(CommonConstants.RESP_MESSAGE, "代付失败");
				
			}
			
		}else{
			
			
			map.put(CommonConstants.RESP_CODE,CommonConstants.FALIED);
			map.put(CommonConstants.RESP_MESSAGE, "请求失败");
		}
		
		
		return map;
	}
	
	
	
	
	@RequestMapping(method=RequestMethod.POST,value="/v1.0/channel/withdraw/req")
	public @ResponseBody Object withdraw(HttpServletRequest request,   		 
			 @RequestParam(value = "merchant_no") String phone,
			 @RequestParam(value = "amount") String amount,
			 @RequestParam(value = "channel_tag", required=false, defaultValue = "YILIAN") String channeltag,
			 @RequestParam(value = "order_desc") String orderdesc,
			 @RequestParam(value = "order_code") String merordercode,
			 @RequestParam(value = "version") String version,
			 @RequestParam(value = "card_no") String cardno,
			 @RequestParam(value = "user_name") String username,
			 @RequestParam(value = "bank_name") String bankname,			 
			 @RequestParam(value = "sign") String sign
			 ){
		
		try {
			orderdesc =  URLDecoder.decode(orderdesc, "UTF-8");
			username =  URLDecoder.decode(username, "UTF-8");
			bankname =  URLDecoder.decode(bankname, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		Map map = new HashMap();
		/**根据商户号获取商户的私钥*/
		OutMerchantSecurityKey securityKey = securityBusiness.getOutMerchantSecurityKey(phone);
		
		List<BasicNameValuePair> nvps = new ArrayList<BasicNameValuePair>();
	    nvps.add(new BasicNameValuePair("merchant_no", phone));
	    nvps.add(new BasicNameValuePair("amount", amount));
	    nvps.add(new BasicNameValuePair("channel_tag", channeltag));
	    nvps.add(new BasicNameValuePair("order_desc", orderdesc));
	    nvps.add(new BasicNameValuePair("order_code", merordercode));
	    nvps.add(new BasicNameValuePair("card_no", cardno));	
	    nvps.add(new BasicNameValuePair("bank_name", bankname));
	    nvps.add(new BasicNameValuePair("user_name", username));
	    //nvps.add(new BasicNameValuePair("return_url", returnURL));	
		nvps.add(new BasicNameValuePair("version", version));	
		try {
			String createsign = SignMd5Util.signData(nvps, securityKey.getKey());
			
			if(!createsign.equalsIgnoreCase(sign)){
				
				map.put(CommonConstants.RESP_CODE,CommonConstants.ERROR_SIGN_NOVALID);
				map.put(CommonConstants.RESP_MESSAGE, "签名无效");
				return map;
			}
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		if(Tools.checkAmount(amount) == false){
			
			map.put(CommonConstants.RESP_CODE,CommonConstants.ERROR_AMOUNT_ERROR);
			map.put(CommonConstants.RESP_MESSAGE, "支付金额有错");
			return map;
		}
		
		/**首先看在不在黑名单里面，如果在不能登录*/
		RestTemplate restTemplate=new RestTemplate();
		URI uri = util.getServiceUrl("risk", "error url request!");
		String url = uri.toString() + "/v1.0/risk/blackwhite/query/phone";
		MultiValueMap<String, String> requestEntity  = new LinkedMultiValueMap<String, String>();
		requestEntity.add("phone", phone);
		/**0为登陆操作*/
		requestEntity.add("operation_type", "2");
		String result = restTemplate.postForObject(url, requestEntity, String.class);
		LOG.info("RESULT================"+result);
		JSONObject jsonObject =  JSONObject.fromObject(result);
		String rescode  =  jsonObject.getString("resp_code");
		if(!rescode.equalsIgnoreCase(CommonConstants.SUCCESS)){
			map.put(CommonConstants.RESP_CODE,CommonConstants.ERROR_USER_BLACK);
			map.put(CommonConstants.RESP_MESSAGE, "用户在黑名单中");
    		return map;	
		}
		
		
		    /**判断订单是否存在*/
		   restTemplate=new RestTemplate();
			uri = util.getServiceUrl("transactionclear", "error url request!");
			url = uri.toString() + "/v1.0/transactionclear/payment/query/outordercode";
			requestEntity  = new LinkedMultiValueMap<String, String>();
			requestEntity.add("order_code", merordercode);
			result = restTemplate.postForObject(url, requestEntity, String.class);
			LOG.info("RESULT================"+result);
			jsonObject =  JSONObject.fromObject(result);
			JSONObject resultObj  =  jsonObject.getJSONObject("result");
		    if(resultObj != null  && !resultObj.isNullObject()){
		    	
		    	map.put(CommonConstants.RESP_CODE,CommonConstants.ERROR_WITHDRAW_ORDER_HASREQ);
				map.put(CommonConstants.RESP_MESSAGE, "订单不能重复提交");
	    		return map;
		    	
		    }
		
			/**调用下单，需要得到用户的订单信息*/
			uri = util.getServiceUrl("transactionclear", "error url request!");
			url = uri.toString() + "/v1.0/transactionclear/payment/add";
			
			/**根据的用户手机号码查询用户的基本信息*/
			requestEntity  = new LinkedMultiValueMap<String, String>();
			requestEntity.add("type", "2");
			requestEntity.add("phone", phone);
			requestEntity.add("amount", amount);
			requestEntity.add("channel_tag", channeltag);
			requestEntity.add("desc", orderdesc);
			requestEntity.add("out_order_code", merordercode);
			result = restTemplate.postForObject(url, requestEntity, String.class);
			LOG.info("RESULT================"+result);
			jsonObject =  JSONObject.fromObject(result);
			resultObj  =  jsonObject.getJSONObject("result");
			String  order  = resultObj.getString("ordercode");
			long  brandid  = resultObj.getLong("brandid");	
			long  userid   = resultObj.getLong("userid");
			String realAmount  = resultObj.getString("realAmount");
			
			
			/*restTemplate=new RestTemplate();
			uri = util.getServiceUrl("user", "error url request!");
			url = uri.toString() + "/v1.0/user/account/query/phone";
			requestEntity  = new LinkedMultiValueMap<String, String>();
			requestEntity.add("phone", phone);
			result = restTemplate.postForObject(url, requestEntity, String.class);
			LOG.info("RESULT================"+result);
			jsonObject =  JSONObject.fromObject(result);
			resultObj  =  jsonObject.getJSONObject("result");
			String balance  =  resultObj.getString("balance");
			
			
			*//**判断用户的真实提现金额和余额比较**//*
			if(new BigDecimal(realAmount).compareTo(new BigDecimal(balance)) > 0){
				
				map.put(CommonConstants.RESP_CODE,CommonConstants.ERROR_WITHDRAW_BALANCE_NO_ENOUGH);
				map.put(CommonConstants.RESP_MESSAGE, "用户的余额不充足");
				return map;
				
			}	*/
			
			restTemplate=new RestTemplate();
			uri = util.getServiceUrl("user", "error url request!");
			url = uri.toString() + "/v1.0/user/account/withdraw/freeze";
			requestEntity  = new LinkedMultiValueMap<String, String>();
			requestEntity.add("user_id", userid+"");
			requestEntity.add("realamount", realAmount);
			requestEntity.add("order_code", order);
			result = restTemplate.postForObject(url, requestEntity, String.class);
			LOG.info("RESULT================"+result);
			jsonObject =  JSONObject.fromObject(result);
			//resultObj  =  jsonObject.getJSONObject("result");
			String withdrawrespcode  =  jsonObject.getString("resp_code");
			if(withdrawrespcode.equalsIgnoreCase("999999")){
				map.put(CommonConstants.RESP_CODE,CommonConstants.ERROR_WITHDRAW_BALANCE_NO_ENOUGH);
				map.put(CommonConstants.RESP_MESSAGE, "用户的余额不充足");
				return map;
			}
			
			
			uri = util.getServiceUrl("paymentchannel", "error url request!");
			url = uri.toString() + "/v1.0/paymentchannel/pay/request";
			
			/**根据的用户手机号码查询用户的基本信息*/
			requestEntity  = new LinkedMultiValueMap<String, String>();
			requestEntity.add("amount", amount);
			requestEntity.add("ordercode", order);
			requestEntity.add("brandcode", brandid+"");
			requestEntity.add("cardno", cardno);
			requestEntity.add("username", username);
			requestEntity.add("bankname", bankname);
			requestEntity.add("phone", phone);
			requestEntity.add("channel_type", "1");
			requestEntity.add("channel_tag", channeltag);
			result = restTemplate.postForObject(url, requestEntity, String.class);

			LOG.info("RESULT================"+result);
			jsonObject =  JSONObject.fromObject(result);
			
			/**如果不是正常返回*/
			if(!jsonObject.getString("resp_code").equalsIgnoreCase("000000")){
				
				/**提现失败的解冻*/
				uri = util.getServiceUrl("user", "error url request!");
				url = uri.toString() + "/v1.0/user/account/freeze";
				requestEntity  = new LinkedMultiValueMap<String, String>();
				requestEntity.add("order_code", order);
				requestEntity.add("user_id", userid+"");
				requestEntity.add("amount", amount);
				requestEntity.add("add_or_sub", "1");
				result = restTemplate.postForObject(url, requestEntity, String.class);
				
				map.put(CommonConstants.RESP_CODE, jsonObject.getString("resp_code"));
				map.put(CommonConstants.RESP_MESSAGE, jsonObject.getString("resp_message"));
				return map;
				
				
			}
			
			resultObj  =  jsonObject.getJSONObject("result");
			String thirdordercode  =  resultObj.getString("thirdordercode");
			String respcode =  resultObj.getString("reqcode");
			if(respcode.equalsIgnoreCase("000000")){
				
				
				/**更新订单, 将第三方的订单号加到订单里面去*/
				uri = util.getServiceUrl("transactionclear", "error url request!");
				url = uri.toString() + "/v1.0/transactionclear/payment/update";
				
				/**根据的用户手机号码查询用户的基本信息*/
				requestEntity  = new LinkedMultiValueMap<String, String>();
				requestEntity.add("order_code", order);
				requestEntity.add("third_code", thirdordercode);
				result = restTemplate.postForObject(url, requestEntity, String.class);
				
				
				/**直接调用**/
				uri = util.getServiceUrl("paymentchannel", "error url request!");
				url = uri.toString() + "/v1.0/paymentchannel/pay/query";
				
				/**根据的用户手机号码查询用户的基本信息*/
				requestEntity  = new LinkedMultiValueMap<String, String>();
				requestEntity.add("ordercode", thirdordercode);
				requestEntity.add("brandcode", brandid+"");
				requestEntity.add("channel_type", "2");
				requestEntity.add("channel_tag", channeltag);
				result = restTemplate.postForObject(url, requestEntity, String.class);
				jsonObject =  JSONObject.fromObject(result);
				resultObj  =  jsonObject.getJSONObject("result");
				respcode =  resultObj.getString("rescode");
				String reqcode  =  resultObj.getString("reqcode");
				map.put(CommonConstants.RESULT, thirdordercode);
				
				String status = "0";
				
				if(reqcode.equalsIgnoreCase(CommonConstants.SUCCESS)){ 
						
						
					if(respcode.equalsIgnoreCase(CommonConstants.SUCCESS)){
						
						/**回调商家的交易处理页面*/
						status = "1";
						map.put(CommonConstants.RESP_CODE,CommonConstants.SUCCESS);
						map.put(CommonConstants.RESP_MESSAGE, "体现成功");
						
					}else if(respcode.equalsIgnoreCase(CommonConstants.WAIT_CHECK)){
					
						status = "3";
					
						/*uri = util.getServiceUrl("user", "error url request!");
						url = uri.toString() + "/v1.0/user/account/freeze";
						requestEntity  = new LinkedMultiValueMap<String, String>();
						requestEntity.add("order_code", order);
						requestEntity.add("user_id", userid+"");
						requestEntity.add("amount", amount);
						result = restTemplate.postForObject(url, requestEntity, String.class);*/
						
						
						map.put(CommonConstants.RESP_CODE,CommonConstants.WAIT_CHECK);
						map.put(CommonConstants.RESP_MESSAGE, "等待处理");
					}else{
						
						status = "2";
						
						map.put(CommonConstants.RESP_CODE,CommonConstants.FALIED);
						map.put(CommonConstants.RESP_MESSAGE, "提现失败");
						
					}
					
				}else{
					
					status = "2";
					
					map.put(CommonConstants.RESP_CODE,CommonConstants.FALIED);
					map.put(CommonConstants.RESP_MESSAGE, "请求失败");
				}
				
				
				uri = util.getServiceUrl("transactionclear", "error url request!");
				url = uri.toString() + "/v1.0/transactionclear/payment/update";
				
				/**根据的用户手机号码查询用户的基本信息*/
				requestEntity  = new LinkedMultiValueMap<String, String>();
				requestEntity.add("order_code", order);
				requestEntity.add("third_code", thirdordercode);
				requestEntity.add("status", status);
				result = restTemplate.postForObject(url, requestEntity, String.class);
				
				return map;
				
			}else{
				
				/**提现失败的解冻*/
				uri = util.getServiceUrl("user", "error url request!");
				url = uri.toString() + "/v1.0/user/account/freeze";
				requestEntity  = new LinkedMultiValueMap<String, String>();
				requestEntity.add("order_code", order);
				requestEntity.add("user_id", userid+"");
				requestEntity.add("amount", amount);
				requestEntity.add("add_or_sub", "1");
				result = restTemplate.postForObject(url, requestEntity, String.class);
				
				
				map.put(CommonConstants.RESP_CODE,CommonConstants.ERROR_WITHDRAW_REQ_FAILD);
				map.put(CommonConstants.RESP_MESSAGE, "下单失败");
				
				return map;
				
			}
	}
	
	
	@RequestMapping(method=RequestMethod.POST,value="/v1.0/channel/topup")
	public @ResponseBody Object topup(HttpServletRequest request,   		 
			 @RequestParam(value = "merchant_no") String phone,
			 @RequestParam(value = "amount") String amount,
			 @RequestParam(value = "channel_tag") String channeltag,
			 @RequestParam(value = "order_desc") String orderdesc,
			 @RequestParam(value = "order_code") String merordercode,
			 @RequestParam(value = "notify_url") String notifyURL,
			 @RequestParam(value = "version") String version,
			 @RequestParam(value = "sign") String sign
			 ){
		
		/**验证签名*/
		/**order_desc,  notify_url, return_url 需要用utf-8解码**/
		try {
			orderdesc =  URLDecoder.decode(orderdesc, "UTF-8");
			notifyURL =  URLDecoder.decode(notifyURL, "UTF-8");
			//returnURL =  URLDecoder.decode(returnURL, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Map map = new HashMap();
		/**根据商户号获取商户的私钥*/
		OutMerchantSecurityKey securityKey = securityBusiness.getOutMerchantSecurityKey(phone);
		
		List<BasicNameValuePair> nvps = new ArrayList<BasicNameValuePair>();
	    nvps.add(new BasicNameValuePair("merchant_no", phone));
	    nvps.add(new BasicNameValuePair("amount", amount));
	    nvps.add(new BasicNameValuePair("channel_tag", channeltag));
	    nvps.add(new BasicNameValuePair("order_desc", orderdesc));
	    nvps.add(new BasicNameValuePair("order_code", merordercode));
	    nvps.add(new BasicNameValuePair("notify_url", notifyURL));	
	    //nvps.add(new BasicNameValuePair("return_url", returnURL));	
		nvps.add(new BasicNameValuePair("version", version));	
		try {
			String createsign = SignMd5Util.signData(nvps, securityKey.getKey());
			
			if(!createsign.equalsIgnoreCase(sign)){
				
				map.put(CommonConstants.RESP_CODE,CommonConstants.ERROR_SIGN_NOVALID);
				map.put(CommonConstants.RESP_MESSAGE, "签名无效");
				return map;
			}
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		

		
		/**首先看在不在黑名单里面，如果在不能登录*/
		RestTemplate restTemplate=new RestTemplate();
		URI uri = util.getServiceUrl("risk", "error url request!");
		String url = uri.toString() + "/v1.0/risk/blackwhite/query/phone";
		MultiValueMap<String, String> requestEntity  = new LinkedMultiValueMap<String, String>();
		requestEntity.add("phone", phone);
		/**0为登陆操作*/
		requestEntity.add("operation_type", "1");
		String result = restTemplate.postForObject(url, requestEntity, String.class);
		LOG.info("RESULT================"+result);
		JSONObject jsonObject =  JSONObject.fromObject(result);
		String rescode  =  jsonObject.getString("resp_code");
		if(!rescode.equalsIgnoreCase(CommonConstants.SUCCESS)){
			map.put(CommonConstants.RESP_CODE,CommonConstants.ERROR_USER_BLACK);
			map.put(CommonConstants.RESP_MESSAGE, "用户在黑名单中");
    		return map;	
		}
		
		
		
		/**调用下单，需要得到用户的订单信息*/
		uri = util.getServiceUrl("transactionclear", "error url request!");
		url = uri.toString() + "/v1.0/transactionclear/payment/add";
		
		/**根据的用户手机号码查询用户的基本信息*/
		requestEntity  = new LinkedMultiValueMap<String, String>();
		requestEntity.add("type", "0");
		requestEntity.add("phone", phone);
		requestEntity.add("amount", amount);
		requestEntity.add("channel_tag", channeltag);
		requestEntity.add("desc", orderdesc);
		requestEntity.add("notify_url", notifyURL);
		//requestEntity.add("return_url", returnURL);
		requestEntity.add("out_order_code", merordercode);
		result = restTemplate.postForObject(url, requestEntity, String.class);
		LOG.info("RESULT================"+result);
		jsonObject =  JSONObject.fromObject(result);
		JSONObject resultObj  =  jsonObject.getJSONObject("result");
		String  order  = resultObj.getString("ordercode");
		long  brandid  = resultObj.getLong("brandid");
		
		uri = util.getServiceUrl("paymentchannel", "error url request!");
		url = uri.toString() + "/v1.0/paymentchannel/topup/request";
		
		/**根据的用户手机号码查询用户的基本信息*/
		requestEntity  = new LinkedMultiValueMap<String, String>();
		requestEntity.add("amount", amount);
		requestEntity.add("ordercode", order);
		requestEntity.add("brandcode", brandid+"");
		requestEntity.add("orderdesc", orderdesc);
		requestEntity.add("channel_tag", channeltag);		
		result = restTemplate.postForObject(url, requestEntity, String.class);
		LOG.info("RESULT================"+result);
		jsonObject =  JSONObject.fromObject(result);
		String redirecturl  =  jsonObject.getString("result");
		
		
		map.put(CommonConstants.RESP_CODE,CommonConstants.SUCCESS);
		map.put(CommonConstants.RESP_MESSAGE, "成功");
		map.put(CommonConstants.RESULT, redirecturl);
		return map;
	}
	
	
	
	
	
	
}
