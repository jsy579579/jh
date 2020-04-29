package cn.jh.clearing.business.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cn.jh.clearing.business.AutoRebateConfigBusiness;
import cn.jh.clearing.pojo.AutoRebateConfig;
import cn.jh.clearing.repository.AutoRebateConfigRepository;
@Service
public class AutoRebateConfigBusinessImpl implements AutoRebateConfigBusiness {

	@Autowired
	private AutoRebateConfigRepository autoRebateConfigRepository;
	@Transactional
	@Override
	public AutoRebateConfig createNewConfig(AutoRebateConfig model) {
		return autoRebateConfigRepository.save(model);
	}

	@Override
	public List<AutoRebateConfig> findAllConfig() {
		return autoRebateConfigRepository.findAll();
	}

	@Override
	public List<AutoRebateConfig> findByOnOff(int onOff) {
		return autoRebateConfigRepository.findByOnOff(onOff);
	}

	@Override
	public List<AutoRebateConfig> findByBrandId(long brandId) {
		return autoRebateConfigRepository.findByBrandId(brandId);
	}

	@Override
	public List<AutoRebateConfig> findByBrandIdAndOnOff(long brandId, int onOff) {
		return autoRebateConfigRepository.findByBrandIdAndOnOff(brandId,onOff);
	}

	@Override
	public AutoRebateConfig findById(long configId) {
		return autoRebateConfigRepository.findOne(configId);
	}

	@Override
	public AutoRebateConfig updateOnOff(AutoRebateConfig model) {
		return autoRebateConfigRepository.save(model);
	}

}
