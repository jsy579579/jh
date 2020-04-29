package com.jh.mircomall.service;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.jh.mircomall.bean.Goods;
import com.jh.mircomall.bean.Order;
import com.jh.mircomall.bean.PageForSQLModel;
import com.jh.mircomall.bean.ShoppingCart;
import com.jh.mircomall.dao.GoodsDao;
import com.jh.mircomall.dao.ShoppingCartDao;
import com.jh.mircomall.utils.PageUtil;

@Service
public class ShoppingCartServiceImpl implements ShoppingCartService {
	@Autowired
	private ShoppingCartDao shoppingcartdao;
	
	@Autowired
	private GoodsDao goodsDao;

	@Override
	public List<ShoppingCart> shoppingcartPage(int businessId, int userId, int currentPage, int pageSize) {
		PageForSQLModel pfs = PageUtil.getPageInfoByPageNoAndSize(currentPage, pageSize);
		List<ShoppingCart> cartList = shoppingcartdao.selectgoodsPage(businessId, userId, pfs.getOffset(),
				pfs.getLimit());
		return cartList;
	}

	@Override
	public int deleteshoppingcartgoods(int id) {

		return shoppingcartdao.deleteshoppingcartgoods(id);
	}

	@Override
	public int updategoodsNum(int Num, int id) {

		return shoppingcartdao.updategoodsNum(Num, id);
	}

	@Override
	public ShoppingCart selectgoods(int userId, int goodsId) {

		return shoppingcartdao.selectgoods(userId, goodsId);
	}

	@Override
	public int intsertgoods(ShoppingCart shop) {

		return shoppingcartdao.intsertgoods(shop);
	}

	@Override
	public int updateShoppingCartgoodsNum(int goodsNum, int id) {

		return shoppingcartdao.updateShoppingCartGoodsNum(goodsNum, id);
	}

	@Override
	public int getShoppingCartCount(int userId) {
		return shoppingcartdao.selectShopCartCount(userId);
	}

	@Override
	public List<Goods> getGoodsNum(int id) {
		return goodsDao.selectGoodsById(id);
	}

}
