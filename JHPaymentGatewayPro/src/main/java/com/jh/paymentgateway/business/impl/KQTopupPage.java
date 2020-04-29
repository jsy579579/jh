package com.jh.paymentgateway.business.impl;

import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.jh.paymentgateway.basechannel.BaseChannel;
import com.jh.paymentgateway.business.TopupPayChannelBusiness;
import com.jh.paymentgateway.business.TopupRequestBusiness;
import com.jh.paymentgateway.controller.KQpageRequest;
import com.jh.paymentgateway.pojo.KQBindCard;
import com.jh.paymentgateway.pojo.KQRegister;
import com.jh.paymentgateway.pojo.PaymentRequestParameter;

import cn.jh.common.utils.CommonConstants;

@Service
public class KQTopupPage extends BaseChannel implements TopupRequestBusiness {
	private static final Logger LOG = LoggerFactory.getLogger(KQTopupPage.class);
	@Autowired
	KQpageRequest kqpageRequest;

	@Autowired
	TopupPayChannelBusiness topupPayChannelBusiness;

	@Value("${payment.ipAddress}")
	private String ip;

	@Override
	public Map<String, Object> topupRequest(Map<String, Object> params) throws Exception {
		PaymentRequestParameter bean = (PaymentRequestParameter) params.get("paymentRequestParameter");
		String orderCode = bean.getOrderCode();
		String userId = bean.getUserId();
		String userName = bean.getUserName();
		String idCard = bean.getIdCard();
		String cardNo = bean.getDebitCardNo();
		String phone = bean.getDebitPhone();// 结算卡手机
		String rip = bean.getIpAddress();
		String bankCard = bean.getBankCard();
		String bankName = bean.getCreditCardBankName();
		String securityCode = bean.getSecurityCode();
		String exTime = bean.getExpiredTime();
		String expiredTime = this.expiredTimeToYYMM(exTime);
		String amount = bean.getAmount();
		String cardName = bean.getDebitBankName();
		String cardType = bean.getDebitCardCardType();
		String cardtype = bean.getCreditCardCardType();
		LOG.info("订单号：" + orderCode);
		Map<String, Object> maps = new HashMap<String, Object>();
		KQRegister kqr = topupPayChannelBusiness.getKQRegisterByIdCard(idCard);
		KQBindCard kqb = topupPayChannelBusiness.getKQBindCardByBankCard(bankCard);
		LOG.info("判断进入快钱消费=============================");
		if (kqr == null) {
			maps = (Map<String, Object>) kqpageRequest.register(orderCode, userId, idCard, phone, userName, rip);
			if ("000000".equals(maps.get("resp_code"))) {
				maps = (Map<String, Object>) kqpageRequest.fileUpload1(phone, orderCode, rip, idCard);
				if ("000000".equals(maps.get("resp_code"))) {
					LOG.info("第一张图片上传成功");
					maps = (Map<String, Object>) kqpageRequest.fileUpload2(phone, orderCode, rip, idCard);
					if ("000000".equals(maps.get("resp_code"))) {
						LOG.info("第二张图片上传成功");
						maps = (Map<String, Object>) kqpageRequest.fileUpload3(phone, orderCode, rip, idCard);
						if ("000000".equals(maps.get("resp_code"))) {
							LOG.info("第三张图片上传成功");
							maps = (Map<String, Object>) kqpageRequest.signContract(orderCode, userId, rip, idCard);
							if ("000000".equals(maps.get("resp_code"))) {
								LOG.info("合同签约成功");
								maps = (Map<String, Object>) kqpageRequest.bindThreeElements(orderCode, idCard, userId,
										cardNo, phone, userName, rip);
								if ("000000".equals(maps.get("resp_code"))) {
									LOG.info("鉴权结算卡成功");
									LOG.info("==========================开始进入绑卡交易");
									maps.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
									maps.put(CommonConstants.RESP_MESSAGE, "成功");
									maps.put(CommonConstants.RESULT, ip
											+ "/v1.0/paymentgateway/quick/kq/jump-DebitCard-view?bankName="
											+ URLEncoder.encode(cardName, "UTF-8") + "&bankNo=" + cardNo + "&bankCard="
											+ bankCard + "&cardName=" + URLEncoder.encode(bankName, "UTF-8")
											+ "&amount=" + amount + "&ordercode=" + orderCode + "&cardType="
											+ URLEncoder.encode(cardType, "UTF-8") + "&cardtype="
											+ URLEncoder.encode(cardtype, "UTF-8") + "&expiredTime=" + expiredTime
											+ "&securityCode=" + securityCode + "&ipAddress=" + ip + "&isRegister=1");
									return maps;
								} else {
									return maps;
								}

							} else {
								return maps;
							}
						} else {
							return maps;
						}
					} else {
						return maps;
					}
				} else {
					return maps;
				}

			} else {
				return maps;
			}
		}
		if (null == kqr.getFssId1()) {
			maps = (Map<String, Object>) kqpageRequest.fileUpload1(phone, orderCode, rip, idCard);
			if ("000000".equals(maps.get("resp_code"))) {
				LOG.info("第一张图片上传成功");
			} else {
				return maps;
			}
		}
		if (null == kqr.getFssId2()) {
			maps = (Map<String, Object>) kqpageRequest.fileUpload2(phone, orderCode, rip, idCard);
			if ("000000".equals(maps.get("resp_code"))) {
				LOG.info("第二张图片上传成功");
			} else {
				return maps;
			}

		}
		if (null == kqr.getFssId3()) {
			maps = (Map<String, Object>) kqpageRequest.fileUpload3(phone, orderCode, rip, idCard);
			if ("000000".equals(maps.get("resp_code"))) {
				LOG.info("第三张图片上传成功");
			} else {
				return maps;
			}

		}
		if ("0".equals(kqr.getSignStatus())) {
			maps = (Map<String, Object>) kqpageRequest.signContract(orderCode, userId, rip, idCard);
			if ("000000".equals(maps.get("resp_code"))) {
				LOG.info("合同签约成功");
			} else {
				return maps;
			}
		}

		if (null == kqr.getBankCard()) {
			maps = (Map<String, Object>) kqpageRequest.bindThreeElements(orderCode, idCard, userId, cardNo, phone,
					userName, rip);
			if ("000000".equals(maps.get("resp_code"))) {
				LOG.info("鉴权结算卡成功");
			} else {
				return maps;
			}
		} else if (!cardNo.equals(kqr.getBankCard())) {
			maps = (Map<String, Object>) kqpageRequest.bindThreeElements(orderCode, idCard, userId, cardNo, phone,
					userName, rip);
			if ("000000".equals(maps.get("resp_code"))) {
				LOG.info("重新鉴权结算卡成功");
			} else {
				return maps;
			}
		}
		if (kqb == null) {
			maps.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
			maps.put(CommonConstants.RESP_MESSAGE, "成功");
			maps.put(CommonConstants.RESULT,
					ip + "/v1.0/paymentgateway/quick/kq/jump-DebitCard-view?bankName="
							+ URLEncoder.encode(cardName, "UTF-8") + "&bankNo=" + cardNo + "&bankCard=" + bankCard
							+ "&cardName=" + URLEncoder.encode(bankName, "UTF-8") + "&amount=" + amount + "&ordercode="
							+ orderCode + "&cardType=" + URLEncoder.encode(cardType, "UTF-8") + "&cardtype="
							+ URLEncoder.encode(cardtype, "UTF-8") + "&expiredTime=" + expiredTime + "&securityCode="
							+ securityCode + "&ipAddress=" + ip + "&isRegister=1");
			return maps;
		} else if (!"1".equals(kqb.getStatus())) {
			maps.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
			maps.put(CommonConstants.RESP_MESSAGE, "成功");
			maps.put(CommonConstants.RESULT,
					ip + "/v1.0/paymentgateway/quick/kq/jump-DebitCard-view?bankName="
							+ URLEncoder.encode(cardName, "UTF-8") + "&bankNo=" + cardNo + "&bankCard=" + bankCard
							+ "&cardName=" + URLEncoder.encode(bankName, "UTF-8") + "&amount=" + amount + "&ordercode="
							+ orderCode + "&cardType=" + URLEncoder.encode(cardType, "UTF-8") + "&cardtype="
							+ URLEncoder.encode(cardtype, "UTF-8") + "&expiredTime=" + expiredTime + "&securityCode="
							+ securityCode + "&ipAddress=" + ip + "&isRegister=1");
			return maps;
		} else {
			maps.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
			maps.put(CommonConstants.RESP_MESSAGE, "成功");
			maps.put(CommonConstants.RESULT,
					ip + "/v1.0/paymentgateway/quick/kq/jump-DebitCard-view?bankName="
							+ URLEncoder.encode(cardName, "UTF-8") + "&bankNo=" + cardNo + "&bankCard=" + bankCard
							+ "&cardName=" + URLEncoder.encode(bankName, "UTF-8") + "&amount=" + amount + "&ordercode="
							+ orderCode + "&cardType=" + URLEncoder.encode(cardType, "UTF-8") + "&cardtype="
							+ URLEncoder.encode(cardtype, "UTF-8") + "&expiredTime=" + expiredTime + "&securityCode="
							+ securityCode + "&ipAddress=" + ip + "&isRegister=2");
			return maps;

		}
	}

}
