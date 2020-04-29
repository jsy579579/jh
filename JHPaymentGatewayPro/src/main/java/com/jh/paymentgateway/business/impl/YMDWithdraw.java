package com.jh.paymentgateway.business.impl;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import com.jh.paymentgateway.basechannel.BaseChannel;
import com.jh.paymentgateway.business.WithdrawRequestBusiness;
import com.jh.paymentgateway.controller.YMDpageRequset;

@Service
public class YMDWithdraw extends BaseChannel implements WithdrawRequestBusiness {

	private static final Logger LOG = LoggerFactory.getLogger(YMDWithdraw.class);
	
	@Autowired
	private YMDpageRequset ymdPageRequset;

	@Value("${payment.ipAddress}")
	private String ip;

	HttpServletRequest request;
	HttpServletResponse response;
	@Override
	public BigDecimal CheckBalanceRequest()  {
		BigDecimal availableBalance =BigDecimal.ZERO;
		try {
			Map<String, Object> result=(Map<String, Object>) ymdPageRequset.CheckBalance(request, response);
			availableBalance=new BigDecimal((String)result.get("result"));
		} catch (UnsupportedEncodingException e) {
			LOG.info("一麻袋余额查询异常");
		}
		return availableBalance;
	}
	
}