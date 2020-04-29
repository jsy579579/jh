package com.jh.notice.service;


import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import cn.jh.common.utils.CommonConstants;
import cn.jh.common.utils.DateUtil;

import com.jh.notice.business.HttpCallbackBusiness;
import com.jh.notice.pojo.HttpNotify;
import com.jh.notice.util.NoticeConstants;


@Configuration
@Controller
@EnableScheduling
public class HttpNotifyService {

	
	private final Logger logger = LoggerFactory.getLogger(getClass());
	
	@Autowired
	private HttpCallbackBusiness callbackBusiness;
	/**
	 * 每两分钟执行一次将没有回调成功的CallBack回调成功
	 * 总共执行24次， 直到被回调方返回6个0就终止
	 */
	@Scheduled(cron = "0 0/2 * * * ?") 
    public void scheduler() {        
			callbackBusiness.callBack();
    }
	
	
	/**查询已经回调的链接*/
	@RequestMapping(method=RequestMethod.POST,value="/v1.0/notice/callback/query")
	public @ResponseBody Object callBackQuery(HttpServletRequest request, 
			 @RequestParam(value = "page", defaultValue = "0", required = false) int page,
				@RequestParam(value = "size", defaultValue = "20", required = false) int size,
				@RequestParam(value = "order", defaultValue = "DESC", required = false) Sort.Direction direction,
				@RequestParam(value = "sort", defaultValue = "createTime", required = false) String sortProperty, 
				@RequestParam(value = "status",  required = false) String status,
				@RequestParam(value = "startTime",  required = false) String startTime, 
				@RequestParam(value = "endTime",  required = false) String endTime
			){
		
		Map map = new HashMap();
        Date startDate = null;
        Date endDate = null;
	    
        if(startTime == null || startTime.equalsIgnoreCase("")){
			 if(endTime !=null && !endTime.equalsIgnoreCase("")){
	    		map.put(CommonConstants.RESP_CODE, NoticeConstants.ERROR_PARAM);
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
		map.put(CommonConstants.RESULT,  callbackBusiness.findAllCallback(pageable, status, startDate, endDate));
		return map;

		
	}
	
	
	
	/**创建一个的回调链接*/
	@RequestMapping(method=RequestMethod.POST,value="/v1.0/notice/callback/create")
	public @ResponseBody Object createCallBack(HttpServletRequest request, 
			 @RequestParam(value = "notify_url") String url,
				@RequestParam(value = "params") String params
				
			){
		
		HttpNotify  httpNotify = new HttpNotify();
		
		try {
			httpNotify.setNotifyURL(URLDecoder.decode(url, "UTF-8"));
			httpNotify.setParams(URLDecoder.decode(params, "UTF-8"));
			httpNotify.setStatus("1");
			httpNotify.setRemainCnt(24);
			httpNotify.setNextCallTime(new Date());
			httpNotify.setCreateTime(new Date());
			callbackBusiness.mergeNotify(httpNotify);
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		
		return null;
	}
}
