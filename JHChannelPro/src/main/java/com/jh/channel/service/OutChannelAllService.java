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
public class OutChannelAllService {

private static final Logger LOG = LoggerFactory.getLogger(OutChannelAllService.class);
	
	@Autowired
	Util util;
	
	@Autowired
	private OutMerchantSecurityBusiness securityBusiness;
	
	
	@RequestMapping(method=RequestMethod.POST,value="/v1.0/channel/order/query/code11")
	public @ResponseBody Object queryOrderBycode(HttpServletRequest request,   		 
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
        String responsesign = "";
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
	            // TODO Auto-generated catch block
	            e.printStackTrace();
	        }
	        OutMerchantPayResponse response = new OutMerchantPayResponse();
	        response.setData(jsonStr);
	        response.setSign(responsesign);
	        response.setMerchant_id(merchantid);
	        return response;

	    }
	
	 private OutMerchantPayResponse getSuccessMap(String version, String charset, String signType, String merchantid, String key,
	            String transactionId) {

	        Map tempmap = new HashMap();
	        tempmap.put("version", version);
	        tempmap.put("charset", charset);
	        tempmap.put("sign_type", signType);
	        tempmap.put("status", "0");
	        tempmap.put("merchant_id", merchantid);
	        tempmap.put("transaction_id", transactionId);
	        JSONObject tempJSONObject = JSONObject.fromObject(tempmap);
	        String jsonStr = tempJSONObject.toString();
	        String responsesign = "";
	        try {
	            responsesign = MD5Util.getSignature(jsonStr, key);
	        } catch (Exception e) {
	            // TODO Auto-generated catch block
	            e.printStackTrace();
	        }
	        OutMerchantPayResponse response = new OutMerchantPayResponse();
	        response.setData(jsonStr);
	        response.setSign(responsesign);
	        response.setMerchant_id(merchantid);
	        return response;

	    }
	 
	   
	 
	 /****/
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/channel/pay/gateway1")
    public @ResponseBody String paymentRequest(HttpServletRequest request, 
    		@RequestParam(value = "data") String data,
            @RequestParam(value = "sign") String sign, 
            @RequestParam(value = "merchant_id") String merchantid) {
		OutMerchantSecurityKey securityKey = null;
        try {
            String version = "1.0";
            String charset = "UTF-8";
            String sign_type = "MD5";
            securityKey = securityBusiness.getOutMerchantSecurityKey(merchantid);

            if (securityKey == null) {
            	//return getMerchantErrorMap(version, charset, sign_type, "00017", "商家未开通", merchantid, "");
            	return null;
            }
            /** 签名验证 **/
            /** 根据商户号获取商户的私钥 */
            JSONObject jsonObject = JSONObject.fromObject(data);
            String createSign = "";
            try {
                createSign = MD5Util.getSignature(data, securityKey.getKey());
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
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
//            	return getErrorMap(version, charset, sign_type, "00001", "签名失败", merchantid, securityKey.getKey());
            	return null;
            }
            /** 参数验证 */
            if (!jsonObject.containsKey("service") || !jsonObject.containsKey("notify_url") || !jsonObject.containsKey("merchant_id")
                    || !jsonObject.containsKey("trade_no") || !jsonObject.containsKey("body") || !jsonObject.containsKey("total_fee")
                    || !jsonObject.containsKey("merchant_ip") || !jsonObject.containsKey("debit_card_name")
                    || !jsonObject.containsKey("debit_card_identity_no") || !jsonObject.containsKey("debit_card_no")
                    || !jsonObject.containsKey("debit_card_mobile") || !jsonObject.containsKey("credit_card_name")
                    || !jsonObject.containsKey("credit_card_identity_no") || !jsonObject.containsKey("credit_card_no")
                    || !jsonObject.containsKey("credit_card_mobile") || !jsonObject.containsKey("credit_card_expire_date")
                    || !jsonObject.containsKey("credit_card_cvn")) {
            	//return getErrorMap(version, charset, sign_type, "00002", "参数不完整", merchantid, securityKey.getKey());
            	return null;
            }
            /** 解构参数 */
            OutMerchantPayRequest payRequest = new OutMerchantPayRequest();
            payRequest.setBody(jsonObject.getString("body"));
            payRequest.setCharset(charset);
            payRequest.setService(jsonObject.getString("service"));
            payRequest.setVersion(version);
            payRequest.setMerchantId(jsonObject.getString("merchant_id"));
            payRequest.setDebitProvince(jsonObject.getString("province"));
            payRequest.setDebitCity(jsonObject.getString("city"));
            payRequest.setDebitAddress(jsonObject.getString("address"));
            payRequest.setDebitBankBranchId(jsonObject.getString("bank_branch_id"));
            payRequest.setDebitBranchName(jsonObject.getString("branch_name"));
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
            payRequest.setMerchantIP(jsonObject.getString("merchant_ip"));
            /** 信息卡信息 */
            payRequest.setCreditCardCvn(jsonObject.getString("credit_card_cvn"));
            payRequest.setCreditCardExpireDate(jsonObject.getString("credit_card_expire_date"));
            payRequest.setCreditCardIdentityNo(jsonObject.getString("credit_card_identity_no"));
            payRequest.setCreditCardMobile(jsonObject.getString("credit_card_mobile"));
            payRequest.setCreditCardName(jsonObject.getString("credit_card_name"));
            payRequest.setCreditCardNo(jsonObject.getString("credit_card_no"));
            /** 借记卡信息 */
            payRequest.setDebitCardIdentityNo(jsonObject.getString("credit_card_identity_no"));
            payRequest.setDebitCardMobile(jsonObject.getString("credit_card_mobile"));
            payRequest.setDebitCardName(jsonObject.getString("debit_card_name"));
            payRequest.setDebitCardNo(jsonObject.getString("debit_card_no"));
            if (jsonObject.containsKey("is_public")) {
                payRequest.setIsPublic(jsonObject.getString("is_public"));
            }
            /** 重复提交验证，已经处理得订单 */
            RestTemplate restTemplate = new RestTemplate();
            URI uri = util.getServiceUrl("transactionclear", "error url request!");
            String url = uri.toString() + "/v1.0/transactionclear/payment/query/outordercode";
            MultiValueMap<String, String> requestEntity = new LinkedMultiValueMap<String, String>();
            requestEntity.add("order_code", payRequest.getTradeNo());
            String result = restTemplate.postForObject(url, requestEntity, String.class);
            JSONObject tempjsonObject = JSONObject.fromObject(result);
            if (tempjsonObject.getString("resp_code").equalsIgnoreCase(CommonConstants.SUCCESS)) {
            /*return getErrorMap(payRequest.getVersion(), payRequest.getCharset(), payRequest.getSignType(), "00003", "订单重复提交", merchantid,
                    securityKey.getKey());*/
            	return null;
            }
            /** 同卡进出验证 */
            if (!payRequest.getCreditCardIdentityNo().equalsIgnoreCase(payRequest.getDebitCardIdentityNo())) {
					/*            return getErrorMap(payRequest.getVersion(), payRequest.getCharset(), payRequest.getSignType(), "00004", "结算卡与交易卡必须同名", merchantid,
					                    securityKey.getKey());*/
            	return null;
            }
            /** 判断用户的手机号码存在， 不存在直接创建 **/
            restTemplate = new RestTemplate();
            uri = util.getServiceUrl("user", "error url request!");
            url = uri.toString() + "/v1.0/user/query/phonebrand";
            requestEntity = new LinkedMultiValueMap<String, String>();
            requestEntity.add("phone", payRequest.getCreditCardMobile());
            requestEntity.add("brandid", securityKey.getBrandId());
            result = restTemplate.postForObject(url, requestEntity, String.class);
            tempjsonObject = JSONObject.fromObject(result);
            JSONObject authObject = JSONObject.fromObject(tempjsonObject);
            JSONObject resultObj = authObject.getJSONObject("result");
            String brandid = "2";
            String userid = "";
            if (resultObj.isNullObject()) {
                /** 创建用户 */
                restTemplate = new RestTemplate();
                uri = util.getServiceUrl("user", "error url request!");
                url = uri.toString() + "/v1.0/user/outchannel/new";
                requestEntity = new LinkedMultiValueMap<String, String>();
                requestEntity.add("phone", payRequest.getCreditCardMobile());
                requestEntity.add("pre_phone", payRequest.getMerchantId());
                requestEntity.add("brand_id", securityKey.getBrandId());
                result = restTemplate.postForObject(url, requestEntity, String.class);
                tempjsonObject = JSONObject.fromObject(result);
                authObject = JSONObject.fromObject(tempjsonObject);
                resultObj = authObject.getJSONObject("result");
                brandid = resultObj.getString("brandId");
                userid = resultObj.getString("id");
            } else {
                brandid = resultObj.getString("brandId");
                userid = resultObj.getString("id");
            }
            /** 借记卡四要素验证， */
            restTemplate = new RestTemplate();
            uri = util.getServiceUrl("paymentchannel", "error url request!");
            
            String token = TokenUtil.createToken(Long.parseLong(userid), Long.parseLong(brandid), payRequest.getCreditCardMobile());
            
            url = uri.toString() + "/v1.0/user/bank/add/"+token;
            requestEntity = new LinkedMultiValueMap<String, String>();
            requestEntity.add("realname", payRequest.getDebitCardName());
            requestEntity.add("idcard", payRequest.getDebitCardIdentityNo());
            requestEntity.add("bankcard", payRequest.getDebitCardNo());
            requestEntity.add("mobile", payRequest.getDebitCardMobile());
            requestEntity.add("type", "2");
            result = restTemplate.postForObject(url, requestEntity, String.class);
            tempjsonObject = JSONObject.fromObject(result);
            authObject = JSONObject.fromObject(tempjsonObject);
            resultObj = authObject.getJSONObject("result");
            if (!resultObj.getString("resCode").equalsIgnoreCase("1")) { 
            	
            /*	return getErrorMap(payRequest.getVersion(), payRequest.getCharset(),
                    payRequest.getSignType(), "00005", "结算卡验证无效", merchantid, securityKey.getKey()); 
            */	
            	return null;
            }

            /** 信用卡四要素验证 **/
            restTemplate = new RestTemplate();
            uri = util.getServiceUrl("paymentchannel", "error url request!");
            url = uri.toString() + "/v1.0/user/bank/add/"+token;
            requestEntity = new LinkedMultiValueMap<String, String>();
            requestEntity.add("realname", payRequest.getCreditCardName());
            requestEntity.add("idcard", payRequest.getCreditCardIdentityNo());
            requestEntity.add("bankcard", payRequest.getCreditCardNo());
            requestEntity.add("mobile", payRequest.getCreditCardMobile());
            requestEntity.add("type", "0");
            result = restTemplate.postForObject(url, requestEntity, String.class);
            tempjsonObject = JSONObject.fromObject(result);
            authObject = JSONObject.fromObject(tempjsonObject);
            resultObj = authObject.getJSONObject("result");
            if (!resultObj.getString("resCode").equalsIgnoreCase("1")) { 
            	
            /*	return getErrorMap(payRequest.getVersion(), payRequest.getCharset(),
                    payRequest.getSignType(), "00006", "信用卡验证无效", merchantid, securityKey.getKey()); 
            */
            	
            	return null;
            }

            /** 创建订单 */
            uri = util.getServiceUrl("transactionclear", "error url request!");
            url = uri.toString() + "/v1.0/transactionclear/payment/add";

            /** 根据的用户手机号码查询用户的基本信息 */
            requestEntity = new LinkedMultiValueMap<String, String>();
            requestEntity.add("type", "0");
            requestEntity.add("phone", payRequest.getDebitCardMobile());
            requestEntity.add("amount", payRequest.getTotalFee());
            requestEntity.add("channel_tag", payRequest.getService());
            requestEntity.add("desc", payRequest.getBody());
            requestEntity.add("notify_url", payRequest.getNotifyURL());
            requestEntity.add("return_url", payRequest.getReturnURL());
            requestEntity.add("out_order_code", payRequest.getTradeNo());
            requestEntity.add("brand_id", brandid);
            requestEntity.add("auto_clearing", "1");
            if (payRequest.getExtraParam() != null && !payRequest.getExtraParam().equalsIgnoreCase("")) {
                requestEntity.add("desc_code", payRequest.getExtraParam());
            }

            result = restTemplate.postForObject(url, requestEntity, String.class);
            jsonObject = JSONObject.fromObject(result);
            resultObj = jsonObject.getJSONObject("result");
            String ordercode = resultObj.getString("ordercode");


            /** 获取银行的名称 */
            restTemplate = new RestTemplate();
            uri = util.getServiceUrl("paymentchannel", "error url request!");
            url = uri.toString() + "/v1.0/paymentchannel/bankcard/location";
            requestEntity = new LinkedMultiValueMap<String, String>();
            requestEntity.add("cardid", payRequest.getDebitCardNo());
            result = restTemplate.postForObject(url, requestEntity, String.class);
            jsonObject = JSONObject.fromObject(result);
            resultObj = jsonObject.getJSONObject("result");
            String bankName = resultObj.getString("bankLocation");

            /** 通过brand_id获取brand_date */
            restTemplate = new RestTemplate();
            uri = util.getServiceUrl("user", "error url request!");
            url = uri.toString() + "/v1.0/user/brandrate/id";
            requestEntity = new LinkedMultiValueMap<String, String>();
            requestEntity.add("brand_id", brandid);
            result = restTemplate.postForObject(url, requestEntity, String.class);
            jsonObject = JSONObject.fromObject(result);
            resultObj = jsonObject.getJSONObject("result");
            String rate = resultObj.getString("rate");
            String fee = resultObj.getString("withdrawFee");

            /** 获取银行编号 */
            restTemplate = new RestTemplate();
            uri = util.getServiceUrl("user", "error url request!");
            url = uri.toString() + "/v1.0/user/bank/code/name";
            requestEntity = new LinkedMultiValueMap<String, String>();
            requestEntity.add("bank_name", bankName);
            result = restTemplate.postForObject(url, requestEntity, String.class);
            jsonObject = JSONObject.fromObject(result);
            resultObj = jsonObject.getJSONObject("result");
            String bankCode = resultObj.getString("bankCode");

            /** 贷记卡预留手机号码得黑名单验证 */
            restTemplate = new RestTemplate();
            uri = util.getServiceUrl("risk", "error url request!");
            url = uri.toString() + "/v1.0/risk/blackwhite/query/phone";
            requestEntity = new LinkedMultiValueMap<String, String>();
            requestEntity.add("phone", payRequest.getDebitCardMobile());
            /** 0为登陆操作 */
            requestEntity.add("operation_type", "1");
            result = restTemplate.postForObject(url, requestEntity, String.class);
            jsonObject = JSONObject.fromObject(result);
            String rescode = jsonObject.getString("resp_code");
            if (!rescode.equalsIgnoreCase(CommonConstants.SUCCESS)) { 
            	
            /*	return getErrorMap(payRequest.getVersion(), payRequest.getCharset(),
                    payRequest.getSignType(), "00009", "处于黑名单中", merchantid, securityKey.getKey());
*/
            	return null;
            
            }


            
            if (jsonObject.getString("resp_code").equalsIgnoreCase("000000")) {
            	
            	/**提交到快捷*/
            	Map  map  = new HashMap();
            	map.put("merchantNo", "10000017");  //商户号-测试环境统一商户号    @
            	map.put("amount", payRequest.getTotalFee());
            	map.put("outTradeNo", ordercode);
            	map.put("payType", "3");
            	map.put("bgUrl", payRequest.getNotifyURL());
            	map.put("pageUrl", payRequest.getReturnURL());
            	map.put("goodsName", payRequest.getBody());
            	map.put("goodsDesc", payRequest.getBody());
            	map.put("payBankCardNo", payRequest.getCreditCardNo());
            	map.put("payBankCardAccountName", payRequest.getCreditCardName());
            	
            	
            	restTemplate = new RestTemplate();
                uri = util.getServiceUrl("paymentchannel", "error url request!");
                url = uri.toString() + "/v1.0/paymentchannel/bankcard/location";
                requestEntity = new LinkedMultiValueMap<String, String>();
                requestEntity.add("cardid", payRequest.getCreditCardNo());
                result = restTemplate.postForObject(url, requestEntity, String.class);
                jsonObject = JSONObject.fromObject(result);
                resultObj = jsonObject.getJSONObject("result");
                String creditbankName = resultObj.getString("bankLocation");
            	map.put("payBankCardName", creditbankName);
            	map.put("payBankCardPhone", payRequest.getCreditCardMobile());
            	map.put("payBankCardIdNo", payRequest.getCreditCardIdentityNo());
            	map.put("payBankCardCVN", payRequest.getCreditCardCvn());
            	map.put("payBankCardExpiredDate", payRequest.getCreditCardExpireDate());
            	map.put("bankNo", payRequest.getDebitCardNo());
            	map.put("realName", payRequest.getDebitCardName());
            	map.put("cardNo", payRequest.getDebitCardIdentityNo());
            	map.put("tel", payRequest.getDebitCardMobile());
            	map.put("bankName", bankName);
            	map.put("province", payRequest.getDebitProvince());
            	map.put("city", payRequest.getDebitCity());
            	map.put("address", payRequest.getDebitAddress());
            	map.put("bankBranchId", payRequest.getDebitBankBranchId());
            	map.put("bankBranchName", payRequest.getDebitBranchName());
            	map.put("bankType", "2");
            	map.put("settlePeriod", "1");
            	map.put("settleFeeRate", rate);
            	map.put("agentFee", fee);
            	String signMsg = Md5Util.MapValuetoString(map);
          		signMsg = Md5Util.mapKey2String(map);
        	    String key = "3566t0npsgiu96029x7n8lh5c355g6ka";
        	    String signature = Md5Util.getMD5(signMsg+key);
        	    System.out.println("signature:"+signature);
        	    map.put("sign", signature);  //加密字符串，加密方式见4.1  @
        	    //converData(map);
        	    //String reqMsg = SignUtil.getURLParam(map, false, null);
        		//log.info("kuaijietopupRequest——reqMsg"+reqMsg);
        		String kjurl = "http://dpt.51qmf.cn/ledgerApi.php?m=QuickPay&a=pay";
        		//String msgg = sendMsg(url, map);
        		JSONObject jsonobj = new JSONObject();
        		jsonobj = jsonobj.fromObject(map);
        		//String msg = HttpRequest.sendPost(url, jsonobj.toString());
        		String msg = HttpRequest.sendPostJSON(url, jsonobj);
        		JSONObject json = JSONObject.fromObject(msg);  
        		
        		String codeurl  = json.getString("codeurl");
        		return "redirect:" + codeurl;
            	
            	/*map.put("bankNo", cardNo);		//开户卡号   @
				map.put("bankName", bankName); //开户行名称(请参考总行.xlsx)  @
				map.put("payBankCardIdNo", idcard); //付款信用卡持卡人身份证号  @
				map.put("cardNo", idcardJJ); //开户人身份证号码  @
				map.put("tel", telphone);  //银行预留手机号     @
				map.put("payBankCardPhone", phone);   //付款信用卡预留手机号    @
				map.put("payBankCardAccountName",userName);   //付款信用卡持卡人姓名(请务必保证与realName参数一致)   @
				map.put("realName", userNameJJ);  //开户人姓名   @
				map.put("settlePeriod", 1); //结算周期，1：T0实时结算，支付成功后，立马结算2：T1隔天结算   @
				map.put("settleFeeRate", decimalFormat.format(rat));  //结算给您的客户的费率(0.60表示百分之0.60)（您给您的客户的费率），必须高于商户费率比如 交易金额是 100，   @
											   //settleFeeRate是百分之 0.60（必须高于您的费率）那么 您的客户实际收到的金额是100 * （1 – settleFeeRate / 100）- 商户代付手续费-代付手续费(此手续费由我们设置)
				map.put("merchantNo", merchant_no);  //商户号-测试环境统一商户号    @
				map.put("outTradeNo",ordercode.substring(0, 30));  	//商户号订单号-最小6位最大64位    @
				map.put("payType", 3);    //支付类型：4有积分,3无积分   @
				map.put("payBankCardNo", bankcard);  		//付款信用卡卡号	 @
				Double amt = Double.valueOf(amount);
				DecimalFormat df = new DecimalFormat("0.00");//格式化
				String CNY = df.format(amt);
				map.put("amount", CNY);		//交易金额以元为单位，如：10.00表示为10元）	  @
				map.put("payBankCardName", bankNameX);  //付款信用卡银行名称(请参考总行.xlsx)  @
				map.put("agentFee", extraFee); //商户代付手续费(保留参数，暂不生效)  @
				map.put("payBankCardExpiredDate", expiredTime);  //付款信用卡有效期,格式YYMM  @
				map.put("payBankCardCVN", securityCode);  //付款信用卡安全码  @
				map.put("bankBranchId", lineNoJJ); //联行行号  @
				map.put("address", province+city);  //详细地址  @
				map.put("city", city);  //开户行所在市  @
				map.put("province", province); //开户行所在省份  @
				map.put("bgUrl",ipAddress+"/v1.0/paymentchannel/topup/kj/notify_call");     	//订单状态回调回调地址    ？
				map.put("pageUrl", ipAddress+"/v1.0/paymentchannel/topup/kj/ret_url");  //重定向地址  @
				map.put("bankBranchName", bankBranchName);  //开户支行名称  @
				map.put("goodsName", brandname.trim());  //商品名称,用于显示在支付界面  @  	
				map.put("goodsDesc", desc); //商品描述  @
  			    map.put("bankType", 2);   //卡类型2:对私1:对公		@	
*/            	
            	
            	
            	
            	

                /** 更新订单 */
               /* uri = util.getServiceUrl("transactionclear", "error url request!");
                url = uri.toString() + "/v1.0/transactionclear/payment/update/outchannel";
                requestEntity = new LinkedMultiValueMap<String, String>();
                requestEntity.add("order_code", ordercode);
                requestEntity.add("trano", xsDm);
                requestEntity.add("pan", payRequest.getCreditCardNo());
                requestEntity.add("preSerial", jsonObject.getString("serial"));
                requestEntity.add("name", payRequest.getCreditCardName());
                requestEntity.add("expiredate", payRequest.getCreditCardExpireDate());
                requestEntity.add("cvn", payRequest.getCreditCardCvn());
                requestEntity.add("merchant_id", payRequest.getMerchantId());
                result = restTemplate.postForObject(url, requestEntity, String.class);*/

            } else {

                /*return getErrorMap(payRequest.getVersion(), payRequest.getCharset(), payRequest.getSignType(), "00010", "交易失败", merchantid,
                        securityKey.getKey());
*/
            	return null;
            }

        } catch (Exception e) {

        	   /*return getErrorMap("1.0", "UTF-8", "MD5", "00016", "参数传递有误", merchantid, securityKey.getKey());
        	   */
        	  return null;
        }
    }
	
	
	
	@RequestMapping(method=RequestMethod.POST,value="/v1.0/channel/account/query/balance1")
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
	
	@RequestMapping(method=RequestMethod.POST,value="/v1.0/channel/account/query/balance/history1")
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
	
	
	@RequestMapping(method=RequestMethod.POST,value="/v1.0/channel/order/query/time1")
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
	
	
	@RequestMapping(method=RequestMethod.POST,value="/v1.0/channel/withdraw/query1")
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
	
	
	
	
	@RequestMapping(method=RequestMethod.POST,value="/v1.0/channel/withdraw/req1")
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
	
	
	@RequestMapping(method=RequestMethod.POST,value="/v1.0/channel/topup1")
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
