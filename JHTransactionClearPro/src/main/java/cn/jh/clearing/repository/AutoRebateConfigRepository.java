package cn.jh.clearing.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import cn.jh.clearing.pojo.AutoRebateConfig;
@Repository
public interface AutoRebateConfigRepository extends JpaRepository<AutoRebateConfig, Long>,JpaSpecificationExecutor<AutoRebateConfig>{

	List<AutoRebateConfig> findByOnOff(int onOff);

	List<AutoRebateConfig> findByBrandId(long brandId);

	List<AutoRebateConfig> findByBrandIdAndOnOff(long brandId, int onOff);
	
}
