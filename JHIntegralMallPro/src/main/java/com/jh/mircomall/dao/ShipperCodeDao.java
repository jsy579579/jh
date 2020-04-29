package com.jh.mircomall.dao;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.jh.mircomall.bean.ShipperCode;

public interface ShipperCodeDao {
	/**
	 * 查询所有快递编码信息
	 * 
	 * @Author ChenFan
	 * @return
	 */
	List<ShipperCode> selectShipperCode();

	/**
	 * 根据名字查询物流编码
	 * 
	 * @Author ChenFan
	 * @param name
	 * @return
	 */
	ShipperCode selectShipperCodeByName(@Param("name") String name);

    ShipperCode selectShipperName(@Param("expCode")String expCode);
}
