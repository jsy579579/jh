package cn.jh.clearing.business;

import java.util.Date;
import java.util.List;

import cn.jh.clearing.pojo.BrandNotifyConfig;
import cn.jh.clearing.pojo.NotifyOrder;

public interface NotifyOrderBusiness {

	NotifyOrder save(NotifyOrder notifyOrder);

	List<NotifyOrder> findByNotifyTimeLessThan(Date date);

	void delete(NotifyOrder notifyOrder);

	public void createBrandNotifyConfig(BrandNotifyConfig brandNotifyConfig);
	
	public BrandNotifyConfig getBrandNotifyConfigByBrandId(int brandId);
	
}
