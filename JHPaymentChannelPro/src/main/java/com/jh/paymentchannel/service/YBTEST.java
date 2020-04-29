package com.jh.paymentchannel.service;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.PostMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.jh.paymentchannel.util.Util;
import com.jh.paymentchannel.util.yeepay.Conts;
import com.jh.paymentchannel.util.yeepay.Digest;

import cn.jh.common.utils.ExceptionUtil;
import net.sf.json.JSONObject;


@Controller
@EnableAutoConfiguration
public class YBTEST {

	private static final Logger LOG = LoggerFactory.getLogger(YBTEST.class);
	private static String key = Conts.hmacKey; // 商户秘钥
	
	/**
	 * 请求参数 验签在数组最后
	 *
	 */
	private static NameValuePair[] param = {
			// 大商户编号
			new NameValuePair("mainCustomerNumber", ""),
			// 小商户编号
			new NameValuePair("customerNumber", ""),
			// 出款金额
			new NameValuePair("balanceType", ""),
			
			// 签名串
			new NameValuePair("hmac", ""),
			
	};
	
	@Autowired
	Util util;
	@Autowired
	RegisterAuthService ras;
	
	@Value("${payment.ipAddress}")
	private String ipAddress;
	@RequestMapping(method=RequestMethod.POST,value="/v1.0/paymentchannel/topup/yb/test")
	public @ResponseBody String notifycalltest(HttpServletRequest req, 
			@RequestParam(value = "customerNumber") String customerNumber,
			@RequestParam(value = "balanceType") String balanceType) throws IOException{
						String mainCustomerNumber = Conts.customerNumber; // 代理商编码
						param[0].setValue(mainCustomerNumber);
						param[1].setValue(customerNumber);
						param[2].setValue(balanceType);
						param[param.length - 1].setValue(hmacSign());
						PostMethod postMethod = new PostMethod("https://skb.yeepay.com/skb-app/customerBalanceQuery.action");
						HttpClient client = new HttpClient();
						postMethod.addRequestHeader("Content-Type","application/x-www-form-urlencoded; charset=UTF-8");
						try {
							postMethod.setRequestBody(param);
							int status2 = client.executeMethod(postMethod);
							LOG.info("==========结算status2=========="+status2);
							String backinfo = postMethod.getResponseBodyAsString();
							LOG.info("==========结算backinfo=========="+backinfo);
							if (status2 == HttpStatus.SC_OK) {
								JSONObject obj = JSONObject.fromObject(backinfo);
								if(obj.getString("code").equals("0000")) {
									return obj.getString("balance");
								}else {
									status2 = client.executeMethod(postMethod);
									backinfo = postMethod.getResponseBodyAsString();
									LOG.info("==========结算backinfo=========="+backinfo);
									obj = JSONObject.fromObject(backinfo);
									if(obj.getString("code").equals("0000")) {
										return "出款成功";
									}else {
										return "出款失败";
									}
								}
							} else {
								return "出款失败";
							}
						} catch (Exception e) {
							e.printStackTrace();LOG.error("",e);
							return "出款失败";
						} finally {
							// 释放连接
							postMethod.releaseConnection();
						}
	}
	/**
	 * 签名
	 *
	 * @return
	 */
	private static String hmacSign() {
		StringBuilder hmacStr = new StringBuilder();
		for (NameValuePair nameValuePair : param) {
			if (nameValuePair.getName().equals("hmac")) {
				continue;
			}
			hmacStr.append(nameValuePair.getValue() == null ? ""
					: nameValuePair.getValue());
			
		}
		
		System.out.println("===============");
		System.out.println("hmacStr.toString()=" + hmacStr.toString());
		System.out.println("===============");
		
		String hmac = Digest.hmacSign(hmacStr.toString(), key);
		
		System.out.println("===============");
		System.out.println("hmac=" + hmac);
		System.out.println("===============");
		
		return hmac;
	}
	
}


