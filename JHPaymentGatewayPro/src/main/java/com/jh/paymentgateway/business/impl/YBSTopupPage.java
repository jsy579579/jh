package com.jh.paymentgateway.business.impl;

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
import com.jh.paymentgateway.config.RedisUtil;
import com.jh.paymentgateway.controller.YBSpageRequest;
import com.jh.paymentgateway.pojo.PaymentRequestParameter;
import com.jh.paymentgateway.pojo.YBSRegister;

import cn.jh.common.utils.CommonConstants;

@Service
public class YBSTopupPage extends BaseChannel implements TopupRequestBusiness {

	private static final Logger LOG = LoggerFactory.getLogger(YBSTopupPage.class);
	@Autowired
	YBSpageRequest ybspageRequest;

	@Autowired
	RedisUtil redisUtil;

	@Value("${payment.ipAddress}")
	private String ip;

	@Autowired
	TopupPayChannelBusiness topupPayChannelBusiness;

	@Override
	public Map<String, Object> topupRequest(Map<String, Object> params) throws Exception {
		PaymentRequestParameter bean = (PaymentRequestParameter) params.get("paymentRequestParameter");

		String orderCode = bean.getOrderCode();
		LOG.info("订单号：" + orderCode);
		PaymentRequestParameter prp = redisUtil.getPaymentRequestParameter(orderCode);
		String idCard = prp.getIdCard();
		String userName = prp.getUserName();// 用户名
		String cardNo = prp.getDebitCardNo();// 到账卡
		String phone = prp.getDebitPhone();// 到账卡预留手机号
		String rip = prp.getIpAddress();
		String rate = prp.getRate();
		String amount = prp.getAmount();
		String bankCard = prp.getBankCard();
		String extraFee = prp.getExtraFee();
		String bankName = prp.getDebitBankName();// 到账卡银行名称

		YBSRegister ybsInfo = topupPayChannelBusiness.getYBSRegisterByidCard(idCard);
		Map<String, Object> maps = new HashMap<String, Object>();

		LOG.info("根据判断进入消费任务======");
		if (ybsInfo == null) {
			LOG.info("=======================第一次实名认证：" + orderCode);
			maps.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
			maps.put(CommonConstants.RESP_MESSAGE, "实名认证");
			maps.put(CommonConstants.RESULT,
					ip + "/v1.0/paymentgateway/quick/jump/fill?ip=" + ip + "&key=" + "dcddf093acb42eed08d077a3d433b03d"
							+ "&mainCustomerNumber=1514312323" + "&orderCode=" + orderCode);
			return maps;
		}
		if ("0".equals(ybsInfo.getStatus())) {
			LOG.info("=======================第二次实名认证补充：" + orderCode);
			maps.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
			maps.put(CommonConstants.RESP_MESSAGE, "实名认证补充");
			maps.put(CommonConstants.RESULT, ip + "/v1.0/paymentgateway/quick/jump/fill-b?ip=" + ip + "&key="
					+ "dcddf093acb42eed08d077a3d433b03d" + "&mainCustomerNumber=1514312323");
			return maps;
		} else if ("3".equals(ybsInfo.getStatus())) {
			maps = (Map<String, Object>) ybspageRequest.queryMerchant(idCard, phone);
			if ("000000".equals(maps.get("resp_code"))) {
				LOG.info("=======================实名审核中变更成功-1：" + orderCode);
			} else {
				return maps;
			}
		}
		if (ybsInfo.getRate() == null | !rate.equals(ybsInfo.getRate())) {
			maps = (Map<String, Object>) ybspageRequest.YBSsetRate(orderCode, rate, extraFee, rip, idCard);
			if ("999999".equals(maps.get("resp_code"))) {
				return maps;

			}
		}
		if (ybsInfo.getExtraFee() == null | !extraFee.equals(ybsInfo.getExtraFee())) {
			maps = (Map<String, Object>) ybspageRequest.YBSsetRate(orderCode, rate, extraFee, rip, idCard);
			if ("999999".equals(maps.get("resp_code"))) {
				return maps;

			}
		}
		if (!cardNo.equals(ybsInfo.getBankCard())) {
			maps = (Map<String, Object>) ybspageRequest.setMerchant(orderCode, bankName, cardNo, rip, idCard);
			if ("999999".equals(maps.get("resp_code"))) {
				return maps;

			}
		}

		maps = (Map<String, Object>) ybspageRequest.pay(orderCode, bankCard, rip, idCard, amount, phone);
		return maps;

	}

}
