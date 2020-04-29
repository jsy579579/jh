package com.jh.mircomall.service;

import java.util.List;

import com.jh.mircomall.bean.Cookies;

public interface CookiesService {
	/**
	 * 逻辑删除浏览记录
	 * 
	 * @Author ChenFan
	 * @Date 2018年5月11日
	 * @param cookies
	 * @return
	 */
	public int removeCookies(Cookies cookies);

	/**
	 * 分页显示浏览记录
	 * 
	 * @Author ChenFan
	 * @Date 2018年5月11日
	 * @param userId
	 * @param brandId
	 * @param pageIndex
	 * @param pageSize
	 * @return
	 */
	public List<Cookies> getCookies(int userId, int brandId, int pageIndex, int pageSize);

	/**
	 * 新增浏览记录
	 * 
	 * @Author ChenFan
	 * @param cookies
	 * @return
	 */
	public int addCookies(Cookies cookies);

	public int getCookiesCount(int userId, int brandId);

	public Cookies getCookiesByGoodid(int goodsId, int userId, int brandId);
}
