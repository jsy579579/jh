package cn.jh.clearing.equalGradeRebate;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface EqualGradeRebateConfigRepository extends JpaRepository<EqualGradeRebateConfig, Long>, JpaSpecificationExecutor<EqualGradeRebateConfig> {

	List<EqualGradeRebateConfig> findByBrandId(String brandId);

	EqualGradeRebateConfig findByBrandIdAndGrade(String brandId, Integer grade);

}
