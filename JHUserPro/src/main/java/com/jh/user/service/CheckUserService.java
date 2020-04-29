package com.jh.user.service;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.jh.user.business.CheckUserBusiness;
import com.jh.user.pojo.CheckUser;
import com.jh.user.util.Util;

import cn.jh.common.utils.CommonConstants;

@Controller
@EnableAutoConfiguration
public class CheckUserService {
	
	private static final Logger LOG = LoggerFactory.getLogger(CheckUserService.class);
	
	@Autowired
	private CheckUserBusiness checkUserBusiness;
	
	@Autowired
	Util util;
	
	//查询用户信息
	@RequestMapping(method=RequestMethod.POST,value="/v1.0/user/checkuser/querybyid")
	public @ResponseBody Object queryUserById(HttpServletRequest request,   
			@RequestParam(value = "id", defaultValue="1", required=false) long id
			){
		
		CheckUser queryUserById = checkUserBusiness.queryUserById(id);
		
		Map map = new HashMap();
		map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
		map.put(CommonConstants.RESULT, queryUserById);
		map.put(CommonConstants.RESP_MESSAGE, "查询成功");
		
		return map;
	}
	
	
	//增加用户信息
	@RequestMapping(method=RequestMethod.POST,value="/v1.0/user/checkuser/add")
	public @ResponseBody Object adduser(HttpServletRequest request,   
			@RequestParam(value = "username") String name,
			@RequestParam(value = "gender", defaultValue="0", required=false) int gender,
			@RequestParam(value = "phone", defaultValue="18300700074", required=false) String phone,
			@RequestParam(value = "userinfo", defaultValue="", required=false) String userinfo
			){
		
		CheckUser checkUser = new CheckUser();
		checkUser.setUserName(name);
		checkUser.setGender(gender);
		checkUser.setPhone(phone);
		checkUser.setUserinfo(userinfo);
		
		CheckUser saveCheckUser = checkUserBusiness.saveCheckUser(checkUser);
		
		Map map = new HashMap();
		map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
		map.put(CommonConstants.RESP_MESSAGE, "保存成功");
		
		return map;
	}
	
	
	
	//修改用户信息
		@RequestMapping(method=RequestMethod.POST,value="/v1.0/user/checkuser/update")
		public @ResponseBody Object updateuser(HttpServletRequest request,
				@RequestParam(value = "id") long id,
				@RequestParam(value = "username") String name,
				@RequestParam(value = "gender", defaultValue="0", required=false) int gender,
				@RequestParam(value = "phone", defaultValue="18300700074", required=false) String phone,
				@RequestParam(value = "userinfo", defaultValue="", required=false) String userinfo
				){
			
			CheckUser checkUser = checkUserBusiness.queryUserById(id);
			checkUser.setUserName(name!=null&&!name.equals("")?name:checkUser.getUserName());
			checkUser.setGender(gender!=-1?gender:checkUser.getGender());
			checkUser.setPhone(phone!=null&&!phone.equals("")?phone:checkUser.getPhone());
			checkUser.setUserinfo(userinfo!=null&&!userinfo.equals("")?userinfo:checkUser.getUserinfo());
			
			CheckUser saveCheckUser = checkUserBusiness.saveCheckUser(checkUser);
			
			Map map = new HashMap();
			map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
			map.put(CommonConstants.RESP_MESSAGE, "修改成功");
			
			return map;
		}
	
	
	
}
