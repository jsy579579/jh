package com.jh.mircomall.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.jh.mircomall.bean.Goods;
import com.jh.mircomall.dao.GoodsDao;
import com.jh.mircomall.dao.GoodsParentDao;

@Service
public class BusinessManagementServiceImpl implements BusinessManagementService {
	@Autowired
	private GoodsDao goodsdao;
	private GoodsParentDao goodsParentDao;

	@Override
	public int GoodsUpload(Goods map) {

		return goodsdao.addGoods(map);
	}

	@Override
	public int removeGoods(int id) {
		return goodsdao.deleteGoods(id);
	}

	@Override
	public int modifyGoods(Map map) {

		return goodsdao.updateGoods(map);
	}

	@Override
	public List<Goods> getAllGoods(Map map) {

		return goodsdao.selectAllGoods(map);
	}

	@Override
	public List<Goods> getGoodsById(int id) {

		return goodsdao.selectGoodsById(id);
	}

	@Override
	public int addGoodsParent(int businessId, int parentid) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int removeGoodsParent(int businessId, int parentid) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int modifyGoods(Goods goods) {
		// TODO Auto-generated method stub
		return goodsdao.updateGoodsTest(goods);
	}

}
