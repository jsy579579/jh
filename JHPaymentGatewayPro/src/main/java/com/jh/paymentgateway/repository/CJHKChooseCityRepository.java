package com.jh.paymentgateway.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.jh.paymentgateway.pojo.CJHKFactory;

@Repository
public interface CJHKChooseCityRepository
		extends JpaRepository<CJHKFactory, String>, JpaSpecificationExecutor<CJHKFactory> {
	@Query("select cj from CJHKFactory cj where cj.area =:name")
	public List<CJHKFactory> CJHKChooseCityByName(@Param("name") String name);
}
