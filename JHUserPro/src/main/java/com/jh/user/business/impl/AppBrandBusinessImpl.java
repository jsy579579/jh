package com.jh.user.business.impl;



import java.util.List;

import javax.persistence.EntityManager;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jh.user.business.AppBrandBusiness;
import com.jh.user.business.UserShopsBusiness;
import com.jh.user.pojo.AppImageText;
import com.jh.user.pojo.AppSlideshow;
import com.jh.user.pojo.UserShops;
import com.jh.user.repository.AppImageTextRepository;
import com.jh.user.repository.AppSlideshowRepository;
import com.jh.user.repository.ResourceRepository;
import com.jh.user.repository.UserShopsRepository;

@Service
public class AppBrandBusinessImpl implements AppBrandBusiness{

	@Autowired
	private EntityManager em;
	/*
	 * 轮播图
	 */
	@Autowired
	private AppSlideshowRepository  appSlideshowRepository;
	
	/**
	 * 图文库
	 * */
	@Autowired
	private AppImageTextRepository  appImageTextRepository;
	
	@Override
	public List<AppSlideshow> findAppSlideshow() {
		return appSlideshowRepository.findAll();
	}
	@Override
	public AppSlideshow findAppSlideshowById(long id) {
		return appSlideshowRepository.findAppSlideshowById(id);
	}
	@Override
	public List<AppSlideshow> findAppSlideshowByBrandId(long BrandId) {
		return appSlideshowRepository.findAppSlideshowBybrandId(BrandId);
	}
	@Transactional
	@Override
	public AppSlideshow addAppSlideshow(AppSlideshow appSlideshow) {
		return appSlideshowRepository.save(appSlideshow);
	}
	@Override
	public List<AppImageText> findAppImageText() {
		return appImageTextRepository.findAppImageText();
	}
	@Override
	public AppImageText findAppImageTextById(long id) {
		return appImageTextRepository.findAppImageTextById(id);
	}
	@Override
	public List<AppImageText> findAppImageTextByBrandId(long BrandId) {
		
		return appImageTextRepository.findAppImageTextBybrandId(BrandId);
	}
	
	@Transactional
	@Override
	public AppImageText addAppImageText(AppImageText appImageText) {
		return appImageTextRepository.save(appImageText);
	}
	
	@Transactional
	@Override
	public void delAppSlideshowById(long id) {
		
		 appSlideshowRepository.delAppSlideshowById(id);
	}
	/**
	 * 删除文库
	 * **/
	@Transactional
	@Override
	public void delAppImageTextById(long id){
		
		appImageTextRepository.delAppImageTextByById(id);
	}
	
}
