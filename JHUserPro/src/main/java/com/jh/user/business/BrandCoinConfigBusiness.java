package com.jh.user.business;

import com.jh.user.pojo.BrandCoinConfig;
import com.jh.user.pojo.BrandCoinGradeConfig;

public interface BrandCoinConfigBusiness {


    BrandCoinConfig findByBrandIdAndGradeAndStatus(Long brandId, int grade, int status);


    BrandCoinGradeConfig findBrandGradeByGradeAndBrandId(Long brandId, int grade);

    BrandCoinGradeConfig findBrandGradeByBrandId(Long brandId);

    BrandCoinGradeConfig saveBrandCoinGradeConfig(BrandCoinGradeConfig brandCoinGradeConfig);
}
