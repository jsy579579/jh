package com.jh.paymentgateway.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.jh.paymentgateway.pojo.BqxCode;
import com.jh.paymentgateway.pojo.HZHKCode;

@Repository
public interface HZHKCodeRepository  extends JpaRepository<BqxCode, Long>,JpaSpecificationExecutor<BqxCode>{

	@Query("select hzhk from HZHKCode hzhk where hzhk.provinceId=000000 and hzhk.grade=1")
	public List<HZHKCode> gethzhkCodeRepository();
	
	@Query("select hzhk from HZHKCode hzhk where hzhk.provinceId=:provinceId")
	public List<HZHKCode> findhzhkCodeCity(@Param("provinceId") String provinceId);
}
