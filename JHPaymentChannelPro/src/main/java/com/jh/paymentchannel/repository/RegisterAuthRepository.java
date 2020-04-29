package com.jh.paymentchannel.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.jh.paymentchannel.pojo.RegisterAuth;

@Repository
public interface RegisterAuthRepository extends JpaRepository<RegisterAuth, String>,JpaSpecificationExecutor<RegisterAuth>{
	
	@Query(" select ra from RegisterAuth ra where ra.mobile=:mobile")
	RegisterAuth queryByMobile(@Param("mobile") String mobile);
	
	@Query(" select ra from RegisterAuth ra where ra.idCard=:idCard")
	RegisterAuth getRegisterAuthByIdCard(@Param("idCard") String idCard);
	
}
