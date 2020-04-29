package com.jh.paymentgateway.business.impl;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
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

import com.jh.paymentgateway.basechannel.BaseChannel;
import com.jh.paymentgateway.business.TopupPayChannelBusiness;
import com.jh.paymentgateway.business.TopupRequestBusiness;
import com.jh.paymentgateway.pojo.PaymentRequestParameter;
import com.jh.paymentgateway.util.WithDrawOrder;
import com.jh.paymentgateway.util.uns.HttpFormParam;
import com.jh.paymentgateway.util.uns.Md5Encrypt;
import com.jh.paymentgateway.util.ymd.HTTPClientUtils;
import com.jh.paymentgateway.util.ymd.RsaUtils;

import cn.jh.common.tools.ResultWrap;
import cn.jh.common.utils.CommonConstants;
import net.sf.json.JSONObject;

@Service
public class UNSTopupPage extends BaseChannel implements TopupRequestBusiness {

	private static final Logger LOG = LoggerFactory.getLogger(UNSTopupPage.class);
	@Autowired
	private TopupPayChannelBusiness topupPayChannelBusiness;

	@Value("${payment.ipAddress}")
	private String ip;

	@Value("${unsxinli.accountId}")
	private String mer_id;
	
	@Value("${unsxinli.payUrl}")
	private String payurl;
	
	@Value("${unsxinli.queryUrl}")
	private String queryurl;
		
	@Value("${unsxinli.key}")
	private String key;

	
	@Override
	public Map<String, Object> topupRequest(Map<String, Object> params) throws Exception {
		PaymentRequestParameter bean = (PaymentRequestParameter) params.get("paymentRequestParameter");

		String orderCode = bean.getOrderCode();
		String BankName = bean.getDebitBankName();
		String bankCode = bean.getDebitCardNo();
		String accountName=bean.getUserName();
		String amount=bean.getRealAmount();
		String notifyURL=ip+"/v1.0/paymentgateway/topup/unspay/notify_call";
		
		Map<String, Object> maps = new HashMap<String, Object>();
		WithDrawOrder drawOrder = new WithDrawOrder();
		try {
			String priOrpub=accountName+"商户提现";
			StringBuffer sf = new StringBuffer();
			sf.append("accountId=").append(mer_id);
			sf.append("&name=").append(accountName);
			sf.append("&cardNo=").append(bankCode);
			sf.append("&orderId=").append(orderCode);
			sf.append("&purpose=").append(priOrpub);
			sf.append("&amount=").append(amount);
			sf.append("&responseUrl=").append(notifyURL);
			sf.append("&key=").append(key);
			LOG.info("usn签名前数据:"+sf); 
			String mac;
			mac = Md5Encrypt.md5(sf.toString()).toUpperCase();
			LOG.info("usn签名结果:"+mac); 
			HashMap<String, String> param = new HashMap<String, String>();  
		        param.put("accountId", mer_id); 
		        param.put("name", accountName); 
		        param.put("cardNo", bankCode); 
		        param.put("orderId", orderCode); 
		        param.put("purpose", priOrpub); 
		        param.put("amount", amount); 
		        param.put("responseUrl", notifyURL); 
		        param.put("mac", mac);
			String result;
			result = HttpFormParam.doPost(payurl, param);
			LOG.info("usn返回结果================"+result);
			JSONObject jsonObject =  JSONObject.fromObject(result);
			if(jsonObject.getString("result_code").equals("0000")) {
				drawOrder.setReqcode("00000");
				drawOrder.setResmsg(jsonObject.getString("result_msg"));
				maps=ResultWrap.init(CommonConstants.SUCCESS, "下单成功",drawOrder);
			}else {
				drawOrder.setReqcode("99999");
				drawOrder.setResmsg("提现失败请稍后重试");
				maps=ResultWrap.init(CommonConstants.FALIED, "hqt",drawOrder);
			}
	       
		} catch (Exception e) {
			LOG.info(e.getMessage()+"");
			maps=ResultWrap.init(CommonConstants.FALIED, "下单失败",this.init("99999", "下单失败", ""));
		}
		return maps;
	}
	
	//余额查询
	public  BigDecimal AvaiLableBalance(Map<String,Object> params) {
		BigDecimal availableBalance =BigDecimal.ZERO;
		
		return availableBalance;
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