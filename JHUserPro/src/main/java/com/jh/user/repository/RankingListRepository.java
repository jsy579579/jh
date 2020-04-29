package com.jh.user.repository;


import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.jh.user.pojo.RankingList;

@Repository
public interface RankingListRepository extends JpaRepository<RankingList,String>,JpaSpecificationExecutor<RankingList>{

	//积分排行
	@Query("select rankingList from  RankingList rankingList where rankingList.brandId=:brandId order by rankingList.cion desc")
	List<RankingList> queryRankingListCoin(@Param("brandId") long brandId);
	
	
	//收益排行
	@Query("select rankingList from  RankingList rankingList where rankingList.brandId=:brandId order by rankingList.earnings desc")
	List<RankingList> queryRankingListEarnings(@Param("brandId") long brandId);
	
	/*****查询指定用户****/
	
	@Query("select rankingList from  RankingList rankingList where rankingList.id=:id ")
	RankingList queryRankingListByid(@Param("id") long id);
	
}
