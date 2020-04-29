package com.jh.paymentgateway.controller.tldhx.dao;


import com.jh.paymentgateway.controller.tldhx.pojo.TLDHXMcc;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TLDHXMccRepository extends JpaRepository<TLDHXMcc,Long>, JpaSpecificationExecutor<TLDHXMcc> {

    @Query("SELECT me from TLDHXMcc me")
    public List<TLDHXMcc> getAllMcc();

}
