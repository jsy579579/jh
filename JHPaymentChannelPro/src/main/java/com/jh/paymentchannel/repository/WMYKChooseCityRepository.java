package com.jh.paymentchannel.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.jh.paymentchannel.pojo.WMYKChooseCity;





@Repository
public interface WMYKChooseCityRepository extends JpaRepository<WMYKChooseCity, String>, JpaSpecificationExecutor<WMYKChooseCity>{
	
	@Query("select wmyk from WMYKChooseCity wmyk where wmyk.bankCard=:bankCard")
	public WMYKChooseCity getWMYKChooseCityByBankCard(@Param("bankCard") String bankCard);
	
}
