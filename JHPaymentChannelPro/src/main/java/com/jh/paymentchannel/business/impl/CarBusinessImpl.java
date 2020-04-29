package com.jh.paymentchannel.business.impl;

import java.util.List;

import javax.persistence.EntityManager;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jh.paymentchannel.business.CarBusiness;
import com.jh.paymentchannel.pojo.CarQueryHistory;
import com.jh.paymentchannel.pojo.CarSupportProvince;
import com.jh.paymentchannel.pojo.Province;
import com.jh.paymentchannel.pojo.UserQueryCount;
import com.jh.paymentchannel.repository.CarQueryHistoryRepository;
import com.jh.paymentchannel.repository.CarSupportProvinceRepository;
import com.jh.paymentchannel.repository.CityRepository;
import com.jh.paymentchannel.repository.ProvinceRepository;
import com.jh.paymentchannel.repository.UserQueryCountRepository;

@Service
public class CarBusinessImpl implements CarBusiness {

	
	@Autowired
	private EntityManager em;

	@Autowired
	private CarSupportProvinceRepository carSupportProvinceRepository;
	
	@Autowired
	private ProvinceRepository provinceRepository;
	
	@Autowired
	private CityRepository cityRepository;
	
	@Autowired
	private UserQueryCountRepository userQueryCountRepository;
	
	@Autowired
	private CarQueryHistoryRepository carQueryHistoryRepository;
	
	@Override
	public CarSupportProvince getCarSupportProvinceByProvince(String province) {
		em.clear();
		CarSupportProvince result = carSupportProvinceRepository.getCarSupportProvinceByProvince(province);
		return result;
	}

	@Override
	public List<CarSupportProvince> getCarSupportProvince() {
		em.clear();
		List<CarSupportProvince> result = carSupportProvinceRepository.getCarSupportProvince();
		return result;
	}

	@Override
	public List<String> getProvince() {
		em.clear();
		List<String> result = provinceRepository.getProvince();
		return result;
	}

	@Override
	public Province getProvinceByProcince(String province) {
		em.clear();
		Province result = provinceRepository.getProvinceByProvince(province);
		return result;
	}

	@Override
	public List<String> getCityByProvinceId(String provinceId) {
		em.clear();
		List<String> result = cityRepository.getCityByProvinceId(provinceId);
		return result;
	}

	@Transactional
	@Override
	public UserQueryCount createUserQueryCount(UserQueryCount userQueryCount) {
		UserQueryCount result = userQueryCountRepository.save(userQueryCount);
		em.flush();
		return result;
	}

	@Override
	public UserQueryCount getUserQueryCountByUserId(String userId) {
		em.clear();
		UserQueryCount result = userQueryCountRepository.getUserQueryCountByUserId(userId);
		return result;
	}

	@Transactional
	@Override
	public CarQueryHistory createCarQueryHistory(CarQueryHistory carQueryHistory) {
		CarQueryHistory result = carQueryHistoryRepository.save(carQueryHistory);
		em.flush();
		return result;
	}

	@Override
	public List<CarQueryHistory> getCarQueryHistoryByUserId(String userId) {
		em.clear();
		List<CarQueryHistory> result = carQueryHistoryRepository.getCarQueryHistoryByUserId(userId);
		return result;
	}

	@Override
	public CarQueryHistory getCarQueryHistoryByUserIdAndId(String userId, long id) {
		em.clear();
		CarQueryHistory result = carQueryHistoryRepository.getCarQueryHistoryByUserIdAndId(userId, id);
		return result;
	}

	
	
	
}
