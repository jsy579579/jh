package com.jh.user.business.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jh.user.business.BrandManageBusiness;
import com.jh.user.pojo.Brand;
import com.jh.user.pojo.BrandCoin;
import com.jh.user.pojo.BrandRate;
import com.jh.user.pojo.BrandRebateRatio;
import com.jh.user.pojo.BrandResource;
import com.jh.user.pojo.Resource;
import com.jh.user.repository.BrandCoinRepository;
import com.jh.user.repository.BrandRateRepository;
import com.jh.user.repository.BrandRebateRatioRepository;
import com.jh.user.repository.BrandRepository;
import com.jh.user.repository.BrandResourceRepository;
import com.jh.user.repository.ResourceRepository;

@Service
public class BrandManageBusinessImpl implements BrandManageBusiness{

	@Autowired
	private BrandRateRepository brandRateRepository;

	@Autowired
	private BrandRepository brandRepository;
	
	@Autowired
	private BrandResourceRepository brandResourceRepository;

	@Autowired
	private BrandCoinRepository brandCoinRepository;

	@Autowired
	private ResourceRepository  resourceRepository;
	
	@Autowired
	private BrandRebateRatioRepository brandRebateRatioRepository;
	
	@Autowired
	private EntityManager em;
	
	@Override
	public Brand findBrandById(long id) {
		Brand  brand=new Brand();
		brand=brandRepository.findBrandByid(id);
		return brand;
	}
	
	@Override
	public  List<Brand>   findAllBrand(){
		  List<Brand>   brands=new ArrayList<Brand>();
		  
		  brands=brandRepository.findAllBrand();
		return brands;
	}

	@Transactional
	@Override
	public Brand mergeBrand(Brand brand) {
		
		Brand result = brandRepository.save(brand);
		em.flush();
		return result;
	}
	
	
	@Override
	public Brand  mergeBrandNumber(String number) {
		Brand result = brandRepository.findBrandNumber(number);
		return result;
	}

	@Override
	public List<Brand> findBrandByName(String name) {
		return brandRepository.findBrandByName(name);
	}

	@Override
	public Brand findBrandByManageid(long manageid) {
		return brandRepository.findBrandByUserid(manageid);
	}
	
	@Override
	public  List<Brand>  findBrandByManageids(Long[]  manageids){
		 List<Brand>  brands= new ArrayList<Brand>();
		 brands=brandRepository.findBrandByUserids(manageids);
				 
		return brands;
	}

	@Transactional
	@Override
	public BrandRate mergeBrandRate(BrandRate brandRate) {
		BrandRate result  = brandRateRepository.save(brandRate);
		em.flush();
		em.clear();
		return result;
	}

	@Override
	public BrandRate findRateByBrandAndChannel(long brandid, long channelid) {
		em.clear();
		return brandRateRepository.findBrandRateBybrandidAndChannelid(brandid, channelid);
	}
	
	@Override
	public List<BrandRate> findRateByBrand(long brandid) {
		
		List<BrandRate> brandRateList=brandRateRepository.findBrandRateBybrandid(brandid);
		
		return brandRateList;
	}

	@Override
	public List<Resource> findResourceByBrand(long brandid) {
		
		return brandResourceRepository.findBrandResourceBybrandid(brandid);
	}

	@Transactional
	@Override
	public void delResourceByBrand(long brandid, long resourceid) {
		brandResourceRepository.delResourceByBrandidAndResource(brandid, resourceid);
	}

	@Override
	public BrandResource saveBrandResource(BrandResource brandResource) {
		BrandResource result =  brandResourceRepository.save(brandResource);
		em.flush();
		return result;
	}

	@Override
	public List<Resource> findAllResource() {
		// TODO Auto-generated method stub
		return resourceRepository.findAllResource();
	}

	@Transactional
	@Override
	public Resource mergeResource(Resource resource) {
		Resource result = resourceRepository.save(resource);
		em.flush();
		return result;
	}

	@Transactional
	@Override
	public void delResource(long resourceid) {
		resourceRepository.delResource(resourceid);
	}

	@Override
	public BrandCoin findBrandCoin(long brandid) {
		// TODO Auto-generated method stub
		return brandCoinRepository.findBrandCoinBybrandid(brandid);
	}
	
	@Transactional
	@Override
	public BrandCoin addBrandCoin(BrandCoin Brandcoin) {
		BrandCoin result =  brandCoinRepository.save(Brandcoin);
		em.flush();
		return result;
	}

	@Transactional
	@Override
	public void updateBrandMange(long brandid, String branddescription) {
		brandRepository.updateBrandDescription(brandid, branddescription);
	}

	@Override
	public List<BrandRate> findMinRateByChannelId(long channelid,BigDecimal costRate) {
		return brandRateRepository.findMinRateByChannelId(channelid,costRate);
	}

	@Transactional
	@Override
	public void createBrandRebateRatio(BrandRebateRatio brandRebateRatio) {
		brandRebateRatioRepository.saveAndFlush(brandRebateRatio);
		em.clear();
	}

	@Override
	public List<BrandRebateRatio> getBrandRebateRatioByBrandId(int brandId) {
		em.clear();
		List<BrandRebateRatio> result = brandRebateRatioRepository.getBrandRebateRatioByBrandId(brandId);
		return result;
	}

	@Override
	public BrandRebateRatio getBrandRebateRatioByBrandIdAndGrade(int brandId, int grade) {
		em.clear();
		BrandRebateRatio result = brandRebateRatioRepository.getBrandRebateRatioByBrandIdAndGrade(brandId, grade);
		return result;
	}

	@Override
	public BrandRebateRatio getBrandRebateRatioByBrandIdAndId(int brandId, long id) {
		em.clear();
		BrandRebateRatio result = brandRebateRatioRepository.getBrandRebateRatioByBrandIdAndId(brandId, id);
		return result;
	}

	@Override
	public List<BrandRebateRatio> getBrandRebateRatioByBrandIdAndId(int brandId, long[] id) {
		em.clear();
		List<BrandRebateRatio> result = brandRebateRatioRepository.getBrandRebateRatioByBrandIdAndId(brandId, id);
		return result;
	}

	@Transactional
	@Override
	public void deleteBrandRebateRatio(BrandRebateRatio brandRebateRatio) {
		brandRebateRatioRepository.delete(brandRebateRatio);
	}

}
