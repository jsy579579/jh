package cn.jh.clearing.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import cn.jh.clearing.pojo.BrandNotifyConfig;

@Repository
public interface BrandNotifyConfigRepository extends JpaRepository<BrandNotifyConfig, Long>, JpaSpecificationExecutor<BrandNotifyConfig> {

	BrandNotifyConfig getBrandNotifyConfigByBrandId(int brandId);

}
