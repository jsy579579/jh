package com.jh.paymentchannel.service;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
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

import com.alibaba.fastjson.JSON;
import com.jh.paymentchannel.basechannel.BaseChannel;
import com.jh.paymentchannel.business.BranchbankBussiness;
import com.jh.paymentchannel.business.TopupPayChannelBusiness;
import com.jh.paymentchannel.pojo.CJBindCard;
import com.jh.paymentchannel.pojo.CJHKRegister;
import com.jh.paymentchannel.pojo.CJRegister;
import com.jh.paymentchannel.pojo.PaymentOrder;
import com.jh.paymentchannel.util.Util;
import com.jh.paymentchannel.util.cjhk.ApiClient;
import com.jh.paymentchannel.util.cjhk.ApiRequest;
import com.jh.paymentchannel.util.cjhk.ApiResponse;
import com.jh.paymentchannel.util.cjhk.EncryptTypeEnum;

import net.sf.json.JSONObject;

@Service
public class CJTopupPage extends BaseChannel implements TopupRequest {

	private static final Logger LOG = LoggerFactory.getLogger(CJTopupPage.class);

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

	private String channelProductCode = "GHTPAY_01";
	
	private static final Charset UTF_8 = StandardCharsets.UTF_8;
	
	@Override
	public Map<String, String> topupRequest(Map<String, Object> params) throws Exception {
		PaymentOrder paymentOrder = (PaymentOrder) params.get("paymentOrder");
		HttpServletRequest request = (HttpServletRequest) params.get("request");
		String ordercode = paymentOrder.getOrdercode();
		String amount = paymentOrder.getAmount().toString();

		Map<String, Object> map = new HashMap<String, Object>();

		Map<String, Object> queryOrdercode = this.queryOrdercode(ordercode);
		Object object = queryOrdercode.get("result");
		JSONObject fromObject = JSONObject.fromObject(object);
		JSONObject resultObj = fromObject.getJSONObject("result");

		String bankCard = resultObj.getString("bankcard");
		String rate = resultObj.getString("rate");
		String extraFee = resultObj.getString("extraFee");
		String realAmount = resultObj.getString("realAmount");
		String userId = resultObj.getString("userid");

		Map<String, Object> queryBankCardByUserId = this.queryBankCardByUserId(userId);

		Object object3 = queryBankCardByUserId.get("result");
		fromObject = JSONObject.fromObject(object3);

		String cardNo = fromObject.getString("cardNo");
		String cardType = fromObject.getString("cardType");
		String idCard = fromObject.getString("idcard");
		String phone = fromObject.getString("phone");
		String bankName = fromObject.getString("bankName");

		Map<String, Object> queryBankCardByCardNo = this.queryBankCardByCardNo(bankCard, "0");

		Object object2 = queryBankCardByCardNo.get("result");
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

		CJRegister cjRegister = topupPayChannelBusiness.getCJRegisterByIdCard(idCard);

		CJBindCard cjBindCard = topupPayChannelBusiness.getCJBindCardByBankCard(bankCard);

		if (cjRegister == null) {
			LOG.info("用户需要进件======");
			Map maps = new HashMap();
			maps.put("resp_code", "success");
			maps.put("channel_type", "jf");
			maps.put("redirect_url",
					ipAddress + "/v1.0/paymentchannel/topup/tocjbankinfo?bankName="
							+ URLEncoder.encode(bankName, "UTF-8") + "&bankNo=" + cardNo + "&bankCard=" + bankCard
							+ "&cardName=" + URLEncoder.encode(cardName, "UTF-8") + "&amount=" + amount + "&ordercode="
							+ ordercode + "&cardType=" + URLEncoder.encode(cardType, "UTF-8") + "&cardtype="
							+ URLEncoder.encode(cardtype, "UTF-8") + "&expiredTime=" + expiredTime + "&securityCode="
							+ securityCode + "&ipAddress=" + ipAddress + "&isRegister=0");
			return maps;

		} else {
			
			if(!rate.equals(cjRegister.getRate())) {
				LOG.info("需要修改费率======");
				boolean updateRate = updateRate(ordercode);
				if(updateRate) {
					LOG.info("修改费率成功======");
					if(!cardNo.equals(cjRegister.getBankCard())) {
						LOG.info("需要修改结算卡信息======");
						boolean updateBankCard = updateBankCard(ordercode);
						if(updateBankCard) {
							LOG.info("修改结算卡成功");
							if (cjBindCard == null) {
								LOG.info("用户需要绑卡======");
								Map maps = new HashMap();
								maps.put("resp_code", "success");
								maps.put("channel_type", "jf");
								maps.put("redirect_url",
										ipAddress + "/v1.0/paymentchannel/topup/tocjbankinfo?bankName="
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
										ipAddress + "/v1.0/paymentchannel/topup/tocjbankinfo?bankName="
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
						if (cjBindCard == null) {
							LOG.info("用户需要绑卡======");
							Map maps = new HashMap();
							maps.put("resp_code", "success");
							maps.put("channel_type", "jf");
							maps.put("redirect_url",
									ipAddress + "/v1.0/paymentchannel/topup/tocjbankinfo?bankName="
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
									ipAddress + "/v1.0/paymentchannel/topup/tocjbankinfo?bankName="
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
				if(!cardNo.equals(cjRegister.getBankCard())) {
					LOG.info("需要修改结算卡信息======");
					boolean updateBankCard = updateBankCard(ordercode);
					if(updateBankCard) {
						LOG.info("修改结算卡成功");
						if (cjBindCard == null) {
							LOG.info("用户需要绑卡======");
							Map maps = new HashMap();
							maps.put("resp_code", "success");
							maps.put("channel_type", "jf");
							maps.put("redirect_url",
									ipAddress + "/v1.0/paymentchannel/topup/tocjbankinfo?bankName="
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
									ipAddress + "/v1.0/paymentchannel/topup/tocjbankinfo?bankName="
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
					
					if (cjBindCard == null) {
						LOG.info("用户需要绑卡======");
						Map maps = new HashMap();
						maps.put("resp_code", "success");
						maps.put("channel_type", "jf");
						maps.put("redirect_url",
								ipAddress + "/v1.0/paymentchannel/topup/tocjbankinfo?bankName="
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
								ipAddress + "/v1.0/paymentchannel/topup/tocjbankinfo?bankName="
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
	public boolean updateRate(String orderCode) {
		LOG.info("修改费率接口======");
		
		boolean istrue = false;
		Map<String, Object> queryOrdercode = this.queryOrdercode(orderCode);
		Object object = queryOrdercode.get("result");
		JSONObject fromObject = JSONObject.fromObject(object);
		JSONObject resultObj = fromObject.getJSONObject("result");

		// 费率
		String rate = resultObj.getString("rate");
		// 额外费率
		String extraFee = resultObj.getString("extraFee");
		// 充值卡卡号
		String bankCard = resultObj.getString("bankcard");

		Map<String, Object> queryBankCardByCardNo = this.queryBankCardByCardNo(bankCard, "0");
		Object object2 = queryBankCardByCardNo.get("result");
		fromObject = JSONObject.fromObject(object2);

		String idcard = fromObject.getString("idcard");

		CJRegister cjRegister = topupPayChannelBusiness.getCJRegisterByIdCard(idcard);
		
		ApiRequest apiRequest = new ApiRequest(merchantNo, key);
		apiRequest.setSupportSign(false);
		apiRequest.setEncryptType(EncryptTypeEnum.AES);
		apiRequest.addParam("merchantNo", cjRegister.getMerchantCode());
		apiRequest.addParam("channelProductCode", channelProductCode);
		apiRequest.addParam("cardType", "CREDIT");
		apiRequest.addParam("bizType", "TRADE");
		apiRequest.addParam("feeValue ", rate);
		apiRequest.addParam("capAmount", "999999");
		
		LOG.info("修改费率的请求报文 apiRequest======"+apiRequest.toString());
		
		ApiResponse post = ApiClient.post(Url+"/rest/v1.0/paybar/modifyMerchantFeeInfo", apiRequest);
		
		LOG.info("请求返回的 post======" + JSON.toJSONString(post));
		
		JSONObject fromObject2 = JSONObject.fromObject(post);

		String state = fromObject2.getString("state");
		LOG.info("state======" + state);
		JSONObject resultMap = fromObject2.getJSONObject("resultMap");

		JSONObject fromObject3 = JSONObject.fromObject(resultMap);
		LOG.info("fromObject3======" + fromObject3);
		
		String bizCode = fromObject3.getString("bizCode");
		String bizMsg = fromObject3.getString("bizMsg");
		String code1 = fromObject3.getString("code");
		
		if("200".equals(code1) && "1".equals(bizCode)) {
			LOG.info("修改费率成功======");
			
			cjRegister.setRate(rate);
			
			topupPayChannelBusiness.createCJRegister(cjRegister);
			
			istrue = true;
			
		}else {
			LOG.info("修改费率失败======");
			
			this.addOrderCauseOfFailure(orderCode, bizMsg);
			
		}
		
		return istrue;

	}

	
	
	//修改结算卡信息
	public boolean updateBankCard(String orderCode) {
		LOG.info("修改结算卡接口======");
		
		boolean istrue = false;
		Map<String, Object> queryOrdercode = this.queryOrdercode(orderCode);
		Object object = queryOrdercode.get("result");
		JSONObject fromObject = JSONObject.fromObject(object);
		JSONObject resultObj = fromObject.getJSONObject("result");

		String userId = resultObj.getString("userid");
		// 充值卡卡号
		String bankCard = resultObj.getString("bankcard");
		String mobile = resultObj.getString("phone");

		Map<String, Object> queryBankCardByCardNo = this.queryBankCardByCardNo(bankCard, "0");
		Object object2 = queryBankCardByCardNo.get("result");
		fromObject = JSONObject.fromObject(object2);

		String idcard = fromObject.getString("idcard");

		Map<String, Object> queryBankCardByUserId = this.queryBankCardByUserId(userId);

		Object object3 = queryBankCardByUserId.get("result");
		fromObject = JSONObject.fromObject(object3);

		String cardNo = fromObject.getString("cardNo");
		String userName1 = fromObject.getString("userName");
		String idcard1 = fromObject.getString("idcard");
		String phone1 = fromObject.getString("phone");
		String bankName1 = fromObject.getString("bankName");
		
		
		Map<String, Object> bankUnitNo = this.getBankUnitNo(Util.queryBankNameByBranchName(bankName1));

		String inBankUnitNo = (String) bankUnitNo.get("result");

		Map<String, Object> bankCodeByBankName = this.getBankCodeByBankName(Util.queryBankNameByBranchName(bankName1));

		String code = (String) bankCodeByBankName.get("result");
		
		CJRegister cjRegister = topupPayChannelBusiness.getCJRegisterByIdCard(idcard);
		
		ApiRequest apiRequest = new ApiRequest(merchantNo, key);
		apiRequest.setSupportSign(false);
		apiRequest.setEncryptType(EncryptTypeEnum.AES);
		apiRequest.addParam("merchantNo", cjRegister.getMerchantCode());
		apiRequest.addParam("bindMobile", mobile);
		apiRequest.addParam("settleBankAccountNo", cardNo);
		apiRequest.addParam("settleBankAccountName", userName1);
		apiRequest.addParam("settleBankName", bankName1);
		apiRequest.addParam("settleBankSubName", "上海宝山支行");
		apiRequest.addParam("settleBankAbbr", code);
		apiRequest.addParam("settleBankChannelNo", inBankUnitNo);
		apiRequest.addParam("settleBankCardProvince", "上海");
		apiRequest.addParam("settleBankCardCity", "上海");
		
		LOG.info("修改结算卡信息的请求报文 apiRequest======"+apiRequest.toString());
		
		ApiResponse post = ApiClient.post(Url+"/rest/v1.0/paybar/modifyMerchantSettlementInfo", apiRequest);
		
		LOG.info("请求返回的 post======" + JSON.toJSONString(post));
		
		JSONObject fromObject2 = JSONObject.fromObject(post);

		String state = fromObject2.getString("state");
		LOG.info("state======" + state);
		JSONObject resultMap = fromObject2.getJSONObject("resultMap");

		JSONObject fromObject3 = JSONObject.fromObject(resultMap);
		LOG.info("fromObject3======" + fromObject3);
		
		String bizCode = fromObject3.getString("bizCode");
		String bizMsg = fromObject3.getString("bizMsg");
		String code1 = fromObject3.getString("code");
		
		if("200".equals(code1) && "1".equals(bizCode)) {
			LOG.info("修改结算卡成功======");
			
			cjRegister.setBankCard(cardNo);;
			
			topupPayChannelBusiness.createCJRegister(cjRegister);
			
			istrue = true;
			
		}else {
			LOG.info("修改结算卡失败======");
			
			this.addOrderCauseOfFailure(orderCode, bizMsg);
			
		}
		
		return istrue;
		
	}
	
}
