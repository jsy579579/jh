package com.jh.paymentgateway.repository;

import com.jh.paymentgateway.pojo.YCMerch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface YCMerchRepository extends JpaRepository<YCMerch, String>, JpaSpecificationExecutor<YCMerch> {
    @Query("select ycm from YCMerch ycm where ycm.area = ?1")
    List<YCMerch> getmerchbyordercode(@Param("city")String city);
}