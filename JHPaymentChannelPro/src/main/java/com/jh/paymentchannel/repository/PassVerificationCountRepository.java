package com.jh.paymentchannel.repository;



import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import com.jh.paymentchannel.pojo.PassVerificationCount;
@Repository
public interface PassVerificationCountRepository extends JpaRepository<PassVerificationCount,Integer>,JpaSpecificationExecutor<PassVerificationCount>{

	PassVerificationCount findByUserId(long userId);
	
	List<PassVerificationCount> findBybrandId(long brandId);
}
