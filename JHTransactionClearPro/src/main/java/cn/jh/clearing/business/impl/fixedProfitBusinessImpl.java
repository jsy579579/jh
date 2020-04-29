package cn.jh.clearing.business.impl;

import java.util.List;

import javax.persistence.EntityManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cn.jh.clearing.business.fixedProfitBusiness;
import cn.jh.clearing.pojo.fixedProfit;
import cn.jh.clearing.repository.fixedProfitRepository;
import cn.jh.clearing.service.ProfitService;
import cn.jh.clearing.util.Util;

@Service
public class fixedProfitBusinessImpl implements fixedProfitBusiness{
	
	private static final Logger LOG = LoggerFactory.getLogger(fixedProfitBusinessImpl.class);

	@Autowired
	private fixedProfitRepository fixedProfitRepository;
	
	@Autowired
	private EntityManager em;
	
	@Autowired
	Util util;

	@Override
	public fixedProfit getfixedProfitByBrandIdAndGrade(String brandId, long grade) {
		em.clear();
		fixedProfit result = fixedProfitRepository.getfixedProfitByBrandIdAndGrade(brandId, grade);
		return result;
	}

	@Override
	public List<fixedProfit> getfixedProfitByBrandId(String brandId) {
		em.clear();
		List<fixedProfit> result = fixedProfitRepository.getfixedProfitByBrandId(brandId);
		return result;
	}

	@Transactional
	@Override
	public void createfixedProfit(fixedProfit fixedProfit) {
		fixedProfitRepository.save(fixedProfit);
		em.flush();
	}

	@Override
	public void deletefixedProfit(fixedProfit fixedProfit) {
		fixedProfitRepository.delete(fixedProfit);
	}

	@Override
	public fixedProfit getfixedProfitByBrandIdAndId(String brandId, long id) {
		em.clear();
		fixedProfit result = fixedProfitRepository.getfixedProfitByBrandIdAndId(brandId, id);
		return result;
	}
	
	
}
