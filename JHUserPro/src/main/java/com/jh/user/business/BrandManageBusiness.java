package com.jh.user.business;

import java.math.BigDecimal;
import java.util.List;

import com.jh.user.pojo.Brand;
import com.jh.user.pojo.BrandCoin;
import com.jh.user.pojo.BrandRate;
import com.jh.user.pojo.BrandRebateRatio;
import com.jh.user.pojo.BrandResource;
import com.jh.user.pojo.Resource;

public interface BrandManageBusiness {

	public Brand  findBrandById(long  id);
	
	public  List<Brand>   findAllBrand();
	
	public Brand  mergeBrand(Brand brand);
	
	public Brand  mergeBrandNumber(String number);

	public BrandCoin  findBrandCoin(long brandid);

	public BrandCoin addBrandCoin(BrandCoin Brandcoin);
	
	public List<Brand>  findBrandByName(String name);

	public Brand  findBrandByManageid(long  manageid);
	
	public  List<Brand>  findBrandByManageids(Long[]  manageids);
	
	public BrandRate mergeBrandRate(BrandRate brandRate);
	
	public BrandRate findRateByBrandAndChannel(long brandid,  long channelid);
	
	public List<BrandRate> findRateByBrand(long brandid);

	public List<Resource> findResourceByBrand(long brandid);

	public void  delResourceByBrand(long brandid,  long resourceid);
	
	public BrandResource   saveBrandResource(BrandResource  brandResource);

	public List<Resource>  findAllResource();

	public Resource  mergeResource(Resource resource);
	
	public void delResource(long resourceid);
	
	public void updateBrandMange(long brandid, String branddescription);

	public List<BrandRate> findMinRateByChannelId(long channelid,BigDecimal costRate);

	public void createBrandRebateRatio(BrandRebateRatio brandRebateRatio);
	
	public List<BrandRebateRatio> getBrandRebateRatioByBrandId(int brandId);
	
	public BrandRebateRatio getBrandRebateRatioByBrandIdAndGrade(int brandId, int grade);

	public BrandRebateRatio getBrandRebateRatioByBrandIdAndId(int brandId, long id);
	
	public List<BrandRebateRatio> getBrandRebateRatioByBrandIdAndId(int brandId, long[] id);
	
	public void deleteBrandRebateRatio(BrandRebateRatio brandRebateRatio);
	
}
