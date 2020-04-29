package com.jh.user.business;

import java.util.List;

import com.jh.user.pojo.ThirdLevelDistribution;
import com.jh.user.pojo.ThirdLevelRate;
import com.jh.user.pojo.ThirdLevelRebateRatio;
import com.jh.user.pojo.ThirdLevelRebateRatioNew;
import com.jh.user.pojo.ThirdLevelRebateRatioNew2;

public interface ThirdLeveDistributionBusiness {

	
	public List<ThirdLevelDistribution>  getAllThirdLevelPrd(long brand);

	public ThirdLevelDistribution  getThirdLevelByBrandidandgrade(long brand,  int grade);
	
	
	public ThirdLevelDistribution getThirdLevelByBrandidandgradeNoStatus(long brand,
			int grade) ;
	
	
	public ThirdLevelDistribution  queryThirdLevelDistri(long thirdlevel);
	
	
	public List<ThirdLevelRate> findAllThirdLevelRates(long thirdlevel);
	
	/**
	 * 查询产品指定通道的费率
	 * **/
	public ThirdLevelRate findAllThirdLevelRatesBylevelidAndChannelId(long thirdlevel,long channelId);
	//查询贴牌下所有产品的指定通道费率
	public List<ThirdLevelRate> queryThirdLevelRatesBylevelidAndChannelId(long[] thirdlevel,long channelId);
	//根据三级分销id和通道id删除数据
	public void deleteThirdLevelThirdLevelRatesBylevelidAndChannelId(long[] thirdlevel,long channelId);
	
	public ThirdLevelRate addThirdLevelRates(ThirdLevelRate tlr);
	
	public ThirdLevelDistribution  mergeThirdDistribution(ThirdLevelDistribution distribution);
	
	public void delThirdLevelByid(long id); 
	
	public List<ThirdLevelRebateRatio>   getAllThirdRatio(long brand);
	
	
	public ThirdLevelRebateRatio getThirdRatioByBrandidandprelevel(long brand,  String prelevel);
	
	public ThirdLevelRebateRatio  mergeThirdLevelRebateRatio(ThirdLevelRebateRatio rebateRatio);
	
	//根据brandid查询最高等级
	public int findThirdLevelDistributionByBrandid(long brandId);
	
	//根据grade删除
	public void deleteThirdLevelDistributionByGrade(int grade,long brandId);

	public List<ThirdLevelRebateRatioNew> getAllThirdRatio(long brandid, Integer thirdLevelId);
	
	

	public ThirdLevelRebateRatioNew getByBrandIdAndPreLevelAndThirdLevelId(Long brandid, Integer thirdLevelId,String preLevel);

	public ThirdLevelRebateRatioNew mergeThirdLevelRebateRatio(ThirdLevelRebateRatioNew ratioModel);

	public List<ThirdLevelRebateRatioNew> getAllThirdRatioByBrandId(Long brandid);

	public ThirdLevelRebateRatioNew getThirdRatioById(Long id);
	
	public ThirdLevelRebateRatioNew2 getThirdLevelRebateRatioNew2ByBrandIdAndPreLevelAndThirdLevelId(Long brandid, Integer thirdLevelId,
			String preLevel);
	public List<ThirdLevelRebateRatioNew2> getAllThirdRatio2(long brandid, Integer thirdLevelId);
	
	public ThirdLevelRebateRatioNew2 mergeThirdLevelRebateRatio2(ThirdLevelRebateRatioNew2 ratioModel);
	
	public List<ThirdLevelRebateRatioNew2> getAllThirdRatio2ByBrandId(Long brandid) ;
	
	public ThirdLevelRebateRatioNew2 getThirdRatio2ById(Long id) ;

    ThirdLevelDistribution findByPrice(String price);
}
