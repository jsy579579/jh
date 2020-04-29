package com.jh.paymentgateway.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.jh.paymentgateway.pojo.MHHQHRegister;


public interface MHHQHRegisterRepository extends JpaRepository<MHHQHRegister, String>,JpaSpecificationExecutor<MHHQHRegister>{

	MHHQHRegister getMHHQHRegisterByIdCard(String idCard);
	

}
