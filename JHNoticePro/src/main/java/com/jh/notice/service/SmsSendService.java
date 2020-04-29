package com.jh.notice.service;

import java.io.IOException;
import java.lang.reflect.Array;
import java.net.URI;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.time.DateFormatUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.jh.notice.business.BrandSMSCountBusiness;
import com.jh.notice.business.SMSBlackListBusiness;
import com.jh.notice.business.SmsSendBusiness;
import com.jh.notice.config.PropertiesConfig;
import com.jh.notice.pojo.BrandSMSCount;
import com.jh.notice.pojo.SMSBlackList;
import com.jh.notice.redis.RedisUtil;
import com.jh.notice.repository.SMSTemplateRepository;
import com.jh.notice.util.NoticeConstants;
import com.jh.notice.util.Util;

import cn.jh.common.utils.AuthorizationHandle;
import cn.jh.common.utils.CommonConstants;
import cn.jh.common.utils.DateUtil;
import cn.jh.common.utils.ExceptionUtil;
import net.sf.json.JSONObject;




@Controller
@EnableAutoConfiguration
public class SmsSendService {


	private static final Logger LOG = LoggerFactory.getLogger(SmsSendService.class);


	@Autowired
	private SmsSendBusiness smsSendBusiness;

	@Autowired
	private RestTemplate restTemplate;

	@Autowired
	private BrandSMSCountBusiness brandSMSCountBusiness;

	@Autowired
	private SMSBlackListBusiness smsBlackListBusiness;

	@Autowired
	private RedisUtil redisUtil;

	@Autowired
	private SMSTemplateRepository smsTemplateRepository;

	@Autowired
	StringRedisTemplate redisTemplate;
	
	@Autowired
	private Util util;

	/**发送短信*/
	@RequestMapping(method=RequestMethod.GET,value="/v1.0/notice/sms/send")
	public @ResponseBody Object sendMsg(HttpServletRequest request, 
			@RequestParam(value = "phone") String phone,
			@RequestParam(value = "brand_id", defaultValue = "1", required=false) long brandid){
		Map<String,Object> map = new HashMap<String, Object>();

		Map<String, Object> verifyStringFiledIsNull = AuthorizationHandle.verifyStringFiledIsNull(phone);
		if(!CommonConstants.SUCCESS.equals(verifyStringFiledIsNull.get(CommonConstants.RESP_CODE))){
			return verifyStringFiledIsNull;
		}

		String ipAddress = getRequestIP(request);
		System.out.println("真实请求ip==========="+ipAddress);
		if (ipAddress != null && ipAddress.contains("127.0.0.1")) {
			ipAddress = ipAddress.replaceAll("127.0.0.1", "");
		}

		if (ipAddress != null && ipAddress.contains("localhost")) {
			ipAddress = ipAddress.replaceAll("localhost", "");
		}

		String ips = "";
		for(String ip:ipAddress.split(",")) {
			ip = ip.trim();
			if ("".equals(ip)) {
				continue;
			}
			ips = ips + ip +",";
		}
		System.out.println("ips==========="+ips);
//		ips = ips.substring(0,ips.length()-1);
		int index = ips.indexOf(",");
		ipAddress=ips.substring(0,index);
		LOG.info("============ipAddress:" + ipAddress + "=============phone:" + phone);

		String[] ipAddressStr = ipAddress.split(",");
		List<String> ipList = new ArrayList<String>();
		for(int i = 0;i < ipAddressStr.length + 1;i++){
			if(i == ipAddressStr.length){
				ipList.add(phone);
			}else{
				ipList.add(ipAddressStr[i]);
			}
		}

//		for(String ip:ipList){
//			ip = ip.trim();
//			SMSBlackList smsBlackList = smsBlackListBusiness.findByIpAddress(ip);
//			if (smsBlackList != null || ip.startsWith("121.56.")) {
//				LOG.info("拦截请求==============" + ipList);
//				map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
//				map.put(CommonConstants.RESP_MESSAGE, "您的操作过于频繁,请明天再试!");
//				return map;
//			}
//		}

//		int sendCount = smsSendBusiness.findCountByIpAddress(ipAddress);

		int phoneCount = smsSendBusiness.findCountByPhoneAndDate(phone);

//		if(sendCount > 50||phoneCount >10)
			if(phoneCount >15){
//			if(sendCount > 50){
//				SMSBlackList smsBlackList;
//				smsBlackList = new SMSBlackList();
//				smsBlackList.setIpAddress(ipAddress);
//				smsBlackListBusiness.save(smsBlackList);
//				smsBlackList = new SMSBlackList();
//				smsBlackList.setIpAddress(phone);
//				smsBlackListBusiness.save(smsBlackList);
//			}
			LOG.info("拦截请求==============" + ipList);
			map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			map.put(CommonConstants.RESP_MESSAGE, "您的操作过于频繁,请明天再试!");
			return map;
		}

		Map<String, Object> restClientLimit = redisUtil.restClientLimit("sendMsg",60, phone);
		if(!CommonConstants.SUCCESS.equals(restClientLimit.get(CommonConstants.RESP_CODE))){
			return restClientLimit;
		}

		Random random = new Random();
		int x = random.nextInt(899999);
		x = x+100000;
		Map params  = new HashMap<String, String>();
		params.put("phone", phone);
		params.put("code", x+"");

		/**
		 * 获取通道信息
		 * *URL：/v1.0/user/brand/query/id
		 * **/
		RestTemplate restTemplate=new RestTemplate();
		URI uri = util.getServiceUrl("user", "error url request!");
		String url = uri.toString() + "/v1.0/user/brand/query/id?brand_id="+brandid;
		String resultStr = restTemplate.getForObject(url,  String.class);
		JSONObject jsonObject =  JSONObject.fromObject(resultStr);
		JSONObject brand=jsonObject.getJSONObject("result");
		String  tpl_id=brand.getString("tplid");
		smsSendBusiness.sendSmsMessage(phone,tpl_id, params,ipAddress);
		map.put(CommonConstants.RESP_CODE,CommonConstants.SUCCESS);
		map.put(CommonConstants.RESP_MESSAGE, "成功");
		return map;

	}




	/**发送还款提醒短信
	 * 
	 * 【还款精灵】您尾号为#bankNum#的#bankName#银行卡在#platform#平台,制定的还款计划有一笔金额为#balance#￥的还款失败，请及时查看处理！
	 * 
	 * */
	@RequestMapping(method=RequestMethod.POST,value="/v1.0/notice/sms/inform/send")
	public @ResponseBody Object sendrepaymentMsg(HttpServletRequest request, 
			//下单人
			@RequestParam(value = "user_id") String userId,

			//发送手机号
			@RequestParam(value = "phone") String phone,

			//所属平台
			@RequestParam(value = "brand_id") long brandid,

			//发送模板
			@RequestParam(value = "tpl_id", defaultValue = "repayment", required=false) String tplId

			){
		Map<String,Object> map = new HashMap<String, Object>();

		Map<String, Object> verifyStringFiledIsNull = AuthorizationHandle.verifyStringFiledIsNull(phone);
		if(!CommonConstants.SUCCESS.equals(verifyStringFiledIsNull.get(CommonConstants.RESP_CODE))){
			return verifyStringFiledIsNull;
		}
		Map<String,String> params =new HashMap<String,String>();
		Map<String, String[]> parameterMap = request.getParameterMap();
		Set<String> keySet = parameterMap.keySet();
		for (String key : keySet) {
			String[] strings = parameterMap.get(key);
			params.put(key, strings[0]);
		}
		String dateDay=DateFormatUtils.format(new Date(), "yyyy-MM-dd");
		Map<String, Object> restClientLimit = redisUtil.restClientLimitval(dateDay+tplId, params.toString(), 60*60*12, phone);
		if(!CommonConstants.SUCCESS.equals(restClientLimit.get(CommonConstants.RESP_CODE))){
			return restClientLimit;
		}
		BrandSMSCount brandSMSCount = brandSMSCountBusiness.findByBrandId(brandid+"");

		String ipAddress=getRequestIP(request);
		if(brandSMSCount != null && brandSMSCount.getSmsCount().intValue() > 0 ){
			smsSendBusiness.sendSmsInformMessage(phone, tplId, params, ipAddress, brandid+"");
		}else{
			map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			map.put(CommonConstants.RESP_MESSAGE, "短信次数不足,请联系管理员及时充值!");
			return map;
		}
		if(brandSMSCount.getSmsCount().intValue() != -1024){
			brandSMSCount.setSmsCount(brandSMSCount.getSmsCount()-1);
		}
		int smsCount = brandSMSCount.getSmsCount().intValue();
		if( smsCount<=100){
			try {
				String url = "http://user/v1.0/user/brand/query/id?brand_id=" + brandid;
				String resultString = restTemplate.getForObject(url, String.class);
				JSONObject resultJSON = JSONObject.fromObject(resultString);
				resultJSON = resultJSON.getJSONObject(CommonConstants.RESULT);
				String manageid = resultJSON.getString("manageid");
				String message = "";
				message = "您帐户中的短信次数已不足" + smsCount + "次,为不影响用户正常使用,请及时充值!";

				util.pushMessage(manageid, message, "短信推送次数提醒","smsjp");
			} catch (RestClientException e) {
				e.printStackTrace();
				LOG.error("",e);
			}
		}
		map.put(CommonConstants.RESP_CODE,CommonConstants.SUCCESS);
		map.put(CommonConstants.RESP_MESSAGE, "成功");
		return map;

	}



	/**短信外放*/
	@RequestMapping(method=RequestMethod.GET,value="/v1.0/notice/smsout/send")
	public @ResponseBody Object sendMsg(HttpServletRequest request, 
			@RequestParam(value = "phone") String phone,
			@RequestParam(value = "brand_id", defaultValue = "1", required=false) long brandid,
			@RequestParam(value = "smscode") String Smscode
			){
		Map<String,Object> map = new HashMap<String, Object>();

		if(Smscode==null||Smscode.length()!=6||!isNumeric(Smscode)){
			LOG.info("拦截请求==============" + Smscode);
			map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			map.put(CommonConstants.RESP_MESSAGE, "验证码必须是六位数字");
			return map;

		}
		Map<String, Object> verifyStringFiledIsNull = AuthorizationHandle.verifyStringFiledIsNull(phone);
		if(!CommonConstants.SUCCESS.equals(verifyStringFiledIsNull.get(CommonConstants.RESP_CODE))){
			return verifyStringFiledIsNull;
		}

		String ipAddress = getRequestIP(request);

		LOG.info("============ipAddress:" + ipAddress + "=============phone:" + phone);

		String[] ipAddressStr = ipAddress.split(",");
		List<String> ipList = new ArrayList<String>();
		for(int i = 0;i < ipAddressStr.length + 1;i++){
			if(i == ipAddressStr.length){
				ipList.add(phone);
			}else{
				ipList.add(ipAddressStr[i]);
			}
		}

		for(String ip:ipList){
			ip = ip.trim();
			SMSBlackList smsBlackList = smsBlackListBusiness.findByIpAddress(ip);
			if (smsBlackList != null || ip.startsWith("121.56.")) {
				LOG.info("拦截请求==============" + ipList);
				map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
				map.put(CommonConstants.RESP_MESSAGE, "您的操作过于频繁,请明天再试!");
				return map;
			}
		}

		int sendCount = smsSendBusiness.findCountByIpAddress(ipAddress);

		int phoneCount = smsSendBusiness.findCountByPhoneAndDate(phone);

		if(sendCount > 50||phoneCount >10){
			SMSBlackList smsBlackList = new SMSBlackList();
			smsBlackList.setIpAddress(ipAddress);
			smsBlackListBusiness.save(smsBlackList);
			smsBlackList = new SMSBlackList();
			smsBlackList.setIpAddress(phone);
			smsBlackListBusiness.save(smsBlackList);
			LOG.info("拦截请求==============" + ipList);
			map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			map.put(CommonConstants.RESP_MESSAGE, "您的操作过于频繁,请明天再试!");
			return map;
		}

		Map<String, Object> restClientLimit = redisUtil.restClientLimit("sendMsg",60, phone);
		if(!CommonConstants.SUCCESS.equals(restClientLimit.get(CommonConstants.RESP_CODE))){
			return restClientLimit;
		}

		//		Random random = new Random();
		//		int x = random.nextInt(899999);
		//		x = x+100000;
		Map params  = new HashMap<String, String>();
		params.put("phone", phone);
		params.put("code", Smscode+"");

		/**
		 * 获取通道信息
		 * *URL：/v1.0/user/brand/query/id
		 * **/
		RestTemplate restTemplate=new RestTemplate();
		URI uri = util.getServiceUrl("user", "error url request!");
		String url = uri.toString() + "/v1.0/user/brand/query/id?brand_id="+brandid;
		String resultStr = restTemplate.getForObject(url,  String.class);
		JSONObject jsonObject =  JSONObject.fromObject(resultStr);
		JSONObject brand=jsonObject.getJSONObject("result");
		String  tpl_id=brand.getString("tplid");
		smsSendBusiness.sendSmsMessage(phone,tpl_id, params,ipAddress);
		map.put(CommonConstants.RESP_CODE,CommonConstants.SUCCESS);
		map.put(CommonConstants.RESP_MESSAGE, "成功");
		return map;

	}


	/**获取验证码**/
	@RequestMapping(method=RequestMethod.GET,value="/v1.0/notice/sms/vericode")
	public @ResponseBody Object queryVericode(HttpServletRequest request, @RequestParam(value = "phone") String phone){		

		Map map = new HashMap();
		map.put(CommonConstants.RESP_CODE,CommonConstants.SUCCESS);
		map.put(CommonConstants.RESULT, smsSendBusiness.querySmscodeByPhone(phone));
		map.put(CommonConstants.RESP_MESSAGE, "成功");

		return map;

	}
	/**
	 * 获取缓存验证码并验证  2019.4.17 
	 * @param phone
	 * @param vericode
	 * @return
	 */
	@RequestMapping(method=RequestMethod.GET,value="/v1.0/notice/sms/queryCacheVericode")
	public Object queryCacheVericode(
			@RequestParam(value = "phone") String phone,
			@RequestParam(value = "vericode") String vericode
			) {
		Map map = new HashMap();
		//到redis缓存中获取对应用户的验证码
		String vCode=String.valueOf(redisTemplate.opsForValue().get(phone));
		LOG.info("验证码===================================>"+vCode);
		if(!vCode.equals(vericode)||vCode==null || vCode=="") {
			LOG.info("验证码输入错误=====================================" + vCode);
			map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			map.put(CommonConstants.RESP_MESSAGE, "验证码输入有误"); 
			return map;
		}
		LOG.info("用户"+phone+"验证成功");
		map.put(CommonConstants.RESP_CODE,CommonConstants.SUCCESS);
		map.put(CommonConstants.RESP_MESSAGE, "成功");
		
		return map;
	}



	//包装TX快捷用的发送短信的接口
	@RequestMapping(method=RequestMethod.POST,value="/v1.0/notice/sms/sendforquick")
	public @ResponseBody Object sendMsg(HttpServletRequest request, 
			@RequestParam(value = "phone") String phone,
			@RequestParam(value = "tplId") String tplId){
		Map<String,Object> map = new HashMap<String, Object>();


		Random random = new Random();
		int x = random.nextInt(899999);
		x = x+100000;
		Map params  = new HashMap<String, String>();
		params.put("phone", phone);
		params.put("code", x+"");

		smsSendBusiness.sendSmsMessage(phone,tplId, params,"");
		map.put(CommonConstants.RESP_CODE,CommonConstants.SUCCESS);
		map.put(CommonConstants.RESP_MESSAGE, "成功");
		return map;

	}


	/**分页获取*/
	@RequestMapping(method=RequestMethod.GET,value="/v1.0/notice/sms/query")
	public @ResponseBody Object addFans(HttpServletRequest request,
			@RequestParam(value = "page", defaultValue = "0", required = false) int page,
			@RequestParam(value = "size", defaultValue = "20", required = false) int size,
			@RequestParam(value = "order", defaultValue = "DESC", required = false) Sort.Direction direction,
			@RequestParam(value = "sort", defaultValue = "createTime", required = false) String sortProperty, 
			@RequestParam(value = "phone",  required = false) String phone, 
			@RequestParam(value = "startTime",  required = false) String startTime, 
			@RequestParam(value = "endTime",  required = false) String endTime) throws IOException{

		Map map = new HashMap();
		Date startDate = null;
		Date endDate = null;

		if(startTime == null || startTime.equalsIgnoreCase("")){
			if(endTime !=null && !endTime.equalsIgnoreCase("")){
				map.put(CommonConstants.RESP_CODE,NoticeConstants.ERROR_PARAM);
				map.put(CommonConstants.RESP_MESSAGE, "参数错误");
				return map;
			}
		}else{
			startDate = DateUtil.getDateFromStr(startTime);
			if(endTime !=null && !endTime.equalsIgnoreCase("")){
				endDate = DateUtil.getDateFromStr(endTime);
			}
		}

		Pageable pageable = new PageRequest(page, size, new Sort(direction,sortProperty));

		map.put(CommonConstants.RESP_CODE,CommonConstants.SUCCESS);
		map.put(CommonConstants.RESP_MESSAGE, "成功");
		map.put(CommonConstants.RESULT, smsSendBusiness.findSmsRecord(pageable, phone, startDate, endDate));
		return map;
	}
	/**
	 * 利用正则表达式判断字符串是否是数字
	 * @param str
	 * @return
	 */
	public boolean isNumeric(String str){
		Pattern pattern = Pattern.compile("[0-9]*");
		Matcher isNum = pattern.matcher(str);
		if( !isNum.matches() ){
			return false;
		}
		return true;
	}

	/**
	 * IP地址获取
	 * 
	 * **/

	public  String  getRequestIP(HttpServletRequest request){
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

		return ipAddress;
	}

	public static Map<String,String> getMap(HttpServletRequest request){

		Map<String,String> map =new HashMap<String,String>();
		map.put("bankNum", "232");
		map.put("bankName", "撒大声地");
		map.put("platform", "大声道");
		map.put("balance", "22.00");
		return map;
	}
	/*public  static void main(String[] args) {

	   Map<String,String> pare=getMap();
	   System.out.println("数据："+pare.toString());

    }*/
	/**
	 ** 发送会员升级通知醒短信
	 * */
	@RequestMapping(method=RequestMethod.POST,value="/v1.0/notice/sms/inform/sendTwo")
	public @ResponseBody Object sendrepaymentMsgTwo(HttpServletRequest request,
												 //下单人
												 @RequestParam(value = "user_id") String userId,
												 //发送手机号
												 @RequestParam(value = "phone") String phone,
												 //所属平台
												 @RequestParam(value = "brand_id") long brandid,
												 //发送模板
												 @RequestParam(value = "tpl_id", defaultValue = "repayment", required=false) String tplId,
												 @RequestParam(value = "content", defaultValue = "repayment", required=false) String content
	){
		Map<String,Object> map = new HashMap<String, Object>();
		Map params = new HashMap();
		params.put("content",content);
		smsSendBusiness.sendSmsNotice(phone,tplId, params,"0.0.0.0");
		map.put(CommonConstants.RESP_CODE,CommonConstants.SUCCESS);
		map.put(CommonConstants.RESP_MESSAGE, "成功");
		return map;
	}

}
