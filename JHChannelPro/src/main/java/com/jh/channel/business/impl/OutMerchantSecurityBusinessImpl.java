package com.jh.channel.business.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.jh.channel.business.OutMerchantSecurityBusiness;
import com.jh.channel.pojo.OutMerchantSecurityKey;
import com.jh.channel.repository.OutMerchantSecurityKeyRepository;


@Service
public class OutMerchantSecurityBusinessImpl  implements OutMerchantSecurityBusiness{

	@Autowired
	private OutMerchantSecurityKeyRepository repository;
	
	
	@Override
	public OutMerchantSecurityKey getOutMerchantSecurityKey(String merchantcode) {
		// TODO Auto-generated method stub
		return repository.findOutSecurityKeyByMerno(merchantcode);
	}

	
	@Override
	public  OutMerchantSecurityKey  getOutMerchantSecurityKeyid(String merchantid) {
		
		return repository.findOutSecurityKeyByMernoId(merchantid);
	}


	@Override
	public OutMerchantSecurityKey addOutMerchantSecurityKey(OutMerchantSecurityKey outMerchantSecurityKey) {
		
		return repository.save(outMerchantSecurityKey);
	}


	@Override
	public OutMerchantSecurityKey findOutMerchantSecurityKeyByUid(long uid) {
		
		
		return repository.findOutMerchantSecurityKeyByUid(uid);
	}
}
