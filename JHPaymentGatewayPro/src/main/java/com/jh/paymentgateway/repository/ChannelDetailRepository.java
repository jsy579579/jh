package com.jh.paymentgateway.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import com.jh.paymentgateway.pojo.ChannelDetail;

@Repository
public interface ChannelDetailRepository extends JpaRepository<ChannelDetail, Long>,JpaSpecificationExecutor<ChannelDetail> {

	ChannelDetail findByChannelTag(String channelTag);
	
	List<ChannelDetail>  findByChannelType(String channelType);

}
