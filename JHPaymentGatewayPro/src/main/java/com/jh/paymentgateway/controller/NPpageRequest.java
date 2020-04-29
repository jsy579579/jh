package com.jh.paymentgateway.controller;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.jh.paymentgateway.util.np.Aes128Util;
import com.jh.paymentgateway.util.np.HttpClient4;
import com.jh.paymentgateway.util.np.HttpRequestUtils;
import com.jh.paymentgateway.util.np.RequestUtil;
import com.jh.paymentgateway.util.np.SignUtil;
import com.jh.paymentgateway.util.xskj.ExpUtil;

import cn.jh.common.tools.ResultWrap;
import cn.jh.common.utils.CommonConstants;

import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.annotate.JsonSerialize.Inclusion;
//import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.jh.paymentgateway.basechannel.BaseChannel;
import com.jh.paymentgateway.business.TopupPayChannelBusiness;
import com.jh.paymentgateway.config.RedisUtil;
import com.jh.paymentgateway.pojo.LMBankNum;
import com.jh.paymentgateway.pojo.NPBindCard;
import com.jh.paymentgateway.pojo.NPRegister;
import com.jh.paymentgateway.pojo.PaymentRequestParameter;


@Controller
@EnableAutoConfiguration
public class NPpageRequest  extends BaseChannel {
	private static final Logger LOG = LoggerFactory.getLogger(NPpageRequest.class);
	protected static final Charset UTF_8 = StandardCharsets.UTF_8;
	static ObjectMapper mapper = new ObjectMapper();

	static {
		mapper.configure(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		mapper.setSerializationInclusion(Inclusion.NON_NULL);
	}
	@Value("${payment.ipAddress}")
	private String paymentGatewayIp;

	@Autowired
	private RedisUtil redisUtil;

	@Autowired
	private TopupPayChannelBusiness topupPayChannelBusiness;

	@Value("${payment.ipAddress}")
	private String ip;
	
	@Value("${np.address}")
	private  String npurl="http://internetpay.naxinpay.com:8010";
	
	
	//代理商号
	private  String AGENTID="agent20190329172904_d2e";
	
	//签名密钥
	private  String   SIGNKEY="D58F55FCCA51D51D0795C37CD5934EE7";
	
	
	//加密密钥
	private String   ASRKEY="QODIEHF254DIELOD";
	

	public final static String DATE_YYYYMMDDHHMMSS = "yyyyMMddHHmmss";

	//商户报件地址
	public final static String ADDWECHATURL="/wxalipay/trade/addwechat_01";
	
	//商户修改省市县开户行信息
	public final static String UPDATE_BASICINFOURL="/wxalipay/trade/update_basicinfo";
	
	//修改结算卡接口
	public final static String UPDATE_CARDINFOURL="/wxalipay/trade/update_cardinfo";
	
	//修改商户费率接口
	public final static String UPDATE_FEESURL="/wxalipay/trade/update_fees";

	//查询进件商户号
	public final static String QUERY_MERURL="/wxalipay/citic/query_mer";
	
	
	//接口请求地址
	public final static String AUTHDEALURL="/wxalipay/up14/authdeal";
	
//	@Test
	public void test() {
		//进件商户
//		addwechat("钟守韩", "13166382981", "邮储银行", "", "6210984630005477081", "370983199302183717", "上海市", "上海市", "松江区", "古楼路2342号", "0.05", "200");
//		查询进件信息
		queryMer("411328198711250011", "18520149705", "6212261714006233899", "罗勇");
		
		//商户修改省市县开户行信息接口
//		updateBasicinfo("mer_20190330131157b4c", "山东省", "泰安市", "岱岳区", "PSBC", "邮储银行" ,"C8C51EC7D8BD40DD81B2A859C63CF421");
		//修改结算卡接口
//		updateCardinfo("mer_20190330131157b4c", "6210984630005477081","C8C51EC7D8BD40DD81B2A859C63CF421");
//		修改商户费率接口
//		updateFees("mer_20190330131157b4c",  "0.02", "200","C8C51EC7D8BD40DD81B2A859C63CF421");
//		 发起交易
//		authdeal2("mer_20190330131157b4c", "sddafsdfsfsdfsdfsdf1", "6225768681617732", "370983199302183717", "钟守韩", "13166382981","376", "0719",
//				"1000", "http://192.168.4.35/npcallback", "12", "25", "C8C51EC7D8BD40DD81B2A859C63CF421");
//		
	}
	//进件接口
	 /**
     * 
     * 绑定到账卡
     *@author Admin
     * @param
     * 1
     * **/
    @RequestMapping(method = RequestMethod.GET, value = "/v1.0/paymentgateway/topup/np/registerpage")
	public String npRegisterpage(HttpServletRequest request,	HttpServletResponse response,
			@RequestParam(value = "orderCode") String orderCode, Model model)
			throws IOException {
		PaymentRequestParameter prp = redisUtil.getPaymentRequestParameter(orderCode);	
		String ipAddress = paymentGatewayIp;
		model.addAttribute("ip", ipAddress);
		model.addAttribute("bankCard",prp.getDebitCardNo());
		model.addAttribute("cardType", prp.getDebitCardCardType());
		model.addAttribute("idCard", prp.getIdCard());
		model.addAttribute("phone", prp.getDebitPhone());
		model.addAttribute("userName", prp.getUserName());
		model.addAttribute("rate", prp.getRate());
		model.addAttribute("extraFee",prp.getExtraFee());
		model.addAttribute("bankName", prp.getDebitBankName());
		model.addAttribute("ordercode", prp.getOrderCode());
		return "npregistet";
	}
    
  //开户接口
  	@SuppressWarnings("unchecked")
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/topup/np/registet")
  	public  @ResponseBody Object  	addwechat(HttpServletRequest request, HttpServletResponse response,
  			@RequestParam(value = "ordercode") String ordercode,
  			@RequestParam(value = "provinceName") String provinceName,
  			@RequestParam(value = "cityName") String cityName){
  		PaymentRequestParameter prp = redisUtil.getPaymentRequestParameter(ordercode);	
  		Map<String, Object> map = new HashMap<String, Object>();
  		Map<String, String > addwechatreturn  = new HashMap<String, String >();
		String bankCard=prp.getDebitCardNo() ;
		String idCard= prp.getIdCard();
		String phone=prp.getPhone();
		String mobile=prp.getDebitPhone();
		String userName= prp.getUserName();
		String rate=prp.getRate();
		String bankName=prp.getDebitBankName();
		String extraFee=prp.getExtraFee(); 
		extraFee=new BigDecimal(extraFee).setScale(2, BigDecimal.ROUND_DOWN).toString();
		// 获取缩写
		LMBankNum sbcode = topupPayChannelBusiness.getLMBankNumCodeByBankName(bankName);
		String bankCode = sbcode.getBankNum();// 缩写
		NPRegister  np =topupPayChannelBusiness.getNPRegisterbyIdcard(idCard);
		if(np==null||!np.getStatus().equals("1")) {
			
			if(np==null) {
				addwechatreturn=(Map<String, String>) addwechat(userName, phone,mobile, bankName, bankCode, bankCard, idCard, provinceName, cityName
						, "未知区县", "位置街道", rate, extraFee);
			}
			
			if(np==null) {
				np=new  NPRegister();
				np.setCreateTime(new Date());
			}
			
			np.setBankCard(bankCard);
			np.setPhone(phone);
			np.setIdCard(idCard);
			np.setUserName(userName);
			np.setFeesDo(extraFee);
			np.setRateDo(rate);
			np.setProvinceName(provinceName);
			np.setCityName(cityName);
			
			String status=addwechatreturn.get("status");
			String resultCode=addwechatreturn.get("result_code");
			if(resultCode.equals("SUCCESS")&&status.equals("0")) {
				String merId=addwechatreturn.get("merid");
				String signKey=addwechatreturn.get("randomkey");
				np.setMerId(merId);
				np.setSignKey(signKey);
				np.setStatus("1");
				map=ResultWrap.init(CommonConstants.SUCCESS, "请求成功",ip + "/v1.0/paymentgateway/topup/np/paypage?orderCode="
						+prp.getOrderCode() );
			}else {
				np.setRemark(resultCode);
				map=ResultWrap.init(CommonConstants.FALIED, resultCode);
			}
			topupPayChannelBusiness.createNPRegister(np);
		}else if(!np.getBankCard().equals(bankCard)){
			//修改卡信息
			addwechatreturn=(Map<String, String>) updateBasicinfo(np.getMerId(), provinceName, cityName, "岱岳区", bankCode, bankName ,np.getSignKey());
			String status = addwechatreturn.get("status");
			String resultCode = addwechatreturn.get("result_code");
			if(resultCode.equals("SUCCESS")&&status.equals("0")) {
				np.setProvinceName(provinceName);
				np.setCityName(cityName);
				//修改结算卡接口
				addwechatreturn=(Map<String, String>) updateCardinfo(np.getMerId(), bankCard,np.getSignKey());
				status = addwechatreturn.get("status");
				resultCode = addwechatreturn.get("result_code");
				if(resultCode.equals("SUCCESS")&&status.equals("0")) {
					map=ResultWrap.init(CommonConstants.SUCCESS, "请求成功",ip + "/v1.0/paymentgateway/topup/np/paypage?orderCode="
							+prp.getOrderCode() );
					np.setBankCard(bankCard);
				}else {
					map=ResultWrap.init(CommonConstants.FALIED, resultCode);
				}
				topupPayChannelBusiness.createNPRegister(np);
			}else {
				map=ResultWrap.init(CommonConstants.FALIED, resultCode);
			}
			
		}else {
			map=ResultWrap.init(CommonConstants.SUCCESS, "请求成功",ip + "/v1.0/paymentgateway/topup/np/paypage?orderCode="
					+prp.getOrderCode() );
		}
		
  		return map;
  	}
  	
  	
    /**
     * 
     * 支付界面
     *@author Admin
     * @param
     * 1
     * **/
    @RequestMapping(method = RequestMethod.GET, value = "/v1.0/paymentgateway/topup/np/paypage")
    public String nppaypage(HttpServletRequest request,	HttpServletResponse response,
    		@RequestParam(value = "orderCode") String orderCode, Model model)
    				throws IOException {
    	PaymentRequestParameter prp = redisUtil.getPaymentRequestParameter(orderCode);
		String bankCard = prp.getBankCard();
		String bankName = prp.getCreditCardBankName();
		String cardType = prp.getCreditCardCardType();

		String expiredTime = this.expiredTimeToMMYY(prp.getExpiredTime());
		String securityCode = prp.getSecurityCode();
		NPBindCard npbc=topupPayChannelBusiness.getNPBindCardbyBankCard(bankCard);
		if(npbc!=null) {
			model.addAttribute("duedate", npbc.getDuedate());
			model.addAttribute("billdate", npbc.getBilldate());
		}else {
			model.addAttribute("duedate", "");
			model.addAttribute("billdate", "");
		}
		model.addAttribute("ordercode", orderCode);
		model.addAttribute("expiredTime", expiredTime);
		model.addAttribute("securityCode", securityCode);
		model.addAttribute("bankName", bankName);
		model.addAttribute("cardType", cardType);
		model.addAttribute("bankCard", bankCard);
		model.addAttribute("ipAddress", ip);
    	return "nppay";
    }
	
  //交易接口
  	@SuppressWarnings("unchecked")
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/topup/np/pay")
  	public  @ResponseBody Object  	nppay(HttpServletRequest request, HttpServletResponse response,
  			@RequestParam(value = "ordercode") String ordercode,
  			@RequestParam(value = "cvv2") String cvv2,
  			@RequestParam(value = "validity") String validity,
  			@RequestParam(value = "duedate") String duedate,
  			@RequestParam(value = "billdate") String billdate){
  		PaymentRequestParameter prp = redisUtil.getPaymentRequestParameter(ordercode);	
  		Map<String, Object> map = new HashMap<String, Object>();
  		Map<String, String > authdealreturn  = new HashMap<String, String >();
  		String backurl=ip+"/v1.0/paymentgateway/topup/np/callback";
		String bankCard=prp.getBankCard() ;
		String idCard= prp.getIdCard();
		String mobile=prp.getCreditCardPhone();
		validity = this.expiredTimeToMMYY(validity);
		String userName= prp.getUserName();
		String amount =prp.getAmount();
		NPRegister  np =topupPayChannelBusiness.getNPRegisterbyIdcard(idCard);
		NPBindCard npbc=topupPayChannelBusiness.getNPBindCardbyBankCard(bankCard);
		if(npbc==null) {
			npbc=new NPBindCard();
			npbc.setCreateTime(new Date());
		}
		npbc.setBankCard(bankCard);
		npbc.setCvv2(cvv2);
		npbc.setPhone(mobile);
		npbc.setBilldate(billdate);
		npbc.setDuedate(duedate);
		npbc.setIdCard(idCard);
		npbc.setValidity(validity);
		npbc=topupPayChannelBusiness.createNPBindCard(npbc);
		authdealreturn=(Map<String, String>) authdeal(np.getMerId(), prp.getOrderCode(), bankCard, idCard, userName, mobile, cvv2, validity, amount, backurl, duedate, billdate, np.getSignKey());
		String status= authdealreturn.get("status");
		String resultCode= authdealreturn.get("result_code");
		String resultMsg= authdealreturn.get("result_msg");
		if("0".equals(status)&&("00".equals(resultCode)||"04".equals(resultCode)||"05".equals(resultCode)||"03".equals(resultCode))) {
			map=ResultWrap.init(CommonConstants.SUCCESS, "下单成功，具体与实际扣款为主!!");
		}else{
			map=ResultWrap.init(CommonConstants.FALIED, resultMsg);
		}
  		return map;
  	}
  
	
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/topup/np/callback")
  	public  @ResponseBody Object  	npCallBack(HttpServletRequest request, HttpServletResponse response){
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
		boolean  verify = true;
		if(!verify) {
			LOG.info("异步通知---------------验签失败");
			return "fail";
		}
		String orderid=(String) notifymap.get("orderid");
		
		String status=(String) notifymap.get("status");
		
		String resultCode=(String) notifymap.get("result_code");
		
		String resultMsg=(String) notifymap.get("result_msg");
		
		String time=(String) notifymap.get("time");
		
		
		PaymentRequestParameter bean = redisUtil.getPaymentRequestParameter(orderid);
		if ("0".equals(status)&&"0".equals(resultCode)) {
			this.updateSuccessPaymentOrder(bean.getIpAddress(), orderid,time);
			
			return "200";
		}
		this.addOrderCauseOfFailure(orderid, resultMsg, bean.getIpAddress());
		
		return ResultWrap.err(LOG, CommonConstants.FALIED, orderid + "非成功回调",resultMsg);
  	}
  	
  	
  	
	/**
	 * 进件商户
	 * */
	@SuppressWarnings("unchecked")
	public  Object addwechat(String userName,String phone ,String mobile,String bankname,String bankCode ,String cardno,String idCard,
			String province,String city,String county,String address,String rateDo ,String feesDo) {
		String url=npurl+ADDWECHATURL;
		feesDo=new BigDecimal(feesDo).multiply(new BigDecimal("100")).setScale(0, BigDecimal.ROUND_DOWN).toString();
		Map<String, String> req = new TreeMap<String, String>();
		Map<String, String> extendparams = new TreeMap<String, String>();
		Map<String, Object> extendparams11_6 = new TreeMap<String, Object>();
		Map<String, String > addwechatreturn  = new HashMap<String, String >();
		
		//垫资封顶手续费
		extendparams.put("prepaidMax", feesDo);
		//垫资费率
		extendparams.put("prepaidFees", "1");
		//d0费率
		extendparams.put("rateDo", rateDo);
		
		extendparams11_6.put("11_6", extendparams);
		String parameterJSON = JSONObject.toJSONString(extendparams11_6);
		
		//代理商号
		req.put("agentid",AGENTID);
		//商户名称
		req.put("wename", userName);
		//商户简称
		req.put("weabbr",userName);
		//商户联系电话
		req.put("telephone", phone);
		//客服电话
		req.put("serverph", phone);
		//邮箱
		req.put("email", phone+"@163.com");
		//省(结算卡)
		req.put("province", province);
		//市(结算卡)
		req.put("city", city);
		//县(结算卡)
		req.put("county", county);
		//地址
		req.put("address", address);
		//联行号
		req.put("bankcode",bankCode);
		//银行名称
		req.put("bankname", bankname);
		//身份证号
		req.put("certno", idCard);
		//开户时绑定的手机号码
		req.put("mobile", mobile);
		//银行卡号
		req.put("cardno", cardno);
		//持卡人姓名
		req.put("realname", userName);
		//扩展费率参数
		req.put("extendparams", parameterJSON);
		//签名值
		try {
			req.put("sign", SignUtil.getSign(req, SIGNKEY));
			LOG.info("============ 请求data:" + req);
			String resStr= HttpClient4.doPost(url, req);
//			resStr = new String(resStr, UTF_8);
			LOG.info("============ 返回报文原文:" + resStr);
			addwechatreturn=JSONObject.parseObject(resStr, new TypeReference<Map<String, String>>(){});
		} catch (Exception e) {
			e.printStackTrace();
		}
		return addwechatreturn;
	}
	/**
	 * 商户修改省市县开户行信息接口
	 * */
	@SuppressWarnings("unchecked")
	public Object updateBasicinfo(String merid,String province,String city,String county,String bankcode,String bankname,String signkey) {
		Map<String,String>  updateBasicinfo =new TreeMap<String,String>();
		Map<String,String>  updateBasicinfoReturn =new HashMap<String,String>();
		//商户号
		updateBasicinfo.put("merid", merid);
		
		//省(结算卡)
		updateBasicinfo.put("province", province);
		
		//市(结算卡)
		updateBasicinfo.put("city", city);
		
		//县(结算卡)
		updateBasicinfo.put("county",county);
		
		//联行号
		updateBasicinfo.put("bankcode", bankcode);
		
		//开户行名
		updateBasicinfo.put("bankname", bankname);
		
		//随机数
		updateBasicinfo.put("random", random(10000));
		String url=npurl+UPDATE_BASICINFOURL;
		//签名值
		try {
			updateBasicinfo.put("sign", SignUtil.getSign(updateBasicinfo, signkey));
			LOG.info("============ 请求data:" + updateBasicinfo);
			String resStr= HttpClient4.doPost(url, updateBasicinfo);
			LOG.info("============ 返回报文原文:" + resStr);
			updateBasicinfoReturn=JSONObject.parseObject(resStr, new TypeReference<Map<String, String>>(){});
		} catch (Exception e) {
			e.printStackTrace();
		}
				
		return updateBasicinfoReturn;
	}
	
	/**
	 * 4.修改结算卡接口
	 * */
	@SuppressWarnings("unchecked")
	public Object updateCardinfo(String merid,String cardno,String signkey ) {
		Map<String,String>  updateCardinfo =new TreeMap();
		Map<String,String>  updateCardinfoReturn =new HashMap<String,String>();
		//商户号
		updateCardinfo.put("merid", merid);
		
		//银行卡号
		updateCardinfo.put("cardno", cardno);
		
		//随机数
		updateCardinfo.put("random", random(10000));
		
		String url=npurl+UPDATE_CARDINFOURL;
		//签名值
		try {
			updateCardinfo.put("sign", SignUtil.getSign(updateCardinfo, signkey));
			LOG.info("============ 请求data:" + updateCardinfo);
			String resStr= HttpClient4.doPost(url, updateCardinfo);
//					resStr = new String(resStr, UTF_8);
			LOG.info("============ 返回报文原文:" + resStr);
			updateCardinfoReturn=JSONObject.parseObject(resStr, new TypeReference<Map<String, String>>(){});
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return updateCardinfoReturn;
	}
	/**
	 * 
	 * 4.5.修改商户费率接口
	 * */	
	@SuppressWarnings("unchecked")
	public Object updateFees(String merid,String rateDo,String feesDo ,String signkey ) {
		Map<String,String>  updateFees =new TreeMap();
		feesDo=new BigDecimal(feesDo).multiply(new BigDecimal("100")).setScale(0, BigDecimal.ROUND_DOWN).toString();
		Map<String,String>  updateFeesReturn =new HashMap<String,String>();
		Map<String, Object> extendparams11_6 = new TreeMap<String, Object>();
		Map<String, String> extendparams = new TreeMap();
		//垫资封顶手续费
		extendparams.put("prepaidMax", feesDo);
		//垫资费率
		extendparams.put("prepaidFees", "1");
		//d0费率
		extendparams.put("rateDo", rateDo);
		extendparams11_6.put("11_6", extendparams);
		String parameterJSON = JSONObject.toJSONString(extendparams11_6);
		//商户号
		updateFees.put("merid", merid);
		
		//银行卡号
		updateFees.put("extendparams", parameterJSON);
		
		//随机数
		updateFees.put("random", random(1000));
		
		String url=npurl+UPDATE_FEESURL;
		//签名值
		try {
			updateFees.put("sign", SignUtil.getSign(updateFees, signkey));
			LOG.info("============ 请求data:" + updateFees);
			String resStr= HttpClient4.doPost(url, updateFees);
//					resStr = new String(resStr, UTF_8);
			LOG.info("============ 返回报文原文:" + resStr);
			updateFeesReturn=JSONObject.parseObject(resStr, new TypeReference<Map<String, String>>(){});
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return updateFeesReturn;
	}
	
	
	
	/**
	 * 查询进件信息
	 * 
	 * */
	@SuppressWarnings("unchecked")
	public  Object queryMer(String idCard,String phone,String bankCard,String userName) {
		Map<String,String> queryMer=new TreeMap<String, String>();
		Map<String,String> queryMerReturn=new TreeMap<String, String>();
		String url=npurl+QUERY_MERURL;
		//代理商号
		queryMer.put("agentid", AGENTID);
		//身份证号
		queryMer.put("cerno", idCard);
		//手机号
		queryMer.put("mobile", phone);
		//卡号
		queryMer.put("accno",bankCard);
		//姓名
		queryMer.put("name", userName);
		try {
			queryMer.put("sign", SignUtil.getSign(queryMer, SIGNKEY));
			LOG.info("============ 查询进件请求data:" + queryMer);
			String resStr= HttpClient4.doPost(url, queryMer);
			LOG.info("============ 返回报文原文:" + resStr);
			queryMerReturn=JSONObject.parseObject(resStr, new TypeReference<Map<String, String>>(){});
			/*if(resJson.get("status").equals("SUCCESS")) {
				String merId=resJson.getString("merchantid");
				String randomKey=resJson.getString("key");
				NPRegister NPRegister =topupPayChannelBusiness.getNPRegisterbyIdcard(idCard);
				if(NPRegister==null) {
					NPRegister=new NPRegister();
					NPRegister.setIdCard(idCard);
					NPRegister.setBankCard(bankCard);
					NPRegister.setPhone(phone);
					NPRegister.setUserName(userName);
					NPRegister.setCreateTime(new Date());
				}
				NPRegister.setMerId(merId);
				NPRegister.setRandomKey(randomKey);
				NPRegister =topupPayChannelBusiness.createNPRegister(NPRegister);
			}*/
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return queryMerReturn;
	}
	
	
	/**
	 * 发起交易
	 * 
	 * */
	@SuppressWarnings("unchecked")
	public Object authdeal(String  merid,String orderid,String bankCard ,String idCard,String userName,String phone,
			String cvv2,String validity,String amount,String backurl,String duedate,String billdate,String signKey) {
		String url=npurl+AUTHDEALURL;
		Map<String, String> params = new HashMap<String, String>();
		amount=new BigDecimal(amount).multiply(new BigDecimal("100")).setScale(0, BigDecimal.ROUND_DOWN).toString();
		params.put("merid", merid);//进件返回商户号
		params.put("orderid", orderid);
		params.put("account", bankCard);
		params.put("idno", idCard);
		params.put("name", userName);
		params.put("mobile", phone);
		params.put("cvv2", cvv2);
		params.put("validity", validity);
		params.put("amount", amount);
		params.put("backurl",backurl);
		params.put("duedate", duedate);
		params.put("billdate",billdate);
		String sign = RequestUtil.getSign(params, signKey);//进件返回签名密钥
		String body = Aes128Util.encrypt(JSONObject.toJSONString(params),ASRKEY);
		Map<String, String> bodyMap = new HashMap<String, String>();
		bodyMap.put("agentid", AGENTID);
		bodyMap.put("body", body);
		bodyMap.put("sign", sign);
		LOG.info("============ 支付请求body:" + params);
		LOG.info("============ 支付请求data:" + bodyMap);
		JSONObject resjosn = HttpRequestUtils.httpPost(url,(Object)bodyMap);	
		LOG.info("============ 返回报文原文:" + resjosn);
		return resjosn;
	}
	
	public String random(int num) {
		
		int  random=(int)((Math.random()*9+1)*num);
		return random+"";
	}
	
	
}
