package com.jh.user.service;

import java.util.HashMap;
import java.util.List;
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

import com.jh.user.business.BankCodeBusiness;
import com.jh.user.pojo.BankCode;
import com.jh.user.pojo.Province;

import cn.jh.common.tools.Log;
import cn.jh.common.utils.CommonConstants;

@Controller
@EnableAutoConfiguration
public class BankCodeService {
	
	private static final Logger LOG = LoggerFactory.getLogger(BankCodeService.class);
	
	@Autowired
	public BankCodeBusiness bankCodeBusiness;
	
	//根据银行名称获得银行编码
	@RequestMapping(method=RequestMethod.POST,value=("/v1.0/user/bankcode/getcodebyname"))
	public @ResponseBody Object getcodebyname(HttpServletRequest request,
			@RequestParam(value="name") String name
			) {
		
		String codeByName = bankCodeBusiness.getCodeByName(name);
		
		Map map = new HashMap();
		if(codeByName!=null&&!codeByName.equals("")){
			map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
			map.put(CommonConstants.RESULT, codeByName);
			map.put(CommonConstants.RESP_MESSAGE, "成功");
			return map;
		}else{
			map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			map.put(CommonConstants.RESP_MESSAGE, "失败");
			return map;
		}
		
	}
	
	
	@RequestMapping(method=RequestMethod.POST,value="/v1.0/user/bankcode/getbankcode/byname")
	public @ResponseBody Object getBankCode(HttpServletRequest request,
			@RequestParam(value="name") String name
			) {
		
		BankCode bankCode = bankCodeBusiness.getBankCode(name);
		
		Map map = new HashMap();
		if(bankCode!=null){
			map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
			map.put(CommonConstants.RESULT, bankCode);
			map.put(CommonConstants.RESP_MESSAGE, "成功");
			return map;
		}else{
			map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			map.put(CommonConstants.RESP_MESSAGE, "失败");
			return map;
		}
		
	}
	
	
}
