package com.jh.user.business.impl;

import java.util.List;

import javax.persistence.EntityManager;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jh.user.business.NewsBusiness;
import com.jh.user.pojo.News;
import com.jh.user.pojo.NewsClassifiCation;
import com.jh.user.repository.NewsClassifiCationRepository;
import com.jh.user.repository.NewsRepository;

@Service
public class NewsBusinessImpl implements NewsBusiness {

	@Autowired
	private EntityManager em;
	
	@Autowired
	private NewsRepository newsRepository;
	
	@Autowired
	private NewsClassifiCationRepository newsClassifiCationRepository;
	
	@Transactional
	@Override
	public News createNews(News news) {
		News result = newsRepository.save(news);
		em.flush();
		return result;
	}

	@Override
	public List<News> getNewsByBrandId(String brandId) {
		em.clear();
		List<News> result = newsRepository.getNewsByBrandId(brandId);
		return result;
	}

	@Override
	public List<News> getNewsByBrandIdAndPublisher(String brandId, String publisher) {
		
		return null;
	}

	@Override
	public Page<News> getNewsByBrandIdAndPage(String brandId, Pageable pageable) {
		em.clear();
		Page<News> result = newsRepository.getNewsByBrandIdAndPage(brandId, pageable);
		return result;
	}

	@Transactional
	@Override
	public void deleteNews(News news) {
		newsRepository.delete(news);
	}

	@Override
	public List<News> getNewsByBrandIdAndId(String brandId, long[] id) {
		em.clear();
		List<News> result = newsRepository.getNewsByBrandIdAndId(brandId, id);
		return result;
	}

	@Override
	public Page<News> getNewsByBrandIdAndClassifiCationAndPage(String brandId, String classifiCation, Pageable pageable) {
		em.clear();
		Page<News> result = newsRepository.getNewsByBrandIdAndClassifiCation(brandId, classifiCation, pageable);
		return result;
	}

	@Override
	public News getNewsByBrandIdAndId(String brandId, long id) {
		em.clear();
		News result = newsRepository.getNewsByBrandIdAndId(brandId, id);
		return result;
	}

	@Transactional
	@Override
	public NewsClassifiCation createNewsClassifiCation(NewsClassifiCation newsClassifiCation) {
		NewsClassifiCation result = newsClassifiCationRepository.save(newsClassifiCation);
		em.flush();
		return result;
	}

	@Override
	public List<NewsClassifiCation> getNewsClassifiCationByBrandId(String brandId) {
		em.clear();
		List<NewsClassifiCation> result = newsClassifiCationRepository.getNewsClassifiCationByBrandId(brandId);
		return result;
	}


	@Override
	public NewsClassifiCation getNewsClassifiCationByBrandIdAndClassifiCation(String brandId, String classifiCation) {
		em.clear();
		NewsClassifiCation result = newsClassifiCationRepository.getNewsClassifiCationByBrandIdAndClassifiCation(brandId, classifiCation);
		return result;
	}


	@Override
	public Page<News> getNewsByBrandIdAndClassifiCationAndTitleAndPage(String brandId, String classifiCation,
			String title, Pageable pageable) {
		em.clear();
		Page<News> result = newsRepository.getNewsByBrandIdAndClassifiCationAndTitleAndPage(brandId, classifiCation, "%" + title + "%", pageable);
		return result;
	}


	@Override
	public Page<News> getNewsByBrandIdAndTitleAndPage(String brandId, String title, Pageable pageable) {
		em.clear();
		Page<News> result = newsRepository.getNewsByBrandIdAndTitleAndPage(brandId, "%" + title + "%", pageable);
		return result;
	}

	@Transactional
	@Override
	public void deleteNewsClassifiCation(NewsClassifiCation newsClassifiCation) {
		newsClassifiCationRepository.delete(newsClassifiCation);
	}


	@Override
	public List<NewsClassifiCation> getNewsClassifiCationByBrandIdAndId(String brandId, long[] id) {
		em.clear();
		List<NewsClassifiCation> result = newsClassifiCationRepository.getNewsClassifiCationByBrandIdAndId(brandId, id);
		return result;
	}
	
	@Override
	public List<News> getNewsByBrandIdAndClassifiCationAndPage(String brandId, String classifiCation) {
		em.clear();
		List<News> result = newsRepository.getNewsByBrandIdAndClassifiCation(brandId, classifiCation);
		return result;
	}
	
}
