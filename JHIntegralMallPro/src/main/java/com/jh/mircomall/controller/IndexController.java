package com.jh.mircomall.controller;

import java.net.URI;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import com.jh.mircomall.bean.Goods;
import com.jh.mircomall.bean.Groups;
import com.jh.mircomall.bean.Taobao;
import com.jh.mircomall.service.GroupsService;
import com.jh.mircomall.service.IndexService;
import com.jh.mircomall.utils.TokenUtil;
import com.jh.mircomall.utils.Util;

import cn.jh.common.utils.CommonConstants;

import net.sf.json.JSONObject;

@Controller
@RequestMapping("/v1.0/integralmall/index")
public class IndexController {

	private static final Logger LOG = LoggerFactory.getLogger(IndexController.class);
	@Autowired
	private IndexService indexService;

	@Autowired
	private GroupsService groupsService;

	@Autowired
	Util util;

	/**
	 * 商品首页显示
	 * 
	 * @param request
	 * @param businessId
	 * @param currentPage
	 * @param pageSize
	 * @return
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/getgoodspage")
	public @ResponseBody Object getGoodsPage(HttpServletRequest request,
			@RequestParam(value = "businessId") int businessId, @RequestParam(value = "currentPage") int currentPage,
			@RequestParam(value = "pageSize") int pageSize,
			@RequestParam(value = "groupsId", required = false) Integer groupsId,
			@RequestParam(value = "goodsName", required = false) String goodsName) {
		Map maps = new HashMap();
		List<Goods> list = null;
		if (groupsId != null) {
			list = indexService.getGoodsPageByParentId(businessId, groupsId, currentPage, pageSize);
		} else if (!"".equals(goodsName)) {
			list = indexService.getGoodsPageBycondition1(businessId, goodsName, currentPage, pageSize);
		} else if (groupsId != null && !"".equals(goodsName)) {
			list = indexService.getGoodsPageBycondition2(businessId, groupsId, currentPage, pageSize, goodsName);
		} else {
			list = indexService.getGoodsPage(businessId, currentPage, pageSize);
		}
		int total = list.size();
		if (list.size() > 0 && !"".equals(list)) {
			total = indexService.getGoodsCount(businessId);
			maps.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
			maps.put(CommonConstants.RESP_MESSAGE, "查询商品成功");
			maps.put(CommonConstants.RESULT, list);
			maps.put("total", total);
			maps.put("totalpage", sum(total, pageSize));
		} else {
			maps.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
			maps.put(CommonConstants.RESP_MESSAGE, "查询商品成功");
			maps.put("total", 0);
			maps.put("totalpage", 0);
		}
		return maps;

	}

	public int sum(int total, int pagesize) {
		int x;
		if (total % pagesize == 0) {
			x = total / pagesize;
		} else {
			x = total / pagesize + 1;
		}
		return x;
	}

	/**
	 * 商品分组分页
	 * 
	 * @param request
	 * @param businessId
	 * @param oodsgTypeId
	 * @param currentPage
	 * @param pageSize
	 * @return
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/getgoodspagebyparent")
	public @ResponseBody Object getGoodsPageByParentId(HttpServletRequest request,
			@RequestParam("businessId") int businessId, @RequestParam("oodsgTypeId") int oodsgTypeId,
			@RequestParam("currentPage") int currentPage, @RequestParam("pageSize") int pageSize) {
		Map maps = new HashMap();
		List<Goods> list = indexService.getGoodsPageByParentId(businessId, oodsgTypeId, currentPage, pageSize);
		if (list.size() > 0 && !"".equals(list)) {
			int total = indexService.getCount(businessId, oodsgTypeId);
			maps.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
			maps.put(CommonConstants.RESP_MESSAGE, "查询商品成功");
			maps.put(CommonConstants.RESULT, list);
			maps.put("total", total);

		} else {
			maps.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
			maps.put(CommonConstants.RESP_MESSAGE, "查询商品成功");
			maps.put(CommonConstants.RESULT, list);
			maps.put("total", 0);
		}
		return maps;

	}

	/**
	 * 搜索栏模糊查询分页
	 * 
	 * @param request
	 * @param businessId
	 * @param text
	 * @param currentPage
	 * @param pageSize
	 * @return
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/getlikegoodspage")
	public @ResponseBody Object getLikeGoodsPage(HttpServletRequest request, @RequestParam("businessId") int businessId,
			@RequestParam("text") String text, @RequestParam("currentPage") int currentPage,
			@RequestParam("pageSize") int pageSize) {
		Map maps = new HashMap();
		List<Goods> list = indexService.getLikeGoods(businessId, text, currentPage, pageSize);
		if (list.size() > 0 && !"".equals(list)) {
			int total = indexService.getLikeCount(businessId, text);
			maps.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
			maps.put(CommonConstants.RESP_MESSAGE, "查询商品成功");
			maps.put(CommonConstants.RESULT, list);
			maps.put("total", total);

		} else {
			maps.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
			maps.put(CommonConstants.RESP_MESSAGE, "查询商品成功");
			maps.put(CommonConstants.RESULT, list);
			maps.put("total", 0);
		}
		return maps;

	}

	/**
	 * 获取所有1级分类
	 * 
	 * @return
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/getgoodslevel")
	public @ResponseBody Object getGoodsLevel() {
		Map maps = new HashMap();
		List<Taobao> list = indexService.getGoodsLevel();
		if (list.size() > 0 && !"".equals(list)) {
			maps.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
			maps.put(CommonConstants.RESP_MESSAGE, "获取leve1分类成功");
			maps.put(CommonConstants.RESULT, list);

		} else {
			maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			maps.put(CommonConstants.RESP_MESSAGE, "获取leve1分类成功");
			maps.put(CommonConstants.RESULT, list);
		}
		return maps;

	}

	/**
	 * 获取所有2级分类
	 * 
	 * @return
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/getgoodslevel2")
	public @ResponseBody Object getGoodsLevel(HttpServletRequest request, @RequestParam("id") int id) {
		Map maps = new HashMap();
		List<Taobao> list = indexService.getGoodsLevel2(id);
		if (list.size() > 0 && !"".equals(list)) {
			maps.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
			maps.put(CommonConstants.RESP_MESSAGE, "获取leve2分类成功");
			maps.put(CommonConstants.RESULT, list);

		} else {
			maps.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
			maps.put(CommonConstants.RESP_MESSAGE, "获取leve2分类成功");
			maps.put(CommonConstants.RESULT, list);
		}
		return maps;

	}

	@RequestMapping(method = RequestMethod.POST, value = "/getUserType")
	public @ResponseBody Object getUserType(HttpServletRequest request, @RequestParam("userId") Integer userId,
			@RequestParam("brandId") Integer brandId) {
		String userid = Integer.toString(userId);
		String brandid = Integer.toString(brandId);
		Map map = new HashMap();
		JSONObject jsonObject = null;
		JSONObject resultObjb = null;
		int userType = 0;
		String result;
		RestTemplate restTemplate = new RestTemplate();
		URI uri = util.getServiceUrl("user", "error url request!");
		String url = uri.toString() + "/v1.0/user/brand/query/id?brand_id=" + brandid;
		try {
			result = restTemplate.getForObject(url, String.class);
			LOG.info("RESULT================" + result);
			jsonObject = JSONObject.fromObject(result);
			resultObjb = jsonObject.getJSONObject("result");
		} catch (Exception e) {
			e.printStackTrace();
			LOG.error("==========/v1.0/user/brand/query/id查询用户异常===========" + e);
		}
		if (resultObjb == null) {
			return "error";
		}
		String manageid = "";
		if (resultObjb.containsKey("manageid")) {
			manageid = resultObjb.getString("manageid");
		}
		if (!"".equals(manageid) && manageid.equals(userid)) {
			userType = 1;
			map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
			map.put(CommonConstants.RESP_MESSAGE, "此用户为代理商");
			map.put(CommonConstants.RESULT, userType);
		} else {
			userType = 0;
			map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
			map.put(CommonConstants.RESP_MESSAGE, "此用户为普通用户");
			map.put(CommonConstants.RESULT, userType);
		}
		return map;
	}

	@RequestMapping(method = RequestMethod.POST, value = "/getUserInfo")
	public @ResponseBody Object getUserInfo(HttpServletRequest request, @RequestParam("userId") Integer userId) {
		Map map = new HashMap();
		JSONObject jsonObject = null;
		JSONObject resultObj = null;
		int userType = 0;
		Object userInfo;
		RestTemplate restTemplate = new RestTemplate();
		URI uri = util.getServiceUrl("user", "error url request!");
		String url = uri.toString() + "/v1.0/user/query/id";
		MultiValueMap<String, Integer> requestEntity = new LinkedMultiValueMap<String, Integer>();
		requestEntity = new LinkedMultiValueMap<String, Integer>();
		requestEntity.add("id", userId);
		userInfo = restTemplate.postForObject(url, requestEntity, Object.class);
		LOG.info("RESULT================" + userInfo);
		/*
		 * jsonObject = JSONObject.fromObject(result); resultObj =
		 * jsonObject.getJSONObject("result");
		 */
		if (userInfo != null) {
			map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
			map.put(CommonConstants.RESP_MESSAGE, "获取用户信息成功");
			map.put("userInfo", userInfo);
		} else {
			map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			map.put(CommonConstants.RESP_MESSAGE, "获取用户信息失败");
		}
		return map;
	}

	@RequestMapping(method = RequestMethod.POST, value = "/getUserCredit")
	public @ResponseBody Object getUserCredit(HttpServletRequest request, @RequestParam("userId") Integer userId) {
		Map map = new HashMap();
		JSONObject jsonObject = null;
		JSONObject resultObj = null;
		Object userCoin;
		RestTemplate restTemplate = new RestTemplate();
		URI uri = util.getServiceUrl("user", "error url request!");
		String url = uri.toString() + "/v1.0/user/account/query/userId";
		MultiValueMap<String, Integer> requestEntity = new LinkedMultiValueMap<String, Integer>();
		requestEntity = new LinkedMultiValueMap<String, Integer>();
		requestEntity.add("user_id", userId);
		userCoin = restTemplate.postForObject(url, requestEntity, Object.class);
		LOG.info("RESULT================" + userCoin);
		/*
		 * jsonObject = JSONObject.fromObject(result); resultObj =
		 * jsonObject.getJSONObject("result");
		 */
		if (userCoin != null) {
			map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
			map.put(CommonConstants.RESP_MESSAGE, "获取用户信息成功");
			map.put(CommonConstants.RESULT, userCoin);
		} else {
			map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			map.put(CommonConstants.RESP_MESSAGE, "获取用户信息失败");
		}
		return map;
	}

	/**
	 * 生成Token测试
	 * 
	 * @param request
	 * @param userId
	 * @param brandId
	 * @param Phone
	 * @return
	 */

	@RequestMapping(method = RequestMethod.POST, value = "/getToken")
	public @ResponseBody Object getUserCredit(HttpServletRequest request, @RequestParam("userId") Integer userId,
			@RequestParam("brandId") Integer brandId, @RequestParam("Phone") String Phone) {
		Map map = new HashMap();
		Object token = TokenUtil.createToken(userId, brandId, Phone);
		if (token != null) {
			map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
			map.put(CommonConstants.RESP_MESSAGE, "获取token成功");
			map.put(CommonConstants.RESULT, token);
		} else {
			map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			map.put(CommonConstants.RESP_MESSAGE, "获取token失败");
		}
		return map;
	}

	/**
	 * 获取全部分组
	 * 
	 * @return
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/getAllGroups")
	public @ResponseBody Object getAllGroups(@RequestParam(value = "brandId") String brandId) {
		Map map = new HashMap();
		List<Groups> list = groupsService.getAllGruops(brandId);
		if (list.size() < 1) {
			map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
			map.put(CommonConstants.RESP_MESSAGE, "暂时未添加分组");
			map.put(CommonConstants.RESULT, null);
		} else {
			map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
			map.put(CommonConstants.RESP_MESSAGE, "获取分组成功");
			map.put(CommonConstants.RESULT, list);
		}
		return map;
	}

	/**
	 * 添加分组
	 * 
	 * @param name
	 * @param brandId
	 * @return
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/addGroups")
	public @ResponseBody Object addGroups(@RequestParam(value = "groupsName") String name,
			@RequestParam(value = "brandId") String brandId) {
		Map map = new HashMap();
		LOG.info("===================" + name);
		Groups gs = groupsService.getGroupsByName(name, brandId);
		if (gs == null) {
			Groups groups = new Groups();
			groups.setBrandId(brandId);
			groups.setGroupsName(name);
			groups.setStatus("1");
			int returnType = groupsService.addGroups(groups);
			if (returnType > 0) {
				map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
				map.put(CommonConstants.RESP_MESSAGE, "添加分组成功");
			} else {
				map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
				map.put(CommonConstants.RESP_MESSAGE, "添加分组失败");
			}
			return map;

		} else {
			map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			map.put(CommonConstants.RESP_MESSAGE, "添加分组已存在");
			return map;
		}

	}

	/**
	 * 删除分组
	 * 
	 * @param id
	 * @param brandId
	 * @return
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/deleteGroups")
	public @ResponseBody Object deleteGroups(@RequestParam(value = "groupsId") Integer id,
			@RequestParam(value = "brandId") String brandId) {
		Map map = new HashMap();
		List<Goods> list = indexService.getGoodsByGroups(id);
		LOG.info("===================" + list.size());
		if (list.size() == 0) {
			int returnType = groupsService.deleteGroups(id);
			if (returnType > 0) {
				map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
				map.put(CommonConstants.RESP_MESSAGE, "刪除分组成功");
			} else {
				map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
				map.put(CommonConstants.RESP_MESSAGE, "刪除分组失败");
			}
		} else {
			map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			map.put(CommonConstants.RESP_MESSAGE, "请先删除分组下的商品");
		}
		return map;
	}

	/**
	 * 编辑分组
	 * 
	 * @param id
	 * @param brandId
	 * @param name
	 * @return
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/updateGroups")
	public @ResponseBody Object updateGroups(@RequestParam(value = "groupsId") Integer id,
			@RequestParam(value = "groupsName") String name) {
		Map map = new HashMap();
		Groups groups = new Groups();
		groups.setGroupsName(name);
		groups.setId(id);

		int returnType = groupsService.modifyGroups(groups);
		if (returnType > 0) {
			map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
			map.put(CommonConstants.RESP_MESSAGE, "编辑分组成功");
		} else {
			map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			map.put(CommonConstants.RESP_MESSAGE, "编辑失败");
		}
		return map;
	}
}
