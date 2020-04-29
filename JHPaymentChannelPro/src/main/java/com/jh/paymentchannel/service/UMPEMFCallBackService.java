package com.jh.paymentchannel.service;

import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.net.URI;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.json.JSONObject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.stereotype.Controller;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestTemplate;

import com.jh.paymentchannel.util.Util;
import com.jh.paymentchannel.util.ump.common.ReqData;
import com.jh.paymentchannel.util.ump.exception.ReqDataException;
import com.jh.paymentchannel.util.ump.exception.RetDataException;
import com.jh.paymentchannel.util.ump.exception.VerifyException;
import com.jh.paymentchannel.util.ump.paygate.v40.HttpRequest;
import com.jh.paymentchannel.util.ump.paygate.v40.Mer2Plat_v40;
import com.jh.paymentchannel.util.ump.paygate.v40.Plat2Mer_v40;

import cn.jh.common.utils.CommonConstants;

@Controller
@EnableAutoConfiguration
public class UMPEMFCallBackService {

	private static final Logger log = LoggerFactory.getLogger(UMPEMFCallBackService.class);

	@Value("${jifu.hzfPriKey}")
	private String hzfPriKey1;

	@Autowired
	Util util;

	// 联动优势地址
	public static final String umpurl = "http://pay.soopay.net/spay/pay/payservice.do";
	// 商户入驻成功
	@Value("${ump.merid}")
	public String merchantCode;

	@Value("${ump.merprikeypath}")
	private String merprikeypath;

	@Value("${ump.platcertpath}")
	private String platcertpath;

	@Value("${payment.ipAddress}")
	private String ipAddress;

	/**
	 * 商户余额查询接口
	 * 
	 ***/
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentchannel/topup/ump/amount")
	public @ResponseBody Object queryUMPAmount(HttpServletRequest request,
			// 金额
			@RequestParam(value = "amount") String amount) {
		Map map = new HashMap();

		map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
		map.put(CommonConstants.RESP_MESSAGE, "成功");
		map.put(CommonConstants.RESULT, UMPAccountBalance(amount));
		return map;
	}

	/***
	 * 商户余额查询
	 **/
	public Boolean UMPAccountBalance(String amount) {

		Map map = new HashMap();
		// 接口名称【service】
		map.put("service", "query_account_balance");
		// 参数字符编码集【charset】：
		map.put("charset", "UTF-8");
		// 商户编号【mer_id】
		map.put("mer_id", merchantCode);
		// 签名方式【sign_type】
		map.put("sign_type", "RSA");
		// 响应数据格式【res_format】
		map.put("res_format", "HTML");
		// 版本号【version】
		map.put("version", "4.0");
		// 账户类型
		map.put("acc_type", "2");

		log.info("联动商户结算账户余额查询请求====" + map.toString());
		try {
			ReqData reqDataGet = Mer2Plat_v40.makeReqDataByGet(map);
			String get_url = reqDataGet.getUrl(); // get请求报文，get方式请求此报文即可
			String Result = HttpRequest.sendGet(get_url);

			/**
			 * 以下是对联动的响应结果做验签的示例
			 * 
			 */

			Map dataMap = new HashMap();
			try {
				dataMap = Plat2Mer_v40.getResData(Result);
				log.info("dataMap" + dataMap);
				log.info("dataMap.get(ret_code)=" + dataMap.get("ret_code"));
				if (dataMap.get("ret_code").equals("0000")) {

					int amountInt = Integer
							.parseInt(new BigDecimal(amount).multiply(new BigDecimal("100")).setScale(0).toString());

					String balsign = (String) dataMap.get("bal_sign");
					int balsignInt = Integer.parseInt(balsign);

					if (balsignInt > amountInt) {
						return true;
					}

				}
			} catch (Exception e) {
				log.error("联动查询商户结算账户失败======" + e);
				return false;

			}

			return false;
		} catch (Exception e) {
			log.error("联动查询商户结算账户失败======" + e);
			return false;

		}
	}

}
