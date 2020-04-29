package com.jh.paymentgateway.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.jh.paymentgateway.pojo.JFRegister;
import com.jh.paymentgateway.pojo.JFSRegister;

@Repository
public interface JFSRegisterRepository extends JpaRepository<JFSRegister, String>, JpaSpecificationExecutor<JFSRegister> {
	@Query("select jfs from JFSRegister jfs where jfs.idCard=:idCard")
	public JFSRegister getJFSRegisterByIdCard(@Param("idCard") String idCard);

}
