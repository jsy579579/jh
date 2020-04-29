package com.jh.paymentgateway.controller.yxe.repository;


import com.jh.paymentgateway.controller.yxe.pojo.YXEAddress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface YXEAddressRepository extends JpaRepository<YXEAddress,Long>, JpaSpecificationExecutor<YXEAddress> {

}
