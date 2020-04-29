package com.jh.mircomall.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.jh.mircomall.service.UserAccountService;

import cn.jh.common.utils.CommonConstants;
@SuppressWarnings("all")
@RequestMapping("/v1.0/integralmall/user")
@RestController
public class UserAccountController {
	@Autowired
	private UserAccountService userAccountService;
	@RequestMapping(value="/get",method=RequestMethod.POST)
	public Object getUserInfo(@RequestParam("userid") int userId){
		Map userInfo = userAccountService.getUserInfo(userId);
		Map map = new HashMap();
		map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
		map.put(CommonConstants.RESP_MESSAGE, "成功");
		map.put(CommonConstants.RESULT, userInfo);
		return map;
	}
}
