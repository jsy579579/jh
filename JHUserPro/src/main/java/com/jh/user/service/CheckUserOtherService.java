package com.jh.user.service;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.jh.user.business.CheckUserOtherBusiness;
import com.jh.user.pojo.CheckUserOther;
import com.jh.user.util.Util;

import cn.jh.common.utils.CommonConstants;

@Controller
@EnableAutoConfiguration
public class CheckUserOtherService {
	
	@Autowired
	Util util;
	
	private static final Logger LOG = LoggerFactory.getLogger(CheckUserOtherService.class);
	
	
	@Autowired
	private CheckUserOtherBusiness checkUserOtherBusiness;
	
	
	//增加用户信息
	@RequestMapping(method=RequestMethod.POST, value="/v1.0/user/checkuserother/add")
	public @ResponseBody Object addUser(HttpServletRequest request,
			@RequestParam(value = "username") String username,
			@RequestParam(value = "gender", required=false, defaultValue="0") int gender,
			@RequestParam(value = "title", required=false, defaultValue="") String title,
			@RequestParam(value = "content", required=false, defaultValue="") String content,
			@RequestParam(value = "love", required=false) int love,
			@RequestParam(value = "dislike", required=false) int dislike
			){
		
		Map map = new HashMap();
		
		CheckUserOther checkusuer = new CheckUserOther();
		
		checkusuer.setUserid(1);
	    checkusuer.setUserName(username);
	    checkusuer.setGender(gender);
	    checkusuer.setTitle(title);
	    checkusuer.setContent(content);
	    checkusuer.setLove(love);
	    checkusuer.setDislike(dislike);
	    checkusuer.setCreateTime(new Date());
		   	
	    checkUserOtherBusiness.saveCheckUserOther(checkusuer);
	    	
		map.put(CommonConstants.RESP_CODE,CommonConstants.SUCCESS);
		map.put(CommonConstants.RESP_MESSAGE, "保存成功");
		
		return map;
	}
	
	
	
	//查看用户信息
	@RequestMapping(method=RequestMethod.POST, value="/v1.0/user/checkuserother/query")
	public @ResponseBody Object queryUserById(HttpServletRequest request,
			@RequestParam(value = "page", defaultValue = "0", required = false) int page,
			@RequestParam(value = "size", defaultValue = "10", required = false) int size,
			@RequestParam(value = "order", defaultValue = "DESC", required = false) Sort.Direction direction,
			@RequestParam(value = "sort", defaultValue = "createTime", required = false) String sortProperty			
			){
		
		Pageable pageable = new PageRequest(page, size, new Sort(direction,sortProperty));
		
		Page<CheckUserOther> queryUserById = checkUserOtherBusiness.queryUserById(pageable);
		
		Map map = new HashMap();
		map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
		map.put(CommonConstants.RESULT, queryUserById);
		map.put(CommonConstants.RESP_MESSAGE, "查询成功");
		
		return map;
	}
	
	//增加信息
	@RequestMapping(method=RequestMethod.POST, value="/v1.0/user/checkuserother/update")
	public @ResponseBody Object update(HttpServletRequest request,
			@RequestParam(value = "userid",required=false, defaultValue="1") long userid,
			@RequestParam(value = "username",required=false, defaultValue="王伟") String username,
			@RequestParam(value = "gender", required=false, defaultValue="0") int gender,
			@RequestParam(value = "title", required=false, defaultValue="") String title,
			@RequestParam(value = "content", required=false, defaultValue="") String content,
			@RequestParam(value = "love", required=false, defaultValue="46") int love,
			@RequestParam(value = "dislike", required=false, defaultValue="16") int dislike
			){
		
		CheckUserOther update = new CheckUserOther();
		update.setUserName(username);
		update.setGender(gender);
		update.setLove(love);
		update.setDislike(dislike);
		update.setCreateTime(new Date());
		update.setUserid(userid);
		update.setTitle(title);
		update.setContent(content);
		
		CheckUserOther saveCheckUser1 = checkUserOtherBusiness.saveCheckUserOther(update);
		
		Map map = new HashMap();
		map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
		map.put(CommonConstants.RESP_MESSAGE, "增加成功");
		
		return map;
	}
		
		
		
	
}
