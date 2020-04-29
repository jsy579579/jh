
package com.jh.mircomall.service;

import java.util.List;
import java.util.Map;

import com.jh.mircomall.bean.Goods;
import com.jh.mircomall.bean.ShoppingCart;

public interface ShoppingCartService {
	// 查询商品是否在购物车
	ShoppingCart selectgoods(int userId, int goodsId);

	// 更新商品数量
	int updategoodsNum(int Num, int id);

	// 添加商品到购物车
	int intsertgoods(ShoppingCart shop);

	// 显示购物车商品-分页
	List<ShoppingCart> shoppingcartPage(int businessId, int userId, int currentPage, int pageSize);

	// 购物车商品删除
	int deleteshoppingcartgoods(int id);

	// 更新购物车商品数量
	int updateShoppingCartgoodsNum(int goodsNum, int id);

	// 查询购物车商品总条数
	int getShoppingCartCount(int userId);
	
	//查询商品库存
	List<Goods> getGoodsNum(int id);

}
