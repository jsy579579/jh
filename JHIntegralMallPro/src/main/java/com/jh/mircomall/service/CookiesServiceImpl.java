package com.jh.mircomall.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.jh.mircomall.bean.Cookies;
import com.jh.mircomall.bean.PageForSQLModel;
import com.jh.mircomall.dao.CookiesDao;
import com.jh.mircomall.utils.PageUtil;

@Service
public class CookiesServiceImpl implements CookiesService {

	@Autowired
	private CookiesDao cookiesDao;

	@Override
	public int removeCookies(Cookies cookies) {

		return cookiesDao.updateCookies(cookies);
	}

	@Override
	public List<Cookies> getCookies(int userId, int brandId, int pageIndex, int pageSize) {
		PageForSQLModel pfs = PageUtil.getPageInfoByPageNoAndSize(pageIndex, pageSize);
		return cookiesDao.getCookiesList(userId, brandId, pfs.getOffset(), pfs.getLimit());
	}

	@Override
	public int addCookies(Cookies cookies) {

		return cookiesDao.insertCookies(cookies);
	}

	@Override
	public int getCookiesCount(int userId,int brandId) {
		return cookiesDao.selectCookiesCount(userId, brandId);
	}

	@Override
	public Cookies getCookiesByGoodid(int goodsId, int userId,int brandId) {
		return cookiesDao.selectCookiesByGoodsid(goodsId, userId,brandId);
	}

}
