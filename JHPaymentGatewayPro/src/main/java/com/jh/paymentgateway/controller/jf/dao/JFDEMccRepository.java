package com.jh.paymentgateway.controller.jf.dao;


import com.jh.paymentgateway.controller.jf.pojo.JFDEMcc;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JFDEMccRepository extends JpaRepository<JFDEMcc,Long>, JpaSpecificationExecutor<JFDEMcc> {

    @Query("SELECT me from JFDEMcc me")
    public List<JFDEMcc> getAllMcc();

}
