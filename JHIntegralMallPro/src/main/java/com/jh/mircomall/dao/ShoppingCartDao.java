package com.jh.mircomall.dao;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Param;

import com.jh.mircomall.bean.Order;
import com.jh.mircomall.bean.ShoppingCart;

public interface ShoppingCartDao {

	ShoppingCart selectgoods(@Param("userId") int userId, @Param("goodsId") int goodsId);

	int updategoodsNum(@Param("Num") int Num, @Param("id") int id);

	int intsertgoods(ShoppingCart shop);

	List<ShoppingCart> selectgoodsPage(@Param("businessId") int businessId, @Param("userId") int userId,
			@Param("offset") int offset, @Param("limit") int limit);

	int deleteshoppingcartgoods(int id);

	int updateShoppingCartGoodsNum(@Param("goodsNum") int goodsNum, @Param("id") int id);
	
	int selectShopCartCount(@Param("userId") int userId);
}
