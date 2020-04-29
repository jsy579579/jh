package com.jh.paymentgateway.controller.qysh.dao;

import com.jh.paymentgateway.controller.qysh.pojo.QYSHBankBranch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface QYSHBankBranchRepository extends JpaRepository<QYSHBankBranch, Long>, JpaSpecificationExecutor<QYSHBankBranch> {

    @Query("SELECT qy from QYSHBankBranch qy where qy.bankbranchName like %:bankname%")
    QYSHBankBranch getBankBranchNo(@Param("bankname") String bankname);
}
