package com.jh.mircomall.controller;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.jh.mircomall.bean.Cookies;
import com.jh.mircomall.service.CookiesService;

import cn.jh.common.utils.CommonConstants;

@SuppressWarnings("all")
@RestController
@RequestMapping("/v1.0/integralmall/cookies")
public class CookiesController {
	@Autowired
	private CookiesService cookiesService;

	@RequestMapping(value = "/removeCookies", method = RequestMethod.POST)
	public Object delCookies(@RequestParam("userId") int userId, @RequestParam("brandId") int brandId) {
		Cookies cookies = new Cookies();
		cookies.setUserId(userId);
		cookies.setBrandId(brandId);
		int isSuccess = cookiesService.removeCookies(cookies);
		Map map = new HashMap();
		if (isSuccess > 0) {
			map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
			map.put(CommonConstants.RESP_MESSAGE, "成功");
			map.put(CommonConstants.RESULT, isSuccess);
		} else {
			map.put(CommonConstants.RESP_CODE, CommonConstants.ERROR_PARAM);
			map.put(CommonConstants.RESP_MESSAGE, "失败");
			map.put(CommonConstants.RESULT, isSuccess);
		}

		return map;
	}

	@SuppressWarnings("all")
	@RequestMapping(value = "/getCookies", method = RequestMethod.POST)
	public Object getCookies(@RequestParam("userid") int userId, @RequestParam("brandid") int brandId,
			@RequestParam("pageindex") int pageIndex, @RequestParam("pagesize") int pageSize) {
		List<Cookies> cookies = cookiesService.getCookies(userId, brandId, pageIndex, pageSize);
		int total = cookiesService.getCookiesCount(userId, brandId);
		Map map = new HashMap();
		map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
		map.put(CommonConstants.RESP_MESSAGE, "成功");
		map.put(CommonConstants.RESULT, cookies);
		map.put("total", total);
		return map;
	}

	@RequestMapping(value = "/add", method = RequestMethod.POST)
	public Object addCookies(@RequestParam("goodsid") int goodsId, @RequestParam("userid") int userId,
			@RequestParam(value = "isdelete", defaultValue = "0") int isDelete,
			@RequestParam(value = "brandId") int brandId) {
		Map map = new HashMap();
		Cookies cook = cookiesService.getCookiesByGoodid(goodsId, userId, brandId);
		if (cook != null) {
			map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
			map.put(CommonConstants.RESP_MESSAGE, "成功");
			return map;
		}

		Cookies cookies = new Cookies();
		cookies.setCreateTime(new Date());
		cookies.setChangeTime(new Date());
		cookies.setIsDelete(isDelete);
		cookies.setUserId(userId);
		cookies.setGoodsId(goodsId);
		cookies.setBrandId(brandId);
		int isSuccess = cookiesService.addCookies(cookies);
		if (isSuccess > 0) {
			map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
			map.put(CommonConstants.RESP_MESSAGE, "成功");
			map.put(CommonConstants.RESULT, isSuccess);
		} else {
			map.put(CommonConstants.RESP_CODE, CommonConstants.ERROR_PARAM);
			map.put(CommonConstants.RESP_MESSAGE, "失败");
			map.put(CommonConstants.RESULT, isSuccess);
		}

		return map;
	}

}
