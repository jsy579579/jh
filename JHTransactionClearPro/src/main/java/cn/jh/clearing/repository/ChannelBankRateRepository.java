package cn.jh.clearing.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import cn.jh.clearing.pojo.ChannelBankRate;

@Repository
public interface ChannelBankRateRepository extends JpaRepository<ChannelBankRate,String>,JpaSpecificationExecutor<ChannelBankRate>{

	@Query("select cb from ChannelBankRate cb where cb.channelTag=:channelTag")
	List<ChannelBankRate> getChannelBankRateByChannelTag(@Param("channelTag") String channelTag);
	
	@Query("select cb from ChannelBankRate cb where cb.channelTag=:channelTag and cb.bankName like %:bankName%")
	ChannelBankRate getChannelBankRateByChannelTagAndBankName(@Param("channelTag") String channelTag, @Param("bankName") String bankName);
	
}
