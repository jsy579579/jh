package com.jh.user.business.impl;

import com.jh.user.business.BrandCoinConfigBusiness;
import com.jh.user.pojo.BrandCoinConfig;
import com.jh.user.pojo.BrandCoinGradeConfig;
import com.jh.user.repository.BrandCoinConfigRepository;
import com.jh.user.repository.BrandCoinGradeConfigRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;

@Service
public class BrandCoinConfigBusinessImpl implements BrandCoinConfigBusiness {

    @Autowired
    private BrandCoinConfigRepository brandCoinConfigRepository;

    @Autowired
    private BrandCoinGradeConfigRepository brandCoinGradeConfigRepository;

    @Autowired
    private EntityManager em;

    @Override
    public BrandCoinConfig findByBrandIdAndGradeAndStatus(Long brandId, int grade, int status) {
        return brandCoinConfigRepository.findByBrandIdAndGradeAndStatus(brandId,grade,status);
    }

    @Override
    public BrandCoinGradeConfig findBrandGradeByGradeAndBrandId(Long brandId, int grade) {
        return brandCoinGradeConfigRepository.findBrandGradeByGradeAndBrandId(brandId,grade);
    }

    @Override
    public BrandCoinGradeConfig findBrandGradeByBrandId(Long brandId) {
        return brandCoinGradeConfigRepository.findBrandGradeByBrandId(brandId);
    }

    @Transactional
    @Override
    public BrandCoinGradeConfig saveBrandCoinGradeConfig(BrandCoinGradeConfig brandCoinGradeConfig) {
        em.clear();
        BrandCoinGradeConfig result=brandCoinGradeConfigRepository.save(brandCoinGradeConfig);
        em.flush();
        return result;
    }


}
