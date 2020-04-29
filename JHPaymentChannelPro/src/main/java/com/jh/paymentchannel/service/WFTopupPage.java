package com.jh.paymentchannel.service;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import com.jh.paymentchannel.business.BranchbankBussiness;
import com.jh.paymentchannel.business.TopupPayChannelBusiness;
import com.jh.paymentchannel.pojo.PaymentOrder;
import com.jh.paymentchannel.util.Util;

import cn.jh.common.tools.ResultWrap;
import cn.jh.common.utils.CommonConstants;
import net.sf.json.JSONObject;

@Service
public class WFTopupPage implements TopupRequest {

	private static final Logger log = LoggerFactory.getLogger(WFTopupPage.class);

	@Autowired
	private TopupPayChannelBusiness topupPayChannelBusiness;

	@Autowired
	private BranchbankBussiness branchbankBussiness;

	@Autowired
	private Util util;
	
	@Autowired
	private WFpageRequest wfpageRequest;

	@Value("${payment.ipAddress}")
	private String ipAddress;

	@Override
	public Map<String, String> topupRequest(Map<String,Object> params) throws Exception {
		PaymentOrder paymentOrder = (PaymentOrder) params.get("paymentOrder");
		HttpServletRequest request = (HttpServletRequest) params.get("request");
		String ordercode = paymentOrder.getOrdercode();
		String amount = paymentOrder.getAmount().toString();
		
		Map<String, String> map = new HashMap<String, String>();
		/** 首先拿到用户的userid, 如果用户已经存在，那么直接拿用户的sdjuserid, 如果用户的默认结算卡已经和当前不一样了。 需要重新注册 **/
		RestTemplate restTemplate = new RestTemplate();
		URI uri = util.getServiceUrl("transactionclear", "error url request!");
		String url = uri.toString() + "/v1.0/transactionclear/payment/query/ordercode";
		MultiValueMap<String, String> requestEntity = new LinkedMultiValueMap<String, String>();
		requestEntity.add("order_code", ordercode);
		String result = restTemplate.postForObject(url, requestEntity, String.class);
		log.info("RESULT================" + result);
		JSONObject jsonObject = JSONObject.fromObject(result);
		JSONObject resultObj = jsonObject.getJSONObject("result");
		String userid = resultObj.getString("userid");
		// 费率
		String rate = resultObj.getString("rate");
		// 充值卡卡号
		String bankCard = resultObj.getString("bankcard");
		
		String orderType = resultObj.getString("type");
		
		String realAmount = resultObj.getString("realAmount");

		restTemplate = new RestTemplate();
		uri = util.getServiceUrl("user", "error url request!");
		url = uri.toString() + "/v1.0/user/bank/default/cardno";
		requestEntity = new LinkedMultiValueMap<String, String>();
		requestEntity.add("cardno", bankCard);
		requestEntity.add("type", "0");
		try {
			result = restTemplate.postForObject(url, requestEntity, String.class);
			log.info("接口/v1.0/user/bank/default/cardno--RESULT================" + result);
			jsonObject = JSONObject.fromObject(result);
			resultObj = jsonObject.getJSONObject("result");
			String mobile = resultObj.getString("phone"); // 预留信用卡手机号码
		} catch (Exception e) {
			log.error("查询银行卡信息出错");
			map.put("resp_code", "failed");
			map.put("channel_type", "sdj");
			map.put("resp_message", "查询不到该银行卡信息,可能已被删除!");
			return map;
		}
		String idcard = resultObj.getString("idcard");// 身份证号
		String userName = resultObj.getString("userName");// 用户姓名
		String bankName = resultObj.getString("bankName");
		String expiredTime = resultObj.getString("expiredTime");
		String securityCode = resultObj.getString("securityCode");
		
		if (bankName != null && bankName.contains("交通")) {
			map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			map.put(CommonConstants.RESP_MESSAGE, "交易失败：该银行卡暂不支持使用该功能");
			map.put("channel_type", "sdj");
			return map;
		}
		
		if(securityCode != null && (securityCode.length() !=3 || !securityCode.matches("^[0-9]*$"))){
			map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			map.put(CommonConstants.RESP_MESSAGE, "安全码非法,请重新设置");
			map.put("channel_type", "sdj");
			return map;
		}
		
		String nature = resultObj.getString("nature");

		//WFRegister wfRegisterByBankCard = topupPayChannelBusiness.getWFRegisterByBankCard(bankCard);

		if("10".equals(orderType)) {
			log.info("根据判断进入消费任务======");
			
			map  = (Map<String, String>) wfpageRequest.wfPay(request, ordercode, securityCode, expiredTime, realAmount);
			
			/*if (null == wfRegisterByBankCard) {

				Map maps = new HashMap();
				maps.put("resp_code", "success");
				maps.put("channel_type", "sdj");
				maps.put("redirect_url",
						ipAddress + "/v1.0/paymentchannel/topup/towfbankinfo?bankName="
								+ URLEncoder.encode(bankName, "UTF-8") + "&bankNo=" + bankCard + "&amount=" + realAmount
								+ "&ordercode=" + ordercode + "&userName=" + URLEncoder.encode(userName, "UTF-8")
								+ "&idCard=" + idcard + "&nature=" + URLEncoder.encode(nature, "UTF-8") + "&expiredTime="
								+ expiredTime + "&securityCode=" + securityCode+"&phone="+mobile+"&ipAddress="+ipAddress);
				return maps;

			}else {
				
				String expDate = wfRegisterByBankCard.getExpDate();
				String cvn2 = wfRegisterByBankCard.getCvn2();
				
				
				
			}*/
			
		}
		
		if("11".equals(orderType)){
			log.info("根据判断进入还款任务");
			map = (Map) wfpageRequest.OrderCodeTransfer(request, ordercode, userName, bankCard, realAmount, ipAddress+"/v1.0/paymentchannel/topup/wf/transfernotify_call");
			
			
		}

		
		if("1".equals(orderType)) {
			log.info("根据判断进入购买产品");
			map  = (Map<String, String>) wfpageRequest.wfPay(request, ordercode, securityCode, expiredTime, amount);

		}
		

		return map;

	}

}
