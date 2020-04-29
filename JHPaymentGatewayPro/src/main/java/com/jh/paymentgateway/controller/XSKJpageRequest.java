package com.jh.paymentgateway.controller;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.security.interfaces.RSAPrivateKey;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import com.jh.paymentgateway.basechannel.BaseChannel;
import com.jh.paymentgateway.config.RedisUtil;
import com.jh.paymentgateway.pojo.PaymentRequestParameter;
import com.jh.paymentgateway.util.xskj.DateUtils;
import com.jh.paymentgateway.util.xskj.ExpConstant;
import com.jh.paymentgateway.util.xskj.ExpUtil;
import com.jh.paymentgateway.util.xskj.HnapayRandom;
import com.jh.paymentgateway.util.xskj.JsonUtil;
import cn.jh.common.tools.ResultWrap;
import cn.jh.common.utils.CommonConstants;

@Controller
public class XSKJpageRequest extends BaseChannel {
	
	private static final Logger LOG = LoggerFactory.getLogger(XSKJpageRequest.class);
	
	private RSAPrivateKey privateKey;
	
	@Value("${payment.ipAddress}")
	private String paymentGatewayIp;

	private String merID="11000004294";
	@Autowired
	private RedisUtil redisUtil;
	
    private static String contractUrl = "https://gateway.hnapay.com/expConsume/payRequest2Step.do";//支付请求下单接口
    private static String confirmUrl = "https://gateway.hnapay.com/expConsume/payConfirm2Step.do";//支付确认接口
    private static String cpaymentUrl = "https://gateway.hnapay.com/exp/payment.do";//补偿接口
    /**
     * 
     * 支付界面
     *@author Admin
     * @param
     * 
     * **/
    @RequestMapping(method = RequestMethod.GET, value = "/v1.0/paymentgateway/topup/xskj/paypage")
	public String xskjtopaypage(HttpServletRequest request,	HttpServletResponse response,
			@RequestParam(value = "orderCode") String orderCode, Model model)
			throws IOException {

		PaymentRequestParameter prp = redisUtil.getPaymentRequestParameter(orderCode);	
		
		String ipAddress = paymentGatewayIp;

		model.addAttribute("ordercode", prp.getOrderCode());
		
		model.addAttribute("bankName", prp.getCreditCardBankName());
		
		model.addAttribute("bankCard", prp.getBankCard());
		
		model.addAttribute("cardType", prp.getCreditCardCardType());
		
		model.addAttribute("amount", prp.getAmount());
		
		model.addAttribute("ipAddress", ipAddress);
		
		return "xskjpaymessage";
	}
	
    /**
     * 发送短信验证码（预下单）
     *@author Admin
     * @param
     * 
     * **/
    @RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/topup/xskj/sms")
	public @ResponseBody Object  returnWMYKQuickBankInfo(HttpServletRequest request,HttpServletResponse response,
			Model model)
			throws Exception {
    	Map<String,Object> map = new HashMap<>();
		
		
		Map<String, String> params12 = genExp14Data(request.getParameter("order_code"),request);
		
		LOG.info("新生预下单报文============"+params12.toString());
		
		String response12 = ExpUtil.submit("EXP14", contractUrl, params12);
		
		//验签
		Map<String, Object> retMap12= JsonUtil.jsonToMap(response12);
		
		boolean verify = ExpUtil.verify("EXP14", retMap12);
		
		if(!verify) {
			LOG.info("---------------验签失败");
			map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			map.put(CommonConstants.RESP_MESSAGE, "交易排队中请重新发起");
			return map;
		}
		if (!"0000".equals(retMap12.get(ExpConstant.RESULTCODE))) {
			map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			map.put(CommonConstants.RESP_MESSAGE, retMap12.get(ExpConstant.ERRORMSG));
			LOG.info("---------------调用支付请求下单失败:" + retMap12.get(ExpConstant.ERRORCODE)+"==描述："+retMap12.get(ExpConstant.ERRORMSG));
			return map;
		}
		String hanpayOrderId = (String) retMap12.get(ExpConstant.HNAPAYORDERID);
		LOG.info("hanpayOrderId=" + hanpayOrderId);
		
		map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
		map.put(CommonConstants.RESP_MESSAGE, "请求成功");
		map.put("orderId", hanpayOrderId);
		return map;
		
	}
	
    /**
     * 
     * 确认下单
     * @throws Exception 
     * 
     * **/
    @RequestMapping(method=RequestMethod.POST,value="/v1.0/paymentgateway/topup/xskj/payfrom")
	public @ResponseBody Object confirmTXPay(HttpServletRequest request,HttpServletResponse response
			) throws Exception {
    	Map<String,Object> map = new HashMap<>();
    	LOG.info("---------------调用支付确认");
    	Map<String, String> params12= new HashMap<String, String>();
    	String ordercode=request.getParameter("order_code");
    	
    	String smsCode=request.getParameter("sms_code");
    	
		String orderId=request.getParameter("order_id");
		
    	params12.put(ExpConstant.MERID,merID);
    	
    	params12.put(ExpConstant.MERORDERID,ordercode);
    	
		Map<String, String> params13 = genExp15Data(params12, orderId, smsCode);
		
		LOG.info("新生确认单报文============"+params13.toString());
		
		String response15 = ExpUtil.submit("EXP15", confirmUrl, params13);
		
		Map<String, Object> retMap15 = JsonUtil.jsonToMap(response15);
		
		boolean verify = ExpUtil.verify("EXP15", retMap15);
		if(!verify) {
			LOG.info("---------------验签失败");
			map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			map.put(CommonConstants.RESP_MESSAGE, "交易排队中请重新发起");
			return map;
		}
		LOG.info("---------------验签通过");
		if (!"9999".equals(retMap15.get(ExpConstant.RESULTCODE))) {
			LOG.info("---------------调用支付请求下单失败:" + retMap15.get(ExpConstant.ERRORCODE)+"==描述："+retMap15.get(ExpConstant.ERRORMSG));
			map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			map.put(CommonConstants.RESP_MESSAGE, retMap15.get(ExpConstant.ERRORMSG));
			return map;
		}
		map=ResultWrap.err(LOG, CommonConstants.SUCCESS, "下单成功");
		return map;
	}
    
    
    /**
     * 
     * 补偿接口
     * @throws Exception 
     * 
     * **/
    @RequestMapping(method=RequestMethod.POST,value="/v1.0/paymentgateway/topup/xskj/payment")
	public @ResponseBody Object payment(HttpServletRequest request,HttpServletResponse response
			) throws Exception {
    	Map<String,Object> map = new HashMap<>();
    	LOG.info("---------------调用支付补偿接口");
    	Map<String, String> params12= new HashMap<String, String>();
    	String ordercode=request.getParameter("order_code");
    	
    	String oldOrderCode=request.getParameter("old_order_code");

    	
    	String orgSubmitTime=request.getParameter("order_time");
    	
		String bankCard=request.getParameter("bank_card");
		
		String phone=request.getParameter("phone");
		
		params12.put(ExpConstant.ORGMERORDERID, oldOrderCode);// 原商户订单号
		
		params12.put(ExpConstant.ORGSUBMITTIME, orgSubmitTime);// 原订单支付下单
		
		params12.put(ExpConstant.PAYEEACCOUNT, bankCard);// 银行卡号
		
		params12.put(ExpConstant.MOBILE, phone);// 手机号
		
		params12.put(ExpConstant.MERID,merID);
    	
    	params12.put(ExpConstant.MERORDERID,ordercode);
		
    	
		Map<String, String> params13 = genExp20Data(params12);
		
		LOG.info("新生补发报文============"+params13.toString());
		
		String response15 = ExpUtil.submit("EXP20", cpaymentUrl, params13);
		
		Map<String, Object> retMap15 = JsonUtil.jsonToMap(response15);
		
		boolean verify = ExpUtil.verify("EXP20", retMap15);
		if(!verify) {
			LOG.info("---------------验签失败");
			map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			map.put(CommonConstants.RESP_MESSAGE, "交易排队中请重新发起");
			return map;
		}
		LOG.info("---------------验签通过"+retMap15);
		if (!"9999".equals(retMap15.get(ExpConstant.RESULTCODE))) {
			LOG.info("---------------调用补单请求下单失败:" + retMap15.get(ExpConstant.ERRORCODE)+"==描述："+retMap15.get(ExpConstant.ERRORMSG));
			map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			map.put(CommonConstants.RESP_MESSAGE, retMap15.get(ExpConstant.ERRORMSG));
			return map;
		}
		map=ResultWrap.err(LOG, CommonConstants.SUCCESS, "下单成功");
		return map;
	}
    
    
    
    /**
	 * 生成支付下单请求报文
	 */
	public  Map<String, String> genExp14Data(String orderCoder,HttpServletRequest request) {
		PaymentRequestParameter prp = redisUtil.getPaymentRequestParameter(orderCoder);
		Map<String, String> params = new HashMap<String, String>();
		String now = DateUtils.getCurrDate();
		String merOrderId = prp.getOrderCode();
		String submitTime = now;
		String amt = HnapayRandom.genRandomDec(2);
		amt = new BigDecimal(amt).toString();
		// 报文密文参数
		params.put("tranAmt", prp.getAmount());
		params.put("payType", "2");
		params.put("tradeFeeUnit", "1");
		params.put("tradeFeeAmt",new BigDecimal(prp.getRate()).multiply(new BigDecimal("100")).setScale(2,BigDecimal.ROUND_UP).toString());
		params.put("extraFeeUnit", "2");
		params.put("extraFeeAmt", prp.getExtraFee());
		params.put("cardNo",prp.getBankCard());
		params.put("holderName", prp.getUserName());
		params.put("cardAvailableDate",this.expiredTimeToMMYY(prp.getExpiredTime()) );
		params.put("cvv2", prp.getSecurityCode());
		params.put("mobileNo", prp.getCreditCardPhone());
		params.put("identityType", "01");
		params.put("identityCode", prp.getIdCard());
		params.put("settleCardNo", prp.getDebitCardNo());
		params.put("settleMobileNo", prp.getDebitPhone());
		params.put("goodsInfo", prp.getExtra());
		params.put("riskExpand", prp.getExtra());
		params.put("notifyUrl", paymentGatewayIp+"/v1.0/paymentgateway/topup/xskj/notify");
		params.put("merUserIp", this.getRemoteIP(request));
		params.put("orderExpireTime", "120");
		params.put("merUserId", prp.getUserId());
		//params.put("subMerchantId", "1902151324491096929");
		
		//20190508更新    新生通道我司商户号
		params.put("subMerchantId", "1905080942141875736");
		// 报文明文参数
		params.put(ExpConstant.VERSION, "2.0");// 版本号
		params.put(ExpConstant.TRANCODE, "EXP14");// 交易代码
		params.put(ExpConstant.MERID, merID);// 商户ID
		params.put(ExpConstant.MERORDERID, merOrderId);// 商户订单号
		params.put(ExpConstant.SUBMITTIME, submitTime);// 请求提交时间
		params.put(ExpConstant.MSGCIPHERTEXT, ExpUtil.encrpt("EXP14", params));// 报文密文
		params.put(ExpConstant.SIGNTYPE, "1");// 签名类型:RSA
		params.put(ExpConstant.MERATTACH, "1");// 附加数据
		params.put(ExpConstant.CHARSET, "1");// 编码方式:UTF-8
		try {
			params.put(ExpConstant.SIGNVALUE, ExpUtil.sign("EXP14", params));// 签名密文串
		} catch (Exception e) {
			e.printStackTrace();
			params.put(ExpConstant.SIGNVALUE, "");
		}
		return params;
	}
	
	
	
	
	
	
	
	/**
	 * 生成支付确认请求报文
	 */
	public static Map<String, String> genExp15Data(Map<String, String> params12, String hanpayOrderId, String smsCode)
			throws Exception {
		Map<String, String> params = new HashMap<String, String>();

		// 报文密文参数
		params.put(ExpConstant.HNAPAYORDERID, hanpayOrderId);// 新生订单号
		params.put(ExpConstant.SMSCODE, smsCode);// 短信验证码
		params.put(ExpConstant.MERUSERIP, "192.168.1.1");// 商户用户IP

		String now = DateUtils.getCurrDate();
		// 报文明文参数
		params.put(ExpConstant.VERSION, "2.0");// 版本号
		params.put(ExpConstant.TRANCODE, "EXP15");// 交易代码
		params.put(ExpConstant.MERID, params12.get(ExpConstant.MERID));// 商户ID
		params.put(ExpConstant.MERORDERID, params12.get(ExpConstant.MERORDERID));// 商户订单号
		params.put(ExpConstant.SUBMITTIME, now);// 请求提交时间
		params.put(ExpConstant.MSGCIPHERTEXT, ExpUtil.encrpt("EXP15", params));// 报文密文
		params.put(ExpConstant.SIGNTYPE, "1");// 签名类型:RSA
		params.put(ExpConstant.MERATTACH, "1");// 附加数据
		params.put(ExpConstant.CHARSET, "1");// 编码方式:UTF-8
		try {
			params.put(ExpConstant.SIGNVALUE, ExpUtil.sign("EXP15", params));
		} catch (Exception e) {
			e.printStackTrace();
			params.put(ExpConstant.SIGNVALUE, "");
		}

		return params;
	}
	
	/**
	 * 出款补偿接口 
	 */
	public static Map<String, String> genExp20Data(Map<String, String> params12)
			throws Exception {
		Map<String, String> params = new HashMap<String, String>();
		params.put(ExpConstant.ORGMERORDERID, params12.get(ExpConstant.ORGMERORDERID));// 原商户订单号
		params.put(ExpConstant.ORGSUBMITTIME, params12.get(ExpConstant.ORGSUBMITTIME));// 原订单支付下单
		params.put(ExpConstant.PAYEEACCOUNT, params12.get(ExpConstant.PAYEEACCOUNT));// 银行卡号
		params.put(ExpConstant.MOBILE, params12.get(ExpConstant.MOBILE));// 手机号
		
		String now = DateUtils.getCurrDate();
		// 报文明文参数
		params.put(ExpConstant.VERSION, "2.0");// 版本号
		params.put(ExpConstant.TRANCODE, "EXP20");// 交易代码
		params.put(ExpConstant.MERID, params12.get(ExpConstant.MERID));// 商户ID
		params.put(ExpConstant.MERORDERID, params12.get(ExpConstant.MERORDERID));// 商户订单号
		params.put(ExpConstant.SUBMITTIME, now);// 请求提交时间
		params.put(ExpConstant.MSGCIPHERTEXT, ExpUtil.encrpt("EXP20", params));// 报文密文
		params.put(ExpConstant.SIGNTYPE, "1");// 签名类型:RSA
		params.put(ExpConstant.MERATTACH, params12.get(ExpConstant.MERORDERID));// 附加数据
		params.put(ExpConstant.CHARSET, "1");// 编码方式:UTF-8
		try {
			params.put(ExpConstant.SIGNVALUE, ExpUtil.sign("EXP20", params));
		} catch (Exception e) {
			e.printStackTrace();
			params.put(ExpConstant.SIGNVALUE, "");
		}

		return params;
	}
	
	/**
	 * 获取ip
	 * **/
	public String getRemoteIP(HttpServletRequest request) {  
		String ipAddress = request.getHeader("x-forwarded-for");  
        if (ipAddress == null || ipAddress.length() == 0 || "unknown".equalsIgnoreCase(ipAddress)) {  
            ipAddress = request.getHeader("Proxy-Client-ipAddress");  
        }  
        if (ipAddress == null || ipAddress.length() == 0 || "unknown".equalsIgnoreCase(ipAddress)) {  
            ipAddress = request.getHeader("WL-Proxy-Client-ipAddress");  
        }  
        if (ipAddress == null || ipAddress.length() == 0 || "unknown".equalsIgnoreCase(ipAddress)) {  
            ipAddress = request.getHeader("HTTP_CLIENT_IP");  
        }  
        if (ipAddress == null || ipAddress.length() == 0 || "unknown".equalsIgnoreCase(ipAddress)) {  
            ipAddress = request.getHeader("HTTP_X_FORWARDED_FOR");  
        }  
        if (ipAddress == null || ipAddress.length() == 0 || "unknown".equalsIgnoreCase(ipAddress)) {  
            ipAddress = request.getRemoteAddr();  
        }
        String[] strs = ipAddress.split(",");
        if (strs.length > 0) {
        	ipAddress = strs[0];
		}
        return ipAddress.trim();  
    }  
	/**
	 * 生成查询参数
	 * **/
	public  Map<String, String> genExp08Data(String ordercode,String creatTime) {
		PaymentRequestParameter prp = redisUtil.getPaymentRequestParameter(ordercode);
        Map<String, String> params = new HashMap<String, String>();
        String version = "2.0";       // 版本号
        String tranCode = "EXP16";    // 交易码
        String merId = merID; // 商户ID
        String merOrderId = ordercode;  // 要查询的商户订单号
        String submitTime = creatTime;   // 要查询的商户订单时间(格式：yyyyMMdd)
        String signType = "1";     // 签名类型(固定1：RSA)
        String merAttach = "TestEXP16";  // 附加数据(可为空)
        String charset = "1";      // 编码方式(固定1：UTF8)

        params.put(ExpConstant.VERSION, version);
        params.put(ExpConstant.TRANCODE, tranCode);
        params.put(ExpConstant.MERID, merId);
        params.put(ExpConstant.MERORDERID, merOrderId);
        params.put(ExpConstant.SUBMITTIME, submitTime);
        params.put(ExpConstant.SIGNTYPE, signType);
        params.put(ExpConstant.MERATTACH, merAttach);
        params.put(ExpConstant.CHARSET, charset);
        try {
        	// 签名(要签名的字段参考新生快捷2.0网关接口文档)
            params.put(ExpConstant.SIGNVALUE, ExpUtil.sign("EXP08", params));
        } catch (Exception e) {
            e.printStackTrace();
            params.put(ExpConstant.SIGNVALUE, "");
        }
        return params;
    }
	/**
	 * tx快捷/还款消费 支付异步通知接口
	 * @param request
	 * @param response
	 * @param linkid
	 * @param orderNo
	 * @param orderStatus
	 * @param orderMemo
	 * @param sign
	 * @return
	 * <p>Description: </p>
	 * @throws Exception 
	 */
	@RequestMapping(method=RequestMethod.POST,value="/v1.0/paymentgateway/topup/xskj/notify")
	public @ResponseBody  Object xsQuickNotifypost(HttpServletRequest request,HttpServletResponse response
			) throws Exception {
		LOG.info("异步回调进来了POST");
		Map<String, String[]> parameterMap = request.getParameterMap();
		Map<String,Object> notifymap = new HashMap<String, Object>();
		Set<String> keySet = parameterMap.keySet();
		for (String key : keySet) {
			LOG.info(key+"====="+parameterMap.get(key)[0]);
			if(parameterMap.get(key)[0]!=null&&!parameterMap.get(key)[0].equals("")){
				notifymap.put(key, parameterMap.get(key)[0]);
			}
		}
		boolean  verify = ExpUtil.verifyNotify("EXP15", notifymap);
		if(!verify) {
			LOG.info("异步通知---------------验签失败");
			return "fail";
		}
		String merOrderId=(String) notifymap.get("merOrderId");
		
		String resultCode=(String) notifymap.get("resultCode");
		
		String respMsg=(String) notifymap.get("respMsg");
		
		String hnapayOrderId=(String) notifymap.get("hnapayOrderId");
		
		
		PaymentRequestParameter bean = redisUtil.getPaymentRequestParameter(merOrderId);
		if ("0000".equals(resultCode)) {
			this.updateSuccessPaymentOrder(bean.getIpAddress(), merOrderId,hnapayOrderId);
			/*try {
				response.getWriter().println("200");
			} catch (IOException e) {
				e.printStackTrace();
				return "200";
			}*/
			return "200";
		}
		this.addOrderCauseOfFailure(merOrderId, respMsg, bean.getIpAddress());
		return ResultWrap.err(LOG, CommonConstants.FALIED, merOrderId + "非成功回调",respMsg);
	
	}
	
	
	
	public static final byte[] readBytes(InputStream is, int contentLen) {
        if (contentLen > 0) {
                int readLen = 0;
                int readLengthThisTime = 0;
                byte[] message = new byte[contentLen];
                try {
                    while (readLen != contentLen) {
                        readLengthThisTime = is.read(message, readLen, contentLen- readLen);
                        if (readLengthThisTime == -1) {// Should not happen.
                           break;
                        }
                        readLen += readLengthThisTime;
                    }
                    return message;
                } catch (IOException e) {
                    e.printStackTrace();
                }
        }

        return new byte[] {};
}
	/*@SuppressWarnings("unchecked")
	public static void main(String[] args) throws Exception {
		System.out.println("---------------调用支付请求下单");
		Map<String, String> params12 = genExp14Data();
		System.out.println(params12);
		String url12 = "https://gateway.hnapay.com/expConsume/payRequest2Step.do";
		String response12 = ExpUtil.submit("EXP14", url12, params12);
		//验签
		Map<String, Object> retMap12= JsonUtil.jsonToMap(response12);
		boolean verify = ExpUtil.verify("EXP14", retMap12);
		if(!verify) {
			System.out.println("---------------验签失败");
			return;
		}
		System.out.println("---------------验签通过");
		if (!"0000".equals(retMap12.get(ExpConstant.RESULTCODE))) {
			System.out.println("---------------调用支付请求下单失败:" + retMap12.get(ExpConstant.ERRORCODE));
			return;
		}
		String hanpayOrderId = (String) retMap12.get(ExpConstant.HNAPAYORDERID);
		System.out.println("hanpayOrderId=" + hanpayOrderId);

		Thread.sleep(2000);

		System.out.println("\n\n---------------如果是有短快捷，请输入短信验证码：");
//		Scanner scanner = new Scanner(System.in);
//		String smsCode = scanner.nextLine();
//		scanner.close();

		System.out.println("---------------调用支付确认");
		Map<String, String> params13 = genExp15Data(params12, "EXT2018122032612336", "831597");
		System.out.println(params13);
		String url13 = "https://gateway.hnapay.com/expConsume/payConfirm2Step.do";
		String response15 = ExpUtil.submit("EXP15", url13, params13);
		Map<String, Object> retMap15 = JsonUtil.jsonToMap(response15);
		verify = ExpUtil.verify("EXP15", retMap15);
		if(!verify) {
			System.out.println("---------------验签失败");
			return;
		}
		System.out.println("---------------验签通过");
		if (!"9999".equals(retMap15.get(ExpConstant.RESULTCODE))) {
			System.out.println("---------------支付失败:" + retMap15.get(ExpConstant.ERRORCODE));
			return;
		}


		Map<String,Object> notifymap = new HashMap<String, Object>();
		notifymap.put("charset","1");
		notifymap.put("bankCode","TBANK");
		notifymap.put("hnapayOrderId","EXT2018032277383580");
		notifymap.put("settleShortCardNo","4454");
		notifymap.put("settleAmt","91.99");
		notifymap.put("cardType","2");
		notifymap.put("resultCode","0000");
		notifymap.put("checkDate","");
		notifymap.put("version","2.0");
		notifymap.put("signValue","VJD05M/CklsyoO04ydSoq/ccGzBc/OPMrkkS/ZVuEZICF6z1+tjM0+qHYXXdHY54w+cYdO0CgxtoniVRzSVtjBbPTmLh1KOw0Co/DIaGxr+kVGa73oclqcDJv5ouNjHxndiwi6nrRA380aTPeBdZ46GigQRROcd1rK/5bskiucA=");
		notifymap.put("shortCardNo","0123");
		notifymap.put("tranAmt","100.00");
		notifymap.put("signType","1");
		notifymap.put("respMsg","成功");
		notifymap.put("merId","11000000115");
		notifymap.put("merAttach","");
		notifymap.put("tranCode","EXP15");
		notifymap.put("merOrderId","EXP14_20180322182459");
		notifymap.put("submitTime","20180402114619");
		notifymap.put("tranFinishTime","20180402114719");

		verify = ExpUtil.verifyNotify("EXP15", notifymap);
		if(!verify) {
			System.out.println("异步通知---------------验签失败");
			return;
		}

	}*/
	/**
	 * 生成支付下单请求报文
	 */
	public static Map<String, String> genExp14Data() {
		Map<String, String> params = new HashMap<String, String>();
		String now = DateUtils.getCurrDate();
		String merOrderId = DateUtils.getCurrDate("yyyyMMddHHmmssSSS");
		String submitTime = now;

		String amt = HnapayRandom.genRandomDec(2);
		amt = new BigDecimal(amt).toString();
		// 报文密文参数
		params.put("tranAmt", "100.00");
		params.put("payType", "2");
		params.put("tradeFeeUnit", "2");
		params.put("tradeFeeAmt", "1");
		params.put("extraFeeUnit", "2");
		params.put("extraFeeAmt", "1");
		params.put("cardNo", "6258081687692968");
		params.put("holderName", "罗勇");
		params.put("cardAvailableDate", "0522");
		params.put("cvv2", "054");
		params.put("mobileNo", "18520149705");
		params.put("identityType", "01");
		params.put("identityCode", "411328198711250011");
		params.put("settleCardNo", "6212261714006233899");
		params.put("settleMobileNo", "18520149705");
		params.put("goodsInfo", "java编程思想");
		params.put("riskExpand", "风控扩展信息");
		params.put("notifyUrl", ExpConstant.NOTIFYURL);
		params.put("merUserIp", "127.0.0.1");
		params.put("orderExpireTime", "120");
		params.put("merUserId", "merUserId_20180326092015");
		// 报文明文参数
		params.put(ExpConstant.VERSION, "2.0");// 版本号
		params.put(ExpConstant.TRANCODE, "EXP14");// 交易代码
		params.put(ExpConstant.MERID, "11000004294");// 商户ID
		params.put(ExpConstant.MERORDERID, merOrderId);// 商户订单号
		params.put(ExpConstant.SUBMITTIME, submitTime);// 请求提交时间
		params.put(ExpConstant.MSGCIPHERTEXT, ExpUtil.encrpt("EXP14", params));// 报文密文
		params.put(ExpConstant.SIGNTYPE, "1");// 签名类型:RSA
		params.put(ExpConstant.MERATTACH, "1");// 附加数据
		params.put(ExpConstant.CHARSET, "1");// 编码方式:UTF-8
		try {
			params.put(ExpConstant.SIGNVALUE, ExpUtil.sign("EXP14", params));// 签名密文串
		} catch (Exception e) {
			e.printStackTrace();
			params.put(ExpConstant.SIGNVALUE, "");
		}

		return params;
	}

	/**
	 * 生成支付确认请求报文
	 */
	public static Map<String, String> genExp15Data1(Map<String, String> params12, String hanpayOrderId, String smsCode)
			throws Exception {
		Map<String, String> params = new HashMap<String, String>();

		// 报文密文参数
		params.put(ExpConstant.HNAPAYORDERID, "EXT2018122032612336");// 新生订单号
		params.put(ExpConstant.SMSCODE, "831591");// 短信验证码
		params.put(ExpConstant.MERUSERIP, "192.168.1.1");// 商户用户IP

		String now = DateUtils.getCurrDate();
		// 报文明文参数
		params.put(ExpConstant.VERSION, "2.0");// 版本号
		params.put(ExpConstant.TRANCODE, "EXP15");// 交易代码
		params.put(ExpConstant.MERID, params12.get(ExpConstant.MERID));// 商户ID
		params.put(ExpConstant.MERORDERID, "20181220164535700");// 商户订单号
		params.put(ExpConstant.SUBMITTIME, now);// 请求提交时间
		params.put(ExpConstant.MSGCIPHERTEXT, ExpUtil.encrpt("EXP15", params));// 报文密文
		params.put(ExpConstant.SIGNTYPE, "1");// 签名类型:RSA
		params.put(ExpConstant.MERATTACH, "1");// 附加数据
		params.put(ExpConstant.CHARSET, "1");// 编码方式:UTF-8
		try {
			params.put(ExpConstant.SIGNVALUE, ExpUtil.sign("EXP13", params));
		} catch (Exception e) {
			e.printStackTrace();
			params.put(ExpConstant.SIGNVALUE, "");
		}

		return params;
	}

	
}
