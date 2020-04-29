package com.jh.user.service;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URI;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.criteria.CommonAbstractCriteria;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.jh.user.business.UserLoginRegisterBusiness;
import com.jh.user.business.UserTempBusiness;
import com.jh.user.pojo.User;
import com.jh.user.pojo.UserTemp;
import com.jh.user.util.Util;
import cn.jh.common.utils.CommonConstants;
import cn.jh.common.utils.ExcelUtil;
import cn.jh.common.utils.ExceptionUtil;
import cn.jh.common.utils.Md5Util;
import net.sf.json.JSONObject;

@Controller
@EnableAutoConfiguration
public class UserTempService {
	
	private final Logger LOG = LoggerFactory.getLogger(getClass());
	
	@Autowired
	private UserTempBusiness userTempBusiness;
	
	@Autowired
	private UserLoginRegisterBusiness userLoginRegisterBusiness;
	
	@Autowired
	private RestTemplate restTemplate;
	
	@Autowired
	private Util util;
	
	@RequestMapping(method=RequestMethod.POST,value="/v1.0/user/usertemp/add")
	public @ResponseBody Object updateUser(){
		int page = 0;
		int size = 20;
		Sort.Direction direction = Sort.Direction.DESC;
		String sortProperty = "phone";
		List<UserTemp> userTemps;
		do{
			Pageable pageable = new PageRequest(page, size, new Sort(direction, sortProperty));
			userTemps = userTempBusiness.findAll(pageable);
			for(UserTemp userTemp:userTemps){
				User user = new User();
				user.setCreateTime(new Date());
				user.setOpenid(null);
				user.setPassword(Md5Util.getMD5("123456"));
				user.setPhone(userTemp.getPhone());
				user.setFullname(userTemp.getRealName());
				user.setPaypass(Md5Util.getMD5("123456"));
				user.setUnionid(null);
				user.setValidStatus(0);
				user.setInviteCode("15072591314");
				user.setBrandId(413L);
				user.setBrandname("亿玖玖");
				user.setProvince(null);
				user.setCity(null);
				user.setCounty(null);
				user.setPreUserId(19064938L);
				user.setPreUserPhone("15072591314");
				userLoginRegisterBusiness.createNewUser(user);
			}
			page += 1;
		}while(userTemps != null);
		return "OK";
	}
	
	@RequestMapping(method=RequestMethod.GET,value="/v1.0/user/download")
	public void downloadExcl(HttpServletRequest request,HttpServletResponse response,
			@RequestParam(value="userId")String userId
			
			) throws IOException{
		List<User> users = userLoginRegisterBusiness.findAfterUsers(Long.valueOf(userId));
		ExcelUtil<User> vExcelUtil = new ExcelUtil<User>();
		vExcelUtil.download(request, response, users,"test","导出测试的数据");
	}
	
	@RequestMapping(method=RequestMethod.POST,value="/v1.0/user/RestTemplate")
	public @ResponseBody Object updateConsumeTask(
			@RequestParam(value="time")String time
			){
		LinkedMultiValueMap<String, String> requestEntity = new LinkedMultiValueMap<String, String>();
		requestEntity.add("time", time);
		URI serviceUrl = util.getServiceUrl("user", "user");
		System.out.println(serviceUrl);
		String postForObject = null;
		try {
			postForObject = restTemplate.postForObject("http://user/v1.0/user/testRestTemplate", requestEntity, String.class);
		} catch (RestClientException e) {
			e.printStackTrace();LOG.error("",e);LOG.error("",e);
			return "Failed";
		}
		return postForObject;
		
//		String orders = "201803271556638303,201803271343685623,2018032713711836133,201803271732086203,201803271743008633";
//		String[] orderCodes = orders.split(",");
//		for(String orderCode:orderCodes){
//			LinkedMultiValueMap<String, String> requestEntity = new LinkedMultiValueMap<String, String>();
//			requestEntity.add("orderCode", orderCode);
//			RestTemplate restTemplate = new RestTemplate();
//			String resultString = restTemplate.postForObject("http://106.15.47.73/v1.0/creditcardmanager/update/taskstatus/by/ordercode", requestEntity, String.class);
//			System.out.println("orderCode====" + orderCode + "====:" + resultString);
//		}
	}
	
	@RequestMapping(method=RequestMethod.POST,value="/v1.0/user/testRestTemplate")
	public @ResponseBody Object updateConsumeTask2(HttpServletRequest request,HttpServletResponse response
			) throws FileNotFoundException{
//		String url = "http://paymentchannel/v1.0/paymentchannel/pass/verification/add/eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJKdUhlaSIsImV4cCI6MTUyNjIzNzQwOCwidXNlcklkIjozNDQ0MjU1ODEsImJyYW5kSWQiOjQ0MywicGhvbmUiOiIxODk3Mzc2MDI3NyJ9.XY9STlTI3CEluGy1YBUACAZPh5LVdPdLGeKd4KYjLEWzwet0q44KMetSccO8dybhmk3fKNNjihKhuM0Ttq6BRg";
//		for(int i = 0;i < 100;i++){
//			LinkedMultiValueMap<String, String> rquestEntity = new LinkedMultiValueMap<>();
//			rquestEntity.add("size", "1000");
//			String result = restTemplate.postForObject(url, rquestEntity, String.class);
//			System.out.println(JSONObject.fromObject(result).get(CommonConstants.RESP_CODE));
//		}
//		System.out.println(request.getRemoteAddr());
//		System.out.println(request.getLocalAddr());
//		System.out.println(request.getServletPath());
//		System.out.println(request.getPathTranslated() );
		File file = new File("C:\\Users\\Administrator\\Desktop\\order.txt");
		FileReader reader = new FileReader(file);
		StringBuffer sb = new StringBuffer();
		int charread = 0 ;
		char[] chars = new char[1024];
		 try {
			while ((charread = reader.read(chars)) != -1) {
				 sb.append(chars);
			 }
		} catch (IOException e) {
			e.printStackTrace();
		}
		String orderCodess = sb.toString() ;
		String[] orderCodes = orderCodess.split(",");
		String url = "";
		RestTemplate restTemplate;
		List<String> strings = new ArrayList<String>();
		for(String orderCode:orderCodes){
//			String[] split = orderCode.split("-");
//			String userId = split[0];
//			String creditCardNumber = split[1];
//			transfer
//			url = "http://139.224.192.147/v1.0/paymentchannel/topup/wf/ordercodequery?ordercode="+orderCode+"&transType=fastPay";
//			url = "http://139.224.192.147/v1.0/paymentchannel/topup/hljc/ordercodequery?ordercode="+orderCode;
//			url = "http://106.15.47.73/v1.0/paymentchannel/topup/wmyk/fastpayquery";
			LinkedMultiValueMap<String, String> rquestEntity = new LinkedMultiValueMap<>();
//			rquestEntity.add("ordercode", orderCode);
//			restTemplate = new RestTemplate();
//			String resultString;
//			try {
//				resultString = restTemplate.postForObject(url, rquestEntity,String.class);
//			} catch (RestClientException e) {
//				e.printStackTrace();
//				continue;
//			}
//			JSONObject resultJSON = JSONObject.fromObject(resultString);
//			System.out.println(orderCode + "====" +resultJSON);
//			if(CommonConstants.SUCCESS.equals(resultJSON.getString(CommonConstants.RESP_CODE))){
//				url = "http://139.224.192.147/v1.0/transactionclear/payment/update";
//				LinkedMultiValueMap<String, String> rquestEntity = new LinkedMultiValueMap<>();
//				rquestEntity.add("order_code", orderCode);
//				rquestEntity.add("status", "1");
//				restTemplate = new RestTemplate();
//				String postForObject = restTemplate.postForObject(url, rquestEntity, String.class);
//				System.out.println(orderCode + "====" +postForObject);
				url = "http://106.15.104.38/v1.0/creditcardmanager/update/taskstatus/by/ordercode";
//				url = "http://106.15.104.38/v1.0/creditcardmanager/clear/by/userid/creditcardnumber";
				rquestEntity = new LinkedMultiValueMap<>();
//				rquestEntity.add("userId", userId);
//				rquestEntity.add("creditCardNumber", creditCardNumber);
				rquestEntity.add("orderCode", orderCode);
				rquestEntity.add("version", "60");
				try {
					restTemplate = new RestTemplate();
					String postForObject = restTemplate.postForObject(url, rquestEntity, String.class);
					System.out.println(orderCode + "==修改任务状态==" +postForObject);
				} catch (RestClientException e) {
					e.printStackTrace();
					strings.add(orderCode);
					continue;
				}
				
//				url = "http://101.132.255.103/v1.0/transactionclear/payment/update";
//				rquestEntity = new LinkedMultiValueMap<>();
//				rquestEntity.add("order_code", orderCode);
//				rquestEntity.add("status", "1");
//				String postForObject = new RestTemplate().postForObject(url, rquestEntity, String.class);
//				System.out.println(orderCode + "==修改订单==" +postForObject);
//				strings.add(orderCode);
			}
			
//		}
		
		System.out.println(strings);
		
		return "OK"; 
	}
	
	/*public static void main(String[] args) {
		File file = new File("C:\\Users\\Administrator\\Desktop\\order.txt");
		FileReader reader = null;
		try {
			reader = new FileReader(file);
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		}
		StringBuffer sb = new StringBuffer();
		int charread = 0 ;
		char[] chars = new char[1024];
		 try {
			while ((charread = reader.read(chars)) != -1) {
				 sb.append(chars);
			 }
		} catch (IOException e) {
			e.printStackTrace();
		}
		String orderCodess = sb.toString();
		String[] orderCodes = orderCodess.split(",");
		for (String orderCode : orderCodes) {
//			if (orderCode.matches("^[0-9]*$")) {
			String[] split = orderCode.split("-");
			String userId = split[0];
			String creditCardNumber = split[1];
			
				LinkedMultiValueMap<String, String> rquestEntity = new LinkedMultiValueMap<>();
				String url = "http://106.15.47.73/v1.0/creditcardmanager/clear/by/userid/creditcardnumber";
				rquestEntity = new LinkedMultiValueMap<>();
				rquestEntity.add("userId", userId);
				rquestEntity.add("creditCardNumber", creditCardNumber);
				rquestEntity.add("version", "6");
				try {
					String postForObject = new RestTemplate().postForObject(url, rquestEntity, String.class);
					System.out.println(postForObject);
				} catch (RestClientException e) {
					e.printStackTrace();
					continue;
				}
//			}
		}
		
	}*/
	
	public static String errInfo(Exception e) {  
	    StringWriter sw = null;  
	    PrintWriter pw = null;  
	    try {  
	        sw = new StringWriter();  
	        pw = new PrintWriter(sw);  
	        // 将出错的栈信息输出到printWriter中  
	        e.printStackTrace(pw);  
	        pw.flush();  
	        sw.flush();  
	    } finally {  
	        if (sw != null) {  
	            try {  
	                sw.close();  
	            } catch (IOException e1) {  
	                e1.printStackTrace();  
	            }  
	        }  
	        if (pw != null) {  
	            pw.close();  
	        }  
	    }  
	    return sw.toString();  
	}  
}
