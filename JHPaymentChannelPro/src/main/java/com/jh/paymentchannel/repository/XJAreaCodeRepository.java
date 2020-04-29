package com.jh.paymentchannel.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.jh.paymentchannel.pojo.XJAreaCode;


@Repository
public interface XJAreaCodeRepository extends JpaRepository<XJAreaCode, String>, JpaSpecificationExecutor<XJAreaCode>{
	
	
	@Query(" select xj.areaCode from XJAreaCode xj where xj.areaName = :areaName and xj.areaLevel = :areaLevel")
	public String getXJAreaCodeByName(@Param("areaName") String areaName, @Param("areaLevel") String areaLevel);
	
}
