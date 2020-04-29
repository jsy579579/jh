package com.jh.good.business;

import com.jh.good.pojo.Goods;
import org.springframework.data.domain.Page;

import java.util.List;

/**
 * 商品接口
 */
public interface GoodsBusiness {

    //查询所有商品
    List<Goods> findAll();
    //分页带搜索商品
    Page<Goods> searchGoods(int page, int size, String goodsName,Long category1Id);
    //新增商品
    void save(Goods goods);
    //查询单个商品
    Goods findById(Long id);
    // 商品上下架
    void isMarketable(Long goodsId, String isMarketable);
}
