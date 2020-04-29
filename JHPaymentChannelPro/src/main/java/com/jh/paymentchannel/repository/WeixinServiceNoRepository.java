package com.jh.paymentchannel.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.jh.paymentchannel.pojo.WeixinServiceNo;

@Repository
public interface WeixinServiceNoRepository extends JpaRepository<WeixinServiceNo,String>,JpaSpecificationExecutor<WeixinServiceNo>{

	@Query("select weixinno from  WeixinServiceNo weixinno where weixinno.appid =:appid")
	public WeixinServiceNo	getWeixinServiceNoByAppid(@Param("appid") String appid);
	
	
}
