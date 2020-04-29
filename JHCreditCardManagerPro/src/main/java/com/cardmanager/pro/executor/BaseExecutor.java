package com.cardmanager.pro.executor;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.RestTemplate;

import com.cardmanager.pro.async.AsyncMethod;
import com.cardmanager.pro.authorization.CreditCardManagerAuthorizationHandle;
import com.cardmanager.pro.business.ConsumeTaskPOJOBusiness;
import com.cardmanager.pro.business.CreditCardAccountBusiness;
import com.cardmanager.pro.business.CreditCardAccountHistoryBusiness;
import com.cardmanager.pro.business.CreditCardManagerConfigBusiness;
import com.cardmanager.pro.business.DeductionChargeBusiness;
import com.cardmanager.pro.business.RepaymentBillBusiness;
import com.cardmanager.pro.business.RepaymentTaskPOJOBusiness;
import com.cardmanager.pro.channel.ChannelBaseAPI;
import com.cardmanager.pro.channel.ChannelFactory;
import com.cardmanager.pro.channel.behavior.DefaultConsumeTaskCheckor;
import com.cardmanager.pro.config.PropertiesConfig;
import com.cardmanager.pro.pojo.ConsumeTaskPOJO;
import com.cardmanager.pro.pojo.CreditCardAccount;
import com.cardmanager.pro.pojo.CreditCardManagerConfig;
import com.cardmanager.pro.pojo.RepaymentTaskPOJO;
import com.cardmanager.pro.util.CardConstss;
import com.cardmanager.pro.util.RestTemplateUtil;
import com.cardmanager.pro.util.SpringContextUtil;

import cn.jh.common.tools.ResultWrap;
import cn.jh.common.utils.CommonConstants;
import net.sf.json.JSONObject;
@Component
public class BaseExecutor {

	private static final Logger LOG = LoggerFactory.getLogger(BaseExecutor.class);
	
	@Autowired
	protected RestTemplateUtil util;
	
	@Autowired
	protected RestTemplate restTemplate;
	
	@Autowired
	protected PropertiesConfig propertiesConfig;
	
	@Autowired
	private AsyncMethod asyncMethod;
	
	@Autowired
	protected ConsumeTaskPOJOBusiness consumeTaskPOJOBusiness;
	
	@Autowired
	protected RepaymentTaskPOJOBusiness repaymentTaskPOJOBusiness;
	
	@Autowired
	protected CreditCardAccountBusiness creditCardAccountBusiness;
	
	@Autowired
	protected CreditCardManagerConfigBusiness creditCardManagerConfigBusiness;
	
	@Autowired
	protected RepaymentBillBusiness repaymentBillBusiness;
	
	@Autowired
	protected CreditCardAccountHistoryBusiness creditCardAccountHistoryBusiness;
	
	@Autowired
	protected CreditCardManagerAuthorizationHandle creditCardManagerAuthorizationHandle;
	
	@Autowired
	protected DeductionChargeBusiness deductionChargeBusiness;
	
	@Autowired
	protected ChannelFactory channelFactory;
	
	public JSONObject addCreditCardOrder(String userId, String rate, String type, String amount, String realAmount,String creditCardNumber, String channelTag, String orderCode, String serviceCharge, String description,String remark) throws RuntimeException{
//		RestTemplate restTemplate = new RestTemplate();
//		URI uri = util.getServiceUrl("transactionclear", "error url request");
		String url = "http://transactionclear/v1.0/transactionclear/payment/add/credit/card/manager/order";
		LinkedMultiValueMap<String, String> requestEntity = new LinkedMultiValueMap<String, String>();
		requestEntity.add("userId", userId);
		requestEntity.add("rate",rate );
		requestEntity.add("type", type);
		requestEntity.add("amount",amount );
		requestEntity.add("realAmount",realAmount );
		requestEntity.add("channelTag",channelTag );
		requestEntity.add("orderCode",orderCode );
		requestEntity.add("serviceCharge", serviceCharge);
		requestEntity.add("description", description);
		requestEntity.add("remark", remark);
		requestEntity.add("creditCardNumber", creditCardNumber);
		JSONObject resultJSONObject;
		System.out.println(requestEntity);
		try {
			String resultString = restTemplate.postForObject(url, requestEntity, String.class);
			resultJSONObject = JSONObject.fromObject(resultString);
		} catch (Exception e) {
			e.printStackTrace();LOG.error("",e);
			throw new RuntimeException(e);
		}
		return resultJSONObject;
	}
	
	
	public JSONObject paymentTopupRequest(
			String amount,String ordercode,String orderdesc,String userid,String brandcode,
			String channel_type,String channel_tag,String extra
			) throws RuntimeException{
//		RestTemplate restTemplate = new RestTemplate();
//		URI uri = util.getServiceUrl("paymentchannel", "error url request");
		String url = "http://paymentchannel/v1.0/paymentchannel/topup/request";
		LinkedMultiValueMap<String, String> requestEntity = new LinkedMultiValueMap<String, String>();
		requestEntity.add("userid", userid);
		requestEntity.add("brandcode",brandcode );
		requestEntity.add("channel_type", channel_type);
		requestEntity.add("amount",amount );
		requestEntity.add("channel_tag",channel_tag );
		requestEntity.add("ordercode",ordercode );
		requestEntity.add("orderdesc", orderdesc);
		requestEntity.add("extra", extra);
		JSONObject resultJSONObject;
		try {
			String resultString = restTemplate.postForObject(url, requestEntity, String.class);
			resultJSONObject = JSONObject.fromObject(resultString);
		} catch (Exception e) {
			e.printStackTrace();LOG.error("",e);
			throw new RuntimeException(e);
		}
		return resultJSONObject;
	}
	
	public JSONObject getUserInfo(String userId) throws RuntimeException{
//		RestTemplate restTemplate = new RestTemplate();
//		URI uri = util.getServiceUrl("user", "error url request");
		String url = "http://user/v1.0/user/find/by/userid";
		LinkedMultiValueMap<String, String> requestEntity = new LinkedMultiValueMap<String, String>();
		requestEntity.add("userId", userId);
		JSONObject resultJSONObject;
		try {
			resultJSONObject = restTemplate.postForObject(url, requestEntity, JSONObject.class);
			LOG.info(url+"====="+resultJSONObject);
		} catch (Exception e) {
			e.printStackTrace();LOG.error("",e);
			throw new RuntimeException(e);
		}
		return resultJSONObject;
		
	}
	
	public JSONObject getOrderStatusByVersion(String orderCode,String orderType,String version)throws RuntimeException{
		JSONObject resultJSONObject = null;
		LinkedMultiValueMap<String, String> requestEntity = new LinkedMultiValueMap<String, String>();
		requestEntity.add("orderCode", orderCode);
		
		if(CardConstss.CARD_VERSION_11.equals(version)||CardConstss.CARD_VERSION_56.equals(version)){
			String bankCard = "";
			if (CommonConstants.ORDER_TYPE_CONSUME.equals(orderType)) {
				ConsumeTaskPOJO consumeTaskPOJO = consumeTaskPOJOBusiness.findByOrderCode(orderCode);
				bankCard = consumeTaskPOJO.getCreditCardNumber();
			}else if (CommonConstants.ORDER_TYPE_REPAYMENT.equals(orderType)) {
				RepaymentTaskPOJO repaymentTaskPOJO = repaymentTaskPOJOBusiness.findByOrderCode(orderCode);
				bankCard = repaymentTaskPOJO.getCreditCardNumber();
			}
			requestEntity.add("bankCard", bankCard);
		}
		try {
			ChannelBaseAPI channelBaseAPI = channelFactory.getChannelBaseAPI(version);
			resultJSONObject = channelBaseAPI.getOrderStatus(requestEntity, orderType);
		} catch (Exception e) {
			LOG.error("查询订单异常==========",e);
			throw new RuntimeException(e);
		}
		return resultJSONObject;
	}
	
	
	public void updatePaymentOrderByOrderCode(String orderCode){
		asyncMethod.updatePaymentOrderByOrderCode(orderCode);
	}
	
	public Map<String,Object> getUserChannelRate(String userId,String brandId,String version){
		Map<String,Object> map = new HashMap<>();
		String channelId = null;
		CreditCardManagerConfig creditCardManagerConfigs = creditCardManagerConfigBusiness.findByVersion(version);
//		LOG.info(creditCardManagerConfigs.toString());
		if(creditCardManagerConfigs != null){
			channelId = creditCardManagerConfigs.getChannelId();
		}else{
			return ResultWrap.init(CommonConstants.FALIED, "无用户费率!");
		}
		
		LinkedMultiValueMap<String,String> requestEntity = new LinkedMultiValueMap<String,String>();
		requestEntity.add("user_id",userId);
		requestEntity.add("brand_id",brandId);
		requestEntity.add("channel_id", channelId);
		Map<String, Object> restTemplateDoPost = util.restTemplateDoPost("user", "/v1.0/user/channel/rate/query/userid", requestEntity);
//		Map<String, Object> restTemplateDoPost = util.restTemplateDoPost("user", "/v1.0/user/brandrate/query", requestEntity);
		if(!CommonConstants.SUCCESS.equals(restTemplateDoPost.get(CommonConstants.RESP_CODE))){
			return restTemplateDoPost;
		}
		JSONObject resultJSONObject = (JSONObject) restTemplateDoPost.get(CommonConstants.RESULT);
		map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
		map.put(CommonConstants.RESP_MESSAGE, "查询成功");
		map.put(CommonConstants.RESULT, resultJSONObject);
		return map;
	}
	
	public JSONObject getUserBankInfo(String userId,String creditCardNumber) {
		String url = "http://user/v1.0/user/bank/find/bankphone";
		LinkedMultiValueMap<String,String> requestEntity = new LinkedMultiValueMap<String,String>();
		requestEntity.add("userId", userId);
		requestEntity.add("cardNo",creditCardNumber);
		JSONObject resultJSON = restTemplate.postForObject(url, requestEntity, JSONObject.class);
		LOG.info(url + "======" + resultJSON);
		return resultJSON;
	}
	
	public Map<String,Object> updateTaskStatusByOrderCode(String orderCode,String version){
		LOG.info("回调订单号================================:" + orderCode);
		Map<String,Object> map = new HashMap<>();
		Map<String, Object> verifyStringFiledIsNullMap = creditCardManagerAuthorizationHandle.verifyStringFiledIsNull(orderCode);
		if(!CommonConstants.SUCCESS.equals(verifyStringFiledIsNullMap.get(CommonConstants.RESP_CODE))){
			return verifyStringFiledIsNullMap;
		}
		LinkedMultiValueMap<String,String> requestEntity = new LinkedMultiValueMap<String,String>();
		requestEntity.add("order_code", orderCode);
		Map<String, Object> restTemplateDoPost = util.restTemplateDoPost("transactionclear", "/v1.0/transactionclear/payment/query/ordercode", requestEntity);
		if(!CommonConstants.SUCCESS.equals(restTemplateDoPost.get(CommonConstants.RESP_CODE))){
			return restTemplateDoPost;
		}
		
		JSONObject resultJSON = (JSONObject) restTemplateDoPost.get(CommonConstants.RESULT);
		String creditCardNumber = resultJSON.getString("bankcard");
		String realAmountStr = resultJSON.getString("amount");
		String phone = resultJSON.getString("phone");
		String orderType = resultJSON.getString("type");
		String brandId = resultJSON.getString("brandid");
		
		
		RepaymentTaskPOJO repaymentTaskPOJO2 = null;
		ConsumeTaskPOJO consumeTaskPOJO2 = null;
		if(CommonConstants.ORDER_TYPE_REPAYMENT.equals(orderType)){
			repaymentTaskPOJO2 = repaymentTaskPOJOBusiness.findByOrderCode(orderCode);
		}
		if (CommonConstants.ORDER_TYPE_CONSUME.equals(orderType)) {
			consumeTaskPOJO2 = consumeTaskPOJOBusiness.findByConsumeTaskId(orderCode);
		}
		
		
		RepaymentTaskPOJO repaymentTaskPOJO0 = null;
		ConsumeTaskPOJO consumeTaskPOJO0 = null;
		if(repaymentTaskPOJO2 == null && CommonConstants.ORDER_TYPE_REPAYMENT.equals(orderType)){
			repaymentTaskPOJO0 = repaymentTaskPOJOBusiness.findByCreditCardNumberAndTaskTypeAndVersion(creditCardNumber,0,version);
		}
		if(consumeTaskPOJO2 == null && CommonConstants.ORDER_TYPE_CONSUME.equals(orderType)){
			consumeTaskPOJO0 = consumeTaskPOJOBusiness.findByCreditCardNumberAndTaskTypeAndVersion(creditCardNumber,0,version);
		}
		
		RepaymentTaskPOJO repaymentTaskPOJO = null;
		ConsumeTaskPOJO consumeTaskPOJO = null;
		if(repaymentTaskPOJO == null && repaymentTaskPOJO0!=null && repaymentTaskPOJO0.getOrderStatus().intValue()!=1){
			repaymentTaskPOJO = repaymentTaskPOJO0;
		}else if(repaymentTaskPOJO2 !=null){
			repaymentTaskPOJO = repaymentTaskPOJO2;
		}
		
		if(consumeTaskPOJO == null && consumeTaskPOJO0!=null && consumeTaskPOJO0.getOrderStatus().intValue() !=1){
			consumeTaskPOJO = consumeTaskPOJO0;
		}else if(consumeTaskPOJO2!=null){
			consumeTaskPOJO = consumeTaskPOJO2;
		}
		
		if(repaymentTaskPOJO!=null && repaymentTaskPOJO.getOrderStatus().intValue()!=1){
			version = repaymentTaskPOJO.getVersion();
			BigDecimal updateAndDelAmount = BigDecimal.ZERO;
			if (CardConstss.CARD_VERSION_6.equals(version)) {
				updateAndDelAmount = deductionChargeBusiness.updateAndDel(repaymentTaskPOJO);
			}
			
			BigDecimal realAmount = repaymentTaskPOJO.getAmount();
			if(repaymentTaskPOJO.getRealAmount().compareTo(repaymentTaskPOJO.getAmount())!= 0&&repaymentTaskPOJO.getTaskType().intValue() != 1 && repaymentTaskPOJO.getTaskType() != 0){
				realAmount = repaymentTaskPOJO.getRealAmount();
				if(BigDecimal.ZERO.compareTo(realAmount) >= 0){
					realAmount = new BigDecimal(realAmountStr);
					realAmount = realAmount.subtract(updateAndDelAmount);
					creditCardAccountBusiness.updateCreditCardAccountAndVersion(repaymentTaskPOJO.getUserId(),repaymentTaskPOJO.getCreditCardNumber(), repaymentTaskPOJO.getRepaymentTaskId(),1,realAmount, "增加冻结余额",version,repaymentTaskPOJO.getCreateTime());
				}
			}
			realAmount = realAmount.subtract(updateAndDelAmount);
			creditCardAccountBusiness.updateCreditCardAccountAndVersion(repaymentTaskPOJO.getUserId(),repaymentTaskPOJO.getCreditCardNumber(), repaymentTaskPOJO.getRepaymentTaskId(),4,realAmount, "还款成功减少冻结余额",version,repaymentTaskPOJO.getCreateTime());
			repaymentTaskPOJO.setTaskStatus(1);
			repaymentTaskPOJO.setOrderStatus(1);
			repaymentTaskPOJO.setOrderCode(orderCode);
			repaymentTaskPOJO.setReturnMessage("还款成功");
			repaymentTaskPOJO.setRealAmount(new BigDecimal(realAmountStr));
			if (CardConstss.CARD_VERSION_10.equals(version) || CardConstss.CARD_VERSION_11.equals(version)) {
				List<ConsumeTaskPOJO> consumeTasks = consumeTaskPOJOBusiness.findByRepaymentTaskId(repaymentTaskPOJO.getRepaymentTaskId());
				realAmount = BigDecimal.ZERO;
				System.out.println("修改version10还款任务金额"+consumeTasks );
				for (ConsumeTaskPOJO consumeTaskPOJO3 : consumeTasks) {
					if (consumeTaskPOJO3.getOrderStatus().intValue() == 1) {
						realAmount = realAmount.add(consumeTaskPOJO3.getAmount());
					}else {
						SpringContextUtil.getBeanOfClass(DefaultConsumeTaskCheckor.class).checkConsumeTask(consumeTaskPOJO3);
						consumeTaskPOJO3 = consumeTaskPOJOBusiness.findByConsumeTaskId(consumeTaskPOJO3.getConsumeTaskId());
						if (consumeTaskPOJO3.getOrderStatus().intValue() == 1) {
							realAmount = realAmount.add(consumeTaskPOJO3.getAmount());
						}
					}
				}
				
				if (BigDecimal.ZERO.compareTo(realAmount) < 0) {
					repaymentTaskPOJO.setRealAmount(realAmount);
					try {
						this.updateOrderAmountByOrderCode(orderCode, realAmount.toString());
					} catch (Exception e) {
						e.printStackTrace();
					}
				} 
			}
			repaymentTaskPOJO = repaymentTaskPOJOBusiness.save(repaymentTaskPOJO);
			LOG.info("回调修改还款任务成功===================" + repaymentTaskPOJO);
			
		}
		if(consumeTaskPOJO!=null && consumeTaskPOJO.getOrderStatus().intValue() !=1){
			version = consumeTaskPOJO.getVersion();
			if(consumeTaskPOJO.getTaskType().intValue() == 0) {
				CreditCardAccount creditCardAccount2 = creditCardAccountBusiness.findByUserIdAndCreditCardNumberAndVersion(consumeTaskPOJO.getUserId(), creditCardNumber, version);
				if (creditCardAccount2 == null) {
					int billDate = 0;
					int repaymentDate = 0;
					String creditBlance = "0";
					try {
						JSONObject userBankInfo = this.getUserBankInfo(consumeTaskPOJO.getUserId(), creditCardNumber);
						userBankInfo = userBankInfo.getJSONObject(CommonConstants.RESULT);
						billDate = userBankInfo.getInt("billDay");
						repaymentDate = userBankInfo.getInt("repaymentDay");
						creditBlance = userBankInfo.getString("creditBlance");
					} catch (Exception e) {
						e.printStackTrace();
					}
					
					creditCardAccountBusiness.createNewAccount(consumeTaskPOJO.getUserId(),creditCardNumber,version,phone,Integer.valueOf(billDate),Integer.valueOf(repaymentDate),new BigDecimal(creditBlance),brandId);
				}
				creditCardAccountBusiness.updateCreditCardAccountAndVersion(consumeTaskPOJO.getUserId(), creditCardNumber, consumeTaskPOJO.getConsumeTaskId(),0, consumeTaskPOJO.getAmount(),"首笔消费任务",version,consumeTaskPOJO.getCreateTime());
			}else {
				creditCardAccountBusiness.updateCreditCardAccountAndVersion(consumeTaskPOJO.getUserId(),consumeTaskPOJO.getCreditCardNumber(), consumeTaskPOJO.getConsumeTaskId(), 0,consumeTaskPOJO.getAmount(), "消费成功",version,consumeTaskPOJO.getCreateTime());
			}
			LOG.info("回调修改消费任务成功===================" + consumeTaskPOJO);
			consumeTaskPOJO.setReturnMessage("消费成功");
			consumeTaskPOJO.setTaskStatus(1);
			consumeTaskPOJO.setOrderStatus(1);
			consumeTaskPOJO.setOrderCode(orderCode);
			consumeTaskPOJO = consumeTaskPOJOBusiness.save(consumeTaskPOJO);
		}
		
		map.put("repaymentTaskPOJO",repaymentTaskPOJO );
		map.put("consumeTaskPOJO",consumeTaskPOJO );
		map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
		map.put(CommonConstants.RESP_MESSAGE, "修改任务状态成功");
		return map;
	}
	
	private void updateOrderAmountByOrderCode(String orderCode,String amount) {
		String url = "http://transactionclear/v1.0/transactionclear/update/repayment/order/amount";
		LinkedMultiValueMap<String,String> requestEntity = new LinkedMultiValueMap<String,String>();
		requestEntity.add("orderCode", orderCode);
		requestEntity.add("amount",amount);
		String resultString = restTemplate.postForObject(url, requestEntity, String.class);
		LOG.info(url + "======" + resultString);

	}

}
