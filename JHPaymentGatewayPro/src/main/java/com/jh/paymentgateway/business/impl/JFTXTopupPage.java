package com.jh.paymentgateway.business.impl;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.time.DateFormatUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.jh.paymentgateway.basechannel.BaseChannel;
import com.jh.paymentgateway.business.TopupPayChannelBusiness;
import com.jh.paymentgateway.business.TopupRequestBusiness;
import com.jh.paymentgateway.pojo.BankNumCode;
import com.jh.paymentgateway.pojo.PaymentRequestParameter;
import com.jh.paymentgateway.util.jftx.CommunicationConsumeSecurityWarper;
import com.jh.paymentgateway.util.jftx.HttpClient4Util;
import com.jh.paymentgateway.util.jftx.RSAKey;
import com.jh.paymentgateway.util.jftx.CommunicationProvideSecurityWarper;

import cn.jh.common.tools.ResultWrap;
import cn.jh.common.utils.CommonConstants;

@Service
public class JFTXTopupPage extends BaseChannel implements TopupRequestBusiness {

	private static final Logger LOG = LoggerFactory.getLogger(JFTXTopupPage.class);
	@Autowired
	private TopupPayChannelBusiness topupPayChannelBusiness;

	@Value("${payment.ipAddress}")
	private String ip;


	@Value("${jftx.requestURL}")
	private String requestURL;
	
	@Value("${jftx.partnerNo}")
	private String partnerNo;
	
	@Value("${jftx.public_key}")
	private String publicKey;
	
	@Value("${jftx.private_key}")
	private String privateKey;

	
	@Override
	public Map<String, Object> topupRequest(Map<String, Object> params) throws Exception {
		PaymentRequestParameter bean = (PaymentRequestParameter) params.get("paymentRequestParameter");

		String orderCode = bean.getOrderCode();
		String bankName = bean.getDebitBankName();
		String debitCardNo = bean.getDebitCardNo();
		String userName=bean.getUserName();
		String Extra=bean.getExtra();
		String debitPhone=bean.getDebitPhone();
		String idCard=bean.getIdCard();
		
		
		// 获取银行联行号
		BankNumCode bcode = topupPayChannelBusiness.getBankNumCodeByBankName(bankName);
		if (bcode == null) {
			return ResultWrap.init(CommonConstants.FALIED, "该到账卡银行暂不支持!");
		}
		String bankUnitNo = bcode.getBankBranchcode();
		Map<String, Object> maps = new HashMap<String, Object>();
    	String url = requestURL + "102001";
        //需与报文头的traceId一致
        String traceId = orderCode;
        Map<String,Object > map = new HashMap<String,Object>();
        Map<String, Object> head = new HashMap<String, Object>();
        head.put("traceId", traceId);
        head.put("charset", "UTF-8");
        head.put("partnerNo", partnerNo);
        head.put("txnCode", "102001");
        head.put("reqDate", DateFormatUtils.format(new Date(),"yyyyMMdd"));
        head.put("partnerType", "OUTER");
        head.put("reqTime", DateFormatUtils.format(new Date(),"yyyyMMddHHmmss"));
        head.put("version", "1.0.0");
        
        // 业务参数
        map.put("callBackUrl", ip+"/v1.0/paymentgateway/quick/jftx/callback");//异步回调地址
		map.put("accountCategory", "PERSON");//收款人账户种类
		map.put("accountName", userName );//收款人姓名
		map.put("purpose",  Extra);//资金用途
		map.put("mobile", debitPhone);//手机号
		map.put("bankName", bcode.getBankName());//收款人账户总行名称
		map.put("certificateNo", idCard);//证件号
		map.put("head", head);//证件号
		map.put("accountNo",  debitCardNo);//收款人账户号
		map.put("bankNo", bankUnitNo);//收款人账户总行联行号
		map.put("currency", "156");//币种
		map.put("txnAmt", new BigDecimal(bean.getRealAmount()).multiply(new BigDecimal("100")).setScale(0).toString());//订单金额
		map.put("certificateType", "ID");//证件类型
		String jsonStr = JSON.toJSONString(map);
		LOG.info("请求明文："+jsonStr);
        //上游代付公钥
        PublicKey publicKeyplatform = RSAKey
                .getRSAPublicKeyByAbsoluteFileSuffix(publicKey, "pem","RSA");
        //平台秘钥
        PrivateKey privatekeypartner = RSAKey
                .getRSAPrivateKeyByAbsoluteFileSuffix(privateKey, "pem","RSA");
        //加密串
        String randomAESKey = "6D0DD441A36B7AEE";

        String cipherB64AESKey = CommunicationConsumeSecurityWarper.getCipherB64AESKeyByplatformPublicKey(randomAESKey,
                publicKeyplatform);

        String cipherB64PlainText = CommunicationConsumeSecurityWarper.getCipherB64PlainTextByPartnerAESKey(jsonStr, randomAESKey);

        String signB64PlainText = CommunicationConsumeSecurityWarper.getSignB64PlainTextByPartnerPrivateKey(jsonStr, privatekeypartner);

        Map<String, String> nvps = new HashMap<String, String>();
        nvps.put("encryptData", cipherB64PlainText);
        nvps.put("encryptKey", cipherB64AESKey);
        nvps.put("signData", signB64PlainText);
        nvps.put("traceId", traceId);
        nvps.put("partnerNo", partnerNo);
        
        byte[] resByte = HttpClient4Util.getInstance().doPost(url, null, nvps);
		String resStr = new String(resByte, StandardCharsets.UTF_8);
		LOG.info("============ 返回报文原文:" + resStr);
		JSONObject resJson = JSON.parseObject(resStr);
		try {
			  String plainText = CommunicationProvideSecurityWarper.getPlainTextByAESKey(resJson.getString("cipherPartnerAES"), randomAESKey);
			  JSONObject plainTextJson = JSON.parseObject(plainText);
		      JSONObject plainheadJson=plainTextJson.getJSONObject("head");
		      if(plainheadJson.get("respCode").equals("000001")||plainheadJson.get("respCode").equals("000000")){
		    	  maps=ResultWrap.init(CommonConstants.SUCCESS, "下单成功",this.init("00000", "下单成功", ""));
		      }else{
		    	  maps=ResultWrap.init(CommonConstants.FALIED, "下单失败",this.init("99999", "下单失败", ""));
		      }
			
		} catch (Exception e) {
			LOG.info(e.getMessage()+"");
			maps=ResultWrap.init(CommonConstants.FALIED, "下单失败",this.init("99999", "下单失败", ""));
		}
		LOG.info(resJson+"");
		return maps;
	}
	
	public static Map<String, Object> init(String respCode, String respMesg,String thirdordercode) {
    	Map<String, Object> map = new HashMap<String, Object>();
        map.put("reqcode", respCode);
        map.put("resmsg", respMesg);
        map.put("thirdordercode", respMesg);
        return map;
    
    }
}