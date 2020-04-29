package com.jh.mircomall.dao;

import java.util.List;

import com.jh.mircomall.bean.Taobao;

public interface TaobaoDao {
	List<Taobao> selectLevel();

	List<Taobao> selectLevel2(int id);
}
