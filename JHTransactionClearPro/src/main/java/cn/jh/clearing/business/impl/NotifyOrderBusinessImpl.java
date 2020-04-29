package cn.jh.clearing.business.impl;

import java.util.Date;
import java.util.List;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cn.jh.clearing.business.NotifyOrderBusiness;
import cn.jh.clearing.pojo.BrandNotifyConfig;
import cn.jh.clearing.pojo.NotifyOrder;
import cn.jh.clearing.repository.BrandNotifyConfigRepository;
import cn.jh.clearing.repository.NotifyOrderRepository;

@Service
public class NotifyOrderBusinessImpl implements NotifyOrderBusiness {
	
	@Autowired
	private NotifyOrderRepository notifyOrderRepository;
	
	@Autowired
	private  BrandNotifyConfigRepository brandNotifyConfigRepository;

	@Override
	@Transactional
	public NotifyOrder save(NotifyOrder notifyOrder) {
		return notifyOrderRepository.saveAndFlush(notifyOrder);
	}

	@Override
	public List<NotifyOrder> findByNotifyTimeLessThan(Date date) {
		return notifyOrderRepository.findByNotifyTimeLessThan(date);
	}

	@Override
	public void delete(NotifyOrder notifyOrder) {
		notifyOrderRepository.delete(notifyOrder);
	}

	@Transactional
	@Override
	public void createBrandNotifyConfig(BrandNotifyConfig brandNotifyConfig) {
		brandNotifyConfigRepository.saveAndFlush(brandNotifyConfig);
	}

	@Override
	public BrandNotifyConfig getBrandNotifyConfigByBrandId(int brandId) {
		BrandNotifyConfig result = brandNotifyConfigRepository.getBrandNotifyConfigByBrandId(brandId);
		return result;
	}

}
