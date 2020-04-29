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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import cn.jh.common.utils.CommonConstants;
import cn.jh.common.utils.DateUtil;

import com.jh.user.business.UserStatisticsBusiness;

@Controller
@EnableAutoConfiguration
public class UserStatisticsSevice {

	private static final Logger LOG = LoggerFactory.getLogger(UserStatisticsSevice.class);
	
	
	@Autowired
	private UserStatisticsBusiness  userStatisticsBusiness;
	
	
	/**用户全局搜索接口**/
	@RequestMapping(method=RequestMethod.POST,value="/v1.0/user/statistics")
	public @ResponseBody Object statisticsUsers(HttpServletRequest request,   
			@RequestParam(value = "phone", required=false) String phone,
			@RequestParam(value = "brandid", required=false) String brandid,  
			@RequestParam(value = "start_time",  required = false) String  startTime,
			@RequestParam(value = "end_time",  required = false) String endTime,
			@RequestParam(value = "page", defaultValue = "0", required = false) int page,
			@RequestParam(value = "size", defaultValue = "20", required = false) int size,
			@RequestParam(value = "order", defaultValue = "DESC", required = false) Sort.Direction direction,
			@RequestParam(value = "sort", defaultValue = "createTime", required = false) String sortProperty){
		
		
		Map<String,Object> map = new HashMap<>();
		
		Pageable pageable = new PageRequest(page, size, new Sort(direction,sortProperty));
		Date StartTimeDate = null;
		if(startTime != null  && !startTime.equalsIgnoreCase("")){
			StartTimeDate = DateUtil.getDateFromStr(startTime);
		}
		Date endTimeDate = null;
		
		if(endTime != null  && !endTime.equalsIgnoreCase("")){
			endTimeDate = DateUtil.getDateFromStr(endTime);
		}
		
		map.put(CommonConstants.RESP_CODE,CommonConstants.SUCCESS);
		map.put(CommonConstants.RESP_MESSAGE, "成功");
		map.put(CommonConstants.RESULT, userStatisticsBusiness.findPageUser(phone, brandid, StartTimeDate, endTimeDate, pageable));
		return map;
		
	}
	
	
}
