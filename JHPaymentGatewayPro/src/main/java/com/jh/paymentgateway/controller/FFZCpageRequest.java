package com.jh.paymentgateway.controller;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.URI;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestTemplate;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.jh.paymentgateway.basechannel.BaseChannel;
import com.jh.paymentgateway.business.TopupPayChannelBusiness;
import com.jh.paymentgateway.common.ChannelUtils;
import com.jh.paymentgateway.config.RedisUtil;
import com.jh.paymentgateway.pojo.BankNumCode;
import com.jh.paymentgateway.pojo.FFZCBindCard;
import com.jh.paymentgateway.pojo.FFZCRegister;
import com.jh.paymentgateway.pojo.PaymentRequestParameter;
import com.jh.paymentgateway.util.ffzc.Base64;
import com.jh.paymentgateway.util.ffzc.Des3Encryption;
import com.jh.paymentgateway.util.ffzc.HttpUtil;
import com.jh.paymentgateway.util.ffzc.Md5Util;
import com.jh.paymentgateway.util.ffzc.QuickPayUtil;

import cn.jh.common.tools.Log;
import cn.jh.common.utils.CommonConstants;
import cn.jh.common.utils.ExceptionUtil;

@Controller
@EnableAutoConfiguration
public class FFZCpageRequest extends BaseChannel {

	@Autowired
	private RedisUtil redisUtil;

	@Autowired
	private TopupPayChannelBusiness topupPayChannelBusiness;

	@Value("${payment.ipAddress}")
	private String ip;

	private static final Logger LOG = LoggerFactory.getLogger(FFZCpageRequest.class);

	private static String signKey = "922gp16Xob99Hdl36g923QaWBB371S0u"; // 加密秘钥
	private static String desKey = "u92149F402kl5As71y4YlF04"; // 卡号加密秘钥
	private static String channelName = "上海复规网络信息有限公司"; // 代理商名称
	private static String channelNo = "C2544328796"; // 代理商编号

	/**
	 * 商户入网
	 * 
	 * @return
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/quick/ffzc/register")
	public @ResponseBody Object register(@RequestParam(value = "orderCode") String orderCode) throws IOException {

		LOG.info("============ 进入商户入网接口 ============");

		PaymentRequestParameter prp = redisUtil.getPaymentRequestParameter(orderCode);

		String cardName = prp.getDebitBankName();
		String userName = prp.getUserName();
		String phoneD = prp.getDebitPhone();
		String rate = prp.getRate();
		String idCard = prp.getIdCard();
		String cardNo = prp.getDebitCardNo();
		String bankCard = prp.getBankCard();
		String ExtraFee = prp.getExtraFee();
		String rip = prp.getIpAddress();
		String bankName = prp.getDebitBankName();
		String phoneC = prp.getCreditCardPhone();

		Map<String, Object> maps = new HashMap<String, Object>();

		FFZCRegister ffzcRegister = topupPayChannelBusiness.getFFZCRegisterByIdCard(idCard);
		FFZCBindCard ffzcBindCard = topupPayChannelBusiness.getFFZCBindCardByBankCard(bankCard);
		if (ffzcRegister == null) {
			JSONObject reqJson = new JSONObject(); // 请注意fastjson的版本，需要用自带排序功能的

			reqJson.put("channelName", channelName);// 渠道名称，请用分配的代理商名称
			reqJson.put("channelNo", channelNo);// 渠道编码，请用分配的代理商号
			reqJson.put("merchantName", userName); // 如果bankType为TOPUBLIC，则legalPersonName与merchantName必须一致
			reqJson.put("merchantBillName", "快捷支付");
			reqJson.put("installProvince", "上海");
			reqJson.put("installCity", "上海市");
			reqJson.put("installCounty", "宝山区");
			reqJson.put("operateAddress", "上海市宝山区");
			reqJson.put("merchantType", "PERSON");// PERSON -个人商户
			reqJson.put("isOneOrBig", "Y");// 填Y
			reqJson.put("legalPersonName", userName); // 法人姓名，如果bankType为TOPRIVATE，则legalPersonName与accountName必须一致,
														// 如果bankType为TOPUBLIC，则legalPersonName与merchantName必须一致
			reqJson.put("legalPersonID", idCard); // 法人身份证
			reqJson.put("merchantPersonType", "AGENT"); // 联系人类型
														// LEGAL_PERSON：法人；
														// CONTROLLER：实际控制人；
														// AGENT：代理人；OTHER：其他
			reqJson.put("merchantPersonName", userName);
			reqJson.put("merchantPersonPhone", phoneD);
			reqJson.put("wxType", "158"); // 请参考附录 支付宝类别码
			reqJson.put("wxT1Fee", rate);
			reqJson.put("wxT0Fee", rate);
			reqJson.put("alipayType", "2015062600002758"); // 请参考附录 支付宝类别码
			reqJson.put("alipayT1Fee", rate);
			reqJson.put("alipayT0Fee", rate);
			reqJson.put("province_code", "310000");//
			reqJson.put("city_code", "310100");// 支付宝市代码
			reqJson.put("district_code", "310113");// 支付宝地区代码
			reqJson.put("bankType", "TOPRIVATE");
			reqJson.put("accountName", userName); // 如果bankType为TOPRIVATE，则legalPersonName与accountName必须一致

			String cardno = Des3Encryption.encode(desKey, cardNo);// 到账卡
			reqJson.put("accountNo", cardno);

			reqJson.put("bankName", cardName);
			reqJson.put("bankProv", "上海市");
			reqJson.put("bankCity", "上海市");
			reqJson.put("bankBranch", "上海市宝山区支行");

			// 获取银行联行号
			BankNumCode bcode = topupPayChannelBusiness.getBankNumCodeByBankName(cardName);
			String bankChannelNo = bcode.getBankBranchcode();// 支行号
			String bankAbbr = bcode.getBankCode();// 缩写
			String bankCode = bcode.getBankNum();// 编号

			reqJson.put("bankCode", bankChannelNo);

			// bankCard = Des3Encryption.encode(desKey, bankCard);
			// reqJson.put("creditCardNo", bankCard);// 结算人信用卡

			reqJson.put("remarks", "C2544328796"); // 商户平台全局唯一，并且需要保存，建议填写商户平台商户号,此参数在商户秘钥查询接口中必填

			String sourceBody = reqJson.toJSONString();
			LOG.info("签名体：" + sourceBody);
			String requestSign = Md5Util.MD5(sourceBody + signKey);
			reqJson.put("sign", requestSign);
			String requestStr = reqJson.toJSONString();
			LOG.info("商户入网请求报文：" + requestStr);

			String url = "https://pay.feifanzhichuang.com/middlepayportal/merchant/in2";
			String respStr = HttpUtil.sendPost(url, requestStr, HttpUtil.CONTENT_TYPE_JSON);

			LOG.info("商户入网响应报文：" + respStr);
			JSONObject respJson = JSON.parseObject(respStr);

			// 去掉签名，md5加密，比对是否正确
			String respSign = (String) respJson.remove("sign");
			// 再次加密
			String generSign = Md5Util.MD5(respJson.toString() + signKey);
			LOG.info("验签结果：" + generSign.equals(respSign));
			// TODO 执行接下来的业务逻辑，写库，保存下发的3个秘钥，balabalabala

			String respCode = respJson.getString("respCode");
			String respMsg = respJson.getString("respMsg");
			String merchantNo = respJson.getString("merchantNo");
			String signKey = respJson.getString("signKey");
			String desKey = respJson.getString("desKey");
			String queryKey = respJson.getString("queryKey");
			if ("0000".equals(respCode)) {
				LOG.info("入网成功：" + respMsg);
				LOG.info("----------  ffzc保存新用户数据  ----------");
				FFZCRegister fFZCRegister = new FFZCRegister();
				fFZCRegister.setIdCard(idCard);
				fFZCRegister.setPhone(phoneD);
				fFZCRegister.setRate(rate);
				fFZCRegister.setExtraFee(ExtraFee);
				fFZCRegister.setBankCard(cardNo);// 到账卡
				fFZCRegister.setMerchantNo(merchantNo);
				fFZCRegister.setSignKey(signKey);
				fFZCRegister.setDesKey(desKey);
				fFZCRegister.setQueryKey(queryKey);

				topupPayChannelBusiness.createFFZCRegister(fFZCRegister);

				maps = (Map<String, Object>) addRate(orderCode);
				String rateReapCode = (String) maps.get("resp_code");
				if ("000000".equals(rateReapCode)) {
					LOG.info("----------   新增费率成功，跳转绑卡 ----------");
					maps = (Map<String, Object>) bindCard(orderCode);
					String respCode1 = (String) maps.get("resp_code");
					String respMsg1 = (String) maps.get("resp_message");
					String r3_url = (String) maps.get("r3_url");
					if ("000000".equals(respCode1)) {
						LOG.info("----------  新增费率成功，跳转第三方绑卡页面  ----------");

						maps.put(CommonConstants.RESULT, r3_url);
						maps.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
						maps.put(CommonConstants.RESP_MESSAGE, respMsg1);
						return maps;
					}else{
						LOG.info("----------  申请绑卡失败  ----------");
						
						maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
						maps.put(CommonConstants.RESP_MESSAGE, rateReapCode);
						return maps;
					}
					

				} else {
					LOG.info("----------  新增费率失败  ----------");

					maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
					maps.put(CommonConstants.RESP_MESSAGE, rateReapCode);
					return maps;
				}

			} else {
				LOG.info("入网失败：" + respMsg);

				this.addOrderCauseOfFailure(orderCode, respMsg, rip);

				maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
				maps.put(CommonConstants.RESP_MESSAGE, respMsg);
				return maps;
			}
		} else if (!ffzcRegister.getRate().equals(rate)) {
			LOG.info("----------  已注册用户，费率不同   ----------");

			maps = (Map<String, Object>) changeRate(orderCode);
			String respCode = (String) maps.get("resp_code");
			String respMsg = (String) maps.get("resp_message");
			if ("000000".equals(respCode)) {
				LOG.info("----------  修改费率成功   ----------");

				if (!ffzcRegister.getBankCard().equals(cardNo)) {
					LOG.info("----------  修改费率成功，结算卡不同   ----------");

					maps = (Map<String, Object>) changeUserInfo(orderCode);
					String respCode2 = (String) maps.get("resp_code");
					String respMsg2 = (String) maps.get("resp_message");
					if ("000000".equals(respCode2)) {
						LOG.info("----------  修改费率成功，修改结算卡成功   ----------");

						if (ffzcBindCard == null) {
							LOG.info("----------  修改费率成功，修改结算卡成功，未绑卡  ----------");
							maps = (Map<String, Object>) bindCard(orderCode);
							String respCode1 = (String) maps.get("resp_code");
							String respMsg1 = (String) maps.get("resp_message");
							String r3_url = (String) maps.get("r3_url");
							if ("000000".equals(respCode1)) {
								LOG.info("----------  修改费率成功，修改结算卡成功，跳转第三方绑卡页面  ----------");

								maps.put(CommonConstants.RESULT, r3_url);
								maps.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
								maps.put(CommonConstants.RESP_MESSAGE, respMsg1);
								return maps;
							} else {

								maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
								maps.put(CommonConstants.RESP_MESSAGE, respMsg1);
								return maps;
							}
						} else {
							LOG.info("----------  修改费率成功，修改结算卡成功，直接交易  ----------");
							maps = (Map<String, Object>) beginPay(orderCode);
							String respCode1 = (String) maps.get("resp_code");
							String respMsg1 = (String) maps.get("resp_message");
							String requestId = (String) maps.get("r3_url");
							if ("000000".equals(respCode1)) {
								LOG.info("----------  跳转预交易页面  ----------");

								maps.put(CommonConstants.RESULT,
										ip + "/v1.0/paymentgateway/quick/ffzc/pay-view?bankName="
												+ URLEncoder.encode(bankName, "UTF-8") + "&bankCard=" + cardNo
												+ "&orderCode=" + orderCode + "&ipAddress=" + ip + "&ips="
												+ prp.getIpAddress() + "&phone=" + phoneC + "&requestId=" + requestId
												+ "&isRegister=1");
								maps.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
								maps.put(CommonConstants.RESP_MESSAGE, "成功");
								return maps;
							} else {

								maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
								maps.put(CommonConstants.RESP_MESSAGE, respMsg1);
								return maps;
							}
						}
					} else {
						LOG.info("----------  修改结算卡失败   ----------");

						maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
						maps.put(CommonConstants.RESP_MESSAGE, respMsg2);
						return maps;
					}
				} else if (ffzcBindCard == null) {
					LOG.info("----------  修改费率成功 ，未绑卡  ----------");
					maps = (Map<String, Object>) bindCard(orderCode);
					String respCode1 = (String) maps.get("resp_code");
					String respMsg1 = (String) maps.get("resp_message");
					String r3_url = (String) maps.get("r3_url");
					if ("000000".equals(respCode1)) {
						LOG.info("----------  修改费率成功 ，跳转第三方绑卡页面  ----------");

						maps.put(CommonConstants.RESULT, r3_url);
						maps.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
						maps.put(CommonConstants.RESP_MESSAGE, respMsg1);
						return maps;
					} else {

						maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
						maps.put(CommonConstants.RESP_MESSAGE, respMsg1);
						return maps;
					}
				} else {
					LOG.info("----------  修改费率成功 ，直接交易  ----------");
					maps = (Map<String, Object>) beginPay(orderCode);
					String respCode1 = (String) maps.get("resp_code");
					String respMsg1 = (String) maps.get("resp_message");
					String requestId = (String) maps.get("r3_url");
					if ("000000".equals(respCode1)) {
						LOG.info("----------  跳转预交易页面  ----------");

						maps.put(CommonConstants.RESULT,
								ip + "/v1.0/paymentgateway/quick/ffzc/pay-view?bankName="
										+ URLEncoder.encode(bankName, "UTF-8") + "&bankCard=" + cardNo + "&orderCode="
										+ orderCode + "&ipAddress=" + ip + "&ips=" + prp.getIpAddress() + "&phone="
										+ phoneC + "&requestId=" + requestId + "&isRegister=1");
						maps.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
						maps.put(CommonConstants.RESP_MESSAGE, "成功");
						return maps;
					} else {

						maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
						maps.put(CommonConstants.RESP_MESSAGE, respMsg1);
						return maps;
					}
				}
			} else {
				LOG.info("----------  修改费率失败   ----------");

				maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
				maps.put(CommonConstants.RESP_MESSAGE, respMsg);
				return maps;
			}
		} else if (!ffzcRegister.getBankCard().equals(cardNo)) {
			LOG.info("----------  已注册用户，结算卡不同   ----------");

			maps = (Map<String, Object>) changeUserInfo(orderCode);
			String respCode2 = (String) maps.get("resp_code");
			String respMsg2 = (String) maps.get("resp_message");
			if ("000000".equals(respCode2)) {
				LOG.info("----------  修改结算卡成功   ----------");

				if (ffzcBindCard == null) {
					LOG.info("----------  修改结算卡成功，未绑卡  ----------");
					maps = (Map<String, Object>) bindCard(orderCode);
					String respCode1 = (String) maps.get("resp_code");
					String respMsg1 = (String) maps.get("resp_message");
					String r3_url = (String) maps.get("r3_url");
					if ("000000".equals(respCode1)) {
						LOG.info("----------  修改结算卡成功，跳转第三方绑卡页面  ----------");

						maps.put(CommonConstants.RESULT, r3_url);
						maps.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
						maps.put(CommonConstants.RESP_MESSAGE, respMsg1);
						return maps;
					} else {

						maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
						maps.put(CommonConstants.RESP_MESSAGE, respMsg1);
						return maps;
					}
				} else {
					LOG.info("----------  修改结算卡成功，直接交易  ----------");
					maps = (Map<String, Object>) beginPay(orderCode);
					String respCode1 = (String) maps.get("resp_code");
					String respMsg1 = (String) maps.get("resp_message");
					String requestId = (String) maps.get("r3_url");
					if ("000000".equals(respCode1)) {
						LOG.info("----------  修改结算卡成功，跳转预交易页面  ----------");

						maps.put(CommonConstants.RESULT,
								ip + "/v1.0/paymentgateway/quick/ffzc/pay-view?bankName="
										+ URLEncoder.encode(bankName, "UTF-8") + "&bankCard=" + cardNo + "&orderCode="
										+ orderCode + "&ipAddress=" + ip + "&ips=" + prp.getIpAddress() + "&phone="
										+ phoneC + "&requestId=" + requestId + "&isRegister=1");
						maps.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
						maps.put(CommonConstants.RESP_MESSAGE, "成功");
						return maps;
					} else {

						maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
						maps.put(CommonConstants.RESP_MESSAGE, respMsg1);
						return maps;
					}
				}
			} else {
				LOG.info("----------  修改结算卡失败   ----------");

				maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
				maps.put(CommonConstants.RESP_MESSAGE, respMsg2);
				return maps;
			}
		} else if (ffzcBindCard == null) {
			LOG.info("----------  已注册用户，未绑卡  ----------");
			maps = (Map<String, Object>) bindCard(orderCode);
			String respCode1 = (String) maps.get("resp_code");
			String respMsg1 = (String) maps.get("resp_message");
			String r3_url = (String) maps.get("r3_url");
			if ("000000".equals(respCode1)) {
				LOG.info("----------  跳转第三方绑卡页面  ----------");

				maps.put(CommonConstants.RESULT, r3_url);
				maps.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
				maps.put(CommonConstants.RESP_MESSAGE, respMsg1);
				return maps;
			} else {

				maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
				maps.put(CommonConstants.RESP_MESSAGE, respMsg1);
				return maps;
			}
		} else {
			LOG.info("----------  已注册，已绑卡，直接交易  ----------");
			maps = (Map<String, Object>) beginPay(orderCode);
			String respCode1 = (String) maps.get("resp_code");
			String respMsg1 = (String) maps.get("resp_message");
			String requestId = (String) maps.get("r3_url");
			if ("000000".equals(respCode1)) {
				LOG.info("----------  跳转预交易页面  ----------");

				maps.put(CommonConstants.RESULT,
						ip + "/v1.0/paymentgateway/quick/ffzc/pay-view?bankName=" + URLEncoder.encode(bankName, "UTF-8")
								+ "&bankCard=" + cardNo + "&orderCode=" + orderCode + "&ipAddress=" + ip + "&ips="
								+ prp.getIpAddress() + "&phone=" + phoneC + "&requestId=" + requestId
								+ "&isRegister=1");
				maps.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
				maps.put(CommonConstants.RESP_MESSAGE, "成功");
				return maps;
			} else {

				maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
				maps.put(CommonConstants.RESP_MESSAGE, respMsg1);
				return maps;
			}
		}
	}

	/**
	 * 新增费率
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/quick/ffzc/addRate")
	public @ResponseBody Object addRate(@RequestParam(value = "orderCode") String orderCode) throws IOException {
		LOG.info("----------  进件成功，新增费率  ----------");

		PaymentRequestParameter prp = redisUtil.getPaymentRequestParameter(orderCode);

		String rate = prp.getRate();
		String rip = prp.getIpAddress();
		String idCard = prp.getIdCard();

		FFZCRegister ffzcRegister = topupPayChannelBusiness.getFFZCRegisterByIdCard(idCard);
		String merchantNo = ffzcRegister.getMerchantNo();

		Map<String, Object> maps = new HashMap<String, Object>();

		JSONObject reqJson1 = new JSONObject(); // 请注意fastjson的版本，需要用自带排序功能的

		reqJson1.put("channelName", channelName);// 渠道名称，请用分配的代理商名称
		reqJson1.put("channelNo", channelNo);// 渠道编码，请用分配的代理商号
		reqJson1.put("merchantNo", merchantNo);// 渠道编码，请用分配的商户编号
		reqJson1.put("productType", "QUICKPAY");// 产品类型（银联二维码）
		reqJson1.put("t0Fee", rate);
		reqJson1.put("t1Fee", rate);

		LOG.info("签名体：" + reqJson1.toJSONString());
		String sign = Md5Util.MD5(reqJson1.toJSONString() + signKey);
		reqJson1.put("sign", sign);
		String requestStr1 = reqJson1.toJSONString();
		LOG.info("商户添加费率请求报文：" + requestStr1);

		String url1 = "https://pay.feifanzhichuang.com/middlepayportal/merchant/addFee";
		String respStr1 = HttpUtil.sendPost(url1, requestStr1, HttpUtil.CONTENT_TYPE_JSON);

		LOG.info("商户费率添加响应报文：" + respStr1);
		com.alibaba.fastjson.JSONObject respJson1 = JSON.parseObject(respStr1);

		// 去掉签名，md5加密，比对是否正确
		String respSign1 = (String) respJson1.remove("sign");
		// 再次加密
		String generSign1 = Md5Util.MD5(respJson1.toString() + signKey);
		LOG.info("验签结果：" + generSign1.equals(respSign1));

		String respCode1 = respJson1.getString("respCode");
		String respMsg1 = respJson1.getString("respMsg");

		if ("0000".equals(respCode1)) {
			LOG.info("----------  商户添加费率成功  ----------");

			maps.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
			maps.put(CommonConstants.RESP_MESSAGE, respMsg1);
			return maps;
		} else if ("3104".equals(respCode1)) {
			LOG.info("----------  t0Fee成功,t1Fee失败  ----------");

			this.addOrderCauseOfFailure(orderCode, "t0Fee费率添加成功,t1Fee费率添加失败", rip);

			maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			maps.put(CommonConstants.RESP_MESSAGE, "t0Fee费率添加成功,t1Fee费率添加失败");
			return maps;
		} else if ("3105".equals(respCode1)) {
			LOG.info("----------  t1Fee成功,t0Fee失败  ----------");

			this.addOrderCauseOfFailure(orderCode, "t1Fee费率添加成功,t0Fee费率添加失败", rip);

			maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			maps.put(CommonConstants.RESP_MESSAGE, "t1Fee费率添加成功,t0Fee费率添加失败");
			return maps;
		} else {
			LOG.info("商户添加费率失败：" + respMsg1);

			this.addOrderCauseOfFailure(orderCode, respMsg1, rip);

			maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			maps.put(CommonConstants.RESP_MESSAGE, respMsg1);
			return maps;
		}
	}

	/**
	 * 费率修改
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/quick/ffzc/changeRate")
	public @ResponseBody Object changeRate(@RequestParam(value = "orderCode") String orderCode) throws IOException {
		LOG.info("----------  进件修改费率   ----------");

		PaymentRequestParameter prp = redisUtil.getPaymentRequestParameter(orderCode);

		String rate = prp.getRate();
		String rip = prp.getIpAddress();
		String idCard = prp.getIdCard();

		FFZCRegister ffzcRegister = topupPayChannelBusiness.getFFZCRegisterByIdCard(idCard);
		String merchantNo = ffzcRegister.getMerchantNo();

		Map<String, Object> maps = new HashMap<String, Object>();

		JSONObject reqJson1 = new JSONObject(); // 请注意fastjson的版本，需要用自带排序功能的

		reqJson1.put("channelName", channelName);// 渠道名称，请用分配的代理商名称
		reqJson1.put("channelNo", channelNo);// 渠道编码，请用分配的代理商号
		reqJson1.put("merchantNo", merchantNo);// 渠道编码，请用分配的商户编号
		reqJson1.put("productType", "QUICKPAY");// 产品类型（银联二维码）
		reqJson1.put("t0Fee", rate);
		reqJson1.put("t1Fee", rate);

		LOG.info("签名体：" + reqJson1.toJSONString());
		String sign = Md5Util.MD5(reqJson1.toJSONString() + signKey);
		reqJson1.put("sign", sign);
		String requestStr1 = reqJson1.toJSONString();
		LOG.info("商户修改费率请求报文：" + requestStr1);

		String url1 = "https://pay.feifanzhichuang.com/middlepayportal/merchant/modifyProductFee";
		String respStr1 = HttpUtil.sendPost(url1, requestStr1, HttpUtil.CONTENT_TYPE_JSON);

		LOG.info("商户费率修改响应报文：" + respStr1);
		com.alibaba.fastjson.JSONObject respJson1 = JSON.parseObject(respStr1);

		// 去掉签名，md5加密，比对是否正确
		String respSign1 = (String) respJson1.remove("sign");
		// 再次加密
		String generSign1 = Md5Util.MD5(respJson1.toString() + signKey);
		LOG.info("验签结果：" + generSign1.equals(respSign1));

		String respCode1 = respJson1.getString("respCode");
		String respMsg1 = respJson1.getString("respMsg");

		if ("0000".equals(respCode1)) {
			LOG.info("----------  修改费率成功  ----------");

			ffzcRegister.setRate(rate);
			topupPayChannelBusiness.createFFZCRegister(ffzcRegister);

			maps.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
			maps.put(CommonConstants.RESP_MESSAGE, respMsg1);
			return maps;
		} else {
			LOG.info("修改费率失败：" + respMsg1);

			this.addOrderCauseOfFailure(orderCode, respMsg1, rip);

			maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			maps.put(CommonConstants.RESP_MESSAGE, respMsg1);
			return maps;
		}
	}

	/**
	 * 商户资料修改
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/quick/ffzc/changeUserInfo")
	public @ResponseBody Object changeUserInfo(@RequestParam(value = "orderCode") String orderCode) throws IOException {
		LOG.info("----------  进件商户资料修改   ----------");

		PaymentRequestParameter prp = redisUtil.getPaymentRequestParameter(orderCode);

		String rate = prp.getRate();
		String rip = prp.getIpAddress();
		String idCard = prp.getIdCard();
		String cardName = prp.getDebitBankName();
		String cardNo = prp.getDebitCardNo();

		FFZCRegister ffzcRegister = topupPayChannelBusiness.getFFZCRegisterByIdCard(idCard);
		String merchantNo = ffzcRegister.getMerchantNo();
		String oriAccountNo = ffzcRegister.getBankCard();

		Map<String, Object> maps = new HashMap<String, Object>();

		JSONObject reqJson1 = new JSONObject(); // 请注意fastjson的版本，需要用自带排序功能的

		reqJson1.put("channelName", channelName);// 渠道名称，请用分配的代理商名称
		reqJson1.put("channelNo", channelNo);// 渠道编码，请用分配的代理商号
		reqJson1.put("merchantNo", merchantNo);// 渠道编码，请用分配的商户编号

		String cardno = Des3Encryption.encode(desKey, cardNo);// 到账卡
		reqJson1.put("accountNo", cardno);

		oriAccountNo = Des3Encryption.encode(desKey, oriAccountNo);// 原到账卡
		reqJson1.put("oriAccountNo", oriAccountNo);

		reqJson1.put("bankBranch", "上海市宝山区支行");
		// 获取银行联行号
		BankNumCode bcode = topupPayChannelBusiness.getBankNumCodeByBankName(cardName);
		String bankChannelNo = bcode.getBankBranchcode();// 支行号
		reqJson1.put("bankCode", bankChannelNo);

		reqJson1.put("bankName", cardName);
		reqJson1.put("bankCity", "上海市");
		reqJson1.put("bankType", "TOPRIVATE");
		reqJson1.put("bankProv", "上海市");
		/*
		 * reqJson1.put("wxT0Fee", rate); reqJson1.put("wxT1Fee", rate);
		 * reqJson1.put("alipayT1Fee", rate); reqJson1.put("alipayT0Fee", rate);
		 */
		reqJson1.put("changeType", "1");// 1 : 变更结算信息（参数4-11必传）

		LOG.info("签名体：" + reqJson1.toJSONString());
		String sign = Md5Util.MD5(reqJson1.toJSONString() + signKey);
		reqJson1.put("sign", sign);
		String requestStr1 = reqJson1.toJSONString();
		LOG.info("商户资料修改请求报文：" + requestStr1);

		String url1 = "https://pay.feifanzhichuang.com/middlepayportal/merchant/modify";
		String respStr1 = HttpUtil.sendPost(url1, requestStr1, HttpUtil.CONTENT_TYPE_JSON);

		LOG.info("商户资料修改响应报文：" + respStr1);
		com.alibaba.fastjson.JSONObject respJson1 = JSON.parseObject(respStr1);

		// 去掉签名，md5加密，比对是否正确
		String respSign1 = (String) respJson1.remove("sign");
		// 再次加密
		String generSign1 = Md5Util.MD5(respJson1.toString() + signKey);
		LOG.info("验签结果：" + generSign1.equals(respSign1));

		String respCode1 = respJson1.getString("respCode");
		String respMsg1 = respJson1.getString("respMsg");

		if ("0000".equals(respCode1)) {
			LOG.info("----------  商户资料修改成功  ----------");

			ffzcRegister.setBankCard(cardNo);
			topupPayChannelBusiness.createFFZCRegister(ffzcRegister);

			maps.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
			maps.put(CommonConstants.RESP_MESSAGE, respMsg1);
			return maps;
		} else {
			LOG.info("商户资料修改失败：" + respMsg1);

			this.addOrderCauseOfFailure(orderCode, respMsg1, rip);

			maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			maps.put(CommonConstants.RESP_MESSAGE, respMsg1);
			return maps;
		}
	}

	/**
	 * 申请绑卡
	 * 
	 * @return
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/quick/ffzc/bindCard")
	public @ResponseBody Object bindCard(@RequestParam(value = "orderCode") String orderCode) throws IOException {

		LOG.info("============ 进入申请开通卡接口  ============");

		PaymentRequestParameter prp = redisUtil.getPaymentRequestParameter(orderCode);

		String phoneC = prp.getCreditCardPhone();
		String idCard = prp.getIdCard();
		String amount = prp.getAmount();
		String bankCard = prp.getBankCard();
		String rip = prp.getIpAddress();

		FFZCRegister ffzcRegister = topupPayChannelBusiness.getFFZCRegisterByIdCard(idCard);
		String merchantNo = ffzcRegister.getMerchantNo();
		String signKey = ffzcRegister.getSignKey();
		String desKey = ffzcRegister.getDesKey();

		Map<String, String> reqMap = new LinkedHashMap<String, String>();
		reqMap.put("trxType", "PRE_CREATE_ORDER");
		reqMap.put("merchantNo", merchantNo);
		reqMap.put("token", Des3Encryption.encode(desKey, "00000000000000000000"));//
		LOG.info("加密之后的token是：" + reqMap.get("token"));
		reqMap.put("goodsName", QuickPayUtil.createOrderNum("流行鞋包/服饰/饰品"));// 商品描述
		reqMap.put("serverDfUrl", ip + "/v1.0/paymentgateway/topup/ffzc/bindcard/notify_call");// 开卡异步通知地址
		reqMap.put("callbackUrl", ip + "/v1.0/paymentgateway/topup/ffzc/bindcard/return_call");// 前台地址
		reqMap.put("serverCallbackUrl", ip + "/v1.0/paymentgateway/topup/ffzc/pay/notify_call");// 交易notifyUrl
		reqMap.put("orderNum", orderCode);// 订单号

		reqMap.put("amount", amount);// 订单金额
		reqMap.put("encrypt", "T0");
		reqMap.put("orderIp", "127.0.0.1");

		// 加密卡号
		String cardNo = Des3Encryption.encode(desKey, bankCard);
		LOG.info("加密过后的卡号：" + cardNo);
		reqMap.put("cardNo", cardNo);// 卡号
		reqMap.put("jsonremark", Base64.encode(phoneC.getBytes()).toString());// json字符串
		String sign = QuickPayUtil.generSign(reqMap, signKey);
		reqMap.put("sign", sign);
		System.out.println(reqMap.get("sign").toString());

		LOG.info("申请绑卡提交的数据：" + reqMap.toString());

		String url = "https://pay.feifanzhichuang.com/middlepaytrx/kuaiPayUnique/preCreateOrder";

		Map<String, Object> maps = new HashMap<String, Object>();
		String respCode1 = "";
		String respMsg1 = "";
		String r3_url = "";
		try {
			String respStr1 = HttpUtil.postAndReturnString(reqMap, url);
			LOG.info("申请绑卡返回的报文" + respStr1);
			com.alibaba.fastjson.JSONObject respJson1 = JSON.parseObject(respStr1);

			// 去掉签名，md5加密，比对是否正确
			String respSign1 = (String) respJson1.remove("sign");
			// 再次加密
			String generSign1 = Md5Util.MD5(respJson1.toString() + signKey);
			LOG.info("验签结果：" + generSign1.equals(respSign1));

			respCode1 = respJson1.getString("retCode");
			respMsg1 = respJson1.getString("retMsg");
			r3_url = respJson1.getString("r3_url");

		} catch (Exception e) {
			e.printStackTrace();
		}

		if ("0000".equals(respCode1)) {
			LOG.info("----------  申请开通卡成功   ----------");

			maps.put("r3_url", r3_url);
			maps.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
			maps.put(CommonConstants.RESP_MESSAGE, respMsg1);
			return maps;
		} else {
			LOG.info("申请开通卡失败：" + respMsg1);

			this.addOrderCauseOfFailure(orderCode, respMsg1, rip);

			maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			maps.put(CommonConstants.RESP_MESSAGE, respMsg1);
			return maps;
		}

	}

	/**
	 * 申请交易
	 * 
	 * @return
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/quick/ffzc/beginPay")
	public @ResponseBody Object beginPay(@RequestParam(value = "orderCode") String orderCode) throws IOException {

		LOG.info("============ 进入申请交易接口  ============");

		PaymentRequestParameter prp = redisUtil.getPaymentRequestParameter(orderCode);

		String phoneC = prp.getCreditCardPhone();
		String idCard = prp.getIdCard();
		String amount = prp.getAmount();
		String bankCard = prp.getBankCard();
		String rip = prp.getIpAddress();

		FFZCRegister ffzcRegister = topupPayChannelBusiness.getFFZCRegisterByIdCard(idCard);
		String merchantNo = ffzcRegister.getMerchantNo();
		String signKey = ffzcRegister.getSignKey();
		String desKey = ffzcRegister.getDesKey();
		// 判断卡是否已绑定
		FFZCBindCard ffzcBindCard = topupPayChannelBusiness.getFFZCBindCardByBankCard(bankCard);
		String token = ffzcBindCard.getToken();

		Map<String, String> reqMap = new LinkedHashMap<String, String>();
		reqMap.put("trxType", "PRE_CREATE_ORDER");
		reqMap.put("merchantNo", merchantNo);
		reqMap.put("token", Des3Encryption.encode(desKey, token));//
//		reqMap.put("token", token);//
		LOG.info("加密之后的token是：" + reqMap.get("token"));
		reqMap.put("goodsName", QuickPayUtil.createOrderNum("流行鞋包/服饰/饰品"));// 商品描述
		// reqMap.put("serverDfUrl", ip +
		// "/v1.0/paymentgateway/topup/ffzc/bindcard/notify_call");// 开卡异步通知地址
		reqMap.put("callbackUrl", ip + "/v1.0/paymentgateway/topup/ffzc/bindcard/return_call");// 前台地址
		reqMap.put("serverCallbackUrl", ip + "/v1.0/paymentgateway/topup/ffzc/pay/notify_call");// 交易notifyUrl
		reqMap.put("orderNum", orderCode);// 订单号
		reqMap.put("amount", amount);// 订单金额
		reqMap.put("encrypt", "T0");
		reqMap.put("orderIp", "127.0.0.1");

		// 加密卡号
		String cardNo = Des3Encryption.encode(desKey, bankCard);
		LOG.info("加密过后的卡号：" + cardNo);
		reqMap.put("cardNo", cardNo);// 卡号
		reqMap.put("jsonremark", Base64.encode(phoneC.getBytes()).toString());// json字符串
		String sign = QuickPayUtil.generSign(reqMap, signKey);
		reqMap.put("sign", sign);
		System.out.println(reqMap.get("sign").toString());

		LOG.info("申请交易提交的数据：" + reqMap.toString());

		String url = "https://pay.feifanzhichuang.com/middlepaytrx/kuaiPayUnique/preCreateOrder";

		Map<String, Object> maps = new HashMap<String, Object>();
		String respCode1 = "";
		String respMsg1 = "";
		String r3_url = "";
		try {
			String respStr1 = HttpUtil.postAndReturnString(reqMap, url);
			LOG.info("申请交易返回的报文" + respStr1);
			com.alibaba.fastjson.JSONObject respJson1 = JSON.parseObject(respStr1);

			// 去掉签名，md5加密，比对是否正确
			String respSign1 = (String) respJson1.remove("sign");
			// 再次加密
			String generSign1 = Md5Util.MD5(respJson1.toString() + signKey);
			LOG.info("验签结果：" + generSign1.equals(respSign1));

			respCode1 = respJson1.getString("retCode");
			respMsg1 = respJson1.getString("retMsg");
			r3_url = respJson1.getString("r3_url");

		} catch (Exception e) {
			e.printStackTrace();
		}

		if ("0000".equals(respCode1)) {
			LOG.info("----------  申请交易成功   ----------");

			maps.put("r3_url", r3_url);
			maps.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
			maps.put(CommonConstants.RESP_MESSAGE, respMsg1);
			return maps;
		} else {
			LOG.info("申请交易失败：" + respMsg1);

			this.addOrderCauseOfFailure(orderCode, respMsg1, rip);

			maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			maps.put(CommonConstants.RESP_MESSAGE, respMsg1);
			return maps;
		}

	}

	/**
	 * 跳转交易页面
	 * 
	 * @return
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/quick/ffzc/smsPay")
	public @ResponseBody Object smsPay(@RequestParam(value = "orderCode") String orderCode,
			@RequestParam(value = "requestId") String requestId) throws IOException {

		LOG.info("============ 进入交易页面接口  ============");

		PaymentRequestParameter prp = redisUtil.getPaymentRequestParameter(orderCode);

		String phoneC = prp.getCreditCardPhone();
		String idCard = prp.getIdCard();
		String bankCard = prp.getBankCard();
		String rip = prp.getIpAddress();

		FFZCRegister ffzcRegister = topupPayChannelBusiness.getFFZCRegisterByIdCard(idCard);
		String merchantNo = ffzcRegister.getMerchantNo();
		String signKey = ffzcRegister.getSignKey();
		String desKey = ffzcRegister.getDesKey();

		Map<String, String> reqMap = new LinkedHashMap<String, String>();
		reqMap.put("trxType", "PAY_SMS_CODE");
		reqMap.put("merchantNo", merchantNo);
		reqMap.put("requestId", requestId);// r3_url ：预下单字段

		SimpleDateFormat df = new SimpleDateFormat("yyyyMMddHHssmm");
		reqMap.put("timestamp", df.format(new Date()));// 时间戳 yyyyMMddHHssmm

		reqMap.put("jsonremark", Base64.encode(phoneC.getBytes()).toString());// json字符串
		String sign = QuickPayUtil.generSign(reqMap, signKey);
		reqMap.put("sign", sign);
		System.out.println(reqMap.get("sign").toString());

		LOG.info("交易页面提交的数据：" + reqMap.toString());

		String url = "https://pay.feifanzhichuang.com/middlepaytrx/kuaiPayUnique/sendPaySMSCode";

		Map<String, Object> maps = new HashMap<String, Object>();
		String respCode1 = "";
		String respMsg1 = "";
		String r5_qrCode = "";
		try {
			String respStr1 = HttpUtil.postAndReturnString(reqMap, url);
			LOG.info("交易页面返回的报文" + respStr1);
			com.alibaba.fastjson.JSONObject respJson1 = JSON.parseObject(respStr1);

			// 去掉签名，md5加密，比对是否正确
			String respSign1 = (String) respJson1.remove("sign");
			// 再次加密
			String generSign1 = Md5Util.MD5(respJson1.toString() + signKey);
			LOG.info("验签结果：" + generSign1.equals(respSign1));

			respCode1 = respJson1.getString("retCode");
			respMsg1 = respJson1.getString("retMsg");
			r5_qrCode = respJson1.getString("r5_qrCode");

		} catch (Exception e) {
			e.printStackTrace();
		}

		if ("0000".equals(respCode1)) {
			LOG.info("----------  跳转交易页面成功    ----------");

			maps.put("redirect_url", r5_qrCode);
			maps.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
			maps.put(CommonConstants.RESP_MESSAGE, respMsg1);
			return maps;
		} else {
			LOG.info("跳转交易页面失败：" + respMsg1);

			this.addOrderCauseOfFailure(orderCode, respMsg1, rip);

			maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			maps.put(CommonConstants.RESP_MESSAGE, respMsg1);
			return maps;
		}

	}
	
	/**
	 * 查询支付
	 * 
	 * @return
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/quick/ffzc/queryPay")
	public @ResponseBody Object queryPay(@RequestParam(value = "orderCode") String orderCode) throws IOException {

		LOG.info("============ 进入查询支付接口  ============");

		PaymentRequestParameter prp = redisUtil.getPaymentRequestParameter(orderCode);

		String idCard = prp.getIdCard();
		String rip = prp.getIpAddress();

		FFZCRegister ffzcRegister = topupPayChannelBusiness.getFFZCRegisterByIdCard(idCard);
		String merchantNo = ffzcRegister.getMerchantNo();
		String queryKey = ffzcRegister.getQueryKey();

		Map<String, String> reqMap = new LinkedHashMap<String, String>();
		reqMap.put("trxType", "OnlineQuery");
		reqMap.put("r1_merchantNo", merchantNo);
		reqMap.put("r2_orderNumber", orderCode);// r3_url ：预下单字段
		String sign = QuickPayUtil.generSign(reqMap, queryKey);
		reqMap.put("sign", sign);
		System.out.println(reqMap.get("sign").toString());

		LOG.info("查询支付提交的数据：" + reqMap.toString());

		String url = "https://pay.feifanzhichuang.com/middlepaytrx/online/query";

		Map<String, Object> maps = new HashMap<String, Object>();
		String respCode1 = "";
		String respMsg1 = "";
		String r8_orderStatus = "";
		try {
			String respStr1 = HttpUtil.postAndReturnString(reqMap, url);
			LOG.info("查询支付返回的报文" + respStr1);
			com.alibaba.fastjson.JSONObject respJson1 = JSON.parseObject(respStr1);

			// 去掉签名，md5加密，比对是否正确
			String respSign1 = (String) respJson1.remove("sign");
			// 再次加密
			String generSign1 = Md5Util.MD5(respJson1.toString() + signKey);
			LOG.info("验签结果：" + generSign1.equals(respSign1));

			respCode1 = respJson1.getString("retCode");
			respMsg1 = respJson1.getString("retMsg");
			r8_orderStatus = respJson1.getString("r8_orderStatus");

		} catch (Exception e) {
			e.printStackTrace();
		}

		if ("0000".equals(respCode1) && "SUCCESS".equals(r8_orderStatus)) {
			LOG.info("----------  查询支付成功    ----------");

			maps.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
			maps.put(CommonConstants.RESP_MESSAGE, r8_orderStatus);
			return maps;
		} else {
			LOG.info("查询支付失败：" + r8_orderStatus);

//			this.addOrderCauseOfFailure(orderCode, respMsg1, rip);

			maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			maps.put(CommonConstants.RESP_MESSAGE, r8_orderStatus);
			return maps;
		}

	}

	// 银联绑卡同步通知
	@RequestMapping(method = RequestMethod.GET, value = "/v1.0/paymentgateway/topup/ffzc/bindcard/return_call")
	public String ffzcReturnCallback(HttpServletRequest request, HttpServletResponse response, Model model)
			throws Exception {

		request.setCharacterEncoding("utf-8");
		response.setCharacterEncoding("utf-8");
		response.setContentType("text/html;charset=utf-8");

		LOG.info("卡开通同步通知进来了");

		return "ffzcbindcardsuccess";
	}

	/**
	 * 跳转到到账卡界面
	 * 
	 * @param request
	 * @param response
	 * @return
	 * @throws IOException
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/v1.0/paymentgateway/quick/ffzc/pay-view")
	public String toPay(HttpServletRequest request, HttpServletResponse response, Model model) throws IOException {
		LOG.info("ffzcPay------------------跳转到交易界面");

		// 设置编码
		request.setCharacterEncoding("utf-8");
		response.setCharacterEncoding("utf-8");
		response.setContentType("text/html;charset=utf-8");
		// bankCard
		String ordercode = request.getParameter("orderCode");
		String bankName = request.getParameter("bankName");
		String bankCard = request.getParameter("bankCard");
		String ipAddress = request.getParameter("ipAddress");
		String phone = request.getParameter("phone");
		String ips = request.getParameter("ips");
		String requestId = request.getParameter("requestId");

		model.addAttribute("ordercode", ordercode);
		model.addAttribute("bankName", bankName);
		model.addAttribute("bankCard", bankCard);
		model.addAttribute("ipAddress", ipAddress);
		model.addAttribute("phone", phone);
		model.addAttribute("requestId", requestId);
		model.addAttribute("ips", ips);

		return "ffzcpaymessage";
	}

	// 卡开通异步通知
	@RequestMapping(method = { RequestMethod.POST,
			RequestMethod.GET }, value = "/v1.0/paymentgateway/topup/ffzc/bindcard/notify_call")
	public void jfshangaoNotifyCallback(HttpServletRequest request, HttpServletResponse response) throws Exception {
		LOG.info("银联绑卡异步通知进来了=======");

		InputStream inputStream = request.getInputStream();
		ByteArrayOutputStream byteArray = null;
		byteArray = new ByteArrayOutputStream();
		byte[] dat = new byte[2048];
		int l = 0;
		while ((l = inputStream.read(dat, 0, 2048)) != -1) {
			byteArray.write(dat, 0, l);
		}
		byteArray.flush();
		LOG.info("ByteArrayOutputStream2String=============" + new String(byteArray.toByteArray(), "UTF-8"));
		String info = new String(byteArray.toByteArray(), "UTF-8");
		JSONObject jsonInfo = JSONObject.parseObject(info);
		LOG.info("jsonInfo=============" + jsonInfo.toString());

		String orderCode = jsonInfo.getString("orderNum");
		String retCode = jsonInfo.getString("retCode");
		String retMsg = jsonInfo.getString("retMsg");
		String token = jsonInfo.getString("token");
		String phone = jsonInfo.getString("phone");
		LOG.info("orderCode-------------------" + orderCode);
		LOG.info("retCode-------------------" + retCode);
		LOG.info("retMsg-------------------" + retMsg);
		LOG.info("token-------------------" + token);
		LOG.info("phone-------------------" + phone);

		PaymentRequestParameter prp = redisUtil.getPaymentRequestParameter(orderCode);
		String bankCard = prp.getBankCard();
		String idCard = prp.getIdCard();

		if ("0000".equals(retCode)) {
			// 判断卡是否已绑定
			FFZCBindCard fFZCBindCard = topupPayChannelBusiness.getFFZCBindCardByBankCard(bankCard);
			if (fFZCBindCard == null) {
				LOG.info("开通卡成功,保存新卡数据---------------");
				FFZCBindCard ffzcBindCard = new FFZCBindCard();
				ffzcBindCard.setPhone(phone);
				ffzcBindCard.setIdCard(idCard);
				ffzcBindCard.setBankCard(bankCard);
				ffzcBindCard.setToken(token);

				topupPayChannelBusiness.createFFZCBindCard(ffzcBindCard);
			} else {
				LOG.info("下单成功---------------");
			}

		} else {
			LOG.info("开通卡失败---------------" + retMsg);

		}

		PrintWriter pw = response.getWriter();
		pw.print("success");
		pw.close();

	}

	// 支付接口异步通知
	@RequestMapping(method = { RequestMethod.POST,
			RequestMethod.GET }, value = "/v1.0/paymentgateway/topup/ffzc/pay/notify_call")
	public void paynotifyCall(HttpServletRequest request, HttpServletResponse response) throws Exception {
		LOG.info("支付异步通知进来了=======");

		String retCode = request.getParameter("retCode");
		String retMsg = request.getParameter("retMsg");
		String orderNumber = request.getParameter("r2_orderNumber");
		String serialNumber = request.getParameter("r9_serialNumber");

		LOG.info("retCode=====" + retCode);
		LOG.info("retMsg=====" + retMsg);
		LOG.info("orderNumber=====" + orderNumber);
		LOG.info("serialNumber=====" + serialNumber);
		PaymentRequestParameter prp = redisUtil.getPaymentRequestParameter(orderNumber);
		try {
			Log.setLogFlag(true);
			Log.println("---交易： 订单结果异步通知-------------------------");

			LOG.info("交易： 订单结果异步通知===================");
			RestTemplate restTemplate = new RestTemplate();
			MultiValueMap<String, String> requestEntity = new LinkedMultiValueMap<String, String>();
			String Url = null;
			String Result = null;
			if ("0000".equals(retCode)) { // 订单已支付;
				LOG.info("*********************交易成功***********************");
				LOG.info("交易订单号：" + orderNumber);

				Url = prp.getIpAddress()+ChannelUtils.getCallBackUrl(prp.getIpAddress());
				//Url = prp.getIpAddress() + "/v1.0/transactionclear/payment/update";

				requestEntity = new LinkedMultiValueMap<String, String>();
				requestEntity.add("status", "1");
				requestEntity.add("order_code", orderNumber);
				requestEntity.add("third_code", "");
				try {
					Result = restTemplate.postForObject(Url, requestEntity, String.class);
				} catch (Exception e) {
					e.printStackTrace();
					LOG.error("",e);
				}

				LOG.info("修改订单状态成功---------：" + orderNumber);

				PrintWriter writer = response.getWriter();
				writer.print("success");
				writer.close();
			} else {
				LOG.info("订单支付失败!");

				PrintWriter writer = response.getWriter();
				writer.print("success");
				writer.close();
			}
		} catch (Exception e) {
			LOG.error("", e);
		}
	}
}
