package com.jh.paymentchannel.business.impl;

import org.hibernate.validator.constraints.Email;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jh.paymentchannel.business.BrandManageBusiness;
import com.jh.paymentchannel.pojo.SwiftBrandMerchant;
import com.jh.paymentchannel.repository.SwiftBrandMerchantRepository;

@Service
public class BrandManageBusinessImpl implements BrandManageBusiness{

	
	@Autowired
	private SwiftBrandMerchantRepository swiftBrandMerchantRepository;

	@Override
	public SwiftBrandMerchant getSwiftBrandMerchant(String brandid) {
		return swiftBrandMerchantRepository.getSwiftBrandMerchant(brandid);
	}



	@Override
	public SwiftBrandMerchant getSwiftBrandMerchantByMchId(String subMerchantid) {
		return swiftBrandMerchantRepository.getSwiftBrandMerchantByMchId(subMerchantid);
	}


	@Transactional
	@Override
	public SwiftBrandMerchant createSwiftBrandMerchant(SwiftBrandMerchant swiftBrandMerchant) {
		return swiftBrandMerchantRepository.save(swiftBrandMerchant);
	}
	
	
}
