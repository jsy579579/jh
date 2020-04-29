package com.jh.paymentchannel.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.jh.paymentchannel.pojo.WMYKNewChooseCity;





@Repository
public interface WMYKNewChooseCityRepository extends JpaRepository<WMYKNewChooseCity, String>, JpaSpecificationExecutor<WMYKNewChooseCity>{
	
	@Query("select wmyk from WMYKNewChooseCity wmyk where wmyk.bankCard=:bankCard")
	public WMYKNewChooseCity getWMYKNewChooseCityByBankCard(@Param("bankCard") String bankCard);
	
}
