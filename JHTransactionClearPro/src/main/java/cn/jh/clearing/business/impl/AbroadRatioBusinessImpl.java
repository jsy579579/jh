package cn.jh.clearing.business.impl;

import javax.persistence.EntityManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cn.jh.clearing.business.AbroadRatioBusiness;
import cn.jh.clearing.pojo.AbroadRatio;
import cn.jh.clearing.repository.AbroadRatioRepository;

@Service
public class AbroadRatioBusinessImpl implements AbroadRatioBusiness {

	private static final Logger LOG = LoggerFactory.getLogger(AbroadRatioBusinessImpl.class);
	
	@Autowired
	private EntityManager em;
	
	@Autowired
	private AbroadRatioRepository abroadRatioRepository;
	
	@Override
	public AbroadRatio getAbroadRatioByBrandIdAndGrade(int brandId, int grade) {
		em.clear();
		AbroadRatio result = abroadRatioRepository.getAbroadRatioByBrandIdAndGrade(brandId, grade);
		return result;
	}

	@Transactional
	@Override
	public void createAbroadRatio(AbroadRatio abroadRatio) {
		abroadRatioRepository.saveAndFlush(abroadRatio);
	}

}
