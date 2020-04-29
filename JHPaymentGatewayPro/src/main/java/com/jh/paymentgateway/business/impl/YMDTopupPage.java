package com.jh.paymentgateway.business.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.codec.binary.Base64;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import com.jh.paymentgateway.basechannel.BaseChannel;
import com.jh.paymentgateway.business.TopupPayChannelBusiness;
import com.jh.paymentgateway.business.TopupRequestBusiness;
import com.jh.paymentgateway.common.ChannelUtils;
import com.jh.paymentgateway.controller.YMDpageRequset;
import com.jh.paymentgateway.pojo.PaymentRequestParameter;
import com.jh.paymentgateway.util.ymd.HTTPClientUtils;
import com.jh.paymentgateway.util.ymd.RsaUtils;

import cn.jh.common.tools.ResultWrap;
import cn.jh.common.utils.CommonConstants;

@Service
public class YMDTopupPage extends BaseChannel implements TopupRequestBusiness {

	private static final Logger LOG = LoggerFactory.getLogger(YMDTopupPage.class);
	@Autowired
	private TopupPayChannelBusiness topupPayChannelBusiness;
	
	@Autowired
	private YMDpageRequset ymdPageRequset;

	@Value("${payment.ipAddress}")
	private String ip;
	private static String Daifu_REQUEST_URL = "https://gwapi.yemadai.com/transfer/transferFixed";   //正式环境请求地址

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
			String bankCode = bean.getDebitBankName();
			String debitCardNo = bean.getDebitCardNo();
			String accountName=bean.getUserName();
			String amount=bean.getRealAmount();
			
			
			Map<String, Object> maps = new HashMap<String, Object>();
			
			try {
				RsaUtils rsaUtils = RsaUtils.getInstance();
		        String transId=orderCode;
		        //5位商户号
		        String accountNumber="";
		        String cardNo=debitCardNo;
		        String plain ="transId="+transId+"&accountNumber="+accountNumber+"&cardNo="+cardNo+"&amount="+amount;
		        //一麻袋私钥
		        String prikey = "";

		        String signInfo = rsaUtils.signData(plain, prikey);
		        StringBuffer stringBuffer = new StringBuffer();
		        stringBuffer.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
		        stringBuffer.append("<yemadai>");
		        stringBuffer.append("<accountNumber>"+accountNumber+"</accountNumber>");
		        stringBuffer.append("<signType>RSA</signType>");
		        stringBuffer.append("<notifyURL>"+ip+"/v1.0/paymentgateway/quick/ymd/callback</notifyURL>");
		        stringBuffer.append("<tt>0</tt>");
		        stringBuffer.append("<transferList>");
			        stringBuffer.append("<transId>"+ transId +"</transId>");
			        stringBuffer.append("<bankCode>"+bankCode+"</bankCode>");
			        stringBuffer.append("<provice>上海</provice>");
			        stringBuffer.append("<city>上海</city>");
			        stringBuffer.append("<branchName>逸仙路支行</branchName>");
			        stringBuffer.append("<accountName>"+accountName+"</accountName>");
			        stringBuffer.append("<cardNo>"+cardNo+"</cardNo>");
			        stringBuffer.append("<amount>"+amount+"</amount>");
			        stringBuffer.append("<remark>123</remark>");
			        stringBuffer.append("<secureCode>" + signInfo + "</secureCode>");
		        stringBuffer.append("</transferList>");
		        stringBuffer.append("</yemadai>");
		        
		        Base64 base64 = new Base64();
		        LOG.info("stringBuffer======="+stringBuffer);
		        List<NameValuePair> nvps = new ArrayList<NameValuePair>(1);
				nvps.add(new BasicNameValuePair("transData", base64.encodeToString(stringBuffer.toString().getBytes("UTF-8"))));
		        LOG.info("nvps====="+nvps);
		        String httpPost = this.connect(nvps, Daifu_REQUEST_URL);
		        LOG.info(new String(base64.decode(httpPost), "utf-8"));
		        maps=ResultWrap.init(CommonConstants.SUCCESS, "下单成功",this.init("00000", "下单成功", ""));
		        
			} catch (Exception e) {
				LOG.info(e.getMessage()+"");
				maps=ResultWrap.init(CommonConstants.FALIED, "下单失败",this.init("99999", "下单失败", ""));
			}
			return maps;
	}
	
	
	public static Map<String, Object> init(String respCode, String respMesg,String thirdordercode) {
    	Map<String, Object> map = new HashMap<String, Object>();
        map.put("reqcode", respCode);
        map.put("resmsg", respMesg);
        map.put("thirdordercode", respMesg);
        return map;
	}
    
	/**
	 * 连接类
	 * 
	 * @param nvps
	 * @param requestURL
	 * @return
	 */
	public String connect(List<NameValuePair> nvps, String requestURL) {
		try {
			
			HTTPClientUtils h = new HTTPClientUtils();
			String httpPost = h.httpPostPara(nvps, requestURL);
			return httpPost;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}
}