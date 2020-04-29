package com.jh.user.service;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import cn.jh.common.utils.CommonConstants;
import cn.jh.common.utils.DateUtil;
import cn.jh.common.utils.TokenUtil;

import com.jh.user.business.UserRelationBusiness;
import com.jh.user.pojo.UserAgentChange;
import com.jh.user.util.Util;

@Controller
@EnableAutoConfiguration
public class UserAgentRelationService {

	private static final Logger LOG = LoggerFactory.getLogger(UserAgentRelationService.class);
	
	
	@Autowired
	Util util;
	
	@Autowired 
	private UserRelationBusiness userRelationBusiness;
	
	
	/**全局遍历*/
	@RequestMapping(method=RequestMethod.POST,value="/v1.0/user/relation/query")
	public @ResponseBody Object pageRelationQuery(HttpServletRequest request,  
			 @RequestParam(value = "start_time", required = false) String startTime,
			 @RequestParam(value = "end_time", required = false) String endTime,			
			 @RequestParam(value = "page", defaultValue = "0", required = false) int page,
			 @RequestParam(value = "size", defaultValue = "20", required = false) int size,
			 @RequestParam(value = "order", defaultValue = "DESC", required = false) Sort.Direction direction,
			 @RequestParam(value = "sort", defaultValue = "createTime", required = false) String sortProperty			
			){
		
		Map map  = new HashMap();
		map.put(CommonConstants.RESP_CODE,CommonConstants.SUCCESS);
		map.put(CommonConstants.RESP_MESSAGE, "成功");
		Pageable pageable = new PageRequest(page, size, new Sort(direction,sortProperty));

		if(startTime != null && !startTime.equalsIgnoreCase("")){
			if(endTime != null && !endTime.equalsIgnoreCase("")){
				map.put(CommonConstants.RESULT, userRelationBusiness.findUserAgentChange(DateUtil.getDateFromStr(startTime), DateUtil.getDateFromStr(endTime), pageable));
				return map;
			}else{
				map.put(CommonConstants.RESULT, userRelationBusiness.findUserAgentChange(DateUtil.getDateFromStr(startTime), pageable));
				return map;
			}
		}else{
			
			map.put(CommonConstants.RESULT, userRelationBusiness.findUserAgentChange(pageable));
			return map;
		}	
	}
	
	
	/**对单个用户的变更历史进行遍历*/
	@RequestMapping(method=RequestMethod.POST,value="/v1.0/user/relation/query/{token}")
	public @ResponseBody Object pageCoinQueryByUserid(HttpServletRequest request,  
			 @PathVariable("token") String token,
			 @RequestParam(value = "page", defaultValue = "0", required = false) int page,
			 @RequestParam(value = "size", defaultValue = "20", required = false) int size,
			 @RequestParam(value = "order", defaultValue = "DESC", required = false) Sort.Direction direction,
			 @RequestParam(value = "sort", defaultValue = "createTime", required = false) String sortProperty			
			){
		
		Map map = new HashMap();
		long userId;
		try{
			userId = TokenUtil.getUserId(token);
		}catch (Exception e) {
			map.put(CommonConstants.RESP_CODE,CommonConstants.ERROR_TOKEN);
			map.put(CommonConstants.RESP_MESSAGE, "token无效");
			return map;		
		}
		
		Pageable pageable = new PageRequest(page, size, new Sort(direction,sortProperty));

		map.put(CommonConstants.RESP_CODE,CommonConstants.SUCCESS);
		map.put(CommonConstants.RESP_MESSAGE, "成功");
		map.put(CommonConstants.RESULT, userRelationBusiness.findUserAgentChangeByUserid(userId, pageable));
		return map;	
	}
	
	
	/**新增一个用户代理关系变化记录**/
	@RequestMapping(method=RequestMethod.POST,value="/v1.0/user/relation/add")
	public @ResponseBody Object addUserAgentRelation(HttpServletRequest request,   
			@RequestParam(value = "user_id") long userId,
			@RequestParam(value = "old_agent") long oldAgent,  
			@RequestParam(value = "new_agent") long newAgent, 
			@RequestParam(value = "remark", required=false) String remark){
		
		UserAgentChange  userAgentChange  =  new UserAgentChange();
		
		userAgentChange.setCreateTime(new Date());
		userAgentChange.setNewAgent(newAgent);
		userAgentChange.setOldAgent(oldAgent);
		userAgentChange.setRemark(remark);
		
		Map map = new HashMap();
		map.put(CommonConstants.RESP_CODE,CommonConstants.SUCCESS);
		map.put(CommonConstants.RESP_MESSAGE, "成功");
		map.put(CommonConstants.RESULT, userRelationBusiness.saveUserAgentChange(userAgentChange));
		return map;	
		
		
	}
	
	
	
}
