package com.jh.user.business;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.jh.user.pojo.News;
import com.jh.user.pojo.NewsClassifiCation;

public interface NewsBusiness {
	
	public News createNews(News news);
	
	public List<News> getNewsByBrandId(String brandId);
	
	public News getNewsByBrandIdAndId(String brandId, long id);
	
	public List<News> getNewsByBrandIdAndId(String brandId, long[] id);
	
	public List<News> getNewsByBrandIdAndPublisher(String brandId, String publisher);
	
	public Page<News> getNewsByBrandIdAndPage(String brandId, Pageable pageable);
	
	public Page<News> getNewsByBrandIdAndClassifiCationAndPage(String brandId, String classifiCation, Pageable pageable);
	
	public Page<News> getNewsByBrandIdAndClassifiCationAndTitleAndPage(String brandId, String classifiCation, String title, Pageable pageable);
	
	public List<News> getNewsByBrandIdAndClassifiCationAndPage(String brandId, String classifiCation);
	
	public Page<News> getNewsByBrandIdAndTitleAndPage(String brandId, String title, Pageable pageable);
	
	public void deleteNews(News news);
	
	public NewsClassifiCation createNewsClassifiCation(NewsClassifiCation newsClassifiCation);
	
	public List<NewsClassifiCation> getNewsClassifiCationByBrandId(String brandId);
	
	public NewsClassifiCation getNewsClassifiCationByBrandIdAndClassifiCation(String brandId, String classifiCation);
	
	public void deleteNewsClassifiCation(NewsClassifiCation newsClassifiCation);
	
	public List<NewsClassifiCation> getNewsClassifiCationByBrandIdAndId(String brandId, long[] id);
}
