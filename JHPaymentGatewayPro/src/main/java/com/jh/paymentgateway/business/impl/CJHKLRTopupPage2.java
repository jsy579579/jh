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
import com.jh.paymentgateway.controller.CJHKLRTopupRequest;
import com.jh.paymentgateway.controller.CJHKLRTopupRequest2;
import com.jh.paymentgateway.pojo.CJHKLRRegister;
import com.jh.paymentgateway.pojo.CJXChannelCode;
import com.jh.paymentgateway.pojo.PaymentRequestParameter;
import com.jh.paymentgateway.util.Util;

import cn.jh.common.tools.ResultWrap;
import cn.jh.common.utils.CommonConstants;

/**
 * @author 作者:lirui
 * @version 创建时间：2019年5月27日 下午1:52:40 类说明
 */
@Service
public class CJHKLRTopupPage2 extends BaseChannel implements TopupRequestBusiness {

	@Value("${payment.ipAddress}")
	private String ipAddress;

	@Autowired
	private TopupPayChannelBusiness topupPayChannelBusiness;

	@Autowired
	private CJHKLRTopupRequest2 cjhkTopupRequest2;
	
	@Autowired
	private CJHKLRTopupRequest cjhkTopupRequest;

	private static final Logger LOG = LoggerFactory.getLogger(CJHKLRTopupPage2.class);

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
		if (cjxChannelCode == null) {
			return ResultWrap.init(CommonConstants.RESP_CODE, "该银行无可用渠道400");
		}
		String channelCode = cjxChannelCode.getChannelCode();
		CJHKLRRegister cjhklr = topupPayChannelBusiness.getRegister(idCard);
		String cityName = null;
		String provinceName = null;
		if ("10".equals(orderType)) {
			LOG.info("判断进入消费任务==============");
			// 判断用户是否需要修改费率和固定手续费
			if (!rate.equals(cjhklr.getRate2()) || !extraFee.equals(cjhklr.getExtraFee())) {
				maps = (Map<String, Object>) cjhkTopupRequest2.cjhkChangeRate(idCard, rate, "101001", extraFee,"1");
				if (!"000000".equals(maps.get("resp_code"))) {
					LOG.info("======================修改渠道:101001==============执行失败");
					return maps;
				}
				maps = (Map<String, Object>) cjhkTopupRequest2.cjhkChangeRate(idCard, rate, "110001", extraFee,"1");
				if (!"000000".equals(maps.get("resp_code"))) {
					LOG.info("======================修改渠道:110001==============执行失败");
					return maps;
				}
				if (!extra.contains("-")) {
					cityName = "上海市";
					provinceName = "上海市";
				} else {
					provinceName = extra.substring(extra.indexOf("|") + 1, extra.indexOf("-"));
					cityName = extra.substring(extra.indexOf("-") + 1, extra.lastIndexOf("-"));
				}
			}
			try {
				provinceName = extra.substring(extra.indexOf("|") + 1, extra.indexOf("-"));
				cityName = extra.substring(extra.indexOf("-") + 1, extra.lastIndexOf("-"));
			} catch (Exception e) {
				e.printStackTrace();
				LOG.info("===============省：" + provinceName + "===========市：" + cityName);
			}
			maps = (Map<String, Object>) cjhkTopupRequest.cjhkconsume(orderCode, channelCode, cityName, provinceName);
		}

		if ("11".equals(orderType)) {
			LOG.info("根据判断进入还款任务================");
			maps = (Map<String, Object>) cjhkTopupRequest.withdrawDeposit(orderCode, "D0");

		}

		return maps;

	}
}
