package com.jh.user.business;

import java.util.List;

import com.jh.user.pojo.AppImageText;
import com.jh.user.pojo.AppSlideshow;
import com.jh.user.pojo.UserShops;

public interface AppBrandBusiness {
	
	
	/*****---------------------------轮播图--------------------******/
	/**
	 * 查询所有商户信息
	 * */
	public List<AppSlideshow> findAppSlideshow();
	
	/**
	 * 通过商户Id查询商户信息
	 * */
	public AppSlideshow findAppSlideshowById(long id);
	
	/**
	 * 通过brandid查询商户
	 * **/
	public List<AppSlideshow> findAppSlideshowByBrandId(long BrandId);
	
	/**
	 * 删除轮播图
	 * **/
	public void delAppSlideshowById(long id);
	
	/**
	 * 删除文库
	 * **/
	public void delAppImageTextById(long id);
	
	
	/**
	 * 添加商户
	 * **/
	public AppSlideshow addAppSlideshow(AppSlideshow appSlideshow);

	/*****---------------------------图文库--------------------******/
	/**
	 * 查询所有商户信息
	 * */
	public List<AppImageText> findAppImageText();
	
	/**
	 * 通过商户Id查询商户信息
	 * */
	public AppImageText findAppImageTextById(long id);
	
	/**
	 * 通过brandid查询商户
	 * **/
	public List<AppImageText> findAppImageTextByBrandId(long BrandId);
	/**
	 * 添加商户
	 * **/
	public AppImageText addAppImageText(AppImageText appImageText);
	
}
