package cn.jh.clearing.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import cn.jh.clearing.pojo.ChannelTag;
@Repository
public interface ChannelTagRepository extends JpaRepository<ChannelTag, Long>,JpaSpecificationExecutor<ChannelTag>{

	@Query("select ct.channelTag from  ChannelTag ct")
	List<String> getChannelTag();

}
