package com.jh.user.business;

import com.jh.user.pojo.TUpgradeOrderViewEntity;
import org.springframework.stereotype.Service;

import java.util.List;


public interface TUpgradeOrderViewBusiness {

    public List<TUpgradeOrderViewEntity> queryAll();

    public List<TUpgradeOrderViewEntity> queryBrandAll(Long brandid);
}
