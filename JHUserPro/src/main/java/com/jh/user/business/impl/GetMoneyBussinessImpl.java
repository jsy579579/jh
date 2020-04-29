package com.jh.user.business.impl;

import java.util.List;

import javax.persistence.EntityManager;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jh.user.business.BranchbankBussiness;
import com.jh.user.business.GetMoneyBusiness;
import com.jh.user.pojo.Branchbank;
import com.jh.user.pojo.GetMoney;
import com.jh.user.repository.BranchbankRepository;
import com.jh.user.repository.GetMoneyRepository;

@Service
public class GetMoneyBussinessImpl implements GetMoneyBusiness{

	@Autowired
	private GetMoneyRepository getMoneyRepository;
	
	@Autowired
	private EntityManager em;
	
	@Transactional
	@Override
	public GetMoney createGetMoney(GetMoney getMoney) {
		GetMoney result = getMoneyRepository.save(getMoney);
		em.flush();
		return result;
	}

	@Override
	public List<GetMoney> getGetMoneyByBrandId(String brandId) {
		em.clear();
		List<GetMoney> result = getMoneyRepository.getGetMoneyByBrandId(brandId);
		return result;
	}

	@Override
	public Page<GetMoney> getGetMoneyByBrandIdAndPage(String brandId, Pageable pageAble) {
		Page<GetMoney> result = getMoneyRepository.getGetMoneyByBrandIdAndPage(brandId, pageAble);
		em.clear();
		return result;
	}

	@Override
	public List<GetMoney> getGetMoneyByBrandIdAndId(String brandId, long[] Id) {
		em.clear();
		List<GetMoney> result = getMoneyRepository.getGetMoneyByBrandIdAndId(brandId, Id);
		return result;
	}

	@Transactional
	@Override
	public void deleteGetMoneyByBrandIdAndId(GetMoney getMoney) {
		getMoneyRepository.delete(getMoney);
	}
	
	

}
