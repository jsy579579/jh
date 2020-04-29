package com.jh.paymentchannel.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.jh.paymentchannel.pojo.Branchbank;
import com.jh.paymentchannel.pojo.ChannelSupportBank;

@Repository
public interface ChannelSupportBankRepository extends JpaRepository<ChannelSupportBank,String>,JpaSpecificationExecutor<ChannelSupportBank>{
	//以通道标识和支持银行卡的名字去查询，该通道是否支持
	@Query("select channelsupportbank from ChannelSupportBank channelsupportbank where channelsupportbank.channelTag=:channelTag and channelsupportbank.supportBankName like %:supportBankName%")
	public ChannelSupportBank querySupportBankByTagAndNameAndType(@Param("channelTag")String channelTag,@Param("supportBankName")String supprortBankName); 
	//以通道标识去查询所有该通道支持的银行卡列表
	@Query("select channelsupportbank from ChannelSupportBank channelsupportbank where channelsupportbank.channelTag=:channelTag")
	public List<ChannelSupportBank> querySupportBankByTag(@Param("channelTag")String channelTag);
	
	@Query("select csb from ChannelSupportBank csb where csb.supportBankName like %:supportBankName% and csb.supportBankType=:supportBankType and csb.channelTag in :channelTag")
	public List<ChannelSupportBank> querySupportBankByName(@Param("supportBankName")String supportBankName, @Param("supportBankType")String supportBankType, @Param("channelTag")String[] channelTag);
	
	@Query("select channelsupportbank from ChannelSupportBank channelsupportbank where channelsupportbank.channelTag=:channelTag and channelsupportbank.supportBankType=:supportBankType and channelsupportbank.supportBankName like %:supportBankName%")
	public ChannelSupportBank getSupportBankByTagAndNameAndType(@Param("channelTag")String channelTag,@Param("supportBankName")String supportBankName,@Param("supportBankType")String supportBankType); 
	
}
 