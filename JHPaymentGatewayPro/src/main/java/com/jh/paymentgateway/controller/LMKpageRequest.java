package com.jh.paymentgateway.controller;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.multipart.MultipartRequestEntity;
import org.apache.commons.httpclient.methods.multipart.Part;
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
import org.springframework.web.multipart.MultipartFile;

import com.jh.paymentgateway.basechannel.BaseChannel;
import com.jh.paymentgateway.business.TopupPayChannelBusiness;
import com.jh.paymentgateway.config.RedisUtil;
import com.jh.paymentgateway.pojo.BankNumCode;
import com.jh.paymentgateway.pojo.LMBankNum;
import com.jh.paymentgateway.pojo.LMRegister;
import com.jh.paymentgateway.pojo.PaymentRequestParameter;
import com.jh.paymentgateway.pojo.RegisterPartsBuilderss;
import com.jh.paymentgateway.util.MD5Util;
import com.jh.paymentgateway.util.PhotoCompressUtil;

import cn.jh.common.utils.CommonConstants;
import cn.jh.common.utils.ExceptionUtil;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

@Controller
@EnableAutoConfiguration
public class LMKpageRequest extends BaseChannel {
	private static final Logger LOG = LoggerFactory.getLogger(LMKpageRequest.class);

	private String mch_id = "1514312323";
	private String app_id = "to3940105693071a7d63e3e44b0a1639c0";
	private String key = "dcddf093acb42eed08d077a3d433b03d";
	private String characterEncoding = "UTF-8"; // 指定字符集UTF-8

	@Autowired
	RedisUtil redisUtil;

	@Value("${payment.ipAddress}")
	private String ip;

	@Autowired
	TopupPayChannelBusiness topupPayChannelBusiness;

	@SuppressWarnings("null")
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/topup/lmk/register")
	public @ResponseBody Object Register(@RequestParam("orderCode") String orderCode) throws IOException {
		PaymentRequestParameter prp = redisUtil.getPaymentRequestParameter(orderCode);
		Map<String, Object> maps = new HashMap<>();
		String userName = prp.getUserName();// 用户名
		String idCard = prp.getIdCard();// 身份证号
		String rip = prp.getIpAddress();
		String rate = prp.getRate();
		String amount = prp.getAmount();
		String bankCard = prp.getBankCard();
		String extraFee = prp.getExtraFee();
		String bankName = prp.getDebitBankName();// 到账卡银行名称
		String creditCardName = prp.getCreditCardBankName();
		String phoneC = prp.getCreditCardPhone();
		LMRegister LMRegister = topupPayChannelBusiness.getLMRegisterByidCard(idCard);

		if (LMRegister != null) {
			String requestNo = "XinLi" + System.currentTimeMillis();
			LOG.info("===register请求订单号：" + requestNo);

			RestTemplate restTemplate2 = new RestTemplate();
			MultiValueMap<String, String> requestEntity2 = new LinkedMultiValueMap<String, String>(); 
			String URL = null; String
			results2 = null; URL = rip + "/v1.0/user/getPicture";
			requestEntity2.add("phone", phoneC); 
			try { 
				results2 = restTemplate2.postForObject(URL, requestEntity2, String.class);
				LOG.info("*******************获取用户图片***********************"); 
			}
			catch (Exception e) { 
				e.printStackTrace();
				LOG.error(ExceptionUtil.errInfo(e)); 
			} 
			JSONObject json = JSONObject.fromObject(results2); 
			List<String> fliestr = (List<String>) json.get("result"); 
			File file = null; 
			List<File> filelist = new ArrayList<>(); 
			JSONArray jsonarry = JSONArray.fromObject(fliestr); 
			for (int i = 0; i < jsonarry.size(); i++) { 
				String base64Byte = jsonarry.getString(i); LOG.info("========下标" + i + "图片文件byte流");
				byte[] buffer = Base64.getDecoder().decode(base64Byte); 
				try {
					String str = ""; 
					str = str + (char) (Math.random() * 26 + 'A' + i); 
					String zipPath = "/" + phoneC + str + ".jpg"; 
					file = new File(zipPath); 
					if (file.exists()) { 
						file.delete(); 
					} 
					OutputStream output = new FileOutputStream(file); 
					BufferedOutputStream bufferedOutput = new BufferedOutputStream(output);
					bufferedOutput.write(buffer); 
					bufferedOutput.close();
					output.close();
					PhotoCompressUtil.compressPhoto(zipPath, zipPath,0.01f); 
					filelist.add(file); 
				} catch (Exception e) {
					LOG.info("=======================读取文件流异常"); 
				} 
			}

			SortedMap<Object, Object> parameters = new TreeMap<Object, Object>();
			parameters.put("mch_id", mch_id);
			parameters.put("app_id", app_id);
			parameters.put("mch_no", requestNo);
			parameters.put("realname", userName);
			parameters.put("id_card", idCard);
			parameters.put("bank_card_no", bankCard);
			parameters.put("bank_card_mobile", phoneC);
			
			String mySign = createSign(characterEncoding, parameters, key);
			LOG.info("=======================设置签名" + mySign);
			Part[] parts = null;
			try {
				parts = new RegisterPartsBuilderss().setMch_id(mch_id).setApp_id(app_id).setMch_no(requestNo)
						.setSign(mySign).setRealname(userName).setidCard(idCard).setCardNo(bankCard)
						.setPhone(phoneC).setIdCardFront(filelist.get(0))
						.setIdCardBack(filelist.get(1))
						.generateParams();
			} catch (Exception e) {
				LOG.info("=======================设置签名出错");
			}

			PostMethod postMethod = new PostMethod(
					"https://pay.longmaoguanjia.com/api/pay/large_pay_k/sub_merchant_register");
			HttpClient client = new HttpClient();
			postMethod.setRequestEntity(new MultipartRequestEntity(parts, postMethod.getParams()));

			int status = client.executeMethod(postMethod);
			LOG.info("==========status==========" + status);
			if (status == HttpStatus.SC_OK) {
				String backinfo = postMethod.getResponseBodyAsString();
				LOG.info("==========register返回响应==========" + backinfo);
				JSONObject jsonstr = JSONObject.fromObject(backinfo);
				String respCode = jsonstr.getString("code");
				String respMessage = jsonstr.getString("message");
				if (respCode.equals("0")) {
					String data = jsonstr.getString("data");
					JSONObject datastr = JSONObject.fromObject(data);
					String merchantNo = datastr.getString("sub_mch_no");
					LMRegister lm = new LMRegister();
					lm.setBankCard(bankCard);
					lm.setMainCustomerNum(mch_id);
					lm.setCustomerNum(merchantNo);
					lm.setIdCard(idCard);
					lm.setUserName(userName);
					lm.setPhone(phoneC);
					lm.setStatus("0");
					topupPayChannelBusiness.createLMRegister(lm);

					return maps;
				} else {
					maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
					maps.put(CommonConstants.RESP_MESSAGE, respMessage);
					this.addOrderCauseOfFailure(orderCode, "进件:" + respMessage + "[" + requestNo + "]", rip);
					return maps;
				}
			}
		}
		return maps;
	}

	/**
	 * 生成签名
	 * 
	 * @param characterEncoding
	 * @param parameters
	 * 
	 * @param key
	 * @return
	 */
	public String createSign(String characterEncoding, SortedMap<Object, Object> parameters, String key) {
		StringBuffer sb = new StringBuffer();
		StringBuffer sbkey = new StringBuffer();
		Set es = parameters.entrySet(); // 所有参与传参的参数按照accsii排序（升序）
		Iterator it = es.iterator();
		while (it.hasNext()) {
			Map.Entry entry = (Map.Entry) it.next();
			String k = (String) entry.getKey();
			Object v = entry.getValue();
			// 空值不传递，不参与签名组串
			if (null != v && !"".equals(v)) {
				sb.append(k + "=" + v + "&");
				sbkey.append(k + "=" + v + "&");
			}
		}
		/* System.out.println("字符串:" + sb.toString()); */
		sbkey = sbkey.append("key=" + key);
		LOG.info("字符串拼接:" + sbkey.toString());
		// MD5加密,结果转换为大写字符
		String sign = MD5Util.digest(sbkey.toString(), characterEncoding).toUpperCase();
		LOG.info("MD5加密值:" + sign);
		return sign;
	}

	public void inputStreamToFile(InputStream ins, File file) {
		try {
			OutputStream os = new FileOutputStream(file);
			int bytesRead = 0;
			byte[] buffer = new byte[8192];
			while ((bytesRead = ins.read(buffer, 0, 8192)) != -1) {
				os.write(buffer, 0, bytesRead);
			}
			os.close();
			ins.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}