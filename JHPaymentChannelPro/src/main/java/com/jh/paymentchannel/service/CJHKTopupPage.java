package com.jh.paymentchannel.service;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.jh.paymentchannel.basechannel.BaseChannel;
import com.jh.paymentchannel.business.BranchbankBussiness;
import com.jh.paymentchannel.business.TopupPayChannelBusiness;
import com.jh.paymentchannel.pojo.CJHKRegister;
import com.jh.paymentchannel.pojo.PaymentOrder;
import com.jh.paymentchannel.util.Util;

import net.sf.json.JSONObject;

@Service
public class CJHKTopupPage extends BaseChannel implements TopupRequest {

	private static final Logger log = LoggerFactory.getLogger(CJHKTopupPage.class);

	@Autowired
	private TopupPayChannelBusiness topupPayChannelBusiness;

	@Autowired
	private BranchbankBussiness branchbankBussiness;

	@Autowired
	private Util util;

	@Autowired
	private CJHKpageRequest cjhkpageRequest;

	@Value("${payment.ipAddress}")
	private String ipAddress;

	@Override
	public Map<String, String> topupRequest(Map<String,Object> params) throws Exception {
		PaymentOrder paymentOrder = (PaymentOrder) params.get("paymentOrder");
		HttpServletRequest request = (HttpServletRequest) params.get("request");
		String extra = (String) params.get("extra");
		String ordercode = paymentOrder.getOrdercode();
		String amount = paymentOrder.getAmount().toString();
		
		
		Map<String, String> map = new HashMap<String, String>();

		Map<String, Object> queryOrdercode = this.queryOrdercode(ordercode);
		Object object = queryOrdercode.get("result");
		JSONObject fromObject = JSONObject.fromObject(object);
		JSONObject resultObj = fromObject.getJSONObject("result");

		String bankCard = resultObj.getString("bankcard");
		String rate = resultObj.getString("rate");
		String extraFee = resultObj.getString("extraFee");
		String realAmount = resultObj.getString("realAmount");
		String orderType = resultObj.getString("type");

		
		Map<String, Object> queryBankCardByCardNo = this.queryBankCardByCardNo(bankCard, "0");
		Object object2 = queryBankCardByCardNo.get("result");
		fromObject = JSONObject.fromObject(object2);
		
		String idCard = fromObject.getString("idcard");
		
		
		CJHKRegister cjhkRegister = topupPayChannelBusiness.getCJHKRegisterByIdCard(idCard);
		
		
		if("10".equals(orderType)) {
			log.info("根据判断进入消费任务======");
			
			if (!rate.equals(cjhkRegister.getRate())) {

				map = (Map<String, String>) cjhkpageRequest.cjhkUpdateMerchant(request, ordercode);
				Object respCode = map.get("resp_code");
				Object respMessage = map.get("resp_message");
				log.info("respCode====="+respCode);
				
				if("000000".equals(respCode)) {
					
					if(!extraFee.equals(cjhkRegister.getExtraFee())) {
						
						map = (Map<String, String>) cjhkpageRequest.cjhkUpdateWithDrawFee(request, ordercode);
						respCode = map.get("resp_code");
						respMessage = map.get("resp_message");
						log.info("respCode====="+respCode);
						
						if("000000".equals(respCode)) {
							
							map = (Map<String, String>) cjhkpageRequest.cjhkFastPay(request, ordercode, "充值缴费", "充值缴费");
						}
					
					}else {
						
						map = (Map<String, String>) cjhkpageRequest.cjhkFastPay(request, ordercode, "充值缴费", "充值缴费");
					}
					
				}

			}else if(!extraFee.equals(cjhkRegister.getExtraFee())) {
				
				map = (Map<String, String>) cjhkpageRequest.cjhkUpdateWithDrawFee(request, ordercode);
				Object respCode = map.get("resp_code");
				Object respMessage = map.get("resp_message");
				log.info("respCode====="+respCode);
				
				if("000000".equals(respCode)) {
					
					map = (Map<String, String>) cjhkpageRequest.cjhkFastPay(request, ordercode, "充值缴费", "充值缴费");
				}
				
			} else {
				
				map = (Map<String, String>) cjhkpageRequest.cjhkFastPay(request, ordercode, "充值缴费", "充值缴费");
				
			}
			
		}
		
		
		if("11".equals(orderType)) {
			
			map = (Map<String, String>) cjhkpageRequest.cjhkTransfer(request, ordercode, extra);
			
		}
		

		return map;

	}

}
