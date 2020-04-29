package com.jh.user.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

import com.jh.user.business.BankBranchBusiness;
import com.jh.user.pojo.Area;
import com.jh.user.pojo.BankBranch;
import com.jh.user.pojo.City;
import com.jh.user.pojo.Province;

import cn.jh.common.utils.CommonConstants;

@Controller
@EnableAutoConfiguration
public class BankBranchService {
	private static final Logger LOG = LoggerFactory.getLogger(BankBranchService.class);
	
	@Autowired
	BankBranchBusiness bbb;
	
	/*根据银行名称和支行名称查询支行信息*/
	@RequestMapping(method=RequestMethod.POST,value=("/v1.0/user/app/bankbranch/query/topname"))
	public @ResponseBody Object findAll(
			@RequestParam(value="province") String province,
			@RequestParam(value="city") String city,
			@RequestParam(value="topName") String topName
			){
		Map map = new HashMap();
		if(topName=="邮储银行"||topName.equals("邮储银行")) {
			topName = "邮政储蓄银行";
		}
		List<BankBranch> list = bbb.findAllBranch(province, city, topName);
		if(list.size()>0) {
			map.put(CommonConstants.RESP_CODE,CommonConstants.SUCCESS);
			map.put(CommonConstants.RESULT, list);
			map.put(CommonConstants.RESP_MESSAGE, "成功");
		}else {
			map.put(CommonConstants.RESP_CODE,CommonConstants.FALIED);
			map.put(CommonConstants.RESP_MESSAGE, "暂不支持该银行卡");
		}
		return map;
	}
	
	/*无条件查询所有的省份、直辖市、自治区*/
	@RequestMapping(method=RequestMethod.POST,value=("/v1.0/user/app/province/queryall"))
	public @ResponseBody Object findProvince() {
		Map map = new HashMap();
		List<Province> list = bbb.findProvince();
		map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
		map.put(CommonConstants.RESULT, list);
		map.put(CommonConstants.RESP_MESSAGE, "成功");
		return map;
	}
	
	/*联动查询城市、区、县*/
	@RequestMapping(method=RequestMethod.POST,value=("/v1.0/user/app/city/queryall"))
	public @ResponseBody Object findCity(@RequestParam(value="provinceid") String provinceid) {
		Map map = new HashMap();
		List<City> list = new ArrayList<City>();
		if(provinceid.equals("110000")) {
			City c = new City();
			c.setCity("北京");
			list.add(c);
			map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
			map.put(CommonConstants.RESP_MESSAGE, "成功");
			map.put(CommonConstants.RESULT, list);
			return map;
		}
		if(provinceid.equals("310000")) {
			City c = new City();
			c.setCity("上海");
			list.add(c);
			map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
			map.put(CommonConstants.RESP_MESSAGE, "成功");
			map.put(CommonConstants.RESULT, list);
			return map;
		}
		if(provinceid.equals("120000")) {
			City c = new City();
			c.setCity("天津");
			list.add(c);
			map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
			map.put(CommonConstants.RESP_MESSAGE, "成功");
			map.put(CommonConstants.RESULT, list);
			return map;
		}
		if(provinceid.equals("500000")) {
			City c = new City();
			c.setCity("重庆");
			list.add(c);
			map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
			map.put(CommonConstants.RESP_MESSAGE, "成功");
			map.put(CommonConstants.RESULT, list);
			return map;
		}
		
		if(provinceid.equals("710000")) {
			City c = new City();
			c.setCity("台湾");
			list.add(c);
			map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
			map.put(CommonConstants.RESP_MESSAGE, "成功");
			map.put(CommonConstants.RESULT, list);
			return map;
		}
		if(provinceid.equals("810000")) {
			City c = new City();
			c.setCity("香港");
			list.add(c);
			map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
			map.put(CommonConstants.RESP_MESSAGE, "成功");
			map.put(CommonConstants.RESULT, list);
			return map;
		}
		if(provinceid.equals("820000")) {
			City c = new City();
			c.setCity("澳门");
			list.add(c);
			map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
			map.put(CommonConstants.RESP_MESSAGE, "成功");
			map.put(CommonConstants.RESULT, list);
			return map;
		}
		/*if(provinceid.equals("110000")||provinceid.equals("120000")||provinceid.equals("310000")||provinceid.equals("500000")) {
			List<Area> listarea = new ArrayList<Area>();
			for(int i=0;i<list.size();i++) {
				String cityid = list.get(i).getCityid();
				List<Area> lis = bbb.findArea(cityid);
				listarea.addAll(lis);
			}
			map.put(CommonConstants.RESULT, listarea);
			map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
			map.put(CommonConstants.RESP_MESSAGE, "成功");
			return map;
		}*/
		list = bbb.findCity(provinceid);
		map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
		map.put(CommonConstants.RESP_MESSAGE, "成功");
		map.put(CommonConstants.RESULT, list);
		return map;
	}
}
