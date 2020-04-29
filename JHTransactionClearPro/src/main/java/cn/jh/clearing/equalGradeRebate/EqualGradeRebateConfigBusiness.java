package cn.jh.clearing.equalGradeRebate;

import java.util.List;

public interface EqualGradeRebateConfigBusiness {

	List<EqualGradeRebateConfig> findByBrandId(String brandId);

	EqualGradeRebateConfig findByBrandIdAndGrade(String brandId, Integer grade);

	EqualGradeRebateConfig save(EqualGradeRebateConfig equalGradeRebateConfig);

	void delete(EqualGradeRebateConfig equalGradeRebateConfig);

}
