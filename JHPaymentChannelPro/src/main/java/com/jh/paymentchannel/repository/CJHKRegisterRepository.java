package com.jh.paymentchannel.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.jh.paymentchannel.pojo.CJHKRegister;





@Repository
public interface CJHKRegisterRepository extends JpaRepository<CJHKRegister, String>, JpaSpecificationExecutor<CJHKRegister>{
	
	@Query("select cjhk from CJHKRegister cjhk where cjhk.bankCard=:bankCard")
	public CJHKRegister getCJHKRegisterByBankCard(@Param("bankCard") String bankCard);
	
	@Query("select cjhk from CJHKRegister cjhk where cjhk.idCard=:idCard")
	public CJHKRegister getCJHKRegisterByIdCard(@Param("idCard") String idCard);
	
	@Query("select cjhk from CJHKRegister cjhk where cjhk.idCard=:idCard and cjhk.version=:version")
	public CJHKRegister getCJHKRegisterByIdCardAndVersion(@Param("idCard") String idCard, @Param("version") String version);
	
	@Query("select cjhk from CJHKRegister cjhk where cjhk.idCard=:idCard and cjhk.versions=:versions")
	public CJHKRegister getCJHKRegisterByIdCardAndVersions(@Param("idCard") String idCard, @Param("versions") String versions);
	
}
