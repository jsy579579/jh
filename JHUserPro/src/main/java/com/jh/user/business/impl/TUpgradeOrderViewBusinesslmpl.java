package com.jh.user.business.impl;

import com.jh.user.business.TUpgradeOrderViewBusiness;
import com.jh.user.pojo.TUpgradeOrderViewEntity;
import com.jh.user.repository.TUpgradeOrderViewRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TUpgradeOrderViewBusinesslmpl implements TUpgradeOrderViewBusiness {
    @Autowired
    TUpgradeOrderViewRepository tUpgradeOrderViewRepository;
    @Override
    public List<TUpgradeOrderViewEntity> queryAll() {
        return tUpgradeOrderViewRepository.findAll();
    }

    @Override
    public List<TUpgradeOrderViewEntity> queryBrandAll(Long brandid) {
        return tUpgradeOrderViewRepository.queryBrandId(brandid);
    }


}
