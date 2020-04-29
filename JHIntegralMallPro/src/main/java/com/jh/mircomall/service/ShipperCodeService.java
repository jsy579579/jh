package com.jh.mircomall.service;

import java.util.List;

import com.jh.mircomall.bean.ShipperCode;

public interface ShipperCodeService {
	/**
	 * 显示所有的物流公司
	 *@Author ChenFan
	 * @return
	 */
	public List<ShipperCode> getShipperCode();
	/**
	 * 得到物流公司对应的编码
	 *@Author ChenFan
	 * @param name
	 * @return
	 */
	public String getShipperCodeByName(String name );
	/**显示物流公司名称
	 * @author lirui
	 * @param expCode
	 * @return
	 */
	public ShipperCode getShipperName(String expCode);
}
