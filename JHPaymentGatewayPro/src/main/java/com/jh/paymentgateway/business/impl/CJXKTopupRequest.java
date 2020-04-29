package com.jh.paymentgateway.business.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import com.jh.paymentgateway.basechannel.BaseChannel;
import com.jh.paymentgateway.business.TopupPayChannelBusiness;
import com.jh.paymentgateway.business.TopupRequestBusiness;
import com.jh.paymentgateway.controller.CJHKXpageRequest;
import com.jh.paymentgateway.pojo.CJHKRegister;
import com.jh.paymentgateway.pojo.CJXChannelCode;
import com.jh.paymentgateway.pojo.PaymentRequestParameter;
import com.jh.paymentgateway.util.Util;

import cn.jh.common.utils.CommonConstants;

@Service
public class CJXKTopupRequest extends BaseChannel implements TopupRequestBusiness {

	private static final Logger LOG = LoggerFactory.getLogger(CJXKTopupRequest.class);

	@Autowired
	private HttpServletRequest request;

	@Autowired
	private CJHKXpageRequest cjhkxpageRequest;

	@Value("${payment.ipAddress}")
	private String ipAddress;

	@Autowired
	private TopupPayChannelBusiness topupPayChannelBusiness;

	@Override
	public Map<String, Object> topupRequest(Map<String, Object> params) throws Exception {
		PaymentRequestParameter bean = (PaymentRequestParameter) params.get("paymentRequestParameter");

		Map<String, Object> maps = new HashMap<String, Object>();
		String orderCode = bean.getOrderCode();
		String orderType = bean.getOrderType();
		String bankName1 = bean.getCreditCardBankName();
		String rate = bean.getRate();
		String idCard = bean.getIdCard();
		String extraFee = bean.getExtraFee();
		String extra = bean.getExtra();// 消费计划|福建省-泉州市-350500
		String bankName = Util.queryBankNameByBranchName(bankName1);
		CJXChannelCode cjxChannelCode = topupPayChannelBusiness.getCJXChannelCode(bankName);
		if (cjxChannelCode != null) {
			String channelCode = cjxChannelCode.getChannelCode();
			CJHKRegister cjhkRegister = topupPayChannelBusiness.getCJHKRegisterByIdCard(idCard);
			LOG.info("判断进入消费任务==============");
			if ("10".equals(orderType)) {
				String cityName = null;
				String provinceName = null;
				if (!extra.contains("-")) {
					cityName = "上海市";
					provinceName = "上海市";
				} else {
					provinceName = extra.substring(extra.indexOf("|") + 1, extra.indexOf("-"));
					cityName = extra.substring(extra.indexOf("-") + 1, extra.lastIndexOf("-"));
				}
				if (!rate.equals(cjhkRegister.getRate())) {

					ArrayList<String> arrayList = new ArrayList<String>();
					arrayList.add("101001");
					arrayList.add("101002");
					arrayList.add("110002");
					arrayList.add("110003");
					arrayList.add("1000");
					for (String channelCode1 : arrayList) {
						maps = (Map<String, Object>) cjhkxpageRequest.cjhkmerChange1(idCard, bankName, rate,
								channelCode1);
						LOG.info("修改费率时改变所有费率========================" + channelCode1 + "========费率" + rate);
					}
					Object respCode = maps.get("resp_code");
					LOG.info("修改费率返回的respCode=====" + respCode);
					if ("000000".equals(respCode)) {
						if (!extraFee.equals(cjhkRegister.getExtraFee())) {

							ArrayList<String> arrayList1 = new ArrayList<String>();
							arrayList1.add("101001");
							arrayList1.add("101002");
							arrayList1.add("110002");
							arrayList1.add("110003");
							arrayList1.add("1000");
							for (String channelCode2 : arrayList1) {
								LOG.info("为新增的渠道号开通费率========================" + channelCode2);
								maps = (Map<String, Object>) cjhkxpageRequest.cjhkmerChange3(idCard, bankName, rate,
										extraFee, channelCode2);

							}
							Object respCode1 = maps.get("resp_code");
							if ("000000".equals(respCode1)) {
								maps = (Map<String, Object>) cjhkxpageRequest.fastpay1(orderCode, channelCode, cityName,
										provinceName);
							}
						} else {
							maps = (Map<String, Object>) cjhkxpageRequest.fastpay1(orderCode, channelCode, cityName,
									provinceName);
						}
					}
				} else if (!extraFee.equals(cjhkRegister.getExtraFee())) {

					ArrayList<String> arrayList1 = new ArrayList<String>();
					arrayList1.add("101001");
					arrayList1.add("101002");
					arrayList1.add("110002");
					arrayList1.add("110003");
					arrayList1.add("1000");
					for (Object cha : arrayList1) {
						String channelCode1 = cha.toString();
						LOG.info("修改提现费用========================" + channelCode1);
						maps = (Map<String, Object>) cjhkxpageRequest.cjhkmerChange3(idCard, bankName, rate, extraFee,
								channelCode1);

					}

					Object respCode = maps.get("resp_code");
					LOG.info("修改体现费返回的respCode=====" + respCode);
					if ("000000".equals(respCode)) {
						maps = (Map<String, Object>) cjhkxpageRequest.fastpay1(orderCode, channelCode, cityName,
								provinceName);
					}

				} else {
					maps = (Map<String, Object>) cjhkxpageRequest.fastpay1(orderCode, channelCode, cityName,
							provinceName);
				}

			}

			if ("11".equals(orderType)) {
				LOG.info("根据判断进入还款任务======");
				maps = (Map<String, Object>) cjhkxpageRequest.withdrawDeposit(request, orderCode);

			}

			return maps;
		} else {
			maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			maps.put(CommonConstants.RESP_MESSAGE, "网络有问题,请稍后重试");
			return maps;
		}
	}
}
