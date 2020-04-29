package com.jh.paymentchannel.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import com.jh.paymentchannel.pojo.BrandCardAuthCount;

@Repository
public interface BrandCardAuthCountRepository extends JpaRepository<BrandCardAuthCount, Long>,JpaSpecificationExecutor<BrandCardAuthCount> {

	BrandCardAuthCount findByBrandId(String brandId);

}
