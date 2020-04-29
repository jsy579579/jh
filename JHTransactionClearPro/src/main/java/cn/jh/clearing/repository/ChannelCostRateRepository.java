package cn.jh.clearing.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import cn.jh.clearing.pojo.ChannelCostRate;

@Repository
public interface ChannelCostRateRepository extends JpaRepository<ChannelCostRate,String>,JpaSpecificationExecutor<ChannelCostRate>{

	ChannelCostRate getChannelCostRateByChannelTag(String channelTag);

	@Query("select cc from ChannelCostRate cc")
	List<ChannelCostRate> getAllChannelCostRate();
	
}
