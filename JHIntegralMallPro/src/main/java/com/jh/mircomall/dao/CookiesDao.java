package com.jh.mircomall.dao;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.jh.mircomall.bean.Cookies;

public interface CookiesDao {
	/**
	 * 修改浏览表
	 * 
	 * @Author ChenFan
	 * @Date 2018年5月11日
	 * @param cookies
	 * @return
	 */
	public int updateCookies(Cookies cookies);

	/**
	 * 分页查询用户的浏览记录
	 * 
	 * @Author ChenFan
	 * @Date 2018年5月11日
	 * @param userId
	 * @param brandId
	 * @param offset
	 * @param limit
	 * @return
	 */
	public List<Cookies> getCookiesList(@Param("userId") int userId, @Param("brandId") int brandId,
			@Param("offset") int offset, @Param("limit") int limit);

	/**
	 * 新增浏览信息
	 * 
	 * @Author ChenFan
	 * @param cookies
	 * @return
	 */
	public int insertCookies(Cookies cookies);

	public int selectCookiesCount(@Param("userId") int userId, @Param("brandId") int brandId);

	public Cookies selectCookiesByGoodsid(@Param("goodsId") int goodsId, @Param("userId") int userId,
			@Param("brandId") int brandId);
}
