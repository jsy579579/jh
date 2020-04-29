package com.jh.user.business.impl;

import java.util.List;

import javax.persistence.EntityManager;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jh.user.business.SignInBusiness;
import com.jh.user.pojo.SignCalc;
import com.jh.user.pojo.SignCoin;
import com.jh.user.pojo.SignCommonCoin;
import com.jh.user.pojo.SignDetail;
import com.jh.user.repository.SignCalcRepository;
import com.jh.user.repository.SignCoinRepository;
import com.jh.user.repository.SignCommonCoinRepository;
import com.jh.user.repository.SignDetailRepository;

@Service
public class SignInBusinessImpl implements SignInBusiness {

	
	@Autowired
	private EntityManager em;

	@Autowired
	private SignDetailRepository signDetailRepository;
	
	@Autowired
	private SignCalcRepository signCalcRepository;
	
	@Autowired
	private SignCoinRepository signCoinRepository;
	
	@Autowired
	private SignCommonCoinRepository signCommonCoinRepository;
	
	@Transactional
	@Override
	public SignDetail createSignDetail(SignDetail signDetail) {
		SignDetail result = signDetailRepository.save(signDetail);
		em.flush();
		return result;
	}

	
	@Override
	public SignDetail getSignDetailByUserIdAndDate(String userId, String date) {
		em.clear();
		SignDetail result = signDetailRepository.getSignDetailByUserIdAndDate(userId, date);
		return result;
	}


	@Override
	public List<SignDetail> getSignDetailByUserIdAndStartTimeAndEndTime(String userId, String startTime,
			String endTime) {
		em.clear();
		List<SignDetail> result = signDetailRepository.getSignDetailByUserIdAndStartTimeAndEndTime(userId, startTime, endTime);
		return result;
	}


	@Transactional
	@Override
	public SignCalc createSignCalc(SignCalc signCalc) {
		SignCalc result = signCalcRepository.save(signCalc);
		em.flush();
		return result;
	}


	@Override
	public SignCalc getSignCalcByUserId(String userId) {
		em.clear();
		SignCalc result = signCalcRepository.getSignCalcByUserId(userId);
		return result;
	}


	@Override
	public int getSignCoin(String brandId, String grade, int continueDays) {
		em.clear();
		int result = signCoinRepository.getSignCoin(brandId, grade, continueDays);
		return result;
	}


	@Override
	public List<SignCoin> getSignCoinByBrandIdAndGrade(String brandId, String grade) {
		em.clear();
		List<SignCoin> result = signCoinRepository.getSignCoinByBrandIdAndGrade(brandId, grade);
		return result;
	}


	@Override
	public List<String> getSignDateByUserIdAndStartTimeAndEndTime(String userId, String startTime, String endTime) {
		em.clear();
		List<String> result = signDetailRepository.getSignDateByUserIdAndStartTimeAndEndTime(userId, startTime, endTime);
		return result;
	}


	@Override
	public List<SignCalc> getSignCalcAll() {
		em.clear();
		List<SignCalc> result = signCalcRepository.getSignCalcAll();
		return result;
	}


	@Transactional
	@Override
	public SignCoin createSignCoin(SignCoin signCoin) {
		SignCoin result = signCoinRepository.save(signCoin);
		em.flush();
		return result;
	}


	@Override
	public List<SignCoin> getSignCoinByBrandId(String brandId) {
		em.clear();
		List<SignCoin> result = signCoinRepository.getSignCoinByBrandId(brandId);
		return result;
	}


	@Override
	public List<SignCoin> getSignCoinByBrandIdAndId(String brandId, long[] id) {
		em.clear();
		List<SignCoin> result = signCoinRepository.getSignCoinByBrandIdAndId(brandId, id);
		return result;
	}


	@Transactional
	@Override
	public void deleteSignCoin(SignCoin signCoin) {
		signCoinRepository.delete(signCoin);
	}


	@Override
	public SignCoin getSignCoinByBrandIdAndGradeAndContinueDays(String brandId, String grade, int continueDays) {
		em.clear();
		SignCoin result = signCoinRepository.getSignCoinByBrandIdAndGradeAndContinueDays(brandId, grade, continueDays);
		return result;
	}

	@Transactional
	@Override
	public SignCommonCoin createSignCommonCoin(SignCommonCoin signCommonCoin) {
		SignCommonCoin result = signCommonCoinRepository.save(signCommonCoin);
		em.flush();
		em.clear();
		return result;
	}


	@Override
	public SignCommonCoin getSignCommonCoinByBrandIdAndGrade(String brandId, String grade) {
		em.clear();
		SignCommonCoin result = signCommonCoinRepository.getSignCommonCoinByBrandIdAndGrade(brandId, grade);
		return result;
	}


	@Override
	public SignCommonCoin getSignCommonCoinByBrandIdAndGradeaAndCoin(String brandId, String grade, String bonusCoin) {
		em.clear();
		SignCommonCoin result = signCommonCoinRepository.getSignCommonCoinByBrandIdAndGradeAndCoin(brandId, grade, bonusCoin);
		return result;
	}


	@Override
	public List<SignCommonCoin> getSignCommonCoinByBrandId(String brandId) {
		em.clear();
		List<SignCommonCoin> result = signCommonCoinRepository.getSignCommonCoinByBrandId(brandId);
		return result;
	}


	@Override
	public List<SignCommonCoin> getSignCommonCoinByBrandIdAndId(String brandId, long[] id) {
		em.clear();
		List<SignCommonCoin> result = signCommonCoinRepository.getSignCommonCoinByBrandIdAndId(brandId, id);
		return result;
	}


	@Transactional
	@Override
	public void deleteSignCommonCoin(SignCommonCoin signCommonCoin) {
		signCommonCoinRepository.delete(signCommonCoin);
	}
	
	
}
