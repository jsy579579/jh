package com.jh.mircomall.dao;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Param;
import com.jh.mircomall.bean.Goods;

public interface GoodsDao {
	// 添加商品
	int addGoods(Goods goods);

	// 删除商品
	int deleteGoods(int id);

	// 查询全部商品
	List<Goods> selectAllGoods(Map map);

	// 修改商品
	int updateGoods(Map map);

	// 查询商品信息goodsid
	List<Goods> selectGoodsById(int id);

	// 商品首页分页显示
	List<Goods> selectGoodsPage(@Param("businessId") int businessId, @Param("offset") int offset,
			@Param("limit") int limit);

	// 商品父分组分页显示
	List<Goods> selectGoodsPageByParentId(@Param("businessId") int businessId, @Param("oodsgTypeId") int oodsgTypeId,
			@Param("offset") int offset, @Param("limit") int limit);

	// 模糊查询
	List<Goods> selectLikeGoods(@Param("businessId") int businessId, @Param("text") String text,
			@Param("offset") int offset, @Param("limit") int limit);

	/**
	 * 修改商品表测试
	 * 
	 * @Author ChenFan
	 * @param goods
	 * @return
	 */
	int updateGoodsTest(Goods goods);

	/**
	 * 查询商品分类总条数
	 * 
	 * @param businessId
	 * @param oodsgTypeId
	 * @return
	 */
	int selectLeveCount(@Param("businessId") int businessId, @Param("oodsgTypeId") int oodsgTypeId);

	int selectGoodsCount(@Param("businessId") int businessId);

	int selectLikeCount(@Param("businessId") int businessId, @Param("text") String text);

	/**
	 * 根据分组获取商品
	 * 
	 * @param id
	 * @return
	 */
	List<Goods> getGoodsByGroups(@Param("id") int id);

	List<Goods> selectGoodsPageBycondition1(@Param("goodsName") String goodsName, @Param("businessId") int businessId,
			@Param("offset") int offset, @Param("limit") int limit);

	Goods selectGroupsByName(@Param("groupsName") String groupsName, @Param("brandId") String brandId);

	List<Goods> selectGoodsPageBycondition2(@Param("goodsName") String goodsName, @Param("oodsgTypeId") int oodsgTypeId,
			@Param("businessId") int businessId, @Param("offset") int offset, @Param("limit") int limit);
}
