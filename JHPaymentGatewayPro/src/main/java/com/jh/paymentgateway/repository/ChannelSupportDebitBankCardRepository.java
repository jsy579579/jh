package com.jh.paymentgateway.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.jh.paymentgateway.pojo.ChannelSupportDebitBankCard;

@Repository
public interface ChannelSupportDebitBankCardRepository extends JpaRepository<ChannelSupportDebitBankCard, String>, JpaSpecificationExecutor<ChannelSupportDebitBankCard>{
	
	@Query("select csdb from ChannelSupportDebitBankCard csdb where csdb.channelTag=:channelTag and csdb.bankName like %:bankName%")
	public ChannelSupportDebitBankCard getChannelSupportDebitBankCardByChannelTagAndBankName(@Param("channelTag") String channelTag, @Param("bankName") String bankName);
	
	@Query("select csdb.bankAbbr from ChannelSupportDebitBankCard csdb where csdb.channelTag=:channelTag")
	public List<String> getChannelSupportDebitBankCardByChannelTag(@Param("channelTag") String channelTag);
	
	
}
