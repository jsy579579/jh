package com.jh.paymentgateway.business.impl;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import com.jh.paymentgateway.basechannel.BaseChannel;
import com.jh.paymentgateway.business.TopupPayChannelBusiness;
import com.jh.paymentgateway.business.TopupRequestBusiness;
import com.jh.paymentgateway.controller.HZHKpageRequest;
import com.jh.paymentgateway.pojo.BankNumCode;
import com.jh.paymentgateway.pojo.HZHKRegister;
import com.jh.paymentgateway.pojo.PaymentRequestParameter;

import cn.jh.common.utils.CommonConstants;

@Service
public class HZHKTopupPage extends BaseChannel implements TopupRequestBusiness {
	private static final Logger LOG = LoggerFactory.getLogger(HZHKTopupPage.class);

	@Value("${payment.ipAddress}")
	private String ip;

	@Autowired
	private HZHKpageRequest hzhkpageRequest;
	
	@Autowired
	private TopupPayChannelBusiness topupPayChannelBusiness;

	public Map<String, Object> topupRequest(Map<String, Object> params) throws Exception {
		
		LOG.info("进入HZHKpageRequest======");

		PaymentRequestParameter bean = (PaymentRequestParameter) params.get("paymentRequestParameter");
		String orderCode = bean.getOrderCode();
		String orderType = bean.getOrderType();
		String extra = bean.getExtra();
		String rate = bean.getRate();
		String rip = bean.getIpAddress();
		String amount = bean.getAmount();
		String idCard = bean.getIdCard();
		String bankCard = bean.getBankCard();
		String expiredTime = bean.getExpiredTime();
		String securityCode = bean.getSecurityCode();
		String bankName = bean.getCreditCardBankName();
		Map<String,Object> map = new HashMap<String, Object>();
		
		HZHKRegister hzhkRegister = topupPayChannelBusiness.getHZHKRegisterByidCard(idCard);
		BankNumCode bankNumCode = topupPayChannelBusiness.getBankNumCodeByBankName(bankName);
		String bankCode = bankNumCode.getBankBranchcode();
		
		if (bankName.contains("交通银行")) {
	        	if (new BigDecimal(amount).compareTo(new BigDecimal("10000")) > 0) {
			map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			map.put(CommonConstants.RESP_MESSAGE, "交通银行卡交易金额限制为10000以内,请核对重新输入金额!");
			this.addOrderCauseOfFailure(orderCode, "交通银行卡交易金额限制为10000以内,请核对重新输入金额!", rip);
			return map;

		 }
		} else if (bankName.contains("光大")) {
				if (new BigDecimal(amount).compareTo(new BigDecimal("5000")) > 0) {
					map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
					map.put(CommonConstants.RESP_MESSAGE, "光大银行卡交易金额限制为5000以内,请核对重新输入金额!");
					this.addOrderCauseOfFailure(orderCode, "光大银行卡交易金额限制为5000以内,请核对重新输入金额!", rip);
					return map;

				}
		}else if (bankName.contains("民生")) {
			if (new BigDecimal(amount).compareTo(new BigDecimal("20000")) > 0) {
				map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
				map.put(CommonConstants.RESP_MESSAGE, "民生银行卡交易金额限制为20000以内,请核对重新输入金额!");
				this.addOrderCauseOfFailure(orderCode, "民生银行卡交易金额限制为20000以内,请核对重新输入金额!", rip);
				return map;
			}
		}
		
		//消费计划|福建省-泉州市-350500
		String areaCode=null;
		//String province = extra.substring(extra.indexOf("|") + 1, extra.indexOf("-")); //福建省
		//String city = extra.substring(extra.indexOf("-") + 1);        //泉州市-350500
		String cityCode = extra.substring(extra.lastIndexOf("-")+1);    //350500
		
		if(extra.contains("|") && cityCode!=null && cityCode.length()==6) { 
			areaCode=cityCode.substring(0,4);
			LOG.info("市编码为：--------"+areaCode);
		}else {
			areaCode="3101";  //否则默认为上海市
			LOG.info("默认市编码为：--------"+areaCode);
		}
		
		
		if ("10".equals(orderType)) {
			LOG.info("根据判断进入消费任务=========");
			
		   if(!rate.equals(hzhkRegister.getRate())) {//费率不同，修改费率
				LOG.info("修改费率--------");
				map = (Map<String, Object>)hzhkpageRequest.HZHKupdateRate(idCard, bankCard); //修改费率
				
				if("000000".equals(map.get("resp_code"))) {
					if(!expiredTime.equals(hzhkRegister.getExpiredTime()) || !securityCode.equals(hzhkRegister.getSecurityCode()) || 
					   !bankName.equals(hzhkRegister.getBankName()) || !bankCode.equals(hzhkRegister.getBankCode())) {
						LOG.info("修改商户信息--------");
						map = (Map<String, Object>)hzhkpageRequest.HZHKupdateMerInfo(idCard, bankName, expiredTime, bankCode, securityCode);//修改商户信息
					
					}else{
						LOG.info("直接进入交易--------");
						map =(Map<String, Object>)hzhkpageRequest.HZHKpay(orderCode,areaCode);//直接去交易
					}
				}else {
					  map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
					  map.put(CommonConstants.RESP_MESSAGE, "修改费率失败");
					  return map;
				   }
		       }else {//费率相同，直接去修改商户信息
				   if(!expiredTime.equals(hzhkRegister.getExpiredTime()) || !securityCode.equals(hzhkRegister.getSecurityCode()) || 
				   !bankName.equals(hzhkRegister.getBankName()) || !bankCode.equals(hzhkRegister.getBankCode())) {
					LOG.info("修改商户信息--------");
					map = (Map<String, Object>)hzhkpageRequest.HZHKupdateMerInfo(idCard, bankName, expiredTime, bankCode, securityCode);//修改商户信息
			   }else {
				    LOG.info("直接进入交易--------");
				    map =(Map<String, Object>)hzhkpageRequest.HZHKpay(orderCode,areaCode);
			}
		}
	}
		if ("11".equals(orderType)) {
			LOG.info("根据判断进入还款任务======");
			map = (Map<String, Object>) hzhkpageRequest.HZHKrepayment(orderCode);
		}
		return map;
	}
}   
