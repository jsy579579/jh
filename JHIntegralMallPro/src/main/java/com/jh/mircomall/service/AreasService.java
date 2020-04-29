package com.jh.mircomall.service;

import java.util.List;

import com.jh.mircomall.bean.Areas;

public interface AreasService {
	/**
	 * 得到对应的区县信息
	 *@Author ChenFan
	 * @param cityid
	 * @return
	 */
	List<Areas> getAreasList(String cityid);
}
