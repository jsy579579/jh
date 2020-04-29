package com.jh.paymentchannel.service;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.stereotype.Controller;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestTemplate;

import cn.jh.common.tools.Tools;
import cn.jh.common.utils.CommonConstants;

import com.jh.paymentchannel.business.AreaNumberBusiness;
import com.jh.paymentchannel.pojo.AreaNumber;
import com.jh.paymentchannel.util.Util;


@Controller
@EnableAutoConfiguration
public class AreaNumberService {
	
	private static final Logger log = LoggerFactory.getLogger(AreaNumberService.class);
	
	@Autowired
	private AreaNumberBusiness areaNumberBusiness;
	
	@Autowired
	private Util util;
	
	@RequestMapping(method=RequestMethod.POST,value="/v1.0/paymentchannel/areanumber/queryareano")
	public @ResponseBody Object queryAreaNo(HttpServletRequest request,
			@RequestParam(value="province", defaultValue="", required=false) String province,
			@RequestParam(value="city", defaultValue="", required=false) String city,
			@RequestParam(value="area", defaultValue="", required=false) String area
			
			){
		
		List<AreaNumber> queryAreaNumberByAll = areaNumberBusiness.queryAreaNumberByAll(province, city, area);
		
		Map map = new HashMap();
		map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
		map.put(CommonConstants.RESULT, queryAreaNumberByAll.get(0));
		map.put(CommonConstants.RESP_MESSAGE, "查询成功");
		
		
		return map ;
		
	}
}
