package com.jh.paymentgateway.controller.hc.dao;

import com.jh.paymentgateway.controller.hc.pojo.HCDEBankBranch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface HCDEBankBranchRepository extends JpaRepository<HCDEBankBranch, Long>, JpaSpecificationExecutor<HCDEBankBranch> {

    @Query("SELECT qy from HCDEBankBranch qy where qy.bankbranchName like %:bankname%")
    HCDEBankBranch getBankBranchNo(@Param("bankname") String bankname);
}
