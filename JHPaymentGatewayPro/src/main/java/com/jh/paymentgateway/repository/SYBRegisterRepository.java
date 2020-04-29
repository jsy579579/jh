package com.jh.paymentgateway.repository;

import com.jh.paymentgateway.pojo.SYBRegister;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;


@Repository
public interface SYBRegisterRepository extends JpaRepository<SYBRegister, String>, JpaSpecificationExecutor<SYBRegister> {

	@Query("select syb from SYBRegister syb where syb.idCard=?1")
	public SYBRegister getSYBRegisterByIdCard(@Param("idCard") String idCard);
}
