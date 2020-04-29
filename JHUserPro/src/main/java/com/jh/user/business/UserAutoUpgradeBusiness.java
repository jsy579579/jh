package com.jh.user.business;

import java.util.List;

import com.jh.user.pojo.BrandAutoUpdateConfig;
import com.jh.user.pojo.BrandAutoUpgrade;
import com.jh.user.pojo.User;

public interface UserAutoUpgradeBusiness {

	
	public List<User>  getNeedAutoGradeUser();
	
	public   List<BrandAutoUpgrade> findBrandAutoUpgradeByBrandid(long brandid);
	
	public BrandAutoUpgrade findBrandAutoUpgradeBybrandidAndchannelId( long brandid, long channelId);
	
	public  BrandAutoUpgrade saveBrandAutoUpgrade(BrandAutoUpgrade brandAutoUpgrade);
	
	public BrandAutoUpdateConfig saveBrandAutoUpdateConfig(BrandAutoUpdateConfig brandAutoUpdateConfig);
	
	public BrandAutoUpdateConfig  getBrandAutoByBrandidAndGrade(long brandid, long grade);
	public BrandAutoUpdateConfig getBrandAutoByBrandidAndGradeNostutas(long brandid,long grade);
	public List<BrandAutoUpdateConfig>  getBrandAutoConfigByBrandidNostutas(long brandid);
	public List<BrandAutoUpdateConfig>  getBrandAutoConfigByBrandid(long brandid);
	
	
}
