package com.jh.mircomall.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.w3c.dom.ls.LSInput;

import com.jh.mircomall.bean.Goods;
import com.jh.mircomall.bean.ShoppingCart;
import com.jh.mircomall.service.ShoppingCartService;

import cn.jh.common.utils.CommonConstants;

@RestController
@RequestMapping("/v1.0/integralmall/shopCart")
public class ShopCartController {

	@Autowired
	private ShoppingCartService shoppingCartService;

	/**
	 * 添加购物车
	 * 
	 * @param request
	 * @param userId
	 * @param goodsId
	 * @param goodsLogo
	 * @param goodsCoin
	 * @param businessId
	 * @param goodsNum
	 * @param goodsPrice
	 * @return
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/addshoppingcart")
	public @ResponseBody Object addShopCart(HttpServletRequest request, @RequestParam("userId") Integer userId,
			@RequestParam("goodsId") Integer goodsId, @RequestParam("goodsLogo") String goodsLogo,
			@RequestParam("goodsCoin") Integer goodsCoin, @RequestParam("businessId") Integer businessId,
			@RequestParam("number") Integer number, @RequestParam("goodsPrice") String goodsPrice) {
		ShoppingCart shop = new ShoppingCart();
		shop.setBusinessId(businessId);
		shop.setGoodsCoin(goodsCoin);
		shop.setGoodsId(goodsId);
		shop.setGoodsLogo(goodsLogo);
		shop.setGoodsPrice(goodsPrice);
		shop.setUserId(userId);
		shop.setGoodsNum(number);
		Map maps = new HashMap();
		// 判断商品是否存在
		ShoppingCart cart = shoppingCartService.selectgoods(userId, goodsId);
		if (cart != null) {
			int id = cart.getId();
			int num = cart.getGoodsNum();
			int Num = num + number;
			/* 已存在,执行更新商品数量 */
			int i = shoppingCartService.updategoodsNum(Num, id);
			if (i <= 0) {
				maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
				maps.put(CommonConstants.RESP_MESSAGE, "添加购物车失败");
			} else {
				maps.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
				maps.put(CommonConstants.RESP_MESSAGE, "添加购物车成功");
			}
			/* 未存在，执行添加购物车 */
		} else {
			int m = shoppingCartService.intsertgoods(shop);
			if (m <= 0) {
				maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
				maps.put(CommonConstants.RESP_MESSAGE, "添加购物车失败");
			} else {
				maps.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
				maps.put(CommonConstants.RESP_MESSAGE, "添加购物车成功");
			}
		}

		return maps;

	}

	// 显示购物车分页
	@RequestMapping(method = RequestMethod.POST, value = "/shoppingcartpage")
	public @ResponseBody Object ShoppingCartPage(HttpServletRequest request, @RequestParam("businessId") int businessId,
			@RequestParam("userId") int userId, @RequestParam("currentPage") int currentPage,
			@RequestParam("pageSize") int pageSize) {
		Map maps = new HashMap();
		List<ShoppingCart> listpage = shoppingCartService.shoppingcartPage(businessId, userId, currentPage, pageSize);
		if (listpage.size() > 0 && !"".equals(listpage)) {
			int total = shoppingCartService.getShoppingCartCount(userId);
			maps.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
			maps.put(CommonConstants.RESP_MESSAGE, "购物车查询成功");
			maps.put(CommonConstants.RESULT, listpage);
			maps.put("total", total);
		} else {
			maps.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
			maps.put(CommonConstants.RESP_MESSAGE, "购物车查询成功");
			maps.put(CommonConstants.RESULT, listpage);
			maps.put("total", 0);
		}
		return maps;

	}

	// 删除购物车商品
	@RequestMapping(method = RequestMethod.POST, value = "/deleteshoppingcartgoods")
	public @ResponseBody Object DeleteShoppingCart(HttpServletRequest request, @RequestParam("arr") int arr[]) {
		Map maps = new HashMap();
		for (int i = 0; i < arr.length; i++) {
			int id = arr[i];
			int g = shoppingCartService.deleteshoppingcartgoods(id);
			if (g > 0) {
				maps.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
				maps.put(CommonConstants.RESP_MESSAGE, "删除成功");
			} else {
				maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
				maps.put(CommonConstants.RESP_MESSAGE, "删除失败");
			}
		}

		return maps;

	}

	/**
	 * 修改购物车内商品数量
	 * 
	 * @param request
	 * @param goodsNum
	 * @param id
	 * @return
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/updateshoppingcartgoodsnum")
	public @ResponseBody Object updateShoppingCartGoodsNum(HttpServletRequest request,
			@RequestParam("goodsNum") int goodsNum, @RequestParam("id") int id) {
		Map maps = new HashMap();
		int g = shoppingCartService.updategoodsNum(goodsNum, id);
		if (g > 0) {
			maps.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
			maps.put(CommonConstants.RESP_MESSAGE, "修改购买数量成功");
		} else {
			maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			maps.put(CommonConstants.RESP_MESSAGE, "修改购买数量失败");
		}

		return maps;

	}

	@RequestMapping(method = RequestMethod.POST, value = "/getGoodsNum")
	public @ResponseBody Object getGoodsNum(HttpServletRequest request, @RequestParam("id") int id) {
		Map maps = new HashMap();
		List<Goods> list = shoppingCartService.getGoodsNum(id);
		if (list != null) {
			maps.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
			maps.put(CommonConstants.RESP_MESSAGE, "查询成功");
			maps.put(CommonConstants.RESULT, list);
		} else {
			maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			maps.put(CommonConstants.RESP_MESSAGE, "查询失败");
		}

		return maps;

	}

}
