package com.jh.paymentgateway.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.jh.paymentgateway.pojo.LDRegister;

@Repository
public interface LDRegisterRepository extends JpaRepository<LDRegister, String>, JpaSpecificationExecutor<LDRegister>{
	
	/*@Query("select ld from LDRegister ld where ld.idCard=:idCard")
	public LDRegister getLDRegisterByIdCard(@Param("idCard") String idCard);*/
	
	@Query(value = "select ld.* from t_ld_register ld where ld.idCard=:idCard", nativeQuery = true)
	public LDRegister getLDRegisterByIdCard(@Param("idCard") String idCard);

	@Query("select ld.bankCard from LDRegister ld where ld.phone=:phone")
	public String getLDRegisterByPhone(@Param("phone") String phone);
	
}
