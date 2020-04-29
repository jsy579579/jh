package com.jh.good.business;

import com.jh.good.pojo.Carousel;

import java.util.List;

/**
 * 轮播图
 */
public interface CarouselBusiness {
    /**
     * 获取轮播图
     * @return
     */
    List<Carousel> findNeed();
}
