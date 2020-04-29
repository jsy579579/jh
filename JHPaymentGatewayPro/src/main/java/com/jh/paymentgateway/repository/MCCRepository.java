package com.jh.paymentgateway.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.jh.paymentgateway.pojo.MCCpo;


@Repository
public interface MCCRepository extends JpaRepository<MCCpo, String>, JpaSpecificationExecutor<MCCpo>{
	
	@Query("select mcc from MCCpo mcc")
	public List<MCCpo> getMCCpo();
	
	@Query("select mcc from MCCpo mcc where mcc.type=:type")
	public MCCpo getMCCpoByType(@Param("type") String type);
	
}
