package com.jh.paymentchannel.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.jh.paymentchannel.pojo.TopupPayChannelRoute;

@Repository
public interface TopupPayChannelRepository extends JpaRepository<TopupPayChannelRoute,String>,JpaSpecificationExecutor<TopupPayChannelRoute>{ 

	
	@Query("select topupPayChannelRoute from  TopupPayChannelRoute topupPayChannelRoute where topupPayChannelRoute.brandcode=:brandcode  and topupPayChannelRoute.type =:channeltype and topupPayChannelRoute.channelcode =:channelcode")
	public TopupPayChannelRoute	getTopupChannelRoute(@Param("brandcode") String brandcode, @Param("channeltype") String channeltype,  @Param("channelcode") String channeltag);
	
	
	@Query("select topupPayChannelRoute from  TopupPayChannelRoute topupPayChannelRoute where topupPayChannelRoute.brandcode=:brandcode  and topupPayChannelRoute.type =:type")
	public List<TopupPayChannelRoute>	getPayChannelRoute(@Param("brandcode") String brandcode ,@Param("type") String type );
	
	@Query("select topupPayChannelRoute from  TopupPayChannelRoute topupPayChannelRoute where topupPayChannelRoute.brandcode=:brandcode  and topupPayChannelRoute.channelcode =:channelcode")
	public List<TopupPayChannelRoute>	getPayChannelRouteChannelTag(@Param("brandcode") String brandcode ,@Param("channelcode") String channelcode );
	
	
	
	@Query("select topupPayChannelRoute from  TopupPayChannelRoute topupPayChannelRoute where topupPayChannelRoute.channelcode=:channelcode  and topupPayChannelRoute.type =:type")
	public List<TopupPayChannelRoute>	getPayChannelRouteBychannelcode(@Param("channelcode") String channelcode ,@Param("type") String type );
	
	@Query("select topupPayChannelRoute from  TopupPayChannelRoute topupPayChannelRoute where  topupPayChannelRoute.type =:type")
	public List<TopupPayChannelRoute>	getPayChannelRoute(@Param("type") String type );
	
	@Query("select topupPayChannelRoute from  TopupPayChannelRoute topupPayChannelRoute where  topupPayChannelRoute.channelcode =:channelcode")
	public List<TopupPayChannelRoute>	getPayChannelRouteBychannelTag(@Param("channelcode") String channelcode );
	
	@Query("select topupPayChannelRoute from  TopupPayChannelRoute topupPayChannelRoute where  topupPayChannelRoute.brandcode=:brandcode")
	public List<TopupPayChannelRoute>	getPayChannelRouteBybrandcode(@Param("brandcode") String brandcode );
	
	@Query("select topupPayChannelRoute from  TopupPayChannelRoute topupPayChannelRoute ")
	public List<TopupPayChannelRoute>	getPayChannelRoute( );
	
	
}
