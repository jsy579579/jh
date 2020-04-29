package cn.jh.clearing.business;

import java.util.List;

import cn.jh.clearing.pojo.AutoRebateConfig;

public interface AutoRebateConfigBusiness {

	AutoRebateConfig createNewConfig(AutoRebateConfig model);

	List<AutoRebateConfig> findAllConfig();

	List<AutoRebateConfig> findByOnOff(int onOff);

	List<AutoRebateConfig> findByBrandId(long brandId);

	List<AutoRebateConfig> findByBrandIdAndOnOff(long brandId, int onOff);

	AutoRebateConfig findById(long configId);

	AutoRebateConfig updateOnOff(AutoRebateConfig model);

}
