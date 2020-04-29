package com.jh.paymentgateway.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.jh.paymentgateway.pojo.KQRegister;

@Repository
public interface KQRegisterRepository extends JpaRepository<KQRegister, String>, JpaSpecificationExecutor<KQRegister> {
	@Query("select kq from KQRegister kq where kq.idCard=:idCard")
	public KQRegister getKQRegisterByIdCard(@Param("idCard") String idCard);

}
