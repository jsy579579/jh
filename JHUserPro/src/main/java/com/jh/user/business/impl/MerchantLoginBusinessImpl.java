package com.jh.user.business.impl;

import java.util.List;

import javax.persistence.EntityManager;
import javax.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.jh.user.business.MerchantLoginBusiness;
import com.jh.user.pojo.Merchant;
import com.jh.user.pojo.User;
import com.jh.user.repository.MerchantLoginRepository;
import com.jh.user.service.UserLoginRegisterService;
import com.jh.user.util.Util;
@Service
public class MerchantLoginBusinessImpl implements MerchantLoginBusiness{
	private static final Logger LOG = LoggerFactory.getLogger(MerchantLoginBusinessImpl.class);
	@Autowired
	private MerchantLoginRepository merchantLoginRepository;
	
	@Autowired
	Util util;
	
	@Autowired
	private EntityManager em;
	
	@Override
	public Merchant isLoginMerchant(String preMchId, String password) {

		return merchantLoginRepository.findMerchantByMchIdAndPassword(preMchId, password);
	}

	@Transactional
	@Override
	public Merchant saveMerchant(Merchant merchant) {
		return merchantLoginRepository.save(merchant);
	}


//	@Override
//	public List<Merchant> findMchIdByAll() {
//		return merchantLoginRepository.findMchIdByAll();
//	}
	
	

	@Override
	public Merchant findMchIdByPreMchid(String preMchId) {
		return merchantLoginRepository.findMchIdByPreMchid(preMchId);
	}

	@Override
	public List<Merchant> findAllMerchant() {
		return merchantLoginRepository.findAllMerchant();
	}

   /**通过商户号查询到商户*/
	@Override
	public Merchant findMerchantByMchId(String preMchId) {
		return merchantLoginRepository.queryMerchantByMchId(preMchId);
		
	}

	@Override
	public Merchant findAllByPreMchId(String preMchId) {
		return merchantLoginRepository.queryAllByPerMchId(preMchId);
    }

}
