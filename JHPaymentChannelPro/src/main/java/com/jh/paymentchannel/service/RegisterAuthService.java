package com.jh.paymentchannel.service;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.jh.paymentchannel.business.RegisterAuthBusiness;
import com.jh.paymentchannel.pojo.RegisterAuth;

import cn.jh.common.utils.CommonConstants;

@Controller
@EnableAutoConfiguration
public class RegisterAuthService {
	
	private static final Logger Log = LoggerFactory.getLogger(RegisterAuthService.class);
	
	@Autowired
	private RegisterAuthBusiness registerAuthBusiness;
	
	
	//保存数据
	@RequestMapping(method=RequestMethod.POST, value="/v1.0/paymentchannel/registerAuth/save")
	public @ResponseBody Object saveAuth(HttpServletRequest request,
			@RequestParam(value="request_id" ) String requestId,
			@RequestParam(value="mobile" ) String mobile,
			@RequestParam(value="id_card" ) String idCard,
			@RequestParam(value="legal_person" ) String legalPerson,
			@RequestParam(value="min_settle_amoun" ) String minSettleAmoun,
			@RequestParam(value="risk_reserve_day" ) String riskReserveDay,
			@RequestParam(value="bank_account_number" ) String bankAccountNumber,
			@RequestParam(value="bank_name" ) String bankName,
			@RequestParam(value="customer_number" ) String customerNumber,
			@RequestParam(value="status") String status,
			@RequestParam(value="charge") String charge,
			@RequestParam(value="rate") String rate
			
			){
		
		RegisterAuth ra = new RegisterAuth();
		ra.setRequestId(requestId);
		ra.setMobile(mobile);
		ra.setIdCard(idCard);
		ra.setLegalPerson(legalPerson);
		ra.setMinSettleAmoun(minSettleAmoun);
		ra.setRiskReserveDay(riskReserveDay);
		ra.setBankAccountNumber(bankAccountNumber);
		ra.setBankName(bankName);
		ra.setCustomerNumber(customerNumber);
		ra.setStatus(status);
		ra.setRate(rate);
		ra.setCharge(charge);
		
		RegisterAuth saveAuth = registerAuthBusiness.saveAuth(ra);
		
		Map map = new HashMap();
		map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
		map.put(CommonConstants.RESP_MESSAGE, "保存成功");
		
		return map;
	}
	
	
	//查询数据
	@RequestMapping(method=RequestMethod.POST, value="/v1.0/paymentchannel/registerAuth/query")
	public @ResponseBody Object queryByMobile(HttpServletRequest request,
			@RequestParam(value="mobile") String mobile
			){
		
		RegisterAuth queryByMobile = registerAuthBusiness.queryByMobile(mobile);
		
		Map map = new HashMap();
		map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
		map.put(CommonConstants.RESULT, queryByMobile);
		map.put(CommonConstants.RESP_MESSAGE, "查询成功");
		
		return map;
	}
	
	//修改数据
	@RequestMapping(method=RequestMethod.POST, value="/v1.0/paymentchannel/registerAuth/update")
	public @ResponseBody Object updateAuth(HttpServletRequest request,
			@RequestParam(value="request_id", defaultValue="", required=false) String requestId,
			@RequestParam(value="mobile" ) String mobile,
			@RequestParam(value="id_card", defaultValue="", required=false) String idCard,
			@RequestParam(value="legal_person", defaultValue="", required=false) String legalPerson,
			@RequestParam(value="min_settle_amoun", defaultValue="", required=false) String minSettleAmoun,
			@RequestParam(value="risk_reserve_day", defaultValue="", required=false) String riskReserveDay,
			@RequestParam(value="bank_account_number", defaultValue="", required=false) String bankAccountNumber,
			@RequestParam(value="bank_name", defaultValue="", required=false) String bankName,
			@RequestParam(value="customer_number", defaultValue="", required=false) String customerNumber,
			@RequestParam(value="charge", defaultValue="", required=false) String charge,
			@RequestParam(value="status", defaultValue="", required=false) String status,
			@RequestParam(value="rate", defaultValue="", required=false) String rate
			
			){
		
		RegisterAuth ra = registerAuthBusiness.queryByMobile(mobile);
		
		ra.setRequestId(requestId!=null&&!requestId.equals("")?requestId:ra.getRequestId());
		ra.setMobile(mobile!=null&&!mobile.equals("")?mobile:ra.getMobile());
		ra.setIdCard(idCard!=null&&!idCard.equals("")?idCard:ra.getIdCard());
		ra.setLegalPerson(legalPerson!=null&&!legalPerson.equals("")?legalPerson:ra.getLegalPerson());
		ra.setMinSettleAmoun(minSettleAmoun!=null&&!minSettleAmoun.equals("")?minSettleAmoun:ra.getMinSettleAmoun());
		ra.setRiskReserveDay(riskReserveDay!=null&&!riskReserveDay.equals("")?riskReserveDay:ra.getRiskReserveDay());
		ra.setBankAccountNumber(bankAccountNumber!=null&&!bankAccountNumber.equals("")?bankAccountNumber:ra.getBankAccountNumber());
		ra.setBankName(bankName!=null&&!bankName.equals("")?bankName:ra.getBankName());
		ra.setCustomerNumber(customerNumber!=null&&!customerNumber.equals("")?customerNumber:ra.getCustomerNumber());
		ra.setStatus(status!=null&&!status.equals("")?status:ra.getStatus());
		ra.setRate(rate!=null&&!rate.equals("")?rate:ra.getRate());
		ra.setCharge(charge!=null&&!charge.equals("")?charge:ra.getCharge());
		
		RegisterAuth updateAuth = registerAuthBusiness.updateAuth(ra);
		
		Map map = new HashMap();
		map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
		map.put(CommonConstants.RESULT, updateAuth);
		map.put(CommonConstants.RESP_MESSAGE, "修改成功");
		
		return map;
	}
}
