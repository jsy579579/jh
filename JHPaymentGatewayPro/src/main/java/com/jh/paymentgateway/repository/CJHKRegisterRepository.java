package com.jh.paymentgateway.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.jh.paymentgateway.pojo.CJHKRegister;


@Repository
public interface CJHKRegisterRepository extends JpaRepository<CJHKRegister, String>, JpaSpecificationExecutor<CJHKRegister>{
	
	@Query("select cjhk from CJHKRegister cjhk where cjhk.idCard=:idCard")
	public CJHKRegister getCJHKRegisterByIdCard(@Param("idCard") String idCard);
	
	
}
