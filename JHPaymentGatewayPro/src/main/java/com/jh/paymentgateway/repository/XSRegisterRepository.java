package com.jh.paymentgateway.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import com.jh.paymentgateway.pojo.XSRegister;

@Repository
public interface XSRegisterRepository extends JpaRepository<XSRegister, Long>,JpaSpecificationExecutor<XSRegister>{

	XSRegister findByIdCard(String idCard);

}
