package com.jh.user.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.jh.user.pojo.Channel;

@Repository
public interface ChannelRepository extends JpaRepository<Channel,String>,JpaSpecificationExecutor<Channel>{

	
	@Query("select channel from  Channel channel where channel.channelTag=:tag ")
	Channel findChannelByTag(@Param("tag") String tag);

	
	@Query("select channel from  Channel channel where channel.id=:id ")
	Channel findChannelById(@Param("id") long id);
	
	@Query("select channel from Channel channel where channel.channelNo=:channelNo")
	List<Channel> findChannelByChannelNo(@Param("channelNo") String channelNo);

	@Query("select channel.channelTag from Channel channel where channel.channelNo=:channelNo")
	List<String> findChannelTagByChannelNo(@Param("channelNo") String channelNo);
	
	Channel findById(long channelId);
	
	@Query("select channel from Channel channel where channel.channelNo=:channelNo and channel.autoclearing=:autoclearing and channel.status=:status and channel.paymentStatus=:paymentStatus")
	List<Channel> findChannelByChannelNoAndStatusAndPaymentStatus(@Param("channelNo") String channelNo,@Param("autoclearing") String autoclearing, @Param("status") String status, @Param("paymentStatus") String paymentStatus);
	
}
