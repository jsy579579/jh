package com.jh.user.repository;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.jh.user.pojo.BrandRate;
import com.jh.user.pojo.Channel;
import com.jh.user.pojo.ChannelRate;

@Repository
public interface ChannelRateRepository extends JpaRepository<ChannelRate,Long>,JpaSpecificationExecutor<ChannelRate>{

	
	@Query("select channelRate  from ChannelRate channelRate  where channelRate.userId=:user_id and channelRate.brandId=:brand_id and channelRate.channelId=:channel_id")
	ChannelRate findChannelRateByUserid(@Param("user_id") long userid, @Param("brand_id") long brandid, @Param("channel_id") long channelid);

	@Query("select channelRate  from ChannelRate channelRate  where  channelRate.brandId=:brand_id and channelRate.channelId=:channel_id")
	 List<ChannelRate>  findChannelRateByBrandid(@Param("brand_id") long brandid, @Param("channel_id") long channelid);
	
	@Modifying
	@Query("delete from ChannelRate channelRate  where channelRate.userId=:user_id and channelRate.brandId=:brand_id and channelRate.channelId=:channel_id")
	void delChannelRateByUserid(@Param("user_id") long userid, @Param("brand_id") long brandid, @Param("channel_id") long channelid);
	
    @Modifying
	@Query("update ChannelRate channelRate set rate=:minrate where channelRate.brandId=:brandid  and channelRate.channelId=:channelid and channelRate.userId=:userid")
	void updChannelRateBybrandidAndChannelidanduserId(@Param("minrate") BigDecimal minrate,@Param("brandid") long brandid, @Param("channelid") long channelid, @Param("userid") long userid);  

    //根据userid注销用户该服务记录
    @Modifying
    @Query("delete from ChannelRate channelRate where channelRate.userId=:userid")
    void delChannelRateByUserid(@Param("userid") long userid);
    // lx 根据channelid查询rate
    @Query("select channelRate  from ChannelRate channelRate  where channelRate.channelId=:channel_id and channelRate.rate < :costRate")
    List<ChannelRate> findChannelRateByChannelId(@Param("channel_id")long channelid ,@Param("costRate")BigDecimal costRate);

   

}
