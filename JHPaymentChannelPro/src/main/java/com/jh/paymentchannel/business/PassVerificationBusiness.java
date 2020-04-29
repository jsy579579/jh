package com.jh.paymentchannel.business;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.jh.paymentchannel.pojo.PassVerification;

public interface PassVerificationBusiness {
	/**
	 * 保存PassVerification
	 * @param model
	 * @return
	 */
	public PassVerification save(PassVerification model);
	
    /**根据user_id查询所有*/
    public PassVerification findPassByUserId(String userId);
    
	public PassVerification findByPasskeyAndStatusAndBrandId(String key,String status,String brandId);

	public PassVerification findPassByUserIdAndBrandId(String trim, String trim2);

	public Integer findLastBatchNoByBrandId(String brandId);

	public Page<PassVerification> findByBrandIdAndStatus(String brandId, String string,Pageable pageable );

	public int queryCountByBrandId(String brandId);

	public void setDependence(Long dependenceUserId, String dependencePhone, String dependenceName, int batchNo,String brandId);

	public Page<PassVerification> queryByDependence(String brandId,Pageable pageable);

	public Page<PassVerification> queryByDependenced(String brandId,Pageable pageable);

	public Page<PassVerification> queryAllByDependenced(String brandId,Pageable pageable);

	public Page<PassVerification> queryByDependenceUserIdAndBrandId(long userId, String brandId, String status,
			Pageable pageable);

	public Page<PassVerification> queryByDependenceUserIdAndBrandId(long userId, String string, Pageable pageable);

	public int queryCountByBatchNoAndBrandIdAndStatus(Integer batchNo, String brandId);

	public int queryActiveCountByBatchNo(Integer batchNo, String brandId, String string);

	public Page<PassVerification> findByBrandId(String brandId, Pageable pageable);

	/***
	 * 获取归属人拥有条数
	 * ***/
	public Integer findCountByBrandId(String brandId, long dependenceUserId ,String status ,String batch_no );
	
	/***
	 * 重新归属人
	 * ***/
	public Integer updateCountByBrandId(String userid, long dependenceUserId ,String dependencePhone,String dependenceName ,int number);

	public PassVerification findByPasskey(String passkey);
   
}
