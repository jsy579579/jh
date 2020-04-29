package com.jh.paymentgateway.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.jh.paymentgateway.pojo.YBSRegister;

@Repository
public interface YBSRepository extends JpaRepository<YBSRegister, String>, JpaSpecificationExecutor<YBSRegister> {
	
	@Query("select ybs from YBSRegister ybs where ybs.idCard=:idCard")
	public YBSRegister getYBSRegisterByidCard(@Param("idCard") String idCard);

}
