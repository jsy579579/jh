package com.jh.paymentchannel.service;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import com.jh.paymentchannel.pojo.PaymentOrder;
import com.jh.paymentchannel.util.Util;
import com.jh.paymentchannel.util.ump.common.ReqData;
import com.jh.paymentchannel.util.ump.paygate.v40.Mer2Plat_v40;

import cn.jh.common.utils.ExceptionUtil;
import net.sf.json.JSONObject;


@Service
public class UMPTopupRequest implements TopupRequest{
	private static final Logger LOG = LoggerFactory.getLogger(UMPTopupRequest.class);
	//联动优势地址
	public static final String ADDRESS_PREFIX = "http://pay.soopay.net/spay/pay/payservice.do";
	//商户入驻成功
	@Value("${ump.merid}")
	public   String merchantCode;
	
	@Value("${ump.merprikeypath}")
	private  String merprikeypath;
	
	@Value("${ump.platcertpath}")
	private  String platcertpath;
	
	@Autowired
	Util util;
	
	@Override
	public Map<String, String> topupRequest(Map<String,Object> params)throws UnsupportedEncodingException {
		PaymentOrder paymentOrder = (PaymentOrder) params.get("paymentOrder");
		String ordercode = paymentOrder.getOrdercode();
		String amount = paymentOrder.getAmount().toString();
		String orderdesc = paymentOrder.getDesc();
		String notifyurl = (String) params.get("notifyURL");
		String returnurl = (String) params.get("returnURL");
		String channelParam = (String) params.get("channelParams");
		
		JSONObject jsonObject = null;
		try {
			jsonObject = testPay(amount, ordercode, orderdesc,  channelParam, notifyurl,returnurl);
		} catch (Exception e) {
			e.printStackTrace();LOG.error("",e);
		}
		 Map map  = new HashMap();		
		 map.put("resp_code", "success");
         map.put("channel_type", "quick");
         map.put("redirect_url", jsonObject.getString("qrCode"));
         return map;
	}
	
	//合作方标识符
	
	public  JSONObject testPay(String amount, String ordercode, String orderdesc, String extraParam,  String notifyurl,  String returnurl) throws Exception{
		
		
		/**根据订单号获取相应的订单*/
		String bankcard ="";
		{
		RestTemplate restTemplate=new RestTemplate();
		URI uri = util.getServiceUrl("transactionclear", "error url request!");
		String url = uri.toString() + "/v1.0/transactionclear/payment/query/ordercode";
		MultiValueMap<String, String> requestEntity  = new LinkedMultiValueMap<String, String>();
		requestEntity.add("order_code", ordercode);
		String result = restTemplate.postForObject(url, requestEntity, String.class);
		LOG.info("RESULT================"+result);
		JSONObject jsonObject =  JSONObject.fromObject(result);
		JSONObject resultObj  =  jsonObject.getJSONObject("result");
		bankcard       =  resultObj.getString("bankcard");
		}	
		if(bankcard == null || bankcard.equalsIgnoreCase("")){
			return null;
		}
		//身份证号
		String certify_id="";
		//姓名
		String customerNm="";
		
		
		{
			/**根据订单号获取相应的订单*/
			RestTemplate restTemplate=new RestTemplate();
			URI uri = util.getServiceUrl("user", "error url request!");
			String url = uri.toString() + "/v1.0/user/bank/default/cardno";
			MultiValueMap<String, String> requestEntity  = new LinkedMultiValueMap<String, String>();
			requestEntity.add("cardno", bankcard);
			requestEntity.add("type", "0");
			String result = restTemplate.postForObject(url, requestEntity, String.class);
			LOG.info("RESULT================"+result);
			JSONObject jsonObject =  JSONObject.fromObject(result);
			JSONObject resultObj  =  jsonObject.getJSONObject("result");
			if(resultObj==null){
				return null;
			}
			certify_id       =  resultObj.getString("idcard");
			customerNm       =  resultObj.getString("userName");
		}
		
		
		
		
		JSONObject respJson = new JSONObject();
		Map<String,String> map =new HashMap<String, String>();
		//接口名称
		map.put("service", "pay_req_h5_frontpage");
		//参数字符编码集
		map.put("charset", "UTF-8");
		//商户编号【mer_id】
		map.put("mer_id", merchantCode);
		//签名方式【sign_type】
		map.put("sign_type", "RSA");
		//服务器异步通知页面路径【notify_url】
		map.put("notify_url", notifyurl );
		//页面跳转同步通知页面路径【ret_url】
		map.put("ret_url", returnurl );
		//响应数据格式【res_format】
		map.put("res_format", "HTML");
		//版本号【version】
		map.put("version", "4.0");
		
		//商户唯一订单号【order_id】：
		map.put("order_id", ordercode);
		//商户订单日期【mer_date】：YYYYMMDD
		map.put("mer_date",  new SimpleDateFormat("yyyyMMdd").format(new Date()));
		
		//付款金额【amount】
		map.put("amount", new  BigDecimal(amount).multiply(new BigDecimal("100")).setScale(0).toString());
		//付款币种【amt_type】：
		map.put("amt_type", "RMB");
		//商品描述信息【goods_inf】：
		map.put("goods_inf", orderdesc);
		//卡号【card_id】：
		map.put("card_id", bankcard);
		//证件类型【identity_type】：
		map.put("identity_type", "1");
		//证件号【identity_code】：
		map.put("identity_code", certify_id);
		//持卡人姓名【card_holder】：
		map.put("card_holder", customerNm);
		//是否允许用户修改支付要素【can_modify_flag】：
		map.put("can_modify_flag", "0");
		
		ReqData reqData = Mer2Plat_v40.makeReqDataByGet(map);
		LOG.info("联动请求："+reqData.getUrl());
		respJson.put("qrCode", reqData.getUrl());
		return respJson;
	}
		
	

}
