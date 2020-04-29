package com.jh.paymentchannel.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.jh.paymentchannel.pojo.AlipayLifeNo;
import com.jh.paymentchannel.pojo.SwiftBrandMerchant;

@Repository
public interface AlipayLifeRepository  extends JpaRepository<AlipayLifeNo,String>,JpaSpecificationExecutor<AlipayLifeNo>{

	
	@Query("select alipaylifeno from  AlipayLifeNo alipaylifeno where alipaylifeno.appid =:appid")
	public AlipayLifeNo	getAlipayLifeNoByAppid(@Param("appid") String appid);
	
	
}
