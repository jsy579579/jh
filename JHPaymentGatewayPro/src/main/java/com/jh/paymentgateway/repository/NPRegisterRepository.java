package com.jh.paymentgateway.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.jh.paymentgateway.pojo.NPRegister;


@Repository
public interface NPRegisterRepository extends JpaRepository<NPRegister, String>, JpaSpecificationExecutor<NPRegister>{
	
	@Query("select np from NPRegister np where np.idCard=:idCard")
	public NPRegister getNPRegisterByIdcard(@Param("idCard") String idCard);
	
}
