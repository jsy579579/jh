package com.jh.mircomall.service;

import java.net.URI;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.jasper.tagplugins.jstl.core.Url;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.jh.mircomall.bean.Goods;
import com.jh.mircomall.bean.LogisticsInfo;
import com.jh.mircomall.bean.Order;
import com.jh.mircomall.bean.PageForSQLModel;
import com.jh.mircomall.bean.User;
import com.jh.mircomall.bean.UserAccount;
import com.jh.mircomall.dao.OrderDao;
import com.jh.mircomall.dao.UserAccountDao;
import com.jh.mircomall.dao.UserDao;
import com.jh.mircomall.utils.PageUtil;
import com.jh.mircomall.utils.Util;

import cn.jh.common.utils.CommonConstants;
import cn.jh.common.utils.Md5Util;
import net.sf.json.JSONObject;

/**
 * 订单
 * 
 * @author ChenFan
 *
 */
@Service
public class OrderServiceImpl implements OrderService {

	private static final Logger LOG = LoggerFactory.getLogger(OrderServiceImpl.class);

	@Autowired
	Util util;

	@Autowired
	private OrderDao orderDao;
	@Autowired
	private UserDao userDao;
	@Autowired
	private BusinessManagementService businessManagementService;
	@Autowired
	private UserAccountDao userAccountDao;
	@Autowired
	private LogisticsInfoService logisticsInfoService;
	@Autowired
	private ShipperCodeService shipperCodeService;

	@Override
	public int addOrder(Order order) {
		return orderDao.insertOrder(order);
	}

	@Override
	public int giveUpTheOrder(Order order) {
		return orderDao.updateOrder(order);
	}

	@Override
	public int alreadySettled(Order order, String paypassword, String token) {
		int isSuccess = 0;
		int update = 0;
		/* 获取订单用户id和订单号 */
		List<Order> orderList = this.getOrderById(order.getId());
		Order orders = orderList.get(0);
		String orderCode = orders.getOrderCode();
		int userId = orders.getUserId();
		String paypass = paypassword;
		/** 支付密码验证 **/
		RestTemplate restTemplate = new RestTemplate();
		URI uri = util.getServiceUrl("user", "error url request!");
		String url = uri.toString() + "/v1.0/user/paypass/auth/" + token;
		MultiValueMap<String, String> requestEntity = new LinkedMultiValueMap<String, String>();
		requestEntity = new LinkedMultiValueMap<String, String>();
		requestEntity.add("paypass", paypass);
		restTemplate = new RestTemplate();
		Object result;
		JSONObject jsonObject;
		JSONObject resultObjb;
		String userCoin = "";
		result = restTemplate.postForObject(url, requestEntity, Object.class);
		LOG.info("RESULT================" + result);
		jsonObject = JSONObject.fromObject(result);
		String resp_code = "";
		resp_code = jsonObject.getString("resp_code");
		System.out.println("验证状态" + resp_code);
		if (!"000004".equals(resp_code)) {
			System.out.println("验证状态" + resp_code);
			// 订单中商品的数量
			int orderNum = orders.getGoodsNum().intValue();
			Integer goodsId = orders.getGoodsId();
			List<Goods> goodsList = businessManagementService.getGoodsById(goodsId);
			/* 获取用户积分 */
			uri = util.getServiceUrl("user", "error url request!");
			url = uri.toString() + "/v1.0/user/account/query/userId";
			MultiValueMap<String, Integer> requestEntitys = new LinkedMultiValueMap<String, Integer>();
			requestEntitys = new LinkedMultiValueMap<String, Integer>();
			requestEntitys.add("user_id", userId);
			result = restTemplate.postForObject(url, requestEntitys, Object.class);
			LOG.info("RESULT================" + result);
			jsonObject = JSONObject.fromObject(result);
			resultObjb = jsonObject.getJSONObject("result");
			userCoin = resultObjb.getString("coin");
			System.out.println("积分" + userCoin);
			int orderCoin = Integer.parseInt(userCoin);
			if (orderCoin >= Integer.parseInt(order.getOutOfPocket())) {
				// 用户积分就要减去商品积分
				/*
				 * int surplusCoin = orderCoin -
				 * Integer.parseInt(order.getOutOfPocket());
				 */
				/* 修改用户积分 */
				String cur_coin = "-" + order.getOutOfPocket().toString();
				uri = util.getServiceUrl("user", "error url request!");
				url = uri.toString() + "/v1.0/user/coin/update/userid";
				requestEntity = new LinkedMultiValueMap<String, String>();
				requestEntity.add("user_id", userId + "");
				requestEntity.add("coin", cur_coin);
				requestEntity.add("order_code", orderCode);
				try {
					result = restTemplate.postForObject(url, requestEntity, Object.class);
					LOG.info("RESULT================" + result);
					update = 1;
				} catch (Exception e) {
					update = 0;
					LOG.error("==========http://user/v1.0/user/coin/update/userid更新用户积分异常===========" + e);
				}
				// 只有在用户金额大于需要消费的积分的时候才能修改
				isSuccess = orderDao.updateOrder(order);
				if (update == 1) {
					isSuccess = 1;
					int surplusGoods = goodsList.get(0).getGoodsNum() - orderNum;
					// 订单成功之后 修改商品的数量
					Goods goods = new Goods();
					goods.setId(goodsList.get(0).getId());
					goods.setGoodsNum(surplusGoods);
					businessManagementService.modifyGoods(goods);
				}

			}
		} else {
			isSuccess = -1;
		}

		return isSuccess;
	}

	@Override
	public List<Order> getOrderListByUser(int userId, int pageIndex, int pageSize) {
		PageForSQLModel pfs = PageUtil.getPageInfoByPageNoAndSize(pageIndex, pageSize);
		List<Order> orderList = orderDao.selectOrderList(userId, pfs.getOffset(), pfs.getLimit());
		return orderList;
	}

	@Override
	public Order getOrder(int goodsId) {
		Order order = null;
		List<Order> orderList = orderDao.selectOrderByGoodsId(goodsId);
		if (null != orderList && orderList.size() != 0) {
			order = orderList.get(0);
			if (null != order.getLogisticsId()) {
				// 根据orderId查询物流信息
				List<LogisticsInfo> logisticsInfoList = logisticsInfoService.listLogisticsInfoByOrderId(order.getId());
				if (null != logisticsInfoList && logisticsInfoList.size() != 0) {
					String logisticsName = logisticsInfoList.get(0).getLogisticsName();
					String code = shipperCodeService.getShipperCodeByName(logisticsName);
					String logisticsNum = logisticsInfoList.get(0).getLogisticsNum();
					String track = null;
					try {
						track = logisticsInfoService.getOrderTracesByJson(code, logisticsNum);
					} catch (Exception e) {

						e.printStackTrace();
					}
					order.setTrack(track);
				}
			}
		}
		return order;
	}

	@Override
	public List<Order> getOrderWaitingforPayment(int pageIndex, int pageSize, int userId, int status) {
		PageForSQLModel pfs = PageUtil.getPageInfoByPageNoAndSize(pageIndex, pageSize);
		List<Order> orderList = orderDao.selectOrderByStatus(userId, status, pfs.getOffset(), pfs.getLimit());
		return orderList;
	}

	@Override
	public int removeOrder(int id) {
		Order order = new Order();
		order.setIsDelete(1);
		order.setId(id);
		return orderDao.updateOrder(order);
	}

	@Override
	public List<Order> getOrderById(int id) {
		return orderDao.selectOrderById(id);
	}

	@Override
	public int updateOrder(Order order) {

		return orderDao.updateOrder(order);
	}

	@Override
	public int calculationIntegral(int goodsId, int orderNum) {
		List<Goods> goodsList = businessManagementService.getGoodsById(goodsId);
		// 查询商品积分
		int goodsCoin = goodsList.get(0).getGoodsCoin().intValue();
		// 一共花费的积分
		int consumption = orderNum * goodsCoin;
		return consumption;
	}

	@Override
	public int getOrderCount(int userId) {
		return orderDao.selectOrderCount(userId);
	}

	@Override
	public int getOrderCountByStatus(int userId, int status) {
		return orderDao.selectOrderCountByStatus(userId, status);
	}

	@Override
	public List<Order> getOrderByOrderCode(String orderCode) {
		return orderDao.selectOrderByOrderCode(orderCode);
	}

}
