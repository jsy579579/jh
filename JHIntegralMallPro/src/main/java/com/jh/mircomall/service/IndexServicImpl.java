package com.jh.mircomall.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.jh.mircomall.bean.BusinessOrder;
import com.jh.mircomall.bean.Goods;
import com.jh.mircomall.bean.GoodsChildren;
import com.jh.mircomall.bean.GoodsParent;
import com.jh.mircomall.bean.PageForSQLModel;
import com.jh.mircomall.bean.Taobao;
import com.jh.mircomall.dao.GoodsDao;
import com.jh.mircomall.dao.GoodsParentDao;
import com.jh.mircomall.dao.TaobaoDao;
import com.jh.mircomall.utils.PageUtil;

@Service
public class IndexServicImpl implements IndexService {
	@Autowired
	private GoodsDao goodsDao;
	@Autowired
	private TaobaoDao taobaodao;
	@Autowired
	private RedisTemplate redisTemplate;

	@Override
	public List<Goods> getGoodsPage(int businessId, int currentPage, int pageSize) {
		PageForSQLModel pfs = PageUtil.getPageInfoByPageNoAndSize(currentPage, pageSize);
		List<Goods> list = goodsDao.selectGoodsPage(businessId, pfs.getOffset(), pfs.getLimit());
		return list;
	}

	@Override
	public List<Goods> getGoodsPageByParentId(int businessId, int oodsgTypeId, int currentPage, int pageSize) {
		PageForSQLModel pfs = PageUtil.getPageInfoByPageNoAndSize(currentPage, pageSize);
		List<Goods> list = goodsDao.selectGoodsPageByParentId(businessId, oodsgTypeId, pfs.getOffset(), pfs.getLimit());
		return list;
	}

	@Override
	public List<Goods> getLikeGoods(int businessId, String text, int currentPage, int pageSize) {
		PageForSQLModel pfs = PageUtil.getPageInfoByPageNoAndSize(currentPage, pageSize);
		List<Goods> list = goodsDao.selectLikeGoods(businessId, text, pfs.getOffset(), pfs.getLimit());
		return list;
	}

	@Override
	public List<Taobao> getGoodsLevel() {

		return taobaodao.selectLevel();
	}

	@Override
	public List<Taobao> getGoodsLevel2(int id) {
		return taobaodao.selectLevel2(id);
	}

	@Override
	public int getCount(int businessId, int oodsgTypeId) {
		return goodsDao.selectLeveCount(businessId, oodsgTypeId);
	}

	@Override
	public int getGoodsCount(int businessId) {
		return goodsDao.selectGoodsCount(businessId);
	}

	@Override
	public int getLikeCount(int businessId, String text) {
		return goodsDao.selectLikeCount(businessId, text);
	}

	@Override
	public List<Goods> getGoodsByGroups(int id) {
		return goodsDao.getGoodsByGroups(id);
	}

	@Override
	public List<Goods> getGoodsPageBycondition2(int businessId, int oodsgTypeId, int currentPage, int pageSize,
			String goodsName) {
		PageForSQLModel pfs = PageUtil.getPageInfoByPageNoAndSize(currentPage, pageSize);
		List<Goods> list = goodsDao.selectGoodsPageBycondition2(goodsName, oodsgTypeId, businessId, pfs.getOffset(),
				pfs.getLimit());
		return list;
	}

	@Override
	public List<Goods> getGoodsPageBycondition1(int businessId, String goodsName, int currentPage, int pageSize) {
		PageForSQLModel pfs = PageUtil.getPageInfoByPageNoAndSize(currentPage, pageSize);
		List<Goods> list = goodsDao.selectGoodsPageBycondition1(goodsName, businessId, pfs.getOffset(), pfs.getLimit());
		return list;
	}

}
