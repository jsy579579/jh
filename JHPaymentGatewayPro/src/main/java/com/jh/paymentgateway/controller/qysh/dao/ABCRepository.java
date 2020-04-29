package com.jh.paymentgateway.controller.qysh.dao;


import com.jh.paymentgateway.controller.qysh.pojo.ABC;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ABCRepository extends JpaRepository<ABC,Long> , JpaSpecificationExecutor<ABC> {

    @Query("select me from ABC me")
    public List<ABC> getAll();


}
