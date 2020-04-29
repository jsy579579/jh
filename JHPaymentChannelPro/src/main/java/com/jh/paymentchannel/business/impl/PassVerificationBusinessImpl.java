package com.jh.paymentchannel.business.impl;


import javax.persistence.EntityManager;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jh.paymentchannel.business.PassVerificationBusiness;
import com.jh.paymentchannel.pojo.PassVerification;
import com.jh.paymentchannel.repository.PassRepository;
@Service
public class PassVerificationBusinessImpl implements PassVerificationBusiness{
	@Autowired
	PassRepository passRepository;
	
	@Autowired
	private EntityManager em;
	
	@Autowired
	private JdbcTemplate jdbcTemplate;
	
	@Transactional
	@Override
	public PassVerification save(PassVerification model) {
		PassVerification pvf = passRepository.save(model);
		em.flush();
		em.clear();
		return pvf;
	}

    
	@Override
	public PassVerification findPassByUserId(String userid) {
		return passRepository.findByUserId(userid);
		
	}

	@Override
	public PassVerification findByPasskeyAndStatusAndBrandId(String key,String status,String brandId) {
		return passRepository.findByPasskeyAndStatusAndBrandId(key,status,brandId);
	}


	@Override
	public PassVerification findPassByUserIdAndBrandId(String userId, String brandId) {
		return  passRepository.findByUserIdAndBrandId(userId,brandId);
	}


	@Override
	public Integer findLastBatchNoByBrandId(String brandId) {
		return passRepository.findLastBatchNoByBrandId(brandId);
	}


	@Override
	public Page<PassVerification> findByBrandIdAndStatus(String brandId, String string,Pageable pageable ) {
		return passRepository.findByBrandIdAndStatus(brandId,string,pageable);
	}


	@Override
	public int queryCountByBrandId(String brandId) {
		return passRepository.queryCountByBrandId(brandId);
	}

	@Transactional
	@Override
	public void setDependence(Long dependenceUserId, String dependencePhone, String dependenceName, int batchNo,String brandId) {
		passRepository.setDependence(dependenceUserId,dependencePhone,dependenceName,batchNo,brandId);
	}


	@Override
	public Page<PassVerification> queryByDependence(String brandId,Pageable pageable) {
		return passRepository.queryByDependence(brandId,pageable);
	}


	@Override
	public Page<PassVerification> queryByDependenced(String brandId,Pageable pageable) {
		return passRepository.queryByDependenced(brandId,pageable);
	}


	@Override
	public Page<PassVerification> queryAllByDependenced(String brandId,Pageable pageable) {
		return passRepository.queryAllByDependenced(brandId,pageable);
	}


	@Override
	public Page<PassVerification> queryByDependenceUserIdAndBrandId(long userId, String brandId, String status,
			Pageable pageable) {
		return passRepository.queryByDependenceUserIdAndBrandId(userId,brandId,status,pageable);
	}


	@Override
	public Page<PassVerification> queryByDependenceUserIdAndBrandId(long userId, String brandId, Pageable pageable) {
		return passRepository.queryByDependenceUserIdAndBrandId(userId,brandId,pageable);
	}


	@Override
	public int queryCountByBatchNoAndBrandIdAndStatus(Integer batchNo, String brandId) {
		return passRepository.queryCountByBatchNoAndBrandIdAndStatus(batchNo,brandId);
	}


	@Override
	public int queryActiveCountByBatchNo(Integer batchNo, String brandId, String string) {
		return passRepository.queryActiveCountByBatchNo(batchNo, brandId, string);
	}


	@Override
	public Page<PassVerification> findByBrandId(String brandId, Pageable pageable) {
		return passRepository.findByBrandId(brandId, pageable);
	}

	@Override
	public Integer findCountByBrandId(String brandId, long dependenceUserId ,String status,String batch_no ) {
		StringBuffer sqlCount= new StringBuffer("select count(*) as count from t_pass_verification pv  where 1=1");
		if(brandId!=null&&brandId.trim().length()>0) {
			sqlCount.append(" and pv.brand_id="+brandId);
		}
		if(dependenceUserId>0) {
			sqlCount.append(" and pv.dependence_user_id="+dependenceUserId);
		}
		
		if(status!=null&&status.trim().length()>0) {
			sqlCount.append(" and pv.`status`="+status);
		}
		
		if(batch_no!=null&&batch_no.trim().length()>0) {
			sqlCount.append(" and pv.batch_no="+batch_no);
		}
		
		int count=Integer.parseInt(jdbcTemplate.queryForMap(sqlCount.toString()).get("count").toString()) ;
		
		return count;
	}
	
	@Transactional
	@Override
	public Integer updateCountByBrandId(String userid, long dependenceUserId ,String dependencePhone,String dependenceName ,int number) {
		
		StringBuffer updateSql= new StringBuffer("update  t_pass_verification set dependence_user_name='"+dependenceName
													+"' , dependence_user_phone='"+dependencePhone
													+"' , dependence_user_id="+dependenceUserId
													+"  where dependence_user_id="+userid
													+" and `status`=0  limit "+ number);
		int count=jdbcTemplate.update(updateSql.toString()); ;
		return count;
	}


	@Override
	public PassVerification findByPasskey(String passkey) {
		return passRepository.findByPasskey(passkey);
	}

}
