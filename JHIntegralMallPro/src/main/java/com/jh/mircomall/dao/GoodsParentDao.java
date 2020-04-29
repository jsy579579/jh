package com.jh.mircomall.dao;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Param;

import com.jh.mircomall.bean.GoodsParent;

public interface GoodsParentDao {
	List<GoodsParent> selectParent();
/*
	int addGoodsParent(@Param("businessId") int businessId, @Param("parentId") int parent,
			@Param("parentName") String parentName);

	int deleteGoodsParent(@Param("businessId") int businessId, @Param("parentId") int parent);

	List<GoodsParent> selectParentId(@Param("businessId") int businessId, @Param("parentId") int parent);

	List<GoodsParent> selectGoodsParent(@Param("businessId") int businessId, @Param("parentId") int parent);*/

}
