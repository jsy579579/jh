package com.jh.paymentgateway.repository;

import com.jh.paymentgateway.pojo.XTRegister;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface XTRegisterRepository extends JpaRepository<XTRegister, String>, JpaSpecificationExecutor<XTRegister> {
	@Query("select xt from XTRegister xt where xt.idCard=:idCard")
	public XTRegister getXTRegisterByIdCard(@Param("idCard") String idCard);

}
