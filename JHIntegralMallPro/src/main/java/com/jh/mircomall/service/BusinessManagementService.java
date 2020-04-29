package com.jh.mircomall.service;

import java.util.List;
import java.util.Map;

import com.jh.mircomall.bean.Goods;


public interface BusinessManagementService {
	// 上传商品
	int GoodsUpload(Goods goods);

	// 删除商品
	int removeGoods(int id);

	// 修改商品信息
	int modifyGoods(Map map);

	// 查询全部商品
	List<Goods> getAllGoods(Map map);

	// 查询商品根据id
	List<Goods> getGoodsById(int id);

	// 添加商品父分组
	int addGoodsParent(int businessId, int parentid);

	// 删除商品父分组
	int removeGoodsParent(int businessId, int parentid);
	/**
	 * 修改商品
	 *@Author ChenFan
	 * @param goods
	 * @return
	 */
	int modifyGoods(Goods goods);
}
