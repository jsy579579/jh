package com.jh.paymentchannel.service;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestTemplate;

import net.sf.json.JSONObject;
import com.epayplusplus.api.EpayppApiException;
import com.epayplusplus.api.EpayppConstants;
import com.epayplusplus.api.EpayppEncrypt;
import com.epayplusplus.api.EpayppSignature;
import com.epayplusplus.api.enums.EncryptTypeEnum;
import com.epayplusplus.api.enums.SignMethodEnum;

import com.epayplusplus.api.request.EpayppMerchantProductOpenRequest;
import com.epayplusplus.api.request.EpayppMerchantProductRateSetRequest;
import com.epayplusplus.api.request.EpayppMerchantRegisterRequest;
import com.epayplusplus.api.request.EpayppMerchantSettleAccountSetRequest;
import com.epayplusplus.api.request.EpayppTradeCreateRequest;
import com.epayplusplus.api.request.EpayppTradePayRequest;
import com.epayplusplus.api.request.EpayppWithoutCardTradeExpressVerifyCodeSubmitRequest;

import com.epayplusplus.api.response.EpayppMerchantProductOpenResponse;
import com.epayplusplus.api.response.EpayppMerchantProductRateSetResponse;
import com.epayplusplus.api.response.EpayppMerchantRegisterResponse;
import com.epayplusplus.api.response.EpayppMerchantSettleAccountSetResponse;
import com.epayplusplus.api.response.EpayppTradeCreateResponse;
import com.epayplusplus.api.response.EpayppTradePayResponse;
import com.epayplusplus.api.response.EpayppWithoutCardTradeExpressVerifyCodeSubmitResponse;

import com.jh.paymentchannel.basechannel.BaseChannel;
import com.jh.paymentchannel.business.RSBusiness;
import com.jh.paymentchannel.business.TopupPayChannelBusiness;
import com.jh.paymentchannel.pojo.CJHKRegister;
import com.jh.paymentchannel.pojo.LDRegister;
import com.jh.paymentchannel.pojo.PaymentOrder;
import com.jh.paymentchannel.pojo.RSRegister;
import com.jh.paymentchannel.util.EpayppEnvironmentData;
import com.jh.paymentchannel.util.Util;
import com.microsoft.schemas.office.x2006.encryption.CTKeyEncryptor.Uri;

import cn.jh.common.utils.CommonConstants;

@Service
public class RSRequestTwo extends BaseChannel implements TopupRequest {

	@Autowired
	private TopupPayChannelBusiness topupPayChannelBusiness;

	@Autowired
	private RSBusiness rsBusiness;

	@Autowired
	private RestTemplate restTemplate;
	
	@Autowired
	private RSRequest rsRequest;

	@Value("${payment.ipAddress}")
	private String ipAddress;

	private static final Logger log = LoggerFactory.getLogger(RSRequestTwo.class);
	
	private static final Charset UTF_8 = StandardCharsets.UTF_8;
	
	@Autowired
	private Util util;

	@Override
	public Map<String, String> topupRequest(Map<String,Object> params) throws Exception {
		PaymentOrder paymentOrder = (PaymentOrder) params.get("paymentOrder");
		HttpServletRequest request = (HttpServletRequest) params.get("request");
		//订单号
		String ordercode = paymentOrder.getOrdercode();	
		Map<String, String> map = new HashMap<String, String>();
		log.info("根据判断进入消费任务======");		
		map = (Map<String, String>) rsRequest.test(request,ordercode);
		
		if ("999999".equals(map.get("resp_code"))) {
			log.info("消费任务创建失败======");
			map.put(CommonConstants.RESP_MESSAGE, "创建交易订单失败！");
		}else{
			log.info("消费任务创建成功======");
	
		}

		return map;

	}


}
