package com.jh.paymentgateway.business.impl;

import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import com.jh.paymentgateway.basechannel.BaseChannel;
import com.jh.paymentgateway.business.TopupRequestBusiness;
import com.jh.paymentgateway.config.RedisUtil;
import com.jh.paymentgateway.pojo.PaymentRequestParameter;
import com.jh.paymentgateway.pojo.YHQuickRegister;
import com.jh.paymentgateway.util.yh.HttpClientNewUtil;
import com.jh.paymentgateway.util.yh.Signature;

import cn.jh.common.utils.CommonConstants;
import net.sf.json.JSONObject;

@Service
public class YHTopupPage extends BaseChannel implements TopupRequestBusiness {
	private static final Logger LOG = LoggerFactory.getLogger(YHTopupPage.class);

	private String updateRateUrl = "http://www.sophiter.com/payment/synmerinfo_rate_update.do";
	private String orgNo = "00000000523685";
	private String Key = "bywezRZemVcR";

	@Autowired
	TopupPayChannelBusinessImpl topupPayChannelBusinessImpl;

	@Value("${payment.ipAddress}")
	private String ipAddress;

	@Autowired
	RedisUtil redisUtil;

	@Override
	public Map<String, Object> topupRequest(Map<String, Object> params) throws Exception {
		Map<String, Object> maps = new HashMap<>();
		PaymentRequestParameter bean = (PaymentRequestParameter) params.get("paymentRequestParameter");
		String orderCode = bean.getOrderCode();
		String idCard = bean.getIdCard();
		String cardName = bean.getDebitBankName();
		String cardNo = bean.getDebitCardNo();
		String amount = bean.getAmount();
		String cardType = bean.getDebitCardCardType();
		String securityCode = bean.getSecurityCode();
		String exTime = bean.getExpiredTime();
		String rate = bean.getRate();
		String extraFee = bean.getExtraFee();
		String rip = bean.getIpAddress();
		String expiredTime = this.expiredTimeToMMYY(exTime);
		LOG.info("转换MMYY：" + expiredTime);

		YHQuickRegister yhQuickRegister = topupPayChannelBusinessImpl.getYHQuickRegisterByIdCard(idCard);
		if (yhQuickRegister == null) {
			LOG.info("需要进件======");
			maps.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
			maps.put(CommonConstants.RESP_MESSAGE, "成功");
			maps.put(CommonConstants.RESULT, ipAddress + "/v1.0/paymentgateway/topup/yh/toyhbankinfo?bankName="
					+ URLEncoder.encode(cardName, "UTF-8") + "&bankNo=" + cardNo + "&amount=" + amount + "&ordercode="
					+ orderCode + "&cardType=" + URLEncoder.encode(cardType, "UTF-8") + "&expiredTime=" + expiredTime
					+ "&securityCode=" + securityCode + "&ipAddress=" + ipAddress + "&isRegister=0");
			return maps;

		} else {
			if (!rate.equals(yhQuickRegister.getRate()) || !extraFee.equals(yhQuickRegister.getExtraFee())) {
				LOG.info("需要修改费率======");
				boolean updateRate = updateRate(orderCode);
				if (updateRate) {
					if (!cardNo.equals(yhQuickRegister.getBankCard())) {
						LOG.info("需要更改结算信息======");
						maps.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
						maps.put(CommonConstants.RESP_MESSAGE, "成功");
						maps.put(CommonConstants.RESULT,
								ipAddress + "/v1.0/paymentgateway/topup/yh/toyhbankinfo?bankName="
										+ URLEncoder.encode(cardName, "UTF-8") + "&bankNo=" + cardNo + "&amount="
										+ amount + "&ordercode=" + orderCode + "&cardType="
										+ URLEncoder.encode(cardType, "UTF-8") + "&expiredTime=" + expiredTime
										+ "&securityCode=" + securityCode + "&ipAddress=" + ipAddress
										+ "&isRegister=2");
						return maps;

					} else {
						LOG.info("发起交易======");
						maps.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
						maps.put(CommonConstants.RESP_MESSAGE, "成功");
						maps.put(CommonConstants.RESULT,
								ipAddress + "/v1.0/paymentgateway/topup/yh/toyhbankinfo?bankName="
										+ URLEncoder.encode(cardName, "UTF-8") + "&bankNo=" + cardNo + "&amount="
										+ amount + "&ordercode=" + orderCode + "&cardType="
										+ URLEncoder.encode(cardType, "UTF-8") + "&expiredTime=" + expiredTime
										+ "&securityCode=" + securityCode + "&ipAddress=" + ipAddress
										+ "&isRegister=1");
						return maps;
					}

				} else {
					LOG.info("修改费率出错啦======");
					this.addOrderCauseOfFailure(orderCode, "修改费率出错", rip);
					maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
					maps.put(CommonConstants.RESP_MESSAGE, "亲,修改费率出错啦,请稍后重试");

					return maps;
				}

			} else {

				if (!cardNo.equals(yhQuickRegister.getBankCard())) {
					LOG.info("需要更改结算信息======");
					maps.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
					maps.put(CommonConstants.RESP_MESSAGE, "成功");
					maps.put(CommonConstants.RESULT,
							ipAddress + "/v1.0/paymentgateway/topup/yh/toyhbankinfo?bankName="
									+ URLEncoder.encode(cardName, "UTF-8") + "&bankNo=" + cardNo + "&amount=" + amount
									+ "&ordercode=" + orderCode + "&cardType=" + URLEncoder.encode(cardType, "UTF-8")
									+ "&expiredTime=" + expiredTime + "&securityCode=" + securityCode + "&ipAddress="
									+ ipAddress + "&isRegister=2");
					return maps;

				} else {
					LOG.info("发起交易======");
					maps.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
					maps.put(CommonConstants.RESP_MESSAGE, "成功");
					maps.put(CommonConstants.RESULT,
							ipAddress + "/v1.0/paymentgateway/topup/yh/toyhbankinfo?bankName="
									+ URLEncoder.encode(cardName, "UTF-8") + "&bankNo=" + cardNo + "&amount=" + amount
									+ "&ordercode=" + orderCode + "&cardType=" + URLEncoder.encode(cardType, "UTF-8")
									+ "&expiredTime=" + expiredTime + "&securityCode=" + securityCode + "&ipAddress="
									+ ipAddress + "&isRegister=1");
					return maps;
				}

			}

		}
	}

	// 修改费率
	public boolean updateRate(String orderCode) throws Exception {
		boolean istrue = false;
		PaymentRequestParameter prp = redisUtil.getPaymentRequestParameter(orderCode);
		String rate = prp.getRate();
		String extraFee = prp.getExtraFee();
		String rip = prp.getIpAddress();
		String idCard = prp.getIdCard();

		YHQuickRegister yhQuickRegister = topupPayChannelBusinessImpl.getYHQuickRegisterByIdCard(idCard);

		SortedMap<String, String> dto = new TreeMap<String, String>();

		dto.put("merId", yhQuickRegister.getMerchantCode());// 商户号
		dto.put("pmerNo", orgNo);// 服务商编号
		dto.put("feerate", "[{\"FEE00049\":\"" + rate + "|" + extraFee + "\"}]");// 银行手机号
		dto.put("signType", "MD5");// 加密方式

		LOG.info("dto======" + dto);

		String sign = Signature.createSign(dto, Key);

		dto.put("signData", sign);// 加密数据

		LOG.info("sign======" + sign);

		String post = HttpClientNewUtil.post(updateRateUrl, dto);

		LOG.info("post=====" + post);

		JSONObject fromObject = JSONObject.fromObject(post);

		String retCode = fromObject.getString("retCode");
		String retMsg = fromObject.getString("retMsg");

		if ("1".equals(retCode)) {
			LOG.info("修改费率成功======");
			yhQuickRegister.setRate(rate);
			yhQuickRegister.setExtraFee(extraFee);

			topupPayChannelBusinessImpl.createYHQuickRegister(yhQuickRegister);

			istrue = true;
		} else {
			LOG.info("修改费率失败======");
			this.addOrderCauseOfFailure(orderCode, "修改费率失败", rip);
		}

		return istrue;

	}
}
