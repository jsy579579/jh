package com.jh.mircomall.service;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.jh.mircomall.bean.BusinessOrder;
import com.jh.mircomall.bean.Order;
import com.jh.mircomall.bean.PageForSQLModel;
import com.jh.mircomall.bean.ShoppingCart;
import com.jh.mircomall.dao.BusinessDao;
import com.jh.mircomall.dao.BusinessOrderDao;
import com.jh.mircomall.dao.OrderDao;
import com.jh.mircomall.utils.PageUtil;

@Service
public class BusinessOrderServiceImpl implements BusinessOrderService {
	@Autowired
	private BusinessOrderDao businessOrderDao;
	@Autowired
	private OrderDao orderdao;

	@Override
	public int addBusinessOrder(Order order) {

		return businessOrderDao.addBusinessOrder(order);
	}

	@Override
	public List<Order> getBusinessOrderPage(int businessId, int currentPage, int pageSize) {
		PageForSQLModel pfs = PageUtil.getPageInfoByPageNoAndSize(currentPage, pageSize);
		List<Order> list = orderdao.businessOrderPage(businessId, pfs.getOffset(),pfs.getLimit());
		return list;
	}

	@Override
	public int modifyBusinessOrder(Map map) {

		return businessOrderDao.updateBusinessOrder(map);
	}

	@Override
	public int removeBusinessOrder(int id) {
		return businessOrderDao.deleteBusinessOrder(id);
	}

	@Override
	public int getBusinessOrderCount(int businessId) {
		return  orderdao.selectBusinessOrderCount(businessId);
	}

}
