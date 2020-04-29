package com.jh.paymentgateway.repository.tldhx;

import com.jh.paymentgateway.pojo.tldhx.TLDHXHHRegister;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface TLDHXHHRegisterRepository extends JpaRepository<TLDHXHHRegister, String>, JpaSpecificationExecutor<TLDHXHHRegister> {
	@Query("select tl from TLDHXHHRegister tl where tl.idCard=:idCard")
	public TLDHXHHRegister getTLDHXRegisterByIdCard(@Param("idCard") String idCard);

}
