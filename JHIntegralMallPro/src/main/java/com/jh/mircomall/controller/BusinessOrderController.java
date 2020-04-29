package com.jh.mircomall.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.jh.mircomall.bean.BusinessOrder;
import com.jh.mircomall.bean.Order;
import com.jh.mircomall.service.BusinessOrderService;
import com.jh.mircomall.service.OrderService;

import cn.jh.common.utils.CommonConstants;

@RestController
@RequestMapping("/v1.0/integralmall/businessOrder")
public class BusinessOrderController {

	private static final Logger log = LoggerFactory.getLogger(BusinessOrderController.class);
	@Autowired
	private BusinessOrderService businessOrderService;
	@Autowired
	private OrderService orderService;

	/**
	 * 商家订单分页
	 * 
	 * @param request
	 * @param businessId
	 * @param currentPage
	 * @param pageSize
	 * @return
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/businessorderpage")
	public @ResponseBody Object BusinessOrderPage(HttpServletRequest request,
			@RequestParam("businessId") int businessId, @RequestParam("currentPage") int currentPage,
			@RequestParam("pageSize") int pageSize) {
		Map maps = new HashMap();
		List<Order> list = businessOrderService.getBusinessOrderPage(businessId, currentPage, pageSize);
		if (list.size() > 0 && !"".equals(list)) {
			int total = businessOrderService.getBusinessOrderCount(businessId);
			maps.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
			maps.put(CommonConstants.RESP_MESSAGE, "成功");
			maps.put(CommonConstants.RESULT, list);
			maps.put("total", total);
			maps.put("totalpage", sum(total, pageSize));

		} else {
			maps.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
			maps.put(CommonConstants.RESP_MESSAGE, "成功");
			maps.put(CommonConstants.RESULT, list);
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
	 * 商家填写运单
	 * 
	 * @author lirui
	 * @param request
	 * @param id
	 * @param logisticsNum
	 * @return
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/updatebusinessorder")
	public @ResponseBody Object updateBusinessOrder(HttpServletRequest request, @RequestParam("id") Integer id,
			@RequestParam("logisticsNum") String logisticsNum, @RequestParam("shipperId") Integer shipperId) {
		Map map = new HashMap();
		Order order = new Order();
		order.setId(id);
		order.setLogisticsNum(logisticsNum);
		order.setShipperId(shipperId);
		int i = orderService.updateOrder(order);
		if (i > 0) {
			map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
			map.put(CommonConstants.RESP_MESSAGE, "成功");
		} else {
			map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			map.put(CommonConstants.RESP_MESSAGE, "失败");
		}
		return map;
	}
}
