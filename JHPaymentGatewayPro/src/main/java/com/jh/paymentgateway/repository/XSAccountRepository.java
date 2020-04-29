package com.jh.paymentgateway.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import com.jh.paymentgateway.pojo.XSAccount;

@Repository
public interface XSAccountRepository extends JpaRepository<XSAccount, Long>,JpaSpecificationExecutor<XSAccount>{

	XSAccount findByIdCard(String idcard);

}
