package com.jh.user.moudle.cardloans;

import java.util.List;

public interface ICardLoansRatioPORepository extends IBaseRepository<CardLoansRatioPO, Long> {

	List<CardLoansRatioPO> findByBrandId(String brandId);

	CardLoansRatioPO findRatiosByBrandIdAndPreGrade(String brandId, String preGrade);

}
