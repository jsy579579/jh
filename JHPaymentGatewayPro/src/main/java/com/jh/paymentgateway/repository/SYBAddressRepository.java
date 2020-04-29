package com.jh.paymentgateway.repository;

import com.jh.paymentgateway.pojo.HZDHAddress;
import com.jh.paymentgateway.pojo.SYBAddress;
import com.jh.paymentgateway.pojo.TYTRegister;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SYBAddressRepository extends JpaRepository<SYBAddress, String>, JpaSpecificationExecutor<SYBAddress> {

	@Query("select syb from SYBAddress syb where syb.level = '2'")
	public List<SYBAddress> findAllprovice();

	@Query("select syb from SYBAddress syb where syb.province = ?1")
	public List<SYBAddress> findarea(@Param("province") String province);
}
