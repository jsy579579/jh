package com.jh.paymentgateway.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.jh.paymentgateway.li.YiBao;

@Repository
public interface YiBaoRepository extends JpaRepository<YiBao, String>,JpaSpecificationExecutor<YiBao>{
    @Query("select yi from YiBao yi where yi.memberNo=:memberNo")
	public YiBao getYiBaoBymemberNo(@Param("memberNo")String memberNo);
    
}
