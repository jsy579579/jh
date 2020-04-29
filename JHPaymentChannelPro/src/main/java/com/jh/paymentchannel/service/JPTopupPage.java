package com.jh.paymentchannel.service;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.google.gson.Gson;
import com.jh.paymentchannel.basechannel.BaseChannel;
import com.jh.paymentchannel.business.BranchbankBussiness;
import com.jh.paymentchannel.business.TopupPayChannelBusiness;
import com.jh.paymentchannel.pojo.CJRegister;
import com.jh.paymentchannel.pojo.JPBindCard;
import com.jh.paymentchannel.pojo.JPRegister;
import com.jh.paymentchannel.pojo.PaymentOrder;
import com.jh.paymentchannel.util.Util;
import com.jh.paymentchannel.util.cjhk.ApiClient;
import com.jh.paymentchannel.util.cjhk.ApiRequest;
import com.jh.paymentchannel.util.cjhk.ApiResponse;
import com.jh.paymentchannel.util.cjhk.EncryptTypeEnum;
import com.jh.paymentchannel.util.jp.HttpUtils;
import com.jh.paymentchannel.util.jp.Rates;
import com.jh.paymentchannel.util.jp.RegisterParam;
import com.jh.paymentchannel.util.jp.UpdateRates;
import com.jh.paymentchannel.util.jp.UpdateSettleCard;

import net.sf.json.JSONObject;

@Service
public class JPTopupPage extends BaseChannel implements TopupRequest {

	private static final Logger LOG = LoggerFactory.getLogger(JPTopupPage.class);

	@Autowired
	private TopupPayChannelBusiness topupPayChannelBusiness;

	@Autowired
	private BranchbankBussiness branchbankBussiness;

	@Autowired
	private Util util;

	@Value("${payment.ipAddress}")
	private String ipAddress;

	private String merchantNo = "121804261562";

	private String Url = "http://apis.miaowpay.com";

	private String key = "914a8687201f4134a426faa603c6e06b";

	private String channelProductCode = "GYPAY_FASTPAY_01";
	
	@Autowired
	private JPpageRequest jppageRequest;
	
	private String RegisterAccessTokenUrl = "https://merchant-wallet.jiedaibao.com/gateway/distributor/main/getAccessToken";
	
	private String merchantNum = "D000000003965266";

	private String Key = "96e2109082844cae924be708bf99694a0e37a565696b4f0b84f6b0cf13d29ae7";
	
	private static final Charset UTF_8 = StandardCharsets.UTF_8;
	
	@Override
	public Map<String, String> topupRequest(Map<String, Object> params) throws Exception {
		PaymentOrder paymentOrder = (PaymentOrder) params.get("paymentOrder");
		HttpServletRequest request = (HttpServletRequest) params.get("request");
		String ordercode = paymentOrder.getOrdercode();
		String amount = paymentOrder.getAmount().toString();

		Map<String, Object> map = new HashMap<String, Object>();

		String bankCard = paymentOrder.getBankcard();
		String rate = paymentOrder.getRate() + "";
		String extraFee = paymentOrder.getExtraFee() + "";
		String realAmount = paymentOrder.getRealAmount() + "";
		String userId = paymentOrder.getUserid() + "";

		Map<String, Object> queryBankCardByUserId = this.queryBankCardByUserId(userId);

		Object object3 = queryBankCardByUserId.get("result");
		JSONObject fromObject = JSONObject.fromObject(object3);

		String cardNo = fromObject.getString("cardNo");
		String cardType = fromObject.getString("cardType");
		String idCard = fromObject.getString("idcard");
		String phone = fromObject.getString("phone");
		String bankName = fromObject.getString("bankName");

		Map<String, Object> queryBankCardByCardNoAndUserId = this.queryBankCardByCardNoAndUserId(bankCard, "0", userId);
		Object object2 = queryBankCardByCardNoAndUserId.get("result");
		fromObject = JSONObject.fromObject(object2);

		String cardtype = fromObject.getString("cardType");
		String cardName = fromObject.getString("bankName");
		String securityCode = fromObject.getString("securityCode");
		String expiredTime = fromObject.getString("expiredTime");

		if (expiredTime == null || "".equals(expiredTime) || "null".equals(expiredTime)) {
			expiredTime = "";
		}

		if (securityCode == null || "".equals(securityCode) || "null".equals(securityCode)) {
			securityCode = "";
		}

		JPRegister jpRegister = topupPayChannelBusiness.getJPRegisterByIdCard(idCard);

		JPBindCard jpBindCard = topupPayChannelBusiness.getJPBindCardByBankCard(bankCard);

		if (jpRegister == null) {
			LOG.info("用户需要进件======");
			Map maps = new HashMap();
			maps.put("resp_code", "success");
			maps.put("channel_type", "jf");
			maps.put("redirect_url",
					ipAddress + "/v1.0/paymentchannel/topup/tojpbankinfo?bankName="
							+ URLEncoder.encode(bankName, "UTF-8") + "&bankNo=" + cardNo + "&bankCard=" + bankCard
							+ "&cardName=" + URLEncoder.encode(cardName, "UTF-8") + "&amount=" + amount + "&ordercode="
							+ ordercode + "&cardType=" + URLEncoder.encode(cardType, "UTF-8") + "&cardtype="
							+ URLEncoder.encode(cardtype, "UTF-8") + "&expiredTime=" + expiredTime + "&securityCode="
							+ securityCode + "&ipAddress=" + ipAddress + "&isRegister=0");
			return maps;

		} else {
			
			if(!rate.equals(jpRegister.getRate())) {
				LOG.info("需要修改费率======");
				boolean updateRate = updateRate(paymentOrder);
				if(updateRate) {
					LOG.info("修改费率成功======");
					if(!cardNo.equals(jpRegister.getBankCard())) {
						LOG.info("需要修改结算卡信息======");
						boolean updateBankCard = updateBankCard(paymentOrder);
						if(updateBankCard) {
							LOG.info("修改结算卡成功");
							if (jpBindCard == null) {
								LOG.info("用户需要绑卡======");
								Map maps = new HashMap();
								maps.put("resp_code", "success");
								maps.put("channel_type", "jf");
								maps.put("redirect_url",
										ipAddress + "/v1.0/paymentchannel/topup/tojpbankinfo?bankName="
												+ URLEncoder.encode(bankName, "UTF-8") + "&bankNo=" + cardNo + "&bankCard=" + bankCard
												+ "&cardName=" + URLEncoder.encode(cardName, "UTF-8") + "&amount=" + amount
												+ "&ordercode=" + ordercode + "&cardType=" + URLEncoder.encode(cardType, "UTF-8")
												+ "&cardtype=" + URLEncoder.encode(cardtype, "UTF-8") + "&expiredTime=" + expiredTime
												+ "&securityCode=" + securityCode + "&ipAddress=" + ipAddress + "&isRegister=1");
								return maps;

							} else {
								LOG.info("发起交易======");
								Map maps = new HashMap();
								maps.put("resp_code", "success");
								maps.put("channel_type", "jf");
								maps.put("redirect_url",
										ipAddress + "/v1.0/paymentchannel/topup/tojpbankinfo?bankName="
												+ URLEncoder.encode(bankName, "UTF-8") + "&bankNo=" + cardNo + "&bankCard=" + bankCard
												+ "&cardName=" + URLEncoder.encode(cardName, "UTF-8") + "&amount=" + amount
												+ "&ordercode=" + ordercode + "&cardType=" + URLEncoder.encode(cardType, "UTF-8")
												+ "&cardtype=" + URLEncoder.encode(cardtype, "UTF-8") + "&expiredTime=" + expiredTime
												+ "&securityCode=" + securityCode + "&ipAddress=" + ipAddress + "&isRegister=2");
								return maps;

							}
							
						}else {
							Map<String, String> maps = new HashMap<String, String>();
							LOG.info("修改结算卡失败======");
							maps.put("resp_code", "failed");
							maps.put("channel_type", "jf");
							maps.put("resp_message", "亲，修改结算卡出错啦");
							return maps;
						}
						
					}else {
						LOG.info("不需要修改结算卡信息======");
						if (jpBindCard == null) {
							LOG.info("用户需要绑卡======");
							Map maps = new HashMap();
							maps.put("resp_code", "success");
							maps.put("channel_type", "jf");
							maps.put("redirect_url",
									ipAddress + "/v1.0/paymentchannel/topup/tojpbankinfo?bankName="
											+ URLEncoder.encode(bankName, "UTF-8") + "&bankNo=" + cardNo + "&bankCard=" + bankCard
											+ "&cardName=" + URLEncoder.encode(cardName, "UTF-8") + "&amount=" + amount
											+ "&ordercode=" + ordercode + "&cardType=" + URLEncoder.encode(cardType, "UTF-8")
											+ "&cardtype=" + URLEncoder.encode(cardtype, "UTF-8") + "&expiredTime=" + expiredTime
											+ "&securityCode=" + securityCode + "&ipAddress=" + ipAddress + "&isRegister=1");
							return maps;

						} else {
							LOG.info("发起交易======");
							Map maps = new HashMap();
							maps.put("resp_code", "success");
							maps.put("channel_type", "jf");
							maps.put("redirect_url",
									ipAddress + "/v1.0/paymentchannel/topup/tojpbankinfo?bankName="
											+ URLEncoder.encode(bankName, "UTF-8") + "&bankNo=" + cardNo + "&bankCard=" + bankCard
											+ "&cardName=" + URLEncoder.encode(cardName, "UTF-8") + "&amount=" + amount
											+ "&ordercode=" + ordercode + "&cardType=" + URLEncoder.encode(cardType, "UTF-8")
											+ "&cardtype=" + URLEncoder.encode(cardtype, "UTF-8") + "&expiredTime=" + expiredTime
											+ "&securityCode=" + securityCode + "&ipAddress=" + ipAddress + "&isRegister=2");
							return maps;

						}
					}
					
					
				}else {
					Map<String, String> maps = new HashMap<String, String>();
					LOG.info("修改费率失败======");
					maps.put("resp_code", "failed");
					maps.put("channel_type", "jf");
					maps.put("resp_message", "亲，修改费率出错啦");
					
					return maps;
				}
				
			}else {
				LOG.info("不需要修改费率======");
				if(!cardNo.equals(jpRegister.getBankCard())) {
					LOG.info("需要修改结算卡信息======");
					boolean updateBankCard = updateBankCard(paymentOrder);
					if(updateBankCard) {
						LOG.info("修改结算卡成功");
						if (jpBindCard == null) {
							LOG.info("用户需要绑卡======");
							Map maps = new HashMap();
							maps.put("resp_code", "success");
							maps.put("channel_type", "jf");
							maps.put("redirect_url",
									ipAddress + "/v1.0/paymentchannel/topup/tojpbankinfo?bankName="
											+ URLEncoder.encode(bankName, "UTF-8") + "&bankNo=" + cardNo + "&bankCard=" + bankCard
											+ "&cardName=" + URLEncoder.encode(cardName, "UTF-8") + "&amount=" + amount
											+ "&ordercode=" + ordercode + "&cardType=" + URLEncoder.encode(cardType, "UTF-8")
											+ "&cardtype=" + URLEncoder.encode(cardtype, "UTF-8") + "&expiredTime=" + expiredTime
											+ "&securityCode=" + securityCode + "&ipAddress=" + ipAddress + "&isRegister=1");
							return maps;

						} else {
							LOG.info("发起交易======");
							Map maps = new HashMap();
							maps.put("resp_code", "success");
							maps.put("channel_type", "jf");
							maps.put("redirect_url",
									ipAddress + "/v1.0/paymentchannel/topup/tojpbankinfo?bankName="
											+ URLEncoder.encode(bankName, "UTF-8") + "&bankNo=" + cardNo + "&bankCard=" + bankCard
											+ "&cardName=" + URLEncoder.encode(cardName, "UTF-8") + "&amount=" + amount
											+ "&ordercode=" + ordercode + "&cardType=" + URLEncoder.encode(cardType, "UTF-8")
											+ "&cardtype=" + URLEncoder.encode(cardtype, "UTF-8") + "&expiredTime=" + expiredTime
											+ "&securityCode=" + securityCode + "&ipAddress=" + ipAddress + "&isRegister=2");
							return maps;

						}
						
					}else {
						Map<String, String> maps = new HashMap<String, String>();
						LOG.info("修改结算卡失败======");
						maps.put("resp_code", "failed");
						maps.put("channel_type", "jf");
						maps.put("resp_message", "亲，修改结算卡出错啦");
						return maps;
					}
					
				}else {
					
					if (jpBindCard == null) {
						LOG.info("用户需要绑卡======");
						Map maps = new HashMap();
						maps.put("resp_code", "success");
						maps.put("channel_type", "jf");
						maps.put("redirect_url",
								ipAddress + "/v1.0/paymentchannel/topup/tojpbankinfo?bankName="
										+ URLEncoder.encode(bankName, "UTF-8") + "&bankNo=" + cardNo + "&bankCard=" + bankCard
										+ "&cardName=" + URLEncoder.encode(cardName, "UTF-8") + "&amount=" + amount
										+ "&ordercode=" + ordercode + "&cardType=" + URLEncoder.encode(cardType, "UTF-8")
										+ "&cardtype=" + URLEncoder.encode(cardtype, "UTF-8") + "&expiredTime=" + expiredTime
										+ "&securityCode=" + securityCode + "&ipAddress=" + ipAddress + "&isRegister=1");
						return maps;

					} else {
						LOG.info("发起交易======");
						Map maps = new HashMap();
						maps.put("resp_code", "success");
						maps.put("channel_type", "jf");
						maps.put("redirect_url",
								ipAddress + "/v1.0/paymentchannel/topup/tojpbankinfo?bankName="
										+ URLEncoder.encode(bankName, "UTF-8") + "&bankNo=" + cardNo + "&bankCard=" + bankCard
										+ "&cardName=" + URLEncoder.encode(cardName, "UTF-8") + "&amount=" + amount
										+ "&ordercode=" + ordercode + "&cardType=" + URLEncoder.encode(cardType, "UTF-8")
										+ "&cardtype=" + URLEncoder.encode(cardtype, "UTF-8") + "&expiredTime=" + expiredTime
										+ "&securityCode=" + securityCode + "&ipAddress=" + ipAddress + "&isRegister=2");
						return maps;

					}
					
				}
				
			}
			
		}

		// return null;
	}

	
	// 修改费率
	public boolean updateRate(PaymentOrder paymentOrder) throws Exception {
		LOG.info("修改费率接口======");
		
		boolean istrue = false;
		String rate = paymentOrder.getRate() + "";
		String extraFee = paymentOrder.getExtraFee() + "";
		String bankCard = paymentOrder.getBankcard();
		String userId = paymentOrder.getUserid() + "";

		Map<String, Object> queryBankCardByCardNoAndUserId = this.queryBankCardByCardNoAndUserId(bankCard, "0", userId);
		
		Object object2 = queryBankCardByCardNoAndUserId.get("result");
		JSONObject fromObject = JSONObject.fromObject(object2);

		String idCard = fromObject.getString("idcard");

		JPRegister jpRegister = topupPayChannelBusiness.getJPRegisterByIdCard(idCard);
		
		String resjson = null;
		Map<String, Object> reqMap = new HashMap<>();
		UpdateRates param = new UpdateRates();
		Rates rates = new Rates();
		Rates rate1 = new Rates();
		Gson gson = new Gson();

		Map token = (Map) jppageRequest.jpGetRegisterToken(RegisterAccessTokenUrl);
		if (!"000000".equals(token.get("resp_code"))) {

			return istrue;
		}

		String accessToken = (String) token.get("result");

		rates.rate = new BigDecimal(rate).multiply(new BigDecimal("100")).setScale(2).toString();
		rates.type = "SAME_NAME_QUICK_PAY";

		rate1.rate = new BigDecimal(extraFee).multiply(new BigDecimal("100")).setScale(2).toString();
		rate1.type = "WITHDRAW_CASH";

		List<Rates> ratelist = new ArrayList<Rates>();
		ratelist.add(rates);
		ratelist.add(rate1);
		param.merchantNo = jpRegister.getMerchantCode();
		param.rates = ratelist;

		reqMap.put("accessToken", accessToken);
		reqMap.put("param", param);
		
		LOG.info("修改费率的请求报文======"+reqMap);
		
		String data = gson.toJson(reqMap);
		String url = "https://merchant-wallet.jiedaibao.com/gateway/distributor/merchant/saveRates";// 参见文档
		try {
			resjson = HttpUtils.sendPost(url, data);
		} catch (IOException e) {
			e.printStackTrace();

			return istrue;
		}

		LOG.info("请求修改费率返回的resjson======" + resjson);

		JSONObject fromObject2 = JSONObject.fromObject(resjson);

		String success = fromObject2.getString("success");
		String errorCode = fromObject2.getString("errorCode");
		String message = fromObject2.getString("message");
		
		
		if("true".equals(success) && "0".equals(errorCode)) {
			LOG.info("修改费率成功======");
			
			jpRegister.setRate(rate);
			jpRegister.setExtraFee(extraFee);
			
			topupPayChannelBusiness.createJPRegister(jpRegister);
			
			istrue = true;
			
		}else {
			LOG.info("修改费率失败======");
			
			//this.addOrderCauseOfFailure(orderCode, bizMsg);
			
		}
		
		return istrue;

	}

	
	
	//修改结算卡信息
	public boolean updateBankCard(PaymentOrder paymentOrder) throws Exception {
		LOG.info("开始进入修改默认结算卡接口======");
		
		boolean istrue = false;
		String userId = paymentOrder.getUserid() + "";

		Map<String, Object> queryBankCardByUserId = this.queryBankCardByUserId(userId);
		if (!"000000".equals(queryBankCardByUserId.get("resp_code"))) {
			return istrue;
		}

		Object object3 = queryBankCardByUserId.get("result");
		JSONObject fromObject = JSONObject.fromObject(object3);

		String idCard = fromObject.getString("idcard");
		String cardNo = fromObject.getString("cardNo");

		JPRegister jpRegister = topupPayChannelBusiness.getJPRegisterByIdCard(idCard);
		
		Map<String, String> map = (Map<String, String>) jppageRequest.jpBindSettlementCard(userId, jpRegister.getMerchantCode());
		
		LOG.info("请求绑定结算卡接口返回的map======" + map);
		if("000000".equals(map.get("resp_code"))) {
			LOG.info("绑定结算卡成功======");
			String resjson = null;
			Map<String, Object> reqMap = new HashMap<>();
			UpdateSettleCard param = new UpdateSettleCard();
			Gson gson = new Gson();

			Map token = (Map) jppageRequest.jpGetRegisterToken(RegisterAccessTokenUrl);
			if (!"000000".equals(token.get("resp_code"))) {

				return istrue;
			}

			String accessToken = (String) token.get("result");

			param.merchantNo = jpRegister.getMerchantCode();
			param.cardNo = cardNo;

			reqMap.put("accessToken", accessToken);
			reqMap.put("param", param);
			
			LOG.info("修改默认结算卡的请求报文======"+reqMap);
			
			String data = gson.toJson(reqMap);
			String url = "https://merchant-wallet.jiedaibao.com/gateway/distributor/merchant/modifyDefaultCard";// 参见文档
			try {
				resjson = HttpUtils.sendPost(url, data);
			} catch (IOException e) {
				e.printStackTrace();

				return istrue;
			}

			LOG.info("请求修改默认结算卡返回的resjson======" + resjson);

			JSONObject fromObject2 = JSONObject.fromObject(resjson);

			String success = fromObject2.getString("success");
			String errorCode = fromObject2.getString("errorCode");
			String message = fromObject2.getString("message");
			
			
			if("true".equals(success) && "0".equals(errorCode)) {
				LOG.info("修改默认结算卡成功======");
				
				jpRegister.setBankCard(cardNo);
				
				topupPayChannelBusiness.createJPRegister(jpRegister);
				
				istrue = true;
				
			}else {
				LOG.info("修改默认结算卡失败======");
				
				//this.addOrderCauseOfFailure(orderCode, bizMsg);
				
			}
			
		}
		
		return istrue;
		
	}
	
}
