package com.jh.user.repository;

import com.jh.user.pojo.TUpgradeOrderViewEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TUpgradeOrderViewRepository extends JpaRepository<TUpgradeOrderViewEntity, Long>, JpaSpecificationExecutor<TUpgradeOrderViewEntity> {

    @Query(value = "select t from TUpgradeOrderViewEntity t where t.brandId = :brandid")
    List<TUpgradeOrderViewEntity> queryBrandId(@Param(value = "brandid") long brandid);
}
