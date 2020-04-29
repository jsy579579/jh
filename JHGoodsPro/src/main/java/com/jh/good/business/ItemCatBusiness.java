package com.jh.good.business;

import com.jh.good.pojo.ItemCat;

import java.util.List;

/**
 * 商品分类接口
 */
public interface ItemCatBusiness {

    List<ItemCat> findAll();
}
