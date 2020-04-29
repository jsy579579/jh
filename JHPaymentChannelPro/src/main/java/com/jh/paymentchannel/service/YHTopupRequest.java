package com.jh.paymentchannel.service;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

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
import com.jh.paymentchannel.pojo.YHQuickRegister;
import com.jh.paymentchannel.util.Util;
import com.jh.paymentchannel.util.yh.HttpClientNewUtil;
import com.jh.paymentchannel.util.yh.Signature;

import net.sf.json.JSONObject;

@Service
public class YHTopupRequest implements TopupRequest {

	private static final Logger log = LoggerFactory.getLogger(YHTopupRequest.class);

	@Autowired
	private TopupPayChannelBusiness topupPayChannelBusiness;

	@Autowired
	private BranchbankBussiness branchbankBussiness;

	@Autowired
	private Util util;

	@Value("${payment.ipAddress}")
	private String ipAddress;

	// 商户编号
	//@Value("${yh.org_no}")
	private String orgNo = "00000000523685";

	// 秘钥
	@Value("${yh.key}")
	private String Key;

	// 进件地址
	@Value("${yh.registerUrl}")
	private String registerUrl;

	// 支付申请地址
	@Value("${yh.payApplyUrl}")
	private String payApplyUrl;

	private String updateRegisterUrl = "http://www.sophiter.com/payment/synmerinfo_Settle_update.do";

	private String updateRateUrl = "http://www.sophiter.com/payment/synmerinfo_rate_update.do";

	private static final Charset UTF_8 = StandardCharsets.UTF_8;

	@Override
	public Map<String, String> topupRequest(Map<String,Object> params) throws Exception {
		Map<String, String> map = new HashMap<String, String>();
		PaymentOrder paymentOrder = (PaymentOrder) params.get("paymentOrder");
		String ordercode = paymentOrder.getOrdercode();
		String amount = paymentOrder.getAmount().toString();
		RestTemplate restTemplate = new RestTemplate();
		URI uri = util.getServiceUrl("transactionclear", "error url request!");
		String url = uri.toString() + "/v1.0/transactionclear/payment/query/ordercode";
		MultiValueMap<String, String> requestEntity = new LinkedMultiValueMap<String, String>();
		requestEntity.add("order_code", ordercode);
		String result = restTemplate.postForObject(url, requestEntity, String.class);
		log.info("RESULT================" + result);
		JSONObject jsonObject;
		JSONObject resultObj;
		try {
			jsonObject = JSONObject.fromObject(result);
			resultObj = jsonObject.getJSONObject("result");
		} catch (Exception e) {
			log.error("查询订单信息出错");
			map.put("resp_code", "failed");
			map.put("channel_type", "yh");
			map.put("resp_message", "没有该订单信息");
			return map;
		}

		String realamount = resultObj.getString("realAmount");
		String userid = resultObj.getString("userid");
		// 费率
		String rate = resultObj.getString("rate");
		String extraFee = resultObj.getString("extraFee");
		// 银行卡(支付卡)
		String bankCard = resultObj.getString("bankcard");

		restTemplate = new RestTemplate();
		uri = util.getServiceUrl("user", "error url request!");
		url = uri.toString() + "/v1.0/user/bank/default/userid";
		requestEntity = new LinkedMultiValueMap<String, String>();
		requestEntity.add("user_id", userid);
		result = restTemplate.postForObject(url, requestEntity, String.class);

		log.info("RESULT================" + result);
		try {
			jsonObject = JSONObject.fromObject(result);
			resultObj = jsonObject.getJSONObject("result");
		} catch (Exception e) {
			log.error("查询默认结算卡出错");
			map.put("resp_code", "failed");
			map.put("channel_type", "yh");
			map.put("resp_message", "查询默认结算卡有误");
			return map;
		}

		// 结算卡卡号
		String cardNo = resultObj.getString("cardNo");
		String userName = resultObj.getString("userName");
		/** 身份证号 */
		String idcard = resultObj.getString("idcard");
		String phone = resultObj.getString("phone");
		// 开户行所在省
		String provinceOfBank = resultObj.getString("province");

		log.info("provinceOfBank" + provinceOfBank);
		// 开户行所在市
		String cityOfBank = resultObj.getString("city");

		log.info("cityOfBank" + cityOfBank);
		// 银行名称
		String bankName = resultObj.getString("bankName");
		// 支行名称
		//String bankBranchName = resultObj.getString("bankBranchName");

		/*String bankBranchNo;
		try {
			bankBranchNo = branchbankBussiness.getNumByName(bankBranchName);
		} catch (Exception e1) {
			map.put("resp_code", "failed");
			map.put("channel_type", "yh");
			map.put("resp_message", "查询支行联行号有误");
			return map;
		}
		log.info("bankBranchNo" + bankBranchNo);*/

		restTemplate = new RestTemplate();
		uri = util.getServiceUrl("user", "error url request!");
		url = uri.toString() + "/v1.0/user/bank/default/cardno";
		requestEntity = new LinkedMultiValueMap<String, String>();
		requestEntity.add("cardno", bankCard);
		requestEntity.add("type", "0");
		result = restTemplate.postForObject(url, requestEntity, String.class);
		log.info("接口/v1.0/user/bank/default/cardno--RESULT================" + result);
		try {
			jsonObject = JSONObject.fromObject(result);
			resultObj = jsonObject.getJSONObject("result");
		} catch (Exception e) {
			log.error("查询银行卡信息出错");
			map.put("resp_code", "failed");
			map.put("channel_type", "yh");
			map.put("resp_message", "查询银行卡信息有误");
			return map;
		}

		String cardName = resultObj.getString("bankName");
		String cardType = resultObj.getString("cardType");
		
		String expiredTime = resultObj.getString("expiredTime");
		String securityCode = resultObj.getString("securityCode");

		String bankname = Util.queryBankNameByBranchName(bankName);
		String cardname = Util.queryBankNameByBranchName(cardName);

		// 交通银行的信用卡单笔不能超过 5000元
		/*if (cardname.contains("交通银行")) {
			String money = "5000";

			BigDecimal bigAmount = new BigDecimal(amount);
			BigDecimal bigMoney = new BigDecimal(money);

			int compareTo = bigAmount.compareTo(bigMoney);

			if (compareTo > 0) {
				map.put("resp_code", "failed");
				map.put("channel_type", "yh");
				map.put("resp_message", "亲,交通银行的信用卡单笔不能超过 5000 元哦");
				return map;
			}
		}
*/
		// 根据银行名称查询得到银行信息
		/*BranchNo findByBankName;
		try {
			findByBankName = branchbankBussiness.findByBankName(bankname);
		} catch (Exception e) {
			map.put("resp_code", "failed");
			map.put("channel_type", "yh");
			map.put("resp_message", "查询银行信息有误");
			return map;
		}
		// 银行总行联行号
		String inBankUnitNo = findByBankName.getBankNo();*/

		if (provinceOfBank == null || "".equals(provinceOfBank) || "null".equals(provinceOfBank)) {
			provinceOfBank = "";
		}

		if (cityOfBank == null || "".equals(cityOfBank) || "null".equals(cityOfBank)) {
			cityOfBank = "";
		}

		/*if (bankBranchNo == null || "".equals(bankBranchNo) || "".equals(bankBranchNo)) {
			bankBranchNo = "";
		}

		if (bankBranchName == null || "".equals(bankBranchName) || "null".equals(bankBranchName)) {
			bankBranchName = "";
		}*/

		YHQuickRegister yhQuickRegister = topupPayChannelBusiness.getYHQuickRegisterByIdCard(idcard);

		if (yhQuickRegister == null) {
			log.info("需要进件======");
			Map maps = new HashMap();
			maps.put("resp_code", "success");
			maps.put("channel_type", "jf");
			maps.put("redirect_url", ipAddress + "/v1.0/paymentchannel/topup/toyhbankinfo?bankName="
					+ URLEncoder.encode(cardName, "UTF-8") + "&bankNo=" + bankCard + "&amount=" + amount + "&ordercode="
					+ ordercode + "&cardType=" + URLEncoder.encode(cardType, "UTF-8") + "&expiredTime=" + expiredTime
					+ "&securityCode=" + securityCode + "&ipAddress=" + ipAddress + "&isRegister=0");
			return maps;

		} else {
			if (!rate.equals(yhQuickRegister.getRate()) || !extraFee.equals(yhQuickRegister.getExtraFee())) {
				log.info("需要修改费率======");
				boolean updateRate = updateRate(ordercode);
				if (updateRate) {
					if (!cardNo.equals(yhQuickRegister.getBankCard())) {
						log.info("需要更改结算信息======");
						Map maps = new HashMap();
						maps.put("resp_code", "success");
						maps.put("channel_type", "jf");
						maps.put("redirect_url", ipAddress + "/v1.0/paymentchannel/topup/toyhbankinfo?bankName="
								+ URLEncoder.encode(cardName, "UTF-8") + "&bankNo=" + bankCard + "&amount=" + amount
								+ "&ordercode=" + ordercode + "&cardType=" + URLEncoder.encode(cardType, "UTF-8")
								+ "&expiredTime=" + expiredTime + "&securityCode=" + securityCode + "&ipAddress="
								+ ipAddress + "&isRegister=2");
						return maps;

					} else {
						log.info("发起交易======");
						Map maps = new HashMap();
						maps.put("resp_code", "success");
						maps.put("channel_type", "jf");
						maps.put("redirect_url", ipAddress + "/v1.0/paymentchannel/topup/toyhbankinfo?bankName="
								+ URLEncoder.encode(cardName, "UTF-8") + "&bankNo=" + bankCard + "&amount=" + amount
								+ "&ordercode=" + ordercode + "&cardType=" + URLEncoder.encode(cardType, "UTF-8")
								+ "&expiredTime=" + expiredTime + "&securityCode=" + securityCode + "&ipAddress="
								+ ipAddress + "&isRegister=1");
						return maps;
					}

				} else {
					log.info("修改费率出错啦======");
					map.put("resp_code", "failed");
					map.put("channel_type", "jf");
					map.put("resp_message", "亲,修改费率出错啦,请稍后重试!");

					restTemplate = new RestTemplate();
					uri = util.getServiceUrl("transactionclear", "error url request!");
					url = uri.toString() + "/v1.0/transactionclear/payment/update/remark";
					requestEntity = new LinkedMultiValueMap<String, String>();
					requestEntity.add("ordercode", ordercode);
					requestEntity.add("remark", "修改费率出错");
					result = restTemplate.postForObject(url, requestEntity, String.class);

					return map;
				}

			} else {

				if (!cardNo.equals(yhQuickRegister.getBankCard())) {
					log.info("需要更改结算信息======");
					Map maps = new HashMap();
					maps.put("resp_code", "success");
					maps.put("channel_type", "jf");
					maps.put("redirect_url",
							ipAddress + "/v1.0/paymentchannel/topup/toyhbankinfo?bankName="
									+ URLEncoder.encode(cardName, "UTF-8") + "&bankNo=" + bankCard + "&amount=" + amount
									+ "&ordercode=" + ordercode + "&cardType=" + URLEncoder.encode(cardType, "UTF-8")
									+ "&expiredTime=" + expiredTime + "&securityCode=" + securityCode + "&ipAddress="
									+ ipAddress + "&isRegister=2");
					return maps;

				} else {
					log.info("发起交易======");
					Map maps = new HashMap();
					maps.put("resp_code", "success");
					maps.put("channel_type", "jf");
					maps.put("redirect_url",
							ipAddress + "/v1.0/paymentchannel/topup/toyhbankinfo?bankName="
									+ URLEncoder.encode(cardName, "UTF-8") + "&bankNo=" + bankCard + "&amount=" + amount
									+ "&ordercode=" + ordercode + "&cardType=" + URLEncoder.encode(cardType, "UTF-8")
									+ "&expiredTime=" + expiredTime + "&securityCode=" + securityCode + "&ipAddress="
									+ ipAddress + "&isRegister=1");
					return maps;
				}

			}

		}

	}

	// 修改费率
	public boolean updateRate(String ordercode) throws Exception {

		Map map = new HashMap();
		Map<String, String> maps = new HashMap<String, String>();
		boolean istrue = false;
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
		String extraFee = resultObj.getString("extraFee");

		restTemplate = new RestTemplate();
		uri = util.getServiceUrl("user", "error url request!");
		url = uri.toString() + "/v1.0/user/bank/default/userid";
		requestEntity = new LinkedMultiValueMap<String, String>();
		requestEntity.add("user_id", userid);
		result = restTemplate.postForObject(url, requestEntity, String.class);

		log.info("RESULT================" + result);
		try {
			jsonObject = JSONObject.fromObject(result);
			resultObj = jsonObject.getJSONObject("result");
		} catch (Exception e) {
			log.error("查询默认结算卡出错");
		}

		String idcard = resultObj.getString("idcard");

		YHQuickRegister yhQuickRegister = topupPayChannelBusiness.getYHQuickRegisterByIdCard(idcard);

		String extraFees = extraFee.substring(0, extraFee.indexOf("."));

		SortedMap<String, String> dto = new TreeMap<String, String>();

		dto.put("merId", yhQuickRegister.getMerchantCode());// 商户号
		dto.put("pmerNo", orgNo);// 服务商编号
		dto.put("feerate", "[{\"FEE00049\":\"" + rate + "|" + extraFees + "\"}]");// 银行手机号
		dto.put("signType", "MD5");// 加密方式

		log.info("dto======" + dto);

		String sign = Signature.createSign(dto, Key);

		dto.put("signData", sign);// 加密数据

		log.info("sign======" + sign);

		String post = HttpClientNewUtil.post(updateRateUrl, dto);

		log.info("post=====" + post);

		JSONObject fromObject = JSONObject.fromObject(post);

		String retCode = fromObject.getString("retCode");
		String retMsg = fromObject.getString("retMsg");
		
		if("1".equals(retCode)) {
			log.info("修改费率成功======");
			yhQuickRegister.setRate(rate);
			yhQuickRegister.setExtraFee(extraFee);
			
			topupPayChannelBusiness.createYHQuickRegister(yhQuickRegister);
			
			istrue = true;
		}else {
			log.info("修改费率失败======");
			restTemplate = new RestTemplate();
			uri = util.getServiceUrl("transactionclear", "error url request!");
			url = uri.toString() + "/v1.0/transactionclear/payment/update/remark";
			requestEntity = new LinkedMultiValueMap<String, String>();
			requestEntity.add("ordercode", ordercode);
			requestEntity.add("remark", "修改费率失败");
			result = restTemplate.postForObject(url, requestEntity, String.class);
		}
		
		return istrue;

	}

}
