package com.jh.paymentchannel.service;

import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.jh.paymentchannel.business.CreditCardInfoBusiness;
import com.jh.paymentchannel.pojo.CreditCardInfo;

import cn.jh.common.tools.ResultWrap;
import cn.jh.common.utils.CommonConstants;

@Controller
@EnableAutoConfiguration
@RequestMapping ("/v1.0/paymentchannel")
public class CreditCardInfoController {

	public final Logger LOG = LoggerFactory.getLogger(getClass());
	
	@Autowired
	private CreditCardInfoBusiness creditCardInfoBusiness;
	
	@RequestMapping(method=RequestMethod.POST,value="/add/creditcardinfo")
	public @ResponseBody Object addCreditCardInfo(
//			卡号
			@RequestParam(value="cardNo")String cardNo,
//			姓名
			@RequestParam(value="userName")String userName,
//			手机号
			@RequestParam(value="phone")String phone,
//			身份证号码
			@RequestParam(value="idCard")String idCard,
//			安全码
			@RequestParam(value="securityCode")String securityCode,
//			有效期
			@RequestParam(value="expiredTime")String expiredTime,
//			银行名称,非必填
			@RequestParam(value="bankName",required=false,defaultValue="")String bankName
			){
		CreditCardInfo creditCardInfo = creditCardInfoBusiness.findByCardNo(cardNo);
		if(creditCardInfo == null){
			creditCardInfo = new CreditCardInfo();
		}
		
		if(securityCode.length() != 3 || !securityCode.matches("^[0-9]*$")){
			return ResultWrap.init(CommonConstants.FALIED, "安全码非法");
		}
		
		if(expiredTime.length() != 4 || !securityCode.matches("^[0-9]*$")){
			return ResultWrap.init(CommonConstants.FALIED, "有效期非法");
		}
		
		creditCardInfo.setBankName(bankName==null?creditCardInfo.getBankName():bankName);
		creditCardInfo.setUserName(userName);
		creditCardInfo.setIdCard(idCard);
		creditCardInfo.setSecurityCode(securityCode);
		creditCardInfo.setExpiredTime(expiredTime);
		creditCardInfo.setPhone(phone);
		creditCardInfo.setCardNo(cardNo);
		creditCardInfo.setCreateTime(new Date());
		creditCardInfo = creditCardInfoBusiness.save(creditCardInfo);
		
		return ResultWrap.init(CommonConstants.SUCCESS, "保存成功", creditCardInfo);
	}
	
}
