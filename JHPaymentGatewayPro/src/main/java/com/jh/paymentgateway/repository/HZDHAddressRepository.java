package com.jh.paymentgateway.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.jh.paymentgateway.pojo.HZDHAddress;

@Repository
public interface HZDHAddressRepository extends JpaRepository<HZDHAddress, String>, JpaSpecificationExecutor<HZDHAddress> {
	
	@Query("select hzdh from HZDHAddress hzdh where hzdh.id=:areaId")
	public HZDHAddress getHZDHXAddress(@Param("areaId") Long areaId);

	@Query("select hzdh.mctAdd from HZDHAddress hzdh GROUP BY hzdh.mctAdd")
	public List<HZDHAddress> findHZDHProvince();

	@Query("select hzdh from HZDHAddress hzdh where hzdh.mctAdd=:provinceName")
	public List<HZDHAddress> findHZDHMerchant(@Param("provinceName") String  provinceName);

	

}
