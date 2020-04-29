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
import com.jh.paymentgateway.pojo.PaymentRequestParameter;
import com.jh.paymentgateway.pojo.YTJFSignCard;
import com.jh.paymentgateway.util.Util;
import com.jh.paymentgateway.util.ytjf.CHexConver;
import com.jh.paymentgateway.util.ytjf.HttpClient;
import com.jh.paymentgateway.util.ytjf.MD5;
import com.jh.paymentgateway.util.ytjf.SecurityUtils;

import cn.jh.common.tools.ResultWrap;
import cn.jh.common.utils.CommonConstants;

@Service
public class YTJFTopupPage extends BaseChannel implements  TopupRequestBusiness{

	private static final Logger LOG = LoggerFactory.getLogger(YTJFTopupPage.class);
	
	private static final String URL = "http://58.56.23.89:7002/NetPay/quickSign.action";
	
    
    private static final String DATEKEY="8EF53C251102A4E6";
    
    private static final String BUSSID="ONL0022";

	@Autowired
	private Util util;
	
	@Autowired
	private TopupPayChannelBusiness topupPayChannelBusiness;
	
	@Value("${payment.ipAddress}")
	private String ip;
	
	@Value("${ytjf.merchantId}")
	private String merchantId;
	
	@Value("${ytjf.datekey}")
	private String datekey;
	
	@Value("${ytjf.payUrl}")
	private String payUrl;
	
	@Value("${ytjf.encode}")
	private String ENCODE;
	
	@Override
	public Map<String, Object> topupRequest(Map<String, Object> params) throws Exception {
		PaymentRequestParameter bean = (PaymentRequestParameter) params.get("paymentRequestParameter");
		
		String creditCardbankCard = bean.getBankCard();
		
		YTJFSignCard  YTJFSignCard=topupPayChannelBusiness.getYTJFSignCardByBankCard(creditCardbankCard);
		
		if(YTJFSignCard!=null&&YTJFSignCard.getStatus().trim().equals("1")){
			return ResultWrap.init(CommonConstants.SUCCESS,"成功",ip + "/v1.0/paymentgateway/quick/ytjf/SynonymNamePay?orderCode="+bean.getOrderCode());
		}else{
			return ResultWrap.init(CommonConstants.SUCCESS,"成功",ip + "/v1.0/paymentgateway/topup/ytjf/toBindCard?orderCode="+bean.getOrderCode());
		}
		
		
	}
	
	
	
	public static String sortParam(Map<String, String> mapData) {

        return
                "bussId=" + mapData.get("bussId") +
                "&cardCvn2=" + mapData.get("cardCvn2") +
                "&cardExpire=" + mapData.get("cardExpire") +
                "&certNo=" + mapData.get("certNo") +
                "&merchantId=" + mapData.get("merchantId") +
                "&merOrderNum=" + mapData.get("merOrderNum") +
                "&tranAmt=" + mapData.get("tranAmt") +
                "&userAcctNo=" + mapData.get("userAcctNo") +
                "&userId=" + mapData.get("userId") +
                "&userIp=" + mapData.get("userIp") +
                "&userName=" + mapData.get("userName") +
                "&userPhone=" + mapData.get("userPhone") + "&";
    }
}

