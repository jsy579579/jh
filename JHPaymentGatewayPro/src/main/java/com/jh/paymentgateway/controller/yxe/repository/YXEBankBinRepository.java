package com.jh.paymentgateway.controller.yxe.repository;

import com.jh.paymentgateway.controller.yxe.pojo.YXEBankBin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface YXEBankBinRepository extends JpaRepository<YXEBankBin,Long>, JpaSpecificationExecutor<YXEBankBin> {

}
