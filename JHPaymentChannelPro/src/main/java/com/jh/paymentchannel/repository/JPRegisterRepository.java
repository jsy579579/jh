package com.jh.paymentchannel.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.jh.paymentchannel.pojo.JPRegister;





@Repository
public interface JPRegisterRepository extends JpaRepository<JPRegister, String>, JpaSpecificationExecutor<JPRegister>{
	
	@Query("select cj from JPRegister cj where cj.idCard=:idCard")
	public JPRegister getJPRegisterByIdCard(@Param("idCard") String idCard);
	
}
