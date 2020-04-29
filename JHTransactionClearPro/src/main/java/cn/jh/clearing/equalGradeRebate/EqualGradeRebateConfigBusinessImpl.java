package cn.jh.clearing.equalGradeRebate;

import java.util.List;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class EqualGradeRebateConfigBusinessImpl implements EqualGradeRebateConfigBusiness {
	
	@Autowired
	private EqualGradeRebateConfigRepository equalGradeRebateConfigRepository;

	@Override
	public List<EqualGradeRebateConfig> findByBrandId(String brandId) {
		return equalGradeRebateConfigRepository.findByBrandId(brandId);
	}

	@Override
	public EqualGradeRebateConfig findByBrandIdAndGrade(String brandId, Integer grade) {
		return equalGradeRebateConfigRepository.findByBrandIdAndGrade(brandId, grade);
	}

	@Override
	@Transactional
	public EqualGradeRebateConfig save(EqualGradeRebateConfig equalGradeRebateConfig) {
		return equalGradeRebateConfigRepository.saveAndFlush(equalGradeRebateConfig);
	}

	@Override
	@Transactional
	public void delete(EqualGradeRebateConfig equalGradeRebateConfig) {
		equalGradeRebateConfigRepository.delete(equalGradeRebateConfig);
	}
	
}
