package com.jh.paymentgateway.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.jh.paymentgateway.pojo.FFZCRegister;
import com.jh.paymentgateway.pojo.TYTRegister;

@Repository
public interface TYTRegisterRepository extends JpaRepository<TYTRegister, String>, JpaSpecificationExecutor<TYTRegister> {

	@Query("select tyt from TYTRegister tyt where tyt.idCard=:idCard")
	public TYTRegister getTYTRegisterByIdCard(@Param("idCard") String idCard);

}
