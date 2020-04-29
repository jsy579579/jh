package com.jh.mircomall.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.jh.mircomall.bean.ConsigneeAddress;
import com.jh.mircomall.dao.ConsigneeAddressDao;

@SuppressWarnings("all")
@Service
public class ConsigneeAddressServiceImpl implements ConsigneeAddressService {
	@Autowired
	private ConsigneeAddressDao consigneeAddressDao;

	@Override
	public int addConsigneeAddress(ConsigneeAddress consigneeAddress) {
		// 查询是否存在数据
		int count = this.getCountConsignAddressByUserId(consigneeAddress.getUserId());
		if (count <= 0) {
			// 设置为默认地址
			consigneeAddress.setDefaultAddr(1);
		}
		// 当需要新增的地址被设置为默认地址时
		if (consigneeAddress.getDefaultAddr().intValue() == 1 && count > 0) {
			ConsigneeAddress address = this.getConsigneeAddress(consigneeAddress.getUserId(), 1);
			// 修改为不是默认地址
			if (null != address) {
				address.setDefaultAddr(0);
				int isSuccess = this.modifyConsigneeAddress(address);
			}
		}
		return consigneeAddressDao.insertConsigneeAddress(consigneeAddress);
	}

	@Override
	public int removeConsignAddress(int userId, int consigneeAddressId) {
		// 查询被删除的地址是否是默认地址
		ConsigneeAddress consigneeAddress = this.getConsigneeAddressById(consigneeAddressId);
		if (consigneeAddress.getDefaultAddr().intValue() == 1) {
			// 是默认地址 就把按时间排序最新的一条变为默认地址
			List<ConsigneeAddress> addressList = this.getAllVonsigneeAddress(userId, 1);
			if (null != addressList && addressList.size() != 0) {
				ConsigneeAddress address = addressList.get(0);
				address.setDefaultAddr(1);
				this.modifyConsigneeAddress(address);
			}
		}
		return consigneeAddressDao.deleteConsigneeAddress(consigneeAddressId);
	}

	@Override
	public int getCountConsignAddressByUserId(int userId) {
		return consigneeAddressDao.countConsigneeAddressByUserId(userId);
	}

	@Override
	public List<ConsigneeAddress> getAllVonsigneeAddress(int userId, int isTimeOrderBy) {
		return consigneeAddressDao.selectAllConsigneeAddress(userId, isTimeOrderBy);
	}

	@Override
	public ConsigneeAddress getConsigneeAddress(int userId, int defaultAddr) {
		return consigneeAddressDao.selectConsigneeAddress(userId, defaultAddr);
	}

	@Override
	public int modifyConsigneeAddress(ConsigneeAddress consigneeAddress) {
		return consigneeAddressDao.updateConsigneeAddress(consigneeAddress);
	}

	@Override
	public ConsigneeAddress getConsigneeAddressById(int id) {

		return consigneeAddressDao.selectConsigneeAddressById(id);
	}

	@Override
	public int modifydefaultAddress(ConsigneeAddress consigneeAddress) {
		int count = this.getCountConsignAddressByUserId(consigneeAddress.getUserId());
		if (consigneeAddress.getDefaultAddr().intValue() == 1 && count > 0) {
			ConsigneeAddress address = this.getConsigneeAddress(consigneeAddress.getUserId(), 1);
			// 修改为不是默认地址
			if (null != address) {
				address.setDefaultAddr(0);
				int isSuccess = this.modifyConsigneeAddress(address);
			}
		}
		return consigneeAddressDao.updateConsigneeAddress(consigneeAddress);

	}
}