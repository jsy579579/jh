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
import com.jh.paymentchannel.pojo.YBHKRegister;
import com.jh.paymentchannel.util.Util;

import net.sf.json.JSONObject;

@Service
public class YBHKTopupPage extends BaseChannel implements TopupRequest {

	private static final Logger log = LoggerFactory.getLogger(YBHKTopupPage.class);

	@Autowired
	private TopupPayChannelBusiness topupPayChannelBusiness;

	@Autowired
	private Util util;

	@Autowired
	private YBHKpageRequest ybhkpageRequest;

	@Value("${payment.ipAddress}")
	private String ipAddress;

	private static String key = "Lp7L74br3H14C5k5ba4i7pA58C11doUu483Kv5Q8Ef85D7z205Pa5ii5hE74";

	private static String mainCustomerNumber = "10022572125";
	
	@Override
	public Map<String, String> topupRequest(Map<String,Object> params) throws Exception {
		PaymentOrder paymentOrder = (PaymentOrder) params.get("paymentOrder");
		HttpServletRequest request = (HttpServletRequest) params.get("request");
		String extra = (String) params.get("extra");
		String ordercode = paymentOrder.getOrdercode();
		String amount = paymentOrder.getAmount().toString();
		String rate = paymentOrder.getRate() + "";
		String extraFee = paymentOrder.getExtraFee() + "";
		String orderType = paymentOrder.getType();
		String userId = paymentOrder.getUserid() + "";
		String bankCard = paymentOrder.getBankcard();
		
		if (extraFee.contains("."))
			extraFee = extraFee.substring(0, extraFee.indexOf("."));
		
		Map<String, String> map = new HashMap<String, String>();

		
		Map<String, Object> queryBankCardByCardNoAndUserId = this.queryBankCardByCardNoAndUserId(bankCard, "0", userId);
		Object object2 = queryBankCardByCardNoAndUserId.get("result");
		JSONObject fromObject = JSONObject.fromObject(object2);
		
		String idCard = fromObject.getString("idcard");
		
		YBHKRegister ybhkRegister = topupPayChannelBusiness.getYBHKRegisterByIdCard(idCard);
		
		
		if("10".equals(orderType)) {
			log.info("根据判断进入消费任务======");
			
			if (!rate.equals(ybhkRegister.getRate())) {

				boolean setfee = ybhkpageRequest.setfee(idCard, ybhkRegister.getCustomerNum(), mainCustomerNumber, rate, "8");
				
				if(setfee) {
					
					ybhkRegister.setRate(rate);
					
					topupPayChannelBusiness.createYBHKRegister(ybhkRegister);
					
					if(!extraFee.equals(ybhkRegister.getExtraFee())) {
						
						boolean setfee1 = ybhkpageRequest.setfee(idCard, ybhkRegister.getCustomerNum(), mainCustomerNumber, extraFee, "2");
						boolean setfee2 = ybhkpageRequest.setfee(idCard, ybhkRegister.getCustomerNum(), mainCustomerNumber, extraFee, "3");
						boolean setfee3 = ybhkpageRequest.setfee(idCard, ybhkRegister.getCustomerNum(), mainCustomerNumber, "0", "4");
						boolean setfee4 = ybhkpageRequest.setfee(idCard, ybhkRegister.getCustomerNum(), mainCustomerNumber, "0", "5");
						
						if(setfee1) {
							
							ybhkRegister.setExtraFee(extraFee);
							
							topupPayChannelBusiness.createYBHKRegister(ybhkRegister);
							
							map = (Map<String, String>) ybhkpageRequest.ybhkFastPay(request, ordercode, "5311");
						}
					
					}else {
						
						map = (Map<String, String>) ybhkpageRequest.ybhkFastPay(request, ordercode, "5311");
					}
					
				}

			}else if(!extraFee.equals(ybhkRegister.getExtraFee())) {
				
				boolean setfee1 = ybhkpageRequest.setfee(idCard, ybhkRegister.getCustomerNum(), mainCustomerNumber, extraFee, "2");
				boolean setfee2 = ybhkpageRequest.setfee(idCard, ybhkRegister.getCustomerNum(), mainCustomerNumber, extraFee, "3");
				boolean setfee3 = ybhkpageRequest.setfee(idCard, ybhkRegister.getCustomerNum(), mainCustomerNumber, "0", "4");
				boolean setfee4 = ybhkpageRequest.setfee(idCard, ybhkRegister.getCustomerNum(), mainCustomerNumber, "0", "5");
				
				if(setfee1) {
					
					ybhkRegister.setExtraFee(extraFee);
					
					topupPayChannelBusiness.createYBHKRegister(ybhkRegister);
					
					map = (Map<String, String>) ybhkpageRequest.ybhkFastPay(request, ordercode, "5311");
				}
				
				
			} else {
				
				map = (Map<String, String>) ybhkpageRequest.ybhkFastPay(request, ordercode, "5311");
				
			}
			
		}
		
		
		if("11".equals(orderType)) {
			
			map = (Map<String, String>) ybhkpageRequest.ybhkTransfer(request, ordercode, extra);
			
		}
		

		return map;

	}

}
