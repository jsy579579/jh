package com.jh.mircomall.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.jh.mircomall.bean.LogisticsInfo;
import com.jh.mircomall.bean.Order;
import com.jh.mircomall.service.LogisticsInfoService;
import com.jh.mircomall.service.OrderService;

import cn.jh.common.utils.CommonConstants;

@SuppressWarnings("all")
@Controller
@RequestMapping("/v1.0/integralmall/logisticsInfo")
public class LogisticsInfoController {

	@Autowired
	private LogisticsInfoService logisticsInfoService;
	@Autowired
	private OrderService orderService;

	/**
	 * 测试物流轨迹代码
	 * 
	 * @param expCode
	 *            物流公司编码
	 * @param expNo
	 *            运单号
	 * @return
	 */

	@RequestMapping(value = "/getLogisticsTrajectory", method = RequestMethod.POST)
	@ResponseBody
	public String getLogisticstRajectory(@RequestParam("expCode") String expCode, @RequestParam("expNo") String expNo) {
		String a = null;
		Map map = new HashMap();
		try {
			a = logisticsInfoService.getOrderTracesByJson(expCode, expNo);
			/*map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
			map.put(CommonConstants.RESP_MESSAGE, "成功");
			map.put(CommonConstants.RESULT, a);*/
		} catch (Exception e) {
			/*map.put(CommonConstants.RESP_CODE, CommonConstants.ERROR_PARAM);
			map.put(CommonConstants.RESP_MESSAGE, "失败");*/
			e.printStackTrace();
		}
		return a;
	}

	/**
	 * 跳转到添加物信息
	 */
	@RequestMapping(value = "/gotoAdd")
	public String gotoAdd() {
		System.out.println("gotoAdd----------------------->>");
		return "logisticsInfo/add";
	}

	/**
	 * 添加物流信息
	 * 
	 */
	@ResponseBody
	@RequestMapping(method = RequestMethod.POST, value = "/addLogisticsInfo")
	public Object addLogisticsInfo(@RequestParam("goodsId") Integer goodsId, @RequestParam("orderId") String orderId,
			@RequestParam("logisticsName") String logisticsName, @RequestParam("logisticsNum") String logisticsNum,
			@RequestParam("userId") Integer userId, @RequestParam("userAddr") String userAddr,
			@RequestParam("userPhone") String userPhone, @RequestParam("userProvinceId") String userProvinceId,
			@RequestParam("userCityId") String userCityId, @RequestParam("userAreasId") String userAreasId,
			@RequestParam("businessName") String businessName, @RequestParam("businessPhone") String businessPhone,
			@RequestParam("businessProvinceId") String businessProvinceId,
			@RequestParam("businessCityId") String businessCityId,
			@RequestParam("businessAreasId") String businessAreasId, HttpSession session) {
		LogisticsInfo logisticsInfo = new LogisticsInfo();
		logisticsInfo.setGoodsId(goodsId);
		logisticsInfo.setOrderId(orderId);
		logisticsInfo.setUserId(userId);
		logisticsInfo.setUserAddr(userAddr);
		logisticsInfo.setUserPhone(userPhone);
		logisticsInfo.setUserProvinceId(userProvinceId);
		logisticsInfo.setUserCityId(userCityId);
		logisticsInfo.setUserAreasId(userAreasId);
		logisticsInfo.setBusinessName(businessName);
		logisticsInfo.setBusinessPhone(businessPhone);
		logisticsInfo.setBusinessProvinceId(businessProvinceId);
		logisticsInfo.setBusinessCityId(businessCityId);
		logisticsInfo.setBusinessAreasId(businessAreasId);
		logisticsInfo.setLogisticsName(logisticsName);
		logisticsInfo.setLogisticsNum(logisticsNum);

		int isSuccess = logisticsInfoService.addLogisticsInfo(logisticsInfo);
		Order order = new Order();
		order.setId(Integer.parseInt(orderId));
		order.setLogisticsId(logisticsInfo.getId());
		order.setLogisticsNum(logisticsNum);
		orderService.updateOrder(order);
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

	/**
	 * 显示所有的物流信息
	 */
	@ResponseBody
	@RequestMapping("/listAllLogisticsInfo")
	public Object listAllLogisticsInfo(@RequestParam("pageindex") int pageIndex,
			@RequestParam("pagesize") int pageSize) {
		List<LogisticsInfo> list = logisticsInfoService.listAllLogisticsInfo(pageIndex, pageSize);
		Map map = new HashMap();
		map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
		map.put(CommonConstants.RESP_MESSAGE, "成功");
		map.put(CommonConstants.RESULT, list);
		return map;
	}

	/**
	 * 根据用户id和订单号查询物流信息
	 */
	@ResponseBody
	@RequestMapping(method = RequestMethod.POST, value = "/listLogisticsInfoByUserIdAndOrderId")
	public Object listLogisticsInfoByUserIdAndOrderId(@RequestParam("userId") Integer userId,
			@RequestParam("orderId") String orderId) {
		LogisticsInfo logisticsInfo = new LogisticsInfo();

		List<LogisticsInfo> list = logisticsInfoService.listLogisticsInfoByUserIdAndOrderId(userId, orderId);
		Map maps = new HashMap();
		maps.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
		maps.put(CommonConstants.RESP_MESSAGE, "成功");
		maps.put(CommonConstants.RESULT, list);
		return maps;
	}

	/**
	 * 根据用户id或手机号或订单号查询物流信息
	 */
	@ResponseBody
	@RequestMapping(method = RequestMethod.POST, value = "/listLogisticsInfoByUserId")
	public Object listLogisticsInfoByUserId(@RequestParam("userId") Integer userId,
			@RequestParam("userPhone") String userPhone, @RequestParam("orderId") String orderId) {
		List<LogisticsInfo> list = logisticsInfoService.listLogisticsInfoByUserId(userId, userPhone, orderId);
		Map maps = new HashMap();
		maps.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
		maps.put(CommonConstants.RESP_MESSAGE, "成功");
		maps.put(CommonConstants.RESULT, list);
		return maps;
	}

	/**
	 * 商家完善用户物流信息（商家手动：添加物流名称，物流单号，寄件人姓名，寄件人电话，寄件人地址）
	 */
	@ResponseBody
	@RequestMapping(method = RequestMethod.POST, value = "/updateLogisticsInfoBusiness")
	public Object updateLogisticsInfoBusiness(@RequestParam("userId") Integer userId,
			@RequestParam("orderId") String orderId, @RequestParam("logisticsName") String logisticsName,
			@RequestParam("logisticsNum") String logisticsNum, @RequestParam("businessName") String businessName,
			@RequestParam("businessPhone") String businessPhone,
			@RequestParam("businessProvinceId") String businessProvinceId,
			@RequestParam("businessCityId") String businessCityId,
			@RequestParam("businessAreasId") String businessAreasId, HttpSession session) {
		LogisticsInfo logisticsInfo = new LogisticsInfo();
		logisticsInfo.setUserId(userId);
		logisticsInfo.setOrderId(orderId);
		logisticsInfo.setLogisticsName(logisticsName);
		logisticsInfo.setLogisticsNum(logisticsNum);
		logisticsInfo.setBusinessName(businessName);
		logisticsInfo.setBusinessPhone(businessPhone);
		logisticsInfo.setBusinessProvinceId(businessProvinceId);
		logisticsInfo.setBusinessCityId(businessCityId);
		logisticsInfo.setBusinessAreasId(businessAreasId);
		int isSuccess = logisticsInfoService.updateLogisticsInfoBusiness(logisticsInfo);
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

	/**
	 * 根据用户id和订单号更新物流信息表
	 */
	@ResponseBody
	@RequestMapping(method = RequestMethod.POST, value = "/updateLogisticsInfo")
	public Object updateLogisticsInfoByUserIdAndOrderId(@RequestParam("userId") Integer userId,
			@RequestParam("orderId") String orderId, @RequestParam("logisticsName") String logisticsName,
			@RequestParam("logisticsNum") String logisticsNum) {
		LogisticsInfo logisticsInfo = new LogisticsInfo();
		logisticsInfo.setUserId(userId);
		logisticsInfo.setOrderId(orderId);
		logisticsInfo.setLogisticsName(logisticsName);
		logisticsInfo.setLogisticsNum(logisticsNum);
		int isSuccess = logisticsInfoService.updateLogisticsInfoByUserIdAndOrderId(logisticsInfo);
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

	/**
	 * 根据用户id和订单号删除物流信息表
	 */
	@ResponseBody
	@RequestMapping(method = RequestMethod.POST, value = "/deleteLogisticsInfo")
	public Object deleteLogisticsInfoByUserIdAndOrderId(@RequestParam("userId") Integer userId,
			@RequestParam("orderId") String orderId) {
		LogisticsInfo logisticsInfo = new LogisticsInfo();
		logisticsInfo.setUserId(userId);
		logisticsInfo.setOrderId(orderId);
		int isSuccess = logisticsInfoService.deleteLogisticsInfoByUserIdAndOrderId(logisticsInfo);
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

	/**
	 * 根据用户id或订单号模糊查询物流信息
	 */
	@ResponseBody
	@RequestMapping(method = RequestMethod.POST, value = "/listLogisticsInfoBlur")
	public Object listLogisticsInfoBlur(@RequestParam("userId") Integer userId,
			@RequestParam("userPhone") String userPhone, @RequestParam("orderId") String orderId) {
		List<LogisticsInfo> list = logisticsInfoService.listLogisticsInfoBlur(userId, userPhone, orderId);
		Map maps = new HashMap();
		maps.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
		maps.put(CommonConstants.RESP_MESSAGE, "成功");
		maps.put(CommonConstants.RESULT, list);
		return maps;
	}

}
