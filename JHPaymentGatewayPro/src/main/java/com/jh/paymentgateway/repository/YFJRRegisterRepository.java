package com.jh.paymentgateway.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.jh.paymentgateway.pojo.YFJRRegister;

@Repository
public interface YFJRRegisterRepository extends JpaRepository<YFJRRegister, Long>,JpaSpecificationExecutor<YFJRRegister>{

	@Query("select yf from YFJRRegister yf where yf.idCard=:idCard")
	public YFJRRegister getYFJRRegisterByIdNum(@Param("idCard") String idCard);
	
}
