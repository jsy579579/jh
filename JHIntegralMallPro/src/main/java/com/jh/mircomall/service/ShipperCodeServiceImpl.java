package com.jh.mircomall.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.jh.mircomall.bean.ShipperCode;
import com.jh.mircomall.dao.ShipperCodeDao;

@Service
public class ShipperCodeServiceImpl implements ShipperCodeService {
	@Autowired
	private ShipperCodeDao shipperCodeDao;

	@Override
	public List<ShipperCode> getShipperCode() {
		return shipperCodeDao.selectShipperCode();
	}

	@Override
	public String getShipperCodeByName(String name) {
		ShipperCode shipperCode = shipperCodeDao.selectShipperCodeByName(name);
		return shipperCode.getCode();
	}

	@Override
	public ShipperCode getShipperName(String expCode) {
		return shipperCodeDao.selectShipperName(expCode);
	}

}
