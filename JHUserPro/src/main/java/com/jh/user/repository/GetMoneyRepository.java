package com.jh.user.repository;


import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.jh.user.pojo.GetMoney;

@Repository
public interface GetMoneyRepository  extends JpaRepository<GetMoney,String>,JpaSpecificationExecutor<GetMoney>{

	
	
	@Query(value = "select * from t_get_money gm where gm.brand_id=:brandId and gm.status='1' order by gm.create_time desc limit 0,20", nativeQuery = true)
	public List<GetMoney>  getGetMoneyByBrandId(@Param("brandId")String brandId);
	
	@Query("select gm from  GetMoney gm where gm.brandId=:brandId and gm.status='1'")
	Page<GetMoney> getGetMoneyByBrandIdAndPage(@Param("brandId")String brandId, Pageable pageAble);
	
	@Query("select gm from  GetMoney gm where gm.brandId=:brandId and gm.id in (:id)")
	public List<GetMoney>  getGetMoneyByBrandIdAndId(@Param("brandId")String brandId, @Param("id")long[] id);
	
}
