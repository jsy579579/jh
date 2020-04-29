package com.jh.paymentgateway.controller.tldhx.dao;

import com.jh.paymentgateway.controller.tldhx.pojo.CheckedBank;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface CheckedBankRepsository extends JpaRepository<CheckedBank,Long>, JpaSpecificationExecutor<CheckedBank> {

}
