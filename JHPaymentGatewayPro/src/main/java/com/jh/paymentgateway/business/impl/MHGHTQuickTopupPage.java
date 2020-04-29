package com.jh.paymentgateway.business.impl;

import java.net.URLEncoder;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.jh.paymentgateway.basechannel.BaseChannel;
import com.jh.paymentgateway.business.MHTopupPayChannelBusiness;
import com.jh.paymentgateway.business.TopupRequestBusiness;
import com.jh.paymentgateway.pojo.GHTBindCard;
import com.jh.paymentgateway.pojo.PaymentRequestParameter;

import cn.jh.common.tools.ResultWrap;
import cn.jh.common.utils.CommonConstants;

@Service
public class MHGHTQuickTopupPage extends BaseChannel implements  TopupRequestBusiness{

	@Value("${payment.ipAddress}")
	private String ip;
	
	@Autowired
	private MHTopupPayChannelBusiness topupPayChannelBusiness;
	
	@Override
	public Map<String, Object> topupRequest(Map<String, Object> params) throws Exception {
		PaymentRequestParameter bean = (PaymentRequestParameter) params.get("paymentRequestParameter");
		
		String orderCode = bean.getOrderCode();
		String expiredTime = bean.getExpiredTime();
		String securityCode = bean.getSecurityCode();
		String bankCard = bean.getBankCard();
		String bankName = bean.getCreditCardBankName();
		String cardType = bean.getCreditCardCardType();
		String amount = bean.getAmount();
		String phone = bean.getCreditCardPhone();
		String ipAddress = bean.getIpAddress();
		
		GHTBindCard ghtBindCardByBankCard = topupPayChannelBusiness.getGHTBindCardByBankCard(bankCard);
		
		if(ghtBindCardByBankCard == null || !"1".equals(ghtBindCardByBankCard.getStatus())) {
			
			return ResultWrap.init(CommonConstants.SUCCESS, "用户需要进行绑卡授权操作",
					ip + "/v1.0/paymentgateway/topup/tomhghtquick/bindcard?bankName=" + URLEncoder.encode(bankName, "UTF-8")
							+ "&cardType=" + URLEncoder.encode(cardType, "UTF-8") + "&bankCard=" + bankCard + "&orderCode="
							+ orderCode + "&expiredTime=" + expiredTime + "&securityCode=" + securityCode + "&ipAddress=" + ip);
			
		}else {
			
			return ResultWrap.init(CommonConstants.SUCCESS,"成功",ip + "/v1.0/paymentgateway/topup/tomhghtquick/pay?bankName="
					+ URLEncoder.encode(bankName, "UTF-8") + "&bankCard=" + bankCard + "&ordercode=" + orderCode
				    + "&nature=" + URLEncoder.encode(cardType, "UTF-8") + "&phone=" + phone + "&amount=" + amount
					+ "&expiredTime=" + expiredTime+ "&securityCode=" + securityCode + "&ipAddress=" + ip + "&ips=" + ipAddress);
			
		}
		
	}

}
