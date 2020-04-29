package com.jh.paymentchannel.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.jh.paymentchannel.pojo.ChannelDetail;

@Repository
public interface ChannelDetailRepository extends JpaRepository<ChannelDetail,String>,JpaSpecificationExecutor<ChannelDetail>{

	@Query("select channelDetail from  ChannelDetail channelDetail where channelDetail.channelTag=:channeltag")
	public ChannelDetail	getChannelDetailByTag(@Param("channeltag") String channeltag);
	
	@Query("select channelDetail from  ChannelDetail channelDetail where channelDetail.channelNo=:channelno")
	public List<ChannelDetail>	getChannelDetailByNo(@Param("channelno") String channelNo);
	
	@Query("select channelDetail from  ChannelDetail channelDetail ")
	public List<ChannelDetail>	getChannelDetail();
		
}
