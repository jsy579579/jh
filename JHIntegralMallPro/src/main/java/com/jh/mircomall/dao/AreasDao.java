package com.jh.mircomall.dao;

import java.util.List;

import com.jh.mircomall.bean.Areas;

public interface AreasDao {
	/**
	 * 根据citis Id查询区域信息
	 *@Author ChenFan
	 * @param citisId
	 * @return
	 */
	List<Areas> selectAreasByCitisId(String citisId);
}
