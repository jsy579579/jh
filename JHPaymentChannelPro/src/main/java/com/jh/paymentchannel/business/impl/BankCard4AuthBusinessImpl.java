package com.jh.paymentchannel.business.impl;

import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;

import com.jh.paymentchannel.service.AliCloudBank4Auth;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jh.paymentchannel.business.BankCard4AuthBusiness;
import com.jh.paymentchannel.config.PropertiesConfig;
import com.jh.paymentchannel.pojo.BankCard4Auth;
import com.jh.paymentchannel.pojo.BankCard4AuthRoute;
import com.jh.paymentchannel.pojo.BankCardLocation;
import com.jh.paymentchannel.repository.BankCard4AuthRepository;
import com.jh.paymentchannel.repository.BankCard4AuthRouteRepository;
import com.jh.paymentchannel.repository.BankCardLocationRepository;
import com.jh.paymentchannel.util.PaymentChannelConstants;

import net.sf.json.JSONObject;

@Service
public class BankCard4AuthBusinessImpl implements BankCard4AuthBusiness{
	
	private static final Logger log = LoggerFactory.getLogger(BankCard4AuthBusinessImpl.class);
	
	@Autowired
    private PropertiesConfig propertiesConfig;
	
	@Autowired
    private BankCard4AuthRepository bankCard4repository;
	
	@Autowired
    private BankCard4AuthRouteRepository bankCard4Routerepository;
	
	@Autowired
    private BankCardLocationRepository bankCardLocationrepository;
	
	@Autowired
	private AliCloudBank4Auth aliCloudBank4Auth;

	@Autowired
	private EntityManager em;
	
	
	@Override
	public List<BankCard4Auth> findAllBankCard4s(Pageable pageable) {
		Page<BankCard4Auth> page =  bankCard4repository.findAll(pageable);
		return page.getContent();
	}

	@Override
	public List<BankCard4Auth> findAllSuccessBankCard4s(Pageable pageable) {
		return bankCard4repository.findBankCard4AuthSuccess(pageable);
	}

	@Override
	public BankCard4Auth findBankCard4AuthByCard(String bankcard) {
		return bankCard4repository.findBankCard4AuthByCard(bankcard);
	}

	@Override
	public BankCard4Auth findBankCard4AuthByMobile(String mobile) {
		return bankCard4repository.findBankCard4AuthByMobile(mobile);
	}

	@Override
	@Transactional
	public Object bankCard4AuthBackstage(String mobile, String bankCard, String idcard,
								String realname) {
		mobile = mobile.trim();
		bankCard = bankCard.trim();
		idcard = idcard.trim();
		realname = realname.trim();
		BankCard4AuthRoute bankCard4AuthRoute = bankCard4Routerepository.getCurActiveBankCard4Channel();

		String authChannel = bankCard4AuthRoute.getCurChannel();
		String result = null;


		BankCard4Auth  bankCard4Auth = bankCard4repository.findBankCard4AuthByCard(bankCard.trim());

//		if(bankCard4Auth==null) {
//			 bankCard4Auth = bankCard4repository.findBankCard4AuthByCard(bankCard.trim());
//		}
//		log.info(PaymentChannelConstants.BANKCARD4_CHANNEL_1+"返回："+result);
		try {
			if(bankCard4Auth == null || (bankCard4Auth != null && bankCard4Auth.getResCode() != 1)){
//				if(authChannel.equalsIgnoreCase(PaymentChannelConstants.BANKCARD4_CHANNEL_1)){
//					result = JuHeAPIBankCard4AuthService.bankCard4AuthJuhe(mobile, bankCard, idcard, realname,propertiesConfig.getAuth4Url(),propertiesConfig.getAuth4Key());
//					log.info(PaymentChannelConstants.BANKCARD4_CHANNEL_1+"返回："+result);
//				}
					result = aliCloudBank4Auth.bank4RealCheck(bankCard, idcard, mobile, realname);
					log.info(PaymentChannelConstants.BANKCARD4_CHANNEL_2+"返回："+result);

				/**如果同样的银行卡已经存在，那么只验一次哦*/
//			if(bankCard4Auth != null){
//				if(bankCard4Auth.getBankcard().equals(bankCard.trim())&&bankCard4Auth.getIdcard().equals(idcard.trim())&&bankCard4Auth.getRealname().equals(realname.trim())&&bankCard4Auth.getMobile().equals(mobile.trim())) {
//				}else {
//
//				}
//			}else{
//				if(authChannel.equalsIgnoreCase(PaymentChannelConstants.BANKCARD4_CHANNEL_1)){
//
//					result = JuHeAPIBankCard4AuthService.bankCard4AuthJuhe(mobile, bankCard, idcard, realname);
//				}
//				if(authChannel.equalsIgnoreCase(PaymentChannelConstants.BANKCARD4_CHANNEL_2)){
//
//					result = JuHeAPIBankCard4AuthService.bankCard4AuthAli(mobile, bankCard, idcard, realname);
//				}
//				bankCard4Auth = new BankCard4Auth();
//			}
				if(bankCard4Auth == null){
					bankCard4Auth = new BankCard4Auth();
				}
				JSONObject jsonObject =  JSONObject.fromObject(result);
//				if(authChannel.equalsIgnoreCase(PaymentChannelConstants.BANKCARD4_CHANNEL_1)) {
//					if("0".equals(jsonObject.getString("error_code"))) {
//						JSONObject resObject  =  jsonObject.getJSONObject("result");
//						if(resObject == null || !jsonObject.containsKey("result")){
//							resObject = new JSONObject();
//							bankCard4Auth.setResCode(2);
//						}
//						if(resObject.has("jobid")){
//							bankCard4Auth.setJobid(resObject.getString("jobid"));
//							bankCard4Auth.setMessage(resObject.getString("message"));
//							bankCard4Auth.setResCode(resObject.getInt("res"));
//						}
//					}else {
//						bankCard4Auth.setMessage(jsonObject.getString("reason"));
//						bankCard4Auth.setResCode(2);
//					}



					if ("01".equals(jsonObject.getString("status"))) {
						if (jsonObject.containsKey("msg")) {
							bankCard4Auth.setMessage(jsonObject.getString("msg"));
							if ("01".equals(jsonObject.getString("status"))) {
								bankCard4Auth.setResCode(1);
							} else {
								bankCard4Auth.setResCode(2);
							}
						}
					} else {
						bankCard4Auth.setMessage(jsonObject.getString("msg"));
						bankCard4Auth.setResCode(2);
					}
				bankCard4Auth.setAuthTime(new Date());
				bankCard4Auth.setBankcard(bankCard);
				bankCard4Auth.setRealname(realname);
				bankCard4Auth.setIdcard(idcard);
				bankCard4Auth.setMobile(mobile);
				bankCard4repository.save(bankCard4Auth);
			}else{
				if(!realname.equals(bankCard4Auth.getRealname()) || !idcard.equals(bankCard4Auth.getIdcard()) || !realname.equals(bankCard4Auth.getRealname()) || !mobile.equals(bankCard4Auth.getMobile())){
					bankCard4Auth.setMessage("姓名/身份证号/预留手机号不匹配!");
					bankCard4Auth.setResCode(2);
				}
			}
		} catch (Exception e) {
			log.error("四要素校验错误::"+e.getMessage()+"\n请求第三方返回："+result);
			e.printStackTrace();
			bankCard4Auth.setMessage("信息有误！");
			bankCard4Auth.setResCode(2);
		}
		return bankCard4Auth;
	}


	@Override
	@Transactional
	public Object bankCard4Auth(String mobile, String bankCard, String idcard,
			String realname) {
		mobile = mobile.trim();
		bankCard = bankCard.trim();
		idcard = idcard.trim();
		realname = realname.trim();
		BankCard4AuthRoute bankCard4AuthRoute = bankCard4Routerepository.getCurActiveBankCard4Channel();
		
		String authChannel = bankCard4AuthRoute.getCurChannel();
		String result = "{}";
		
		
		BankCard4Auth  bankCard4Auth = bankCard4repository.findBankCard4AuthByCard(bankCard.trim());
		
//		if(bankCard4Auth==null) {
//			 bankCard4Auth = bankCard4repository.findBankCard4AuthByCard(bankCard.trim());
//		}
//		log.info(PaymentChannelConstants.BANKCARD4_CHANNEL_1+"返回："+result);
		try {
			if(bankCard4Auth == null || (bankCard4Auth != null && bankCard4Auth.getResCode() != 1)){
				if(authChannel.equalsIgnoreCase(PaymentChannelConstants.BANKCARD4_CHANNEL_1)){
					result = JuHeAPIBankCard4AuthService.bankCard4AuthJuhe(mobile, bankCard, idcard, realname,propertiesConfig.getAuth4Url(),propertiesConfig.getAuth4Key());
					log.info(PaymentChannelConstants.BANKCARD4_CHANNEL_1+"返回："+result);
				}	
				if(authChannel.equalsIgnoreCase(PaymentChannelConstants.BANKCARD4_CHANNEL_2)){
					result = JuHeAPIBankCard4AuthService.bankCard4AuthAli(mobile, bankCard, idcard, realname);
					log.info(PaymentChannelConstants.BANKCARD4_CHANNEL_2+"返回："+result);
				}
			
			/**如果同样的银行卡已经存在，那么只验一次哦*/
//			if(bankCard4Auth != null){
//				if(bankCard4Auth.getBankcard().equals(bankCard.trim())&&bankCard4Auth.getIdcard().equals(idcard.trim())&&bankCard4Auth.getRealname().equals(realname.trim())&&bankCard4Auth.getMobile().equals(mobile.trim())) {
//				}else {
//					
//				}
//			}else{
//				if(authChannel.equalsIgnoreCase(PaymentChannelConstants.BANKCARD4_CHANNEL_1)){
//					
//					result = JuHeAPIBankCard4AuthService.bankCard4AuthJuhe(mobile, bankCard, idcard, realname);
//				}
//				if(authChannel.equalsIgnoreCase(PaymentChannelConstants.BANKCARD4_CHANNEL_2)){
//					
//					result = JuHeAPIBankCard4AuthService.bankCard4AuthAli(mobile, bankCard, idcard, realname);
//				}
//				bankCard4Auth = new BankCard4Auth();
//			}
				if(bankCard4Auth == null){
					bankCard4Auth = new BankCard4Auth();
				}
				JSONObject jsonObject =  JSONObject.fromObject(result);
				if(authChannel.equalsIgnoreCase(PaymentChannelConstants.BANKCARD4_CHANNEL_1)) {
					if("0".equals(jsonObject.getString("error_code"))) {
						JSONObject resObject  =  jsonObject.getJSONObject("result");
						if(resObject == null || !jsonObject.containsKey("result")){
							resObject = new JSONObject();
							bankCard4Auth.setResCode(2);
						}
						if(resObject.has("jobid")){
							bankCard4Auth.setJobid(resObject.getString("jobid"));
							bankCard4Auth.setMessage(resObject.getString("message"));
							bankCard4Auth.setResCode(resObject.getInt("res"));
						}
					}else {
						bankCard4Auth.setMessage(jsonObject.getString("reason"));
						bankCard4Auth.setResCode(2);
					}
					
				}
				if(authChannel.equalsIgnoreCase(PaymentChannelConstants.BANKCARD4_CHANNEL_2)) {
					if("0".equals(jsonObject.getString("status"))) {
						JSONObject resObject  =  jsonObject.getJSONObject("result");
						if(resObject.containsKey("verifystatus")){
							bankCard4Auth.setMessage(resObject.getString("verifymsg"));
							if("0".equals(resObject.getString("verifystatus"))) {
								bankCard4Auth.setResCode(1);
							}else {
								bankCard4Auth.setResCode(2);
							}
						}
					}else {
						bankCard4Auth.setMessage(jsonObject.getString("msg"));
						bankCard4Auth.setResCode(2);
					}
				}
				bankCard4Auth.setAuthTime(new Date());
				bankCard4Auth.setBankcard(bankCard);
				bankCard4Auth.setRealname(realname);
				bankCard4Auth.setIdcard(idcard);
				bankCard4Auth.setMobile(mobile);
				bankCard4repository.save(bankCard4Auth);
			}else{
				if(!realname.equals(bankCard4Auth.getRealname()) || !idcard.equals(bankCard4Auth.getIdcard()) || !realname.equals(bankCard4Auth.getRealname()) || !mobile.equals(bankCard4Auth.getMobile())){
					bankCard4Auth.setMessage("姓名/身份证号/预留手机号不匹配!");
					bankCard4Auth.setResCode(2);
				}
			}
		} catch (Exception e) {
			log.error("四要素校验错误::"+e.getMessage()+"\n请求第三方返回："+result,e);
			bankCard4Auth.setMessage("信息有误！");
			bankCard4Auth.setResCode(2);
		}
		return bankCard4Auth;
	}
	
	@Transactional
	@Override
	public BankCardLocation findCardLocation(String cardid) {
		
		/**判断cardid是否已经验证过了*/
		BankCardLocation  location = bankCardLocationrepository.getCurBankCardLocation(cardid);
		if(location != null && location.getId() >0){
			return location;
		}else{
			
			String result =  JuHeAPIBankCard4AuthService.bankCardLocation(cardid,propertiesConfig.getCardLocationUrl(),propertiesConfig.getCardLocationKey());
			BankCardLocation bankCardLocation = new BankCardLocation();
			JSONObject jsonObject =  JSONObject.fromObject(result);
			log.info("result................"+result);
			
			if(jsonObject.getString("error_code").equalsIgnoreCase("0")){
			
				JSONObject resObject  =  jsonObject.getJSONObject("result");
				if(resObject == null || resObject.isNullObject()){
					resObject = new JSONObject();
				}
				if(resObject.has("bank")){
					
					bankCardLocation.setBankLocation(resObject.getString("bank"));
					bankCardLocation.setCardid(cardid);
					bankCardLocation.setCreateTime(new Date());
					bankCardLocation.setLogo(resObject.getString("logo"));
					bankCardLocation.setType(resObject.getString("nature"));
					bankCardLocation.setNature(resObject.getString("cardtype"));
					bankCardLocation.setInfo(resObject.getString("info"));
				}
			}
			bankCardLocation.setErrorCode(jsonObject.getString("error_code"));
			bankCardLocation.setReason(jsonObject.getString("reason"));
			bankCardLocation = bankCardLocationrepository.save(bankCardLocation);
			em.flush();
			return bankCardLocation;
			
		}

	}
	
	
	
}
