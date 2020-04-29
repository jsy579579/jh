package com.jh.mircomall.controller;

import java.math.BigDecimal;
import java.net.URI;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestTemplate;

import com.jh.mircomall.bean.Brand;
import com.jh.mircomall.bean.Business;
import com.jh.mircomall.service.BrandService;
import com.jh.mircomall.service.BusinessService;

import cn.jh.common.utils.CommonConstants;

@Controller
@RequestMapping("/v1.0/integralmall/business")
public class BusinessController {

	@Autowired
	private BusinessService businessService;
	@Autowired
	private BrandService brandService;
	
	Business business = new Business();
	
	/**
	 * 商户开户
	 * username:用户名
	 * password:密码
	 * brandId:贴牌id
	 * phone:商家电话
	 * addr:商家地址
	 * goodsParentId:主营商品
	 * createTime:创建时间
	 * */
	@ResponseBody
	@RequestMapping(method=RequestMethod.POST,value="/addBusiness")
	public Object addBusiness(@RequestParam("username") String username,@RequestParam("password") String password,
			@RequestParam("brandId") Integer brandId,@RequestParam("phone") String phone,@RequestParam("addr") String addr,
			@RequestParam("goodsParentId") Integer goodsParentId,HttpSession session
			){
		List<Brand> map = new ArrayList<Brand>();
		map = brandService.findbusinessById(brandId);
		Map maps = new HashMap();
		if (map.size()>0&&!map.equals("")) {
			business.setUsername(username);
			business.setPassword(password);
			business.setBrandId(brandId);
			business.setPhone(phone);
			business.setAddr(addr);
			business.setGoodsParentId(goodsParentId);
			int isSuccess = businessService.addBusiness(business);
			if (isSuccess > 0) {
				maps.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
				maps.put(CommonConstants.RESP_MESSAGE, "成功");
				maps.put(CommonConstants.RESULT, isSuccess);
			} else {
				maps.put(CommonConstants.RESP_CODE, CommonConstants.ERROR_PARAM);
				maps.put(CommonConstants.RESP_MESSAGE, "失败");
				maps.put(CommonConstants.RESULT, isSuccess);
			}
		}
		return maps;
	}
	
	/**
	 * 分页显示所有商家信息  ,
	 * */
	@ResponseBody
	@RequestMapping(method=RequestMethod.POST,value="/listAllBusiness")
	public Object listAllBusiness(@RequestParam("currPage") int currPage,@RequestParam("pageSize") int pageSize){
		List<Business> list = businessService.listAllBusiness(currPage,pageSize);
		Map maps = new HashMap();
		if (list.size()>0&&!list.equals("")) {
			maps.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
			maps.put(CommonConstants.RESP_MESSAGE, "成功");
			maps.put(CommonConstants.RESULT, list);
		} else {
			maps.put(CommonConstants.RESP_CODE, CommonConstants.ERROR_PARAM);
			maps.put(CommonConstants.RESP_MESSAGE, "失败");
			maps.put(CommonConstants.RESULT, list);
		}
		return maps;	
	}
	
	/**
	 * 商家登录
	 * */
	@ResponseBody
	@RequestMapping(method=RequestMethod.POST,value="/lgoinBusiness")
	public Object lgoinBusiness(@RequestParam Map map){
		List<Business> list = businessService.loginBusiness(map);
		Map maps = new HashMap();
		if (list.size()>0&&!list.equals("")) {
			maps.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
			maps.put(CommonConstants.RESP_MESSAGE, "成功");
			maps.put(CommonConstants.RESULT, list);
		} else {
			maps.put(CommonConstants.RESP_CODE, CommonConstants.ERROR_PARAM);
			maps.put(CommonConstants.RESP_MESSAGE, "失败");
			maps.put(CommonConstants.RESULT, list);
		}
		return maps;	
	}
	
	/**
	 * 商家注销
	 * */
	@ResponseBody
	@RequestMapping(method=RequestMethod.POST,value="/deleteBusiness")
	public Object deleteBusiness(@RequestParam Map map){
		System.out.println(map);
		int isSuccess = businessService.deleteBusiness(map);
		Map maps = new HashMap();
		if (isSuccess > 0) {
			maps.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
			maps.put(CommonConstants.RESP_MESSAGE, "成功");
			maps.put(CommonConstants.RESULT, isSuccess);
		} else {
			maps.put(CommonConstants.RESP_CODE, CommonConstants.ERROR_PARAM);
			maps.put(CommonConstants.RESP_MESSAGE, "失败");
			maps.put(CommonConstants.RESULT, isSuccess);
		}
		return maps;
	}
	
	/**
	 *  修改商家登录密码
	 * */
	@ResponseBody
	@RequestMapping(method=RequestMethod.POST,value="/updateBusinessPWD")
	public Object updateBusinessPWD(@RequestParam Map map){
		int isSuccess = businessService.updateBusinessPWD(map);
		Map maps = new HashMap();
		if (isSuccess > 0) {
			maps.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
			maps.put(CommonConstants.RESP_MESSAGE, "成功");
			maps.put(CommonConstants.RESULT, isSuccess);
		} else {
			maps.put(CommonConstants.RESP_CODE, CommonConstants.ERROR_PARAM);
			maps.put(CommonConstants.RESP_MESSAGE, "失败");
			maps.put(CommonConstants.RESULT, isSuccess);
		}
		return maps;
	}
	
}
