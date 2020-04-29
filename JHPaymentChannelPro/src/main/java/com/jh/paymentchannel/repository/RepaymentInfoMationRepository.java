package com.jh.paymentchannel.repository;


import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.jh.paymentchannel.pojo.RepaymentInfoMation;

@Repository
public interface RepaymentInfoMationRepository  extends JpaRepository<RepaymentInfoMation, String>,JpaSpecificationExecutor<RepaymentInfoMation>{

	@Query("select rim from RepaymentInfoMation rim")
	List<RepaymentInfoMation>  getRepaymentInfoMationAll();
	
}
