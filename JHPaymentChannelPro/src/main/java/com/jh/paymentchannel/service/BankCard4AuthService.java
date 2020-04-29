package com.jh.paymentchannel.service;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.jh.paymentchannel.business.BankCard4AuthBusiness;
import com.jh.paymentchannel.business.BankCard4AuthHistoryBusiness;
import com.jh.paymentchannel.business.BankCardLocationHistoryBusiness;
import com.jh.paymentchannel.business.BrandCardAuthCountBusiness;
import com.jh.paymentchannel.pojo.BankCard4AuthHistory;
import com.jh.paymentchannel.pojo.BankCardLocation;
import com.jh.paymentchannel.pojo.BankCardLocationHistory;
import com.jh.paymentchannel.pojo.BrandCardAuthCount;
import com.jh.paymentchannel.util.Util;

import cn.jh.common.utils.AuthorizationHandle;
import cn.jh.common.utils.CommonConstants;
import cn.jh.common.utils.ExceptionUtil;
import cn.jh.common.utils.Md5Util;
import net.sf.json.JSONObject;


@Controller
@EnableAutoConfiguration
public class BankCard4AuthService {

	
	private static final Logger LOG = LoggerFactory.getLogger(BankCard4AuthService.class);
	
	@Autowired
	private BankCard4AuthBusiness bankCard4AuthBusiness;
	
	@Autowired
	private BrandCardAuthCountBusiness brandCardAuthCountBusiness;
	
	@Autowired
	private BankCard4AuthHistoryBusiness bankCard4AuthHistoryBusiness;
	
	@Autowired
	private BankCardLocationHistoryBusiness bankCardLocationHistoryBusiness;
	
	@Autowired
	private RestTemplate restTemplate;
	
	@Autowired
	private Util util;
	
	private static final BigDecimal perCardAuth4Price = BigDecimal.valueOf(0.3500).setScale(4);
	
	private static final BigDecimal perCardTypePrice = BigDecimal.valueOf(0.0099).setScale(4);
	
	private static final BigDecimal perSmsInFormPrice = BigDecimal.valueOf(0.045).setScale(4);
	
	
	/**银行四要素验证**/
	@RequestMapping(method=RequestMethod.POST,value="/v1.0/paymentchannel/bankcard4/auth")
	public @ResponseBody Object bandcard4Auth(HttpServletRequest request, 
			@RequestParam(value = "realname") String realname,
			@RequestParam(value = "idcard") String idcard,
			@RequestParam(value = "bankcard") String bankcard,	
			@RequestParam(value = "brandId") String brandId,	
			@RequestParam(value = "mobile") String mobile
			){
		Map<String,Object> map = new HashMap<String,Object>();
		BrandCardAuthCount brandCardAuthCount = brandCardAuthCountBusiness.findByBrandId(brandId);
		BankCardLocation cardLocation = null;
		Object bankCard4Auth = null;
		
		if(brandCardAuthCount != null && brandCardAuthCount.getBankCardAuthCount().intValue() > 0 && brandCardAuthCount.getBankCardTypeCount().intValue() > 0 || brandCardAuthCount.getBankCardAuthCount().intValue() == -1024){
			cardLocation = bankCard4AuthBusiness.findCardLocation(bankcard);
			bankCard4Auth = bankCard4AuthBusiness.bankCard4Auth(mobile, bankcard, idcard, realname);
		}else{
			map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			map.put(CommonConstants.RESP_MESSAGE, "绑卡次数不足,请联系管理员及时充值!");
			return map;
		}
		if(brandCardAuthCount.getBankCardAuthCount().intValue() != -1024){
			brandCardAuthCount.setBankCardAuthCount(brandCardAuthCount.getBankCardAuthCount()-1);
			brandCardAuthCount.setBankCardTypeCount(brandCardAuthCount.getBankCardTypeCount()-1);
			brandCardAuthCount = brandCardAuthCountBusiness.save(brandCardAuthCount);
		}
		
		int bankCardAuthCount = brandCardAuthCount.getBankCardAuthCount().intValue();
		int bankCardTypeCount = brandCardAuthCount.getBankCardTypeCount().intValue();
		if( bankCardAuthCount == 50 || bankCardTypeCount == 50 || bankCardAuthCount == 10 || bankCardTypeCount == 10){
			try {
				String url = "http://user/v1.0/user/brand/query/id?brand_id=" + brandId;
				String resultString = restTemplate.getForObject(url, String.class);
				JSONObject resultJSON = JSONObject.fromObject(resultString);
				resultJSON = resultJSON.getJSONObject(CommonConstants.RESULT);
				String userId = resultJSON.getString("manageid");
				String message = "";
				if(bankCardAuthCount <= 50){
					message = "您帐户中的银行卡四要素验证次数已不足" + bankCardAuthCount + "次,为不影响用户正常使用,请及时充值!";
				}else{
					message = "您帐户中的银行卡真伪验证次数已不足" + bankCardTypeCount + "次,为不影响用户正常使用,请及时充值!";
				}
				util.pushMessage(userId, message, "银行卡验证次数提醒");
			} catch (RestClientException e) {
				e.printStackTrace();LOG.error("",e);
			}
		}
		
		BankCard4AuthHistory bankCard4AuthHistory = new BankCard4AuthHistory();
		BankCardLocationHistory bankCardLocationHistory = new BankCardLocationHistory();
		BeanUtils.copyProperties(bankCard4Auth, bankCard4AuthHistory);
		BeanUtils.copyProperties(cardLocation, bankCardLocationHistory);
		bankCard4AuthHistory.setBrandId(brandId);
		bankCard4AuthHistory.setId(0);
		bankCard4AuthHistory.setAuthTime(new Date());
		bankCardLocationHistory.setBrandId(brandId);
		bankCardLocationHistory.setId(0);
		bankCardLocationHistory.setCreateTime(new Date());
		bankCard4AuthHistory = bankCard4AuthHistoryBusiness.save(bankCard4AuthHistory);
		bankCardLocationHistory = bankCardLocationHistoryBusiness.save(bankCardLocationHistory);
		map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
		map.put("location", cardLocation);
		map.put("auth",bankCard4Auth);
		return map;
	}

	/**
	 * 银行四要素验证，后台接口
	 * @author jayden
	 * @param request
	 * @param realname
	 * @param idcard
	 * @param bankcard
	 * @param brandId
	 * @param mobile
	 * @return
	 */
	@RequestMapping(method=RequestMethod.POST,value="/v1.0/paymentchannel/bankcard4/auth/backstage")
	public @ResponseBody Object bandcard4AuthBackstage(HttpServletRequest request,
											  @RequestParam(value = "realname") String realname,
											  @RequestParam(value = "idcard") String idcard,
											  @RequestParam(value = "bankcard") String bankcard,
											  @RequestParam(value = "brandId") String brandId,
											  @RequestParam(value = "mobile") String mobile
	){
		Map<String,Object> map = new HashMap<String,Object>();
		BrandCardAuthCount brandCardAuthCount = brandCardAuthCountBusiness.findByBrandId(brandId);
		BankCardLocation cardLocation = null;
		Object bankCard4Auth = null;

		if(brandCardAuthCount != null && brandCardAuthCount.getBankCardAuthCount().intValue() > 0 && brandCardAuthCount.getBankCardTypeCount().intValue() > 0 || brandCardAuthCount.getBankCardAuthCount().intValue() == -1024){
			cardLocation = bankCard4AuthBusiness.findCardLocation(bankcard);
			bankCard4Auth = bankCard4AuthBusiness.bankCard4AuthBackstage(mobile, bankcard, idcard, realname);
		}else{
			map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			map.put(CommonConstants.RESP_MESSAGE, "绑卡次数不足,请联系管理员及时充值!");
			return map;
		}
		if(brandCardAuthCount.getBankCardAuthCount().intValue() != -1024){
			brandCardAuthCount.setBankCardAuthCount(brandCardAuthCount.getBankCardAuthCount()-1);
			brandCardAuthCount.setBankCardTypeCount(brandCardAuthCount.getBankCardTypeCount()-1);
			brandCardAuthCount = brandCardAuthCountBusiness.save(brandCardAuthCount);
		}

		int bankCardAuthCount = brandCardAuthCount.getBankCardAuthCount().intValue();
		int bankCardTypeCount = brandCardAuthCount.getBankCardTypeCount().intValue();
		if( bankCardAuthCount == 50 || bankCardTypeCount == 50 || bankCardAuthCount == 10 || bankCardTypeCount == 10){
			try {
				String url = "http://user/v1.0/user/brand/query/id?brand_id=" + brandId;
				String resultString = restTemplate.getForObject(url, String.class);
				JSONObject resultJSON = JSONObject.fromObject(resultString);
				resultJSON = resultJSON.getJSONObject(CommonConstants.RESULT);
				String userId = resultJSON.getString("manageid");
				String message = "";
				if(bankCardAuthCount <= 50){
					message = "您帐户中的银行卡四要素验证次数已不足" + bankCardAuthCount + "次,为不影响用户正常使用,请及时充值!";
				}else{
					message = "您帐户中的银行卡真伪验证次数已不足" + bankCardTypeCount + "次,为不影响用户正常使用,请及时充值!";
				}
				util.pushMessage(userId, message, "银行卡验证次数提醒");
			} catch (RestClientException e) {
				e.printStackTrace();LOG.error("",e);
			}
		}

		BankCard4AuthHistory bankCard4AuthHistory = new BankCard4AuthHistory();
		BankCardLocationHistory bankCardLocationHistory = new BankCardLocationHistory();
		BeanUtils.copyProperties(bankCard4Auth, bankCard4AuthHistory);
		BeanUtils.copyProperties(cardLocation, bankCardLocationHistory);
		bankCard4AuthHistory.setBrandId(brandId);
		bankCard4AuthHistory.setId(0);
		bankCard4AuthHistory.setAuthTime(new Date());
		bankCardLocationHistory.setBrandId(brandId);
		bankCardLocationHistory.setId(0);
		bankCardLocationHistory.setCreateTime(new Date());
		bankCard4AuthHistory = bankCard4AuthHistoryBusiness.save(bankCard4AuthHistory);
		bankCardLocationHistory = bankCardLocationHistoryBusiness.save(bankCardLocationHistory);
		map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
		map.put("location", cardLocation);
		map.put("auth",bankCard4Auth);
		return map;
	}
	/**
	 * 验证卡类型
	 * @param request
	 * @param cardNumber
	 * @return
	 */
	@RequestMapping(method=RequestMethod.POST,value="/v1.0/paymentchannel/auth/bankcardtype")
	public @ResponseBody Object verifyBankCardType(HttpServletRequest request,
			@RequestParam(value="cardNumber")String cardNumber,
			@RequestParam(value="brandId",required=false)String brandId
			){
		Map<String,Object> map = new HashMap<>();
		
		Map<String, Object> verifyStringFiledIsNull = AuthorizationHandle.verifyStringFiledIsNull(cardNumber);
		if(!CommonConstants.SUCCESS.equals(verifyStringFiledIsNull.get(CommonConstants.RESP_CODE))){
			return verifyStringFiledIsNull;
		}
		
		BrandCardAuthCount brandCardAuthCount = null;
		BankCardLocation cardLocation = null;
		if(brandId != null && !"".equals(brandId)){
			brandCardAuthCount = brandCardAuthCountBusiness.findByBrandId(brandId);
		}

		if(brandCardAuthCount == null || brandCardAuthCount.getBankCardTypeCount().intValue() > 0 || brandCardAuthCount.getBankCardAuthCount().intValue() == -1024){
			cardLocation = bankCard4AuthBusiness.findCardLocation(cardNumber);
		}
		if(brandCardAuthCount != null && brandCardAuthCount.getBankCardAuthCount().intValue() != -1024){
			brandCardAuthCount.setBankCardTypeCount(brandCardAuthCount.getBankCardTypeCount() - 1);
			brandCardAuthCount = brandCardAuthCountBusiness.save(brandCardAuthCount);
			BankCardLocationHistory bankCardLocationHistory = new BankCardLocationHistory();
			BeanUtils.copyProperties(cardLocation, bankCardLocationHistory);
			bankCardLocationHistory.setCardid(cardNumber);
			bankCardLocationHistory.setBrandId(brandId);
			bankCardLocationHistory.setId(0);
			bankCardLocationHistory.setCreateTime(new Date());
			bankCardLocationHistory = bankCardLocationHistoryBusiness.save(bankCardLocationHistory);
		}
		
		
		if(cardLocation ==null){
			map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			map.put(CommonConstants.RESP_MESSAGE, "卡号有误,请检查后再输入");
			return map;
		}
		
		map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
		map.put(CommonConstants.RESP_MESSAGE, "查询成功");
		map.put(CommonConstants.RESULT, cardLocation);
		return map;
	}
	
	
	/**所有银行卡四要素的分页查询*/
	@RequestMapping(method=RequestMethod.GET,value="/v1.0/paymentchannel/bankcard4/all")
	public @ResponseBody Object allBankCard4s(HttpServletRequest request,
			@RequestParam(value = "page", defaultValue = "0", required = false) int page,
			@RequestParam(value = "size", defaultValue = "20", required = false) int size,
			@RequestParam(value = "order", defaultValue = "DESC", required = false) Sort.Direction direction,
			@RequestParam(value = "sort", defaultValue = "authTime", required = false) String sortProperty){
		Pageable pageable = new PageRequest(page, size, new Sort(direction,sortProperty));
		Map map = new HashMap();
		map.put(CommonConstants.RESP_CODE,CommonConstants.SUCCESS);
		map.put(CommonConstants.RESULT, bankCard4AuthBusiness.findAllBankCard4s(pageable));
		return map;
	}
	
	
	/**查询所有成功验证的分页*/
	@RequestMapping(method=RequestMethod.GET,value="/v1.0/paymentchannel/bankcard4/all_success")
	public @ResponseBody Object allSuccessBankcard4s(HttpServletRequest request, 
			@RequestParam(value = "page", defaultValue = "0", required = false) int page,
			@RequestParam(value = "size", defaultValue = "20", required = false) int size,
			@RequestParam(value = "order", defaultValue = "DESC", required = false) Sort.Direction direction,
			@RequestParam(value = "sort", defaultValue = "authTime", required = false) String sortProperty){
		Pageable pageable = new PageRequest(page, size, new Sort(direction,sortProperty));
		
		Map map = new HashMap();
		map.put(CommonConstants.RESP_CODE,CommonConstants.SUCCESS);
		map.put(CommonConstants.RESULT, bankCard4AuthBusiness.findAllSuccessBankCard4s(pageable));
		return map;
	}
	
	/**根据银行卡号*/
	@RequestMapping(method=RequestMethod.POST,value="/v1.0/paymentchannel/bankcard4/bankcard")
	public @ResponseBody Object bankCard4ByBank(HttpServletRequest request, 
			@RequestParam(value = "bankcard") String bankcard){
		
		Map map = new HashMap();
		map.put(CommonConstants.RESP_CODE,CommonConstants.SUCCESS);
		map.put(CommonConstants.RESULT, bankCard4AuthBusiness.findBankCard4AuthByCard(bankcard));
		return map;
	}
	
	
	/**根据手机号码*/
	@RequestMapping(method=RequestMethod.POST,value="/v1.0/paymentchannel/bankcard4/mobile")
	public @ResponseBody Object bankCard4ByMobile(HttpServletRequest request, 
		   @RequestParam(value = "mobile") String mobile){
		
		Map map = new HashMap();
		map.put(CommonConstants.RESP_CODE,CommonConstants.SUCCESS);
		map.put(CommonConstants.RESULT, bankCard4AuthBusiness.findBankCard4AuthByMobile(mobile));
		return map;
	
	}
	

	
	/**根据卡号获取归属地*/
	@RequestMapping(method=RequestMethod.POST,value="/v1.0/paymentchannel/bankcard/location")
	public @ResponseBody Object queryLocationByCard(HttpServletRequest request, 
			@RequestParam(value = "cardid") String cardid){
		Map map = new HashMap();
		map.put(CommonConstants.RESP_CODE,CommonConstants.SUCCESS);
		map.put(CommonConstants.RESULT,bankCard4AuthBusiness.findCardLocation(cardid));
		return map;
	}
	
	
	@RequestMapping(method=RequestMethod.POST,value="/v1.0/paymentchannel/create/bankcard/auth/count")
	public @ResponseBody Object createNewBrandCardAuthCount(
			@RequestParam(value = "brandId")String brandId
			){
		Map<String,Object> map = new HashMap<>();
		BrandCardAuthCount cardAuthCount = brandCardAuthCountBusiness.findByBrandId(brandId);
		if(cardAuthCount != null){
			map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			map.put(CommonConstants.RESP_MESSAGE, "已创建过该贴牌帐户,无法再次创建!");
			return map;
		}else{
			cardAuthCount = new BrandCardAuthCount();
			cardAuthCount.setBrandId(brandId);
			cardAuthCount.setBankCardAuthCount(2000L);
			cardAuthCount.setBankCardTypeCount(2000L);
			cardAuthCount = brandCardAuthCountBusiness.save(cardAuthCount);
		}
		
		map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
		map.put(CommonConstants.RESP_MESSAGE, "创建贴牌帐户成功!");
		map.put(CommonConstants.RESULT, cardAuthCount);
		return map;
	}
	
	@RequestMapping(method=RequestMethod.POST,value="/v1.0/paymentchannel/delete/bankcard/auth/count")
	public @ResponseBody Object deleteBrandCardAuthCount(
			@RequestParam(value = "brandId")String brandId
			){
		Map<String,Object> map = new HashMap<>();
		BrandCardAuthCount cardAuthCount = brandCardAuthCountBusiness.findByBrandId(brandId);
		if(cardAuthCount == null){
			map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			map.put(CommonConstants.RESP_MESSAGE, "无该贴牌帐户,无法进行删除!");
			return map;
		}else{
			brandCardAuthCountBusiness.delete(cardAuthCount);
		}
		
		map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
		map.put(CommonConstants.RESP_MESSAGE, "删除贴牌帐户成功!");
		return map;
	}
	
	@RequestMapping(method=RequestMethod.POST,value="/v1.0/paymentchannel/update/bankcard/auth/count")
	public @ResponseBody Object updateCardAuthCount(
//			type: auth4 四要素充值, type: cardType 银行卡类型充值 ;type: sms 短信推送次数
			@RequestParam(value="type")String type,
			@RequestParam(value="amount")String amount,
			@RequestParam(value="brandId")String brandId
			){
		Map<String,Object> map = new HashMap<>();
		BrandCardAuthCount cardAuthCount = brandCardAuthCountBusiness.findByBrandId(brandId);
		if(cardAuthCount == null){
			map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			map.put(CommonConstants.RESP_MESSAGE, "无该贴牌帐户,无法充值!");
			return map;			
		}
		
		
		if("auth4".equals(type)){
			int count = new BigDecimal(amount).divide(perCardAuth4Price,0,BigDecimal.ROUND_HALF_UP).intValue();
			cardAuthCount.setBankCardAuthCount(cardAuthCount.getBankCardAuthCount() + Long.valueOf(count));
		}else if("cardType".equals(type)){
			int count = new BigDecimal(amount).divide(perCardTypePrice,0,BigDecimal.ROUND_HALF_UP).intValue();
			cardAuthCount.setBankCardTypeCount(cardAuthCount.getBankCardTypeCount() + Long.valueOf(count));
		}else if("sms".equals(type)){
			int count = new BigDecimal(amount).divide(perSmsInFormPrice,0,BigDecimal.ROUND_HALF_UP).intValue();
			String url = "http://notice/v1.0/notice/sms/update/count";
			MultiValueMap<String, String> requestEntity = new LinkedMultiValueMap<String, String>();
			requestEntity.add("type", "smsInform");
			requestEntity.add("amount", amount);
			requestEntity.add("brandId", brandId);
			
			String resultString = restTemplate.postForObject(url,requestEntity, String.class);
			JSONObject resultJSON = JSONObject.fromObject(resultString);
			if(resultJSON.get(CommonConstants.RESP_CODE).equals(CommonConstants.SUCCESS)) {
				resultJSON = resultJSON.getJSONObject(CommonConstants.RESULT);
				cardAuthCount.setSmsCount(resultJSON.getLong("smsCount"));
			}else {
				cardAuthCount.setSmsCount(Long.valueOf(count));
			}
			
		}
		
		brandCardAuthCountBusiness.save(cardAuthCount);
		map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
		map.put(CommonConstants.RESP_MESSAGE, "充值成功!");
		map.put(CommonConstants.RESULT, cardAuthCount);
		return map;
	}
	
	
	@RequestMapping(method=RequestMethod.POST,value="/v1.0/paymentchannel/query/bankcard/auth/count")
	public @ResponseBody Object queryCountByBrandId(
			@RequestParam(value="brandId")String brandId
			){
		Map<String,Object> map = new HashMap<>();
		BrandCardAuthCount cardAuthCount = brandCardAuthCountBusiness.findByBrandId(brandId);
		if(cardAuthCount == null){
			map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			map.put(CommonConstants.RESP_MESSAGE, "无该贴牌帐户");
			return map;
		}
		try {
			
			String url = "http://notice/v1.0/notice/sms/selcreat/brandcount";
			MultiValueMap<String, String> requestEntity = new LinkedMultiValueMap<String, String>();
			requestEntity.add("brandId", brandId);
			
			String resultString = restTemplate.postForObject(url,requestEntity, String.class);
			JSONObject resultJSON = JSONObject.fromObject(resultString);
			if(resultJSON.get(CommonConstants.RESP_CODE).equals(CommonConstants.SUCCESS)) {
				resultJSON = resultJSON.getJSONObject(CommonConstants.RESULT);
				cardAuthCount.setSmsCount(resultJSON.getLong("smsCount"));
			}else {
				cardAuthCount.setSmsCount(0l);
			}
		} catch (Exception e) {
			
			cardAuthCount.setSmsCount(0l);
			
		}
		map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
		map.put(CommonConstants.RESP_MESSAGE, "查询成功!");
		map.put(CommonConstants.RESULT, cardAuthCount);
		return map;
	}
	
	
	@RequestMapping(method=RequestMethod.POST,value="/v1.0/paymentchannel/get/bankcard/auth/price")
	public @ResponseBody Object getBankCardAuthPrice(){
		Map<String,Object>map = new HashMap<>();
		map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
		map.put(CommonConstants.RESP_MESSAGE, "查询成功");
		Map<String,Object> resultMap = new HashMap<>();
		DecimalFormat df1 = new DecimalFormat("0.0000"); 
		resultMap.put("perCardAuth4Price", df1.format(perCardAuth4Price));
		resultMap.put("perCardTypePrice",  df1.format(perCardTypePrice));
		resultMap.put("perSmsInFormPrice",  df1.format(perSmsInFormPrice));
		map.put(CommonConstants.RESULT, resultMap);
		return map;
	}
	
	@RequestMapping(method=RequestMethod.POST,value="/v1.0/paymentchannel/topup/bankcard/auth/count")
	public @ResponseBody Object buyCardAuthCount(
//			type: auth4 四要素验证充值, type: cardType 银行卡类型充值 
			@RequestParam(value="type")String type,
			@RequestParam(value="amount")String amount,
			@RequestParam(value="userId")String userId,
			@RequestParam(value="payPassword")String payPassword
			){
		payPassword = payPassword.trim();
		Map<String, Object> verifyMoney = AuthorizationHandle.verifyMoney(amount, 2, BigDecimal.ROUND_HALF_DOWN);
		if(!CommonConstants.SUCCESS.equals(verifyMoney.get(CommonConstants.RESP_CODE))){
			return verifyMoney;
		}
		
		Map<String,Object> map = new HashMap<>();
		String url = "http://user/v1.0/user/find/by/userid";
		MultiValueMap<String, String> requestEntity = new LinkedMultiValueMap<String, String>();
		requestEntity.add("userId", userId);
		String resultString = restTemplate.postForObject(url,requestEntity, String.class);
		JSONObject resultJSON = JSONObject.fromObject(resultString);
		String respCode = resultJSON.getString(CommonConstants.RESP_CODE);
		if(!CommonConstants.SUCCESS.equals(respCode)){
			map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			map.put(CommonConstants.RESP_MESSAGE, resultJSON.containsKey(CommonConstants.RESP_MESSAGE)?resultJSON.getString(CommonConstants.RESP_MESSAGE):"由于未知原因,查询用户失败!");
			return map;
		}
		resultJSON = resultJSON.getJSONObject(CommonConstants.RESULT);
		String brandId = resultJSON.getString("brandId");
		String payPass = resultJSON.getString("paypass");
		String phone = resultJSON.getString("phone");
		if(!payPass.equals(Md5Util.getMD5(payPassword))){
			map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			map.put(CommonConstants.RESP_MESSAGE, "支付密码有误,请重新输入!");
			return map;
		}
		
		url = "http://user/v1.0/user/account/query/userId";
		requestEntity = new LinkedMultiValueMap<String, String>();
		requestEntity.add("user_id", userId);
		resultString = restTemplate.postForObject(url, requestEntity, String.class);
		resultJSON = JSONObject.fromObject(resultString);
		resultJSON = resultJSON.getJSONObject(CommonConstants.RESULT);
		String balance = resultJSON.getString("balance");
		if(new BigDecimal(balance).compareTo(new BigDecimal(amount)) < 0){
			map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			map.put(CommonConstants.RESP_MESSAGE, "您的余额不足"+amount+"元,请充值!");
			return map;
		}
		String orderCode = null;
		if("auth4".equals(type)){
			url = "http://transactionclear/v1.0/transactionclear/payment/add";
			requestEntity = new LinkedMultiValueMap<String, String>();
			requestEntity.add("type", "12");
			requestEntity.add("phone", phone);
			requestEntity.add("amount", amount);
			requestEntity.add("channel_tag", "JIEFUBAO");
			requestEntity.add("desc", "银行卡四要素验证次数充值");
			resultString = restTemplate.postForObject(url, requestEntity,String.class);
			resultJSON = JSONObject.fromObject(resultString);
			resultJSON = resultJSON.getJSONObject(CommonConstants.RESULT);
			orderCode = resultJSON.getString("ordercode");

		}else if("cardType".equals(type)){
			url = "http://transactionclear/v1.0/transactionclear/payment/add";
			requestEntity = new LinkedMultiValueMap<String, String>();
			requestEntity.add("type", "12");
			requestEntity.add("phone", phone);
			requestEntity.add("amount", amount);
			requestEntity.add("channel_tag", "JIEFUBAO");
			requestEntity.add("desc", "银行卡真伪及类型次数充值");
			resultString = restTemplate.postForObject(url, requestEntity,String.class);
			resultJSON = JSONObject.fromObject(resultString);
			resultJSON = resultJSON.getJSONObject(CommonConstants.RESULT);
			orderCode = resultJSON.getString("ordercode");
		}else if("sms".equals(type)){
			url = "http://transactionclear/v1.0/transactionclear/payment/add";
			requestEntity = new LinkedMultiValueMap<String, String>();
			requestEntity.add("type", "12");
			requestEntity.add("phone", phone);
			requestEntity.add("amount", amount);
			requestEntity.add("channel_tag", "JIEFUBAO");
			requestEntity.add("desc", "短信推送次数充值");
			resultString = restTemplate.postForObject(url, requestEntity,String.class);
			resultJSON = JSONObject.fromObject(resultString);
			resultJSON = resultJSON.getJSONObject(CommonConstants.RESULT);
			orderCode = resultJSON.getString("ordercode");
		}else{
			map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			map.put(CommonConstants.RESP_MESSAGE, "充值类型有误,请重新输入!");
			return map;
		}
		
		url = "http://user/v1.0/user/account/update";
		requestEntity = new LinkedMultiValueMap<String, String>();
		requestEntity.add("user_id", userId);
		requestEntity.add("amount", amount);
		requestEntity.add("addorsub", "1");
		requestEntity.add("order_code", orderCode);
		resultString = restTemplate.postForObject(url, requestEntity,String.class);
		resultJSON = JSONObject.fromObject(resultString);
		respCode = resultJSON.getString(CommonConstants.RESP_CODE);
		if(!CommonConstants.SUCCESS.equals(respCode)){
			map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			map.put(CommonConstants.RESP_MESSAGE, resultJSON.containsKey(CommonConstants.RESP_MESSAGE)?resultJSON.getString(CommonConstants.RESP_MESSAGE):"由于未知原因,支付失败!");
			return map;
		}
		
		map = (Map<String, Object>) this.updateCardAuthCount(type, amount, brandId);
		if(!CommonConstants.SUCCESS.equals(map.get(CommonConstants.RESP_CODE))){
			
			url = "http://user/v1.0/user/account/update";
			requestEntity = new LinkedMultiValueMap<String, String>();
			requestEntity.add("user_id", userId);
			requestEntity.add("amount", amount);
			requestEntity.add("addorsub", "0");
			requestEntity.add("order_code", orderCode);
			resultString = restTemplate.postForObject(url, requestEntity,String.class);
			
			map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			map.put(CommonConstants.RESP_MESSAGE, map.get(CommonConstants.RESP_MESSAGE));
			return map;
		}
		
		
		url = "http://transactionclear/v1.0/transactionclear/payment/update";
		requestEntity = new LinkedMultiValueMap<String, String>();
		requestEntity.add("order_code", orderCode);
		requestEntity.add("status", "1");
		resultString = restTemplate.postForObject(url, requestEntity,String.class);
		
		
		return map;
	}
	
	
			
	
}