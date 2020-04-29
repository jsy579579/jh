package com.jh.channel.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.jh.channel.pojo.OutMerchantSecurityKey;

@Repository
public interface OutMerchantSecurityKeyRepository extends JpaRepository<OutMerchantSecurityKey,String>,JpaSpecificationExecutor<OutMerchantSecurityKey>{


	@Query("select securitykey from  OutMerchantSecurityKey securitykey where securitykey.phone=:phone")
	OutMerchantSecurityKey findOutSecurityKeyByMerno(@Param("phone") String phone);
	
	@Query("select securitykey from  OutMerchantSecurityKey securitykey where securitykey.id=:id")
	OutMerchantSecurityKey findOutSecurityKeyByMernoId(@Param("id") String merchantid);
	
	@Query("select securitykey from  OutMerchantSecurityKey securitykey where securitykey.userId=:Uid")
	OutMerchantSecurityKey findOutMerchantSecurityKeyByUid(@Param("Uid") long userId);
}
