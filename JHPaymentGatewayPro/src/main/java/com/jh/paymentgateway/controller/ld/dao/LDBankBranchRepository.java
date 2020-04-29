package com.jh.paymentgateway.controller.ld.dao;

import com.jh.paymentgateway.controller.ld.pojo.LDBankBranch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface LDBankBranchRepository extends JpaRepository<LDBankBranch, Long>, JpaSpecificationExecutor<LDBankBranch> {

    @Query("SELECT qy from LDBankBranch qy where qy.bankbranchName like %:bankname%")
    LDBankBranch findByDebitCardName(@Param("bankname") String bankname);
}
