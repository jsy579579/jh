package com.jh.paymentgateway.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.jh.paymentgateway.pojo.RHJFRegister;



@Repository
public interface RHJFRegisterRepository extends JpaRepository<RHJFRegister, String>, JpaSpecificationExecutor<RHJFRegister>{
	
	@Query("select rhjf from RHJFRegister rhjf where rhjf.idCard=:idCard")
	public RHJFRegister getRHJFRegisterByIdCard(@Param("idCard") String idCard);
	
	@Query("select rhjf from RHJFRegister rhjf where rhjf.merchantNo=:merchantNo")
	public RHJFRegister getRHJFRegisterByMerchantNo(String merchantNo);
		
}
