package com.jh.paymentgateway.business.impl;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.jh.paymentgateway.basechannel.BaseChannel;
import com.jh.paymentgateway.business.MHTopupPayChannelBusiness;
import com.jh.paymentgateway.business.TopupRequestBusiness;
import com.jh.paymentgateway.controller.MHHQGpageRequest;
import com.jh.paymentgateway.pojo.MHHQGBindCard;
import com.jh.paymentgateway.pojo.MHHQGRegister;
import com.jh.paymentgateway.pojo.PaymentRequestParameter;

import cn.jh.common.utils.CommonConstants;

@Service
public class MHHQGTopupPage extends BaseChannel implements TopupRequestBusiness {

	private static final Logger LOG = LoggerFactory.getLogger(MHHQGTopupPage.class);

	@Autowired
	private HttpServletRequest request;

	@Autowired
	private MHHQGpageRequest hqgPageRequest;

	@Value("${payment.ipAddress}")
	private String ipAddress;

	@Autowired
	private MHTopupPayChannelBusiness topupPayChannelBusiness;

	@Override
	public Map<String, Object> topupRequest(Map<String, Object> params) throws Exception {
		PaymentRequestParameter bean = (PaymentRequestParameter) params.get("paymentRequestParameter");
		Map<String, Object> map = new HashMap<String, Object>();
		Map<String, String> mapr = new HashMap<String, String>();
		String orderCode = bean.getOrderCode();
		String orderType = bean.getOrderType();
		String bankNo = bean.getBankCard();
		String rate = bean.getRate();
		String extraFee = bean.getExtraFee();
		String idCard = bean.getIdCard();
		MHHQGRegister hqRegister = topupPayChannelBusiness.getMHHQGRegisterByIdCard(idCard);
		MHHQGBindCard hqBindCard = topupPayChannelBusiness.getMHHQGBindCardByBankCard(bankNo);
		if(hqRegister!=null&&hqBindCard!=null&&hqRegister.getStatus().equals("1")&&hqBindCard.getStatus().equals("1")){
			if(!hqRegister.getRate().equals(rate)||!hqRegister.getExtraFee().equals(extraFee)){
				mapr= hqgPageRequest.HQGUpdate( hqRegister.getMerchantCode(), rate, extraFee);
				String returncode=mapr.get("returncode");
				String errtext=mapr.get("errtext");
				if(returncode.equals("0000")){
					hqRegister.setExtraFee(extraFee);
					hqRegister.setRate(rate);
					topupPayChannelBusiness.createMHHQGRegister(hqRegister);
					if ("10".equals(orderType)) {
						LOG.info("判断进入消费任务==============");
						map=(Map<String, Object>)hqgPageRequest.hqFastPay(request, orderCode);
					}
					if ("11".equals(orderType)) {
						LOG.info("根据判断进入还款任务======");
						map = (Map<String, Object>) hqgPageRequest.transfer(request, orderCode);
					}
				}else{
					map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
					map.put(CommonConstants.RESP_MESSAGE, errtext);
					this.addOrderCauseOfFailure(orderCode, errtext, bean.getIpAddress());
				}
			}else{
				if ("10".equals(orderType)) {
					LOG.info("判断进入消费任务==============");
					map=(Map<String, Object>)hqgPageRequest.hqFastPay(request, orderCode);
				}
				if ("11".equals(orderType)) {
					LOG.info("根据判断进入还款任务======");
					map = (Map<String, Object>) hqgPageRequest.transfer(request, orderCode);
				}
			}
			
		}
		

		return map;
	}
}
