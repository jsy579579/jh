package com.jh.mircomall.service;

import java.util.List;

import com.jh.mircomall.bean.Goods;
import com.jh.mircomall.bean.GoodsChildren;
import com.jh.mircomall.bean.GoodsParent;
import com.jh.mircomall.bean.Taobao;

public interface IndexService {
	/**
	 * 商品分页显示
	 * 
	 * @param businessId
	 * @param currentPage
	 * @param pageSize
	 * @return
	 */
	List<Goods> getGoodsPage(int businessId, int currentPage, int pageSize);

	/**
	 * 商品父分类分页
	 * 
	 * @param businessId
	 * @param oodsgTypeId
	 * @param currentPage
	 * @param pageSize
	 * @return
	 */
	List<Goods> getGoodsPageByParentId(int businessId, int oodsgTypeId, int currentPage, int pageSize);

	/**
	 * 查询所有父分类
	 * 
	 * @return
	 */
	List<Taobao> getGoodsLevel();

	List<Taobao> getGoodsLevel2(int id);

	/**
	 * 模糊查询
	 * 
	 * @param businessId
	 * @param text
	 * @param currentPage
	 * @param pageSize
	 * @return
	 */
	List<Goods> getLikeGoods(int businessId, String text, int currentPage, int pageSize);

	/**
	 * 查询分类商品总条数
	 * 
	 * @param businessId
	 * @param oodsgTypeId
	 * @return
	 */

	int getCount(int businessId, int oodsgTypeId);

	/**
	 * 查询商品总条数
	 * 
	 * @param businessId
	 * @return
	 */
	int getGoodsCount(int businessId);

	/**
	 * 模糊查询商品总条数
	 * 
	 * @param businessId
	 * @param text
	 * @return
	 */
	int getLikeCount(int businessId, String text);

	/**
	 * 查询分组商品
	 * 
	 * @param id
	 * @return
	 */
	List<Goods> getGoodsByGroups(int id);

	List<Goods> getGoodsPageBycondition2(int businessId, int oodsgTypeId, int currentPage, int pageSize,
			String goodsName);

	List<Goods> getGoodsPageBycondition1(int businessId, String goodsName, int currentPage, int pageSize);
}
