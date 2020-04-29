package com.jh.paymentgateway.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import com.jh.paymentgateway.pojo.XSBindCard;

@Repository
public interface XSBindCardRepository extends JpaRepository<XSBindCard, Long>,JpaSpecificationExecutor<XSBindCard>{

	XSBindCard findByCardNo(String bankCard);

}
