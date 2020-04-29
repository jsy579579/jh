package com.jh.paymentgateway.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.jh.paymentgateway.pojo.CJHKLRRegister;

/**
 * @author 作者:lirui
 * @version 创建时间：2019年5月27日 下午5:52:32 类说明
 */
@Repository
public interface CJHKLRRegisterRepository
		extends JpaRepository<CJHKLRRegister, String>, JpaSpecificationExecutor<CJHKLRRegister> {
	@Query("select cj from CJHKLRRegister cj where cj.idCard=:idCard")
	CJHKLRRegister getCJHKLRRegisterByIdCard(@Param("idCard")String idCard);
}
