package com.jh.mircomall.controller;

import java.math.BigDecimal;
import java.net.URI;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import com.jh.mircomall.bean.ConsigneeAddress;
import com.jh.mircomall.bean.Goods;
import com.jh.mircomall.bean.Order;
import com.jh.mircomall.service.BusinessManagementService;
import com.jh.mircomall.service.BusinessOrderService;
import com.jh.mircomall.service.ConsigneeAddressService;
import com.jh.mircomall.service.OrderService;
import com.jh.mircomall.utils.OrderUtil;
import com.jh.mircomall.utils.Util;

import cn.jh.common.utils.CommonConstants;
import net.sf.json.JSONObject;

@SuppressWarnings("all")
@RequestMapping("/v1.0/integralmall/order")
@RestController
public class OrderController {
	@Autowired
	private OrderService orderService;
	@Autowired
	private ConsigneeAddressService consigneeAddressService;
	@Autowired
	private BusinessOrderService businessOrderService;
	@Autowired
	private BusinessManagementService businessManagementService;

	@Autowired
	Util util;

	/**
	 * 
	 * @param userId
	 * @param businessId
	 * @param consigneeId
	 * @param outOfPocket
	 * @param goodsLogo
	 * @param goodsUrl
	 * @param status
	 * @param goodsId
	 * @param goodsNum
	 * @return
	 */
	@RequestMapping(value = "/add", method = RequestMethod.POST)
	public Object addObject(@RequestParam("userid") int userId, @RequestParam("businessid") int businessId,
			@RequestParam("outofpocket") String outOfPocket, @RequestParam("consigneeid") int consigneeId,
			@RequestParam("goodslogo") String goodsLogo, @RequestParam("goodsurl") String goodsUrl,
			@RequestParam(value = "status", defaultValue = "0") int status, @RequestParam("goodsid") int goodsId,
			@RequestParam(value = "goodsnum", defaultValue = "1") int goodsNum,
			@RequestParam(value = "isDelete", defaultValue = "0") int isDelete) {
		String requestNo = "xinli" + System.currentTimeMillis();
		System.out.println("========请求订单号：" + requestNo);
		Order order = new Order();
		order.setOrderCode(requestNo);
		order.setUserId(userId);
		order.setBusinessId(businessId);
		order.setIsDelete(isDelete);
		Date now = new Date();
		order.setChangeTime(now);
		order.setCreateTime(now);
		ConsigneeAddress consigneeAddress = consigneeAddressService.getConsigneeAddress(userId, 1);
		Map map = new HashMap();
		if (consigneeAddress == null) {
			map.put(CommonConstants.RESP_CODE, CommonConstants.ERROR_PARAM);
			map.put(CommonConstants.RESP_MESSAGE, "该用户没有收件地址，请添加");
			map.put(CommonConstants.RESULT, consigneeAddress);
			return map;
		}
		order.setConsigneeId(consigneeAddress.getId());
		if (consigneeId != 0) {
			order.setConsigneeId(consigneeId);
		}
		order.setGoodsLogo(goodsLogo);
		order.setGoodsUrl(goodsUrl);
		order.setStatus(status);
		order.setGoodsId(goodsId);
		order.setOutOfPocket(outOfPocket);
		order.setGoodsNum(goodsNum);
		int isSuccess = orderService.addOrder(order);
		int id = order.getId();
		if (isSuccess > 0) {
			map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
			map.put(CommonConstants.RESP_MESSAGE, "成功");
			map.put("id", id);
			map.put("orderCode", requestNo);
		} else {
			map.put(CommonConstants.RESP_CODE, CommonConstants.ERROR_PARAM);
			map.put(CommonConstants.RESP_MESSAGE, "失败");
			map.put(CommonConstants.RESULT, isSuccess);
		}
		return map;
	}

	@RequestMapping(value = "/giveuptheorder", method = RequestMethod.POST)
	public Object giveUpTheOrder(@RequestParam("orderid") int orderId) {
		Order order = new Order();
		order.setIsDelete(1);
		order.setId(orderId);
		order.setChangeTime(new Date());
		int isSuccess = orderService.giveUpTheOrder(order);
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
	 * 积分结算
	 * 
	 * @param outOfPocket
	 * @param status
	 * @param id
	 * @param consigneeId
	 * @param paypassword
	 * @param token
	 * @return
	 */

	@RequestMapping(value = "/alreadySettled", method = RequestMethod.POST)
	public Object alreadySettled(@RequestParam("outOfPocket") String outOfPocket,
			@RequestParam(value = "status", defaultValue = "2") int status, @RequestParam("id") int id,
			@RequestParam("consigneeId") int consigneeId, @RequestParam("paypassword") String paypassword,
			@RequestParam("token") String token) {
		System.out.println("token");
		Order order = new Order();
		order.setOutOfPocket(outOfPocket);
		order.setStatus(status);
		order.setId(id);
		order.setConsigneeId(consigneeId);
		order.setChangeTime(new Date());
		int isSuccess = orderService.alreadySettled(order, paypassword, token);
		Map map = new HashMap();
		if (isSuccess > 0) {
			map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
			map.put(CommonConstants.RESP_MESSAGE, "支付成功");
			map.put(CommonConstants.RESULT, isSuccess);
		} else if (isSuccess == -1) {
			map.put(CommonConstants.RESP_CODE, CommonConstants.ERROR_PARAM);
			map.put(CommonConstants.RESP_MESSAGE, "支付密码错误");
			map.put(CommonConstants.RESULT, isSuccess);
		} else {
			map.put(CommonConstants.RESP_CODE, CommonConstants.ERROR_PARAM);
			map.put(CommonConstants.RESP_MESSAGE, "支付失败");
			map.put(CommonConstants.RESULT, isSuccess);
		}
		return map;
	}

	/**
	 * 获取用户订单
	 * 
	 * @author lirui
	 * @param userId
	 * @param pageIndex
	 * @param pageSize
	 * @param status
	 *            0：待支付 1：取消支付 2：支付完成 3：已收货 4：默认全部订单
	 * @return
	 */
	@RequestMapping(value = "/getOrderList", method = RequestMethod.POST)
	public Object getOrderList(@RequestParam("userId") int userId, @RequestParam("pageIndex") int pageIndex,
			@RequestParam("pageSize") int pageSize, @RequestParam("status") int status) {
		Map map = new HashMap();
		int total = 0;
		if (status == 4) {
			List<Order> list = orderService.getOrderListByUser(userId, pageIndex, pageSize);
			total = orderService.getOrderCount(userId);
			map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
			map.put(CommonConstants.RESP_MESSAGE, "获取订单信息成功");
			map.put(CommonConstants.RESULT, list);
			map.put("total", total);
		} else {
			List<Order> statusList = orderService.getOrderWaitingforPayment(pageIndex, pageSize, userId, status);
			total = orderService.getOrderCountByStatus(userId, status);
			map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
			map.put(CommonConstants.RESP_MESSAGE, "获取订单信息成功");
			map.put(CommonConstants.RESULT, statusList);
			map.put("total", total);
		}
		return map;
	}

	@RequestMapping(value = "/getOrder", method = RequestMethod.GET)
	public Object getOrder(@RequestParam("goodsid") int goodsId) {
		Order order = orderService.getOrder(goodsId);
		Map map = new HashMap();
		if (null == order) {
			map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
			map.put(CommonConstants.RESP_MESSAGE, "查无此订单");
			map.put(CommonConstants.RESULT, order);
		} else {
			map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
			map.put(CommonConstants.RESP_MESSAGE, "获取订单信息成功");
			map.put(CommonConstants.RESULT, order);
		}

		return map;
	}

	/*
	 * @RequestMapping(value = "/getWaitingOrder", method = RequestMethod.POST)
	 * public Object getOrderWaitingforpayment(@RequestParam("userid") int
	 * userId, @RequestParam("brandid") int brandId,
	 * 
	 * @RequestParam int status) { List<Order> order =
	 * orderService.getOrderWaitingforPayment(userId, brandId, status); Map map
	 * = new HashMap(); map.put(CommonConstants.RESP_CODE,
	 * CommonConstants.SUCCESS); map.put(CommonConstants.RESP_MESSAGE, "成功");
	 * map.put(CommonConstants.RESULT, order); return map; }
	 */

	@RequestMapping(value = "/removeOrder", method = RequestMethod.POST)
	public Object delOrder(@RequestParam("orderid") int orderId) {
		int isSuccess = orderService.removeOrder(orderId);
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

	@RequestMapping(value = "/getOrdeyOnly", method = RequestMethod.POST)
	public Object getOrderById(@RequestParam("orderid") int orderId) {
		List<Order> orderList = orderService.getOrderById(orderId);
		Map map = new HashMap();
		map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
		map.put(CommonConstants.RESP_MESSAGE, "成功");
		map.put(CommonConstants.RESULT, orderList);
		return map;
	}

	/**
	 * 根据订单号查询订单
	 * 
	 * @param orderCode
	 * @return
	 */
	@RequestMapping(value = "/getOrdeyByOrderCode", method = RequestMethod.POST)
	public Object getOrdeyByOrderCode(@RequestParam("orderCode") String orderCode) {
		Map map = new HashMap();
		List<Order> order = orderService.getOrderByOrderCode(orderCode);
		if (order != null) {
			map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
			map.put(CommonConstants.RESP_MESSAGE, "成功");
			map.put(CommonConstants.RESULT, order);
		} else {
			map.put(CommonConstants.RESP_CODE, CommonConstants.ERROR_PARAM);
			map.put(CommonConstants.RESP_MESSAGE, "查询无此订单");
			map.put(CommonConstants.RESULT, order);
		}
		return map;
	}

	/**
	 * 收货，已完成订单
	 * 
	 * @param id
	 * @param status
	 * @return
	 */
	@RequestMapping(value = "/getOrderCompletion", method = RequestMethod.POST)
	public Object getOrderCompletion(@RequestParam("id") int id, @RequestParam("status") int status) {
		Order order = new Order();
		Map map = new HashMap();
		order.setId(id);
		order.setStatus(status);
		int i = orderService.updateOrder(order);
		if (i > 0) {
			map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
			map.put(CommonConstants.RESP_MESSAGE, "收货成功");
		} else {
			map.put(CommonConstants.RESP_CODE, CommonConstants.ERROR_PARAM);
			map.put(CommonConstants.RESP_MESSAGE, "收货失败");
		}
		return map;
	}

	@RequestMapping(value = "/updateOrderAndGoods", method = RequestMethod.POST)
	public @ResponseBody Object Alipayment(@RequestParam("orderCode") String orderCode) {
		System.out.println("==========更新支付宝支付订单进来了*******");
		List<Order> orderlist = orderService.getOrderByOrderCode(orderCode);
		Order order = orderlist.get(0);
		Map map = new HashMap();
		int ordergoodsNum = order.getGoodsNum();// 订单内 购置商品数量
		try {
			List<Goods> goodsList = businessManagementService.getGoodsById(order.getGoodsId());
			int goodsNum = goodsList.get(0).getGoodsNum();// 商品数量
			order.setId(order.getId());
			order.setStatus(2);
			orderService.updateOrder(order);
			BigDecimal b1 = new BigDecimal(ordergoodsNum);
			BigDecimal b2 = new BigDecimal(goodsNum);
			int goodnum = b2.subtract(b1).intValue();
			Goods goods = new Goods();
			goods.setId(order.getGoodsId());
			goods.setGoodsNum(goodnum);
			businessManagementService.modifyGoods(goods);
			map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
			map.put(CommonConstants.RESP_MESSAGE, "更新订单成功");
			return map;
		} catch (Exception e) {
			map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			map.put(CommonConstants.RESP_MESSAGE, "更新订单失败");
			return map;
		}

	}

}
