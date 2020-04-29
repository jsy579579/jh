package com.jh.user.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.jh.user.pojo.ThirdLevelRate;

@Repository
public interface ThirdLevelRateRepository  extends JpaRepository<ThirdLevelRate,String>,JpaSpecificationExecutor<ThirdLevelRate>{

	
	@Query("select thirdlevelrate from  ThirdLevelRate thirdlevelrate where thirdlevelrate.thirdLevelId =:thirdlevelid")
	List<ThirdLevelRate> findAllThirdLevelRatesBylevelid(@Param("thirdlevelid") long thirdlevelid);
	
	@Query("select thirdlevelrate from  ThirdLevelRate thirdlevelrate where thirdlevelrate.thirdLevelId =:thirdlevelid and thirdlevelrate.channelId=:channelId ")
	ThirdLevelRate findAllThirdLevelRatesBylevelidAndChannelId(@Param("thirdlevelid") long thirdlevelid,@Param("channelId")long channelId);
	
	@Query("from  ThirdLevelRate thirdlevelrate where thirdlevelrate.thirdLevelId in (:thirdlevel) and thirdlevelrate.channelId=:channelId")
	List<ThirdLevelRate> queryThirdLevelRatesBylevelidAndChannelId(@Param("thirdlevel") long[] thirdlevel, @Param("channelId") long channelId);
	
	@Modifying
	@Query("delete from ThirdLevelRate thirdlevelrate where thirdlevelrate.thirdLevelId in (:thirdlevel) and thirdlevelrate.channelId=:channelId")
	void deleteThirdLevelThirdLevelRatesBylevelidAndChannelId(@Param("thirdlevel") long[] thirdlevel, @Param("channelId") long channelId);
}
