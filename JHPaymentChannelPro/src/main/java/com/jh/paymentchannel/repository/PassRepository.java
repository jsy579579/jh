package com.jh.paymentchannel.repository;


import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.jh.paymentchannel.pojo.PassVerification;
@Repository
public interface PassRepository extends JpaRepository<PassVerification,Integer>,JpaSpecificationExecutor<PassVerification>{

	PassVerification findByUserId(String userId);

	PassVerification findByPasskeyAndStatusAndBrandId(String key,String status,String brandId);

	PassVerification findByUserIdAndBrandId(String userId, String brandId);
	@Query("select max(pv.batchNo) from PassVerification pv where pv.brandId =:brandId")
	Integer findLastBatchNoByBrandId(@Param("brandId")String brandId);

	Page<PassVerification> findByBrandIdAndStatus(String brandId, String string,Pageable pageable );
	
	@Query("select count(*) from PassVerification pv where pv.brandId=:brandId and pv.status='0'")
	int queryCountByBrandId(@Param("brandId")String brandId);
	
	@Modifying
	@Query("update PassVerification pv set pv.dependenceUserId=:dependenceUserId,pv.dependencePhone=:dependencePhone,pv.dependenceName=:dependenceName where pv.batchNo=:batchNo and pv.brandId =:brandId")
	void setDependence(@Param("dependenceUserId")Long dependenceUserId,@Param("dependencePhone") String dependencePhone, @Param("dependenceName")String dependenceName,@Param("batchNo") int batchNo ,@Param("brandId")String brandId);
	
	@Query("select pv.batchNo,pv.dependenceName,pv.dependencePhone from PassVerification pv where pv.brandId=:brandId and pv.dependencePhone = 0 group by pv.batchNo")
	Page<PassVerification> queryByDependence(@Param("brandId")String brandId,Pageable pageable);
	
	@Query("select pv.batchNo,pv.dependenceName,pv.dependencePhone from PassVerification pv where pv.brandId=:brandId and pv.dependencePhone != 0 group by pv.batchNo")
	Page<PassVerification> queryByDependenced(@Param("brandId")String brandId,Pageable pageable);
	
	@Query("select pv.batchNo,pv.dependenceName,pv.dependencePhone from PassVerification pv where pv.brandId=:brandId group by pv.batchNo")
	Page<PassVerification> queryAllByDependenced(@Param("brandId")String brandId,Pageable pageable);
	
	@Query("select pv from PassVerification pv where pv.dependenceUserId=:userId and pv.brandId=:brandId and pv.status=:status")
	Page<PassVerification> queryByDependenceUserIdAndBrandId(@Param("userId")long userId, @Param("brandId")String brandId, @Param("status")String status, Pageable pageable);
	
	@Query("select pv from PassVerification pv where pv.dependenceUserId=:userId and pv.brandId=:brandId")
	Page<PassVerification> queryByDependenceUserIdAndBrandId(@Param("userId")long userId,@Param("brandId")String brandId, Pageable pageable);

	@Query("select count(*) from PassVerification pv where pv.brandId=:brandId and pv.batchNo =:batchNo")
	int queryCountByBatchNoAndBrandIdAndStatus(@Param("batchNo")Integer batchNo,@Param("brandId")String brandId);

	@Query("select count(*) from PassVerification pv where pv.brandId=:brandId and pv.batchNo =:batchNo and status=:status")
	int queryActiveCountByBatchNo(@Param("batchNo")Integer batchNo,@Param("brandId")String brandId,@Param("status")String status);

	@Query("select count(*) from PassVerification pv where pv.brandId=:brandId and pv.dependenceUserId =:dependenceUserId and status=:status")
	int queryActiveCountByUserId(@Param("dependenceUserId")Long dependenceUserId , @Param("brandId")String brandId , @Param("status")String status);

	
	Page<PassVerification> findByBrandId(String brandId, Pageable pageable);

	PassVerification findByPasskey(String passkey);
	
}
