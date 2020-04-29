package com.jh.paymentchannel.util.abroad;

import java.util.Map;
import java.util.TreeMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jh.paymentchannel.service.AbroadConsumptionPageRequest;

public class PaymentUtil {
	
	private static final Logger LOG = LoggerFactory.getLogger(PaymentUtil.class);
	
	public static String merchant_no = "1180921152243326"; // 商户号
	public static String partner_id = "10803580000000896713"; // 商户签约PID
	//public static String apiUrl = "http://api.mgpayment.com/v2"; // 接口地址（测试）
	public static String apiUrl = "http://api.smtpayment.com/v2";
	
	// 商户私钥
	private static String privateKey = "MIIEvAIBADANBgkqhkiG9w0BAQEFAASCBKYwggSiAgEAAoIBAQDZLzrQPFyqrvJHeoSCP5GNTyuOCLZaWB373r3KpLu6PM+2NlO+XMNOciKBpTTaq5Bnp18LVSspf8x1UqB4tLi+qHEsqB6r2J9/PzzjWdQ+3RVu/F1PZlaOvRaVTdFwyawAhbpaQrMTcl8PCXTbmv3PJWaJMN1FW2DtZAxSuCtwS1HEdmzWvpi9sgXd2BoGIfwF9FKTkz2sIRBuoHi0Y/C2BVkxteUsLqrxT/iK/Kh44Egc8mwl4KJjgShvEeVHHy+7AREQxLHpkK6OEarmdig+/cFnDgRc0Ky9gL80G7VuBHfK3lQdx6wlqBtV4NA98pzrFVMn5V4TrGPdUXpe4uoJAgMBAAECggEAd6YuZ5QplRrGfRyBLeRpgokQPLKOC52p/x2KP76bcZGY+RBa+G/QZUI5wmTKhoGoD4LEuNCV88nlUOlRYM5nTRotJ2YDZ7byv76OA850QKG58if0HA8MNpzzeROjA9kG+k6yCJHgaCFFP1p9V5K+q0U2hPQ3osMh1gadR2ERBU5lR3omy9B6iyigWvouCv/x9PMdA8K3ksNZToR9QCGk+q0q0NQbkBbp4Pm+EieoIV6bWv2n1Cxnru3eIh7W2cXJVXIH1rjaC0Y5JNDCPrrKZLbI5tmDLUoftgjGlNv3+cGo7Jn9pIQpHKm8jO0oaHW84ELwuB2whM82Qgtfc3/XZQKBgQD3OGl99ePcV37c1sbiNs4b/9N/CoPYNXnzL8pbh628HOxjSTFttnPLtodBCS06jNCVuC/09yJ+x9IBpVFnvvACNIsn+JWBHkC0uiAAffZAZizSdw2VOISL4m3uRxxB606tecEzWYalH330pX5h+tVdAI89KAuN7Toq8N2gvLEmmwKBgQDg5b+d+931pJDp04mMsCpPsR7czbC4+J71siu355vxFKeAy7A0aU+t2Ha3NcKfPjLf2msMhb9saQpZIyfFEB/lru/xITLO8gKUtDNqL8wVmRCYDdBjoqsBYVWeJ16VrmsJxGtBds5/k6thOuB5lMmzZA/o7NanQZGpLXIv/SQqKwKBgGImSGOU0uEw75H3x0AaaBvfpnUilJOrL35oM9vA8y3jAcqtTW10aWawUeR1IzrCmzFFzexgjZZt+KdknfwlZx5JBBJbc+euPhxCZOAtIi4RIqxwTbk3lg//VouqOcLGDBd9ZkHfwkAeT6Bob9OwhTMGPNgX5PZ0/4n61NKy91YPAoGAZqERj3PHt3hQ1RO+mmAt/s3yKosHEXILgE29W6Aq7pohUkohb9l7DfdRPYwf83F73+GZpk5Gq59HpKPBW1FfbP4m6t/egse328pcCCIlvNlr1CfBqCYsaWXUn2aR6DZ/DCcBVw9zbo6hz64BkBoQ7XdFN2MVFaB7azuc9z9KyJ8CgYAbY0gSasGh3ta8FDqWlklxKkDQPaWxwozad7YT5fBcLjU5tczjL5LIUj2bPk0qWSupPRdt2k4hXr+JU7tPvJLhgeoj9AU3k+S4D0cRbUhiD21M58QsDbm5gNgcVCwcAxPH+ezi5IBREIgUaLbOBfy04lWI8VKzAwT05zFAzcspdw==";

	// 商户公钥--测试验签用到，正常情况下无用
	private static String publicKey = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA2S860Dxcqq7yR3qEgj+RjU8rjgi2Wlgd+969yqS7ujzPtjZTvlzDTnIigaU02quQZ6dfC1UrKX/MdVKgeLS4vqhxLKgeq9iffz8841nUPt0VbvxdT2ZWjr0WlU3RcMmsAIW6WkKzE3JfDwl025r9zyVmiTDdRVtg7WQMUrgrcEtRxHZs1r6YvbIF3dgaBiH8BfRSk5M9rCEQbqB4tGPwtgVZMbXlLC6q8U/4ivyoeOBIHPJsJeCiY4EobxHlRx8vuwEREMSx6ZCujhGq5nYoPv3BZw4EXNCsvYC/NBu1bgR3yt5UHcesJagbVeDQPfKc6xVTJ+VeE6xj3VF6XuLqCQIDAQAB";

	// 平台公钥
	private static String ptPublicKey = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCmfFoTkK3COrp5HkvO5Z2Je2xVxqTkOzB0flbPc+w/6+gAZ49ko4/41PTMeQ8zzyTh8ZmAXCOHjx1ZoNrm5usB1qFTHPa9le2CWfHSVU4gKWOF6E+hYY3nRxoaAJaKlNRhRhCeCZpG31azyB00iBBqdnepu7sXAA/MTiq+59BReQIDAQAB";

	// 签名类型
	private static String algorithm = "SHA1WithRSA"; // SHA1WithRSA，SHA256WithRSA
	// 签名类型
	private static String sign_type = "RSA"; // RSA，RSA2 与上面两个参数同步修改

	/**
	 * 支付最终请求接口
	 * 
	 * @param requestMap
	 * @return
	 */
	public static Map<String, Object> sendPost(String service, Map<String, String> requestMap) throws Exception {
		NetUtil netUtil = new NetUtil();
		String randstr = PaymentUtil.createRandom(false, 32); // 生成32位随机字符串

		requestMap.put("partner_id", partner_id); // 商户签约PID
		requestMap.put("service", service); // 接口名称
		requestMap.put("sign_type", sign_type); // 签名方式
		requestMap.put("rand_str", randstr); // 随机字符串
		requestMap.put("version", "v1"); // 版本号v1
		requestMap.put("merchant_no", merchant_no); // 商户号
		String data = Tools.getUrlParamsByMap(new TreeMap<String, Object>(requestMap)) // 待提交的数据
				.replaceAll("\\\\/", "/").replaceAll("\\\\\\\\", "\\\\");
		String sign = RSAUtils.rsaSign(data, privateKey, "utf-8", algorithm); // 加密数据
		LOG.info("verify====>" + RSAUtils.doCheck(data, sign, publicKey, "utf-8", algorithm)); // 测试签名是否有问题

		LOG.info("签名====>" + sign);
		requestMap.put("sign", sign); // 把签名加入到参数
		
		LOG.info("请求报文 requestMap======" + requestMap);
		String responseMsg = netUtil.sendPost(apiUrl, 30000, requestMap, "utf-8"); // 请求接口
		LOG.info(service + "====>responseMsg====>" + responseMsg);
		Map<String, Object> responseMap = Serialize.parseJsonToMap(responseMsg);
		LOG.info(Serialize.toJosnDate(responseMap)); // 打印返回数据
		if ("0".equals(responseMap.get("errcode").toString())) { // 返回0再验签
			Map<String, String> responseData = (Map<String, String>) responseMap.get("data"); // 取出返回data，用于验签
			String responseParam = Tools.getUrlParamsByMap(new TreeMap<String, Object>(responseData)); // 排序data并转换成a=1&b=2
			Boolean isVerify = RSAUtils.doCheck(responseParam, String.valueOf(responseMap.get("sign")), ptPublicKey,
					"utf-8", algorithm); // 验签
			if (isVerify) {
				LOG.info("签名验证成功！");
				return responseMap;
			} else {
				LOG.info("签名验证失败！");
				return null;
			}
		} else {
			return responseMap;
		}
	}

	/**
	 * 发送下单
	 * 
	 * @param requestMap
	 * @return
	 * @throws Exception
	 */
	public static Map<String, Object> sendDirectPayPost(Map<String, String> requestMap) throws Exception {
		return sendPost(Const.DIRECT_PAY_TRADE, requestMap);
	}

	/**
	 * 发送下单查询
	 * 
	 * @param requestMap
	 * @return
	 * @throws Exception
	 */
	public static Map<String, Object> sendDirectQueryPost(Map<String, String> requestMap) throws Exception {
		return sendPost(Const.OPERATEORDER_VIEW, requestMap);
	}

	/**
	 * 发送代付
	 * 
	 * @param requestMap
	 * @return
	 * @throws Exception
	 */
	public static Map<String, Object> sendProxyPayPost(Map<String, String> requestMap) throws Exception {
		return sendPost(Const.PROXY_PAY, requestMap);
	}

	/**
	 * 发送代付查询
	 * 
	 * @param requestMap
	 * @return
	 * @throws Exception
	 */
	public static Map<String, Object> sendProxyQueryPost(Map<String, String> requestMap) throws Exception {
		return sendPost(Const.PROXY_QUERY, requestMap);
	}

	/**
	 * 发送可用金额查询
	 * 
	 * @param requestMap
	 * @return
	 * @throws Exception
	 */
	public static Map<String, Object> sendBalanceQueryPost(Map<String, String> requestMap) throws Exception {
		return sendPost(Const.BALANCE_QUERY, requestMap);
	}

	/**
	 * 发送汇率查询
	 * 
	 * @param requestMap
	 * @return
	 * @throws Exception
	 */
	public static Map<String, Object> sendRateQueryPost(Map<String, String> requestMap) throws Exception {
		return sendPost(Const.RATE_QUERY, requestMap);
	}
	
	/**
	 * 中文数据encode
	 * 
	 * @param gbString
	 * @return
	 */
	public static String gbEncoding(final String gbString) { // gbString = "测试"
		char[] utfBytes = gbString.toCharArray(); // utfBytes = [测, 试]
		String unicodeBytes = "";
		for (int byteIndex = 0; byteIndex < utfBytes.length; byteIndex++) {
			String hexB = Integer.toHexString(utfBytes[byteIndex]); // 转换为16进制整型字符串
			if (hexB.length() <= 2) {
				hexB = "00" + hexB;
			}
			unicodeBytes = unicodeBytes + "\\u" + hexB;
		}
		System.out.println("unicodeBytes is: " + unicodeBytes);
		return unicodeBytes;
	}

	/**
	 * 创建指定数量的随机字符串
	 * 
	 * @param numberFlag
	 *            是否是数字
	 * @param length
	 * @return
	 */
	public static String createRandom(boolean numberFlag, int length) {
		String retStr = "";
		String strTable = numberFlag ? "1234567890" : "1234567890abcdefghijkmnpqrstuvwxyz";
		int len = strTable.length();
		boolean bDone = true;
		do {
			retStr = "";
			int count = 0;
			for (int i = 0; i < length; i++) {
				double dblR = Math.random() * len;
				int intR = (int) Math.floor(dblR);
				char c = strTable.charAt(intR);
				if (('0' <= c) && (c <= '9')) {
					count++;
				}
				retStr += strTable.charAt(intR);
			}
			if (count >= 2) {
				bDone = false;
			}
		} while (bDone);
		return retStr;
	}
}
