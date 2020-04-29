package com.jh.paymentgateway.repository;

import com.jh.paymentgateway.pojo.YCACCount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface YCAccountRepository extends JpaRepository<YCACCount, Long>,JpaSpecificationExecutor<YCACCount>{

    @Query("select yca from YCACCount yca where yca.idCard =?1")
    YCACCount findByIdCard(@Param("idcard")String idcard);
}
