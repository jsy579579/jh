package com.jh.notice.service;

import java.io.IOException;
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

import com.jh.notice.business.EmailSendBusiness;


@Controller
@EnableAutoConfiguration
public class EmailSendService {

	
	private static final Logger LOG = LoggerFactory.getLogger(EmailSendService.class);
	
	@Autowired
	private EmailSendBusiness emailSendBusiness;
	

	//添加关注（我关注别人，我是粉丝）
	@RequestMapping(method=RequestMethod.GET,value="/v1.0/notice/email/query")
	public @ResponseBody Object addFans(HttpServletRequest request,@RequestParam(value = "page", defaultValue = "0", required = false) int page,
			@RequestParam(value = "size", defaultValue = "20", required = false) int size,
			@RequestParam(value = "order", defaultValue = "DESC", required = false) Sort.Direction direction,
			@RequestParam(value = "sort", defaultValue = "createTime", required = false) String sortProperty)  throws IOException{
		LOG.info("添加关注....");
		
		Pageable pageable = new PageRequest(page, size, new Sort(direction,sortProperty));
		

		Map map = new HashMap();
		map.put(CommonConstants.RESP_CODE,CommonConstants.SUCCESS);
		map.put(CommonConstants.RESP_MESSAGE, "成功");
		map.put(CommonConstants.RESULT, emailSendBusiness.findEmailRecord(pageable));
		return map;
	}
	
}
